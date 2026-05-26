package net.dannyfather.mca_descendants.events;

import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.entity.VillagerLike;
import net.conczin.mca.server.world.data.FamilyTree;
import net.conczin.mca.server.world.data.FamilyTreeNode;
import net.conczin.mca.server.world.data.PlayerSaveData;
import net.dannyfather.mca_descendants.MCADescendants;
import net.dannyfather.mca_descendants.config.MCADescendantsCommonConfig;
import net.dannyfather.mca_descendants.effects.ModEffects;
import net.dannyfather.mca_descendants.server.world.StructureSpawnData;
import net.dannyfather.mca_descendants.server.world.data.DescendantLocationData;
import net.dannyfather.mca_descendants.util.ModUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.*;

import static net.dannyfather.mca_descendants.network.c2s.getDescendantsRequest.getGrandchildren;
import static net.dannyfather.mca_descendants.network.c2s.getDescendantsRequest.getValidRespawnCandidates;
import static net.dannyfather.mca_descendants.util.ModUtils.respawnAfterlife;
import static net.minecraft.ChatFormatting.DARK_AQUA;

@EventBusSubscriber(modid = MCADescendants.MOD_ID)
public class MCADescendantsEvents {
    public static final Map<UUID, DamageSource> LAST_DEATH = new HashMap<>();
    public static final Map<UUID, String> LAST_DEATH_MESSAGE = new HashMap<>();
    public static final Map<UUID, String> LAST_VILLAGER_NAME = new HashMap<>();
    public static final Map<UUID, Integer> CHILDREN_COUNT = new HashMap<>();
    public static final Map<UUID, Integer> GRANDCHILDREN_COUNT = new HashMap<>();
    public static final Map<UUID, Set<UUID>> DESCENDANTS = new HashMap<>();
    private static final Map<UUID, Integer> RESPAWN_TICKS = new HashMap<>();

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if(event.getEntity().level() instanceof ServerLevel serverLevel) {
            if(event.getEntity() instanceof ServerPlayer player) {
                if (serverLevel.getLevelData().isHardcore() || !MCADescendantsCommonConfig.HARDCORE_ONLY.get()) {
                    if (!ModList.get().isLoaded("sync")) {
                        FamilyTree tree = FamilyTree.get(serverLevel);
                        FamilyTreeNode playerNode = tree.getOrEmpty(player.getUUID()).get();
                        int childrenCount = playerNode.children().size();
                        CHILDREN_COUNT.put(player.getUUID(), childrenCount);
                        int grandchildrenCount = getGrandchildren(playerNode, serverLevel).size();
                        GRANDCHILDREN_COUNT.put(player.getUUID(), grandchildrenCount);
                        DamageSource death = event.getSource();
                        String deathMsg = death.getLocalizedDeathMessage(player).getString();
                        LAST_DEATH.put(player.getUUID(), death);
                        LAST_DEATH_MESSAGE.put(player.getUUID(), deathMsg);
                        String villagerName = player.getCustomName().getString();
                        LAST_VILLAGER_NAME.put(player.getUUID(), villagerName);
                        player.setRespawnPosition(player.level().dimension(),player.blockPosition(),0F,true,false);
                        if(player.hasCustomName()) {
                            if(!player.getCustomName().getString().equals("\uD83D\uDC7B")){
                            Entity soul = ModUtils.summonSoul(player, serverLevel);
                            soul.moveTo(player.blockPosition(), player.getYRot(), player.getXRot());
                            serverLevel.addFreshEntity(soul);
                            ModUtils.evilSwapVillagerAndPlayer(((LivingEntity) soul), player, event.getSource());
                            if (ModList.get().isLoaded("corpse")) {
                                serverLevel.getAllEntities().forEach(entity -> {
                                    CompoundTag entityNBT = new CompoundTag();
                                    entity.save(entityNBT);
                                    if (entityNBT.getString("id").equals("corpse:corpse")) {
                                        if (entityNBT.getCompound("Death").getString("PlayerName").equals(player.getName().getString())) {
                                            entityNBT.getCompound("Death").putString("PlayerName", LAST_VILLAGER_NAME.get(player.getUUID()));
                                            entity.load(entityNBT);
                                        }
                                    }
                                });
                            }
                            } else {
                                int deathCount = player.getStats().getValue(Stats.CUSTOM.get(Stats.DEATHS));
                                player.getStats().setValue(player,Stats.CUSTOM.get(Stats.DEATHS),deathCount - 1);
                            }
                        }

                    }

                }

            }


        }
    }


    @SubscribeEvent
    public static void onLeaveEvent(EntityLeaveLevelEvent event) {
        Entity entity = event.getEntity();
        if(event.getLevel() instanceof ServerLevel serverLevel && entity instanceof VillagerLike<?>) {
            DescendantLocationData data = DescendantLocationData.get(serverLevel);
            serverLevel.getPlayers(serverPlayer -> {data.remove(entity,serverPlayer.getUUID());
                return false;
            });
        }
    }



    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        Player player = event.getEntity();


        if (event.getLevel() instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            ResourceKey<Level> afterlifeDimension = ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(MCADescendants.MOD_ID, "afterlife"));
            BlockState state = level.getBlockState(pos);

            if (state.getBlock() instanceof LecternBlock && serverLevel.dimension() == afterlifeDimension) {
                BlockEntity be = level.getBlockEntity(pos);

                if (be instanceof LecternBlockEntity lectern) {
                    ItemStack book = lectern.getBook();

                    if (!book.isEmpty()) {

                        BlockState empty = Blocks.AIR.defaultBlockState();;
                        BlockState newLectern = Blocks.LECTERN.defaultBlockState();
                        serverLevel.setBlock(pos,empty,3);
                        serverLevel.setBlock(pos,newLectern,3);

                        event.setCanceled(true);
                    }
                }
            }
        }


    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {

        Player player = event.getEntity();
        ItemStack stack = player.getMainHandItem();

        if (!player.isShiftKeyDown()) {
            return;
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        if (!stack.is(Items.WRITTEN_BOOK)) {
            return;
        }

        WrittenBookContent content =
                stack.get(DataComponents.WRITTEN_BOOK_CONTENT);

        if (content == null) {
            return;
        }

        String currentAuthor = content.author();

        if (!currentAuthor.equals(player.getName().getString())) {
            return;
        }

        String villagerName =
                PlayerSaveData.get(serverPlayer)
                        .getEntityData()
                        .getString("villagerName");

        String newAuthor;

        if (villagerName.equals(player.getName().getString())) {
            newAuthor = player.getName().getString() + "_";
        } else {
            newAuthor = villagerName;
        }

        WrittenBookContent newContent = new WrittenBookContent(
                content.title(),
                newAuthor,
                content.generation(),
                content.pages(),
                content.resolved()
        );

        stack.set(DataComponents.WRITTEN_BOOK_CONTENT, newContent);

        player.getInventory().setChanged();
    }


    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity().level() instanceof ServerLevel serverLevel && event.getEntity() instanceof ServerPlayer serverPlayer) {
            if(serverLevel.getLevelData().isHardcore() || !MCADescendantsCommonConfig.HARDCORE_ONLY.get()) {
                boolean isDeath = !event.isEndConquered();

                if (isDeath) {
                    RESPAWN_TICKS.put(serverPlayer.getUUID(),80);



                }
            }

        }

    }



    @SubscribeEvent
    public static void TickEvent(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        ResourceKey afterLife = ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(MCADescendants.MOD_ID, "afterlife"));

        if(entity.level() instanceof ServerLevel serverLevel) {
            Scoreboard scoreboard = serverLevel.getScoreboard();
            Team ghostTeam = scoreboard.getPlayerTeam("ghosts");
            DescendantLocationData data = DescendantLocationData.get(serverLevel);
            FamilyTree tree = FamilyTree.get(serverLevel);

            if (ghostTeam == null) {
                scoreboard.addPlayerTeam("ghosts");
                ghostTeam = scoreboard.getPlayerTeam("ghosts");
                if (ghostTeam instanceof PlayerTeam playerTeam){
                    playerTeam.setColor(DARK_AQUA);
                    playerTeam.setAllowFriendlyFire(false);
                }
            }


            if(entity instanceof LivingEntity livingEntity && livingEntity.isAlive()){
                if(entity.getTeam() == ghostTeam){
                    livingEntity.addEffect(new MobEffectInstance(ModEffects.SPIRIT,-1,0,false,false));
                } else {
                    if(livingEntity.hasEffect(ModEffects.SPIRIT)){
                        livingEntity.removeEffect(ModEffects.SPIRIT);
                        livingEntity.setGlowingTag(false);
                    }
                    if(!livingEntity.hasEffect(MobEffects.GLOWING)) {
                        livingEntity.setGlowingTag(false);
                    }
                }
            }

            if(entity instanceof ServerPlayer serverPlayer) {
                FamilyTreeNode playerNode = tree.getOrCreate(serverPlayer);
                if(MCADescendantsCommonConfig.INSTANT_RESPAWN.get()) {
                    serverLevel.getGameRules().getRule(GameRules.RULE_DO_IMMEDIATE_RESPAWN).set(true,serverPlayer.server);
                }


                if (!serverPlayer.hasCustomName() || !serverPlayer.getCustomName().getString().equals(playerNode.getName())) {
                    serverPlayer.setCustomName(Component.literal(playerNode.getName()));
                } else {
                    if (ghostTeam instanceof PlayerTeam playerTeam) {
                        if (!serverPlayer.getCustomName().getString().equals("\uD83D\uDC7B")) {
                            scoreboard.removePlayerFromTeam(serverPlayer.getName().getString());
                        } else {
                            scoreboard.addPlayerToTeam(serverPlayer.getName().getString(), playerTeam);
                        }

                    }
                }
                UUID id = serverPlayer.getUUID();
                Set<UUID> descendantSet = new HashSet<>();
                DESCENDANTS.put(id, descendantSet);
                playerNode.children().forEach(descendantSet::add);
                getValidRespawnCandidates(playerNode, serverLevel).forEach(descendantSet::add);
                for (UUID uuid : descendantSet) {
                    Entity e = serverLevel.getEntity(uuid);
                    if (e instanceof VillagerLike<?>){
                        if(e.isAlive()) {
                            if(serverPlayer.tickCount % 100 == 0) {
                                data.update(e, id);

                            }
                        }
                    }
                }


            }
            if(serverLevel.dimension() == afterLife) {
                if(entity instanceof Bee bee) {
                    bee.discard();
                }else if(entity instanceof VillagerEntityMCA && entity.hasCustomName() && entity.getCustomName().getString().equals("\uD83D\uDC7B")) {
                    FamilyTree.get(serverLevel).remove(entity.getUUID());
                    entity.remove(Entity.RemovalReason.DISCARDED);
                }
            }
        }

    }
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {

        Iterator<Map.Entry<UUID, Integer>> it = RESPAWN_TICKS.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<UUID, Integer> entry = it.next();

            int time = entry.getValue() - 1;

            if (time <= 0) {
                ServerPlayer player = event.getServer().getPlayerList().getPlayer(entry.getKey());
                ServerLevel serverLevel = player.serverLevel();

                if (player != null) {
                    MinecraftServer server = player.getServer();
                    FamilyTree tree = FamilyTree.get(serverLevel);

                    Scoreboard scoreboard = serverLevel.getScoreboard();

                    PlayerTeam ghostTeam = scoreboard.getPlayerTeam("ghosts");
                    scoreboard.addPlayerToTeam(player.getName().getString(), ghostTeam);
                    BlockPos spawnPos = new BlockPos(16, 301, 6);
                    ResourceKey<Level> targetDimension = ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(MCADescendants.MOD_ID, "afterlife"));

                    ServerLevel tpDim = server.getLevel(targetDimension);
                    if (tpDim == null) return;

                    tpDim.getChunkAt(spawnPos);
                    player.getServer().execute(
                                () -> {
                                    respawnAfterlife(player, tpDim, spawnPos, server);
                    });
                }

                it.remove();
            } else {
                entry.setValue(time);
            }
        }
    }

}
