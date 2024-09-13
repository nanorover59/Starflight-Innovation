package space.block;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import space.block.entity.FluidTankControllerBlockEntity;
import space.block.entity.OxygenTankBlockEntity;

public class OxygenTankBlock extends FluidTankControllerBlock
{
	public OxygenTankBlock(Settings settings)
	{
		super(settings, StarflightBlocks.OXYGEN_TANK_CAPACITY);
	}
	
	@Override
	public BlockRenderType getRenderType(BlockState state)
	{
		return BlockRenderType.MODEL;
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state)
	{
		return new OxygenTankBlockEntity(pos, state);
	}
	
	@Override
	public int initializeFluidTank(World world, BlockPos position, FluidTankControllerBlockEntity fluidTankController)
	{
		return initializeFluidTank(world, position, "oxygen", StarflightBlocks.OXYGEN_TANK_CAPACITY, fluidTankController);
	}
}