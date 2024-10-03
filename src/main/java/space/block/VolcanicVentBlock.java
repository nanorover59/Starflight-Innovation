package space.block;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import space.block.entity.VolcanicVentBlockEntity;

public class VolcanicVentBlock extends BlockWithEntity
{
	public static final MapCodec<VolcanicVentBlock> CODEC = VolcanicVentBlock.createCodec(VolcanicVentBlock::new);
	
	public VolcanicVentBlock(Settings settings)
	{
		super(settings);
	}
	
	@Override
	public MapCodec<? extends VolcanicVentBlock> getCodec()
	{
		return CODEC;
	}
	
	@Override
	public BlockRenderType getRenderType(BlockState state)
	{
		return BlockRenderType.MODEL;
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state)
	{
		return new VolcanicVentBlockEntity(pos, state);
	}

	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type)
	{
		return world.isClient ? null : validateTicker(type, StarflightBlocks.VOLCANIC_VENT_BLOCK_ENTITY, VolcanicVentBlockEntity::serverTick);
	}
}