package space.world;

import org.joml.Vector3f;

import com.mojang.serialization.Codec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;
import space.block.StarflightBlocks;

public class AeroplanktonFeature extends Feature<DefaultFeatureConfig>
{
	public AeroplanktonFeature(Codec<DefaultFeatureConfig> configCodec)
	{
		super(configCodec);
	}

	@Override
	public boolean generate(FeatureContext<DefaultFeatureConfig> context)
	{
		BlockPos origin = context.getOrigin();
		StructureWorldAccess structureWorldAccess = context.getWorld();
		Random random = context.getRandom();
		int typeSelection = random.nextInt(3);
		
		if(typeSelection == 0)
		{
			// Fingers
			int divisions = random.nextBetween(6, 12);
			int reach = Math.round(powerLaw(random, 2.5f, 2.0f, 16.0f));
			Vector3f vector = new Vector3f(random.nextFloat() - random.nextFloat(), random.nextFloat() - random.nextFloat(), random.nextFloat() - random.nextFloat()).normalize();
			Vector3f axis = new Vector3f(random.nextFloat() - random.nextFloat(), random.nextFloat() - random.nextFloat(), random.nextFloat() - random.nextFloat()).normalize();
			vector.rotateAxis((float) (Math.PI * 2.0) * random.nextFloat(), axis.x(), axis.y(), axis.z());
			
			for(int i = 0; i < divisions; i++)
			{
				Vector3f pos = new Vector3f(origin.getX(), origin.getY(), origin.getZ());
				Mutable mutable = new Mutable();
				
				for(int j = 0; j < reach; j++)
				{
					pos.add(vector);
					mutable.set(pos.x(), pos.y(), pos.z());
					placeAeroplanktonBlock(structureWorldAccess, random, mutable, origin);
				}
				
				vector.rotateAxis((float) ((Math.PI * 2.0) / divisions), axis.x(), axis.y(), axis.z());
			}
		}
		else
		{
			// Orb
			int radius = Math.round(powerLaw(random, 2.5f, 2.0f, 8.0f));
			int boundary = Math.round(powerLaw(random, 2.5f, 4.0f, 16.0f));
			Mutable mutable = new Mutable();
			
			for(int x = origin.getX() - radius; x <= origin.getX() + radius; x++)
			{
				for(int y = origin.getY() - radius; y <= origin.getY() + radius; y++)
				{
					for(int z = origin.getZ() - radius; z <= origin.getZ() + radius; z++)
					{
						mutable.set(x, y, z);
						double sd = mutable.getSquaredDistance(origin);
						
						if(sd < radius * radius && sd > radius * radius - boundary)
							placeAeroplanktonBlock(structureWorldAccess, random, mutable, origin);
					}
				}
			}
		}
		
		return true;
	}
	
	private void placeAeroplanktonBlock(StructureWorldAccess structureWorldAccess, Random random, BlockPos blockPos, BlockPos originPos)
	{
		double r = blockPos.getSquaredDistance(originPos);
		BlockState state;
		
		if(random.nextDouble() * 256.0 < r)
			state = StarflightBlocks.RED_AEROPLANKTON.getDefaultState();
		else
			state = StarflightBlocks.AEROPLANKTON.getDefaultState();
		
		structureWorldAccess.setBlockState(blockPos, state, Block.NOTIFY_LISTENERS);
	}
	
	private float powerLaw(Random random, float alpha, float min, float max)
	{
		float u = random.nextFloat();
		return (float) (Math.pow((Math.pow(max, 1.0f - alpha) - Math.pow(min, 1.0f - alpha)) * u + Math.pow(min, 1.0f - alpha), 1.0f / (1.0f - alpha)));
	}
}