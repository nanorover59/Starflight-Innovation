package space.block;

import java.text.DecimalFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item.TooltipContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import space.block.entity.PumpBlockEntity;
import space.item.StarflightItems;
import space.util.BlockSearch;
import space.util.FluidResourceType;

public class PumpBlock extends BlockWithEntity implements EnergyBlock, FluidUtilityBlock
{
	public static final MapCodec<PumpBlock> CODEC = PumpBlock.createCodec(PumpBlock::new);
	public static final DirectionProperty FACING = Properties.FACING;
	public static final BooleanProperty LIT = Properties.LIT;
	public static final BooleanProperty WATER = BooleanProperty.of("water");
	
	public PumpBlock(Settings settings)
	{
		super(settings);
		this.setDefaultState(this.getStateManager().getDefaultState().with(FACING, Direction.NORTH).with(LIT, false).with(WATER, false));
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
		stateManager.add(WATER);
	}
	
	@Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType options)
	{
		ArrayList<Text> textList = new ArrayList<Text>();
		DecimalFormat df = new DecimalFormat("#.##");
		textList.add(Text.translatable("block.space.energy_consumer").append(String.valueOf(df.format(getInput()))).append("kJ/s").formatted(Formatting.LIGHT_PURPLE));
		textList.add(Text.translatable("block.space.pump.description_1"));
		textList.add(Text.translatable("block.space.pump.description_2"));
		textList.add(Text.translatable("block.space.pump.description_3"));
		StarflightItems.hiddenItemTooltip(tooltip, textList);
	}
	
	@Override
	public double getInput()
	{
		return 4.0;
	}
	
	@Override
	public double getEnergyCapacity()
	{
		return 16.0;
	}
	
	@Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random)
	{
        if(state.get(WATER) && state.get(LIT))
        {
        	int count = 2 + random.nextInt(4);
        	Vec3d offset = pos.offset(state.get(FACING).getOpposite()).toCenterPos();
        	Vec3d velocity = pos.toCenterPos().subtract(offset).normalize().multiply(0.1);
        	
        	for(int i = 0; i < count; i++)
        	{
        		double x = offset.getX() + (random.nextDouble() - random.nextDouble()) * 0.5;
        		double y = offset.getY() + (random.nextDouble() - random.nextDouble()) * 0.5;
        		double z = offset.getZ() + (random.nextDouble() - random.nextDouble()) * 0.5;
        		world.addParticle(ParticleTypes.BUBBLE, x, y, z, velocity.getX(), velocity.getY(), velocity.getZ());
        	}
        }
    }
	
	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack)
	{
		if(world.isClient)
			return;
	
		BlockSearch.energyConnectionSearch(world, pos);
		updateWaterState(world, state, pos);
	}
	
	@Override
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved)
	{
		if(world.isClient() || state.isOf(newState.getBlock()))
			return;
		
		world.removeBlockEntity(pos);
		BlockSearch.energyConnectionSearch(world, pos);
	}
	
	@Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify)
    {
		if(!world.isClient)
			updateWaterState(world, state, pos);
    }
	
	@Override
	public BlockState getPlacementState(ItemPlacementContext context)
	{
		return (BlockState) this.getDefaultState().with(FACING, context.getPlayerLookDirection());
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
	public boolean isOutput(World world, BlockPos pos, BlockState state, Direction direction)
	{
		return false;
	}

	@Override
	public boolean isInput(World world, BlockPos pos, BlockState state, Direction direction)
	{
		return direction != (Direction) state.get(FACING) && direction != (Direction) state.get(FACING).getOpposite();
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
	
	public void updateWaterState(World world, BlockState state, BlockPos pos)
	{
		Deque<BlockPos> stack = new ArrayDeque<BlockPos>();
		Set<BlockPos> set = new HashSet<BlockPos>();
		stack.push(pos.offset(state.get(FACING)));
		
		while(stack.size() > 0)
		{
			if(set.size() >= 1024)
			{
				world.setBlockState(pos, state.with(WATER, true));
				return;
			}
			
			BlockPos blockPos = stack.pop();
			
			if(set.contains(blockPos))
				continue;
			
			if(world.getFluidState(blockPos).isOf(Fluids.WATER))
			{
				set.add(blockPos);
				
				for(Direction direction : Direction.values())
				{
					BlockPos offset = blockPos.offset(direction);
					stack.push(offset);
				}
			}
		}
		
		world.setBlockState(pos, state.with(WATER, false));
	}
}