package net.dannyfather.mca_descendants;

import net.dannyfather.mca_descendants.network.ClientHandler;
import net.dannyfather.mca_descendants.network.s2c.OpenGuiRequest;
import net.dannyfather.mca_descendants.network.s2c.getDescendantResponse;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;

import static net.dannyfather.MCADescendants.MOD_ID;


public class ClientProxyImpl extends ClientProxyAbstractImpl {

    private final ClientHandler networkHandler = new ClientHandler() {
        @Override
        public void handleGuiRequest(OpenGuiRequest message) {

        }

        @Override
        public void handleDescendantDataResponse(getDescendantResponse message) {

        }
    };

    @Override
    public Player getClientPlayer() {
        return Minecraft.getInstance().player;
    }

    @Override
    public ClientHandler getNetworkHandler() {
        return networkHandler;
    }
}
