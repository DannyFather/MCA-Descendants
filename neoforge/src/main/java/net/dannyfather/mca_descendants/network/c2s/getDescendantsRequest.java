package net.dannyfather.mca_descendants.network.c2s;

import net.conczin.mca.entity.VillagerLike;
import net.conczin.mca.server.world.data.FamilyTree;
import net.conczin.mca.server.world.data.FamilyTreeNode;
import net.conczin.mca.server.world.data.PlayerSaveData;
import net.dannyfather.mca_descendants.MCADescendants;
import net.dannyfather.mca_descendants.config.MCADescendantsCommonConfig;
import net.dannyfather.mca_descendants.network.HandleablePayload;
import net.dannyfather.mca_descendants.network.ModNetwork;
import net.dannyfather.mca_descendants.network.s2c.getDescendantResponse;
import net.dannyfather.mca_descendants.server.world.data.DescendantLocationData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ChunkPos;

import java.io.Serial;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

public record getDescendantsRequest() implements HandleablePayload {
    public static final CustomPacketPayload.Type<getDescendantsRequest> TYPE = new CustomPacketPayload.Type<>(MCADescendants.locate("get_descendant_request"));
    public static final StreamCodec<FriendlyByteBuf, getDescendantsRequest> STREAM_CODEC = StreamCodec.unit(new getDescendantsRequest());
    //public static Map<UUID,String> villagerNames = new HashMap<>();

    public static Set<UUID> getGrandchildren(FamilyTreeNode node, ServerLevel serverLevel){
        Iterator<UUID> childrenIterator = node.getChildren().iterator();
        FamilyTree tree = FamilyTree.get(serverLevel);
        Set<UUID> grandchildrenSet = new HashSet<>();
        while (childrenIterator.hasNext()) {
            FamilyTreeNode childNode = tree.getOrEmpty(childrenIterator.next()).get();
            Iterator<UUID> grandchildrenIterator = childNode.getChildren().iterator();
            while (grandchildrenIterator.hasNext()) {
                grandchildrenSet.add(grandchildrenIterator.next());
            }
        }
        return grandchildrenSet;
    }

    public static Set<UUID> getValidRespawnCandidates(FamilyTreeNode node, ServerLevel serverLevel){
        Set<UUID> dSet = new HashSet<>();
        dSet.addAll(getGrandchildren(node,serverLevel));
        if(MCADescendantsCommonConfig.PLAY_AS_SIBLINGS.get()) {
            dSet.addAll(node.siblings());
        }
        return dSet;
    }

    @Override
    public void handleServer(ServerPlayer player) {
        CompoundTag familyData = new CompoundTag();
        Map<UUID,String> villagerNames = new HashMap<>();
        FamilyTree tree = FamilyTree.get(player.serverLevel());
        FamilyTreeNode soulNode = tree.getOrCreate(player);
        if(player.hasCustomName() && player.getCustomName().getString().equals("\uD83D\uDC7B")) {
            soulNode = tree.getOrEmpty(PlayerSaveData.get(player).getEntityData().getUUID("UUID")).get();
        }
        FamilyTreeNode playerNode = soulNode;

        MinecraftServer server = player.server;

        server.getAllLevels().forEach(level ->{
            DescendantLocationData data = DescendantLocationData.get(level);
            Map<UUID,Map<String, BlockPos>> playerdata = data.get(player.getUUID());
            playerdata.keySet().forEach(uuidkey -> {
                Map<String,BlockPos> villagerData = playerdata.get(uuidkey);
                villagerData.keySet().forEach(name -> {
                    villagerNames.put(uuidkey,name);
                    ChunkPos chunkPos = new ChunkPos(villagerData.get(name));
                    level.setChunkForced(chunkPos.x,chunkPos.z,true);
                });
            });

            Stream.concat(
                            playerNode.getChildren(),
                            getValidRespawnCandidates(playerNode,level).stream()
                    ).distinct()
                    .map(level::getEntity)
                    .filter(e -> e instanceof VillagerLike<?>)
                    .filter(Entity::isAlive)
                    .filter(e -> {
                        if(e instanceof LivingEntity livingEntity) {
                                return !livingEntity.isBaby() || !MCADescendantsCommonConfig.ADULTS_ONLY.get();
                        } else {return false;}
                    })
                    .limit(100)
                    .forEach(e -> {
                        CompoundTag nbt = new CompoundTag();
                        ((Mob) e).addAdditionalSaveData(nbt);
                        nbt.remove("Brain");
                        nbt.remove("Memories");
                        nbt.remove("Inventory");
                        nbt.putString("name",villagerNames.get(e.getUUID()));
                        familyData.put(e.getUUID().toString(), nbt);
                    });
        });
        ModNetwork.sendToPlayer(new getDescendantResponse(familyData), player);
    }

    @Override
    public Type<getDescendantsRequest> type() {
        return TYPE;
    }
}

