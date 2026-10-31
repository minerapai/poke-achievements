package kr.pokeachv;

import kr.pokeevents.EventStats;
import net.minecraft.server.network.ServerPlayerEntity;

/** What a player caught thanks to the server events. Only loaded when the events mod is installed (see Conditions). */
final class EventRecords {
    private EventRecords() {
    }

    static long progress(ServerPlayerEntity player, String type) {
        EventStats.Record record = EventStats.get(player.getUuid());

        if (record == null) {
            return 0;
        }

        return switch (type) {
            case "event_legend" -> record.legendSub + record.legendBox;
            case "event_legend_box" -> record.legendBox;
            case "event_outbreak" -> record.outbreak;
            case "event_shiny" -> record.shinyEvent;
            default -> 0;
        };
    }
}
