package kr.pokeachv;

import kr.poketrainer.PokeTrainer;
import kr.poketrainer.TrainerData;
import net.minecraft.server.network.ServerPlayerEntity;

/** Records of the player's raised trainer. Only loaded when the poke-trainer mod is installed (see Conditions). */
final class TrainerRecords {
    private TrainerRecords() {
    }

    static long progress(ServerPlayerEntity player, String type, String key) {
        TrainerData data = PokeTrainer.data();
        TrainerData.Record record;

        try {
            record = data != null ? data.get(player.getUuid()) : null;
        } catch (IllegalStateException e) {
            return 0;
        }

        if (record == null) {
            return 0;
        }

        return switch (type) {
            case "trainer_defeat" -> record.defeated.contains(key) ? 1 : 0;
            case "trainer_friendly_wins" -> record.friendlyWins;
            case "gym_wins" -> record.gymWins;
            case "gym_defenses" -> record.gym != null ? record.gym.defenses : 0;
            default -> 0;
        };
    }
}
