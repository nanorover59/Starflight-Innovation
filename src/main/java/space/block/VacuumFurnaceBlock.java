package space.block;

import java.text.DecimalFormat;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.BlockView;
import space.planet.PlanetDimensionData;
import space.planet.PlanetList;

public class VacuumFurnaceBlock extends ElectricFurnaceBlock
{
	public VacuumFurnaceBlock(Settings settings)
	{
		super(settings);
	}
	
	@Override
    public void appendTooltip(ItemStack stack, @Nullable BlockView world, List<Text> tooltip, TooltipContext context)
	{
		double pressure = 1.0;
		MinecraftClient client = MinecraftClient.getInstance();
		
		if(client.player != null)
		{
			PlanetDimensionData data = PlanetList.viewpointDimensionData;
			pressure = data == null ? 1.0 : data.getPressure();
		}
		
		DecimalFormat df = new DecimalFormat("#.##");
		tooltip.add(Text.translatable("block.space.energy_consumer_nominal").append(String.valueOf(df.format(getInput()))).append("kJ/s").formatted(Formatting.LIGHT_PURPLE));
		tooltip.add(Text.translatable("block.space.energy_consumer_local").append(String.valueOf(df.format(getInput() * (1.0 + 3.0 * pressure)))).append("kJ/s").formatted(Formatting.LIGHT_PURPLE));
	}
	
	@Override
	public double getInput()
	{
		return 8.0;
	}
	
	@Override
	public double getEnergyCapacity()
	{
		return 128.0;
	}
}