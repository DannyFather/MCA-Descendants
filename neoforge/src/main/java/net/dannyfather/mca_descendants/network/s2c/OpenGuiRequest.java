package net.dannyfather.mca_descendants.network.s2c;

import net.dannyfather.mca_descendants.ClientProxy;
import net.dannyfather.mca_descendants.MCADescendants;
import net.dannyfather.mca_descendants.network.HandleablePayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.function.Supplier;

public record OpenGuiRequest(int gui, int villager) implements HandleablePayload {
    public static final CustomPacketPayload.Type<OpenGuiRequest> TYPE = new CustomPacketPayload.Type<>(MCADescendants.locate("open_gui_request"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenGuiRequest> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, OpenGuiRequest::gui,
                    ByteBufCodecs.INT, OpenGuiRequest::villager,
                    OpenGuiRequest::new
            );

    public OpenGuiRequest(Type gui, Entity villager) {
        this(gui.ordinal(), villager.getId());
    }

    public OpenGuiRequest(Type gui) {
        this(gui.ordinal(), 0);
    }

    public Type getGui() {
        return Type.values()[gui];
    }

    @Override
    public void handle(Player player) {
        ClientProxy.getNetworkHandler().handleGuiRequest(this);
    }

    @Override
    public CustomPacketPayload.Type<OpenGuiRequest> type() {
        return TYPE;
    }

    public enum Type {
        PHONE,
        PHONEOPTIONS
    }
}