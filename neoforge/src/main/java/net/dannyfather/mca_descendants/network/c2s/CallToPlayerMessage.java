package net.dannyfather.mca_descendants.network.c2s;

import harmonised.pmmo.commands.CmdNodeAdmin;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.core.IDataStorage;
import harmonised.pmmo.network.Networking;
import harmonised.pmmo.network.clientpackets.CP_SyncData_ClearXp;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.network.Network;
import net.conczin.mca.network.c2s.GetFamilyTreeRequest;
import net.conczin.mca.server.world.data.FamilyTree;
import net.dannyfather.mca_descendants.MCADescendants;
import net.dannyfather.mca_descendants.events.MCAGrowthEvents;
import net.dannyfather.mca_descendants.network.HandleablePayload;
import net.dannyfather.mca_descendants.server.world.data.DescendantLocationData;
import net.dannyfather.mca_descendants.util.ModUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.ModList;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import static net.dannyfather.mca_descendants.config.MCADescendantsCommonConfig.*;
import static net.dannyfather.mca_descendants.config.MCADescendantsCommonConfig.ADULT_HEATLH;
import static net.dannyfather.mca_descendants.config.MCADescendantsCommonConfig.ADULT_SPEED;
import static net.dannyfather.mca_descendants.config.MCADescendantsCommonConfig.BABY_HEATLH;
import static net.dannyfather.mca_descendants.config.MCADescendantsCommonConfig.CHILD_HEATLH;
import static net.dannyfather.mca_descendants.config.MCADescendantsCommonConfig.CHILD_SPEED;
import static net.dannyfather.mca_descendants.config.MCADescendantsCommonConfig.TEEN_HEATLH;
import static net.dannyfather.mca_descendants.config.MCADescendantsCommonConfig.TEEN_SPEED;
import static net.dannyfather.mca_descendants.config.MCADescendantsCommonConfig.TODDLER_HEATLH;
import static net.dannyfather.mca_descendants.config.MCADescendantsCommonConfig.TODDLER_SPEED;

public record CallToPlayerMessage(UUID uuid) implements HandleablePayload {
    public static final CustomPacketPayload.Type<CallToPlayerMessage> TYPE = new CustomPacketPayload.Type<>(MCADescendants.locate("call_to_player"));
    public static final StreamCodec<FriendlyByteBuf, CallToPlayerMessage> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, CallToPlayerMessage::uuid,
            CallToPlayerMessage::new
    );

    @Override
    public void handleServer(ServerPlayer player) {
        Entity e = null;

        for (ServerLevel level : player.server.getAllLevels()) {
            e = level.getEntity(uuid);
            if (e != null) break;
        }

        if (e instanceof VillagerEntityMCA v) {
            MinecraftServer server = v.getServer();
            if (v.isSleeping()) {
                v.stopSleeping();
            }
            v.stopRiding();
            if(v.isBaby() && INSTANT_GROWTH.get()) {
                v.setAge(0);
            }
            for (int k = 0; k < 27; k++) {
                player.getInventory().add(v.getInventory().getItem(k));
            }
            ServerLevel targetLevel = (ServerLevel) v.level();


            BlockPos playerBlockPos = player.blockPosition();
            ServerLevel playerLevel = player.serverLevel();



            if(!player.getCustomName().equals("\uD83D\uDC7B"))  {
                Entity soul = ModUtils.summonSoul(player,playerLevel);
                soul.moveTo(playerBlockPos,player.getYRot(),player.getXRot());
                playerLevel.addFreshEntity(soul);
                if (soul instanceof LivingEntity eSoul) {
                    ModUtils.swapVillagerAndPlayer(eSoul,player);
                    FamilyTree.get(playerLevel).remove(eSoul.getUUID());
                }
            } else {
                player.setCustomName(v.getCustomName());
            }

            player.teleportTo(
                    targetLevel,
                    v.getX(),
                    v.getY(),
                    v.getZ(),
                    player.getYRot(),
                    player.getXRot()
            );


            server.execute(()->{
                ModUtils.goodSwapVillagerAndPlayer(v, player);

                Network.sendToServer(new GetFamilyTreeRequest(player.getUUID()));
                ModUtils.removeStats(player);

                if(ModList.get().isLoaded("pmmo") && RESET_PMMO_STATS.get()) {
                    IDataStorage data = Core.get(LogicalSide.SERVER).getData();
                    data.setXpMap(player.getUUID(), new HashMap<>());
                    Networking.sendToClient(new CP_SyncData_ClearXp(""), player);
                }

                player.getServer().getAllLevels().forEach(level -> {
                    DescendantLocationData data = DescendantLocationData.get(level);
                    data.get(player.getUUID()).values().forEach(name -> {
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

                    MCAGrowthEvents.updatePlayerAttributes(player);

                });
            });


        }
    }

    @Override
    public CustomPacketPayload.Type<CallToPlayerMessage> type() {
        return TYPE;
    }
}