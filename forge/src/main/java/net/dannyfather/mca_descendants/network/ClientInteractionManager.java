package net.dannyfather.mca_descendants.network;

import net.dannyfather.mca_descendants.client.gui.PhoneScreen;
import net.dannyfather.mca_descendants.network.s2c.OpenGuiRequest;
import net.dannyfather.mca_descendants.network.s2c.getDescendantResponse;
import net.minecraft.client.Minecraft;

public interface ClientInteractionManager {
    static void handle(OpenGuiRequest msg) {
        Minecraft.getInstance().setScreen(new PhoneScreen(msg.villager));
    }
    void handleDescendantDataResponse(getDescendantResponse message);
}
