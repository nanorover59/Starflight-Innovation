package space.block;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import space.block.entity.SolarPanelBlockEntity;
import space.client.StarflightModClient;
import space.planet.ClientPlanet;
import space.planet.ClientPlanetList;
import space.util.BlockSearch;

public class SolarPanelBlock extends BlockWithEntity implements Waterloggable, EnergyBlock
{
	public static final MapCodec<SolarPanelBlock> CODEC = SolarPanelBlock.createCodec(SolarPanelBlock::new);
	public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
	protected static final VoxelShape SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 4.0, 16.0);
	
	public SolarPanelBlock(Settings settings)
	{
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState().with(WATERLOGGED, false));
	}
	
	@Override
	public MapCodec<? extends SolarPanelBlock> getCodec()
	{
		return CODEC;
	}
	
	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
	{
		builder.add(WATERLOGGED);
	}
	
	@Override
    public void appendTooltip(ItemStack stack, @Nullable BlockView world, List<Text> tooltip, TooltipContext options)
	{
		double solarMultiplier = 1.0;
		ClientPlanet viewpointPlanet = ClientPlanetList.getViewpointPlanet();
		
		if(viewpointPlanet != null)
		{
			double d = viewpointPlanet.getPosition().lengthSquared();
			
			if(d > 0.0)
			{
				d /= 2.238016e22; // Convert the distance from meters to astronomical units.
				solarMultiplier = (1.0 / d) * (viewpointPlanet.hasCloudCover ? 0.5 : 1.0);
			}
		}
		
		ArrayList<Text> textList = new ArrayList<Text>();
		DecimalFormat df = new DecimalFormat("#.##");
		textList.add(Text.translatable("block.space.energy_producer_nominal").append(String.valueOf(df.format(getOutput()))).append("kJ/s").formatted(Formatting.GOLD));
		textList.add(Text.translatable("block.space.energy_producer_local").append(String.valueOf(df.format(getOutput() * solarMultiplier))).append("kJ/s").formatted(Formatting.GOLD));
		StarflightModClient.hiddenItemTooltip(tooltip, Text.translatable("block.space.solar_panel.description"));
		tooltip.addAll(textList);
	}
	
	@Override
	public double getOutput()
	{
		return 4.0;
	}
	
	@Override
	public double getEnergyCapacity()
	{
		return 16.0;
	}

	@Override
	public BlockRenderType getRenderType(BlockState state)
	{
		return BlockRenderType.MODEL;
	}

	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context)
	{
		return SHAPE;
	}
	
	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context)
	{
		return SHAPE;
	}
	
	@Override
	public VoxelShape getCullingShape(BlockState state, BlockView world, BlockPos pos)
	{
		return SHAPE;
	}

	@Override
	public boolean isTransparent(BlockState state, BlockView world, BlockPos pos)
	{
		return !(Boolean) state.get(WATERLOGGED);
	}

	@Override
	public FluidState getFluidState(BlockState state)
	{
		if(state.get(WATERLOGGED).booleanValue())
			return Fluids.WATER.getStill(false);

		return super.getFluidState(state);
	}
	
	@Override
	@Nullable
	public BlockState getPlacementState(ItemPlacementContext context)
	{
		BlockPos blockPos = context.getBlockPos();
		FluidState fluidState = context.getWorld().getFluidState(blockPos);
		return this.getDefaultState().with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER && fluidState.isStill());
	}
	
	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack)
	{
		if(!world.isClient)
			BlockSearch.energyConnectionSearch(world, pos);
	}
	
	@Override
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved)
	{
		if(state.hasBlockEntity() && !state.isOf(newState.getBlock()))
			world.removeBlockEntity(pos);
		
		if(!world.isClient)
			BlockSearch.energyConnectionSearch(world, pos);
	}
	
	@Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify)
    {
		if(state.get(WATERLOGGED).booleanValue())
			world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
    }
	
	@Override
	public boolean isOutput(World world, BlockPos pos, BlockState state, Direction direction)
	{
		return direction != Direction.UP;
	}

	@Override
	public boolean isInput(World world, BlockPos pos, BlockState state, Direction direction)
	{
		return false;
	}
	
	@Override
	public boolean isPassThrough(World world, BlockPos pos, BlockState state, Direction direction)
	{
		return direction != Direction.UP;
	}
	
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state)
	{
		return new SolarPanelBlockEntity(pos, state);
	}
	
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type)
	{
		return world.isClient ? null : validateTicker(type, StarflightBlocks.SOLAR_PANEL_BLOCK_ENTITY, SolarPanelBlockEntity::serverTick);
	}
}