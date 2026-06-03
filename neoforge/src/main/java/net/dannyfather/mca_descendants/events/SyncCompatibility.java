package net.dannyfather.mca_descendants.events;


import com.breakinblocks.neosync.api.shell.Shell;
import net.conczin.mca.server.world.data.FamilyTree;
import net.conczin.mca.server.world.data.FamilyTreeNode;
import net.conczin.mca.server.world.data.PlayerSaveData;
import net.dannyfather.mca_descendants.MCADescendants;
import net.dannyfather.mca_descendants.config.MCADescendantsCommonConfig;
import net.dannyfather.mca_descendants.effects.ModEffects;
import net.dannyfather.mca_descendants.server.world.data.DescendantLocationData;
import net.dannyfather.mca_descendants.util.ModUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import static net.dannyfather.mca_descendants.events.MCADescendantsEvents.*;
import static net.dannyfather.mca_descendants.network.c2s.getDescendantsRequest.getGrandchildren;
import static net.minecraft.ChatFormatting.DARK_AQUA;


@EventBusSubscriber(modid = MCADescendants.MOD_ID)
public class SyncCompatibility {


    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {

        if (ModList.get().isLoaded("neosync")) {
            if (event.getEntity() instanceof ServerPlayer player && event.getEntity().level() instanceof ServerLevel serverLevel) {
                if (serverLevel.getLevelData().isHardcore() || !MCADescendantsCommonConfig.HARDCORE_ONLY.get()) {
                    boolean hasRespawnShell = true;

                    if (player instanceof Shell shell) {
                        hasRespawnShell = shell.isArtificial() && !player.isSpectator();
                    }

                    if (!hasRespawnShell) {
                        if(MCADescendantsCommonConfig.INSTANT_RESPAWN.get()) {
                            serverLevel.getGameRules().getRule(GameRules.RULE_DO_IMMEDIATE_RESPAWN).set(true, player.server);
                        }
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
                        player.setRespawnPosition(player.level().dimension(), player.blockPosition(), 0F, true, false);
                        if (player.hasCustomName()) {
                            if (!player.getCustomName().getString().equals("\uD83D\uDC7B")) {
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
                                player.getStats().setValue(player, Stats.CUSTOM.get(Stats.DEATHS), deathCount - 1);
                            }
                        }

                    }


                }

            }
        }
    }

    @SubscribeEvent
    public static void TickEvent(EntityTickEvent.Post event) {
        if (ModList.get().isLoaded("neosync")) {
            Entity entity = event.getEntity();
            if (entity.level() instanceof ServerLevel serverLevel) {
                if (entity instanceof ServerPlayer serverPlayer) {
                    serverLevel.getGameRules().getRule(GameRules.RULE_DO_IMMEDIATE_RESPAWN).set(false, serverPlayer.server);
                }
            }
        }
    }

}