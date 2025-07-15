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

public class RocketThrusterExtensionBlock extends FacingBlock implements Waterloggable
{

	public static final MapCodec<RocketThrusterExtensionBlock> CODEC = RocketThrusterExtensionBlock.createCodec(RocketThrusterExtensionBlock::new);
	public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
	private final double vacuumFactor;
	private final double atmFactor;
	
	protected RocketThrusterExtensionBlock(Settings settings, double vacuumFactor, double atmFactor)
	{
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState().with(WATERLOGGED, false));
		this.vacuumFactor = vacuumFactor;
		this.atmFactor = atmFactor;
	}

	public RocketThrusterExtensionBlock(Settings settings)
	{
		this(settings, 0.0, 0.0);
	}
	
	@Override
	public MapCodec<? extends RocketThrusterExtensionBlock> getCodec()
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
	
	public double getVacuumFactor()
	{
		return vacuumFactor;
	}
	
	public double getAtmFactor()
	{
		return atmFactor;
	}

	@Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType options)
	{
		ArrayList<Text> textList = new ArrayList<Text>();
		DecimalFormat df = new DecimalFormat("#.#");
		textList.add(Text.translatable("block.space.engine_extension_vacuum", (vacuumFactor < 1.0 ? "" : "+") + df.format((vacuumFactor - 1.0) * 100.0)).formatted(Formatting.LIGHT_PURPLE));
		textList.add(Text.translatable("block.space.engine_extension_atm", (atmFactor < 1.0 ? "" : "+") + df.format((atmFactor - 1.0) * 100.0)).formatted(Formatting.GOLD));
		StarflightItems.hiddenItemTooltip(tooltip, textList);
	}
}