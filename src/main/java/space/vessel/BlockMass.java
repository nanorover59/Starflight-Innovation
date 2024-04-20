package space.vessel;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

public class BlockMass
{
	public static double volumeForBlock(BlockState blockState, World world, BlockPos pos)
	{
		double volume = 0;
		
		for(Box box : blockState.getOutlineShape(world, pos).getBoundingBoxes())
			volume += box.getLengthX() * box.getLengthY() * box.getLengthZ();
		
		return volume;
	}
	
	public static double getMass(World world, BlockPos pos)
	{
		BlockState blockState = world.getBlockState(pos);
		
		if(blockState.isAir())
			return 0.0;
		
		double density = 100; // Assume a density of 25kg per cubic meter by default.
		
		//if(blockState.isIn(StarflightBlocks.FLUID_TANK_BLOCK_TAG))
		//	density = 20;
		
		double mass = volumeForBlock(blockState, world, pos) * density;
		BlockEntity blockEntity = world.getBlockEntity(pos);
	
		// If the block has a block entity, check for an inventory using the same function as hoppers.
		if(blockEntity != null)
    	{
			Inventory inventory = HopperBlockEntity.getInventoryAt(world, pos);
			
			if(inventory != null)
			{
				mass *= 0.25;
				
	    		for(int i = 0; i < inventory.size(); i++)
	    		{
	    			ItemStack stack = inventory.getStack(i);
	    			
	    			if(stack.getItem() instanceof BlockItem)
	    				mass += stack.getCount() * volumeForBlock(((BlockItem) stack.getItem()).getBlock().getDefaultState(), world, pos) * 100.0;
	    			else
	    				mass += stack.getCount() * 10.0;
	    		}
			}
    	}
		
		return mass;
	}
}