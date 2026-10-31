package kr.pokeachv;

import kr.pokequest.QuestData;
import net.minecraft.server.network.ServerPlayerEntity;

/** Guide quest progress of a player. Only loaded when the quest mod is installed (see Conditions). */
final class QuestRecords {
    private QuestRecords() {
    }

    static long progress(ServerPlayerEntity player, String type, String key) {
        QuestData.Record record = QuestData.get(player.getUuid());

        if (record == null) {
            return 0;
        }

        return switch (type) {
            case "quest_step" -> record.done.contains(key) ? 1 : 0;
            case "rival_wins" -> record.rivalWins;
            default -> 0;
        };
    }
}
