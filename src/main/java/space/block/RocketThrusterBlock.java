package space.block;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.FacingBlock;
import net.minecraft.block.Waterloggable;
import net.minecraft.client.MinecraftClient;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item.TooltipContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import space.client.StarflightModClient;
import space.planet.PlanetDimensionData;
import space.planet.PlanetList;
import space.util.CubicHermiteSpline;

public class RocketThrusterBlock extends FacingBlock implements Waterloggable
{
	public static final MapCodec<RocketThrusterBlock> CODEC = RocketThrusterBlock.createCodec(RocketThrusterBlock::new);
	
	public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
	private static final double ISP_MULTIPLIER = 1.0;
	private final double standardGravity = 9.80665;
	private final double massFlow;
	private final double vacuumThrust;
	private final double atmThrust;
	private final double vacuumISP;
	private final double atmISP;
	private final double maxExitPressure;
	private final double gimbal;
	private final CubicHermiteSpline splineISP1;
	private final CubicHermiteSpline splineISP2;
	
	public RocketThrusterBlock(Settings settings, double vacuumThrust, double vacuumISP, double atmISP, double maxExitPressure, double gimbal)
	{
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState().with(WATERLOGGED, false));
		this.vacuumThrust = vacuumThrust;
		this.vacuumISP = vacuumISP * ISP_MULTIPLIER;
		this.atmISP = atmISP * ISP_MULTIPLIER;
		this.maxExitPressure = maxExitPressure;
		this.gimbal = gimbal;
		this.splineISP1 = new CubicHermiteSpline(0.0, 1.0, this.vacuumISP, this.atmISP, -10.0, -1.0);
		this.splineISP2 = new CubicHermiteSpline(1.0, this.maxExitPressure, this.atmISP, 0.0001, -1.0, -0.0001);
		this.massFlow = vacuumThrust / (this.vacuumISP * standardGravity);
		this.atmThrust = atmISP * massFlow * standardGravity;
	}
	
	public RocketThrusterBlock(Settings settings)
	{
		this(settings, 0.0, 0.0, 0.0, 0.0, 0.0);
	}
	
	@Override
	public MapCodec<? extends RocketThrusterBlock> getCodec()
	{
		return CODEC;
	}
	
	@Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
	{
		builder.add(FACING);
        builder.add(WATERLOGGED);
    }
	
	@Override
	public BlockRenderType getRenderType(BlockState state)
	{
		return BlockRenderType.MODEL;
	}

	@Override
	public VoxelShape getCullingShape(BlockState state, BlockView world, BlockPos pos)
	{
		return VoxelShapes.empty();
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
	@Nullable
	public BlockState getPlacementState(ItemPlacementContext context)
	{
		BlockPos blockPos = context.getBlockPos();
		FluidState fluidState = context.getWorld().getFluidState(blockPos);
		return this.getDefaultState().with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER && fluidState.isStill()).with(FACING, context.getPlayerLookDirection());
	}
	
	@Override
	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos)
	{
		if(state.get(WATERLOGGED).booleanValue())
			world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));

		return state;
	}
	
	@Override
	public BlockState rotate(BlockState state, BlockRotation rotation)
	{
		return (BlockState) state.with(FACING, rotation.rotate(state.get(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, BlockMirror mirror)
	{
		return state.rotate(mirror.getRotation(state.get(FACING)));
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
	
	public double getMaxGimbal()
	{
		return gimbal;
	}
	
	@Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType options)
	{
		double pressure = 1.0;
		MinecraftClient client = MinecraftClient.getInstance();
		
		if(client.player != null)
		{
			PlanetDimensionData data = PlanetList.getClient().getViewpointDimensionData();
			pressure = data == null ? 1.0 : data.getPressure();
		}
		
		ArrayList<Text> textList = new ArrayList<Text>();
		DecimalFormat df = new DecimalFormat("#.##");
		textList.add(Text.translatable("block.space.vacuum_thrust").append(df.format(vacuumThrust / 1000.0)).append("kN"));
		textList.add(Text.translatable("block.space.vacuum_isp").append(df.format(vacuumISP)).append("s"));
		textList.add(Text.translatable("block.space.local_thrust").append(df.format(getThrust(pressure) / 1000.0)).append("kN"));
		textList.add(Text.translatable("block.space.local_isp").append(df.format(getISP(pressure))).append("s"));
		textList.add(Text.translatable("block.space.fuel_draw").append(df.format(massFlow)).append("kg/s"));
		StarflightModClient.hiddenItemTooltip(tooltip, textList);
	}
}