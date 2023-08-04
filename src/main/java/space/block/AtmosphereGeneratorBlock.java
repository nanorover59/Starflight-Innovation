package space.block;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import space.block.entity.AtmosphereGeneratorBlockEntity;
import space.energy.EnergyNet;
import space.util.AirUtil;
import space.util.StarflightEffects;

public class AtmosphereGeneratorBlock extends BlockWithEntity implements FluidUtilityBlock, EnergyBlock
{
	public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
	public static final BooleanProperty LIT = Properties.LIT;
	
	public AtmosphereGeneratorBlock(Settings settings)
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
	public BlockEntity createBlockEntity(BlockPos blockPos, BlockState blockState)
	{
		return new AtmosphereGeneratorBlockEntity(blockPos, blockState);
	}
	
	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx)
	{
		return (BlockState) this.getDefaultState().with(FACING, ctx.getPlayerFacing().getOpposite());
	}
	
	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack)
	{
		if(!world.isClient())
			addNode(world, pos);
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
	public String getFluidName()
	{
		return "oxygen";
	}

	@Override
	public boolean canPipeConnectToSide(WorldAccess world, BlockPos pos, BlockState state, Direction direction)
	{
		return direction != (Direction) state.get(FACING).getOpposite();
	}
	
	@Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify)
    {
		if(world.isClient)
			return;
		
		BlockPos frontPos = pos.offset(state.get(FACING));
		BlockState frontState = world.getBlockState(frontPos);
		
		if(world.isReceivingRedstonePower(pos) && !state.get(LIT) && !fromPos.equals(frontPos) && frontState.getBlock() == Blocks.AIR)
		{
			//long time = System.currentTimeMillis();
			
			ArrayList<BlockPos> checkList = new ArrayList<BlockPos>();
			ArrayList<BlockPos> foundList = new ArrayList<BlockPos>();
			ArrayList<BlockPos> updateList = new ArrayList<BlockPos>();
			double supply = AirUtil.searchSupply(world, pos, checkList, AirUtil.MAX_VOLUME, StarflightBlocks.ATMOSPHERE_GENERATOR);
			
			//System.out.println("searchSupply: " + (System.currentTimeMillis() - time));
			//time = System.currentTimeMillis();
			
			boolean tooLarge = !AirUtil.findVolume(world, frontPos, foundList, updateList, AirUtil.MAX_VOLUME);
			double required = foundList.size() * HabitableAirBlock.DENSITY;
			
			//System.out.println("findVolume: " + (System.currentTimeMillis() - time));
			//time = System.currentTimeMillis();
			
			if(tooLarge || required > supply)
			{
				MutableText text = Text.translatable("block.space.atmosphere_generator.error_" + (tooLarge ? "volume" : "supply"));
				
				for(PlayerEntity player : world.getPlayers())
				{
		            if(player.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ()) < 1024.0)
		            	player.sendMessage(text, true);
		        }
			}
			else
			{
				AirUtil.useSupply(world, checkList, required);
				
				//System.out.println("useSupply: " + (System.currentTimeMillis() - time));
				//time = System.currentTimeMillis();
				
				AirUtil.fillVolume(world, foundList, updateList);
				
				//System.out.println("fillVolume: " + foundList.size() + " " + (System.currentTimeMillis() - time));
				//time = System.currentTimeMillis();
				
				world.setBlockState(pos, (BlockState) state.with(AtmosphereGeneratorBlock.LIT, true), Block.NOTIFY_ALL);
				StarflightEffects.sendOutgas(world, pos, frontPos, true);
			}
		}
		else if(frontState.getBlock() == StarflightBlocks.HABITABLE_AIR)
			world.setBlockState(pos, (BlockState) state.with(AtmosphereGeneratorBlock.LIT, true), Block.NOTIFY_ALL);
		else if(frontState.getBlock() == Blocks.AIR)
			world.setBlockState(pos, (BlockState) state.with(AtmosphereGeneratorBlock.LIT, false), Block.NOTIFY_ALL);
    }

	@Override
	public double getPowerOutput(World world, BlockPos pos, BlockState state)
	{
		return 0;
	}

	@Override
	public double getPowerDraw(World world, BlockPos pos, BlockState state)
	{
		return state.get(LIT) ? 5.0 : 0.0;
	}

	@Override
	public boolean isSideInput(WorldAccess world, BlockPos pos, BlockState state, Direction direction)
	{
		return direction != (Direction) state.get(FACING);
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
}