package net.dannyfather.mca_descendants.config;


import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.ModConfigSpec;

public class MCADescendantsCommonConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.ConfigValue<Boolean> HARDCORE_ONLY;
    public static final ModConfigSpec.ConfigValue<Boolean> PLAY_AS_SIBLINGS;
    public static final ModConfigSpec.ConfigValue<Boolean> INSTANT_RESPAWN;
    public static final ModConfigSpec.ConfigValue<Boolean> ADULTS_ONLY;
    public static final ModConfigSpec.ConfigValue<Boolean> INSTANT_GROWTH;
    public static final ModConfigSpec.ConfigValue<Boolean> PLAYER_GROWTH;

    //speed modifiers
    public static final ModConfigSpec.ConfigValue<Float> BABY_SPEED;
    public static final ModConfigSpec.ConfigValue<Float> TODDLER_SPEED;
    public static final ModConfigSpec.ConfigValue<Float> CHILD_SPEED;
    public static final ModConfigSpec.ConfigValue<Float> TEEN_SPEED;
    public static final ModConfigSpec.ConfigValue<Float> ADULT_SPEED;

    //health modifiers
    public static final ModConfigSpec.ConfigValue<Integer> BABY_HEATLH;
    public static final ModConfigSpec.ConfigValue<Integer> TODDLER_HEATLH;
    public static final ModConfigSpec.ConfigValue<Integer> CHILD_HEATLH;
    public static final ModConfigSpec.ConfigValue<Integer> TEEN_HEATLH;
    public static final ModConfigSpec.ConfigValue<Integer> ADULT_HEATLH;


    //compatibility
    public static ModConfigSpec.ConfigValue<Boolean> RESET_PMMO_STATS;


    static {
        BUILDER.push("Configs for MCA Descendants");

        HARDCORE_ONLY = BUILDER.comment("Configure Whether The Descendant Mechanic is Hardcore Only")
                .define("Hardcore Only", true);

        PLAY_AS_SIBLINGS = BUILDER.define("Play as Siblings",true);
        INSTANT_RESPAWN = BUILDER.define("Instant Respawn", true);

        ADULTS_ONLY = BUILDER.comment("Only be able to spawn as fully grown offspring").define("Adults Only",false);
        INSTANT_GROWTH = BUILDER.comment("If you respawn as non-adult offspring, they instantly grow up").define("Instant Growup",false);
        PLAYER_GROWTH = BUILDER.define("Player Growth", true);

        //speed
        BABY_SPEED = BUILDER.comment("Adjust speed modifiers of each age state").define("Baby Movement Speed",0.05f);
        TODDLER_SPEED = BUILDER.define("Toddler Movement Speed", 0.2f);
        CHILD_SPEED = BUILDER.define("Child Movement Speed", 0.9f);
        TEEN_SPEED = BUILDER.define("Teen Movement Speed", 1.2f);
        ADULT_SPEED = BUILDER.define("Adult Movement Speed", 1f);

        //heath bars
        BABY_HEATLH = BUILDER.comment("Adjust heath modifiers (amount of half-hearts) of each age state").define("Baby health",4);
        TODDLER_HEATLH = BUILDER.define("Toddler health", 6);
        CHILD_HEATLH = BUILDER.define("Child health", 10);
        TEEN_HEATLH = BUILDER.define("Teen health", 16);
        ADULT_HEATLH = BUILDER.define("Adult health", 20);



        //mod compat
        if(ModList.get().isLoaded("pmmo")) {
            RESET_PMMO_STATS = BUILDER.define("Reset Project MMO Stats",true);
        }

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
