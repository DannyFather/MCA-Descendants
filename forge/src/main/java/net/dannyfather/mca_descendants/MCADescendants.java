package net.dannyfather.mca_descendants;

import com.mojang.logging.LogUtils;

import net.dannyfather.mca_descendants.block.ModBlocks;
import net.dannyfather.mca_descendants.config.MCADescendantsCommonConfig;
import net.dannyfather.mca_descendants.config.MCADescendantsServerConfig;
import net.dannyfather.mca_descendants.effects.ModEffects;
import net.dannyfather.mca_descendants.items.ModCreativeModeTabs;
import net.dannyfather.mca_descendants.items.ModItems;
import net.dannyfather.mca_descendants.network.ClientInteractionManager;
import net.dannyfather.mca_descendants.network.ClientInteractionManagerImpl;
import net.dannyfather.mca_descendants.network.ModNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.WritableBookItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(MCADescendants.MODID)
public class MCADescendants
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "mca_descendants";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public MCADescendants(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModEffects.register(modEventBus);
        ModCreativeModeTabs.register(modEventBus);
        modEventBus.addListener(this::commonSetup);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, MCADescendantsCommonConfig.SPEC,"mca-descendants.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, MCADescendantsServerConfig.SERVER_SPEC,"mca-descendants-server.toml");

        MinecraftForge.EVENT_BUS.register(this);

        modEventBus.addListener(this::addCreative);

    }

    @SubscribeEvent
    public void commonSetup(FMLCommonSetupEvent event) {
        ModNetwork.register();
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.DECAYED_GRASS.get(), RenderType.cutout());
            BlockColors blockColors = Minecraft.getInstance().getBlockColors();

            blockColors.register((state, world, pos, tintIndex) -> {
                if (tintIndex == 0) { // top layer
                    return world != null && pos != null ? BiomeColors.getAverageGrassColor(world, pos) : 0x00FF00;
                } else if (tintIndex == 1) { // overlay
                    return world != null && pos != null ? BiomeColors.getAverageGrassColor(world, pos) : 0x00FF00;
                }
                return 0x777784; // fallback
            }, ModBlocks.DECAYED_GRASS.get());
            ItemColors itemColors = Minecraft.getInstance().getItemColors();

            itemColors.register((stack, tintIndex) -> {
                return 0x777784;
            }, ModBlocks.DECAYED_GRASS.get());
            event.enqueueWork(() -> {

                ClientProxy.init(new ClientProxy.Impl() {

                    private final ClientInteractionManager manager = new ClientInteractionManagerImpl();

                    @Override
                    public Player getClientPlayer() {
                        return Minecraft.getInstance().player;
                    }

                    @Override
                    public ClientInteractionManager getNetworkHandler() {
                        return manager;
                    }
                });

            });
        }
    }
}
