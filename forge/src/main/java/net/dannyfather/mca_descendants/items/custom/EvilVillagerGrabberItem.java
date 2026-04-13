package net.dannyfather.mca_descendants.items.custom;

import net.dannyfather.mca_descendants.util.ModUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
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

import static net.minecraft.ChatFormatting.DARK_AQUA;
public class EvilVillagerGrabberItem extends Item {
    public EvilVillagerGrabberItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pUsedHand);
        if (pLevel instanceof ServerLevel serverLevel && pPlayer instanceof ServerPlayer serverPlayer) {
            pPlayer.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY,-1,0,false,false));
            pPlayer.addEffect(new MobEffectInstance(MobEffects.GLOWING,-1,0,false,false));
            Entity soul = ModUtils.summonSoul(serverPlayer,serverLevel);
            serverLevel.addFreshEntity(soul);
            ModUtils.evilSwapVillagerAndPlayer(((LivingEntity) soul),serverPlayer);
            Scoreboard scoreboard = serverLevel.getScoreboard();

            PlayerTeam ghostTeam = scoreboard.getPlayerTeam("ghosts");
            if (ghostTeam == null) {
                scoreboard.addPlayerTeam("ghosts");
                ghostTeam = scoreboard.getPlayerTeam("ghosts");
                ghostTeam.setColor(DARK_AQUA);
            }
            scoreboard.addPlayerToTeam(serverPlayer.getName().getString(),ghostTeam);
            soul.discard();
        }

        return InteractionResultHolder.success(itemstack);
    }
}
