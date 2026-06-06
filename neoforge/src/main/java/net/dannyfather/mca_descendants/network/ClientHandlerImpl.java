package net.dannyfather.mca_descendants.network;

import net.dannyfather.mca_descendants.client.gui.PhoneOptionsScreen;
import net.dannyfather.mca_descendants.client.gui.PhoneScreen;
import net.dannyfather.mca_descendants.network.s2c.OpenGuiRequest;
import net.dannyfather.mca_descendants.network.s2c.getDescendantResponse;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;

import static net.dannyfather.MCADescendants.MOD_ID;

public class ClientHandlerImpl implements ClientHandler {
    private final Minecraft client = Minecraft.getInstance();

    @Override
    public void handleGuiRequest(OpenGuiRequest message) {
        Entity entity;
        assert client.level != null;
        assert Minecraft.getInstance().player != null;

        client.execute(() -> {
            switch (message.getGui()) {

                case PHONE -> {
                    client.execute(() -> {
                        if (client.player == null || client.level == null) return;

                        client.setScreen(new PhoneScreen());
                    });
                }

                case PHONEOPTIONS -> {
                    client.execute(() -> {
                        if (client.player == null || client.level == null) return;

                        client.setScreen(new PhoneOptionsScreen());
                    });
                }
            }
        });
    }

    @Override
    public void handleDescendantDataResponse(getDescendantResponse message) {
        Minecraft.getInstance().execute(() -> {
            if (Minecraft.getInstance().screen instanceof PhoneScreen screen) {
                screen.setVillagerData(message.getData());
            }
        });
    }


}
