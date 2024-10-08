package space.item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import space.block.StarflightBlocks;
import space.util.BlockSearch;

public class DividerItem extends Item
{
	public DividerItem(Settings settings)
	{
		super(settings);
	}
	
	@Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType options)
	{
		tooltip.add(Text.translatable("item.space.creative").formatted(Formatting.ITALIC, Formatting.RED));
		StarflightItems.hiddenItemTooltip(tooltip, Text.translatable("item.space.divider.description_1"), Text.translatable("item.space.divider.description_2"));
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context)
	{
		World world = context.getWorld();
		BlockPos position = context.getBlockPos();
		boolean valid = false;
		
		if(!world.isClient)
		{
			for(Direction direction : Direction.values())
			{
				ArrayList<BlockPos> checkList = new ArrayList<BlockPos>(); // Check list to avoid ensure each block is only checked once.
				
				BiPredicate<World, BlockPos> include = (w, p) -> {
					return !world.getBlockState(p).isIn(StarflightBlocks.FLUID_TANK_BLOCK_TAG);
				};
				
				BlockSearch.search(world, position.offset(direction), checkList, include, BlockSearch.MAX_VOLUME, true);
				
				if(!checkList.isEmpty() && checkList.size() < BlockSearch.MAX_VOLUME)
				{
					double cx = 0;
					double cy = 0;
					double cz = 0;
					int count = 0;
	
					for(BlockPos p : checkList)
					{
						if(world.getBlockState(p).isAir())
						{	
							cx += p.getX();
							cy += p.getY();
							cz += p.getZ();
							count++;
						}
					}
					
					cx /= count;
					cy /= count;
					cz /= count;
					
					for(BlockPos p : checkList)
					{
						if(p.getY() == Math.round(cy))
						{
							world.setBlockState(p, StarflightBlocks.STRUCTURAL_ALUMINUM.getDefaultState());
							
							if(p.getX() == Math.round(cx) && p.getZ() == Math.round(cz))
							{
								world.setBlockState(p.up(), Blocks.GLOWSTONE.getDefaultState());
								world.setBlockState(p.down(), Blocks.GLOWSTONE.getDefaultState());
							}
						}
					}
					
					valid = true;
				}
			}
			
			if(!context.getPlayer().isCreative() && valid)
	            context.getStack().decrement(1);
			
			if(valid)
				context.getPlayer().sendMessage(Text.translatable("item.space.divider.complete"), true);
			else
				context.getPlayer().sendMessage(Text.translatable("item.space.divider.error"), true);
		}
		
		return ActionResult.success(world.isClient);
	}
}