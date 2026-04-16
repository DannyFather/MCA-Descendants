package net.dannyfather.mca_descendants.network;

import net.dannyfather.mca_descendants.MCADescendants;
import net.dannyfather.mca_descendants.client.gui.PhoneScreen;
import net.dannyfather.mca_descendants.network.s2c.OpenGuiRequest;
import net.dannyfather.mca_descendants.network.s2c.getDescendantResponse;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MCADescendants.MODID,value = Dist.CLIENT)
public interface ClientInteractionManager {
    static void handle(OpenGuiRequest msg) {
        Minecraft.getInstance().setScreen(new PhoneScreen(msg.villager));
    }
    void handleDescendantDataResponse(getDescendantResponse message);
}
