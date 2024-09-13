package space.block;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import space.block.entity.FluidTankControllerBlockEntity;
import space.block.entity.HydrogenTankBlockEntity;

public class HydrogenTankBlock extends FluidTankControllerBlock
{
	public HydrogenTankBlock(Settings settings)
	{
		super(settings, StarflightBlocks.HYDROGEN_TANK_CAPACITY);
	}

	@Override
	public BlockRenderType getRenderType(BlockState state)
	{
		return BlockRenderType.MODEL;
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state)
	{
		return new HydrogenTankBlockEntity(pos, state);
	}
	
	@Override
	public int initializeFluidTank(World world, BlockPos position, FluidTankControllerBlockEntity fluidTankController)
	{
		return initializeFluidTank(world, position, "hydrogen", StarflightBlocks.HYDROGEN_TANK_CAPACITY, fluidTankController);
	}
}