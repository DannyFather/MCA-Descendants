package net.dannyfather.mca_descendants.items.custom;

import net.dannyfather.mca_descendants.util.ModUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import static net.dannyfather.mca_descendants.util.ModUtils.evilSwapVillagerAndPlayer;
import static net.dannyfather.mca_descendants.util.ModUtils.goodSwapVillagerAndPlayer;
import static net.minecraft.ChatFormatting.DARK_AQUA;
public class EvilVillagerGrabberItem extends Item {
    public EvilVillagerGrabberItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(ItemStack pStack, Player pPlayer, LivingEntity target, InteractionHand hand) {
        if(pPlayer instanceof ServerPlayer serverPlayer) {
            evilSwapVillagerAndPlayer(target, serverPlayer);
        }
        return InteractionResult.SUCCESS;
    }
}
