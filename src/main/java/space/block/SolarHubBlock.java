package space.block;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import space.block.entity.SolarHubBlockEntity;
import space.client.StarflightModClient;
import space.energy.EnergyNet;
import space.util.BlockSearch;

public class SolarHubBlock extends BlockWithEntity implements EnergyBlock
{
	public static double NOMINAL_OUTPUT = 2.5;
	
	public SolarHubBlock(Settings settings)
	{
		super(settings);
	}
	
	@Override
    public void appendTooltip(ItemStack stack, @Nullable BlockView world, List<Text> tooltip, TooltipContext options)
	{
		ArrayList<Text> textList = new ArrayList<Text>();
		DecimalFormat df = new DecimalFormat("#.##");
		textList.add(Text.translatable("block.space.energy_producer").append(String.valueOf(df.format(NOMINAL_OUTPUT))).append("kJ/s / m^2 (Nominal)").formatted(Formatting.GOLD));
		tooltip.addAll(textList);
		StarflightModClient.hiddenItemTooltip(tooltip, Text.translatable("block.space.solar_hub.description"));
	}
	
	public BlockRenderType getRenderType(BlockState state)
	{
		return BlockRenderType.MODEL;
	}
	
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state)
	{
		return new SolarHubBlockEntity(pos, state);
	}
	
	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack)
	{
		if(!world.isClient())
		{
			updateSolarPanels(world, pos);
			addNode(world, pos);
		}
	}
	
	@Override
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved)
	{
		if(!world.isClient())
			SolarHubBlock.updateSolarPanels(world, pos);
	}
	
	@Override
	public double getPowerOutput(World world, BlockPos pos, BlockState state)
	{
		BlockEntity blockEntity = world.getBlockEntity(pos);
		
		if(!world.getDimension().hasSkyLight() || !(blockEntity instanceof SolarHubBlockEntity))
			return 0;
		else
		{
			SolarHubBlockEntity solarHub = (SolarHubBlockEntity) blockEntity;
			
			// Calculate the output of this solar panel at Earth's distance to to the sun taking the sky angle into account.
			float f = world.getSkyAngle(1.0f);
			float highLimit1 = 0.05f;
			float highLimit2 = 1.0f - highLimit1;
			float lowLimit1 = 0.25f;
			float lowLimit2 = 1.0f - lowLimit1;
			double nominal = NOMINAL_OUTPUT * solarHub.getPanelCount() * solarHub.getPowerFraction();
			
			if(f < highLimit1 || f > highLimit2)
				return nominal;
			else if(f < lowLimit1)
				return nominal * Math.pow(1.0 - ((f - highLimit1) / (lowLimit1 - highLimit1)), 1.0 / 3.0);
			else if(f > lowLimit2)
				return nominal * Math.pow(1.0 - ((f - highLimit2) / (lowLimit2 - highLimit2)), 1.0 / 3.0);
			return 0.0;
		}
	}

	@Override
	public double getPowerDraw(World world, BlockPos pos, BlockState state)
	{
		return 0;
	}

	@Override
	public boolean isSideInput(WorldAccess world, BlockPos pos, BlockState state, Direction direction)
	{
		return false;
	}

	@Override
	public boolean isSideOutput(WorldAccess world, BlockPos pos, BlockState state, Direction direction)
	{
		return true;
	}

	@Override
	public void addNode(World world, BlockPos pos)
	{
		EnergyNet.addProducer(world, pos);
	}
	
	public static void updateSolarPanels(World world, BlockPos startPos)
	{
		if(world.getBlockState(startPos).isAir())
		{
			for(Direction direction : DIRECTIONS)
			{
				BlockState blockState = world.getBlockState(startPos.offset(direction));
				
				if(blockState.getBlock() == StarflightBlocks.SOLAR_PANEL || blockState.getBlock() == StarflightBlocks.SOLAR_HUB)
					updateSolarPanels(world, startPos.offset(direction));
			}
			
			return;
		}
		
		BiPredicate<World, BlockPos> include = (w, p) -> {
			BlockState blockState = w.getBlockState(p);
			return blockState.getBlock() == StarflightBlocks.SOLAR_PANEL || blockState.getBlock() == StarflightBlocks.SOLAR_HUB;
		};
		
		ArrayList<BlockPos> foundList = new ArrayList<BlockPos>();
		ArrayList<BlockPos> hubList = new ArrayList<BlockPos>();
		BlockSearch.search(world, startPos, foundList, include, BlockSearch.MAX_VOLUME, false);
		int panelCount = 0;
		
		for(BlockPos pos : foundList)
		{
			Block block = world.getBlockState(pos).getBlock();
			
			if(block == StarflightBlocks.SOLAR_PANEL && world.isSkyVisible(pos.up()))
				panelCount++;
			else if(block == StarflightBlocks.SOLAR_HUB)
				hubList.add(pos);
		}
		
		for(BlockPos pos : hubList)
		{
			BlockEntity blockEntity = world.getBlockEntity(pos);
			
			if(blockEntity instanceof SolarHubBlockEntity)
			{
				SolarHubBlockEntity solarHub = (SolarHubBlockEntity) blockEntity;
				solarHub.setPanelCount(panelCount);
				solarHub.setPowerFraction(1.0 / hubList.size());
			}
		}
	}
}