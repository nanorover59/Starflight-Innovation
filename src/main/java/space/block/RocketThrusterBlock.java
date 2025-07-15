package space.block;

import java.util.ArrayList;
import java.util.List;

import javax.swing.text.html.StyleSheet;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.FacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
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
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import space.item.StarflightItems;
import space.util.CubicHermiteSpline;

public class RocketThrusterBlock extends FacingBlock implements Waterloggable
{
	public static final MapCodec<RocketThrusterBlock> CODEC = RocketThrusterBlock.createCodec(RocketThrusterBlock::new);
	
	public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
	private static final double ISP_MULTIPLIER = 2.0;
	private final double vacuumThrust;
	private final double atmThrust;
	private final double vacuumISP;
	private final double atmISP;
	private final double gimbal;
	
	public RocketThrusterBlock(Settings settings, double vacuumThrust, double vacuumISP, double atmISP, double gimbal)
	{
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState().with(WATERLOGGED, false));
		this.vacuumISP = vacuumISP * ISP_MULTIPLIER;
		this.atmISP = atmISP * ISP_MULTIPLIER;
		this.vacuumThrust = vacuumThrust;
		this.atmThrust = getThrust(vacuumISP, atmISP, vacuumThrust, 1.0);
		this.gimbal = gimbal;
	}
	
	public RocketThrusterBlock(Settings settings)
	{
		this(settings, 0.0, 0.0, 0.0, 0.0);
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
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context)
	{
		return Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 15.99, 16.0);
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
	
	public double getVacuumnISP()
	{
		return vacuumISP;
	}
	
	public double getAtmISP()
	{
		return atmISP;
	}
	
	public double getVacuumThrust()
	{
		return vacuumThrust;
	}
	
	public double getAtmThrust()
	{
		return atmThrust;
	}

	public double getGimbal()
	{
		return gimbal;
	}
	
	@Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType options)
	{
		ArrayList<Text> textList = new ArrayList<Text>();
		textList.add(Text.translatable("block.space.engine_thrust_vacuum", (int) (vacuumThrust / 1000.0)).formatted(Formatting.LIGHT_PURPLE));
		textList.add(Text.translatable("block.space.engine_isp_vacuum", (int) vacuumISP).formatted(Formatting.LIGHT_PURPLE));
		textList.add(Text.translatable("block.space.engine_thrust_atm", (int) (atmThrust / 1000.0)).formatted(Formatting.GOLD));
		textList.add(Text.translatable("block.space.engine_isp_atm", (int) atmISP).formatted(Formatting.GOLD));
		StarflightItems.hiddenItemTooltip(tooltip, textList);
	}
	
	public static double getISP(double vacuumISP, double atmISP, double pressure)
	{
		CubicHermiteSpline splineISP1 = new CubicHermiteSpline(0.0, 1.0, vacuumISP, atmISP, -10.0, -1.0);
		CubicHermiteSpline splineISP2 = new CubicHermiteSpline(1.0, 100.0, atmISP, 0.0001, -1.0, -0.0001);
		return pressure <= 1.0 ? splineISP1.get(pressure) : splineISP2.get(pressure);
	}
	
	public static double getThrust(double vacuumISP, double atmISP, double vacuumThrust, double pressure)
	{
		return getISP(vacuumISP, atmISP, pressure) * (vacuumThrust / vacuumISP);
	}
}