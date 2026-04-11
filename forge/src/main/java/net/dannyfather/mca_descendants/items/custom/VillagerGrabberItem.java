package net.dannyfather.mca_descendants.items.custom;

import net.dannyfather.mca_descendants.util.ModUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;


public class VillagerGrabberItem extends Item {

    public VillagerGrabberItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(ItemStack pStack, Player pPlayer, LivingEntity target, InteractionHand hand) {
        if (pStack.getItem() instanceof VillagerGrabberItem) {
            ModUtils.swapVillagerAndPlayer(target,pPlayer);
        }
        return InteractionResult.SUCCESS;
    }
}
