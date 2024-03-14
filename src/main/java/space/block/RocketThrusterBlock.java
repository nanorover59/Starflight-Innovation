package space.block;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import space.client.StarflightModClient;
import space.planet.PlanetDimensionData;
import space.planet.PlanetList;
import space.util.CubicHermiteSpline;

public class RocketThrusterBlock extends Block implements Waterloggable
{
	public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
	private static final double ISP_MULTIPLIER = 2.0;
	private final double standardGravity = 9.80665;
	private final double massFlow;
	private final double vacuumThrust;
	private final double atmThrust;
	private final double vacuumISP;
	private final double atmISP;
	private final double maxExitPressure;
	private final CubicHermiteSpline splineISP1;
	private final CubicHermiteSpline splineISP2;
	private VoxelShape shape;
	
	public RocketThrusterBlock(Settings settings, VoxelShape shape, double vacuumThrust, double vacuumISP, double atmISP, double maxExitPressure)
	{
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState().with(WATERLOGGED, false));
		this.shape = shape;
		this.vacuumThrust = vacuumThrust;
		this.vacuumISP = vacuumISP * ISP_MULTIPLIER;
		this.atmISP = atmISP * ISP_MULTIPLIER;
		this.maxExitPressure = maxExitPressure;
		this.splineISP1 = new CubicHermiteSpline(0.0, 1.0, this.vacuumISP, this.atmISP, -20.0, -1.0);
		this.splineISP2 = new CubicHermiteSpline(1.0, this.maxExitPressure, this.atmISP, 0.0001, -1.0, -0.0001);
		this.massFlow = vacuumThrust / (this.vacuumISP * standardGravity);
		this.atmThrust = atmISP * massFlow * standardGravity;
	}
	
	@Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
	{
        builder.add(WATERLOGGED);
    }
	
	@Override
	public BlockRenderType getRenderType(BlockState state)
	{
		return BlockRenderType.MODEL;
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context)
	{
		return shape;
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context)
	{
		return shape;
	}

	@Override
	public VoxelShape getCullingShape(BlockState state, BlockView world, BlockPos pos)
	{
		return VoxelShapes.empty();
	}
	
	@Override
	public VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos)
	{
		return shape;
	}
	
	@Override
	public FluidState getFluidState(BlockState state)
	{
		if(state.get(WATERLOGGED).booleanValue())
			return Fluids.WATER.getStill(false);
		
		return super.getFluidState(state);
	}

	@Override
	public boolean isTransparent(BlockState state, BlockView world, BlockPos pos)
	{
		return !(Boolean) state.get(WATERLOGGED);
	}
	
	@Override
	public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos)
	{
		Iterator<BlockState> iterator = world.getStatesInBox(shape.getBoundingBox().offset(pos)).iterator();
		int nonAir = 0;

		while(iterator.hasNext())
		{
			BlockState iteratorBlockState = iterator.next();
			
			if(!iteratorBlockState.isAir())
				nonAir++;
		}
		
		if(nonAir <= 1 && !(shape.getBoundingBox().minY < 0.0 && world.getBlockState(pos.down()).blocksMovement()))
			return true;
		else
		{
			if(world.isClient())
			{
				MinecraftClient client = MinecraftClient.getInstance();
				client.player.sendMessage(Text.translatable("block.space.thruster.error"), true);
			}
			
			return false;
		}
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
	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos)
	{
		if(state.get(WATERLOGGED).booleanValue())
			world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));

		return state;
	}
	
	/**
	 * Get the maximum mass flow of this rocket engine.
	 */
	public double getMassFlow()
	{
		return massFlow;
	}
	
	/**
	 * Get the specific impulse (ISP) of this rocket engine at the given pressure in atm.
	 */
	public double getISP(double p)
	{
		return p <= 1.0 ? splineISP1.get(p) : splineISP2.get(p);
	}
	
	/**
	 * Get the thrust of this rocket engine at the given pressure in atm.
	 */
	public double getThrust(double p)
	{
		return getISP(p) * massFlow * standardGravity;
	}
	
	@Override
    public void appendTooltip(ItemStack stack, @Nullable BlockView world, List<Text> tooltip, TooltipContext options)
	{
		double p = 0.0;
		MinecraftClient client = MinecraftClient.getInstance();
		
		if(client.player != null)
		{
			PlanetDimensionData data = PlanetList.viewpointDimensionData;
			p = data == null ? 1.0 : data.getPressure();
		}
		
		ArrayList<Text> textList = new ArrayList<Text>();
		DecimalFormat df = new DecimalFormat("#.##");
		textList.add(Text.translatable("block.space.vacuum_thrust").append(df.format(vacuumThrust / 1000.0)).append("kN"));
		textList.add(Text.translatable("block.space.vacuum_isp").append(df.format(vacuumISP)).append("s"));
		textList.add(Text.translatable("block.space.local_thrust").append(df.format(getThrust(p) / 1000.0)).append("kN"));
		textList.add(Text.translatable("block.space.local_isp").append(df.format(getISP(p))).append("s"));
		textList.add(Text.translatable("block.space.fuel_draw").append(df.format(massFlow)).append("kg/s"));
		StarflightModClient.hiddenItemTooltip(tooltip, textList);
	}
}