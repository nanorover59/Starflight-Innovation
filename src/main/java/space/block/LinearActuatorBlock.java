package space.block;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.BiPredicate;

import org.joml.Vector3f;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.PillarBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import space.entity.LinearPlatformEntity;
import space.entity.MovingCraftEntity;
import space.util.BlockSearch;

public class LinearActuatorBlock extends SimpleFacingBlock
{
	public LinearActuatorBlock(Settings settings)
	{
		super(settings);
	}
	
	@Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify)
    {
		if(world.isClient || !world.isReceivingRedstonePower(pos))
			return;
		
		Direction redstoneDirection = Direction.fromVector(pos.getX() - sourcePos.getX(), pos.getY() - sourcePos.getY(), pos.getZ() - sourcePos.getZ());
		tryMotion(state, world, pos, redstoneDirection);
    }
	
	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit)
	{
		OptionalInt optionalInt = getDirectionButton(hit, state);
		
		if(optionalInt.isEmpty())
			return ActionResult.PASS;
		
		Direction facing = state.get(FACING);
		Direction direction = (facing == Direction.UP || facing == Direction.DOWN) ? Direction.SOUTH : Direction.UP;
		Axis axis = facing.getAxis();
		
		if(facing == Direction.UP || facing == Direction.DOWN || facing == Direction.EAST || facing == Direction.SOUTH)
		{
			for(int i = 0; i < optionalInt.getAsInt(); i++)
				direction = direction.rotateClockwise(axis);
		}
		else
		{
			for(int i = 0; i < optionalInt.getAsInt(); i++)
				direction = direction.rotateCounterclockwise(axis);
		}
		
		tryMotion(state, world, pos, direction);
		return ActionResult.SUCCESS;
	}
	
	private OptionalInt getDirectionButton(BlockHitResult hit, BlockState state)
	{
		return (OptionalInt) getHitPos(hit, state.get(FACING)).map(hitPos -> {
			float angle = (float) MathHelper.atan2(hitPos.x - 0.5, hitPos.y - 0.5) * MathHelper.DEGREES_PER_RADIAN;
			int button = MathHelper.floor(angle / 90.0 + 0.5) & 3;
			return OptionalInt.of(button);
		}).orElseGet(OptionalInt::empty);
	}

	private Optional<Vec2f> getHitPos(BlockHitResult hit, Direction facing)
	{
		Direction direction = hit.getSide();
		
		if(facing != direction)
			return Optional.empty();
		else
		{
			BlockPos blockPos = hit.getBlockPos().offset(direction);
			Vec3d vec3d = hit.getPos().subtract((double) blockPos.getX(), (double) blockPos.getY(), (double) blockPos.getZ());
			double x = vec3d.getX();
			double y = vec3d.getY();
			double z = vec3d.getZ();

			return switch(direction)
			{
				case NORTH -> Optional.of(new Vec2f((float) (1.0 - x), (float) y));
				case SOUTH -> Optional.of(new Vec2f((float) x, (float) y));
				case WEST -> Optional.of(new Vec2f((float) z, (float) y));
				case EAST -> Optional.of(new Vec2f((float) (1.0 - z), (float) y));
				case DOWN -> Optional.of(new Vec2f((float) (1.0 - x), (float) z));
				case UP -> Optional.of(new Vec2f((float) (1.0 - x), (float) z));
			};
		}
	}
	
	public static void tryMotion(BlockState state, World world, BlockPos pos, Direction movementDirection)
	{
		Direction direction = state.get(FACING);
		BlockPos offsetPos = pos.offset(direction.getOpposite());
		BlockState offsetState = world.getBlockState(offsetPos);
		
		if(offsetState.getBlock() == StarflightBlocks.LINEAR_TRACK || offsetState.getBlock() == StarflightBlocks.CALL_TRACK)
		{
			if(offsetState.getBlock() == StarflightBlocks.LINEAR_TRACK && !offsetState.get(PillarBlock.AXIS).test(movementDirection))
				return;
			
			Mutable mutable = offsetPos.mutableCopy();
			int distance = 0;
			boolean junctionFlag = false;

			while((world.getBlockState(mutable).getBlock() == StarflightBlocks.LINEAR_TRACK && world.getBlockState(mutable).get(PillarBlock.AXIS).test(movementDirection)) || world.getBlockState(mutable).getBlock() == StarflightBlocks.CALL_TRACK)
			{
				if(!mutable.equals(offsetPos) && world.getBlockState(mutable).getBlock() == StarflightBlocks.CALL_TRACK)
					junctionFlag = true;
				
				if(!junctionFlag)
					distance++;
				
				mutable.move(movementDirection);
			}
			
			mutable = offsetPos.mutableCopy();
			
			while((world.getBlockState(mutable).getBlock() == StarflightBlocks.LINEAR_TRACK && world.getBlockState(mutable).get(PillarBlock.AXIS).test(movementDirection)) || world.getBlockState(mutable).getBlock() == StarflightBlocks.CALL_TRACK)
				mutable.move(movementDirection.getOpposite());
			
			if(!junctionFlag)
				distance--;
			
			if(distance > 0)
				spawnEntity(world, pos, offsetPos, pos.offset(movementDirection, distance));
		}
	}
	
	public static void spawnEntity(World world, BlockPos pos, BlockPos trackPos, BlockPos targetPos)
	{
		// Exclude blocks that are part of the track.
		BiPredicate<World, BlockPos> include = (w, p) -> {
			BlockState blockState = w.getBlockState(p);
			return blockState.getBlock() == StarflightBlocks.LINEAR_TRACK || blockState.getBlock() == StarflightBlocks.CALL_TRACK;
		};
		
		ArrayList<BlockPos> positionList = new ArrayList<BlockPos>();
		BlockSearch.search(world, trackPos, positionList, include, BlockSearch.MAX_VOLUME, false);
		Set<BlockPos> set = new HashSet<BlockPos>();
		set.addAll(positionList);
		
		// Detect blocks to be included in the craft construction.
		positionList = new ArrayList<BlockPos>();
		Direction direction = Direction.fromVector(targetPos.getX() - pos.getX(), targetPos.getY() - pos.getY(), targetPos.getZ() - pos.getZ());
		BlockSearch.movingCraftSearch(world, pos, positionList, set, direction, BlockSearch.MAX_VOLUME, BlockSearch.MAX_DISTANCE);
		
		if(positionList.isEmpty())
			return;
		
		ArrayList<MovingCraftEntity.BlockData> blockDataList = MovingCraftEntity.captureBlocks(world, new BlockPos(MathHelper.floor(pos.getX()), MathHelper.floor(pos.getY()), MathHelper.floor(pos.getZ())), positionList);
		LinearPlatformEntity entity = new LinearPlatformEntity(world, pos, blockDataList, 0.0, 0.0, new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.0f, 0.0f, 0.0f), targetPos);
		MovingCraftEntity.removeBlocksFromWorld(world, pos, blockDataList);
		world.spawnEntity(entity);
	}
}