package net.dannyfather.mca_descendants.config;


import net.neoforged.neoforge.common.ModConfigSpec;

public class MCADescendantsCommonConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.ConfigValue<Boolean> HARDCORE_ONLY;
    public static final ModConfigSpec.ConfigValue<Boolean> PLAY_AS_SIBLINGS;
    public static final ModConfigSpec.ConfigValue<Boolean> INSTANT_RESPAWN;

    static {
        BUILDER.push("Configs for MCA Descendants");

        HARDCORE_ONLY = BUILDER.comment("Configure Whether The Descendant Mechanic is Hardcore Only")
                .define("Hardcore Only", true);

        PLAY_AS_SIBLINGS = BUILDER.define("Play as Siblings",true);

        INSTANT_RESPAWN = BUILDER.comment("Disabling may cause gamebreaking errors").define("Instant Respawn", true);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
