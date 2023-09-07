package space.particle;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import space.StarflightMod;

public class StarflightParticleTypes
{
	public static final DefaultParticleType THRUSTER = FabricParticleTypes.simple();
	public static final DefaultParticleType AIR_FILL = FabricParticleTypes.simple();
	public static final DefaultParticleType MARS_DUST = FabricParticleTypes.simple();

	public static void initializeParticles()
	{
		Registry.register(Registries.PARTICLE_TYPE, new Identifier(StarflightMod.MOD_ID, "thruster"), THRUSTER);
		Registry.register(Registries.PARTICLE_TYPE, new Identifier(StarflightMod.MOD_ID, "air_fill"), AIR_FILL);
		Registry.register(Registries.PARTICLE_TYPE, new Identifier(StarflightMod.MOD_ID, "mars_dust"), MARS_DUST);
	}
}