package space.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.serialization.Lifecycle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.LevelProperties;

@Environment(value=EnvType.CLIENT)
@Mixin(LevelProperties.class)
public abstract class LevelPropertiesMixin
{
	@Shadow @Final private Lifecycle lifecycle;
	
	/**
	 * Disable the experimental world settings warning.
	 */
	@Inject(method = "getLifecycle()Lcom/mojang/serialization/Lifecycle;", at = @At("HEAD"), cancellable = true)
	private void getLifecycleInject(CallbackInfoReturnable<Lifecycle> info)
	{
		if(lifecycle == Lifecycle.experimental())
		{
			info.setReturnValue(Lifecycle.stable());
			info.cancel();
		}
	}
}