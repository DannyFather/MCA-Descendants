package net.dannyfather.mca_descendants.server.world.data;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DescendantLocationData extends SavedData {
    private final Map<UUID,Map<UUID,Map<String,BlockPos>>> location = new HashMap<>();
    public void update(Entity entity, UUID playerUUID) {
        if(entity.hasCustomName()) {
            location.computeIfAbsent(playerUUID, k -> new HashMap<>()).computeIfAbsent(entity.getUUID(),j -> new HashMap<>())
                    .put(entity.getCustomName().getString(),entity.blockPosition());
            setDirty();
        }
    }

    public void remove(Entity entity, UUID playerUUID) {
        Map<UUID, Map<String, BlockPos>> map = location.get(playerUUID);
        if(map != null) {
            map.remove(entity.getUUID());
            if(map.isEmpty()){
                location.remove(playerUUID);
            }
        }
        setDirty();
    }

    public Map<UUID, Map<String, BlockPos>> get(UUID playerUUID){
        return location.getOrDefault(playerUUID, Collections.emptyMap());
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        for (Map.Entry<UUID, Map<UUID, Map<String, BlockPos>>> playerEntry : location.entrySet()) {

            UUID playerUUID = playerEntry.getKey();
            CompoundTag entityTag = new CompoundTag();
            for(Map.Entry<UUID, Map<String, BlockPos>> entityEntry : playerEntry.getValue().entrySet()) {
                for(Map.Entry<String, BlockPos> entityName : entityEntry.getValue().entrySet()) {
                    UUID entityUUID = entityEntry.getKey();
                    BlockPos entityPos = entityName.getValue();
                    CompoundTag entityPosTag = new CompoundTag();
                    entityPosTag.putString("villagerName",entityName.getKey());
                    entityPosTag.putInt("X", entityPos.getX());
                    entityPosTag.putInt("Y", entityPos.getY());
                    entityPosTag.putInt("Z", entityPos.getZ());
                    entityTag.put(entityUUID.toString(), entityPosTag);
                }
            }
            tag.put(playerUUID.toString(),entityTag);

        }
        return tag;
    }

    public static DescendantLocationData load(CompoundTag tag,HolderLookup.Provider provider) {
        DescendantLocationData data = new DescendantLocationData();
        tag.getAllKeys().forEach(playerUUIDString -> {
            UUID playerUUID = UUID.fromString(playerUUIDString);
            tag.getCompound(playerUUIDString).getAllKeys().forEach( villagerUUIDString -> {
                UUID villagerUUID = UUID.fromString(villagerUUIDString);
                CompoundTag villagerTag = tag.getCompound(playerUUIDString).getCompound(villagerUUIDString);
                BlockPos villagerBlockPos = new BlockPos(villagerTag.getInt("X"),villagerTag.getInt("Y"),villagerTag.getInt("Z"));
                String villagerName = villagerTag.getString("villagerName");
                Map<String,BlockPos> villagerMap = new HashMap<>();
                villagerMap.put(villagerName,villagerBlockPos);
                data.location.computeIfAbsent(playerUUID, k -> new HashMap<>()).put(villagerUUID,villagerMap);
            });
        });
        return data;
    }

    public static DescendantLocationData get(ServerLevel level) {

        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(
                        DescendantLocationData::new,
                        DescendantLocationData::load
                ),
                "mca_descendants_locations"
        );
    }
}
