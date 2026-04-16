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
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.WritableBookItem;
import net.minecraft.world.item.WrittenBookItem;
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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.core.jmx.Server;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.dannyfather.mca_descendants.network.c2s.getDescendantsRequest.getGrandchildren;
import static net.minecraft.ChatFormatting.DARK_AQUA;

@Mod.EventBusSubscriber(modid = MCADescendants.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MCADescendantsEvents {
    public static final Map<UUID, String> LAST_DEATH_MESSAGE = new HashMap<>();
    public static final Map<UUID, String> LAST_VILLAGER_NAME = new HashMap<>();
    public static final Map<UUID, Integer> CHILDREN_COUNT = new HashMap<>();
    public static final Map<UUID, Integer> GRANDCHILDREN_COUNT = new HashMap<>();

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if(event.getEntity() instanceof ServerPlayer player && player.level() instanceof ServerLevel serverLevel) {
            if(serverLevel.getLevelData().isHardcore() || !MCADescendantsCommonConfig.HARDCORE_ONLY.get() || !MCADescendantsServerConfig.SERVER.SERVER_HARDCORE_ONLY.get()) {
                if (!ModList.get().isLoaded("sync")) {
                    FamilyTree tree = FamilyTree.get(serverLevel);
                    FamilyTreeNode playerNode = tree.getOrEmpty(player.getUUID()).get();
                    int childrenCount = playerNode.children().size();
                    CHILDREN_COUNT.put(player.getUUID(),childrenCount);
                    int grandchildrenCount = getGrandchildren(playerNode,serverLevel).size();
                    GRANDCHILDREN_COUNT.put(player.getUUID(),grandchildrenCount);
                    String deathMsg = event.getSource().getLocalizedDeathMessage(player).getString();
                    LAST_DEATH_MESSAGE.put(player.getUUID(),deathMsg);
                    String villagerName = PlayerSaveData.get(player).getEntityData().getString("villagerName");
                    LAST_VILLAGER_NAME.put(player.getUUID(),villagerName);
                    player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY,-1,0,false,false));
                    Entity soul = ModUtils.summonSoul(player,serverLevel);
                    soul.moveTo(player.blockPosition(),player.getYRot(),player.getXRot());
                    serverLevel.addFreshEntity(soul);
                    ModUtils.evilSwapVillagerAndPlayer(((LivingEntity) soul),player);
                    if (ModList.get().isLoaded("corpse")) {
                        serverLevel.getAllEntities().forEach(entity -> {
                            CompoundTag entityNBT = entity.serializeNBT();
                            if (entityNBT.getString("id").equals("corpse:corpse")) {
                                if (entityNBT.getCompound("Death").getString("PlayerName").equals(player.getName().getString())) {
                                    entityNBT.getCompound("Death").putString("PlayerName", LAST_VILLAGER_NAME.get(player.getUUID()));
                                    entity.deserializeNBT(entityNBT);
                                }
                            }
                        });
                    }
                }

            }

        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        Player player = event.getEntity();


        if (event.getLevel() instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            ResourceKey<Level> afterlifeDimension = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(MCADescendants.MODID, "afterlife"));
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
        if (player.isShiftKeyDown()) {
            if (!stack.isEmpty() && player instanceof ServerPlayer serverPlayer) {
                if(stack.getItem() == Items.WRITTEN_BOOK) {
                    CompoundTag tag = stack.getOrCreateTag();
                    String villagerName = PlayerSaveData.get(serverPlayer).getEntityData().getString("villagerName");
                    if (tag.getString("author").equals(player.getName().getString())) {
                        if (villagerName.equals(player.getName().getString())) {
                            tag.putString("author", player.getName().getString() + "_");
                        } else {
                            tag.putString("author", villagerName);
                        }
                        stack.setTag(tag);
                        player.getInventory().setChanged();
                    }
                }

            }
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity().level() instanceof ServerLevel serverLevel && event.getEntity() instanceof ServerPlayer serverPlayer) {
            if(serverLevel.getLevelData().isHardcore() || !MCADescendantsCommonConfig.HARDCORE_ONLY.get()) {
                boolean isDeath = !event.isEndConquered();

                if (isDeath) {
                    String soulName = LAST_VILLAGER_NAME.get(serverPlayer.getUUID());
                    FamilyTree tree = FamilyTree.get(serverLevel);
                    FamilyTreeNode playerNode = tree.getOrCreate(event.getEntity());

                    Scoreboard scoreboard = serverLevel.getScoreboard();

                    PlayerTeam ghostTeam = scoreboard.getPlayerTeam("ghosts");
                    scoreboard.addPlayerToTeam(serverPlayer.getName().getString(), ghostTeam);

                    ResourceKey<Level> targetDimension = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(MCADescendants.MODID, "afterlife"));
                    ServerLevel tpDim = serverPlayer.server.getLevel(targetDimension);
                    BlockPos spawnPos = new BlockPos(16, 301, 6);
                    assert tpDim != null;
                    tpDim.setChunkForced(spawnPos.getX() >> 4, spawnPos.getZ() >> 4, true);
                    serverPlayer.changeDimension(tpDim, new SimpleTeleporter(spawnPos.getX(),spawnPos.getY(),spawnPos.getZ()));
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

        }

    }

    @SubscribeEvent
    public static void TickEvent(LivingEvent.LivingTickEvent event) {
        Entity entity = event.getEntity();
        ResourceKey afterLife = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(MCADescendants.MODID, "afterlife"));
        if(entity.level() instanceof ServerLevel serverLevel) {
            Scoreboard scoreboard = serverLevel.getScoreboard();
            Team ghostTeam = scoreboard.getPlayerTeam("ghosts");

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
                    livingEntity.addEffect(new MobEffectInstance(ModEffects.SPIRIT.get(),-1,0,false,false));
                } else {
                    if(livingEntity.hasEffect(ModEffects.SPIRIT.get())){
                        livingEntity.removeEffect(ModEffects.SPIRIT.get());
                        livingEntity.setGlowingTag(false);
                    }
                    if(!livingEntity.hasEffect(MobEffects.GLOWING)) {
                        livingEntity.setGlowingTag(false);
                    }
                }
            }
            if(entity instanceof ServerPlayer serverPlayer) {
                if(ghostTeam instanceof PlayerTeam playerTeam) {
                    if (!PlayerSaveData.get(serverPlayer).getEntityData().getString("villagerName").equals("Soul")) {
                        scoreboard.removePlayerFromTeam(serverPlayer.getName().getString());
                    } else {
                        scoreboard.addPlayerToTeam(serverPlayer.getName().getString(), playerTeam);
                    }

                }

                FamilyTree tree = FamilyTree.get(serverLevel);
                FamilyTreeNode playerNode = tree.getOrCreate(serverPlayer);
                if(!PlayerSaveData.get(serverPlayer).getEntityData().getString("villagerName").equals(playerNode.getName())) {
                    playerNode.setName(PlayerSaveData.get(serverPlayer).getEntityData().getString("villagerName"));
                }

            }
            else if(!(entity instanceof Player) && entity.serializeNBT().getString("villagerName").equals("Soul")) {
                entity.discard();
            }
        }
        if(entity.level().dimension() == afterLife) {
            if(entity instanceof Bee bee) {
                bee.discard();
            }
        }
    }


}
