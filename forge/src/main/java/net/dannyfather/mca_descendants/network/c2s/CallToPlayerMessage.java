package net.dannyfather.mca_descendants.network.c2s;

import forge.net.mca.entity.VillagerEntityMCA;
import net.dannyfather.mca_descendants.util.ModUtils;
import net.dannyfather.mca_descendants.worldgen.teleporters.SimpleTeleporter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class CallToPlayerMessage {

    private final UUID uuid;

    public CallToPlayerMessage(UUID uuid) {
        this.uuid = uuid;
    }

    public CallToPlayerMessage(FriendlyByteBuf buf) {
        this.uuid = buf.readUUID();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(uuid);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            //search all dimensions
            Entity e = null;
            for (ServerLevel level : player.server.getAllLevels()) {
                e = level.getEntity(uuid);
                if (e != null) break;
            }

            if (!(e instanceof VillagerEntityMCA v)) {
                System.out.println("Entity not found!");
                return;
            }

            if (v.isSleeping()) {
                v.stopSleeping();
            }

            v.stopRiding();

            if (!(v.level() instanceof ServerLevel targetLevel)) {
                return;
            }

            if (v.isBaby()) {
                v.setAge(0);
            }

            for (int k = 0; k < 27; k++) {
                player.getInventory().add(v.getInventory().getItem(k));
            }

            player.changeDimension(
                    targetLevel,
                    new SimpleTeleporter(v.getX(), v.getY(), v.getZ())
            );
            ModUtils.goodSwapVillagerAndPlayer(v, player);

            MinecraftServer server = player.server;
            if(ModList.get().isLoaded("corpse")){
                server.getAllLevels().forEach(level -> {
                    level.getAllEntities().forEach( entity -> {
                        CompoundTag entityNBT = entity.serializeNBT();
                        if(entityNBT.getString("id").equals("corpse:corpse")) {
                            if(entityNBT.getInt("Age") < 72000) {
                                entityNBT.putInt("Age",72000);
                            }
                            entity.deserializeNBT(entityNBT);
                        }
                    }
                    );
                });

            }

            ModUtils.removeStats(player);
        });

        ctx.get().setPacketHandled(true);
    }
}