package net.dannyfather.mca_descendants.network.c2s;

import net.dannyfather.mca_descendants.MCADescendants;
import net.dannyfather.mca_descendants.network.HandleablePayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

import java.util.function.Supplier;

public class SpectateWorldMessage implements HandleablePayload {
    public static final CustomPacketPayload.Type<SpectateWorldMessage> TYPE = new CustomPacketPayload.Type<>(MCADescendants.locate("spectate_world"));
    public static final StreamCodec<FriendlyByteBuf, SpectateWorldMessage> STREAM_CODEC =
            StreamCodec.of(
                    (buf, msg) -> {},
                    buf -> new SpectateWorldMessage()
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
                serverPlayer.setGameMode(GameType.SPECTATOR);
            }


    }

    @Override
    public CustomPacketPayload.Type<SpectateWorldMessage> type() {
        return TYPE;
    }
}