package space.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.world.ServerWorld;
import space.planet.PlanetList;

@Mixin(ServerWorld.class)
public class ServerWorldMixin
{
	@Inject(method = "tick(Ljava/util/function/BooleanSupplier;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;setTimeOfDay(J)V"))
	private void tickInject(CallbackInfo info)
	{
		PlanetList.skipToMorning(PlanetList.getByName("earth"));
	}
}