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
import net.minecraft.block.Blocks;
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
import space.block.entity.PumpBlockEntity;
import space.client.StarflightModClient;
import space.util.BlockSearch;
import space.util.FluidResourceType;

public class PumpBlock extends BlockWithEntity implements EnergyBlock, FluidUtilityBlock
{
	public static final MapCodec<PumpBlock> CODEC = PumpBlock.createCodec(PumpBlock::new);
	public static final DirectionProperty FACING = Properties.FACING;
	public static final BooleanProperty LIT = Properties.LIT;
	
	public PumpBlock(Settings settings)
	{
		super(settings);
		this.setDefaultState(this.getStateManager().getDefaultState().with(FACING, Direction.NORTH).with(LIT, false));
	}
	
	@Override
	public MapCodec<? extends PumpBlock> getCodec()
	{
		return CODEC;
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
		tooltip.add(Text.translatable("block.space.energy_consumer").append(String.valueOf(df.format(getInput()))).append("kJ/s").formatted(Formatting.LIGHT_PURPLE));
		StarflightModClient.hiddenItemTooltip(tooltip, Text.translatable("block.space.pump.description_1"), Text.translatable("block.space.pump.description_2"));
	}
	
	@Override
	public double getInput()
	{
		return 16.0;
	}
	
	@Override
	public double getEnergyCapacity()
	{
		return 64.0;
	}
	
	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack)
	{
		updateWaterState(world, pos, (Direction) state.get(FACING));
		
		if(!world.isClient)
			BlockSearch.energyConnectionSearch(world, pos);
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
		PumpBlockEntity blockEntity = (PumpBlockEntity) world.getBlockEntity(position);
		
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
	
	/*@Override
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random)
	{
		if((Boolean) state.get(LIT))
		{
			double d = (double) pos.getX() + 0.5;
			double e = (double) pos.getY();
			double f = (double) pos.getZ() + 0.5;
			
			if(random.nextDouble() < 0.1)
				world.playSound(d, e, f, StarflightEffects.CURRENT_SOUND_EVENT, SoundCategory.BLOCKS, 0.25f, 0.5f - 0.1f * random.nextFloat(), true);
		}
	}*/
	
	@Override
	public BlockState getPlacementState(ItemPlacementContext context)
	{
		return (BlockState) this.getDefaultState().with(FACING, context.getPlayerLookDirection().getOpposite());
	}
	
	@Override
	public BlockRenderType getRenderType(BlockState state)
	{
		return BlockRenderType.MODEL;
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
	
	@Override
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved)
	{
		if(state.hasBlockEntity() && !state.isOf(newState.getBlock()))
			world.removeBlockEntity(pos);
		
		if(!world.isClient && !state.isOf(newState.getBlock()))
			BlockSearch.energyConnectionSearch(world, pos);
	}
	
	@Override
	public boolean isOutput(World world, BlockPos pos, BlockState state, Direction direction)
	{
		return false;
	}

	@Override
	public boolean isInput(World world, BlockPos pos, BlockState state, Direction direction)
	{
		return direction == (Direction) state.get(FACING).getOpposite() || direction == Direction.UP || direction == Direction.DOWN;
	}

	@Override
	public FluidResourceType getFluidType()
	{
		return FluidResourceType.ANY;
	}

	@Override
	public boolean canPipeConnectToSide(WorldAccess world, BlockPos pos, BlockState state, Direction direction)
	{
		return direction == (Direction) state.get(FACING).rotateYClockwise() || direction == (Direction) state.get(FACING).rotateYCounterclockwise();
	}
	
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state)
	{
		return new PumpBlockEntity(pos, state);
	}
	
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type)
	{
		return world.isClient ? null : validateTicker(type, StarflightBlocks.PUMP_BLOCK_ENTITY, PumpBlockEntity::serverTick);
	}
}