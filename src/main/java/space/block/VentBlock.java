package space.block;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import space.block.entity.VentBlockEntity;
import space.client.StarflightModClient;
import space.particle.StarflightParticleTypes;
import space.util.FluidResourceType;

public class VentBlock extends BlockWithEntity implements FluidUtilityBlock
{
	public static final MapCodec<VentBlock> CODEC = VentBlock.createCodec(VentBlock::new);
	public static final DirectionProperty FACING = Properties.FACING;
	public static final IntProperty VENT_STATE = IntProperty.of("vent_state", 0, 2);

	public VentBlock(Settings settings)
	{
		super(settings);
		this.setDefaultState(this.getStateManager().getDefaultState().with(FACING, Direction.UP).with(VENT_STATE, 0));
	}
	
	@Override
	protected MapCodec<? extends VentBlock> getCodec()
	{
		return CODEC;
	}

	@Override
	public void appendTooltip(ItemStack stack, @Nullable BlockView world, List<Text> tooltip, TooltipContext context)
	{
		StarflightModClient.hiddenItemTooltip(tooltip, Text.translatable("block.space.vent.description_1"), Text.translatable("block.space.vent.description_2"));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager)
	{
		stateManager.add(FACING);
		stateManager.add(VENT_STATE);
	}
	
	@Override
	public BlockRenderType getRenderType(BlockState state)
	{
		return BlockRenderType.MODEL;
	}
	
	@Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random)
	{
        if(state.get(VENT_STATE) > 0)
        {
        	Direction direction = state.get(FACING);
        	BlockPos forwardPos = pos.offset(direction);
        	Vector3f vector = direction.getUnitVector();
        	double x = forwardPos.getX() + 0.5 + random.nextDouble() * 0.5 - random.nextDouble() * 0.5;
        	double y = forwardPos.getY() + 0.5 + random.nextDouble() * 0.5 - random.nextDouble() * 0.5;
        	double z = forwardPos.getZ() + 0.5 + random.nextDouble() * 0.5 - random.nextDouble() * 0.5;
        	double vx = vector.x() * 0.25;
        	double vy = vector.y() * 0.25;
        	double vz = vector.z() * 0.25;
        	
        	if(state.get(VENT_STATE) == 1 && world.getBlockState(forwardPos).isAir())
        		world.addImportantParticle(StarflightParticleTypes.AIR_FILL, x, y, z, vx, vy, vz);
        	else if(state.get(VENT_STATE) == 2 && world.getFluidState(forwardPos).isOf(Fluids.WATER))
        		world.addImportantParticle(ParticleTypes.BUBBLE, x, y, z, -vx, -vy, -vz);
        }
	}
	
	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack)
	{
		if(world.isClient)
			return;
		
		VentBlockEntity blockEntity = (VentBlockEntity) world.getBlockEntity(pos);
		blockEntity.updateWaterState(world, pos);
		blockEntity.findPump(world, pos);
	}
	
	@Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify)
    {
		if(world.isClient)
			return;
		
		VentBlockEntity blockEntity = (VentBlockEntity) world.getBlockEntity(pos);
		blockEntity.updateWaterState(world, pos);
		blockEntity.findPump(world, pos);
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
	public BlockState getPlacementState(ItemPlacementContext ctx)
	{
		return (BlockState) this.getDefaultState().with(FACING, ctx.getPlayerLookDirection().getOpposite());
	}

	@Override
	public FluidResourceType getFluidType()
	{
		return FluidResourceType.ANY;
	}

	@Override
	public boolean canPipeConnectToSide(WorldAccess world, BlockPos pos, BlockState state, Direction direction)
	{
		return true;
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state)
	{
		return new VentBlockEntity(pos, state);
	}
	
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type)
	{
		return world.isClient ? null : validateTicker(type, StarflightBlocks.VENT_BLOCK_ENTITY, VentBlockEntity::serverTick);
	}
}