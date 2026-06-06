package net.dannyfather.mca_descendants.network.c2s;

import harmonised.pmmo.core.Core;
import harmonised.pmmo.core.IDataStorage;
import harmonised.pmmo.network.Networking;
import harmonised.pmmo.network.clientpackets.CP_SyncData_ClearXp;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.entity.VillagerLike;
import net.conczin.mca.entity.ai.chatAI.modules.TraitsModule;
import net.conczin.mca.entity.ai.relationship.AgeState;
import net.conczin.mca.entity.ai.relationship.Gender;
import net.conczin.mca.network.Network;
import net.conczin.mca.network.c2s.GetFamilyTreeRequest;
import net.conczin.mca.network.s2c.PlayerDataMessage;
import net.conczin.mca.registry.EntitiesMCA;
import net.conczin.mca.server.ServerInteractionManager;
import net.conczin.mca.server.world.data.FamilyTree;
import net.conczin.mca.server.world.data.FamilyTreeNode;
import net.conczin.mca.server.world.data.PlayerSaveData;
import net.dannyfather.mca_descendants.MCADescendants;
import net.dannyfather.mca_descendants.config.MCADescendantsCommonConfig;
import net.dannyfather.mca_descendants.events.MCAGrowthEvents;
import net.dannyfather.mca_descendants.network.HandleablePayload;
import net.dannyfather.mca_descendants.server.world.data.DescendantLocationData;
import net.dannyfather.mca_descendants.util.ModUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.ModList;

import java.util.HashMap;
import java.util.UUID;
import java.util.function.Supplier;

import static java.util.UUID.randomUUID;
import static net.conczin.mca.entity.ai.Traits.ASEXUAL;
import static net.conczin.mca.entity.ai.Traits.COLOR_BLIND;
import static net.dannyfather.mca_descendants.config.MCADescendantsCommonConfig.RESET_PMMO_STATS;

public class RandomToPlayerMessage implements HandleablePayload {
    public static final CustomPacketPayload.Type<RandomToPlayerMessage> TYPE = new CustomPacketPayload.Type<>(MCADescendants.locate("random_to_player"));
    public static final StreamCodec<FriendlyByteBuf, RandomToPlayerMessage> STREAM_CODEC =
            StreamCodec.of(
                    (buf, msg) -> {},
                    buf -> new RandomToPlayerMessage()
            );

    @Override
    public void handleServer(ServerPlayer serverPlayer) {
            if (serverPlayer == null) return;

            MinecraftServer server = serverPlayer.getServer();
            ServerLevel serverLevel = server.getLevel(ServerLevel.OVERWORLD);
            if(serverLevel != null) {
                BlockPos respawnPos = serverLevel.getSharedSpawnPos();

                serverPlayer.setRespawnPosition(ServerLevel.OVERWORLD, respawnPos, 0, true, false);
                serverPlayer.teleportTo(
                        serverLevel,
                        respawnPos.getX(),
                        respawnPos.getY(),
                        respawnPos.getZ(),
                        0f,
                        0f
                );
                boolean isMale = RandomSource.create().nextBoolean();
                VillagerLike villager = VillagerLike.toVillager(serverPlayer);


                float adultChance = 1 - MCADescendantsCommonConfig.TEEN_SPAWN_PERCENTAGE.get() * 0.01f;
                boolean randomVar = RandomSource.create().nextFloat() < adultChance;
                if(villager instanceof VillagerEntityMCA randomVillager) {
                    if (randomVar) {
                        randomVillager.setAgeState(AgeState.ADULT);
                        randomVillager.setAge(0);
                    } else {
                        randomVillager.setAgeState(AgeState.TEEN);
                        randomVillager.setAge(RandomSource.create().nextInt(90000));
                    }

                    CompoundTag rvData = new CompoundTag();
                    randomVillager.save(rvData);

                    if(isMale) {
                        rvData.putString("id", "mca:male_villager");
                        rvData.putInt("Gender", 1);
                    } else {
                        rvData.putString("id", "mca:female_villager");
                        rvData.putInt("Gender", 2);
                    }

                    randomVillager.load(rvData);

                    randomVillager.getTraits().removeTrait(ASEXUAL);
                    randomVillager.getTraits().removeTrait(COLOR_BLIND);
                    randomVillager.getTraits().randomize();
                    randomVillager.getGenetics().randomize();
                    randomVillager.randomizeClothes();
                    randomVillager.randomizeHair();
                    randomVillager.setUUID(randomUUID());
                    randomVillager.setCustomName(Component.literal(serverPlayer.getGameProfile().getName()));
                    randomVillager.moveTo(serverPlayer.blockPosition().getCenter());
                    serverLevel.addFreshEntity(randomVillager);

                    server.execute(() -> {
                        Network.sendToServer(new GetFamilyTreeRequest(serverPlayer.getUUID()));
                        ModUtils.removeStats(serverPlayer);

                        if(ModList.get().isLoaded("pmmo") && RESET_PMMO_STATS.get()) {
                            IDataStorage data = Core.get(LogicalSide.SERVER).getData();
                            data.setXpMap(serverPlayer.getUUID(), new HashMap<>());
                            Networking.sendToClient(new CP_SyncData_ClearXp(""), serverPlayer);
                        }
                        randomVillager.hurtTime = 0;
                        ModUtils.goodSwapVillagerAndPlayer(randomVillager,serverPlayer);

                        serverPlayer.getServer().getAllLevels().forEach(level -> {
                            DescendantLocationData data = DescendantLocationData.get(level);
                            data.get(serverPlayer.getUUID()).values().forEach(name -> {
                                name.values().forEach(pos -> {
                                    ChunkPos chunkPos = new ChunkPos(pos);

                                    level.setChunkForced(
                                            chunkPos.x,
                                            chunkPos.z,
                                            false
                                    );
                                });

                            });
                            FamilyTree.get(level).getAllWithName("\uD83D\uDC7B").forEach(familyTreeNode -> {
                                FamilyTree.get(level).remove(familyTreeNode.id());
                            });


                        });


                        MCAGrowthEvents.updatePlayerAttributes(serverPlayer);

                        ServerInteractionManager.launchDestiny(serverPlayer);


                    });
                }


            }

    }

    @Override
    public CustomPacketPayload.Type<RandomToPlayerMessage> type() {
        return TYPE;
    }
}