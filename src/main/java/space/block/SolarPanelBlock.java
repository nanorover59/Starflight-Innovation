package space.block;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import space.energy.EnergyNet;

public class SolarPanelBlock extends Block implements EnergyBlock
{
	public static double NOMINAL_OUTPUT = 2.5;
	
	public SolarPanelBlock(Settings settings)
	{
		super(settings);
	}
	
	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack)
	{
		if(!world.isClient())
		{
			addNode(world, pos);
			ArrayList<BlockPos> checkList = new ArrayList<BlockPos>();
			EnergyNet.updateEnergyNodes(world, pos, checkList);
		}
	}
	
	@Override
	public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify)
	{
		if(!world.isClient())
		{
			addNode(world, pos);
			ArrayList<BlockPos> checkList = new ArrayList<BlockPos>();
			EnergyNet.updateEnergyNodes(world, pos, checkList);
		}
	}
	
	@Override
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved)
	{
		if(!world.isClient())
		{
			ArrayList<BlockPos> checkList = new ArrayList<BlockPos>();
			EnergyNet.updateEnergyNodes(world, pos, checkList);
		}
	}
	
	@Override
	public double getPowerOutput(WorldAccess world, BlockPos pos, BlockState state)
	{
		if(!world.getDimension().hasSkyLight() || !world.isSkyVisible(pos.up()))
			return 0;
		else
		{
			// Calculate the output of this solar panel at Earth's distance to to the sun taking the sky angle into account.
			float f = world.getSkyAngle(1.0f);
			float highLimit1 = 0.05f;
			float highLimit2 = 1.0f - highLimit1;
			float lowLimit1 = 0.25f;
			float lowLimit2 = 1.0f - lowLimit1;
			
			if(f < highLimit1 || f > highLimit2)
				return NOMINAL_OUTPUT;
			else if(f < lowLimit1)
				return NOMINAL_OUTPUT * Math.pow(1.0 - ((f - highLimit1) / (lowLimit1 - highLimit1)), 1.0 / 3.0);
			else if(f > lowLimit2)
				return NOMINAL_OUTPUT * Math.pow(1.0 - ((f - highLimit2) / (lowLimit2 - highLimit2)), 1.0 / 3.0);
			return 0.0;
		}
	}

	@Override
	public double getPowerDraw(WorldAccess world, BlockPos pos, BlockState state)
	{
		return 0;
	}

	@Override
	public boolean isSideInput(WorldAccess world, BlockPos pos, BlockState state, Direction direction)
	{
		return false;
	}

	@Override
	public boolean isSideOutput(WorldAccess world, BlockPos pos, BlockState state, Direction direction)
	{
		return true;
	}

	@Override
	public void addNode(World world, BlockPos pos)
	{
		EnergyNet.addProducer(world, pos);
	}
}
