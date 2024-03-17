package space.world;

import org.joml.Vector3f;

import com.mojang.serialization.Codec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
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
		
		System.out.println(origin.toShortString());
		
		int divisions = 8;
		int reach = 8;
		Vector3f vector = new Vector3f(1.0f, 0.0f, 0.0f);
		Vector3f axis = new Vector3f(random.nextFloat() - random.nextFloat(), random.nextFloat() - random.nextFloat(), random.nextFloat() - random.nextFloat()).normalize();
		vector.rotateAxis((float) (Math.PI * 2.0) * random.nextFloat(), axis.x(), axis.y(), axis.z());
		
		for(int i = 0; i < divisions; i++)
		{
			BlockPos.Mutable mutable = origin.mutableCopy();
			
			for(int j = 0; j < reach; j++)
			{
				mutable.set(origin.add((int) vector.x() * j, (int) vector.y() * j, (int) vector.z() * j));
				placeAeroplanktonBlock(structureWorldAccess, random, mutable, origin);
			}
			
			vector.rotateAxis((float) ((Math.PI * 2.0) / divisions), axis.x(), axis.y(), axis.z());
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