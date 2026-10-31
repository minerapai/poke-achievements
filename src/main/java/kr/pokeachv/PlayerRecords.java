package kr.pokeachv;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Achievements reached and the chosen title per player, saved in the world folder (pokeachv-records.json). */
public final class PlayerRecords {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static final class Record {
        public String name = "";
        public Set<String> done = new LinkedHashSet<>();
        public String title = "";
    }

    private final Path file;
    private final Map<UUID, Record> records;

    private PlayerRecords(Path file, Map<UUID, Record> records) {
        this.file = file;
        this.records = records;
    }

    public static PlayerRecords load(Path file) {
        Map<UUID, Record> records = new LinkedHashMap<>();

        if (Files.exists(file)) {
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                Map<UUID, Record> read = GSON.fromJson(reader, new TypeToken<Map<UUID, Record>>() { }.getType());

                if (read != null) {
                    records.putAll(read);
                }
            } catch (IOException | RuntimeException e) {
                PokeAchievements.LOGGER.error("Could not read {}, starting empty", file, e);
            }
        }

        return new PlayerRecords(file, records);
    }

    /** The record of a player, or null when the player was never checked (first check grants retroactively). */
    public Record get(UUID player) {
        return records.get(player);
    }

    public Record create(UUID player) {
        return records.computeIfAbsent(player, uuid -> new Record());
    }

    public void save() {
        try {
            Path tmp = file.resolveSibling(file.getFileName() + ".tmp");

            try (Writer writer = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8)) {
                GSON.toJson(records, writer);
            }

            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            PokeAchievements.LOGGER.error("Could not save {}", file, e);
        }
    }
}
