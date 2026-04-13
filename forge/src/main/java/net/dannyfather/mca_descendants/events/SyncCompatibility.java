package net.dannyfather.mca_descendants.events;


import forge.net.mca.server.world.data.FamilyTree;
import forge.net.mca.server.world.data.FamilyTreeNode;
import forge.net.mca.server.world.data.PlayerSaveData;
import net.dannyfather.mca_descendants.MCADescendants;
import net.dannyfather.mca_descendants.config.MCADescendantsCommonConfig;
import net.dannyfather.mca_descendants.config.MCADescendantsServerConfig;
import net.dannyfather.mca_descendants.util.ModUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.sumik.sync.Sync;
import net.sumik.sync.api.event.PlayerSyncEvents;
import net.sumik.sync.api.shell.ServerShell;
import net.sumik.sync.api.shell.Shell;
import net.sumik.sync.api.shell.ShellState;
import net.sumik.sync.common.utils.client.PlayerUtil;
import net.sumik.sync.mixins.sync.client.PlayerEntityModelMixin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.dannyfather.mca_descendants.events.MCADescendantsEvents.*;
import static net.dannyfather.mca_descendants.network.c2s.getDescendantsRequest.getGrandchildren;

@Mod.EventBusSubscriber(modid = "sync", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SyncCompatibility {

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {

        if (ModList.get().isLoaded("sync")) {
            if (event.getEntity() instanceof ServerPlayer player && event.getEntity().level() instanceof ServerLevel serverLevel) {
                if (serverLevel.getLevelData().isHardcore() || !MCADescendantsCommonConfig.HARDCORE_ONLY.get() || !MCADescendantsServerConfig.SERVER.SERVER_HARDCORE_ONLY.get()) {
                    int shellCount = 0;

                    if (player instanceof Shell shell) {
                        shellCount = ((int) shell.getAvailableShellStates().count());
                    }

                    if (shellCount == 0) {
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
                    }

                }

            }
        }
    }
}
