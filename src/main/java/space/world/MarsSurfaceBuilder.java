package space.world;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.chunk.BlockColumn;
import space.block.StarflightBlocks;

public class MarsSurfaceBuilder extends CustomSurfaceBuilder
{
	MarsSurfaceBuilder()
	{
		applyToBiome(StarflightBiomes.MARS_LOWLANDS);
		applyToBiome(StarflightBiomes.MARS_MIDLANDS);
		applyToBiome(StarflightBiomes.MARS_HIGHLANDS);
		applyToBiome(StarflightBiomes.MARS_ICE);
	}
	
	@Override
	public void buildSurface(ChunkRegion region, BlockColumn blockColumn, int bottomY, int surfaceY, int x, int z)
	{
		RegistryKey<Biome> biome = region.getBiome(new BlockPos(x, 0, z)).getKey().get();
		BlockState topState = StarflightBlocks.FERRIC_SAND.getDefaultState();
		BlockState bottomState = StarflightBlocks.FERRIC_STONE.getDefaultState();
		int topDepth = 4;
		int bottomDepth = region.getRandom().nextInt(2, 5);
		int newSeaLevel = 30;
		
		if(biome == StarflightBiomes.MARS_ICE)
			topState = StarflightBlocks.DRY_SNOW_BLOCK.getDefaultState();
		
		for(int y = surfaceY; y >= bottomY; y--)
		{
			if(blockColumn.getState(y).getBlock() != Blocks.BEDROCK)
				blockColumn.setState(y, StarflightBlocks.FERRIC_STONE.getDefaultState());
		}
		
		for(int y = region.getSeaLevel(); y >= newSeaLevel; y--)
		{
			if(blockColumn.getState(y).getBlock() == Blocks.WATER)
			{
				if(y == newSeaLevel)
				{
					int j = region.getRandom().nextInt(3);
					j += region.getRandom().nextBoolean() ? 2 : 3;
					
					for(int i = 0; i < j; i++)
						blockColumn.setState(y - i, i < 2 || region.getRandom().nextBoolean() ? Blocks.PACKED_ICE.getDefaultState() :  Blocks.ICE.getDefaultState());
					
					if(y > surfaceY)
						blockColumn.setState(y, topState);
				}
				else
					blockColumn.setState(y, Blocks.AIR.getDefaultState());
			}
		}
		
		if(surfaceY < 30)
			blockColumn.setState(surfaceY + 1, topState);
		else
		{
			for(int y = surfaceY - (bottomDepth + topDepth); y <= surfaceY; y++)
			{
				if(y > surfaceY - topDepth)
				{
					if(biome == StarflightBiomes.MARS_ICE && y <= surfaceY)
						blockColumn.setState(y, y == surfaceY && region.getRandom().nextFloat() < 0.25f ? Blocks.BLUE_ICE.getDefaultState() : Blocks.PACKED_ICE.getDefaultState());
					else
						blockColumn.setState(y, topState);
				}
				else
					blockColumn.setState(y, bottomState);
			}
		}
		
		if(biome == StarflightBiomes.MARS_ICE)
			blockColumn.setState(surfaceY + 1, StarflightBlocks.DRY_SNOW_BLOCK.getDefaultState());
	}
}
