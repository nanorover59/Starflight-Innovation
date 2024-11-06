package space.world;

import com.mojang.serialization.Codec;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.Direction;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;
import space.block.StarflightBlocks;

public class VolcanicVentFeature extends Feature<DefaultFeatureConfig>
{
	public VolcanicVentFeature(Codec<DefaultFeatureConfig> codec)
	{
		super(codec);
	}

	@Override
	public boolean generate(FeatureContext<DefaultFeatureConfig> context)
	{
		StructureWorldAccess world = context.getWorld();
		Mutable mutable = context.getOrigin().mutableCopy();
		
		while(world.getBlockState(mutable).getBlock() != Blocks.MAGMA_BLOCK && mutable.getY() > world.getBottomY())
			mutable.move(Direction.DOWN);
		
		if(world.getBlockState(mutable).getBlock() == Blocks.MAGMA_BLOCK)
		{
			world.setBlockState(mutable, StarflightBlocks.VOLCANIC_VENT.getDefaultState(), Block.NOTIFY_LISTENERS);
			return true;
		}
		
		return false;
	}
}