package net.dannyfather.mca_descendants.world;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class StructureSpawnData extends SavedData {

    private boolean spawned = false;

    public static StructureSpawnData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                tag -> {
                    StructureSpawnData data = new StructureSpawnData();
                    data.spawned = tag.getBoolean("spawned");
                    return data;
                },
                StructureSpawnData::new,
                "waiting_room"
        );
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putBoolean("spawned", spawned);
        return tag;
    }

    public boolean hasSpawned() {
        return spawned;
    }

    public void setSpawned() {
        this.spawned = true;
        setDirty();
    }
}