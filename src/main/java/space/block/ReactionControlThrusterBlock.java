package space.block;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.FacingBlock;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Direction;

public class ReactionControlThrusterBlock extends FacingBlock
{
	public static final MapCodec<ReactionControlThrusterBlock> CODEC = ReactionControlThrusterBlock.createCodec(ReactionControlThrusterBlock::new);
	
	public static final List<Pair<Vector3f, Vector3f>> DIAGONAL = List.of(new Pair<Vector3f, Vector3f>(new Vector3f(0.4f, 0.5f, 0.0f), new Vector3f(0.866f, 0.5f, 0.0f)),
			   															  new Pair<Vector3f, Vector3f>(new Vector3f(-0.4f, 0.5f, 0.0f), new Vector3f(-0.866f, 0.5f, 0.0f)),
			   															  new Pair<Vector3f, Vector3f>(new Vector3f(0.0f, 0.5f, 0.4f), new Vector3f(0.0f, 0.5f, 0.866f)),
			   															  new Pair<Vector3f, Vector3f>(new Vector3f(0.0f, 0.5f, -0.4f), new Vector3f(0.0f, 0.5f, -0.866f)));
	
	List<Pair<Vector3f, Vector3f>> thrusters = new ArrayList<Pair<Vector3f, Vector3f>>();
	
	public ReactionControlThrusterBlock(Settings settings, List<Pair<Vector3f, Vector3f>> thrusters)
	{
		super(settings);
		this.setDefaultState((BlockState) ((BlockState) ((BlockState) this.stateManager.getDefaultState()).with(FACING, Direction.UP)));
		this.thrusters.addAll(thrusters);
	}
	
	public ReactionControlThrusterBlock(Settings settings)
	{
		this(settings, null);
	}
	
	@Override
	public MapCodec<? extends ReactionControlThrusterBlock> getCodec()
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
	
	public List<Pair<Vector3f, Vector3f>> getThrusters()
	{
		return thrusters;
	}
}