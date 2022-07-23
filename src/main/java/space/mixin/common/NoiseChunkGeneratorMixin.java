package space.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.BlockColumn;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.gen.noise.NoiseConfig;
import space.world.CustomSurfaceBuilder;
import space.world.StarflightBiomes;

@Mixin(NoiseChunkGenerator.class)
public abstract class NoiseChunkGeneratorMixin
{
	@Inject(method = "buildSurface(Lnet/minecraft/world/ChunkRegion;Lnet/minecraft/world/gen/StructureAccessor;Lnet/minecraft/world/gen/noise/NoiseConfig;Lnet/minecraft/world/chunk/Chunk;)V", at = @At("HEAD"), cancellable = true)
	public void buildSurfaceInject(ChunkRegion region, StructureAccessor structures, NoiseConfig noiseConfig, Chunk chunk, CallbackInfo info)
	{
		CustomSurfaceBuilder surfaceBuilder = StarflightBiomes.getSurfaceBuilder(region.getBiome(chunk.getPos().getStartPos()).getKey().get());
		
		if(surfaceBuilder != null)
		{
			final BlockPos.Mutable mutable = new BlockPos.Mutable();
	        final ChunkPos chunkPos = chunk.getPos();
	        int i = chunkPos.getStartX();
	        int j = chunkPos.getStartZ();
	        
	        BlockColumn blockColumn = new BlockColumn()
	        {
	            @Override
	            public BlockState getState(int y)
	            {
	                return chunk.getBlockState(mutable.setY(y));
	            }

	            @Override
	            public void setState(int y, BlockState state)
	            {
	                HeightLimitView heightLimitView = chunk.getHeightLimitView();
	                
	                if(y >= heightLimitView.getBottomY() && y < heightLimitView.getTopY())
	                {
	                    chunk.setBlockState(mutable.setY(y), state, false);
	                    
	                    if(!state.getFluidState().isEmpty())
	                        chunk.markBlockForPostProcessing(mutable);
	                }
	            }

	            public String toString()
	            {
	                return "ChunkBlockColumn " + chunkPos;
	            }
	        };
	        
	        for (int k = 0; k < 16; k++)
	        {
	            for (int l = 0; l < 16; l++)
	            {
	                int m = i + k;
	                int n = j + l;
	                int o = chunk.sampleHeightmap(Heightmap.Type.OCEAN_FLOOR_WG, k, l);
	                int p = chunk.getBottomY();
	                mutable.setX(m).setZ(n);
	                surfaceBuilder.buildBedrock(region, blockColumn, p);
	                surfaceBuilder.buildSurface(region, blockColumn, p, o, m, n);
	            }
	        }
	        
	        info.cancel();
		}
	}
}
