package space.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.world.BlockView;
import space.entity.MovingCraftEntity;

@Environment(value=EnvType.CLIENT)
@Mixin(Camera.class)
public abstract class CameraMixin
{
	@Shadow abstract double clipToSpace(double desiredCameraDistance);
	@Shadow abstract void moveBy(double x, double y, double z);
	
	@Inject(method = "update(Lnet/minecraft/world/BlockView;Lnet/minecraft/entity/Entity;ZZF)V", at = @At("TAIL"), cancellable = true)
	public void updateInject(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo info)
	{
		if(thirdPerson && focusedEntity.getVehicle() instanceof MovingCraftEntity)
		{
			this.moveBy(-this.clipToSpace(32.0), 0.0, 0.0);
		}
	}
}