package net.dannyfather.mca_descendants;

import com.mojang.logging.LogUtils;
import net.dannyfather.mca_descendants.block.ModBlocks;
import net.dannyfather.mca_descendants.client.gui.PhoneScreen;
import net.dannyfather.mca_descendants.config.MCADescendantsCommonConfig;
import net.dannyfather.mca_descendants.effects.ModEffects;
import net.dannyfather.mca_descendants.events.ClientModEvents;
import net.dannyfather.mca_descendants.item.ModItems;
import net.dannyfather.mca_descendants.network.*;
import net.dannyfather.mca_descendants.network.c2s.CallToPlayerMessage;
import net.dannyfather.mca_descendants.network.c2s.getDescendantsRequest;
import net.dannyfather.mca_descendants.network.s2c.OpenGuiRequest;
import net.dannyfather.mca_descendants.network.s2c.getDescendantResponse;
import net.dannyfather.mca_descendants.sound.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

@Mod(MCADescendants.MOD_ID)
public class MCADescendants {


    public static final String MOD_ID = "mca_descendants";
    private static final Logger LOGGER = LogUtils.getLogger();

    public MCADescendants(IEventBus modEventBus, ModContainer modContainer) {

        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(this::onRegisterPayloadHandlers);

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModSounds.register(modEventBus);
        ModEffects.register(modEventBus);


        modContainer.registerConfig(
                ModConfig.Type.COMMON,
                MCADescendantsCommonConfig.SPEC
        );

    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.VILLAGERGRABBER);
            event.accept(ModItems.EVILVILLAGERGRABBER);
            event.accept(ModItems.GOODVILLAGERGRABBER);
        }
    }

    @SubscribeEvent
    public void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {

        PayloadRegistrar registrar = event.registrar(MOD_ID);

        registrar.playToClient(
                OpenGuiRequest.TYPE,
                OpenGuiRequest.STREAM_CODEC,
                (payload, ctx) -> {
                    ctx.enqueueWork(ClientModEvents::openPhoneScreen);
                }
        );

        registrar.playToClient(
                getDescendantResponse.TYPE,
                getDescendantResponse.STREAM_CODEC,
                (msg, ctx) -> ctx.enqueueWork(msg::handleClient)
        );


        registrar.playToServer(
                getDescendantsRequest.TYPE,
                getDescendantsRequest.STREAM_CODEC,
                (payload, ctx) -> {
                    ctx.enqueueWork(() -> {
                        payload.handle(ctx.player());
                    });
                }
        );

        registrar.playToServer(
                CallToPlayerMessage.TYPE,
                CallToPlayerMessage.STREAM_CODEC,
                (msg, ctx) -> ctx.enqueueWork(() -> msg.handleServer((ServerPlayer) ctx.player()))
        );

        ModNetwork.registerSender(PacketDistributor::sendToPlayer);
        ModNetwork.registerClientSender(PacketDistributor::sendToServer);

        LOGGER.info("Networking registered for MCA Descendants");
    }

    public static ResourceLocation locate(String id) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, id);
    }


}