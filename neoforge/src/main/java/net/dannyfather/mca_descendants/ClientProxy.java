package net.dannyfather.mca_descendants;

import net.dannyfather.mca_descendants.network.ClientHandler;
import net.dannyfather.mca_descendants.network.ClientHandlerImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;


public class ClientProxy {

    private static Impl INSTANCE = new Impl();

    @Nullable
    public static Player getClientPlayer() {
        return INSTANCE.getClientPlayer();
    }

    @Nullable
    public static ClientHandler getNetworkHandler() {
        return INSTANCE.getNetworkHandler();
    }

    public static class Impl {

        private final ClientHandler networkHandler = new ClientHandlerImpl();

        @Nullable
        public Player getClientPlayer() {
            return Minecraft.getInstance().player;
        }

        @Nullable
        public ClientHandler getNetworkHandler() {
            return networkHandler;
        }
    }
}