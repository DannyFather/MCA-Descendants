package net.dannyfather.mca_descendants.worldgen.teleporters;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.util.ITeleporter;

import java.util.function.Function;

public class SimpleTeleporter implements ITeleporter {
    private final double x, y, z;

    public SimpleTeleporter(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public Entity placeEntity(Entity entity, ServerLevel currentWorld,
                              ServerLevel destWorld, float yaw,
                              Function<Boolean, Entity> repositionEntity) {

        Entity newEntity = repositionEntity.apply(false);

        newEntity.teleportTo(x, y, z);

        return newEntity;
    }
}
