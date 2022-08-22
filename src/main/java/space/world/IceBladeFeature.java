package space.world;

import java.util.ArrayList;

import com.mojang.serialization.Codec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class IceBladeFeature extends Feature<DefaultFeatureConfig>
{
	public IceBladeFeature(Codec<DefaultFeatureConfig> configCodec)
	{
		super(configCodec);
	}
	
	@Override
	public boolean generate(FeatureContext<DefaultFeatureConfig> context)
	{
		BlockPos blockPos = context.getOrigin();
		StructureWorldAccess structureWorldAccess = context.getWorld();
		Random random = context.getRandom();
		BlockState blockState = Blocks.PACKED_ICE.getDefaultState();
		float angle = random.nextFloat() * (float) Math.PI * 2.0f;
		int size = random.nextBetween(1, 6);
		ArrayList<Integer> yList = new ArrayList<Integer>();
		
		for(int i = structureWorldAccess.getBottomY(); i < structureWorldAccess.getTopY() - 1; i++)
		{
			BlockPos pos = new BlockPos(blockPos.getX(), i, blockPos.getZ());
			
			if((structureWorldAccess.getBlockState(pos).getBlock() == Blocks.ICE || structureWorldAccess.getBlockState(pos).getBlock() == Blocks.PACKED_ICE) && structureWorldAccess.getBlockState(pos.up()).getBlock() == Blocks.AIR)
				yList.add(i);
		}
		
		if(yList.isEmpty())
			return false;
		
		blockPos = new BlockPos(blockPos.getX(), yList.get(random.nextInt(yList.size())), blockPos.getZ());
		
		for(int i = 0; i < size; i++)
		{
			Vec3d offset = new Vec3d(1.0, 0.0, 0.0).rotateY(angle).multiply(i);
			BlockPos pos1 = blockPos.add(offset.getX(), 0, offset.getZ());
			BlockPos pos2 = blockPos.add(-offset.getX(), 0, -offset.getZ());
			int height1 = size - i + (random.nextFloat() < 0.25f ? 1 : 0);
			int height2 = size - i + (random.nextFloat() < 0.25f ? 1 : 0);
			
			for(int j = 0; j < height1; j++)
				structureWorldAccess.setBlockState(pos1.up(j), blockState, Block.NO_REDRAW);
			
			for(int j = 0; j < height2; j++)
				structureWorldAccess.setBlockState(pos2.up(j), blockState, Block.NO_REDRAW);
		}
		
		return true;
	}
}