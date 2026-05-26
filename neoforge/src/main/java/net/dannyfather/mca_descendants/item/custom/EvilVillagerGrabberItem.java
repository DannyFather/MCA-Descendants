package net.dannyfather.mca_descendants.item.custom;

import net.dannyfather.mca_descendants.MCADescendants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import static net.dannyfather.mca_descendants.util.ModUtils.evilSwapVillagerAndPlayer;

public class EvilVillagerGrabberItem extends Item {
    public EvilVillagerGrabberItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(ItemStack pStack, Player pPlayer, LivingEntity target, InteractionHand hand) {

        if(pPlayer instanceof ServerPlayer serverPlayer) {
            evilSwapVillagerAndPlayer(target, serverPlayer, target.damageSources().genericKill());
        }
        return InteractionResult.SUCCESS;
    }
}
