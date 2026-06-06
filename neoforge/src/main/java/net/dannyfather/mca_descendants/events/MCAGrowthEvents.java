package net.dannyfather.mca_descendants.events;

import net.conczin.mca.MCA;
import net.conczin.mca.entity.VillagerLike;
import net.conczin.mca.entity.ai.relationship.AgeState;
import net.conczin.mca.entity.ai.relationship.Gender;
import net.conczin.mca.network.Network;
import net.conczin.mca.network.s2c.PlayerDataMessage;
import net.conczin.mca.registry.EntitiesMCA;
import net.conczin.mca.resources.ClothingList;
import net.conczin.mca.server.world.data.PlayerSaveData;
import net.dannyfather.mca_descendants.MCADescendants;
import net.dannyfather.mca_descendants.entity.BabySittingEntity;
import net.dannyfather.mca_descendants.entity.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityMountEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.common.carry.CarryOnDataManager;
import tschipp.carryon.common.carry.PickupHandler;
import tschipp.carryon.common.carry.PlacementHandler;
import tschipp.carryon.events.EntityPickupEvent;

import static com.mojang.text2speech.Narrator.LOGGER;
import static net.dannyfather.mca_descendants.config.MCADescendantsCommonConfig.*;

@EventBusSubscriber(modid = MCADescendants.MOD_ID)
public class MCAGrowthEvents {
    @SubscribeEvent
    public static void TickEvent(EntityTickEvent.Post event) {
        if (PLAYER_GROWTH.get()) {
            Entity entity = event.getEntity();
            if (entity instanceof ServerPlayer serverPlayer) {
                int tickFreq = 100;
                //every 100 ticks = 5 seconds
                if (serverPlayer.tickCount % tickFreq == 0) {
                    CompoundTag playerVData = PlayerSaveData.get(serverPlayer).getEntityData();
                    int ageState = playerVData.getInt("AgeState");

                    if (ageState != 0 && ageState != 5) {

                        int age = playerVData.getInt("Age");
                        if (age % AgeState.getStageDuration() == 0) {
                            int newAgeState = ageState + 1;
                            VillagerLike sampleMan = VillagerLike.toVillager(serverPlayer);
                            sampleMan.setAgeState(AgeState.byId(newAgeState));
                            sampleMan.randomizeClothes();
                            playerVData.putInt("AgeState", newAgeState);
                            playerVData.putString("Clothes", sampleMan.getClothes());
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
                        if (!serverPlayer.getCustomName().getString().equals("\uD83D\uDC7B")) {
                            PlayerSaveData.get(serverPlayer).setEntityData(playerVData);
                            serverPlayer.serverLevel().players().forEach(p -> Network.sendToPlayer(new PlayerDataMessage(serverPlayer.getUUID(), playerVData), p));
                        }
                    }

                }

                //instant events
                CompoundTag playerVData = PlayerSaveData.get(serverPlayer).getEntityData();
                int ageState = playerVData.getInt("AgeState");
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
        int ageState = playerVData.getInt("AgeState");
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
                int ageState = PlayerSaveData.get(childPlayer).getEntityData().getInt("AgeState");
                if (ageState <= 3) {
                    childPlayer.startRiding(player, true);
                }
            }

        }
        if (entity instanceof ServerPlayer baby) {
            int ageState = PlayerSaveData.get(baby).getEntityData().getInt("AgeState");
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
            if (playerVData.getInt("AgeState") == 1 && serverPlayer.gameMode.getGameModeForPlayer().equals(GameType.SURVIVAL)) {
                serverPlayer.setOnGround(true);
            }
        }
    }
}






