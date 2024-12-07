package space.world;

import com.mojang.serialization.Codec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
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
		BlockPos blockPos = context.getOrigin();
		BlockPos.Mutable mutable = new BlockPos.Mutable();
		
		for(int x = 0; x < 16; x++)
		{
			for(int z = 0; z < 16; z++)
			{
				mutable.set(blockPos.getX() + x, 128, blockPos.getZ() + z);
				
				while(mutable.getY() > structureWorldAccess.getBottomY())
				{
					if(structureWorldAccess.getFluidState(mutable).isOf(Fluids.WATER) && structureWorldAccess.getFluidState(mutable.up()).isOf(Fluids.EMPTY) && !structureWorldAccess.getBlockState(mutable.up()).blocksMovement() && !structureWorldAccess.getBiome(mutable).isIn(StarflightWorldGeneration.LIQUID_WATER))
					{
						BlockState blockState = Blocks.PACKED_ICE.getDefaultState();
						
						for(int y = 0; y < 6; y++)
						{
							structureWorldAccess.setBlockState(mutable, blockState, Block.NOTIFY_LISTENERS);
							mutable.move(Direction.DOWN);
							
							if(context.getRandom().nextInt(3) > 0)
							{
								if(blockState.isOf(Blocks.PACKED_ICE))
									blockState = Blocks.ICE.getDefaultState();
								else
									break;
							}
								
						}
					}
					
					mutable.move(Direction.DOWN);
				}
			}
		}
		
		return true;
	}
}