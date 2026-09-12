package net.dannyfather.mca_descendants.events;


import com.majesttyx.mcacapitals.capital.CapitalManager;
import com.majesttyx.mcacapitals.capital.CapitalRecord;
import forge.net.conczin.mca.server.world.data.FamilyTree;
import forge.net.conczin.mca.server.world.data.FamilyTreeNode;
import forge.net.conczin.mca.server.world.data.PlayerSaveData;
import mc.craig.software.regen.common.regen.IRegen;
import mc.craig.software.regen.common.regen.RegenerationData;
import net.dannyfather.mca_descendants.MCADescendants;
import net.dannyfather.mca_descendants.config.MCADescendantsCommonConfig;
import net.dannyfather.mca_descendants.util.ModUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.sumik.sync.api.shell.Shell;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.dannyfather.mca_descendants.events.MCADescendantsEvents.*;
import static net.dannyfather.mca_descendants.network.c2s.getDescendantsRequest.getGrandchildren;


@Mod.EventBusSubscriber(modid = MCADescendants.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Compatibility {


    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (ModList.get().isLoaded("sync") || ModList.get().isLoaded("regen")) {
            boolean isProperlyDead = true;

            if (ModList.get().isLoaded("sync")) {
                isProperlyDead = false;
                if (event.getEntity() instanceof ServerPlayer player && event.getEntity().level() instanceof ServerLevel serverLevel) {
                    if (serverLevel.getLevelData().isHardcore() || !MCADescendantsCommonConfig.HARDCORE_ONLY.get()) {
                        int shellCount = 0;

                        if (player instanceof Shell shell) {
                            shellCount = ((int) shell.getAvailableShellStates().count());
                        }

                        if (shellCount == 0) {
                            isProperlyDead = true;

                        }


                    }

                }
            }
            if (ModList.get().isLoaded("regen")) {
                isProperlyDead = false;
                if (event.getEntity() instanceof ServerPlayer player && event.getEntity().level() instanceof ServerLevel serverLevel) {
                    if (serverLevel.getLevelData().isHardcore() || !MCADescendantsCommonConfig.HARDCORE_ONLY.get()) {
                        IRegen cap = RegenerationData.get(player).orElse(null);
                        if (cap != null && !cap.canRegenerate()) {
                            isProperlyDead = true;

                        }
                    }
                }
            }

            if (isProperlyDead) {
                if (event.getEntity() instanceof ServerPlayer player && event.getEntity().level() instanceof ServerLevel serverLevel) {
                    if (serverLevel.getLevelData().isHardcore() || !MCADescendantsCommonConfig.HARDCORE_ONLY.get()) {
                        player.setRespawnPosition(player.level().dimension(), player.blockPosition(), player.getXRot(), true, false);
                        FamilyTree tree = FamilyTree.get(serverLevel);
                        FamilyTreeNode playerNode = tree.getOrEmpty(player.getUUID()).get();
                        int childrenCount = ((int) playerNode.getChildren().count());
                        int grandchildrenCount = getGrandchildren(playerNode, serverLevel).size();
                        String deathMsg = event.getSource().getLocalizedDeathMessage(player).getString();
                        String villagerName = PlayerSaveData.get(player).getEntityData().getString("villagerName");
                        MinecraftServer server = player.getServer();
                        if (ModList.get().isLoaded("mcacapitals")) {
                            CapitalRecord capital = CapitalManager.getCapitalForResident(player.getUUID());
                            if (capital != null && capital.isPlayerSovereign() && capital.getPlayerSovereignId().equals(player.getUUID())) {
                                capital.setPlayerSovereign(false);
                                capital.setPlayerSovereignId(null);
                                capital.setPlayerSovereignName(null);
                            }
                        }
                        server.execute(() -> {
                            if (!playerNode.getName().equals("Soul")) {
                                CHILDREN_COUNT.put(player.getUUID(), childrenCount);
                                GRANDCHILDREN_COUNT.put(player.getUUID(), grandchildrenCount);
                                LAST_DEATH_MESSAGE.put(player.getUUID(), deathMsg);
                                LAST_VILLAGER_NAME.put(player.getUUID(), villagerName);
                                Entity soul = ModUtils.summonSoul(player, serverLevel);
                                soul.moveTo(player.blockPosition(), player.getYRot(), player.getXRot());
                                serverLevel.addFreshEntity(soul);

                                if (ModList.get().isLoaded("mcacapitals")) {
                                    CapitalRecord capital = CapitalManager.getCapitalForResident(player.getUUID());
                                    if (capital != null && capital.getSovereign().equals(player.getUUID())) {
                                        capital.setSovereign(soul.getUUID());
                                    }
                                }
                                ModUtils.evilSwapVillagerAndPlayer(((LivingEntity) soul), player, event.getSource());

                                if (!serverLevel.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
                                    player.getInventory().dropAll();
                                    int xp = player.getExperienceReward();
                                    if (xp > 0) {
                                        ExperienceOrb.award(serverLevel, player.blockPosition().getCenter(), xp);
                                    }
                                }

                                int deathCount = player.getStats().getValue(Stats.CUSTOM.get(Stats.DEATHS));
                                player.getStats().setValue(player, Stats.CUSTOM.get(Stats.DEATHS), deathCount + 1);
                            }
                        });
                    }
                }
            }
        }
    }


    @SubscribeEvent
    public static void TickEvent(LivingEvent.LivingTickEvent event) {
        Entity entity = event.getEntity();
        if (entity.level() instanceof ServerLevel serverLevel) {
            if (entity instanceof ServerPlayer serverPlayer) {
                if (ModList.get().isLoaded("sync")) {
                    serverLevel.getGameRules().getRule(GameRules.RULE_DO_IMMEDIATE_RESPAWN).set(false, serverPlayer.server);
                }
            }
        }
    }

}