package net.dannyfather.mca_descendants.network.s2c;

import net.dannyfather.mca_descendants.ClientProxy;
import net.dannyfather.mca_descendants.client.gui.PhoneScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
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

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft.getInstance().setScreen(new PhoneScreen(villager));
        });
        ctx.get().setPacketHandled(true);
    }

    public Type getGui() {
        return Type.values()[gui];
    }

    public enum Type {
        PHONE
    }
}