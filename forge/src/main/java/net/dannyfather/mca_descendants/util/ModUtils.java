package net.dannyfather.mca_descendants.util;

import forge.net.mca.cobalt.network.NetworkHandler;
import forge.net.mca.entity.EntitiesMCA;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.VillagerLike;
import forge.net.mca.entity.ai.Genetics;
import forge.net.mca.entity.ai.relationship.Gender;
import forge.net.mca.entity.ai.relationship.RelationshipState;
import forge.net.mca.network.c2s.VillagerEditorSyncRequest;
import forge.net.mca.network.s2c.PlayerDataMessage;
import forge.net.mca.server.world.data.FamilyTree;
import forge.net.mca.server.world.data.FamilyTreeNode;
import forge.net.mca.server.world.data.PlayerSaveData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static forge.net.mca.entity.ai.Traits.*;
import static net.dannyfather.mca_descendants.events.MCADescendantsEvents.LAST_DEATH_MESSAGE;
public class ModUtils {
    public static void swapVillagerAndPlayer(LivingEntity target, ServerPlayer pPlayer) {
        if (pPlayer.level() instanceof ServerLevel serverLevel) {
            if (target instanceof VillagerEntityMCA villagerEntityMCA) {
                //villager to player
                FamilyTree tree = FamilyTree.get(serverLevel);
                CompoundTag playerVillagerData = VillagerLike.toVillager(pPlayer).asEntity().serializeNBT();
                CompoundTag villagerMCAData = villagerEntityMCA.serializeNBT();
                playerVillagerData.remove("UUID");
                playerVillagerData.putUUID("UUID", target.getUUID());
                if (!villagerMCAData.getString("custom_skin").isEmpty()) {
                    villagerMCAData.putInt("playerModel", 1);
                }
                if (PlayerSaveData.get(pPlayer).getEntityData().getInt("playerModel") >= 1) {
                    playerVillagerData.putString("custom_skin", pPlayer.getName().getString());
                }
                //player into villager
                Entity newVillagerEntity = EntityType.loadEntityRecursive(playerVillagerData, serverLevel, (e) -> {
                    return e;
                });
                newVillagerEntity.moveTo(target.getOnPos().above(), target.getYRot(), target.getXRot());
                villagerEntityMCA.discard();
                serverLevel.addFreshEntity(newVillagerEntity);
                serverLevel.players().forEach(p ->
                        NetworkHandler.sendToPlayer(
                                new PlayerDataMessage(pPlayer.getUUID(), villagerMCAData),
                                p
                        )
                );
                PlayerSaveData.get(pPlayer).setEntityData(villagerMCAData);

                pPlayer.setCustomName(villagerEntityMCA.getCustomName());

                //
                //Family Tree Stuff
                //
                FamilyTreeNode playerNode = tree.getOrEmpty(pPlayer.getUUID()).orElse(null);
                FamilyTreeNode villagerNode = tree.getOrEmpty(target.getUUID()).orElse(null);
                Gender playerNodeGender = playerNode.gender();
                Gender villagerNodeGender = villagerNode.gender();
                villagerNode.setGender(playerNodeGender);
                playerNode.setGender(villagerNodeGender);

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
                playerNode.updatePartner(null,RelationshipState.SINGLE);
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

                    if (playerNode.gender() == Gender.FEMALE) {
                        pchild.removeMother();
                    } else {
                        pchild.removeFather();
                    }

                    if (villagerNode.gender() == Gender.FEMALE) {
                        pchild.setMother(villagerNode);
                    } else {
                        pchild.setFather(villagerNode);
                    }

                    villagerNode.children().add(pchildUUID);
                }

                for (UUID vchildUUID : villagerChildren) {
                    FamilyTreeNode vchild = tree.getOrEmpty(vchildUUID).get();

                    if (villagerNode.gender() == Gender.FEMALE) {
                        vchild.removeMother();
                    } else {
                        vchild.removeFather();
                    }

                    if (playerNode.gender() == Gender.FEMALE) {
                        vchild.setMother(playerNode);
                    } else {
                        vchild.setFather(playerNode);
                    }

                    playerNode.children().add(vchildUUID);
                }


            }
        }
    }

    public static void evilSwapVillagerAndPlayer(LivingEntity target, ServerPlayer pPlayer) {
        UUID villagerUUID = target.getUUID();
        swapVillagerAndPlayer(target, pPlayer);
        if (pPlayer.level() instanceof ServerLevel serverLevel) {
            Vec3 targetPos = target.position();
            Vec3 playerPos = pPlayer.position();
            Entity entity = serverLevel.getEntity(villagerUUID);
            if (entity != null) {
                entity.moveTo(playerPos);
                pPlayer.teleportTo(targetPos.x,targetPos.y,targetPos.z);
                if(entity instanceof LivingEntity livingEntity){
                    livingEntity.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 200, 0,false,false));
                }
                entity.kill();
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
        CompoundTag soulNBTData = VillagerLike.toVillager(serverPlayer).asEntity().serializeNBT();
        soulNBTData.remove("UUID");
        soulNBTData.putUUID("UUID", UUID.randomUUID());
        soulNBTData.remove("id");
        if (soulNBTData.getInt("gender")==1) {
            soulNBTData.putString("id","mca:male_villager");
        } else if(soulNBTData.getInt("gender")==2) {
            soulNBTData.putString("id","mca:female_villager");
        }
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
        Entity soulEntity = EntityType.loadEntityRecursive(soulNBTData, serverLevel, (e) -> {
            return e;
        });
        VillagerEntityMCA soulNPC = (VillagerEntityMCA) soulEntity;
        assert soulNPC != null;
        soulNPC.setCustomNameVisible(true);
        soulNPC.setCustomName(Component.literal("Soul"));
        soulNPC.getTraits().addTrait(ASEXUAL);
        soulNPC.getTraits().addTrait(COLOR_BLIND);
        soulNPC.getGenetics().setGene(Genetics.SKIN, 0f);
        soulNPC.moveTo(serverPlayer.position());
        return soulEntity;
    }

    public static void placeBookOnLectern(ServerLevel world, BlockPos pos, String playerName,int childrenCount,int grandchildrenCount, ServerPlayer pPlayer) {
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof LecternBlockEntity lectern) {

            // Create a new written book
            ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
            CompoundTag bookTag = book.getOrCreateTag();
            bookTag.putString("title", playerName + "\'s Statistics");
            bookTag.putString("author", playerName);

            //Register Stats
            ResourceLocation id1 = new ResourceLocation("mca","male_villager");
            ResourceLocation id2 = new ResourceLocation("mca","female_villager");
            EntityType<?> type1 = BuiltInRegistries.ENTITY_TYPE.get(id1);
            EntityType<?> type2 = BuiltInRegistries.ENTITY_TYPE.get(id2);
            int mobsKilled = 0;
            int humansKilled = pPlayer.getStats().getValue(Stats.CUSTOM.get(Stats.PLAYER_KILLS)) + pPlayer.getStats().getValue(Stats.ENTITY_KILLED.get(type1)) + pPlayer.getStats().getValue(Stats.ENTITY_KILLED.get(type2));
            int villagersKilled = pPlayer.getStats().getValue(Stats.ENTITY_KILLED.get(EntityType.VILLAGER));
            int daysLived = pPlayer.getStats().getValue(Stats.CUSTOM.get(Stats.PLAY_TIME))/24000;
            int distanceTravelledCM = pPlayer.getStats().getValue(Stats.CUSTOM.get(Stats.WALK_ONE_CM)) + pPlayer.getStats().getValue(Stats.CUSTOM.get(Stats.WALK_ON_WATER_ONE_CM)) + pPlayer.getStats().getValue(Stats.CUSTOM.get(Stats.WALK_UNDER_WATER_ONE_CM)) + pPlayer.getStats().getValue(Stats.CUSTOM.get(Stats.SPRINT_ONE_CM))+ pPlayer.getStats().getValue(Stats.CUSTOM.get(Stats.CROUCH_ONE_CM));
            int distanceTravelled = distanceTravelledCM / 100;
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

            // Create pages
            ListTag pages = new ListTag();
            pages.add(StringTag.valueOf("{\"text\":\""+ "\\n\\n\\n\\n" +
                    " §lThe Scores And\\n Achievements of\\n " + playerName +"\"}" ));
            pages.add(StringTag.valueOf("{\"text\":\"" + "§l§8Days Lasted: §r§4"+daysLived+"\\n" +
                    "\\n" +
                    "§l§8Children: §r§4" + childrenCount + "\\n" +
                    "\\n" +
                    "§l§8Grandchildren: §r§4" + grandchildrenCount + "\\n" +
                    "\\n" +
                    "§l§8Mobs Killed: §r§4"+ mobsKilled +"§7\\n" +
                    "   (Humans: §c"+humansKilled+"§7)\\n" +
                    "   (Villagers: §c"+villagersKilled+"§7)" + "\"}"));
            pages.add(StringTag.valueOf("{\"text\":\""+ "§l§n§8Distance Travelled:§r§o§4\\n" +
                    "  " + distanceTravelled + " meters\\n" +
                    "§l§n§8Amount of Jumps:§r§o§4\\n" +
                    "  " + jumpAmount + " jumps\\n" +
                    "§l§n§8Damage Taken:§r§o§4\\n" +
                    "  " + damageTaken + " hearts\\n" +
                    "§l§n§8Damage Dealt:§r§o§4\\n" + "  " + damageDealt +" hearts\\n" +
                    "§l§n§8Social Interactions:§r§o§4\\n" + "  " + villagersTalkedTo + " conversations\\n" +
                    "§l§n§8Amount of Trades:§r§o§4\\n" + "  " + tradeAmount + " trades" + "\"}"));
            pages.add(StringTag.valueOf("{\"text\":\""+ "§lCause of Death:\\n\\n§r§4"+ deathMsg +"\"}"));
            bookTag.put("pages", pages);

            // Place book on lectern
            lectern.setBook(book);
            BlockState state = world.getBlockState(pos);
            world.setBlock(pos, state.setValue(LecternBlock.HAS_BOOK, true), 3);
            lectern.setChanged();
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



}
