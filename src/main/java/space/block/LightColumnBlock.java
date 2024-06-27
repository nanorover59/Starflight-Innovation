package space.block;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.PillarBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.World;
import space.block.entity.LightColumnBlockEntity;

public class LightColumnBlock extends BlockWithEntity implements BlockEntityProvider, EnergyBlock
{
	public static final MapCodec<LightColumnBlock> CODEC = LightColumnBlock.createCodec(LightColumnBlock::new);
	public static final EnumProperty<Direction.Axis> AXIS = Properties.AXIS;
	public static final BooleanProperty LIT = Properties.LIT;

	public LightColumnBlock(Settings settings)
	{
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState().with(AXIS, Axis.Y).with(LIT, false));
	}

	@Override
	public MapCodec<? extends LightColumnBlock> getCodec()
	{
		return CODEC;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
	{
		builder.add(AXIS);
		builder.add(LIT);
	}
	
	@Override
	public double getInput()
	{
		return 0.25;
	}
	
	@Override
	public double getEnergyCapacity()
	{
		return 4.0;
	}
	
	@Override
	public BlockRenderType getRenderType(BlockState state)
	{
		return BlockRenderType.MODEL;
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext context)
	{
		return this.getDefaultState().with(AXIS, context.getSide().getAxis());
	}

	@Override
	public BlockState rotate(BlockState state, BlockRotation rotation)
	{
		return PillarBlock.changeRotation(state, rotation);
	}
	
	@Override
	public boolean isOutput(World world, BlockPos pos, BlockState state, Direction direction)
	{
		return false;
	}

	@Override
	public boolean isInput(World world, BlockPos pos, BlockState state, Direction direction)
	{
		return direction.getAxis() == state.get(AXIS);
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state)
	{
		return new LightColumnBlockEntity(pos, state);
	}

	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type)
	{
		return world.isClient ? null : validateTicker(type, StarflightBlocks.LIGHT_COLUMN_BLOCK_ENTITY, LightColumnBlockEntity::serverTick);
	}
}