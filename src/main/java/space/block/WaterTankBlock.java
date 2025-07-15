package space.block;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import space.block.entity.WaterTankBlockEntity;
import space.util.FluidResourceType;

public class WaterTankBlock extends BlockWithEntity implements FluidUtilityBlock
{
	public static final MapCodec<WaterTankBlock> CODEC = WaterTankBlock.createCodec(WaterTankBlock::new);

	protected WaterTankBlock(Settings settings)
	{
		super(settings);
	}
	
	@Override
	protected MapCodec<? extends BlockWithEntity> getCodec()
	{
		return CODEC;
	}
	
	@Override
	public BlockRenderType getRenderType(BlockState state)
	{
		return BlockRenderType.MODEL;
	}

	@Override
	public boolean isTransparent(BlockState state, BlockView world, BlockPos pos)
	{
		return true;
	}
	
	@Override
	public boolean isSideInvisible(BlockState state, BlockState stateFrom, Direction direction)
	{
		if(stateFrom.isOf(this))
			return true;
		
		return super.isSideInvisible(state, stateFrom, direction);
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state)
	{
		return new WaterTankBlockEntity(pos, state);
	}
	
	@Override
	public boolean canPipeConnectToSide(WorldAccess world, BlockPos pos, BlockState state, Direction direction, FluidResourceType fluidType)
	{
		return fluidType == FluidResourceType.WATER;
	}
	
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type)
	{
		return world.isClient ? null : validateTicker(type, StarflightBlocks.WATER_TANK_BLOCK_ENTITY, WaterTankBlockEntity::serverTick);
	}
}