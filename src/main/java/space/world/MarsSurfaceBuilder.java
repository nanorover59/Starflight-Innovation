package space.world;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
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
		applyToBiome(StarflightBiomes.MARS_DRIPSTONE);
		applyToBiome(StarflightBiomes.MARS_LUSH_CAVES);
	}
	
	@Override
	public void buildSurface(ChunkRegion region, BlockColumn blockColumn, int bottomY, int surfaceY, int x, int z)
	{
		RegistryKey<Biome> biome = region.getBiome(new BlockPos(x, 0, z)).getKey().get();
		BlockState topState = StarflightBlocks.FERRIC_SAND.getDefaultState();
		BlockState bottomState = StarflightBlocks.FERRIC_STONE.getDefaultState();
		int topDepth = 4;
		int bottomDepth = 2 + region.getRandom().nextInt(3);
		
		if(biome == StarflightBiomes.MARS_LOWLANDS)
			bottomState = StarflightBlocks.REDSLATE.getDefaultState();
		
		if(biome == StarflightBiomes.MARS_ICE)
			topState = StarflightBlocks.DRY_SNOW_BLOCK.getDefaultState();
		
		for(int y = surfaceY; y >= bottomY; y--)
		{
			if(blockColumn.getState(y).getMaterial().isSolid())
				blockColumn.setState(y, StarflightBlocks.FERRIC_STONE.getDefaultState());
			
			if(blockColumn.getState(y).getBlock() == Blocks.WATER && blockColumn.getState(y + 1).getMaterial() == Material.AIR)
				blockColumn.setState(y, region.getRandom().nextBoolean() ? Blocks.PACKED_ICE.getDefaultState() : Blocks.ICE.getDefaultState());
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
						blockColumn.setState(y, y == surfaceY && region.getRandom().nextFloat() < 0.25f ? Blocks.PACKED_ICE.getDefaultState() : StarflightBlocks.FERRIC_SAND.getDefaultState());
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
