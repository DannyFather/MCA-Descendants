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
    public static final ModConfigSpec.ConfigValue<Integer> BABY_HEALTH;
    public static final ModConfigSpec.ConfigValue<Integer> TODDLER_HEALTH;
    public static final ModConfigSpec.ConfigValue<Integer> CHILD_HEALTH;
    public static final ModConfigSpec.ConfigValue<Integer> TEEN_HEALTH;
    public static final ModConfigSpec.ConfigValue<Integer> ADULT_HEALTH;

    //Respawn Without Descendant
    public static final ModConfigSpec.ConfigValue<Boolean> RESPAWN_RANDOM;
    public static final ModConfigSpec.ConfigValue<Float> TEEN_SPAWN_PERCENTAGE;


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
        BABY_SPEED = BUILDER.comment("Adjust speed modifiers of each age state").define("Baby Movement Speed",0.0f);
        TODDLER_SPEED = BUILDER.define("Toddler Movement Speed", 0.2f);
        CHILD_SPEED = BUILDER.define("Child Movement Speed", 0.9f);
        TEEN_SPEED = BUILDER.define("Teen Movement Speed", 1.2f);
        ADULT_SPEED = BUILDER.define("Adult Movement Speed", 1f);

        //heath bars
        BABY_HEALTH = BUILDER.comment("Adjust heath modifiers (amount of half-hearts) of each age state").define("Baby health",4);
        TODDLER_HEALTH = BUILDER.define("Toddler health", 6);
        CHILD_HEALTH = BUILDER.define("Child health", 10);
        TEEN_HEALTH = BUILDER.define("Teen health", 16);
        ADULT_HEALTH = BUILDER.define("Adult health", 20);


        //Randomised Spawn
        RESPAWN_RANDOM = BUILDER.comment("Setting this to true enables you to redo villager creation, feature for survival/creative only").define("Enable Respawn Without Descendants",true);
        TEEN_SPAWN_PERCENTAGE = BUILDER.comment("If respawning as a random villager, percentage chance you respawn as a teenager").define("Teenager Chance",10.0f);

        //mod compat
        if(ModList.get().isLoaded("pmmo")) {
            RESET_PMMO_STATS = BUILDER.define("Reset Project MMO Stats",true);
        }

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
