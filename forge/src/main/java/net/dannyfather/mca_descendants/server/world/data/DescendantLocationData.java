package net.dannyfather.mca_descendants.server.world.data;

import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.*;

public class DescendantLocationData extends SavedData {
    private final Map<UUID,Map<UUID,BlockPos>> location = new HashMap<>();
    public void update(Entity entity, UUID playerUUID) {
        location.computeIfAbsent(playerUUID, k -> new HashMap<>())
                .put(entity.getUUID(),entity.blockPosition());
        setDirty();
    }

    public void remove(Entity entity, UUID playerUUID) {
        Map<UUID,BlockPos> map = location.get(playerUUID);
        if(map != null) {
            map.remove(entity.getUUID());
            if(map.isEmpty()){
                location.remove(playerUUID);
            }
        }
        setDirty();
    }

    public Map<UUID,BlockPos> get(UUID playerUUID){
        return location.getOrDefault(playerUUID, Collections.emptyMap());
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        for (Map.Entry<UUID, Map<UUID, BlockPos>> playerEntry : location.entrySet()) {

            UUID playerUUID = playerEntry.getKey();
            CompoundTag entityTag = new CompoundTag();
            for(Map.Entry<UUID,BlockPos> entityEntry : playerEntry.getValue().entrySet()) {
                UUID entityUUID = entityEntry.getKey();
                BlockPos entityPos = entityEntry.getValue();
                CompoundTag entityPosTag = new CompoundTag();
                entityPosTag.putInt("X",entityPos.getX());
                entityPosTag.putInt("Y",entityPos.getY());
                entityPosTag.putInt("Z",entityPos.getZ());
                entityTag.put(entityUUID.toString(),entityPosTag);
            }
            tag.put(playerUUID.toString(),entityTag);

        }
        return tag;
    }

    public static DescendantLocationData load(CompoundTag tag) {
        DescendantLocationData data = new DescendantLocationData();
        tag.getAllKeys().forEach(playerUUIDString -> {
            UUID playerUUID = UUID.fromString(playerUUIDString);
            tag.getCompound(playerUUIDString).getAllKeys().forEach( villagerUUIDString -> {
                UUID villagerUUID = UUID.fromString(villagerUUIDString);
                CompoundTag villagerTag = tag.getCompound(playerUUIDString).getCompound(villagerUUIDString);
                BlockPos villagerBlockPos = new BlockPos(villagerTag.getInt("X"),villagerTag.getInt("Y"),villagerTag.getInt("Z"));
                data.location.computeIfAbsent(playerUUID, k -> new HashMap<>()).put(villagerUUID,villagerBlockPos);
            });
        });
        return data;
    }

    public static DescendantLocationData get(ServerLevel level){
        return level.getDataStorage().computeIfAbsent(
                DescendantLocationData::load,
                DescendantLocationData::new,
                "mca_descendants_locations"
        );
    }
}
