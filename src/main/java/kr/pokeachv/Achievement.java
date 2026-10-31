package kr.pokeachv;

import java.util.List;

/**
 * One achievement, as written in config/poke-achievements.json.
 *
 * type / key / target:
 * - "stat": a custom statistic (key = stat id, e.g. "cobblemon:captured") reaches target
 * - "dex": number of species caught in the pokedex
 * - "dex_kanto": number of Kanto species (national dex 1-151) caught
 * - "dex_legendary": number of legendary / mythical species caught
 * - "rct_series": the RCT series (key = "kanto", "johto", ...) completed at least target times
 * - "rct_all": every series listed in key (comma separated) completed
 * - "max_level": a party pokemon at level target or higher
 * - "tower_single" / "tower_multi": best battle tower win streak alone / with a partner; "tower_wins": tower wins;
 *   "tower_hard_single" / "tower_hard_multi" / "tower_hard_wins": the same for the hard tower
 * - "event_legend": announced legendaries caught (any tier); "event_legend_box": box legendaries / mythicals;
 *   "event_outbreak": the outbreak species caught during its outbreak; "event_shiny": shinies caught in a shiny event
 * - "quest_step": the guide quest step key is finished; "rival_wins": rival battles won
 * - "trainer_defeat": the player's raised trainer (poke-trainer) beat the RCT trainer key;
 *   "trainer_friendly_wins": friendly battles the player's trainer won;
 *   "gym_wins": player gyms beaten in person; "gym_defenses": challengers the player's own gym held off
 *
 * icon: item shown in the /achv menu, optional (see defaultIcon). category: the menu theme (see CATEGORIES),
 * optional (see categoryOrDefault).
 */
public record Achievement(
    String id,
    String name,
    String description,
    String type,
    String key,
    long target,
    String title,
    long money,
    List<String> items,
    String icon,
    String category
) {
    /** The themes of the /achv menu, in order: id, name, icon. */
    public static final List<List<String>> CATEGORIES = List.of(
        List.of("dex", "포켓몬 도감", "cobblemon:pokedex_red"),
        List.of("catch", "포획", "cobblemon:poke_ball"),
        List.of("raise", "육성", "cobblemon:rare_candy"),
        List.of("league", "체육관과 리그", "cobbleversebadges:kanto_league_trophy"),
        List.of("battle", "배틀과 레이드", "cobblemon:choice_band"),
        List.of("tower", "배틀 타워", "minecraft:gold_block"),
        List.of("event", "서버 이벤트", "minecraft:bell"),
        List.of("quest", "퀘스트와 라이벌", "minecraft:writable_book"),
        List.of("trainer", "내 트레이너", "minecraft:armor_stand"),
        List.of("etc", "기타", "minecraft:paper"));

    public static Achievement of(String id, String name, String description, String type, String key, long target,
                                 String title, long money, String... items) {
        return new Achievement(id, name, description, type, key, target, title, money, List.of(items), null, null);
    }

    /** The menu theme: category from the config, or one that fits the type. */
    public String categoryOrDefault() {
        if (category != null && !category.isBlank()) {
            return category;
        }

        if (type.startsWith("tower_")) {
            return "tower";
        }

        if (type.startsWith("event_")) {
            return "event";
        }

        if (type.startsWith("trainer_") || type.startsWith("gym_")) {
            return "trainer";
        }

        return switch (type) {
            case "dex", "dex_kanto", "dex_legendary" -> "dex";
            case "rct_series", "rct_all" -> "league";
            case "max_level" -> "raise";
            case "quest_step", "rival_wins" -> "quest";
            case "stat" -> switch (key) {
                case "cobblemon:captured", "cobblemon:shinies_captured" -> "catch";
                case "cobblemon:eggs_hatched", "cobblemon:evolved" -> "raise";
                case "cobblemon:battles_won" -> "battle";
                default -> key.startsWith("cobblemonraiddens:") ? "battle" : "etc";
            };
            default -> "etc";
        };
    }

    /** The menu icon when the config does not set one. */
    public String iconOrDefault() {
        if (icon != null && !icon.isBlank()) {
            return icon;
        }

        if (type.equals("trainer_defeat")) {
            return key.contains("champion") ? "minecraft:golden_helmet" : key.contains("league") ? "minecraft:iron_helmet"
                : "minecraft:leather_helmet";
        }

        return switch (id) {
            case "trainer_friendly_1" -> "minecraft:armor_stand";
            case "trainer_friendly_10" -> "minecraft:totem_of_undying";
            case "gym_win_1" -> "minecraft:iron_sword";
            case "gym_win_10" -> "minecraft:diamond_sword";
            case "gym_defense_10" -> "minecraft:shield";
            case "gym_defense_50" -> "minecraft:lectern";
            case "dex_10" -> "cobblemon:pokedex_red";
            case "dex_50" -> "cobblemon:pokedex_blue";
            case "dex_151" -> "cobblemon:pokedex_green";
            case "dex_300" -> "cobblemon:pokedex_yellow";
            case "dex_500" -> "cobblemon:pokedex_black";
            case "dex_kanto" -> "cobbleversebadges:kanto_badge_box";
            case "legend_1" -> "cobblemon:ultra_ball";
            case "legend_10" -> "cobblemon:master_ball";
            case "champ_kanto" -> "cobbleversebadges:kanto_league_trophy";
            case "champ_johto" -> "cobbleversebadges:johto_league_trophy";
            case "champ_hoenn" -> "cobbleversebadges:hoenn_league_trophy";
            case "champ_sinnoh" -> "cobbleversebadges:sinnoh_league_trophy";
            case "champ_all" -> "minecraft:nether_star";
            case "champ_unova", "champ_kalos", "champ_alola", "champ_galar", "champ_paldea" -> "minecraft:golden_helmet";
            case "champ_all9" -> "minecraft:dragon_egg";
            case "catch_100" -> "cobblemon:poke_ball";
            case "catch_500" -> "cobblemon:great_ball";
            case "shiny_1" -> "cobblemon:shiny_stone";
            case "shiny_10" -> "cobblemon:dawn_stone";
            case "hatch_50" -> "minecraft:egg";
            case "hatch_300" -> "cobblemon:destiny_knot";
            case "evolve_50" -> "cobblemon:fire_stone";
            case "level_100" -> "cobblemon:exp_candy_xl";
            case "win_100" -> "cobblemon:choice_band";
            case "win_1000" -> "cobblemon:life_orb";
            case "raid_10" -> "cobblemonraiddens:raid_shard";
            case "raid_50" -> "cobblemonraiddens:raid_pouch";
            case "raid_7star" -> "cobblemonraiddens:raid_crystal_block";
            case "tower_7" -> "minecraft:iron_block";
            case "tower_21" -> "minecraft:gold_block";
            case "tower_49" -> "minecraft:diamond_block";
            case "tower_multi_21" -> "minecraft:emerald_block";
            case "tower_wins_100" -> "minecraft:netherite_block";
            case "hard_tower_7" -> "cobblemon:master_ball";
            case "hard_tower_21" -> "minecraft:nether_star";
            case "hard_tower_multi_7" -> "cobblemon:beast_ball";
            case "hard_tower_wins_50" -> "minecraft:end_crystal";
            case "event_legend_1" -> "minecraft:compass";
            case "event_legend_5" -> "minecraft:recovery_compass";
            case "event_legend_box" -> "minecraft:dragon_egg";
            case "event_outbreak_10" -> "minecraft:spyglass";
            case "event_outbreak_100" -> "minecraft:bell";
            case "event_shiny_1" -> "minecraft:glowstone_dust";
            case "rival_1" -> "minecraft:target";
            case "rival_9" -> "minecraft:crossbow";
            case "quest_all" -> "minecraft:writable_book";
            default -> "minecraft:paper";
        };
    }

    /** The default list, written to the config file the first time. Items are "id count". */
    /** The stages of a raised trainer (as poke-trainer's Stages): RCT id, Korean name, region index. */
    private static final String[][] TRAINER_ROAD = {
        {"kanto_brock", "웅"}, {"kanto_misty", "이슬"}, {"kanto_ltsurge", "마티스"}, {"kanto_erika", "민화"},
        {"kanto_koga", "독수"}, {"kanto_sabrina", "초련"}, {"kanto_blaine", "강연"}, {"kanto_giovanni", "비주기"},
        {"kanto_league_lorelei", "칸나"}, {"kanto_league_bruno", "시바"}, {"kanto_league_agatha", "국화"},
        {"kanto_league_lance", "목호"}, {"kanto_champion_blue", "그린"},
        {"johto_valerio", "비상"}, {"johto_raffaello", "호일"}, {"johto_chiara", "꼭두"}, {"johto_angelo", "유빈"},
        {"johto_furio", "사도"}, {"johto_jasmine", "규리"}, {"johto_alfredo", "류옹"}, {"johto_sandra", "이향"},
        {"johto_league_pino", "일목"}, {"johto_league_koga", "독수"}, {"johto_league_bruno", "시바"},
        {"johto_league_karen", "카렌"}, {"johto_champion_lance", "목호"},
        {"hoenn_petra", "원규"}, {"hoenn_rudi", "철구"}, {"hoenn_walter", "암페어"}, {"hoenn_fiammetta", "민지"},
        {"hoenn_norman", "종길"}, {"hoenn_alice", "은송"}, {"hoenn_tell", "풍&란"}, {"hoenn_adriano", "아단"},
        {"hoenn_league_fosco", "혁진"}, {"hoenn_league_ester", "회연"}, {"hoenn_league_frida", "미혜"},
        {"hoenn_league_drake", "권수"}, {"hoenn_champion_rocco", "성호"},
        {"sinnoh_pedro", "강석"}, {"sinnoh_gardenia", "유채"}, {"sinnoh_marzia", "자두"}, {"sinnoh_omar", "맥실러"},
        {"sinnoh_fannie", "멜리사"}, {"sinnoh_ferruccio", "동관"}, {"sinnoh_bianca", "무청"}, {"sinnoh_corrado", "전진"},
        {"sinnoh_league_aaron", "충호"}, {"sinnoh_league_terrie", "들국화"}, {"sinnoh_league_vulcano", "대엽"},
        {"sinnoh_league_luciano", "오엽"}, {"sinnoh_champion_camilla", "난천"},
        // added by the wildkanto_regions data pack
        {"pokemon_trainer_cedric_03961", "체렌"}, {"cyclist_kayla_03011", "보미카"}, {"artist_ismael_04811", "아티"}, {"lady_kylie_048b1", "카밀레"}, {"worker_braden_04601", "야콘"}, {"pokemon_ranger_allison_03371", "풍란"}, {"dragon_tamer_andre_03881", "사간"}, {"title_defense_marlon_05eb1", "시즈"}, {"madame_rebecca_04881", "망초"}, {"black_belt_haugh_022a", "연무"}, {"rich_boy_jason_032b1", "블래리"}, {"parasol_lady_alexa_03e11", "카틀레야"}, {"poke_kid_ariel_04441", "아이리스"}, {"cyclist_kayla_03012", "비올라"}, {"jogger_craig_03ce1", "자크로"}, {"battle_girl_helen_03081", "코르니"}, {"pokemon_breeder_albert_03101", "후쿠지"}, {"scientist_beau_01541", "시트론"}, {"lady_kylie_048b2", "마슈"}, {"parasol_lady_alexa_03e12", "고지카"}, {"skier_bjorn_03561", "우루프"}, {"trainer_shelly_00111", "파키라"}, {"swimmer_adrian_035e1", "즈미"}, {"mega_trainer_aloysius_05cc1", "간피"}, {"madame_rebecca_04882", "드라세나"}, {"lady_jacki_020", "카르네"}, {"pokemon_trainer_lucas_04cd1", "일리마"}, {"swimmer_aubree_03751", "수련"}, {"ninja_boy_antonio_03591", "카키"}, {"pokemon_trainer_dawn_04d01", "마오"}, {"scientist_beau_015410", "마마네"}, {"poke_kid_ariel_04442", "마쇼"}, {"pi_carlos_03461", "크치나"}, {"pokemon_ranger_allison_033710", "하푸우"}, {"veteran_armando_03fb1", "할라"}, {"hoenn_liris", "라이치"}, {"poke_kid_ariel_04443", "아세로라"}, {"skier_andrea_03521", "카히리"}, {"pokemon_ranger_deshawn_04b91", "쿠쿠이"}, {"worker_braden_046010", "야로"}, {"swimmer_aubree_037510", "야청"}, {"veteran_armando_03fb10", "순무"}, {"battle_girl_helen_03082", "채두"}, {"madame_rebecca_04883", "포플러"}, {"guitarist_arturo_04321", "마쿠와"}, {"guitarist_arturo_04322", "두송"}, {"dragon_tamer_andre_038810", "금랑"}, {"pokemon_trainer_dawn_04d010", "마리"}, {"pokemon_trainer_cedric_039610", "호브"}, {"rich_boy_jason_032b2", "비트"}, {"black_belt_haugh_02b9", "마스타드"}, {"mega_trainer_aloysius_05cc10", "단델"}, {"cyclist_kayla_03013", "단풍"}, {"artist_ismael_04812", "콜사"}, {"poke_kid_ariel_04444", "모야모"}, {"swimmer_adrian_035e10", "곤포"}, {"collector_brady_03ab1", "청목"}, {"trainer_shelly_00112", "라임"}, {"parasol_lady_alexa_03e13", "리피"}, {"skier_bjorn_03562", "그루샤"}, {"pokemon_ranger_allison_033711", "칠리"}, {"poke_kid_ariel_04445", "뽀삐"}, {"collector_brady_03ab10", "청목"}, {"dragon_tamer_andre_038811", "팔자크"}, {"lady_kylie_048b3", "오모다카"}};
    private static final String[] REGIONS = {"관동", "성도", "호연", "신오", "하나", "칼로스", "알로라", "가라르", "팔데아"};

    /** One achievement per opponent the player's raised trainer beats, plus friendly battle wins. */
    public static List<Achievement> trainerDefaults() {
        List<Achievement> list = new java.util.ArrayList<>();

        for (int i = 0; i < TRAINER_ROAD.length; i++) {
            String id = TRAINER_ROAD[i][0];
            String region = REGIONS[i / 13];
            int step = i % 13;
            String role = step < 8 ? "관장" : step < 12 ? "사천왕" : "챔피언";
            long money = (long) (2_000 + step * 1_000) * (i / 13 + 1);
            boolean champion = step == 12;
            list.add(new Achievement("trainer_" + id, "내 트레이너: " + region + " " + role + " " + TRAINER_ROAD[i][1],
                "내 트레이너가 " + region + " " + role + " " + TRAINER_ROAD[i][1] + "을(를) 이기기", "trainer_defeat", id, 1,
                champion ? region + " 트레이너 마스터" : step < 8 ? region + " 배지 " + (step + 1) + "개" : TRAINER_ROAD[i][1] + " 격파",
                champion ? money * 5 : money,
                champion ? List.of("cobblemon:rare_candy 5") : List.of(), null, null));
        }

        list.add(of("trainer_friendly_1", "첫 친선전 승리", "내 트레이너로 다른 플레이어의 트레이너에게 친선전 1승",
            "trainer_friendly_wins", "", 1, "친선전 입문", 5_000));
        list.add(of("trainer_friendly_10", "친선전의 강자", "내 트레이너로 친선전 10승", "trainer_friendly_wins", "", 10,
            "친선전의 강자", 30_000, "cobblemon:exp_candy_l 5"));
        list.add(of("gym_win_1", "도장 깨기", "다른 플레이어의 체육관에 직접 도전해 이기기", "gym_wins", "", 1, "도장 깨기", 10_000));
        list.add(of("gym_win_10", "체육관 사냥꾼", "다른 플레이어의 체육관을 10번 이기기", "gym_wins", "", 10, "체육관 사냥꾼", 50_000,
            "cobblemon:ability_capsule 1"));
        list.add(of("gym_defense_10", "든든한 관장", "내 체육관이 도전자를 10번 막아내기", "gym_defenses", "", 10, "든든한 관장", 30_000));
        list.add(of("gym_defense_50", "철벽의 체육관", "내 체육관이 도전자를 50번 막아내기", "gym_defenses", "", 50, "철벽의 관장", 150_000,
            "obc:bottle_cap_gold 1"));
        return list;
    }

    public static List<Achievement> defaults() {
        List<Achievement> all = new java.util.ArrayList<>(baseDefaults());
        all.addAll(trainerDefaults());
        return List.copyOf(all);
    }

    private static List<Achievement> baseDefaults() {
        String leaves = "cobblemon:red_mint_leaf 4,cobblemon:blue_mint_leaf 4,cobblemon:cyan_mint_leaf 4,"
            + "cobblemon:pink_mint_leaf 4,cobblemon:green_mint_leaf 4,cobblemon:white_mint_leaf 4";
        String[] mintLeaves = leaves.split(",");

        return List.of(
            // pokedex
            of("dex_10", "도감 첫걸음", "포켓몬 10종 포획", "dex", "", 10, "새내기 트레이너", 5_000,
                "cobblemon:exp_candy_l 5", "cobblemon:rare_candy 3"),
            of("dex_50", "도감 수집가", "포켓몬 50종 포획", "dex", "", 50, "수집가", 20_000,
                concat(mintLeaves, "cobblemon:hp_up 3", "cobblemon:protein 3", "cobblemon:iron 3",
                    "cobblemon:calcium 3", "cobblemon:zinc 3", "cobblemon:carbos 3")),
            of("dex_151", "도감 연구원", "포켓몬 151종 포획", "dex", "", 151, "도감 연구원", 50_000,
                "obc:bottle_cap 4", "cobblemon:ability_capsule 2"),
            of("dex_300", "도감 박사", "포켓몬 300종 포획", "dex", "", 300, "포켓몬 박사", 100_000,
                concat(mintLeaves, "obc:bottle_cap_gold 1", "cobblemon:ability_patch 1")),
            of("dex_500", "도감 마스터", "포켓몬 500종 포획", "dex", "", 500, "도감 마스터", 200_000,
                "obc:bottle_cap_gold 3", "cobblemon:ability_patch 2", "cobblemon:master_ball 1"),
            of("dex_kanto", "관동 도감 완성", "관동 포켓몬 151종(1~151번) 모두 포획", "dex_kanto", "", 151,
                "관동 도감 완성자", 100_000, "obc:bottle_cap_gold 2", "cobblemon:ability_patch 1"),
            of("legend_1", "전설과의 조우", "전설·환상 포켓몬 1종 포획", "dex_legendary", "", 1, "전설의 목격자", 30_000,
                "obc:bottle_cap 2", "cobblemon:rare_candy 10"),
            of("legend_10", "전설 수집가", "전설·환상 포켓몬 10종 포획", "dex_legendary", "", 10, "전설 수집가", 150_000,
                "cobblemon:master_ball 1", "obc:bottle_cap_gold 2"),
            // gyms and leagues (RCT)
            of("champ_kanto", "관동 챔피언", "관동 체육관과 리그 제패", "rct_series", "kanto", 1, "관동 챔피언", 50_000,
                "cobblemon:power_weight 1", "cobblemon:power_bracer 1", "cobblemon:power_belt 1",
                "cobblemon:power_lens 1", "cobblemon:power_band 1", "cobblemon:power_anklet 1",
                "cobblemon:destiny_knot 1"),
            of("champ_johto", "성도 챔피언", "성도 체육관과 리그 제패", "rct_series", "johto", 1, "성도 챔피언", 80_000,
                "cobblemon:ability_patch 1", "obc:bottle_cap 4"),
            of("champ_hoenn", "호연 챔피언", "호연 체육관과 리그 제패", "rct_series", "hoenn", 1, "호연 챔피언", 100_000,
                "obc:bottle_cap_gold 1", "cobblemon:exp_candy_xl 10"),
            of("champ_sinnoh", "신오 챔피언", "신오 체육관과 리그 제패", "rct_series", "sinnoh", 1, "신오 챔피언", 120_000,
                "obc:bottle_cap_gold 1", "cobblemon:ability_patch 1"),
            of("champ_unova", "하나 챔피언", "하나 체육관과 리그 제패", "rct_series", "unova", 1, "하나 챔피언", 140000,
                "obc:bottle_cap_gold 1", "cobblemon:ability_patch 1"),
            of("champ_kalos", "칼로스 챔피언", "칼로스 체육관과 리그 제패", "rct_series", "kalos", 1, "칼로스 챔피언", 160000,
                "obc:bottle_cap_gold 1", "cobblemon:ability_patch 1"),
            of("champ_alola", "알로라 챔피언", "알로라 체육관과 리그 제패", "rct_series", "alola", 1, "알로라 챔피언", 180000,
                "obc:bottle_cap_gold 1", "cobblemon:ability_patch 1"),
            of("champ_galar", "가라르 챔피언", "가라르 체육관과 리그 제패", "rct_series", "galar", 1, "가라르 챔피언", 200000,
                "obc:bottle_cap_gold 1", "cobblemon:ability_patch 1"),
            of("champ_paldea", "팔데아 챔피언", "팔데아 체육관과 리그 제패", "rct_series", "paldea", 1, "팔데아 챔피언", 220000,
                "obc:bottle_cap_gold 1", "cobblemon:ability_patch 1"),
            of("champ_all9", "9지방 정복", "관동부터 팔데아까지 9개 지방 리그 모두 제패", "rct_all",
                "kanto,johto,hoenn,sinnoh,unova,kalos,alola,galar,paldea", 1, "전설의 포켓몬 마스터", 1_000_000,
                "cobblemon:master_ball 5", "obc:bottle_cap_gold 5"),
            of("champ_all", "4지방 정복", "관동·성도·호연·신오 리그 모두 제패", "rct_all", "kanto,johto,hoenn,sinnoh", 1,
                "포켓몬 마스터", 300_000, "cobblemon:master_ball 2", "obc:bottle_cap_gold 3"),
            // catching
            of("catch_100", "포획 전문가", "포켓몬 100마리 포획", "stat", "cobblemon:captured", 100, "포획 전문가", 20_000,
                "cobblemon:exp_candy_l 10", "cobblemon:everstone 1"),
            of("catch_500", "포획 달인", "포켓몬 500마리 포획", "stat", "cobblemon:captured", 500, "포획 달인", 80_000,
                "cobblemon:destiny_knot 1", "obc:bottle_cap 3"),
            of("shiny_1", "반짝이는 첫 만남", "이로치 포켓몬 1마리 포획", "stat", "cobblemon:shinies_captured", 1,
                "이로치 헌터", 30_000, "cobblemon:mirror_herb 1", "obc:bottle_cap 2"),
            of("shiny_10", "이로치 수집가", "이로치 포켓몬 10마리 포획", "stat", "cobblemon:shinies_captured", 10,
                "빛나는 수집가", 150_000, "obc:bottle_cap_gold 2", "cobblemon:ability_patch 1"),
            // breeding and raising
            of("hatch_50", "육성의 시작", "알 50개 부화", "stat", "cobblemon:eggs_hatched", 50, "브리더", 30_000,
                "cobblemon:destiny_knot 1", "cobblemon:everstone 1", "cobblemon:mirror_herb 1"),
            of("hatch_300", "베테랑 브리더", "알 300개 부화", "stat", "cobblemon:eggs_hatched", 300, "마스터 브리더", 100_000,
                "cobblemon:destiny_knot 2", "obc:bottle_cap_gold 1", "cobblemon:ability_capsule 2"),
            of("evolve_50", "진화 연구가", "포켓몬 50번 진화", "stat", "cobblemon:evolved", 50, "진화 연구가", 20_000,
                "cobblemon:rare_candy 10"),
            of("level_100", "최고 레벨", "레벨 100 포켓몬을 파티에 보유", "max_level", "", 100, "레벨 마스터", 30_000,
                "cobblemon:exp_candy_xl 10", "cobblemon:pp_max 3"),
            // battles and raids
            of("win_100", "배틀러", "배틀 100승", "stat", "cobblemon:battles_won", 100, "배틀러", 20_000,
                "cobblemon:hp_up 2", "cobblemon:protein 2", "cobblemon:iron 2", "cobblemon:calcium 2",
                "cobblemon:zinc 2", "cobblemon:carbos 2"),
            of("win_1000", "배틀 마스터", "배틀 1000승", "stat", "cobblemon:battles_won", 1000, "배틀 마스터", 150_000,
                "obc:bottle_cap_gold 2", "cobblemon:rare_candy 30"),
            of("raid_10", "레이드 대원", "레이드 10번 클리어", "stat", "cobblemonraiddens:raids_completed", 10,
                "레이드 대원", 30_000, "obc:bottle_cap 2", "cobblemon:ability_capsule 1"),
            of("raid_50", "레이드 베테랑", "레이드 50번 클리어", "stat", "cobblemonraiddens:raids_completed", 50,
                "레이드 베테랑", 150_000, "cobblemonraiddens:catching_charm 1", "obc:bottle_cap_gold 1"),
            of("raid_7star", "7성 정복자", "7성 레이드 클리어", "stat", "cobblemonraiddens:tier_seven_completed", 1,
                "7성 정복자", 100_000, "cobblemon:ability_patch 1", "obc:bottle_cap 3"),
            // battle tower
            of("tower_7", "타워 도전자", "배틀 타워 싱글 7연승", "tower_single", "", 7, "타워 도전자", 30_000,
                "obc:bottle_cap 2", "cobblemon:exp_candy_xl 5"),
            of("tower_21", "타워 브레인", "배틀 타워 싱글 21연승", "tower_single", "", 21, "타워 브레인", 100_000,
                "obc:bottle_cap_gold 1", "cobblemon:ability_patch 1"),
            of("tower_49", "타워 타이쿤", "배틀 타워 싱글 49연승", "tower_single", "", 49, "타워 타이쿤", 300_000,
                "cobblemon:master_ball 1", "obc:bottle_cap_gold 3"),
            of("tower_multi_21", "환상의 콤비", "배틀 타워 멀티 21연승", "tower_multi", "", 21, "환상의 콤비", 100_000,
                "obc:bottle_cap_gold 1", "cobblemon:destiny_knot 1"),
            of("tower_wins_100", "타워 단골", "배틀 타워 통산 100승", "tower_wins", "", 100, "타워 단골", 50_000,
                "cobblemon:ability_capsule 2", "obc:bottle_cap 2"),
            of("hard_tower_7", "전설에 맞선 자", "하드 타워 싱글 7연승", "tower_hard_single", "", 7, "전설에 맞선 자", 150_000,
                "obc:bottle_cap_gold 2", "cobblemon:ability_patch 1"),
            of("hard_tower_21", "하드 타워 정복자", "하드 타워 싱글 21연승", "tower_hard_single", "", 21, "신화를 넘은 자", 500_000,
                "cobblemon:master_ball 2", "obc:bottle_cap_gold 5", "cobblemon:ability_patch 2"),
            of("hard_tower_multi_7", "전설의 콤비", "하드 타워 멀티 7연승", "tower_hard_multi", "", 7, "전설의 콤비", 150_000,
                "obc:bottle_cap_gold 2", "cobblemon:destiny_knot 1"),
            of("hard_tower_wins_50", "하드 타워 베테랑", "하드 타워 통산 50승", "tower_hard_wins", "", 50, "하드 타워 베테랑", 200_000,
                "cobblemon:master_ball 1", "obc:bottle_cap_gold 2"),
            // server events
            of("event_legend_1", "전설의 추적자", "전설 출현 이벤트의 전설 포켓몬 포획", "event_legend", "", 1, "전설의 추적자", 50_000,
                "obc:bottle_cap 2", "cobblemon:rare_candy 10"),
            of("event_legend_5", "전설 사냥꾼", "전설 출현 이벤트의 전설 포켓몬 5마리 포획", "event_legend", "", 5, "전설 사냥꾼", 200_000,
                "cobblemon:master_ball 1", "obc:bottle_cap_gold 2"),
            of("event_legend_box", "신화의 목격자", "전설 출현 이벤트의 초전설·환상 포켓몬 포획", "event_legend_box", "", 1,
                "신화의 목격자", 300_000, "cobblemon:master_ball 1", "cobblemon:ability_patch 2"),
            of("event_outbreak_10", "대량발생 탐험가", "대량발생 중 그 포켓몬 10마리 포획", "event_outbreak", "", 10,
                "대량발생 탐험가", 20_000, "cobblemon:exp_candy_l 10", "cobblemon:everstone 1"),
            of("event_outbreak_100", "대량발생 마스터", "대량발생 중 그 포켓몬 100마리 포획", "event_outbreak", "", 100,
                "대량발생 마스터", 100_000, "obc:bottle_cap_gold 1", "cobblemon:destiny_knot 1"),
            of("event_shiny_1", "행운의 시간", "이로치 확률 이벤트 중 이로치 포획", "event_shiny", "", 1, "행운의 시간", 50_000,
                "cobblemon:mirror_herb 1", "obc:bottle_cap 2"),
            // guide quest and rival
            of("rival_1", "라이벌 첫 승리", "가이드 퀘스트의 라이벌에게 처음 이기기", "rival_wins", "", 1, "라이벌의 호적수", 20_000,
                "cobblemon:exp_candy_l 5", "cobblemon:rare_candy 3"),
            of("rival_9", "라이벌 완전 정복", "라이벌과의 마지막(9번째) 대결에서 이기기", "rival_wins", "", 9, "영원한 라이벌", 200_000,
                "cobblemon:master_ball 1", "obc:bottle_cap_gold 2"),
            of("quest_all", "모든 여행의 끝", "가이드 퀘스트 전부 완료 (관동부터 신오까지)", "quest_step", "rival_9", 1, "전설의 트레이너",
                300_000, "cobblemon:ability_patch 2", "obc:bottle_cap_gold 2")
        );
    }

    private static String[] concat(String[] first, String... rest) {
        String[] all = new String[first.length + rest.length];
        System.arraycopy(first, 0, all, 0, first.length);
        System.arraycopy(rest, 0, all, first.length, rest.length);
        return all;
    }
}
