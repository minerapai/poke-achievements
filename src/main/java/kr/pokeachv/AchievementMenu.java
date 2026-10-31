package kr.pokeachv;

import com.mojang.authlib.GameProfile;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * /achv as a chest screen (a vanilla screen, so clients need no mod). The first screen shows one icon per theme
 * (pokedex, catching, battle tower, ...) with how many of its achievements are reached; clicking a theme lists its
 * achievements with their progress and rewards in the tooltip, click a reached one to use its title. Nothing can be
 * taken out; every click is handled here.
 */
public final class AchievementMenu extends GenericContainerScreenHandler {
    private static final int ROWS = 6;
    private static final int PER_PAGE = 45; // first five rows; the last row holds the controls
    /** where the theme icons go on the first screen */
    private static final int[] THEME_SLOTS = {19, 20, 21, 22, 23, 24, 25, 29, 30, 31, 32, 33, 37, 38, 39, 40, 41, 42, 43};
    private static final int SLOT_SUMMARY = 4;
    private static final int SLOT_PREVIOUS = 45;
    private static final int SLOT_TITLE_OFF = 48;
    private static final int SLOT_THEME = 49;
    private static final int SLOT_NEXT = 53;

    private final ServerPlayerEntity player;
    private final SimpleInventory inventory;
    /** the theme being listed, null on the first screen */
    private List<String> theme;
    private int page;

    private AchievementMenu(int syncId, PlayerInventory playerInventory, SimpleInventory inventory,
                            ServerPlayerEntity player) {
        super(ScreenHandlerType.GENERIC_9X6, syncId, playerInventory, inventory, ROWS);
        this.player = player;
        this.inventory = inventory;
        fill();
    }

    public static int open(ServerPlayerEntity player, int page) {
        player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
            (syncId, playerInventory, p) -> new AchievementMenu(syncId, playerInventory, new SimpleInventory(ROWS * 9), player),
            Text.literal("업적")));
        return 1;
    }

    /** Themes that have achievements, in menu order. */
    private static List<List<String>> themes() {
        List<Achievement> all = PokeAchievements.achievements();
        return Achievement.CATEGORIES.stream()
            .filter(t -> all.stream().anyMatch(a -> a.categoryOrDefault().equals(t.get(0)) || isOther(a) && t.get(0).equals("etc")))
            .toList();
    }

    /** Achievements whose category is not one of the menu themes go to "etc". */
    private static boolean isOther(Achievement achievement) {
        return Achievement.CATEGORIES.stream().noneMatch(t -> t.get(0).equals(achievement.categoryOrDefault()));
    }

    private static List<Achievement> inTheme(List<String> theme) {
        return PokeAchievements.achievements().stream()
            .filter(a -> a.categoryOrDefault().equals(theme.get(0)) || theme.get(0).equals("etc") && isOther(a))
            .toList();
    }

    private int pages() {
        return theme == null ? 1 : Math.max(1, (inTheme(theme).size() + PER_PAGE - 1) / PER_PAGE);
    }

    private void fill() {
        inventory.clear();
        PlayerRecords.Record record = PokeAchievements.record(player);
        ItemStack filler = named(new ItemStack(Items.GRAY_STAINED_GLASS_PANE), Text.literal(" "), List.of());

        for (int slot = 0; slot < ROWS * 9; slot++) {
            inventory.setStack(slot, filler.copy());
        }

        if (theme == null) {
            fillThemes(record);
        } else {
            fillTheme(record);
        }

        inventory.setStack(SLOT_TITLE_OFF, named(new ItemStack(Items.BARRIER), plain("칭호 끄기", Formatting.RED),
            List.of(plain("채팅 이름 앞의 칭호를 없앱니다.", Formatting.GRAY))));
        sendContentUpdates();
    }

    private void fillThemes(PlayerRecords.Record record) {
        List<List<String>> themes = themes();

        for (int i = 0; i < themes.size() && i < THEME_SLOTS.length; i++) {
            List<String> t = themes.get(i);
            List<Achievement> list = inTheme(t);
            long done = list.stream().filter(a -> record.done.contains(a.id())).count();
            boolean all = done == list.size();
            List<Text> lore = new ArrayList<>();
            lore.add(plain("달성: " + done + " / " + list.size() + "  " + bar(done, list.size()), all ? Formatting.GREEN : Formatting.YELLOW));
            lore.add(Text.empty());

            for (Achievement a : list) {
                boolean reached = record.done.contains(a.id());
                lore.add(plain((reached ? " ✔ " : " ✘ ") + a.name(), reached ? Formatting.GREEN : Formatting.GRAY));
            }

            lore.add(Text.empty());
            lore.add(plain("클릭: 자세히 보기", Formatting.AQUA));
            ItemStack stack = named(new ItemStack(item(t.get(2))), plain(t.get(1), Formatting.GOLD), lore);
            stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, all);
            stack.set(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP, net.minecraft.util.Unit.INSTANCE);
            inventory.setStack(THEME_SLOTS[i], stack);
        }

        inventory.setStack(SLOT_SUMMARY, summary(record));
    }

    private void fillTheme(PlayerRecords.Record record) {
        List<Achievement> list = inTheme(theme);
        Conditions.Dex dex = Conditions.Dex.of(player);
        page = Math.min(page, pages() - 1);

        for (int i = 0; i < PER_PAGE; i++) {
            inventory.setStack(i, ItemStack.EMPTY);
        }

        for (int i = 0; i < PER_PAGE && page * PER_PAGE + i < list.size(); i++) {
            inventory.setStack(i, icon(list.get(page * PER_PAGE + i), record, dex));
        }

        inventory.setStack(SLOT_PREVIOUS, named(new ItemStack(Items.ARROW),
            plain(page > 0 ? "이전 쪽" : "테마 목록으로", Formatting.AQUA), List.of()));

        if (page < pages() - 1) {
            inventory.setStack(SLOT_NEXT, named(new ItemStack(Items.ARROW), plain("다음 쪽", Formatting.AQUA), List.of()));
        }

        long done = list.stream().filter(a -> record.done.contains(a.id())).count();
        inventory.setStack(SLOT_THEME, named(new ItemStack(item(theme.get(2))), plain(theme.get(1), Formatting.GOLD), List.of(
            plain("달성: " + done + " / " + list.size() + "  " + bar(done, list.size()), Formatting.YELLOW),
            plain("쪽: " + (page + 1) + " / " + pages(), Formatting.GRAY))));
    }

    private static Item item(String id) {
        return Optional.ofNullable(Identifier.tryParse(id)).flatMap(Registries.ITEM::getOrEmpty).orElse(Items.PAPER);
    }

    private ItemStack icon(Achievement achievement, PlayerRecords.Record record, Conditions.Dex dex) {
        boolean reached = record.done.contains(achievement.id());
        boolean inUse = reached && achievement.id().equals(record.title);
        List<Text> lore = new ArrayList<>();
        lore.add(plain(achievement.description(), Formatting.GRAY));

        if (reached) {
            lore.add(plain("달성 완료", Formatting.GREEN));
        } else {
            long progress = Math.min(Conditions.progress(player, achievement, dex), achievement.target());
            lore.add(plain("진행도: " + progress + " / " + achievement.target() + "  " + bar(progress, achievement.target()),
                Formatting.DARK_AQUA));
        }

        lore.add(Text.empty());
        lore.addAll(PokeAchievements.rewardLines(achievement));
        lore.add(Text.empty());

        if (inUse) {
            lore.add(plain("사용 중인 칭호입니다.", Formatting.AQUA));
        } else if (reached) {
            lore.add(plain("클릭하면 칭호로 설정합니다.", Formatting.YELLOW));
        } else {
            lore.add(plain("달성하면 칭호를 쓸 수 있습니다.", Formatting.DARK_GRAY));
        }

        Text name = Text.literal((reached ? "✔ " : "✘ ") + achievement.name())
            .setStyle(Style.EMPTY.withItalic(false).withColor(reached ? Formatting.GREEN : Formatting.GRAY))
            .copy().append(inUse ? Text.literal(" [칭호 사용 중]").formatted(Formatting.AQUA) : Text.empty());
        ItemStack stack = named(new ItemStack(item(achievement.iconOrDefault())), name, lore);
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, reached);
        stack.set(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP, net.minecraft.util.Unit.INSTANCE);
        return stack;
    }

    private ItemStack summary(PlayerRecords.Record record) {
        List<Achievement> all = PokeAchievements.achievements();
        long done = all.stream().filter(a -> record.done.contains(a.id())).count();
        String title = all.stream().filter(a -> a.id().equals(record.title) && record.done.contains(a.id()))
            .map(Achievement::title).findFirst().orElse("없음");
        ItemStack head = new ItemStack(Items.PLAYER_HEAD);
        GameProfile profile = player.getGameProfile();
        head.set(DataComponentTypes.PROFILE, new ProfileComponent(profile));
        return named(head, plain(profile.getName() + "의 업적", Formatting.GOLD), List.of(
            plain("전체 달성: " + done + " / " + all.size() + "  " + bar(done, all.size()), Formatting.YELLOW),
            plain("칭호: " + title, Formatting.AQUA),
            plain("테마를 눌러 업적을 확인하세요.", Formatting.GRAY)));
    }

    private static String bar(long value, long max) {
        int filled = max > 0 ? (int) Math.min(10, value * 10 / max) : 10;
        return "■".repeat(filled) + "□".repeat(10 - filled);
    }

    private static MutableText plain(String text, Formatting color) {
        return Text.literal(text).setStyle(Style.EMPTY.withItalic(false).withColor(color));
    }

    private static ItemStack named(ItemStack stack, Text name, List<Text> lore) {
        stack.set(DataComponentTypes.CUSTOM_NAME, name.copy().setStyle(name.getStyle().withItalic(false)));
        List<Text> lines = new ArrayList<>();

        for (Text line : lore) {
            lines.add(line.copy().setStyle(line.getStyle().withItalic(false)));
        }

        stack.set(DataComponentTypes.LORE, new LoreComponent(lines));
        return stack;
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity clicker) {
        // never move items: only react to plain clicks on the menu's own slots
        if (actionType == SlotActionType.PICKUP && slotIndex >= 0 && slotIndex < ROWS * 9) {
            click(slotIndex);
        }

        syncState(); // undo whatever the client already showed moving
    }

    private void click(int slot) {
        if (slot == SLOT_TITLE_OFF) {
            PokeAchievements.setTitle(player, null);
            fill();
            return;
        }

        if (theme == null) {
            List<List<String>> themes = themes();

            for (int i = 0; i < themes.size() && i < THEME_SLOTS.length; i++) {
                if (THEME_SLOTS[i] == slot) {
                    theme = themes.get(i);
                    page = 0;
                    fill();
                }
            }

            return;
        }

        List<Achievement> list = inTheme(theme);

        if (slot < PER_PAGE) {
            int index = page * PER_PAGE + slot;

            if (index < list.size() && PokeAchievements.record(player).done.contains(list.get(index).id())) {
                PokeAchievements.setTitle(player, list.get(index).id());
                fill();
            }
        } else if (slot == SLOT_PREVIOUS) {
            if (page > 0) {
                page--;
            } else {
                theme = null;
            }

            fill();
        } else if (slot == SLOT_NEXT && page < pages() - 1) {
            page++;
            fill();
        }
    }

    @Override
    public ItemStack quickMove(PlayerEntity clicker, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canUse(PlayerEntity clicker) {
        return true;
    }
}
