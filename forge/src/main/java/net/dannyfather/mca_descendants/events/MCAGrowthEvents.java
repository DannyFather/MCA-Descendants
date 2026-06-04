package net.dannyfather.mca_descendants.events;

import com.google.common.graph.Network;
import forge.net.mca.cobalt.network.NetworkHandler;
import forge.net.mca.entity.VillagerLike;
import forge.net.mca.entity.ai.relationship.AgeState;
import forge.net.mca.network.s2c.PlayerDataMessage;
import forge.net.mca.server.world.data.PlayerSaveData;
import net.dannyfather.mca_descendants.MCADescendants;
import net.dannyfather.mca_descendants.entity.BabySittingEntity;
import net.dannyfather.mca_descendants.entity.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

import static net.dannyfather.mca_descendants.config.MCADescendantsCommonConfig.*;

@Mod.EventBusSubscriber(modid = MCADescendants.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MCAGrowthEvents {
    @SubscribeEvent
    public static void TickEvent(LivingEvent.LivingTickEvent event) {
        if (PLAYER_GROWTH.get()) {
            Entity entity = event.getEntity();
            if (entity instanceof ServerPlayer serverPlayer) {
                int tickFreq = 100;
                //every 100 ticks = 5 seconds
                if (serverPlayer.tickCount % tickFreq == 0) {
                    CompoundTag playerVData = PlayerSaveData.get(serverPlayer).getEntityData();
                    int ageState = playerVData.getInt("ageState");

                    if (ageState != 0 && ageState != 5) {

                        int age = playerVData.getInt("Age");
                        if (age % AgeState.getStageDuration() == 0) {
                            int newAgeState = ageState + 1;
                            VillagerLike sampleMan = VillagerLike.toVillager(serverPlayer);
                            sampleMan.setAgeState(AgeState.byId(newAgeState));
                            sampleMan.randomizeClothes();
                            playerVData.putInt("ageState", newAgeState);
                            playerVData.putString("clothes", sampleMan.getClothes());
                            if (newAgeState == 2) {
                                serverPlayer.displayClientMessage(Component.translatable("actionbar.ages.toddler"), true);
                            } else if (newAgeState == 3) {
                                serverPlayer.displayClientMessage(Component.translatable("actionbar.ages.child"), true);
                            } else if (newAgeState == 4) {
                                serverPlayer.displayClientMessage(Component.translatable("actionbar.ages.teen"), true);
                            } else {
                                serverPlayer.displayClientMessage(Component.translatable("actionbar.ages.adult"), true);
                            }
                        }
                        updatePlayerAttributes(serverPlayer);
                        playerVData.putInt("Age", age + tickFreq);
                        serverPlayer.serverLevel().players().forEach(p ->
                                NetworkHandler.sendToPlayer(
                                        new PlayerDataMessage(serverPlayer.getUUID(), playerVData),
                                        p
                                )
                        );
                    }

                }

                //instant events
                CompoundTag playerVData = PlayerSaveData.get(serverPlayer).getEntityData();
                int ageState = playerVData.getInt("ageState");
                if (ageState == 1) {
                    if (serverPlayer.getVehicle() == null && serverPlayer.gameMode.getGameModeForPlayer().equals(GameType.SURVIVAL)) {
                        serverPlayer.serverLevel().getAllEntities().forEach(e -> {
                            if (e instanceof BabySittingEntity babySeat && babySeat.getPassengers().isEmpty()) {
                                babySeat.discard();
                            }
                        });
                        BabySittingEntity babySeat = new BabySittingEntity(ModEntities.BABY_SEAT.get(), serverPlayer.serverLevel());
                        Vec3 pos = new Vec3(serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ());
                        babySeat.setPos(pos.x + 0.01, pos.y + 0.01, pos.z + 0.01);
                        serverPlayer.serverLevel().addFreshEntity(babySeat);
                        serverPlayer.startRiding(babySeat, false);
                    }

                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            updatePlayerAttributes(serverPlayer);
        }
    }

    public static void updatePlayerAttributes(ServerPlayer serverPlayer) {
        CompoundTag playerVData = PlayerSaveData.get(serverPlayer).getEntityData();
        int ageState = playerVData.getInt("ageState");
        AttributeInstance playerSpeed = serverPlayer.getAttribute(Attributes.MOVEMENT_SPEED);
        AttributeInstance playerHealth = serverPlayer.getAttribute(Attributes.MAX_HEALTH);
        if (playerSpeed != null && playerHealth != null) {
            if (ageState == 1) {
                playerSpeed.setBaseValue(0.1 * BABY_SPEED.get());
                playerHealth.setBaseValue(BABY_HEALTH.get());
            } else if (ageState == 2) {
                if (serverPlayer.getVehicle() instanceof BabySittingEntity) {
                    serverPlayer.getVehicle().discard();
                }
                playerSpeed.setBaseValue(0.1 * TODDLER_SPEED.get());
                playerHealth.setBaseValue(TODDLER_HEALTH.get());
            } else if (ageState == 3) {
                playerSpeed.setBaseValue(0.1 * CHILD_SPEED.get());
                playerHealth.setBaseValue(CHILD_HEALTH.get());
            } else if (ageState == 4) {
                playerSpeed.setBaseValue(0.1 * TEEN_SPEED.get());
                playerHealth.setBaseValue(TEEN_HEALTH.get());
            } else {
                playerSpeed.setBaseValue(0.1 * ADULT_SPEED.get());
                playerHealth.setBaseValue(ADULT_HEALTH.get());
            }
        }
    }


    @SubscribeEvent
    public static void onRightClickEntity(PlayerInteractEvent.EntityInteractSpecific event) {
        Entity entity = event.getEntity();
        Entity targetEntity = event.getTarget();
        Entity childSeatEntity = targetEntity.getVehicle();

        if (!ModList.get().isLoaded("carryon")) {
            if (entity instanceof ServerPlayer player && player.isCrouching() && targetEntity instanceof ServerPlayer childPlayer) {
                int ageState = PlayerSaveData.get(childPlayer).getEntityData().getInt("ageState");
                if (ageState <= 3) {
                    childPlayer.startRiding(player, true);
                }
            }

        }
        if (entity instanceof ServerPlayer baby) {
            int ageState = PlayerSaveData.get(baby).getEntityData().getInt("ageState");
            if (ageState == 1) {
                baby.startRiding(targetEntity, false);
            }
        }
    }


    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Entity entity = event.getEntity();
        if (entity instanceof ServerPlayer player && !event.getLevel().isClientSide() && player.getFirstPassenger() instanceof ServerPlayer child) {
            player.ejectPassengers();
            BlockPos pos = event.getPos().above();
            child.moveTo(pos.getX(), pos.getY(), pos.getZ());
            child.setDeltaMovement(Vec3.ZERO);
            child.resetFallDistance();
        }

    }

    @SubscribeEvent
    public static void onEntityMount(EntityMountEvent event) {
        Entity entity = event.getEntityMounting();
        if (event.isDismounting() && event.getEntityBeingMounted() instanceof BabySittingEntity && entity instanceof ServerPlayer serverPlayer) {
            CompoundTag playerVData = PlayerSaveData.get(serverPlayer).getEntityData();
            if (playerVData.getInt("ageState") == 1 && serverPlayer.gameMode.getGameModeForPlayer().equals(GameType.SURVIVAL)) {
                serverPlayer.setOnGround(true);
            }
        }
    }
}






