package net.dannyfather.mca_descendants.network.c2s;

import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.server.world.data.FamilyTree;
import forge.net.mca.server.world.data.PlayerSaveData;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.core.IDataStorage;
import harmonised.pmmo.network.Networking;
import harmonised.pmmo.network.clientpackets.CP_SyncData_ClearXp;
import net.dannyfather.mca_descendants.events.MCAGrowthEvents;
import net.dannyfather.mca_descendants.util.ModUtils;
import net.dannyfather.mca_descendants.worldgen.teleporters.SimpleTeleporter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.UUID;
import java.util.function.Supplier;

import static net.dannyfather.mca_descendants.config.MCADescendantsCommonConfig.INSTANT_GROWTH;
import static net.dannyfather.mca_descendants.config.MCADescendantsCommonConfig.RESET_PMMO_STATS;

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

            if (v.isBaby() && INSTANT_GROWTH.get()) {
                v.setAge(0);
            }

            for (int k = 0; k < 27; k++) {
                player.getInventory().add(v.getInventory().getItem(k));
            }

            BlockPos playerBlockPos = player.blockPosition();
            ServerLevel playerLevel = player.serverLevel();

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
            MCAGrowthEvents.updatePlayerAttributes(player);
            if(ModList.get().isLoaded("pmmo") && RESET_PMMO_STATS.get()) {
                IDataStorage data = Core.get(LogicalSide.SERVER).getData();
                data.setXpMap(player.getUUID(), new HashMap<>());
                Networking.sendToClient(new CP_SyncData_ClearXp(), player);
            }
        });

        ctx.get().setPacketHandled(true);
    }
}