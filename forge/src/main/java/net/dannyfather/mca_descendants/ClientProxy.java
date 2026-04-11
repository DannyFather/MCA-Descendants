package net.dannyfather.mca_descendants;

import net.dannyfather.mca_descendants.network.ClientInteractionManager;
import net.minecraft.world.entity.player.Player;

public class ClientProxy {

    private static Impl INSTANCE;

    public static void init(Impl impl) {
        INSTANCE = impl;
    }

    public static Player getClientPlayer() {
        return INSTANCE != null ? INSTANCE.getClientPlayer() : null;
    }

    public static ClientInteractionManager getNetworkHandler() {
        return INSTANCE != null ? INSTANCE.getNetworkHandler() : null;
    }

    public static abstract class Impl {

        public abstract Player getClientPlayer();

        public abstract ClientInteractionManager getNetworkHandler();
    }
}
