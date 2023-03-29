package space.world;

import com.mojang.serialization.Codec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;
import space.block.StarflightBlocks;

public class RockPatchFeature extends Feature<DefaultFeatureConfig>
{
	public RockPatchFeature(Codec<DefaultFeatureConfig> configCodec)
	{
		super(configCodec);
	}
	
	@Override
	public boolean generate(FeatureContext<DefaultFeatureConfig> context)
	{
		BlockPos blockPos = context.getOrigin();
		StructureWorldAccess structureWorldAccess = context.getWorld();
		Random random = context.getRandom();
		BlockState blockState = structureWorldAccess.getBlockState(blockPos.down(6 + random.nextInt(8)));
		float chance = 0.15f;
		
		if(structureWorldAccess.getBiome(blockPos).isIn(StarflightWorldGeneration.MORE_SCATTER))
			chance = 0.8f;
		
		// Random chance of generating a stray basalt rock patch.
		if(random.nextInt(4) == 0)
			blockState = Blocks.SMOOTH_BASALT.getDefaultState();
				
		// Skip generating at random or if the block found is not stone related.
		if(random.nextFloat() > chance || !blockState.isIn(BlockTags.PICKAXE_MINEABLE))
			return true;
		
		BlockPos corner = context.getWorld().getChunk(blockPos).getPos().getStartPos();
		int count = random.nextBetween(4, 12);
		
		for(int i = 0; i < count; i++)
		{
			BlockPos offset = corner.add(random.nextInt(16), blockPos.getY(), random.nextInt(16));
			boolean replacingBlock = false;
			
			while(blockPos.getY() > structureWorldAccess.getBottomY() + 3 && !(structureWorldAccess.getBlockState(blockPos.down()).isSideSolidFullSquare(structureWorldAccess, blockPos, Direction.UP)))
				blockPos = blockPos.down();
			
			if(random.nextBoolean())
			{
				blockPos = blockPos.down();
				replacingBlock = true;
			}
			
			if(structureWorldAccess.getBlockState(offset.down()).isSideSolidFullSquare(structureWorldAccess, blockPos, Direction.UP))
				structureWorldAccess.setBlockState(blockPos, getFinalBlockState(random, blockState, replacingBlock), Block.NOTIFY_NEIGHBORS);
		}
		
		return true;
	}
	
	// Randomize the end result of block selection.
	private BlockState getFinalBlockState(Random random, BlockState blockState, boolean replacingBlock)
	{
		if(blockState.getBlock() == Blocks.STONE)
		{
			if(random.nextInt(4) == 0 && !replacingBlock)
				return Blocks.STONE_SLAB.getDefaultState();
			else if(random.nextInt(4) == 0)
				return Blocks.COBBLESTONE.getDefaultState();
		}
		else if(blockState.getBlock() == Blocks.COBBLESTONE && !replacingBlock)
		{
			if(random.nextInt(4) == 0)
				return Blocks.COBBLESTONE_SLAB.getDefaultState();
			else if(random.nextInt(32) == 0)
				return Blocks.COBBLESTONE_WALL.getDefaultState();
		}
		else if(blockState.getBlock() == Blocks.SMOOTH_BASALT)
		{
			if(random.nextInt(4) == 0)
				return Blocks.BASALT.getDefaultState();
			else if(random.nextInt(16) == 0)
				return Blocks.POLISHED_BASALT.getDefaultState();
		}
		else if(blockState.getBlock() == StarflightBlocks.FERRIC_STONE && random.nextInt(4) == 0)
			return StarflightBlocks.REDSLATE.getDefaultState();
		else if((blockState.getBlock() == Blocks.IRON_ORE || blockState.getBlock() == StarflightBlocks.FERRIC_IRON_ORE) && random.nextInt(8) == 0)
			return Blocks.RAW_IRON_BLOCK.getDefaultState();
		else if((blockState.getBlock() == Blocks.GOLD_ORE || blockState.getBlock() == StarflightBlocks.FERRIC_GOLD_ORE) && random.nextInt(8) == 0)
			return Blocks.RAW_GOLD_BLOCK.getDefaultState();
		
		return blockState;
	}
}