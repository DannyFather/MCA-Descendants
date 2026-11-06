package net.dannyfather.mca_descendants.events;

import com.majesttyx.mcacapitals.house.PlayerHouseService;
import net.conczin.mca.server.world.data.FamilyTree;
import net.dannyfather.mca_butchery.network.VillagerSlimPayload;
import net.dannyfather.mca_descendants.MCADescendants;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.stirdrem.overgeared.components.ModComponents;

@EventBusSubscriber(modid = MCADescendants.MOD_ID)
public class OverGearedCompat {
    @SubscribeEvent
    public static void tickEvent(EntityTickEvent.Post event) {
        if(ModList.get().isLoaded("overgeared")) {
            if(event.getEntity() instanceof ServerPlayer player) {
                if (player.tickCount % 200 == 0) {
                    ItemStack item = player.getMainHandItem();
                    if (item.has(ModComponents.CREATOR)) {
                        String creator = item.get(ModComponents.CREATOR);
                        String treeName = FamilyTree.get(player.serverLevel()).getOrCreate(player).getName();
                        if(ModList.get().isLoaded("mcacapitals")) {
                            String houseName = PlayerHouseService.get(player.serverLevel(), player.getUUID()).getHouseName();
                            treeName = treeName + " " + houseName;
                        }if (creator != null && treeName != null) {
                            if (creator.equals(player.getGameProfile().getName()) && !creator.equals(treeName)) {
                                item.set(ModComponents.CREATOR, treeName);
                                player.setItemInHand(InteractionHand.MAIN_HAND, item);
                            }
                        }
                    }
                }
            }
        }
    }
}
