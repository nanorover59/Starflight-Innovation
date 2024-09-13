package space.block;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.FacingBlock;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.Direction;

public class SimpleFacingBlock extends FacingBlock
{
	public static final MapCodec<SimpleFacingBlock> CODEC = SimpleFacingBlock.createCodec(SimpleFacingBlock::new);
	
	public SimpleFacingBlock(Settings settings)
	{
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.UP));
	}
	
	@Override
	public MapCodec<? extends SimpleFacingBlock> getCodec()
	{
		return CODEC;
	}
	
	@Override
	public BlockRenderType getRenderType(BlockState state)
	{
		return BlockRenderType.MODEL;
	}
	
	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
	{
		builder.add(FACING);
	}
	
	@Override
	public BlockState rotate(BlockState state, BlockRotation rotation)
	{
		if(rotation != null)
			return (BlockState) state.with(FACING, rotation.rotate(state.get(FACING)));
		else
			return state;
	}

	@Override
	public BlockState mirror(BlockState state, BlockMirror mirror)
	{
		if(mirror != null)
			return state.rotate(mirror.getRotation(state.get(FACING)));
		else
			return state;
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext context)
	{
		return (BlockState) this.getDefaultState().with(FACING, context.getPlayerLookDirection().getOpposite());
	}
}