package space.block;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import space.block.entity.ElectrolyzerBlockEntity;
import space.client.StarflightModClient;
import space.energy.EnergyNet;

public class ElectrolyzerBlock extends BlockWithEntity implements EnergyBlock, FluidUtilityBlock
{
	public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
	public static final BooleanProperty LIT = Properties.LIT;
	private static final double POWER_DRAW = 50.0;
	
	public ElectrolyzerBlock(Settings settings)
	{
		super(settings);
		this.setDefaultState(this.getStateManager().getDefaultState().with(FACING, Direction.NORTH).with(LIT, false));
	}
	
	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager)
	{
		stateManager.add(FACING);
		stateManager.add(LIT);
	}
	
	@Override
    public void appendTooltip(ItemStack stack, @Nullable BlockView world, List<Text> tooltip, TooltipContext context)
	{
		DecimalFormat df = new DecimalFormat("#.##");
		tooltip.add(Text.translatable("block.space.energy_consumer").append(String.valueOf(df.format(POWER_DRAW))).append("kJ/s").formatted(Formatting.GOLD));
		StarflightModClient.hiddenItemTooltip(tooltip, Text.translatable("block.space.electrolyzer.description_1"), Text.translatable("block.space.electrolyzer.description_2"));
	}
	
	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack)
	{
		addNode(world, pos);
		updateWaterState(world, pos, (Direction) state.get(FACING));
	}
	
	@Override
	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos)
	{
		updateWaterState((World) world, pos, (Direction) state.get(FACING));
		return state;
	}
	
	private static void updateWaterState(World world, BlockPos position, Direction direction)
	{
		if(world.isClient())
			return;
		
		int limit = 512;
		BlockPos startPos = position.offset(direction);
		ArrayList<BlockPos> checkList = new ArrayList<BlockPos>();
		checkWater(world, startPos, checkList, limit);
		ElectrolyzerBlockEntity blockEntity = (ElectrolyzerBlockEntity) world.getBlockEntity(position);
		
		if(blockEntity != null)
			blockEntity.setWater(checkList.size() >= limit);
	}
	
	private static void checkWater(World world, BlockPos position, ArrayList<BlockPos> checkList, int limit)
	{
		if(world.getBlockState(position).getBlock() != Blocks.WATER || checkList.contains(position))
			return;
		
		checkList.add(position);
		
		if(checkList.size() >= limit)
			return;
		
		for(Direction direction : Direction.values())
			checkWater(world, position.offset(direction), checkList, limit);
	}
	
	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx)
	{
		return (BlockState) this.getDefaultState().with(FACING, ctx.getPlayerFacing().getOpposite());
	}
	
	@Override
	public BlockRenderType getRenderType(BlockState state)
	{
		return BlockRenderType.MODEL;
	}

	@Override
	public BlockState rotate(BlockState state, BlockRotation rotation)
	{
		return (BlockState) state.with(FACING, rotation.rotate((Direction) state.get(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, BlockMirror mirror)
	{
		return state.rotate(mirror.getRotation((Direction) state.get(FACING)));
	}
	
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state)
	{
		return new ElectrolyzerBlockEntity(pos, state);
	}
	
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type)
	{
		return world.isClient ? null : checkType(type, StarflightBlocks.ELECTROLYZER_BLOCK_ENTITY, ElectrolyzerBlockEntity::serverTick);
	}
	
	@Override
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved)
	{
		if(state.hasBlockEntity() && !state.isOf(newState.getBlock()))
			world.removeBlockEntity(pos);
	}

	@Override
	public double getPowerOutput(WorldAccess world, BlockPos pos, BlockState state)
	{
		return 0.0;
	}

	@Override
	public double getPowerDraw(WorldAccess world, BlockPos pos, BlockState state)
	{
		BlockEntity blockEntity = world.getBlockEntity(pos);
		
		if(blockEntity != null && blockEntity instanceof ElectrolyzerBlockEntity)
			return ((ElectrolyzerBlockEntity) blockEntity).getWater() ? POWER_DRAW : 0.0;
		
		return 0.0;
	}

	@Override
	public boolean isSideInput(WorldAccess world, BlockPos pos, BlockState state, Direction direction)
	{
		return direction == (Direction) state.get(FACING).getOpposite() || direction == Direction.UP || direction == Direction.DOWN;
	}

	@Override
	public boolean isSideOutput(WorldAccess world, BlockPos pos, BlockState state, Direction direction)
	{
		return false;
	}

	@Override
	public void addNode(World world, BlockPos pos)
	{
		EnergyNet.addConsumer(world, pos);
	}
	
	@Override
	public void addNode(World world, BlockPos pos, double storedEnergy)
	{
		EnergyNet.addConsumer(world, pos, storedEnergy);
	}

	@Override
	public String getFluidName()
	{
		return "hydrogen/oxygen";
	}

	@Override
	public boolean canPipeConnectToSide(WorldAccess world, BlockPos pos, BlockState state, Direction direction)
	{
		return direction == (Direction) state.get(FACING).rotateYClockwise() || direction == (Direction) state.get(FACING).rotateYCounterclockwise();
	}
}