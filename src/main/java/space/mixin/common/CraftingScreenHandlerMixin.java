package space.mixin.common;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import space.block.StarflightBlocks;

@Mixin(CraftingScreenHandler.class)
public abstract class CraftingScreenHandlerMixin extends ScreenHandler
{
	@Shadow @Final ScreenHandlerContext context;
	
	protected CraftingScreenHandlerMixin(ScreenHandlerType<?> type, int syncId)
	{
		super(type, syncId);
	}
	
	@Inject(method = "canUse(Lnet/minecraft/entity/player/PlayerEntity;)Z", at = @At("RETURN"), cancellable = true)
	public void canUseInject(PlayerEntity player, CallbackInfoReturnable<Boolean> info)
	{
		info.setReturnValue(info.getReturnValue() || ScreenHandler.canUse(context, player, StarflightBlocks.METAL_CRAFTING_TABLE));
	}
}