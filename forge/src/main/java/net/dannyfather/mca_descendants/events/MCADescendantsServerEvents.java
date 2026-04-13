package net.dannyfather.mca_descendants.events;

import forge.net.mca.server.world.data.FamilyTree;
import forge.net.mca.server.world.data.FamilyTreeNode;
import forge.net.mca.server.world.data.PlayerSaveData;
import net.dannyfather.mca_descendants.MCADescendants;
import net.dannyfather.mca_descendants.config.MCADescendantsCommonConfig;
import net.dannyfather.mca_descendants.config.MCADescendantsServerConfig;
import net.dannyfather.mca_descendants.effects.ModEffects;
import net.dannyfather.mca_descendants.util.ModUtils;
import net.dannyfather.mca_descendants.world.StructureSpawnData;
import net.dannyfather.mca_descendants.worldgen.teleporters.SimpleTeleporter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.dannyfather.mca_descendants.events.MCADescendantsEvents.*;
import static net.dannyfather.mca_descendants.network.c2s.getDescendantsRequest.getGrandchildren;
import static net.minecraft.ChatFormatting.DARK_AQUA;

@Mod.EventBusSubscriber(value = Dist.DEDICATED_SERVER,modid = MCADescendants.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MCADescendantsServerEvents {
    @SubscribeEvent
    public static void TickEvent(LivingEvent.LivingTickEvent event) {
        Entity entity = event.getEntity();
        ServerLevel serverLevel = entity.getServer().getLevel(entity.level().dimension());
            Scoreboard scoreboard = serverLevel.getScoreboard();
            PlayerTeam ghostTeam = scoreboard.getPlayerTeam("ghosts");
            if(entity instanceof ServerPlayer serverPlayer) {
                FamilyTree tree = FamilyTree.get(serverLevel);
                FamilyTreeNode playerNode = tree.getOrCreate(serverPlayer);
                if(PlayerSaveData.get(serverPlayer).getEntityData().getString("villagerName").equals("Soul")) {
                    ResourceKey<Level> targetDimension = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(MCADescendants.MODID, "afterlife"));
                    ServerLevel tpDim = serverPlayer.server.getLevel(targetDimension);
                    if (!serverPlayer.serverLevel().equals(tpDim) && serverPlayer.isAlive()) {
                        String soulName = LAST_VILLAGER_NAME.get(serverPlayer.getUUID());
                        assert ghostTeam != null;
                        scoreboard.addPlayerToTeam(serverPlayer.getName().getString(), ghostTeam);


                        BlockPos spawnPos = new BlockPos(16, 301, 6);
                        assert tpDim != null;
                        tpDim.setChunkForced(spawnPos.getX() >> 4, spawnPos.getZ() >> 4, true);
                        serverPlayer.changeDimension(tpDim, new SimpleTeleporter(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ()));
                        ResourceLocation structureId = new ResourceLocation(MCADescendants.MODID, "waiting_room");
                        StructureTemplate template = serverLevel.getStructureManager().get(structureId).orElse(null);
                        StructureSpawnData structureSpawnData = StructureSpawnData.get(serverLevel);
                        if (!structureSpawnData.hasSpawned()) {
                            if (template != null) {
                                BlockPos pos = new BlockPos(0, 300, 0);

                                StructurePlaceSettings settings = new StructurePlaceSettings()
                                        .setRotation(Rotation.NONE)
                                        .setMirror(Mirror.NONE)
                                        .setIgnoreEntities(false);

                                template.placeInWorld(
                                        tpDim,
                                        pos,
                                        pos,
                                        settings,
                                        tpDim.getRandom(),
                                        2 // flags (2 = update neighbors)
                                );

                                structureSpawnData.setSpawned();
                            }
                        }
                        BlockPos lecternPos = new BlockPos(10, 303, 21);
                        ModUtils.placeBookOnLectern(tpDim, lecternPos, soulName, CHILDREN_COUNT.get(serverPlayer.getUUID()), GRANDCHILDREN_COUNT.get(serverPlayer.getUUID()), serverPlayer);
                        serverPlayer.setGameMode(GameType.ADVENTURE);
                    }
                }
                if(!PlayerSaveData.get(serverPlayer).getEntityData().getString("villagerName").equals(playerNode.getName())) {
                    playerNode.setName(PlayerSaveData.get(serverPlayer).getEntityData().getString("villagerName"));
                }
            }

    }


}
