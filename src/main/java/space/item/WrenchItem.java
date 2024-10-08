package space.item;

import java.util.List;

import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import space.block.AirwayBlock;
import space.block.ValveBlock;
import space.util.StarflightSoundEvents;

public class WrenchItem extends Item
{
	public WrenchItem(Settings settings)
	{
		super(settings);
	}
	
	@Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType options)
	{
		StarflightItems.hiddenItemTooltip(tooltip, Text.translatable("item.space.wrench.description"));
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context)
	{
		World world = context.getWorld();
        BlockPos position = context.getBlockPos();
        BlockState blockState = world.getBlockState(position);
        
        if(blockState.getProperties().contains(HorizontalFacingBlock.FACING))
        {
        	Direction previousDirection = blockState.get(HorizontalFacingBlock.FACING);
        	world.setBlockState(position, blockState.with(HorizontalFacingBlock.FACING, previousDirection.rotateYClockwise()));
        	world.playSoundAtBlockCenter(position, StarflightSoundEvents.WRENCH_SOUND_EVENT, SoundCategory.BLOCKS, 1.0f, 1.0f, true);
        	return ActionResult.success(world.isClient);
        }
        else if(blockState.getProperties().contains(ValveBlock.MODE))
        {
        	world.setBlockState(position, blockState.cycle(ValveBlock.MODE));
        	world.playSoundAtBlockCenter(position, StarflightSoundEvents.WRENCH_SOUND_EVENT, SoundCategory.BLOCKS, 1.0f, 1.0f, true);
        	return ActionResult.success(world.isClient);
        }
        else if(blockState.getProperties().contains(AirwayBlock.CLOSED))
        {
        	world.setBlockState(position, blockState.cycle(AirwayBlock.CLOSED));
        	world.playSoundAtBlockCenter(position, StarflightSoundEvents.WRENCH_SOUND_EVENT, SoundCategory.BLOCKS, 1.0f, 1.0f, true);
        	return ActionResult.success(world.isClient);
        }
		
		return ActionResult.FAIL;
	}
}