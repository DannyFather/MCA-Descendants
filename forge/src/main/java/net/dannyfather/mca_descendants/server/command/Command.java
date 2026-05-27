package net.dannyfather.mca_descendants.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import forge.net.mca.server.world.data.FamilyTree;
import net.dannyfather.mca_descendants.network.ModNetwork;
import net.dannyfather.mca_descendants.network.s2c.OpenGuiRequest;
import net.dannyfather.mca_descendants.sound.ModSounds;
import net.dannyfather.mca_descendants.util.ModUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.PacketDistributor;

import static net.minecraft.ChatFormatting.*;
import static net.minecraft.ChatFormatting.GOLD;
import static net.minecraft.ChatFormatting.GRAY;
import static net.minecraft.ChatFormatting.LIGHT_PURPLE;
import static net.minecraft.ChatFormatting.RESET;

public class Command {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("mcad")
                .then(Commands.literal("respawn").executes(context -> {
                    ServerPlayer serverPlayer = context.getSource().getPlayerOrException();
                    serverPlayer.serverLevel().playSound(null,serverPlayer.blockPosition(), ModSounds.PHONE_PICKUP.get(), SoundSource.BLOCKS,1f,1f);
                    ModNetwork.CHANNEL.send(
                            PacketDistributor.PLAYER.with(() -> serverPlayer),
                            new OpenGuiRequest(OpenGuiRequest.Type.PHONE, 0)
                    );
                    return 1;
                }).requires((serverCommandSource) -> serverCommandSource.hasPermission(2)))
                .then(Commands.literal("swapWithVillager")
                        .then(Commands.argument("villager", EntityArgument.entity()).executes(context -> {
                                            Entity vEntity = EntityArgument.getEntity(context, "villager");
                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                            if(vEntity instanceof LivingEntity villager) {
                                                ModUtils.swapVillagerAndPlayer(villager,player);
                                            }
                                            return  1;
                                        })
                                        .then(Commands.argument("player",EntityArgument.player()).executes(context -> {
                                                            Entity vEntity = EntityArgument.getEntity(context, "villager");
                                                            ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                                            if(vEntity instanceof LivingEntity villager) {
                                                                ModUtils.swapVillagerAndPlayer(villager,player);
                                                            }
                                                            return  1;
                                                        })
                                                        .then(Commands.literal("remove").executes(context -> {
                                                                    Entity vEntity = EntityArgument.getEntity(context, "villager");
                                                                    ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                                                    if(vEntity instanceof LivingEntity villager) {
                                                                        ModUtils.goodSwapVillagerAndPlayer(villager,player);
                                                                    }
                                                                    return  1;
                                                                })
                                                        ).then(Commands.literal("kill").executes(context -> {
                                                                    Entity vEntity = EntityArgument.getEntity(context, "villager");
                                                                    ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                                                    if(vEntity instanceof LivingEntity villager) {
                                                                        ModUtils.evilSwapVillagerAndPlayer(villager,player,villager.damageSources().genericKill());
                                                                    }
                                                                    return  1;
                                                                })
                                                        )
                                        )
                        ).requires((serverCommandSource) -> serverCommandSource.hasPermission(2))
                ).then(Commands.literal("setname")
                        .then(Commands.argument("CustomName", StringArgumentType.string()).executes(context -> {
                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                            player.setCustomName(Component.literal(StringArgumentType.getString(context,"CustomName")));
                                            FamilyTree.get(player.serverLevel()).getOrCreate(player).setName(StringArgumentType.getString(context,"CustomName"));
                                            return 1;
                                        })
                                        .then(Commands.argument("player", EntityArgument.player()).executes(context -> {
                                                    ServerPlayer player = EntityArgument.getPlayer(context,"player");
                                                    player.setCustomName(Component.literal(StringArgumentType.getString(context,"CustomName")));
                                                    FamilyTree.get(player.serverLevel()).getOrCreate(player).setName(StringArgumentType.getString(context,"CustomName"));
                                                    return 1;
                                                }).requires((serverCommandSource) -> serverCommandSource.hasPermission(2))
                                        )
                        )
                )
                .then(Commands.literal("help").executes(Command::displayHelp))
        );
    }
    private static int displayHelp(CommandContext<CommandSourceStack> ctx) {
        Entity player = ctx.getSource().getEntity();
        if (player == null) {
            return 0;
        }

        sendMessage(player, GRAY + "/mcad swapWithVillager " + GOLD + "[VILLAGER] " + "[PLAYER] " + RED + "[KILL/REMOVE]");
        sendMessage(player, GRAY + "/mcad respawn");
        sendMessage(player, GRAY + "/mcad setname " + GOLD + "[PLAYER]");


        return 0;
    }
    private static void sendMessage(Entity commandSender, String message) {
        commandSender.sendSystemMessage(Component.literal(LIGHT_PURPLE + "[MCA Descendants] " + RESET + message));
    }
}
