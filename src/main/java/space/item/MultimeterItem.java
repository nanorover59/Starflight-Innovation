package space.item;

import java.text.DecimalFormat;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import space.block.entity.EnergyBlockEntity;
import space.block.entity.FluidTankControllerBlockEntity;
import space.client.StarflightModClient;

public class MultimeterItem extends Item
{
	public MultimeterItem(Settings settings)
	{
		super(settings);
	}
	
	@Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context)
	{
		StarflightModClient.hiddenItemTooltip(tooltip, Text.translatable("item.space.multimeter.description_1"), Text.translatable("item.space.multimeter.description_2"));
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context)
	{
		World world = context.getWorld();
		PlayerEntity player = context.getPlayer();
        BlockPos position = context.getBlockPos();
        BlockEntity blockEntity = world.getBlockEntity(position);
        MutableText text = Text.translatable("");
        DecimalFormat df = new DecimalFormat("#.##");
        player.getItemCooldownManager().update();
        
        if(blockEntity != null && blockEntity instanceof EnergyBlockEntity)
        {
        	EnergyBlockEntity energyBlockEntity = (EnergyBlockEntity) blockEntity;
        	text.append(Text.translatable("block.space.energy.level"));
    		text.append(String.valueOf(df.format(energyBlockEntity.getEnergyStored())));
    		text.append("kJ / ");
    		text.append(String.valueOf(df.format(energyBlockEntity.getEnergyCapacity())));
    		text.append("kJ    ");
    		text.append(String.valueOf(df.format((energyBlockEntity.getEnergyStored() / energyBlockEntity.getEnergyCapacity()) * 100)) + "%");
        }
        else if(blockEntity != null && blockEntity instanceof FluidTankControllerBlockEntity)
        {
        	FluidTankControllerBlockEntity fluidContainer = (FluidTankControllerBlockEntity) blockEntity;
        	
        	if(fluidContainer.getStorageCapacity() > 0)
        	{
	        	text.append(Text.translatable("block.space." + fluidContainer.getFluidType().getName() + "_container.level"));
	        	text.append(String.valueOf(df.format(fluidContainer.getStoredFluid())));
	        	text.append("kg / ");
	        	text.append(String.valueOf(df.format(fluidContainer.getStorageCapacity())));
	        	text.append("kg    ");
	        	text.append(String.valueOf(df.format((fluidContainer.getStoredFluid() / fluidContainer.getStorageCapacity()) * 100)) + "%");
        	}
        }
        
        if(text != Text.EMPTY)
        {	
        	player.sendMessage(text, true);
        	return ActionResult.success(world.isClient);
        }
        else
        	return ActionResult.FAIL;
    }
}
