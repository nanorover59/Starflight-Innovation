package space.world.surface;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.chunk.BlockColumn;
import space.block.StarflightBlocks;
import space.world.CustomSurfaceBuilder;
import space.world.StarflightBiomes;

public class MoonSurfaceBuilder extends CustomSurfaceBuilder
{
	public MoonSurfaceBuilder()
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
		int bottomDepth = 2 + region.getRandom().nextInt(2);
		
		if(biome == StarflightBiomes.MOON_LOWLANDS)
		{
			topState = StarflightBlocks.BALSALTIC_REGOLITH.getDefaultState();
			bottomState = Blocks.SMOOTH_BASALT.getDefaultState();
		}
		
		if(surfaceY > 0)
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
		
		for(int y = surfaceY; y > region.getBottomY(); y--)
		{
			if(biome == StarflightBiomes.MOON_ICE && blockColumn.getState(y).getBlock() == Blocks.AIR && (blockColumn.getState(y - 1).getBlock() == Blocks.STONE || blockColumn.getState(y + 1).getBlock() == Blocks.STONE))
				blockColumn.setState(y, region.getRandom().nextBoolean() ? (y > -40 ? Blocks.ICE.getDefaultState() : Blocks.AIR.getDefaultState()) : Blocks.PACKED_ICE.getDefaultState());
			else if(blockColumn.getState(y + 1).getBlock() == Blocks.AIR && blockColumn.getState(y).getBlock() == Blocks.STONE)
				blockColumn.setState(y, region.getRandom().nextBoolean() ? (region.getRandom().nextBoolean() ? Blocks.COBBLESTONE.getDefaultState() : Blocks.GRAVEL.getDefaultState()) : Blocks.STONE.getDefaultState());	
		}
	}
}
