package space.mixin.common;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BlockState;
import net.minecraft.block.PillarBlock;
import net.minecraft.item.AxeItem;
import space.block.StarflightBlocks;

@Mixin(AxeItem.class)
public abstract class AxeItemMixin
{
	@Inject(method = "getStrippedState(Lnet/minecraft/block/BlockState;)Ljava/util/Optional;", at = @At("HEAD"), cancellable = true)
	public void getStrippedStateInject(BlockState state, CallbackInfoReturnable<Optional<BlockState>> info)
	{
		if(state.getBlock() == StarflightBlocks.RUBBER_LOG)
		{
			info.setReturnValue(Optional.ofNullable(StarflightBlocks.STRIPPED_RUBBER_LOG.getDefaultState().with(PillarBlock.AXIS, state.get(PillarBlock.AXIS))));
			info.cancel();
		}
	}
}
