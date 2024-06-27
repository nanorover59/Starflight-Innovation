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
	@Shadow abstract float clipToSpace(float f);
	@Shadow abstract void moveBy(float x, float y, float z);
	
	@Inject(method = "update(Lnet/minecraft/world/BlockView;Lnet/minecraft/entity/Entity;ZZF)V", at = @At("TAIL"), cancellable = true)
	public void updateInject(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo info)
	{
		if(thirdPerson && focusedEntity.getVehicle() instanceof MovingCraftEntity)
		{
			this.moveBy(-this.clipToSpace(32.0f), 0.0f, 0.0f);
		}
	}
}