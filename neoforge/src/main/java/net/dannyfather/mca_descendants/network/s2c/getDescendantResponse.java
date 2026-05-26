package net.dannyfather.mca_descendants.network.s2c;

import net.dannyfather.mca_descendants.ClientProxy;
import net.dannyfather.mca_descendants.MCADescendants;
import net.dannyfather.mca_descendants.network.HandleablePayload;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public record getDescendantResponse(CompoundTag nbt) implements HandleablePayload {
    public static final CustomPacketPayload.Type<getDescendantResponse> TYPE = new CustomPacketPayload.Type<>(MCADescendants.locate("get_descendant_response"));
    public static final StreamCodec<FriendlyByteBuf, getDescendantResponse> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG, getDescendantResponse::nbt,
            getDescendantResponse::new
    );

    public CompoundTag getData() {
        return nbt;
    }

    public void handleClient() {
        ClientProxy.getNetworkHandler().handleDescendantDataResponse(this);

    }

    @Override
    public CustomPacketPayload.Type<getDescendantResponse> type() {
        return TYPE;
    }
}