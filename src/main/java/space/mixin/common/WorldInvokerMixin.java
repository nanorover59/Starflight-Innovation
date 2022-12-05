package space.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.World;
import space.planet.PlanetDimensionData;

@Mixin(World.class)
public interface WorldInvokerMixin
{
	@Invoker()
	public PlanetDimensionData callGetPlanetDimensionData();
}