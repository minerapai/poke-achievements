package kr.pokeachv;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.CommandSource;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server side achievements: reaching one (pokedex, gyms and leagues, catches, shinies, eggs, battles, raids) gives
 * items for raising competitive pokemon, CobbleDollars and a chat title. Checked every few seconds for online players;
 * a player's first check also grants everything already reached. /achv lists them and picks the title.
 * Messages are plain Korean text since clients do not have this mod.
 */
public class PokeAchievements implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("pokeachv");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final int CHECK_INTERVAL_TICKS = 100;
    private static final int PAGE_SIZE = 9;

    /** Chosen title per online player, read by PlayerDisplayNameMixin for chat. */
    private static final Map<UUID, String> TITLES = new ConcurrentHashMap<>();

    /** Every achievement of the config, and those this server can reach (worked out once RCT has its data). */
    private static List<Achievement> configured = List.of();
    private static List<Achievement> achievements;
    private static PlayerRecords records;

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            setAchievements(loadConfig());
            records = PlayerRecords.load(server.getSavePath(WorldSavePath.ROOT).resolve("pokeachv-records.json"));
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            if (records != null) {
                records.save();
            }
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (records != null && server.getTicks() % CHECK_INTERVAL_TICKS == 0) {
                server.getPlayerManager().getPlayerList().forEach(PokeAchievements::check);
            }
        });
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            dispatcher.register(CommandManager.literal("achv")
                .executes(ctx -> AchievementMenu.open(ctx.getSource().getPlayerOrThrow(), 0))
                .then(CommandManager.literal("list")
                    .executes(ctx -> list(ctx.getSource().getPlayerOrThrow(), 1))
                    .then(CommandManager.argument("page", IntegerArgumentType.integer(1))
                        .executes(ctx -> list(ctx.getSource().getPlayerOrThrow(), IntegerArgumentType.getInteger(ctx, "page")))))
                .then(CommandManager.literal("title")
                    .then(CommandManager.literal("off")
                        .executes(ctx -> setTitle(ctx.getSource().getPlayerOrThrow(), null)))
                    .then(CommandManager.argument("id", StringArgumentType.word())
                        .suggests((ctx, builder) -> CommandSource.suggestMatching(
                            achievements().stream().map(Achievement::id), builder))
                        .executes(ctx -> setTitle(ctx.getSource().getPlayerOrThrow(),
                            StringArgumentType.getString(ctx, "id")))))
                .then(CommandManager.literal("reload")
                    .requires(source -> source.hasPermissionLevel(2))
                    .executes(ctx -> {
                        setAchievements(loadConfig());
                        ctx.getSource().sendFeedback(() -> Text.literal("업적 설정을 다시 읽었습니다: " + achievements().size() + "개"), true);
                        return 1;
                    }))));
    }

    public static String titleOf(UUID player) {
        return TITLES.get(player);
    }

    // config

    private static List<Achievement> loadConfig() {
        Path file = FabricLoader.getInstance().getConfigDir().resolve("poke-achievements.json");

        try {
            if (Files.notExists(file)) {
                try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                    GSON.toJson(Achievement.defaults(), writer);
                }
            }

            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                List<Achievement> read = GSON.fromJson(reader, new TypeToken<List<Achievement>>() { }.getType());

                for (Achievement achievement : read) {
                    achievement.items().forEach(PokeAchievements::parseItem); // logs unknown items once
                }

                LOGGER.info("Loaded {} achievements", read.size());
                return List.copyOf(read);
            }
        } catch (IOException | RuntimeException e) {
            LOGGER.error("Could not read {}, using the defaults", file, e);
            return Achievement.defaults();
        }
    }

    /** "namespace:item count" -> stack, empty when the item does not exist. */
    private static Optional<ItemStack> parseItem(String entry) {
        String[] parts = entry.trim().split("\\s+");
        Identifier id = Identifier.tryParse(parts[0]);
        Optional<Item> item = id != null ? Registries.ITEM.getOrEmpty(id) : Optional.empty();

        if (item.isEmpty()) {
            LOGGER.warn("Unknown reward item '{}', skipped", entry);
            return Optional.empty();
        }

        int count = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;
        return Optional.of(new ItemStack(item.get(), count));
    }

    // checking and rewards

    private static void check(ServerPlayerEntity player) {
        PlayerRecords.Record record = records.get(player.getUuid());
        boolean first = record == null;

        if (first) {
            record = records.create(player.getUuid());
        }

        record.name = player.getGameProfile().getName();
        Conditions.Dex dex = Conditions.Dex.of(player);
        List<Achievement> reached = new ArrayList<>();

        for (Achievement achievement : achievements()) {
            if (!record.done.contains(achievement.id()) && Conditions.progress(player, achievement, dex) >= achievement.target()) {
                reached.add(achievement);
            }
        }

        if (!reached.isEmpty()) {
            for (Achievement achievement : reached) {
                record.done.add(achievement.id());
                reward(player, achievement, !first);
            }

            if (record.title.isEmpty()) {
                record.title = reached.get(reached.size() - 1).id();
                player.sendMessage(Text.literal("칭호가 [" + reached.get(reached.size() - 1).title()
                    + "] 로 설정되었습니다. /achv 에서 바꿀 수 있습니다.").formatted(Formatting.GRAY));
            }

            if (first) { // everything reached before the mod was installed, in one message
                player.sendMessage(Text.literal("지금까지의 기록으로 업적 " + reached.size() + "개를 달성해 보상을 받았습니다! /achv 로 확인하세요.")
                    .formatted(Formatting.GOLD));
                broadcast(player.getServer(), Text.literal(record.name + "님이 업적 " + reached.size() + "개를 달성했습니다!")
                    .formatted(Formatting.GOLD));
            }

            records.save();
        }

        updateTitle(player, record);
    }

    private static void reward(ServerPlayerEntity player, Achievement achievement, boolean announce) {
        for (String entry : achievement.items()) {
            parseItem(entry).ifPresent(stack -> {
                if (!player.getInventory().insertStack(stack)) {
                    player.dropItem(stack, false); // inventory full: at the player's feet
                }
            });
        }

        if (achievement.money() > 0) {
            MinecraftServer server = player.getServer();
            server.getCommandManager().executeWithPrefix(server.getCommandSource().withSilent(),
                "cobbledollars give " + player.getGameProfile().getName() + " " + achievement.money());
        }

        LOGGER.info("{} reached achievement {}", player.getGameProfile().getName(), achievement.id());

        if (announce) {
            broadcast(player.getServer(), Text.literal(player.getGameProfile().getName() + "님이 업적 ")
                .formatted(Formatting.GOLD)
                .append(Text.literal("[" + achievement.name() + "]").styled(style -> style.withColor(Formatting.YELLOW)
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, rewardText(achievement)))))
                .append(Text.literal("을(를) 달성했습니다!").formatted(Formatting.GOLD)));
        }
    }

    private static void broadcast(MinecraftServer server, Text text) {
        server.getPlayerManager().broadcast(text, false);
    }

    // titles

    private static void updateTitle(ServerPlayerEntity player, PlayerRecords.Record record) {
        Optional<Achievement> title = find(record.title).filter(a -> record.done.contains(a.id()));

        if (title.isPresent()) {
            TITLES.put(player.getUuid(), title.get().title());
        } else {
            TITLES.remove(player.getUuid());
        }
    }

    static int setTitle(ServerPlayerEntity player, String id) {
        PlayerRecords.Record record = records.create(player.getUuid());

        if (id == null) {
            record.title = "none";
            player.sendMessage(Text.literal("칭호를 껐습니다.").formatted(Formatting.GRAY));
        } else {
            Optional<Achievement> achievement = find(id);

            if (achievement.isEmpty() || !record.done.contains(id)) {
                player.sendMessage(Text.literal("달성한 업적의 칭호만 쓸 수 있습니다.").formatted(Formatting.RED));
                return 0;
            }

            record.title = id;
            player.sendMessage(Text.literal("칭호를 [" + achievement.get().title() + "] 로 바꿨습니다.").formatted(Formatting.GREEN));
        }

        records.save();
        updateTitle(player, record);
        return 1;
    }

    private static Optional<Achievement> find(String id) {
        return achievements().stream().filter(a -> a.id().equals(id)).findFirst();
    }

    // listing

    private static int list(ServerPlayerEntity player, int page) {
        PlayerRecords.Record record = records.create(player.getUuid());
        Conditions.Dex dex = Conditions.Dex.of(player);
        int pages = Math.max(1, (achievements().size() + PAGE_SIZE - 1) / PAGE_SIZE);
        int shown = Math.min(page, pages);
        long done = achievements().stream().filter(a -> record.done.contains(a.id())).count();

        player.sendMessage(Text.literal("===== 업적 " + done + "/" + achievements().size() + " (" + shown + "/" + pages + "쪽) =====")
            .formatted(Formatting.GOLD));

        for (Achievement achievement : achievements().subList((shown - 1) * PAGE_SIZE,
            Math.min(achievements().size(), shown * PAGE_SIZE))) {
            boolean reached = record.done.contains(achievement.id());
            MutableText line = Text.literal(reached ? "✔ " : "✘ ").formatted(reached ? Formatting.GREEN : Formatting.DARK_GRAY)
                .append(Text.literal(achievement.name()).formatted(reached ? Formatting.YELLOW : Formatting.GRAY))
                .append(Text.literal(" - " + achievement.description()).formatted(Formatting.GRAY));

            if (!reached) {
                long progress = Math.min(Conditions.progress(player, achievement, dex), achievement.target());
                line.append(Text.literal(" (" + progress + "/" + achievement.target() + ")").formatted(Formatting.DARK_AQUA));
            } else if (achievement.id().equals(record.title)) {
                line.append(Text.literal(" [사용 중인 칭호]").formatted(Formatting.AQUA));
            } else {
                line.append(Text.literal(" [칭호로 설정]").formatted(Formatting.AQUA, Formatting.UNDERLINE)
                    .styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                        "/achv title " + achievement.id()))));
            }

            player.sendMessage(line.styled(style -> style.withHoverEvent(
                new HoverEvent(HoverEvent.Action.SHOW_TEXT, rewardText(achievement)))));
        }

        MutableText footer = Text.literal("마우스를 올리면 보상이 보입니다. ").formatted(Formatting.GRAY);

        if (shown < pages) {
            footer.append(Text.literal("[다음 쪽]").formatted(Formatting.AQUA, Formatting.UNDERLINE)
                .styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/achv list " + (shown + 1)))));
        }

        footer.append(Text.literal(" [칭호 끄기]").formatted(Formatting.AQUA, Formatting.UNDERLINE)
            .styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/achv title off"))));
        player.sendMessage(footer);
        return 1;
    }

    private static Text rewardText(Achievement achievement) {
        MutableText text = Text.empty();
        List<Text> lines = rewardLines(achievement);

        for (int i = 0; i < lines.size(); i++) {
            text.append(i > 0 ? Text.literal("\n") : Text.empty()).append(lines.get(i));
        }

        return text;
    }

    /** "보상", the title, the money and the items, one per line (chat hover and menu tooltips). */
    static List<Text> rewardLines(Achievement achievement) {
        List<Text> lines = new ArrayList<>();
        lines.add(Text.literal("보상").formatted(Formatting.GOLD));
        lines.add(Text.literal(" 칭호: [" + achievement.title() + "]").formatted(Formatting.AQUA));

        if (achievement.money() > 0) {
            lines.add(Text.literal(" " + String.format("%,d", achievement.money()) + " CobbleDollars").formatted(Formatting.GREEN));
        }

        for (String entry : achievement.items()) {
            parseItem(entry).ifPresent(stack ->
                lines.add(Text.literal(" ").append(stack.getName()).append(" x" + stack.getCount()).formatted(Formatting.WHITE)));
        }

        return lines;
    }

    private static void setAchievements(List<Achievement> all) {
        configured = all;
        achievements = null;
    }

    static List<Achievement> achievements() {
        if (achievements != null) {
            return achievements;
        }

        List<Achievement> available = Conditions.available(configured);

        if (Conditions.rctLoaded()) {
            achievements = available;
            LOGGER.info("{} of {} achievements available on this server", available.size(), configured.size());
        }

        return available;
    }

    static PlayerRecords.Record record(ServerPlayerEntity player) {
        return records.create(player.getUuid());
    }
}
