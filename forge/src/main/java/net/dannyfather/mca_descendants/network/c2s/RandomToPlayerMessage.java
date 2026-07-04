package net.dannyfather.mca_descendants.network.c2s;

import forge.net.mca.cobalt.network.NetworkHandler;
import forge.net.mca.entity.EntitiesMCA;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.VillagerLike;
import forge.net.mca.entity.ai.relationship.AgeState;
import forge.net.mca.entity.ai.relationship.Gender;
import forge.net.mca.network.s2c.PlayerDataMessage;
import forge.net.mca.server.ServerInteractionManager;
import forge.net.mca.server.world.data.PlayerSaveData;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.core.IDataStorage;
import harmonised.pmmo.network.Networking;
import harmonised.pmmo.network.clientpackets.CP_SyncData_ClearXp;
import net.dannyfather.mca_descendants.config.MCADescendantsCommonConfig;
import net.dannyfather.mca_descendants.events.MCAGrowthEvents;
import net.dannyfather.mca_descendants.util.ModUtils;
import net.dannyfather.mca_descendants.worldgen.teleporters.SimpleTeleporter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.UUID;
import java.util.function.Supplier;

import static net.dannyfather.mca_descendants.config.MCADescendantsCommonConfig.INSTANT_GROWTH;
import static net.dannyfather.mca_descendants.config.MCADescendantsCommonConfig.RESET_PMMO_STATS;

public class RandomToPlayerMessage {

    public RandomToPlayerMessage() {
    }

    public RandomToPlayerMessage(FriendlyByteBuf buf) {

    }

    public void encode(FriendlyByteBuf buf) {

    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            MinecraftServer server = player.getServer();
            ServerLevel serverLevel = server.getLevel(ServerLevel.OVERWORLD);
            if(serverLevel != null) {
                BlockPos respawnPos = serverLevel.getSharedSpawnPos();

                player.setRespawnPosition(ServerLevel.OVERWORLD, respawnPos, 0, true, false);
                player.changeDimension(
                        serverLevel,
                        new SimpleTeleporter(respawnPos.getX(),respawnPos.getY(),respawnPos.getZ())
                );
                EntityType<VillagerEntityMCA> entity = EntitiesMCA.FEMALE_VILLAGER.get();
                Gender gender = Gender.FEMALE;
                boolean isMale = RandomSource.create().nextBoolean();
                if (isMale) {
                    entity = EntitiesMCA.MALE_VILLAGER.get();
                    gender = Gender.MALE;
                }
                VillagerEntityMCA randomVillager = new VillagerEntityMCA(entity,serverLevel,gender);

                double adultChance = 1 - (MCADescendantsCommonConfig.TEEN_SPAWN_PERCENTAGE.get() * 0.01D);
                boolean randomVar = RandomSource.create().nextFloat() < adultChance;
                if(randomVar) {
                    randomVillager.setAgeState(AgeState.ADULT);
                    randomVillager.setAge(0);
                } else {
                    randomVillager.setAgeState(AgeState.TEEN);
                    randomVillager.setAge(- RandomSource.create().nextInt(90000));
                }
                randomVillager.getTraits().randomize();
                randomVillager.getGenetics().randomize();
                randomVillager.randomizeClothes();
                randomVillager.randomizeHair();
                randomVillager.moveTo(player.blockPosition().getCenter());
                Component rVName = randomVillager.getCustomName();
                CompoundTag rVTag = new CompoundTag();
                randomVillager.save(rVTag);
                serverLevel.players().forEach(p ->
                        NetworkHandler.sendToPlayer(
                                new PlayerDataMessage(player.getUUID(), rVTag),
                                p
                        )
                );
                PlayerSaveData.get(player).setEntityData(rVTag);
                player.setCustomName(rVName);
                player.setGameMode(server.getDefaultGameType());
                ServerInteractionManager.launchDestiny(player);
            }


            ctx.get().setPacketHandled(true);
        });
    }
}