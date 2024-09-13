package space.item;

import java.text.DecimalFormat;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
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
import space.block.BatteryBlock;
import space.block.EnergyBlock;
import space.block.entity.BatteryBlockEntity;
import space.block.entity.FluidTankControllerBlockEntity;
import space.client.StarflightModClient;
import space.energy.EnergyNet;
import space.energy.EnergyNode;

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
        BlockState blockState = world.getBlockState(position);
        BlockEntity blockEntity = world.getBlockEntity(position);
        MutableText text = Text.translatable("");
        DecimalFormat df = new DecimalFormat("#.##");
        player.getItemCooldownManager().update();
        if(blockEntity != null && blockEntity instanceof FluidTankControllerBlockEntity)
        {
        	FluidTankControllerBlockEntity fluidContainer = (FluidTankControllerBlockEntity) blockEntity;
        	
        	if(fluidContainer.isActive())
        	{
	        	text.append(Text.translatable("block.space." + fluidContainer.getFluidName() + "_container.level"));
	        	text.append(String.valueOf(df.format(fluidContainer.getStoredFluid())));
	        	text.append("kg / ");
	        	text.append(String.valueOf(df.format(fluidContainer.getStorageCapacity())));
	        	text.append("kg    ");
	        	text.append(String.valueOf(df.format((fluidContainer.getStoredFluid() / fluidContainer.getStorageCapacity()) * 100)) + "%");
        	}
        }
        else if(blockState.getBlock() instanceof EnergyBlock)
        {
        	EnergyNode producer = EnergyNet.getProducer(position, world.getRegistryKey());
        	EnergyNode consumer = EnergyNet.getConsumer(position, world.getRegistryKey());
        	
        	if(blockState.getBlock() instanceof BatteryBlock && producer != null)
        	{
        		BatteryBlockEntity batteryBlockEntity = (BatteryBlockEntity) world.getBlockEntity(position);
        		double chargeCapacity = batteryBlockEntity.getChargeCapacity();
        		double charge = batteryBlockEntity.getCharge();
        		text.append(Text.translatable("block.space.battery.level"));
        		text.append(String.valueOf(df.format(charge)));
        		text.append("kJ / ");
        		text.append(String.valueOf(df.format(chargeCapacity)));
        		text.append("kJ    ");
        		
        		if(chargeCapacity > 0)
        			text.append(String.valueOf(df.format((charge / chargeCapacity) * 100)) + "%");
        	}
        	else if(producer != null)
        	{
        		text.append(Text.translatable("block.space.energy_producer"));
        		text.append(String.valueOf(df.format(((EnergyBlock) blockState.getBlock()).getPowerOutput(world, position, blockState))));
        		text.append("kJ/s");
        	}
        	else if(consumer != null)
        	{
        		text.append(Text.translatable("block.space.energy_consumer"));
        		text.append(String.valueOf(df.format(((EnergyBlock) blockState.getBlock()).getPowerDraw(world, position, blockState))));
        		text.append("kJ/s");
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
