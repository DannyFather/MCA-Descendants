package net.dannyfather.mca_descendants.block.custom;

import jdk.jfr.Category;
import net.dannyfather.mca_descendants.client.gui.PhoneScreen;
import net.dannyfather.mca_descendants.network.ModNetwork;
import net.dannyfather.mca_descendants.network.s2c.OpenGuiRequest;
import net.dannyfather.mca_descendants.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;
public class PhoneBlock extends HorizontalDirectionalBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final VoxelShape SHAPE = Stream.of(
            Block.box(3.5, 0, 3.5, 12.5, 3, 12.5),
            Block.box(3.5, 3, 5.5, 12.5, 4, 12.5),
            Block.box(4.75, 4, 8.5, 11.25, 8, 12.5),
            Block.box(0.75, 6, 8.5, 15.25, 10, 12.5)
    ).reduce((v1, v2) -> Shapes.join(v1,v2, BooleanOp.OR)).get();


    public PhoneBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(POWERED, false));
    }


    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        ItemStack stack = pPlayer.getItemInHand(pHand);

        if (pPlayer instanceof ServerPlayer serverPlayer) {
            ModNetwork.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> serverPlayer),
                    new OpenGuiRequest(OpenGuiRequest.Type.PHONE, 0)
            );
            pLevel.playSound((Player) null,pPos, ModSounds.PHONE_PICKUP.get(), SoundSource.BLOCKS,1.0F,1.0F);
            if(pLevel instanceof ServerLevel serverLevel) {
                BlockPos poweredPos = pPos.relative(Direction.SOUTH, 1);
                BlockPos leverPos = pPos.relative(Direction.SOUTH, 2);
                BlockState poweredState = serverLevel.getBlockState(poweredPos);
                BlockState leverState = serverLevel.getBlockState(leverPos);
                if (poweredState.hasProperty(POWERED)) {
                    serverLevel.setBlock(poweredPos, poweredState.setValue(POWERED, false), 3);
                }
                serverLevel.setBlock(leverPos, Blocks.AIR.defaultBlockState(), 3);
                serverLevel.updateNeighborsAt(leverPos, leverState.getBlock());
                serverLevel.updateNeighborsAt(poweredPos, poweredState.getBlock());
                serverLevel.updateNeighborsAt(pPos, pState.getBlock());
            }
        }

        return InteractionResult.sidedSuccess(pLevel.isClientSide);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos,
                                Block block, BlockPos fromPos, boolean isMoving) {

        if (!level.isClientSide) {
            boolean isPowered = level.hasNeighborSignal(pos);
            boolean wasPowered = state.getValue(POWERED);

            if (isPowered != wasPowered) {
                level.setBlock(pos, state.setValue(POWERED, isPowered), 3);

                if (isPowered) {
                    level.scheduleTick(pos, this, 1);
                }
            }
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(POWERED)) {
            level.playSound(null, pos,
                    ModSounds.PHONE_RINGING.get(),
                    SoundSource.BLOCKS,
                    1.0F, 1.0F);


            level.scheduleTick(pos, this, 100);
        }
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING,pContext.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, LevelAccessor level, BlockPos pos, Rotation direction) {
        return state.setValue(FACING,direction.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState pState, Mirror pMirror) {
        return pState.rotate(pMirror.getRotation(pState.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING);
        pBuilder.add(POWERED);
    }
}
