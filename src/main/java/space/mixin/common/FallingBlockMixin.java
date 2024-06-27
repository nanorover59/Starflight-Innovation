package space.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BlockState;
import net.minecraft.block.FallingBlock;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import space.StarflightMod;

@Mixin(FallingBlock.class)
public abstract class FallingBlockMixin
{
	/**
	 * Prevent blocks from falling when in orbit.
	 */
	@Inject(method = "onBlockAdded(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Z)V", at = @At("HEAD"), cancellable = true)
	public void onBlockAddedInject(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify, CallbackInfo info)
	{
		if(world.getBiome(pos).matchesId(Identifier.of(StarflightMod.MOD_ID, "space")))
			info.cancel();
	}
	
	/**
	 * Prevent blocks from falling when in orbit.
	 */
	@Inject(method = "getStateForNeighborUpdate(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/Direction;Lnet/minecraft/block/BlockState;Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;", at = @At("HEAD"), cancellable = true)
	public void getStateForNeighborUpdateInject(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos, CallbackInfoReturnable<BlockState> info)
	{
		if(world.getBiome(pos).matchesId(Identifier.of(StarflightMod.MOD_ID, "space")))
		{
			info.setReturnValue(state);
			info.cancel();
		}
	}
}