package net.dannyfather.mca_descendants.events;

import net.dannyfather.mca_descendants.ClientProxy;
import net.dannyfather.mca_descendants.block.ModBlocks;
import net.dannyfather.mca_descendants.client.gui.PhoneScreen;
import net.dannyfather.mca_descendants.network.s2c.OpenGuiRequest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import static net.dannyfather.MCADescendants.MOD_ID;

@EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {

        ItemBlockRenderTypes.setRenderLayer(
                ModBlocks.DECAYED_GRASS.get(),
                RenderType.cutout()
        );

        BlockColors blockColors =
                Minecraft.getInstance().getBlockColors();

        blockColors.register((state, world, pos, tintIndex) -> {

            if (tintIndex == 0) {
                return world != null && pos != null
                        ? BiomeColors.getAverageGrassColor(world, pos)
                        : 0x00FF00;
            }

            if (tintIndex == 1) {
                return world != null && pos != null
                        ? BiomeColors.getAverageGrassColor(world, pos)
                        : 0x00FF00;
            }

            return 0x777784;

        }, ModBlocks.DECAYED_GRASS.get());

        ItemColors itemColors =
                Minecraft.getInstance().getItemColors();

        itemColors.register((stack, tintIndex) ->
                        0x777784,
                ModBlocks.DECAYED_GRASS.get()
        );
        new ClientProxy.Impl();

    }

    public static void openPhoneScreen() {
        Minecraft.getInstance().setScreen(new PhoneScreen());
    }

}
