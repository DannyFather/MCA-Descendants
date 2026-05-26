package net.dannyfather.mca_descendants.server.world;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

public class StructureSpawnData extends SavedData {

    private static final String NAME = "waiting_room";

    private boolean spawned = false;

    public static StructureSpawnData get(ServerLevel level) {
        DimensionDataStorage storage = level.getDataStorage();

        return storage.computeIfAbsent(
                new SavedData.Factory<>(
                        StructureSpawnData::new,
                        StructureSpawnData::load
                ),
                NAME
        );
    }

    public static StructureSpawnData load(CompoundTag tag, HolderLookup.Provider registries) {
        StructureSpawnData data = new StructureSpawnData();
        data.spawned = tag.getBoolean("spawned");
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
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