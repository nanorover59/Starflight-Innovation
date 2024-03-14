package space.world;

import com.mojang.serialization.Codec;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class FreezeWaterFeature extends Feature<DefaultFeatureConfig>
{
	public FreezeWaterFeature(Codec<DefaultFeatureConfig> codec)
	{
		super(codec);
	}

	@Override
	public boolean generate(FeatureContext<DefaultFeatureConfig> context)
	{
		StructureWorldAccess structureWorldAccess = context.getWorld();
		BlockPos origin = context.getOrigin();
		BlockPos.Mutable mutable = new BlockPos.Mutable();
		
		for(int i = 0; i < 16; i++)
		{
			for(int j = 0; j < 16; j++)
			{
				int x = origin.getX() + i;
				int z = origin.getZ() + j;
				int topY = structureWorldAccess.getTopY(Heightmap.Type.OCEAN_FLOOR_WG, x, z);
				
				for(int y = topY; y > context.getWorld().getBottomY(); y--)
				{
					mutable.set(x, y, z);
					
					if(structureWorldAccess.getFluidState(mutable).getFluid() == Fluids.WATER && structureWorldAccess.getBlockState(mutable.up()).isAir())
					{
						if(!structureWorldAccess.getBiome(mutable).isIn(StarflightWorldGeneration.LIQUID_WATER))
							structureWorldAccess.setBlockState(mutable, Blocks.ICE.getDefaultState(), Block.NOTIFY_LISTENERS);
					}
				}
			}
		}
		
		return true;
	}
}