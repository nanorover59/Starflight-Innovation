package space.world;

import java.util.ArrayList;

import net.minecraft.block.Blocks;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.chunk.BlockColumn;

public class CustomSurfaceBuilder
{
	private ArrayList<RegistryKey<Biome>> biomes = new ArrayList<RegistryKey<Biome>>();
	
	public CustomSurfaceBuilder()
	{
	}
	
	/**
	 * Add the given biome to the list of biomes this surface builder is used for.
	 */
	public void applyToBiome(RegistryKey<Biome> biome)
	{
		biomes.add(biome);
	}
	
	/**
	 * Check if this surface builder is used for the given biome.
	 */
	public boolean isForBiome(RegistryKey<Biome> biome)
	{
		return biomes.contains(biome);
	}
	
	/**
	 * Build surface blocks in a specific block column.
	 */
	public void buildSurface(ChunkRegion region, BlockColumn blockColumn, int bottomY, int surfaceY, int x, int z)
	{
	}
	
	public void buildBedrock(ChunkRegion region, BlockColumn blockColumn, int bottomY)
	{
		for(int y = bottomY; y < bottomY + 3; y++)
        {
        	if(y == bottomY || region.getRandom().nextBoolean())
        		blockColumn.setState(y, Blocks.BEDROCK.getDefaultState());
        }
	}
}
