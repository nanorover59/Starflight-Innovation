package space.item;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import space.block.EnergyBlock;
import space.block.FluidUtilityBlock;
import space.client.StarflightModClient;

public class WrenchItem extends Item
{
	public WrenchItem(Settings settings)
	{
		super(settings);
	}
	
	@Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context)
	{
		StarflightModClient.hiddenItemTooltip(tooltip, Text.translatable("item.space.wrench.description"));
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context)
	{
		World world = context.getWorld();
        BlockPos position = context.getBlockPos();
        BlockState blockState = world.getBlockState(position);
        
        if((blockState.getBlock() instanceof EnergyBlock || blockState.getBlock() instanceof FluidUtilityBlock) && blockState.getProperties().contains(HorizontalFacingBlock.FACING))
        {
        	Direction previousDirection = blockState.get(HorizontalFacingBlock.FACING);
        	world.setBlockState(position, blockState.with(HorizontalFacingBlock.FACING, previousDirection.rotateYClockwise()));
        	return ActionResult.success(world.isClient);
        }
		
		return ActionResult.FAIL;
	}
	
	/*
	@Override
	public boolean canMine(BlockState blockState, World world, BlockPos pos, PlayerEntity player)
	{
		if(world.isClient || player.isCreative())
			return true;
		
		 if(blockState.getBlock() instanceof EnergyBlock || blockState.getBlock() instanceof FluidUtilityBlock)
		 {
			 blockState.onStacksDropped((ServerWorld) world, pos, getDefaultStack());
			 world.setBlockState(pos, Blocks.AIR.getDefaultState());
		 }
		
		return true;
	}
	*/
}
