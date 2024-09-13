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
import space.planet.PlanetDimensionData;
import space.planet.PlanetList;

public class VacuumFurnaceBlock extends ElectricFurnaceBlock
{
	public VacuumFurnaceBlock(Settings settings)
	{
		super(settings);
	}
	
	@Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType options)
	{
		double pressure = 1.0;
		PlanetDimensionData data = PlanetList.getClient().getViewpointDimensionData();
		
		if(data != null)
			pressure = data == null ? 1.0 : data.getPressure();
		
		ArrayList<Text> textList = new ArrayList<Text>();
		DecimalFormat df = new DecimalFormat("#.##");
		textList.add(Text.translatable("block.space.energy_consumer_nominal").append(String.valueOf(df.format(getInput()))).append("kJ/s").formatted(Formatting.LIGHT_PURPLE));
		textList.add(Text.translatable("block.space.energy_consumer_local").append(String.valueOf(df.format(getInput() * (1.0 + 3.0 * pressure)))).append("kJ/s").formatted(Formatting.LIGHT_PURPLE));
		textList.add(Text.translatable("block.space.vacuum_furnace.description_1"));
		textList.add(Text.translatable("block.space.vacuum_furnace.description_2"));
		StarflightItems.hiddenItemTooltip(tooltip, textList);
	}
	
	@Override
	public double getInput()
	{
		return 16.0;
	}
	
	@Override
	public double getEnergyCapacity()
	{
		return 128.0;
	}
	
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state)
	{
		return new VacuumFurnaceBlockEntity(pos, state);
	}
}