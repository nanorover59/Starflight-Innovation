package space.item;

import java.text.DecimalFormat;
import java.util.List;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import space.block.entity.BalloonControllerBlockEntity;
import space.block.entity.EnergyBlockEntity;
import space.block.entity.FluidTankControllerBlockEntity;

public class MultimeterItem extends Item
{
	public MultimeterItem(Settings settings)
	{
		super(settings);
	}
	
	@Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType options)
	{
		StarflightItems.hiddenItemTooltip(tooltip, Text.translatable("item.space.multimeter.description_1"), Text.translatable("item.space.multimeter.description_2"));
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context)
	{
		World world = context.getWorld();
		PlayerEntity player = context.getPlayer();
        BlockPos position = context.getBlockPos();
        BlockEntity blockEntity = world.getBlockEntity(position);
        MutableText text = Text.translatable("");
        DecimalFormat df = new DecimalFormat("#.#");
        player.getItemCooldownManager().update();
        
        if(blockEntity != null && blockEntity instanceof EnergyBlockEntity)
        {
        	EnergyBlockEntity energyBlockEntity = (EnergyBlockEntity) blockEntity;
        	text.append(Text.translatable("block.space.energy.level", df.format(energyBlockEntity.getEnergy()), df.format(energyBlockEntity.getEnergyCapacity()), df.format((energyBlockEntity.getEnergy() / energyBlockEntity.getEnergyCapacity()) * 100)));
        }
        else if(blockEntity != null && blockEntity instanceof FluidTankControllerBlockEntity)
        {
        	FluidTankControllerBlockEntity fluidContainer = (FluidTankControllerBlockEntity) blockEntity;
        	
        	if(fluidContainer.getStorageCapacity() > 0)
	        	text.append(Text.translatable("block.space." + fluidContainer.getFluidType().getName() + "_container.level", df.format(fluidContainer.getStoredFluid()), df.format(fluidContainer.getStorageCapacity()), df.format((fluidContainer.getStoredFluid() / fluidContainer.getStorageCapacity()) * 100)));
        }
        else if(blockEntity != null && blockEntity instanceof BalloonControllerBlockEntity)
        {
        	BalloonControllerBlockEntity fluidContainer = (BalloonControllerBlockEntity) blockEntity;
        	
        	if(fluidContainer.getStorageCapacity() > 0)
	        	text.append(Text.translatable("block.space.hydrogen_container.level", df.format(fluidContainer.getStoredFluid()), df.format(fluidContainer.getStorageCapacity()), df.format((fluidContainer.getStoredFluid() / fluidContainer.getStorageCapacity()) * 100)));
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
