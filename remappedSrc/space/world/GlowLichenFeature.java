package space.world;

import java.util.Arrays;
import java.util.Objects;

import com.mojang.serialization.Codec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.GlowLichenBlock;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class GlowLichenFeature extends Feature<DefaultFeatureConfig>
{
	public GlowLichenFeature(Codec<DefaultFeatureConfig> codec)
	{
		super(codec);
	}

	@Override
	public boolean generate(FeatureContext<DefaultFeatureConfig> context)
	{
		StructureWorldAccess world = context.getWorld();
		Random random = context.getRandom();
		BlockPos blockPos = context.getOrigin();
		BlockPos.Mutable mutable = new BlockPos.Mutable();
		int i = 5;
		int j = 5;
		int k = 5;
		
		for(int l = 0; l < 8; l++)
		{
			mutable.set(blockPos, random.nextInt(j) - random.nextInt(j), random.nextInt(k) - random.nextInt(k), random.nextInt(j) - random.nextInt(j));
			FluidState fluidState = world.getFluidState(mutable);
			
			if(fluidState.getFluid() == Fluids.WATER || (world.getBiome(mutable).isIn(StarflightWorldGeneration.LIQUID_WATER) && !world.getBlockState(mutable).getMaterial().blocksMovement()))
			{
				BlockState blockState = Arrays.stream(Direction.values()).map(direction -> ((GlowLichenBlock) Blocks.GLOW_LICHEN).withDirection(Blocks.GLOW_LICHEN.getDefaultState(), world, mutable, (Direction) direction)).filter(Objects::nonNull).findFirst().orElse(null);
				
				if(blockState != null)
				{
					if(fluidState.getFluid() == Fluids.WATER)
						blockState.with(Properties.WATERLOGGED, true);
					
					world.setBlockState(mutable, blockState, Block.NOTIFY_LISTENERS);
					i++;
				}
			}
		}
		
		return i > 0;
	}
}