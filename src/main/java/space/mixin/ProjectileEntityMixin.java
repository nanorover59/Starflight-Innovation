package space.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.hit.HitResult;

@Mixin(ProjectileEntity.class)
public interface ProjectileEntityMixin
{
	@Invoker()
	public static float callUpdateRotation(float prevRot, float newRot)
	{
		return 0;
	}
	
	@Invoker()
	public void callOnCollision(HitResult hitResult);
	
	@Invoker()
	public boolean callCanHit(Entity entity);
}
