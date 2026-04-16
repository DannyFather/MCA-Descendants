package net.dannyfather.mca_descendants;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber(modid = MCADescendants.MODID,value = Dist.CLIENT)
public class ClientProxyImpl extends ClientProxyAbstractImpl{
    @Override
    public Player getClientPlayer() {
        return Minecraft.getInstance().player;
    }
}
