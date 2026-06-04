package net.dannyfather.mca_descendants.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModList;

public class MCADescendantsCommonConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Boolean> HARDCORE_ONLY;
    public static final ForgeConfigSpec.ConfigValue<Boolean> PLAY_AS_SIBLINGS;
    public static final ForgeConfigSpec.ConfigValue<Boolean> INSTANT_RESPAWN;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ADULTS_ONLY;
    public static final ForgeConfigSpec.ConfigValue<Boolean> INSTANT_GROWTH;
    public static final ForgeConfigSpec.ConfigValue<Boolean> PLAYER_GROWTH;

    //speed modifiers
    public static final ForgeConfigSpec.ConfigValue<Float> BABY_SPEED;
    public static final ForgeConfigSpec.ConfigValue<Float> TODDLER_SPEED;
    public static final ForgeConfigSpec.ConfigValue<Float> CHILD_SPEED;
    public static final ForgeConfigSpec.ConfigValue<Float> TEEN_SPEED;
    public static final ForgeConfigSpec.ConfigValue<Float> ADULT_SPEED;

    //health modifiers
    public static final ForgeConfigSpec.ConfigValue<Integer> BABY_HEALTH;
    public static final ForgeConfigSpec.ConfigValue<Integer> TODDLER_HEALTH;
    public static final ForgeConfigSpec.ConfigValue<Integer> CHILD_HEALTH;
    public static final ForgeConfigSpec.ConfigValue<Integer> TEEN_HEALTH;
    public static final ForgeConfigSpec.ConfigValue<Integer> ADULT_HEALTH;


    //compatibility
    public static ForgeConfigSpec.ConfigValue<Boolean> RESET_PMMO_STATS;


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



        //mod compat
        if(ModList.get().isLoaded("pmmo")) {
            RESET_PMMO_STATS = BUILDER.define("Reset Project MMO Stats",true);
        }

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
