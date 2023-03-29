package space.world;

import com.mojang.serialization.Codec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class SmallCraterFeature extends Feature<DefaultFeatureConfig>
{
	public SmallCraterFeature(Codec<DefaultFeatureConfig> configCodec)
	{
		super(configCodec);
	}

	@Override
	public boolean generate(FeatureContext<DefaultFeatureConfig> context)
	{
		StructureWorldAccess structureWorldAccess = context.getWorld();
		Random random = context.getRandom();
		float chance = 0.1f;
		
		if(random.nextFloat() > chance)
			return true;
		
		int radius = 4 + random.nextInt(12);
		double depthFactor = 0.3 + random.nextDouble() * 0.5;
		double rimWidth = 0.5 + random.nextDouble() * 0.15;
		double rimSteepness = 0.25 + random.nextDouble() * 0.25;
		BlockPos.Mutable mutable = new BlockPos.Mutable();
		BlockPos startPos = context.getOrigin().add(random.nextInt(4) - random.nextInt(4), 0, random.nextInt(4) - random.nextInt(4));
		
		for(int x = -16; x < 16; x++)
		{
			for(int z = -16; z < 16; z++)
			{
				mutable.set(startPos.getX() + x, 0, startPos.getZ() + z);
				int localSurfaceY = structureWorldAccess.getTopPosition(Type.OCEAN_FLOOR_WG, mutable).getY();
				mutable.setY(localSurfaceY);
				BlockState surfaceState = structureWorldAccess.getBlockState(mutable);	
				double r = MathHelper.hypot(mutable.getX() - startPos.getX(), mutable.getZ() - startPos.getZ()) / radius;
				double parabola = r * r - 1.0;
				double rimR = Math.min(r - rimWidth - 1.0, 0.0);
				double rim = rimR * rimR * rimSteepness;
				double shape = Math.min(parabola, rim);
				shape = Math.max(shape, -depthFactor);
				int y = localSurfaceY + (int) (shape * radius);
				
				if(y <= localSurfaceY)
				{
					for(int i = y; i <= localSurfaceY + 5; i++)
					{
						mutable.setY(i);
						structureWorldAccess.setBlockState(mutable, Blocks.AIR.getDefaultState(), Block.REDRAW_ON_MAIN_THREAD);
					}
					
					mutable.setY(y - 1);
					structureWorldAccess.setBlockState(mutable, surfaceState, Block.REDRAW_ON_MAIN_THREAD);
				}
				else
				{
					for(int i = y; i > localSurfaceY; i--)
					{
						mutable.setY(i);
						structureWorldAccess.setBlockState(mutable, surfaceState, Block.REDRAW_ON_MAIN_THREAD);
					}
				}
			}
		}
			
		return true;
	}
}