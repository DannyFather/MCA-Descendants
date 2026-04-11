package net.dannyfather.mca_descendants.network;

import net.dannyfather.mca_descendants.client.gui.PhoneScreen;
import net.dannyfather.mca_descendants.network.s2c.OpenGuiRequest;
import net.dannyfather.mca_descendants.network.s2c.getDescendantResponse;
import net.minecraft.client.Minecraft;

import static net.dannyfather.mca_descendants.block.ModBlocks.PHONE;

public class ClientInteractionManagerImpl implements ClientInteractionManager {

    @Override
    public void handleGuiRequest(OpenGuiRequest message) {

        Minecraft.getInstance().execute(() -> {
            switch (message.getGui()) {

                case PHONE -> {
                    Minecraft.getInstance().setScreen(
                            new PhoneScreen(message.villager)
                    );
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
