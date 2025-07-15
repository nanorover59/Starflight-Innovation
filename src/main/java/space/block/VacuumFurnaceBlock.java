package space.block;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.Item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import space.block.entity.VacuumFurnaceBlockEntity;
import space.item.StarflightItems;

public class VacuumFurnaceBlock extends ElectricFurnaceBlock
{
	public VacuumFurnaceBlock(Settings settings)
	{
		super(settings);
	}
	
	@Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType options)
	{
		ArrayList<Text> textList = new ArrayList<Text>();
		DecimalFormat df = new DecimalFormat("#.#");
		textList.add(Text.translatable("block.space.energy_consumer_vacuum", df.format(getInput())).formatted(Formatting.LIGHT_PURPLE));
		textList.add(Text.translatable("block.space.energy_consumer_atm", df.format(getInput() * 4.0)).formatted(Formatting.RED));
		textList.add(Text.translatable("block.space.vacuum_furnace.description"));
		StarflightItems.hiddenItemTooltip(tooltip, textList);
	}
	
	@Override
	public long getInput()
	{
		return 16;
	}
	
	@Override
	public long getEnergyCapacity()
	{
		return 128;
	}
	
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state)
	{
		return new VacuumFurnaceBlockEntity(pos, state);
	}
}