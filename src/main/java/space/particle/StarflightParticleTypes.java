package space.particle;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import space.StarflightMod;

public class StarflightParticleTypes
{
	public static final SimpleParticleType THRUSTER = FabricParticleTypes.simple();
	public static final SimpleParticleType RCS_THRUSTER = FabricParticleTypes.simple();
	public static final SimpleParticleType AIR_FILL = FabricParticleTypes.simple();
	public static final SimpleParticleType MARS_DUST = FabricParticleTypes.simple();
	public static final SimpleParticleType EYE = FabricParticleTypes.simple();

	public static void initializeParticles()
	{
		Registry.register(Registries.PARTICLE_TYPE, Identifier.of(StarflightMod.MOD_ID, "thruster"), THRUSTER);
		Registry.register(Registries.PARTICLE_TYPE, Identifier.of(StarflightMod.MOD_ID, "rcs_thruster"), RCS_THRUSTER);
		Registry.register(Registries.PARTICLE_TYPE, Identifier.of(StarflightMod.MOD_ID, "air_fill"), AIR_FILL);
		Registry.register(Registries.PARTICLE_TYPE, Identifier.of(StarflightMod.MOD_ID, "mars_dust"), MARS_DUST);
		Registry.register(Registries.PARTICLE_TYPE, Identifier.of(StarflightMod.MOD_ID, "eye"), EYE);
	}
}