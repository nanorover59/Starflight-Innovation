package space.world;

import org.joml.Vector3f;

import com.mojang.serialization.Codec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.Direction;
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
		
		if(!structureWorldAccess.getBlockState(origin).isAir())
			return false;
		
		int divisions = Math.round(powerLaw(random, 2.5f, 6.0f, 12.0f));
		int reach = Math.round(powerLaw(random, 2.5f, 8.0f, 16.0f));
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
				
				for(Direction direction : Direction.values())
					placeAeroplanktonBlock(structureWorldAccess, random, mutable.offset(direction), origin);
			}
			
			vector.rotateAxis((float) ((Math.PI * 2.0) / divisions), axis.x(), axis.y(), axis.z());
		}
		
		return true;
	}
	
	private void placeAeroplanktonBlock(StructureWorldAccess structureWorldAccess, Random random, BlockPos blockPos, BlockPos originPos)
	{
		double r = blockPos.getSquaredDistance(originPos);
		BlockState state;
		
		if(random.nextDouble() * 128.0 < r)
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