package net.dannyfather.mca_descendants.network.s2c;


import forge.net.mca.network.NbtDataMessage;
import net.dannyfather.mca_descendants.ClientProxy;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.io.Serial;
import java.util.function.Supplier;

public class getDescendantResponse {

    private final CompoundTag data;

    public getDescendantResponse(CompoundTag data) {
        this.data = data;
    }

    public getDescendantResponse(FriendlyByteBuf buf) {
        this.data = buf.readNbt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeNbt(data);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientProxy.getNetworkHandler().handleDescendantDataResponse(this);
        });

        ctx.get().setPacketHandled(true);
    }

    public CompoundTag getData() {
        return data;
    }
}