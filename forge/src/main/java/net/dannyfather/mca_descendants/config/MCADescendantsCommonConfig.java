package net.dannyfather.mca_descendants.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class MCADescendantsCommonConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Boolean> HARDCORE_ONLY;
    public static final ForgeConfigSpec.ConfigValue<Boolean> PLAY_AS_SIBLINGS;

    static {
        BUILDER.push("Configs for MCA Descendants");

        HARDCORE_ONLY = BUILDER.comment("Configure Whether The Descendant Mechanic is Hardcore Only")
                .define("Hardcore Only", true);

        PLAY_AS_SIBLINGS = BUILDER.define("Play as Siblings",true);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
