package net.dannyfather.mca_descendants.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.SpecialPlantable;

public class DecayedGrassBlock extends GrassBlock {
    public DecayedGrassBlock(Properties properties) {
        super(properties);
    }

    public boolean canSustainPlant(BlockState state, BlockGetter world, BlockPos pos, Direction direction, SpecialPlantable plantable) {
        return true;
    }
}
