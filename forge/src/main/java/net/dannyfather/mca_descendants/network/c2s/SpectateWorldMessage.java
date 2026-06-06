package net.dannyfather.mca_descendants.network.c2s;

import forge.net.mca.entity.EntitiesMCA;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.ai.relationship.AgeState;
import forge.net.mca.entity.ai.relationship.Gender;
import forge.net.mca.server.ServerInteractionManager;
import net.dannyfather.mca_descendants.config.MCADescendantsCommonConfig;
import net.dannyfather.mca_descendants.util.ModUtils;
import net.dannyfather.mca_descendants.worldgen.teleporters.SimpleTeleporter;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.GameType;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SpectateWorldMessage {

    public SpectateWorldMessage() {
    }

    public SpectateWorldMessage(FriendlyByteBuf buf) {

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
                player.setGameMode(GameType.SPECTATOR);
            }


            ctx.get().setPacketHandled(true);
        });
    }
}