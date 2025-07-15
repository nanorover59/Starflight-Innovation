package space.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

@Environment(EnvType.CLIENT)
@Mixin(ChunkRendererRegion.class)
public abstract class ChunkRendererRegionMixin implements BlockRenderView
{

	@Inject(method = "getBlockState", at = @At("HEAD"), cancellable = true)
	private void onGetBlockState(BlockPos pos, CallbackInfoReturnable<BlockState> info)
	{
		if(pos.getY() < 0)
		{
			info.setReturnValue(Blocks.WATER.getDefaultState());
		}
	}

	@Inject(method = "getFluidState", at = @At("HEAD"), cancellable = true)
	private void onGetFluidState(BlockPos pos, CallbackInfoReturnable<FluidState> info)
	{
		if(pos.getY() < 0)
		{
			info.setReturnValue(Fluids.WATER.getDefaultState());
		}
	}
}