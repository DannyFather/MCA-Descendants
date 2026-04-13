package net.dannyfather.mca_descendants.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class MCADescendantsServerConfig {
    public static final ForgeConfigSpec SERVER_SPEC;
    public static final Server SERVER;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        SERVER = new Server(builder);
        SERVER_SPEC = builder.build();
    }

    public static class Server {
        public final ForgeConfigSpec.BooleanValue SERVER_HARDCORE_ONLY;
        public final ForgeConfigSpec.BooleanValue SERVER_PLAY_AS_SIBLINGS;

        public Server(ForgeConfigSpec.Builder builder) {
            builder.push("general");

            SERVER_HARDCORE_ONLY = builder
                    .define("Hardcore Only", false);

            SERVER_PLAY_AS_SIBLINGS = builder
                    .define("Play as Siblings",true);

            builder.pop();
        }
    }

}
