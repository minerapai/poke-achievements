package kr.pokeachv;

import kr.battletower.BattleTower;
import kr.battletower.TowerData;
import net.minecraft.server.network.ServerPlayerEntity;

/** Battle tower records of a player. Only loaded when the battle tower mod is installed (see Conditions). */
final class TowerRecords {
    private TowerRecords() {
    }

    static long progress(ServerPlayerEntity player, String type) {
        TowerData data = BattleTower.data();

        if (data == null) {
            return 0;
        }

        TowerData.PlayerRecord record = data.players.get(player.getUuid());

        return switch (type) {
            case "tower_single" -> record != null ? record.single.best : 0;
            case "tower_wins" -> record != null ? record.wins : 0;
            case "tower_multi" -> data.pairs.values().stream().filter(p -> p.players.contains(player.getUuid()))
                .mapToLong(p -> p.best).max().orElse(0);
            case "tower_hard_single" -> record != null ? record.run(true).best : 0;
            case "tower_hard_wins" -> record != null ? record.hardWins : 0;
            case "tower_hard_multi" -> data.pairs(true).values().stream().filter(p -> p.players.contains(player.getUuid()))
                .mapToLong(p -> p.best).max().orElse(0);
            default -> 0;
        };
    }
}
