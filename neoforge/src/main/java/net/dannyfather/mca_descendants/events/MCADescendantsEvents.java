package net.dannyfather.mca_descendants.events;

import com.majesttyx.mcacapitals.capital.CapitalManager;
import com.majesttyx.mcacapitals.capital.CapitalRecord;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.entity.VillagerLike;
import net.conczin.mca.entity.ai.Genetics;
import net.conczin.mca.entity.ai.Traits;
import net.conczin.mca.registry.EntitiesMCA;
import net.conczin.mca.server.world.data.FamilyTree;
import net.conczin.mca.server.world.data.FamilyTreeNode;
import net.conczin.mca.server.world.data.PlayerSaveData;
import net.dannyfather.mca_butchery.config.MCAButcheryCommonConfig;
import net.dannyfather.mca_butchery.item.MCAButcheryItems;
import net.dannyfather.mca_butchery.network.DownloadVillagerSkinPayload;
import net.dannyfather.mca_butchery.network.VillagerSlimPayload;
import net.dannyfather.mca_descendants.MCADescendants;
import net.dannyfather.mca_descendants.config.MCADescendantsCommonConfig;
import net.dannyfather.mca_descendants.effects.ModEffects;
import net.dannyfather.mca_descendants.network.ModNetwork;
import net.dannyfather.mca_descendants.network.s2c.OpenGuiRequest;
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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Containers;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

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
    public static final Map<UUID, Integer> RESPAWN_TICKS = new HashMap<>();
    public static final Map<UUID, Integer> LOADING_TICKS = new HashMap<>();


    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if(event.getEntity().level() instanceof ServerLevel serverLevel) {
            if(event.getEntity() instanceof ServerPlayer player) {
                if (serverLevel.getLevelData().isHardcore() || !MCADescendantsCommonConfig.HARDCORE_ONLY.get()) {
                    if (!ModList.get().isLoaded("neosync")) {
                        int model = PlayerSaveData.get(player).getEntityData().getInt("PlayerModel");
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
                        if(ModList.get().isLoaded("mcacapitals")) {
                            CapitalRecord capital = CapitalManager.getCapitalForResident(player.getUUID());
                            if(capital != null && capital.isPlayerSovereign() && capital.getPlayerSovereignId().equals(player.getUUID())){
                                capital.setPlayerSovereign(false);
                                capital.setPlayerSovereignId(null);
                                capital.setPlayerSovereignName(null);
                            }
                        }
                        if(player.hasCustomName()) {
                            if(!player.getCustomName().getString().equals("\uD83D\uDC7B")){
                                Entity soul = ModUtils.summonSoul(player, serverLevel);
                                soul.moveTo(player.blockPosition(), player.getYRot(), player.getXRot());
                                serverLevel.addFreshEntity(soul);
                                if(ModList.get().isLoaded("mcacapitals")) {
                                    CapitalRecord capital = CapitalManager.getCapitalForResident(player.getUUID());
                                    if(capital != null && capital.getSovereign().equals(player.getUUID())){
                                        capital.setSovereign(soul.getUUID());
                                    }
                                }
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
                                if (ModList.get().isLoaded("mca_butchery")) {
                                    ItemStack itemStack = event.getSource().getWeaponItem();
                                    if (itemStack != null && itemStack.is(ItemTags.create(ResourceLocation.parse("c:cleaver")))) {
                                        int entityId = player.getId();
                                        PacketDistributor.sendToPlayer(player, new DownloadVillagerSkinPayload(entityId, model));

                                        BlockPos pos = player.blockPosition();
                                        ItemStack item = MCAButcheryItems.MCAVILLAGERCORPSE.toStack();
                                        if (model == 0) {
                                            CompoundTag pData = new CompoundTag();
                                            VillagerLike.toVillager(player).asEntity().save(pData);
                                            UUID vUUID = UUID.randomUUID();
                                            pData.remove("UUID");
                                            pData.putUUID("UUID", vUUID);
                                            pData.remove("id");
                                            pData.remove("tree_mother_name");
                                            pData.remove("tree_father_name");
                                            pData.remove("tree_spouse_name");
                                            pData.remove("tree_mother_UUID");
                                            pData.remove("tree_father_UUID");
                                            pData.remove("tree_spouse_UUID");
                                            if (PlayerSaveData.get(player).getEntityData().getInt("playerModel") >= 1) {
                                                pData.putString("custom_skin", player.getName().getString());
                                            }
                                            VillagerEntityMCA pVillager = EntitiesMCA.FEMALE_VILLAGER.create(serverLevel);
                                            if (pData.getInt("gender") == 1) {
                                                pVillager = EntitiesMCA.MALE_VILLAGER.create(serverLevel);
                                            }
                                            pVillager.load(pData);
                                            pVillager.setCustomName(Component.literal(FamilyTree.get(serverLevel).getOrCreate(player).getName()));
                                            pVillager.setCustomNameVisible(true);
                                            pVillager.moveTo(player.position());
                                            serverLevel.addFreshEntity(pVillager);
                                            int face = (int) Math.floor((double) pVillager.getGenetics().getGene(Genetics.FACE) * 12);
                                            switch (face) {
                                                case 0, 1, 2, 3, 4, 5, 6 -> {
                                                    face = 2;
                                                }
                                                case 7, 8, 10 -> {
                                                    face = 1;
                                                }
                                                default -> {
                                                    face = 0;
                                                }

                                            }
                                            if (MCAButcheryCommonConfig.CLOSED_EYES.get()) {
                                                face = 0;
                                            }
                                            item.set(MCAButcheryItems.FACE, face);
                                            PacketDistributor.sendToPlayer(player, new DownloadVillagerSkinPayload(pVillager.getId(), 0));
                                            item.set(MCAButcheryItems.VILLAGER_UUID, vUUID);
                                            pVillager.discard();
                                        } else {
                                            PacketDistributor.sendToPlayer(player, new DownloadVillagerSkinPayload(entityId, 1));
                                            item.set(MCAButcheryItems.VILLAGER_UUID, player.getUUID());
                                            boolean isSlim = player.getPersistentData().getBoolean("Slim");
                                            item.set(MCAButcheryItems.SLIM, isSlim);
                                        }
                                        String itemName = Component.translatable("block.mca_butchery.villager_corpse_named").getString().replace("{VILLAGERNAME}", villagerName);
                                        item.set(DataComponents.CUSTOM_NAME, Component.literal(itemName).withStyle(style -> style.withItalic(false)));
                                        if (model != 2) {
                                            Genetics genetics = VillagerLike.toVillager(player).getGenetics();
                                            item.set(MCAButcheryItems.B_SIZE, genetics.getBreastSize());
                                            Traits villagerTraits = VillagerLike.toVillager(player).getTraits();
                                            float hScale = (villagerTraits.hasTrait(Traits.DWARFISM) ? 0.65f : 1.0f) * genetics.getVerticalScaleFactor();
                                            float wScale = (villagerTraits.hasTrait(Traits.DWARFISM) ? 0.85f : 1.0f)
                                                    * (villagerTraits.hasTrait(Traits.TOUGH) ? 1.2F : 1.0F)
                                                    * (villagerTraits.hasTrait(Traits.WEAK) ? 0.85F : 1.0F) * genetics.getHorizontalScaleFactor();
                                            item.set(MCAButcheryItems.H_SIZE, hScale);
                                            item.set(MCAButcheryItems.W_SIZE, wScale);
                                        }
                                        Containers.dropItemStack(serverLevel, pos.getX(), pos.getY(), pos.getZ(), item);
                                        if (!serverLevel.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
                                            player.getInventory().dropAll();
                                            int xp = player.getExperienceReward(serverLevel, event.getSource().getEntity());
                                            if (xp > 0) {
                                                ExperienceOrb.award(serverLevel, player.blockPosition().getCenter(), xp);
                                            }
                                        }
                                        ModUtils.evilSwapVillagerAndPlayer(((LivingEntity) soul), player, player.damageSources().genericKill());
                                    } else {

                                        ModUtils.evilSwapVillagerAndPlayer(((LivingEntity) soul), player, event.getSource());
                                    }
                                } else {

                                    ModUtils.evilSwapVillagerAndPlayer(((LivingEntity) soul), player, event.getSource());
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
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if(ModList.get().isLoaded("mca_butchery")) {
            Player player = event.getEntity();
            if (player instanceof ServerPlayer serverPlayer) {
                int entityId = serverPlayer.getId();
                PacketDistributor.sendToPlayer(serverPlayer, new DownloadVillagerSkinPayload(entityId, 1));
            }
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
            if (serverLevel.getLevelData().isHardcore() || !MCADescendantsCommonConfig.HARDCORE_ONLY.get()) {
                boolean isDeath = !event.isEndConquered();

                if (isDeath) {
                    serverPlayer.setGameMode(GameType.SPECTATOR);
                    RESPAWN_TICKS.put(serverPlayer.getUUID(), 80);


                }
            }

        }

    }



    @SubscribeEvent
    public static void TickEvent(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        ResourceKey afterLife = ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(MCADescendants.MOD_ID, "afterlife"));

        if(entity.level() instanceof ServerLevel serverLevel) {
            if(entity instanceof VillagerEntityMCA villager && villager.getCustomName().getString().equals("\uD83D\uDC7B")){
                entity.remove(Entity.RemovalReason.DISCARDED);
            }

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
                if(MCADescendantsCommonConfig.INSTANT_RESPAWN.get() && !ModList.get().isLoaded("neosync") && (serverLevel.getLevelData().isHardcore() || !MCADescendantsCommonConfig.HARDCORE_ONLY.get())) {
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
        Iterator<Map.Entry<UUID, Integer>> lIt = LOADING_TICKS.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<UUID, Integer> entry = it.next();

            int time = entry.getValue() - 1;

            if (time <= 0) {
                ServerPlayer player = event.getServer().getPlayerList().getPlayer(entry.getKey());
                ServerLevel serverLevel = player.serverLevel();

                if(MCADescendantsCommonConfig.RESPAWN_IN_AFTERLIFE.get()) {
                    if (player != null) {
                        MinecraftServer server = player.getServer();

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
                } else {
                    ModNetwork.sendToPlayer(
                            new OpenGuiRequest(OpenGuiRequest.Type.PHONE),
                            player
                    );
                    player.closeContainer();
                    LOADING_TICKS.put(player.getUUID(),40);
                    ModNetwork.sendToPlayer(
                            new OpenGuiRequest(OpenGuiRequest.Type.PHONE),
                            player
                    );
                }
                it.remove();
            } else {
                entry.setValue(time);
            }
        }
    }

}
