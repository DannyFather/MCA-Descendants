package net.dannyfather.mca_descendants.util;


import net.conczin.mca.client.gui.VillagerEditorScreen;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.entity.VillagerLike;
import net.conczin.mca.entity.ai.Genetics;
import net.conczin.mca.entity.ai.relationship.Gender;
import net.conczin.mca.entity.ai.relationship.RelationshipState;
import net.conczin.mca.network.Network;
import net.conczin.mca.network.c2s.GetVillagerRequest;
import net.conczin.mca.network.c2s.VillagerEditorSyncRequest;
import net.conczin.mca.network.s2c.PlayerDataMessage;
import net.conczin.mca.registry.EntitiesMCA;
import net.conczin.mca.server.world.data.FamilyTree;
import net.conczin.mca.server.world.data.FamilyTreeNode;
import net.conczin.mca.server.world.data.PlayerSaveData;
import net.dannyfather.mca_descendants.MCADescendants;
import net.dannyfather.mca_descendants.effects.ModEffects;
import net.dannyfather.mca_descendants.server.world.StructureSpawnData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;

import java.util.*;

import static net.conczin.mca.entity.ai.Traits.ASEXUAL;
import static net.conczin.mca.entity.ai.Traits.COLOR_BLIND;
import static net.dannyfather.mca_descendants.events.MCADescendantsEvents.*;


public class ModUtils {
    public static void swapVillagerAndPlayer(LivingEntity target, ServerPlayer pPlayer) {
        if (pPlayer.level() instanceof ServerLevel serverLevel) {
            if (target instanceof VillagerEntityMCA villagerEntityMCA) {
                //villager to player
                FamilyTree tree = FamilyTree.get(serverLevel);
                CompoundTag playerVillagerData = new CompoundTag();
                CompoundTag villagerMCAData = new CompoundTag();
                VillagerLike.toVillager(pPlayer).asEntity().save(playerVillagerData);
                villagerEntityMCA.save(villagerMCAData);

                playerVillagerData.remove("UUID");
                playerVillagerData.putUUID("UUID", target.getUUID());

                /*if (!villagerMCAData.getString("custom_skin").isEmpty()) {
                    villagerMCAData.putInt("PlayerModel", 1);
                }
                if (PlayerSaveData.get(pPlayer).getEntityData().getInt("PlayerModel") >= 1) {
                    playerVillagerData.putString("custom_skin", pPlayer.getName().getString());
                }*/

                //player into villager
                Entity newVillagerEntity = EntityType.loadEntityRecursive(playerVillagerData, serverLevel, (e) -> {
                    return e;
                });
                newVillagerEntity.moveTo(target.getOnPos().above(), target.getYRot(), target.getXRot());
                newVillagerEntity.setCustomName(pPlayer.getCustomName());
                Component newName = Component.nullToEmpty(villagerEntityMCA.getCustomName().getString());
                villagerEntityMCA.discard();
                serverLevel.addFreshEntity(newVillagerEntity);
                pPlayer.setCustomName(newName);




                //
                //Family Tree Stuff
                //
                FamilyTreeNode playerNode = tree.getOrCreate(pPlayer);
                FamilyTreeNode villagerNode = tree.getOrEmpty(target.getUUID()).orElse(null);

                Gender villagerGender = villagerNode.gender();
                if (playerVillagerData.getInt("Gender") == 1) {
                    villagerNode.setGender(Gender.MALE);
                } else if(playerVillagerData.getInt("Gender") == 2) {
                    villagerNode.setGender(Gender.FEMALE);
                }
                playerNode.setGender(villagerGender);

                //swap parents

                FamilyTreeNode pFather = tree.getOrEmpty(playerNode.father()).orElse(null);
                FamilyTreeNode pMother = tree.getOrEmpty(playerNode.mother()).orElse(null);
                FamilyTreeNode vFather = tree.getOrEmpty(villagerNode.father()).orElse(null);
                FamilyTreeNode vMother = tree.getOrEmpty(villagerNode.mother()).orElse(null);
                detachFromParents(playerNode, tree);
                detachFromParents(villagerNode, tree);
                if(vFather != null) {
                    playerNode.setFather(vFather);
                }
                if(vMother != null) {
                    playerNode.setMother(vMother);
                }
                if(pFather != null) {
                    villagerNode.setFather(pFather);
                }
                if(pMother != null) {
                    villagerNode.setMother(pMother);
                }

                //swap spouses
                FamilyTreeNode pSpouse = tree.getOrEmpty(playerNode.partner()).orElse(null);
                FamilyTreeNode vSpouse = tree.getOrEmpty(villagerNode.partner()).orElse(null);
                playerNode.updatePartner(null, RelationshipState.SINGLE);
                villagerNode.updatePartner(null,RelationshipState.SINGLE);

                if (pSpouse != null) {
                    pSpouse.updatePartner(null,RelationshipState.SINGLE);
                    pSpouse.updatePartner(villagerNode);
                    villagerNode.updatePartner(pSpouse);
                }
                if (vSpouse != null) {
                    vSpouse.updatePartner(null,RelationshipState.SINGLE);
                    vSpouse.updatePartner(playerNode);
                    playerNode.updatePartner(vSpouse);
                }

                //swap children
                Set<UUID> playerChildren = new HashSet<>(playerNode.children());
                Set<UUID> villagerChildren = new HashSet<>(villagerNode.children());

                playerNode.children().clear();
                villagerNode.children().clear();

                for (UUID pchildUUID : playerChildren) {
                    FamilyTreeNode pchild = tree.getOrEmpty(pchildUUID).get();

                    if (playerNode.gender().binary() == Gender.FEMALE) {
                        pchild.removeMother();
                    } else {
                        pchild.removeFather();
                    }
                    pchild.assignParent(villagerNode);

                    villagerNode.children().add(pchildUUID);
                }

                for (UUID vchildUUID : villagerChildren) {
                    FamilyTreeNode vchild = tree.getOrEmpty(vchildUUID).get();

                    if (villagerNode.gender().binary() == Gender.FEMALE) {
                        vchild.removeMother();
                    } else {
                        vchild.removeFather();
                    }

                    vchild.assignParent(playerNode);

                    playerNode.children().add(vchildUUID);
                }
                PlayerSaveData.get(pPlayer).setEntityData(villagerMCAData);

                pPlayer.serverLevel().players().forEach(p -> {
                    Network.sendToPlayer(new PlayerDataMessage(pPlayer.getUUID(), villagerMCAData), p);
                    FamilyTreeNode pNode = FamilyTree.get(p.serverLevel()).getOrCreate(p);
                    pNode.setName(p.getCustomName().getString());
                });

            }
        }
    }

    public static void evilSwapVillagerAndPlayer(LivingEntity target, ServerPlayer pPlayer, DamageSource source) {
        UUID villagerUUID = target.getUUID();
        String playerName = pPlayer.getCustomName().getString();
        swapVillagerAndPlayer(target, pPlayer);
        if (pPlayer.level() instanceof ServerLevel serverLevel) {
            Vec3 targetPos = target.position();
            Vec3 playerPos = pPlayer.position();
            Entity entity = serverLevel.getEntity(villagerUUID);
            if (entity != null) {
                entity.moveTo(playerPos);
                pPlayer.teleportTo(targetPos.x,targetPos.y,targetPos.z);
                entity.setCustomName(Component.literal(playerName));
                if(entity instanceof LivingEntity livingEntity){
                    livingEntity.hurt(source,target.getMaxHealth() + 500f);
                }
            }

        }
    }

    public static void goodSwapVillagerAndPlayer(LivingEntity target, ServerPlayer pPlayer) {
        UUID villagerUUID = target.getUUID();
        if (pPlayer.level() instanceof ServerLevel serverLevel) {
            swapVillagerAndPlayer(target, pPlayer);
            Scoreboard scoreboard = serverLevel.getScoreboard();
            scoreboard.removePlayerFromTeam(pPlayer.getName().getString());
            Vec3 targetPos = target.position();
            Entity entity = serverLevel.getEntity(villagerUUID);
            pPlayer.teleportTo(targetPos.x,targetPos.y,targetPos.z);
            pPlayer.removeAllEffects();
            pPlayer.setGameMode(GameType.SURVIVAL);
            FamilyTree tree = FamilyTree.get(serverLevel);
            tree.remove(entity.getUUID());
            entity.discard();
        }
    }

    public static Entity summonSoul(ServerPlayer serverPlayer,ServerLevel serverLevel) {
        CompoundTag soulNBTData = new CompoundTag();
        VillagerLike.toVillager(serverPlayer).asEntity().save(soulNBTData);
        soulNBTData.remove("UUID");
        soulNBTData.putUUID("UUID", UUID.randomUUID());
        soulNBTData.remove("id");
        soulNBTData.remove("tree_mother_name");
        soulNBTData.remove("tree_father_name");
        soulNBTData.remove("tree_spouse_name");
        soulNBTData.remove("tree_mother_UUID");
        soulNBTData.remove("tree_father_UUID");
        soulNBTData.remove("tree_spouse_UUID");
        soulNBTData.putString("villagerName","Soul");
        if (PlayerSaveData.get(serverPlayer).getEntityData().getInt("playerModel") >= 1) {
            soulNBTData.putString("custom_skin", serverPlayer.getName().getString());
        }
        VillagerEntityMCA soulNPC = EntitiesMCA.FEMALE_VILLAGER.create(serverLevel);
        if(soulNBTData.getInt("gender")==1) {
            soulNPC = EntitiesMCA.MALE_VILLAGER.create(serverLevel);
        }
        soulNPC.load(soulNBTData);
        soulNPC.setCustomNameVisible(true);
        soulNPC.setCustomName(Component.literal("\uD83D\uDC7B"));
        soulNPC.getTraits().addTrait(ASEXUAL);
        soulNPC.getTraits().addTrait(COLOR_BLIND);
        soulNPC.getGenetics().setGene(Genetics.SKIN, 0f);
        soulNPC.moveTo(serverPlayer.position());
        return soulNPC;
    }

    public static void placeBookOnLectern(ServerLevel world, BlockPos pos, String playerName, int childrenCount, int grandchildrenCount, ServerPlayer pPlayer, String lastVillagerName) {
        //stats
        ResourceLocation id1 = ResourceLocation.fromNamespaceAndPath("mca","male_villager");
        ResourceLocation id2 = ResourceLocation.fromNamespaceAndPath("mca","female_villager");
        EntityType<?> type1 = BuiltInRegistries.ENTITY_TYPE.get(id1);
        EntityType<?> type2 = BuiltInRegistries.ENTITY_TYPE.get(id2);
        int mobsKilled = 0;
        int humansKilled = pPlayer.getStats().getValue(Stats.CUSTOM.get(Stats.PLAYER_KILLS)) + pPlayer.getStats().getValue(Stats.ENTITY_KILLED.get(type1)) + pPlayer.getStats().getValue(Stats.ENTITY_KILLED.get(type2));
        int villagersKilled = pPlayer.getStats().getValue(Stats.ENTITY_KILLED.get(EntityType.VILLAGER));
        int distanceTravelledCM = pPlayer.getStats().getValue(Stats.CUSTOM.get(Stats.WALK_ONE_CM)) + pPlayer.getStats().getValue(Stats.CUSTOM.get(Stats.WALK_ON_WATER_ONE_CM)) + pPlayer.getStats().getValue(Stats.CUSTOM.get(Stats.WALK_UNDER_WATER_ONE_CM)) + pPlayer.getStats().getValue(Stats.CUSTOM.get(Stats.SPRINT_ONE_CM))+ pPlayer.getStats().getValue(Stats.CUSTOM.get(Stats.CROUCH_ONE_CM));
        int distanceTravelled = distanceTravelledCM / 100;
        int deathCount = pPlayer.getStats().getValue(Stats.CUSTOM.get(Stats.DEATHS));
        int jumpAmount = pPlayer.getStats().getValue(Stats.CUSTOM.get(Stats.JUMP));
        int damageTaken = pPlayer.getStats().getValue(Stats.CUSTOM.get(Stats.DAMAGE_TAKEN))/10;
        int damageDealt = pPlayer.getStats().getValue(Stats.CUSTOM.get(Stats.DAMAGE_DEALT))/10;
        int tradeAmount = pPlayer.getStats().getValue(Stats.CUSTOM.get(Stats.TRADED_WITH_VILLAGER));
        int villagersTalkedTo = pPlayer.getStats().getValue(Stats.CUSTOM.get(Stats.TALKED_TO_VILLAGER));
        String playerDeathMsg = LAST_DEATH_MESSAGE.get(pPlayer.getUUID());
        String deathMsg = playerDeathMsg.replace(pPlayer.getName().getString(),playerName);
        Iterator<Stat<EntityType<?>>> mobskilledstat = Stats.ENTITY_KILLED.iterator();
        while (mobskilledstat.hasNext()) {
            mobsKilled += pPlayer.getStats().getValue(mobskilledstat.next());
        }

        //place book
        BlockState state = world.getBlockState(pos);
        if(state.getBlock() instanceof LecternBlock) {
            BlockEntity be = world.getBlockEntity(pos);
            if(be instanceof LecternBlockEntity lectern) {
                ItemStack book = new ItemStack(Items.WRITTEN_BOOK);

                WrittenBookContent content = new WrittenBookContent(
                        Filterable.passThrough(playerName + "'s Statistics"),
                        playerName,
                        0,
                        List.of(
                                Filterable.passThrough(
                                        Component.literal("\n\n\n\n" +
                                                " §lThe Scores And\n Achievements of\n " + playerName)
                                ),
                                Filterable.passThrough(
                                        Component.literal(
                                                "§l§8Children: §r§4" + childrenCount + "\n" +
                                                "\n" +
                                                "§l§8Grandchildren: §r§4" + grandchildrenCount + "\n" +
                                                "\n" +
                                                "§l§8Mobs Killed: §r§4"+ mobsKilled +"§7\n" +
                                                "   (Humans: §c"+humansKilled+"§7)\n" +
                                                "   (Villagers: §c"+villagersKilled+"§7)" +
                                                "\n\n\n" +
                                                "§l§8Incarnation: §r§4" + deathCount + "\n")

                                ),
                                Filterable.passThrough(
                                        Component.literal( "§l§n§8Distance Travelled:§r§o§4\n" +
                                                "  " + distanceTravelled + " meters\n" +
                                                "§l§n§8Amount of Jumps:§r§o§4\n" +
                                                "  " + jumpAmount + " jumps\n" +
                                                "§l§n§8Damage Taken:§r§o§4\n" +
                                                "  " + damageTaken + " hearts\n" +
                                                "§l§n§8Damage Dealt:§r§o§4\n" + "  " + damageDealt +" hearts\n" +
                                                "§l§n§8Social Interactions:§r§o§4\n" + "  " + villagersTalkedTo + " conversations\n" +
                                                "§l§n§8Amount of Trades:§r§o§4\n" + "  " + tradeAmount + " trades")
                                ),
                                Filterable.passThrough(
                                        Component.literal( "§lCause of Death:\n\n§r§4"+ deathMsg)
                                )
                        ),
                        true
                );

                if(pPlayer.hasCustomName() && !lastVillagerName.equals("\uD83D\uDC7B")) {
                    book.set(DataComponents.WRITTEN_BOOK_CONTENT, content);
                    lectern.setBook(book);
                    world.setBlock(pos,state.setValue(LecternBlock.HAS_BOOK,true),3);
                    lectern.setChanged();
                }

            }
        }
    }


    public static void removeStats(ServerPlayer pPlayer){
        pPlayer.getStats().setValue(pPlayer,Stats.CUSTOM.get(Stats.PLAY_TIME),0);
        Iterator<Stat<EntityType<?>>> mobskilledstat = Stats.ENTITY_KILLED.iterator();
        while (mobskilledstat.hasNext()) {
            pPlayer.getStats().setValue(pPlayer,Stats.ENTITY_KILLED.get(mobskilledstat.next().getValue()),0);
        }

        pPlayer.getStats().setValue(pPlayer,Stats.CUSTOM.get(Stats.WALK_UNDER_WATER_ONE_CM),0);
        pPlayer.getStats().setValue(pPlayer,Stats.CUSTOM.get(Stats.WALK_ONE_CM),0);
        pPlayer.getStats().setValue(pPlayer,Stats.CUSTOM.get(Stats.WALK_ON_WATER_ONE_CM),0);
        pPlayer.getStats().setValue(pPlayer,Stats.CUSTOM.get(Stats.SPRINT_ONE_CM),0);
        pPlayer.getStats().setValue(pPlayer,Stats.CUSTOM.get(Stats.CROUCH_ONE_CM),0);
        pPlayer.getStats().setValue(pPlayer,Stats.CUSTOM.get(Stats.JUMP),0);
        pPlayer.getStats().setValue(pPlayer,Stats.CUSTOM.get(Stats.JUMP),0);
        pPlayer.getStats().setValue(pPlayer,Stats.CUSTOM.get(Stats.DAMAGE_TAKEN),0);
        pPlayer.getStats().setValue(pPlayer,Stats.CUSTOM.get(Stats.DAMAGE_DEALT),0);
        pPlayer.getStats().setValue(pPlayer,Stats.CUSTOM.get(Stats.TRADED_WITH_VILLAGER),0);
        pPlayer.getStats().setValue(pPlayer,Stats.CUSTOM.get(Stats.TALKED_TO_VILLAGER),0);
        pPlayer.getStats().setValue(pPlayer,Stats.CUSTOM.get(Stats.PLAY_TIME),0);

    }

    private static void detachFromParents(FamilyTreeNode node, FamilyTree tree) {
        FamilyTreeNode father = tree.getOrEmpty(node.father()).orElse(null);
        FamilyTreeNode mother = tree.getOrEmpty(node.mother()).orElse(null);

        if (father != null) father.children().remove(node.id());
        if (mother != null) mother.children().remove(node.id());
    }

    public static void respawnAfterlife(ServerPlayer serverPlayer, ServerLevel tpDim, BlockPos spawnPos, MinecraftServer server) {
        String soulName = LAST_VILLAGER_NAME.get(serverPlayer.getUUID());
        serverPlayer.teleportTo(
                tpDim,
                spawnPos.getX(),
                spawnPos.getY(),
                spawnPos.getZ(),
                0f,
                0f
        );
        serverPlayer.setGameMode(GameType.ADVENTURE);
        server.execute(() -> {

            BlockPos structurePos = new BlockPos(0, 299, 0);

            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    tpDim.getChunk(structurePos.offset(x * 16, 0, z * 16));
                }
            }

            StructureSpawnData spawnData = StructureSpawnData.get(tpDim);

            if (!spawnData.hasSpawned()) {

                ResourceLocation structureId =
                        ResourceLocation.fromNamespaceAndPath(MCADescendants.MOD_ID, "waiting_room");

                tpDim.getStructureManager().get(structureId).ifPresent(template -> {

                    StructurePlaceSettings settings = new StructurePlaceSettings()
                            .setRotation(Rotation.NONE)
                            .setMirror(Mirror.NONE)
                            .setIgnoreEntities(false);

                    template.placeInWorld(
                            tpDim,
                            structurePos,
                            structurePos,
                            settings,
                            tpDim.getRandom(),
                            2
                    );


                });
            }

            server.execute(() -> {
                spawnData.setSpawned();
                BlockPos lecternPos = new BlockPos(10, 303, 21);
                BlockPos phonePos = new BlockPos(7, 304, 20);
                BlockPos wallPos = new BlockPos(7, 304, 21);
                BlockPos leverPos = new BlockPos(7, 304, 22);
                BlockState phoneState = tpDim.getBlockState(phonePos);
                BlockState wallState = tpDim.getBlockState(phonePos);
                BlockState leverState = tpDim.getBlockState(phonePos);
                ModUtils.placeBookOnLectern(tpDim, lecternPos, soulName, CHILDREN_COUNT.get(serverPlayer.getUUID()), GRANDCHILDREN_COUNT.get(serverPlayer.getUUID()), serverPlayer,soulName);
                tpDim.setBlock(wallPos, Blocks.GRAY_TERRACOTTA.defaultBlockState(),3);
                tpDim.setBlock(leverPos, Blocks.REDSTONE_WIRE.defaultBlockState().setValue(RedStoneWireBlock.NORTH, RedstoneSide.SIDE),3);
                tpDim.updateNeighborsAt(leverPos, leverState.getBlock());
                tpDim.updateNeighborsAt(wallPos, wallState.getBlock());
                tpDim.updateNeighborsAt(phonePos, phoneState.getBlock());
                server.execute(() -> {
                    serverPlayer.setCustomName(Component.literal("\uD83D\uDC7B"));
                    serverPlayer.setGameMode(GameType.ADVENTURE);
                    serverPlayer.moveTo(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
                });
            });


        });
    }


}
