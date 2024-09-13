package space.item;

import java.text.DecimalFormat;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import space.block.FluidTankControllerBlock;
import space.block.entity.FluidTankControllerBlockEntity;
import space.client.StarflightModClient;

public class LoaderItem extends Item
{
	private String fluid;
	
	public LoaderItem(Settings settings, String fluid)
	{
		super(settings);
		this.fluid = fluid;
	}
	
	@Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context)
	{
		tooltip.add(Text.translatable("item.space.creative").formatted(Formatting.ITALIC, Formatting.RED));
		StarflightModClient.hiddenItemTooltip(tooltip, Text.translatable("item.space." + fluid + "_loader.description_1"), Text.translatable("item.space." + fluid + "_loader.description_2"));
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context)
	{
		World world = context.getWorld();
        BlockPos position = context.getBlockPos();
        
        if(!world.isClient)
        {
        	BlockEntity blockEntity = world.getBlockEntity(position);
        	
        	if(blockEntity != null && blockEntity instanceof FluidTankControllerBlockEntity)
        	{
        		FluidTankControllerBlockEntity fluidTank = (FluidTankControllerBlockEntity) blockEntity;
        		
        		if(fluidTank.getFluidName() == fluid)
        		{
	        		if(!fluidTank.isActive())
	        			((FluidTankControllerBlock) world.getBlockState(position).getBlock()).initializeFluidTank(world, position, fluidTank);
	        		
					fluidTank.setStoredFluid(fluidTank.getStorageCapacity());
					MutableText text = Text.translatable("");
			        DecimalFormat df = new DecimalFormat("#.##");
					text.append(Text.translatable("block.space." + fluidTank.getFluidName() + "_container.level"));
		        	text.append(String.valueOf(df.format(fluidTank.getStoredFluid())));
		        	text.append("kg / ");
		        	text.append(String.valueOf(df.format(fluidTank.getStorageCapacity())));
		        	text.append("kg    ");
		        	text.append(String.valueOf(df.format((fluidTank.getStoredFluid() / fluidTank.getStorageCapacity()) * 100)) + "%");
		        	context.getPlayer().sendMessage(text, true);
        		}
        	}
        }
		
		return ActionResult.success(world.isClient);
	}
}