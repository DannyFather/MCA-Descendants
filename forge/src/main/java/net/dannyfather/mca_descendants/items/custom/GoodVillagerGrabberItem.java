package net.dannyfather.mca_descendants.items.custom;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static net.dannyfather.mca_descendants.util.ModUtils.goodSwapVillagerAndPlayer;
import static net.dannyfather.mca_descendants.util.ModUtils.swapVillagerAndPlayer;


public class GoodVillagerGrabberItem extends Item {

    public GoodVillagerGrabberItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(ItemStack pStack, Player pPlayer, LivingEntity target, InteractionHand hand) {
        goodSwapVillagerAndPlayer(target,pPlayer);
        return InteractionResult.SUCCESS;
    }
}
