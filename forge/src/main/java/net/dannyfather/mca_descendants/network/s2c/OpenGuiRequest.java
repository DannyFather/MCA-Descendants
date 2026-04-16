package net.dannyfather.mca_descendants.network.s2c;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenGuiRequest {

    public final int gui;
    public final int villager;

    public OpenGuiRequest(Type gui, int villager) {
        this.gui = gui.ordinal();
        this.villager = villager;
    }

    public OpenGuiRequest(FriendlyByteBuf buf) {
        this.gui = buf.readInt();
        this.villager = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(gui);
        buf.writeInt(villager);
    }

    public void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();

        ctx.enqueueWork(() -> {
            net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(
                    net.minecraftforge.api.distmarker.Dist.CLIENT,
                    () -> () -> {
                        net.dannyfather.mca_descendants.client.ClientHandler.openGui(this);
                    }
            );
        });

        ctx.setPacketHandled(true);
    }


    public Type getGui() {
        return Type.values()[gui];
    }

    public enum Type {
        PHONE
    }
}