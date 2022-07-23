package space.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.google.common.collect.ImmutableList;

import net.minecraft.entity.Entity;

@Mixin(Entity.class)
public interface EntityMixin
{
	@Accessor("passengerList")
    public void setPassengerList(ImmutableList<Entity> passengerList);
	
	@Accessor("vehicle")
    public void setVehicle(Entity vehicle);
}
