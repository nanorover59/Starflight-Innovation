package space.world;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.chunk.BlockColumn;
import space.block.StarflightBlocks;

public class MoonSurfaceBuilder extends CustomSurfaceBuilder
{
	MoonSurfaceBuilder()
	{
		applyToBiome(StarflightBiomes.MOON_LOWLANDS);
		applyToBiome(StarflightBiomes.MOON_MIDLANDS);
		applyToBiome(StarflightBiomes.MOON_HIGHLANDS);
		applyToBiome(StarflightBiomes.MOON_ICE);
		applyToBiome(StarflightBiomes.MOON_ROCKS);
	}
	
	@Override
	public void buildSurface(ChunkRegion region, BlockColumn blockColumn, int bottomY, int surfaceY, int x, int z)
	{
		RegistryKey<Biome> biome = region.getBiome(new BlockPos(x, 0, z)).getKey().get();
		BlockState topState = StarflightBlocks.REGOLITH.getDefaultState();
		BlockState bottomState = Blocks.COBBLESTONE.getDefaultState();
		int topDepth = 5;
		int bottomDepth = region.getRandom().nextInt(2, 4);
		
		if(biome == StarflightBiomes.MOON_LOWLANDS)
		{
			topState = StarflightBlocks.BALSALTIC_REGOLITH.getDefaultState();
			bottomState = Blocks.SMOOTH_BASALT.getDefaultState();
		}
		
		if(surfaceY < 40)
			blockColumn.setState(surfaceY + 1, topState);
		else
		{
			for(int y = surfaceY - (bottomDepth + topDepth); y <= surfaceY; y++)
			{
				if(y > surfaceY - topDepth)
				{
					if(biome == StarflightBiomes.MOON_ICE && y < surfaceY)
						blockColumn.setState(y, StarflightBlocks.ICY_REGOLITH.getDefaultState());
					else
						blockColumn.setState(y, topState);
				}
				else
					blockColumn.setState(y, bottomState);
			}
		}
	}
}
