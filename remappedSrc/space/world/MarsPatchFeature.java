package space.world;

import java.util.function.Predicate;

import com.mojang.serialization.Codec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.VegetationPatchFeatureConfig;
import net.minecraft.world.gen.feature.WaterloggedVegetationPatchFeature;

public class MarsPatchFeature extends WaterloggedVegetationPatchFeature
{
	public MarsPatchFeature(Codec<VegetationPatchFeatureConfig> codec)
	{
        super(codec);
    }
	
	@Override
	protected boolean placeGround(StructureWorldAccess world, VegetationPatchFeatureConfig config, Predicate<BlockState> replaceable, Random random, BlockPos.Mutable pos, int depth)
	{
		for(int i = 0; i < depth; i++)
		{
			BlockState blockState = i > 0 ? config.groundState.getBlockState(random, pos) : Blocks.PODZOL.getDefaultState();
			
			if(!replaceable.test(world.getBlockState(pos)))
				return i != 0;
			
			world.setBlockState(pos, blockState, Block.NOTIFY_LISTENERS);
			pos.move(config.surface.getDirection());
		}
		
		return true;
	}
}