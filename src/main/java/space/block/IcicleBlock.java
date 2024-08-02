/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package space.block;

import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.MapCodec;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LandingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.enums.Thickness;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.WorldView;

public class IcicleBlock extends Block implements LandingBlock, Waterloggable
{
	public static final MapCodec<IcicleBlock> CODEC = IcicleBlock.createCodec(IcicleBlock::new);
	public static final DirectionProperty VERTICAL_DIRECTION = Properties.VERTICAL_DIRECTION;
	public static final EnumProperty<Thickness> THICKNESS = Properties.THICKNESS;
	public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
	private static final VoxelShape TIP_MERGE_SHAPE = Block.createCuboidShape(5.0, 0.0, 5.0, 11.0, 16.0, 11.0);
	private static final VoxelShape UP_TIP_SHAPE = Block.createCuboidShape(5.0, 0.0, 5.0, 11.0, 11.0, 11.0);
	private static final VoxelShape DOWN_TIP_SHAPE = Block.createCuboidShape(5.0, 5.0, 5.0, 11.0, 16.0, 11.0);
	private static final VoxelShape BASE_SHAPE = Block.createCuboidShape(4.0, 0.0, 4.0, 12.0, 16.0, 12.0);
	private static final VoxelShape FRUSTUM_SHAPE = Block.createCuboidShape(3.0, 0.0, 3.0, 13.0, 16.0, 13.0);
	private static final VoxelShape MIDDLE_SHAPE = Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);

	public MapCodec<IcicleBlock> getCodec()
	{
		return CODEC;
	}

	public IcicleBlock(AbstractBlock.Settings settings)
	{
		super(settings);
		this.setDefaultState((BlockState) ((BlockState) ((BlockState) ((BlockState) this.stateManager.getDefaultState()).with(VERTICAL_DIRECTION, Direction.UP)).with(THICKNESS, Thickness.TIP)).with(WATERLOGGED, false));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
	{
		builder.add(VERTICAL_DIRECTION, THICKNESS, WATERLOGGED);
	}

	@Override
	protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos)
	{
		return IcicleBlock.canPlaceAtWithDirection(world, pos, state.get(VERTICAL_DIRECTION));
	}

	@Override
	protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos)
	{
		if(state.get(WATERLOGGED).booleanValue())
		{
			world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
		}
		if(direction != Direction.UP && direction != Direction.DOWN)
		{
			return state;
		}
		Direction direction2 = state.get(VERTICAL_DIRECTION);
		if(direction2 == Direction.DOWN && world.getBlockTickScheduler().isQueued(pos, this))
		{
			return state;
		}
		if(direction == direction2.getOpposite() && !this.canPlaceAt(state, world, pos))
		{
			if(direction2 == Direction.DOWN)
			{
				world.scheduleBlockTick(pos, this, 2);
			} else
			{
				world.scheduleBlockTick(pos, this, 1);
			}
			return state;
		}
		boolean bl = state.get(THICKNESS) == Thickness.TIP_MERGE;
		Thickness thickness = IcicleBlock.getThickness(world, pos, direction2, bl);
		return (BlockState) state.with(THICKNESS, thickness);
	}

	@Override
	protected void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile)
	{
		if(world.isClient)
			return;
		
		BlockPos blockPos = hit.getBlockPos();
		
		if(projectile.canModifyAt(world, blockPos) && projectile.canBreakBlocks(world) && projectile instanceof TridentEntity && projectile.getVelocity().length() > 0.6)
			world.breakBlock(blockPos, true);
	}

	@Override
	public void onLandedUpon(World world, BlockState state, BlockPos pos, Entity entity, float fallDistance)
	{
		if(state.get(VERTICAL_DIRECTION) == Direction.UP && state.get(THICKNESS) == Thickness.TIP)
			entity.handleFallDamage(fallDistance + 2.0f, 2.0f, world.getDamageSources().stalagmite());
		else
			super.onLandedUpon(world, state, pos, entity, fallDistance);
	}

	@Override
	protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random)
	{
		if(IcicleBlock.isPointingUp(state) && !this.canPlaceAt(state, world, pos))
			world.breakBlock(pos, true);
		else
			IcicleBlock.spawnFallingBlock(state, world, pos);
	}

	@Override
	@Nullable
	public BlockState getPlacementState(ItemPlacementContext context)
	{
		BlockPos blockPos;
		World worldAccess = context.getWorld();
		Direction direction2 = IcicleBlock.getDirectionToPlaceAt(worldAccess, blockPos = context.getBlockPos(), context.getVerticalPlayerLookDirection().getOpposite());
		
		if(direction2 == null)
			return null;
		
		boolean bl = !context.shouldCancelInteraction();
		Thickness thickness = IcicleBlock.getThickness(worldAccess, blockPos, direction2, bl);
		
		if(thickness == null)
			return null;
		
		return (BlockState) ((BlockState) ((BlockState) this.getDefaultState().with(VERTICAL_DIRECTION, direction2)).with(THICKNESS, thickness)).with(WATERLOGGED, worldAccess.getFluidState(blockPos).getFluid() == Fluids.WATER);
	}

	@Override
	protected FluidState getFluidState(BlockState state)
	{
		if(state.get(WATERLOGGED).booleanValue())
		{
			return Fluids.WATER.getStill(false);
		}
		return super.getFluidState(state);
	}

	@Override
	protected VoxelShape getCullingShape(BlockState state, BlockView world, BlockPos pos)
	{
		return VoxelShapes.empty();
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context)
	{
		Thickness thickness = state.get(THICKNESS);
		VoxelShape voxelShape = thickness == Thickness.TIP_MERGE ? TIP_MERGE_SHAPE : (thickness == Thickness.TIP ? (state.get(VERTICAL_DIRECTION) == Direction.DOWN ? DOWN_TIP_SHAPE : UP_TIP_SHAPE) : (thickness == Thickness.FRUSTUM ? BASE_SHAPE : (thickness == Thickness.MIDDLE ? FRUSTUM_SHAPE : MIDDLE_SHAPE)));
		Vec3d vec3d = state.getModelOffset(world, pos);
		return voxelShape.offset(vec3d.x, 0.0, vec3d.z);
	}

	@Override
	protected boolean isShapeFullCube(BlockState state, BlockView world, BlockPos pos)
	{
		return false;
	}

	@Override
	protected float getMaxHorizontalModelOffset()
	{
		return 0.125f;
	}

	@Override
	public void onDestroyedOnLanding(World world, BlockPos pos, FallingBlockEntity fallingBlockEntity)
	{
		if(!fallingBlockEntity.isSilent())
			world.syncWorldEvent(WorldEvents.POINTED_DRIPSTONE_LANDS, pos, 0);
	}

	@Override
	public DamageSource getDamageSource(Entity attacker)
	{
		return attacker.getDamageSources().fallingStalactite(attacker);
	}

	private static void spawnFallingBlock(BlockState state, ServerWorld world, BlockPos pos)
	{
		BlockPos.Mutable mutable = pos.mutableCopy();
		BlockState blockState = state;
		
		while(IcicleBlock.isPointingDown(blockState))
		{
			FallingBlockEntity fallingBlockEntity = FallingBlockEntity.spawnFromBlock(world, mutable, blockState);
			
			if(IcicleBlock.isTip(blockState, true))
			{
				int i = Math.max(1 + pos.getY() - mutable.getY(), 6);
				float f = 1.0f * (float) i;
				fallingBlockEntity.setHurtEntities(f, 40);
				break;
			}
			
			mutable.move(Direction.DOWN);
			blockState = world.getBlockState(mutable);
		}
	}

	@VisibleForTesting
	public static void tryGrow(BlockState state, ServerWorld world, BlockPos pos, Random random)
	{
		BlockPos blockPos = IcicleBlock.getTipPos(state, world, pos, 7, false);
		
		if(blockPos == null)
			return;
		
		BlockState blockState3 = world.getBlockState(blockPos);
		
		if(!IcicleBlock.canDrip(blockState3) || !IcicleBlock.canGrow(blockState3, world, blockPos))
			return;
		
		if(random.nextBoolean())
			IcicleBlock.tryGrow(world, blockPos, Direction.DOWN);
		else
			IcicleBlock.tryGrowStalagmite(world, blockPos);
	}

	private static void tryGrowStalagmite(ServerWorld world, BlockPos pos)
	{
		BlockPos.Mutable mutable = pos.mutableCopy();
		
		for(int i = 0; i < 10; i++)
		{
			mutable.move(Direction.DOWN);
			BlockState blockState = world.getBlockState(mutable);
			
			if(!blockState.getFluidState().isEmpty())
				return;
			
			if(IcicleBlock.isTip(blockState, Direction.UP) && IcicleBlock.canGrow(blockState, world, mutable))
			{
				IcicleBlock.tryGrow(world, mutable, Direction.UP);
				return;
			}
			
			if(IcicleBlock.canPlaceAtWithDirection(world, mutable, Direction.UP) && !world.isWater((BlockPos) mutable.down()))
			{
				IcicleBlock.tryGrow(world, (BlockPos) mutable.down(), Direction.UP);
				return;
			}
		}
	}

	private static void tryGrow(ServerWorld world, BlockPos pos, Direction direction)
	{
		BlockPos blockPos = pos.offset(direction);
		BlockState blockState = world.getBlockState(blockPos);
		
		if(IcicleBlock.isTip(blockState, direction.getOpposite()))
			IcicleBlock.growMerged(blockState, world, blockPos);
		else if(blockState.isAir())
			IcicleBlock.place(world, blockPos, direction, Thickness.TIP);
	}

	private static void place(WorldAccess world, BlockPos pos, Direction direction, Thickness thickness)
	{
		BlockState blockState = (BlockState) ((BlockState) ((BlockState) StarflightBlocks.ICICLE.getDefaultState().with(VERTICAL_DIRECTION, direction)).with(THICKNESS, thickness)).with(WATERLOGGED, world.getFluidState(pos).getFluid() == Fluids.WATER);
		world.setBlockState(pos, blockState, Block.NOTIFY_ALL);
	}

	private static void growMerged(BlockState state, WorldAccess world, BlockPos pos)
	{
		BlockPos blockPos2;
		BlockPos blockPos;
		
		if(state.get(VERTICAL_DIRECTION) == Direction.UP)
		{
			blockPos = pos;
			blockPos2 = pos.up();
		}
		else
		{
			blockPos2 = pos;
			blockPos = pos.down();
		}
		
		IcicleBlock.place(world, blockPos2, Direction.DOWN, Thickness.TIP_MERGE);
		IcicleBlock.place(world, blockPos, Direction.UP, Thickness.TIP_MERGE);
	}

	@Nullable
	private static BlockPos getTipPos(BlockState state, WorldAccess world, BlockPos pos, int range, boolean allowMerged)
	{
		if(IcicleBlock.isTip(state, allowMerged))
			return pos;
		
		Direction direction = state.get(VERTICAL_DIRECTION);
		BiPredicate<BlockPos, BlockState> biPredicate = (posx, statex) -> statex.isOf(StarflightBlocks.ICICLE) && statex.get(VERTICAL_DIRECTION) == direction;
		return IcicleBlock.searchInDirection(world, pos, direction.getDirection(), biPredicate, statex -> IcicleBlock.isTip(statex, allowMerged), range).orElse(null);
	}

	@Nullable
	private static Direction getDirectionToPlaceAt(WorldView world, BlockPos pos, Direction direction)
	{
		Direction direction2;
		if(IcicleBlock.canPlaceAtWithDirection(world, pos, direction))
		{
			direction2 = direction;
		} else if(IcicleBlock.canPlaceAtWithDirection(world, pos, direction.getOpposite()))
		{
			direction2 = direction.getOpposite();
		} else
		{
			return null;
		}
		return direction2;
	}

	private static Thickness getThickness(WorldView world, BlockPos pos, Direction direction, boolean tryMerge)
	{
		Direction direction2 = direction.getOpposite();
		BlockState blockState = world.getBlockState(pos.offset(direction));
		if(IcicleBlock.isPointedDripstoneFacingDirection(blockState, direction2))
		{
			if(tryMerge || blockState.get(THICKNESS) == Thickness.TIP_MERGE)
			{
				return Thickness.TIP_MERGE;
			}
			return Thickness.TIP;
		}
		if(!IcicleBlock.isPointedDripstoneFacingDirection(blockState, direction))
		{
			return Thickness.TIP;
		}
		Thickness thickness = blockState.get(THICKNESS);
		if(thickness == Thickness.TIP || thickness == Thickness.TIP_MERGE)
		{
			return Thickness.FRUSTUM;
		}
		BlockState blockState2 = world.getBlockState(pos.offset(direction2));
		if(!IcicleBlock.isPointedDripstoneFacingDirection(blockState2, direction))
		{
			return Thickness.BASE;
		}
		return Thickness.MIDDLE;
	}

	public static boolean canDrip(BlockState state)
	{
		return IcicleBlock.isPointingDown(state) && state.get(THICKNESS) == Thickness.TIP && state.get(WATERLOGGED) == false;
	}

	private static boolean canGrow(BlockState state, ServerWorld world, BlockPos pos)
	{
		Direction direction = state.get(VERTICAL_DIRECTION);
		BlockPos blockPos = pos.offset(direction);
		BlockState blockState = world.getBlockState(blockPos);
		
		if(!blockState.getFluidState().isEmpty())
			return false;
		
		if(blockState.isAir())	
			return true;
		
		return IcicleBlock.isTip(blockState, direction.getOpposite());
	}

	private static boolean canPlaceAtWithDirection(WorldView world, BlockPos pos, Direction direction)
	{
		BlockPos blockPos = pos.offset(direction.getOpposite());
		BlockState blockState = world.getBlockState(blockPos);
		return blockState.isSideSolidFullSquare(world, blockPos, direction) || IcicleBlock.isPointedDripstoneFacingDirection(blockState, direction);
	}

	private static boolean isTip(BlockState state, boolean allowMerged)
	{
		if(!state.isOf(StarflightBlocks.ICICLE))
			return false;
		
		Thickness thickness = state.get(THICKNESS);
		return thickness == Thickness.TIP || allowMerged && thickness == Thickness.TIP_MERGE;
	}

	private static boolean isTip(BlockState state, Direction direction)
	{
		return IcicleBlock.isTip(state, false) && state.get(VERTICAL_DIRECTION) == direction;
	}

	private static boolean isPointingDown(BlockState state)
	{
		return IcicleBlock.isPointedDripstoneFacingDirection(state, Direction.DOWN);
	}

	private static boolean isPointingUp(BlockState state)
	{
		return IcicleBlock.isPointedDripstoneFacingDirection(state, Direction.UP);
	}

	@Override
	protected boolean canPathfindThrough(BlockState state, NavigationType type)
	{
		return false;
	}

	private static boolean isPointedDripstoneFacingDirection(BlockState state, Direction direction)
	{
		return state.isOf(StarflightBlocks.ICICLE) && state.get(VERTICAL_DIRECTION) == direction;
	}
	
	private static Optional<BlockPos> searchInDirection(WorldAccess world, BlockPos pos, Direction.AxisDirection direction, BiPredicate<BlockPos, BlockState> continuePredicate, Predicate<BlockState> stopPredicate, int range)
	{
		Direction direction2 = Direction.get(direction, Direction.Axis.Y);
		BlockPos.Mutable mutable = pos.mutableCopy();
		
		for(int i = 1; i < range; ++i)
		{
			mutable.move(direction2);
			BlockState blockState = world.getBlockState(mutable);
			
			if(stopPredicate.test(blockState))
				return Optional.of(mutable.toImmutable());
			
			if(!world.isOutOfHeightLimit(mutable.getY()) && continuePredicate.test(mutable, blockState))
				continue;
			
			return Optional.empty();
		}
		
		return Optional.empty();
	}
}
