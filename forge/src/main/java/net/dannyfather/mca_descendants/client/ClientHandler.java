package net.dannyfather.mca_descendants.client;

import net.dannyfather.mca_descendants.MCADescendants;
import net.dannyfather.mca_descendants.client.gui.PhoneScreen;
import net.dannyfather.mca_descendants.network.s2c.OpenGuiRequest;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MCADescendants.MODID,value = Dist.CLIENT)
public class ClientHandler {
    public static void openGui(OpenGuiRequest msg) {
        Minecraft.getInstance().setScreen(new PhoneScreen(msg.villager));
    }
}
