package space.block;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.client.item.TooltipContext;
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
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import space.client.StarflightModClient;
import space.util.AirUtil;
import space.util.StarflightEffects;

public class AtmosphereGeneratorBlock extends HorizontalFacingBlock implements FluidUtilityBlock
{
	public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
	public static final BooleanProperty LIT = Properties.LIT;
	
	public AtmosphereGeneratorBlock(Settings settings)
	{
		super(settings);
		this.setDefaultState(this.getStateManager().getDefaultState().with(FACING, Direction.NORTH).with(LIT, false));
	}
	
	@Override
    public void appendTooltip(ItemStack stack, @Nullable BlockView world, List<Text> tooltip, TooltipContext context)
	{
		StarflightModClient.hiddenItemTooltip(tooltip, Text.translatable("block.space.atmosphere_generator.description_1"), Text.translatable("block.space.atmosphere_generator.description_2"));
	}
	
	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager)
	{
		stateManager.add(FACING);
		stateManager.add(LIT);
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
			ArrayList<BlockPos> checkList = new ArrayList<BlockPos>();
			ArrayList<BlockPos> foundList = new ArrayList<BlockPos>();
			double supply = AirUtil.searchSupply(world, pos, checkList, AirUtil.MAX_VOLUME, StarflightBlocks.ATMOSPHERE_GENERATOR);
			AirUtil.findVolume(world, frontPos, foundList, AirUtil.MAX_VOLUME);
			double required = foundList.size() * HabitableAirBlock.DENSITY;
			
			if(required == 0 || required > supply)
			{
				PlayerEntity player = world.getClosestPlayer(pos.getX(), pos.getY(), pos.getZ(), 32.0, false);
				MutableText text = Text.translatable("block.space.atmosphere_generator.error");
				player.sendMessage(text, true);
			}
			else
			{
				AirUtil.useSupply(world, checkList, required);
				AirUtil.fillVolume(world, foundList);
				world.setBlockState(pos, (BlockState) state.with(AtmosphereGeneratorBlock.LIT, true), Block.NOTIFY_ALL);
				StarflightEffects.sendOutgas(world, pos, frontPos, true);
			}
		}
		else if(frontState.getBlock() == Blocks.AIR)
			world.setBlockState(pos, (BlockState) state.with(AtmosphereGeneratorBlock.LIT, false), Block.NOTIFY_ALL);
    }
}