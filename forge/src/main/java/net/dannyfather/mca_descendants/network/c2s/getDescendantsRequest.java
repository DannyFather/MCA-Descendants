package net.dannyfather.mca_descendants.network.c2s;

import forge.net.mca.entity.VillagerLike;
import forge.net.mca.server.world.data.FamilyTree;
import forge.net.mca.server.world.data.FamilyTreeNode;
import forge.net.mca.server.world.data.PlayerSaveData;
import net.dannyfather.mca_descendants.config.MCADescendantsCommonConfig;
import net.dannyfather.mca_descendants.network.ModNetwork;
import net.dannyfather.mca_descendants.network.s2c.getDescendantResponse;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import org.apache.logging.log4j.core.jmx.Server;
import org.objectweb.asm.commons.SerialVersionUIDAdder;

import java.io.Serial;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Stream;import static forge.net.mca.MCAClient.playerData;

public class getDescendantsRequest {
    @Serial
    private static final long serialVersionUID = 5658784094255697697L;

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

    public getDescendantsRequest() {}

    public getDescendantsRequest(FriendlyByteBuf buf) {}

    public void encode(FriendlyByteBuf buf) {}

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            CompoundTag familyData = new CompoundTag();
            FamilyTree tree = FamilyTree.get(player.serverLevel());
            FamilyTreeNode playerNode = tree.getOrEmpty(PlayerSaveData.get(player).getEntityData().getUUID("UUID")).get();

            MinecraftServer server = player.server;

            server.getAllLevels().forEach(level -> {

                Stream.concat(
                                playerNode.streamChildren(),
                                getValidRespawnCandidates(playerNode,level).stream()
                        ).distinct()
                        .map(level::getEntity)
                        .filter(e -> e instanceof VillagerLike<?>)
                        .limit(100)
                        .forEach(e -> {
                            CompoundTag nbt = new CompoundTag();
                            ((Entity)e).save(nbt);
                            nbt.remove("Brain");
                            nbt.remove("memories");
                            nbt.remove("Inventory");
                            familyData.put(e.getUUID().toString(), nbt);
                        });
            });

            ModNetwork.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new getDescendantResponse(familyData)
            );
        });

        ctx.get().setPacketHandled(true);
    }
    }

