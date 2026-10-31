package kr.pokeachv;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.pokedex.PokedexEntryProgress;
import com.cobblemon.mod.common.api.pokedex.PokedexManager;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;

import java.util.Map;

/** Current progress of a player towards an achievement's target (see Achievement for the types). */
public final class Conditions {
    private Conditions() {
    }

    /** Pokedex counts, computed once per check of a player since every dex condition needs the same walk. */
    public record Dex(int caught, int kanto, int legendary) {
        public static Dex of(ServerPlayerEntity player) {
            PokedexManager dex = Cobblemon.INSTANCE.getPlayerDataManager().getPokedexData(player);
            int caught = 0;
            int kanto = 0;
            int legendary = 0;

            for (Identifier id : dex.getSpeciesRecords().keySet()) {
                if (dex.getKnowledgeForSpecies(id) != PokedexEntryProgress.CAUGHT) {
                    continue;
                }

                caught++;
                Species species = PokemonSpecies.INSTANCE.getByIdentifier(id);

                if (species == null) {
                    continue;
                }

                int number = species.getNationalPokedexNumber();
                kanto += number >= 1 && number <= 151 ? 1 : 0;
                legendary += species.getLabels().contains("legendary") || species.getLabels().contains("mythical") ? 1 : 0;
            }

            return new Dex(caught, kanto, legendary);
        }
    }

    public static long progress(ServerPlayerEntity player, Achievement achievement, Dex dex) {
        return switch (achievement.type()) {
            case "stat" -> stat(player, achievement.key());
            case "dex" -> dex.caught();
            case "dex_kanto" -> dex.kanto();
            case "dex_legendary" -> dex.legendary();
            case "rct_series" -> series(player).getOrDefault(achievement.key(), 0);
            case "rct_all" -> allSeries(player, achievement.key()) ? 1 : 0;
            case "max_level" -> maxLevel(player);
            case "tower_single", "tower_multi", "tower_wins", "tower_hard_single", "tower_hard_multi", "tower_hard_wins" ->
                FabricLoader.getInstance().isModLoaded("battletower") ? TowerRecords.progress(player, achievement.type()) : 0;
            case "event_legend", "event_legend_box", "event_outbreak", "event_shiny" ->
                FabricLoader.getInstance().isModLoaded("pokeevents") ? EventRecords.progress(player, achievement.type()) : 0;
            case "quest_step", "rival_wins" -> FabricLoader.getInstance().isModLoaded("pokequest")
                ? QuestRecords.progress(player, achievement.type(), achievement.key()) : 0;
            case "trainer_defeat", "trainer_friendly_wins", "gym_wins", "gym_defenses" -> FabricLoader.getInstance().isModLoaded("poketrainer")
                ? TrainerRecords.progress(player, achievement.type(), achievement.key()) : 0;
            default -> 0;
        };
    }

    private static long stat(ServerPlayerEntity player, String key) {
        Identifier id = Identifier.tryParse(key);

        // a stat of a mod that is not installed is simply never reached
        if (id == null || !Registries.CUSTOM_STAT.containsId(id)) {
            return 0;
        }

        return player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Registries.CUSTOM_STAT.get(id)));
    }

    private static Map<String, Integer> series(ServerPlayerEntity player) {
        return RCTMod.getInstance().getTrainerManager().getData(player).getCompletedSeries();
    }

    private static boolean allSeries(ServerPlayerEntity player, String keys) {
        Map<String, Integer> completed = series(player);

        for (String key : keys.split(",")) {
            if (completed.getOrDefault(key.trim(), 0) < 1) {
                return false;
            }
        }

        return true;
    }

    private static long maxLevel(ServerPlayerEntity player) {
        PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);
        int max = 0;

        for (int slot = 0; slot < party.size(); slot++) {
            Pokemon pokemon = party.get(slot);
            max = pokemon != null ? Math.max(max, pokemon.getLevel()) : max;
        }

        return max;
    }
}
