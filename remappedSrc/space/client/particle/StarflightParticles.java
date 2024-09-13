package space.client.particle;

import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import space.StarflightMod;

public class StarflightParticles
{
	public static final DefaultParticleType THRUSTER = FabricParticleTypes.simple();
	public static final DefaultParticleType AIR_FILL = FabricParticleTypes.simple();
	public static final DefaultParticleType MARS_DUST = FabricParticleTypes.simple();
	
	public static void initializeParticles()
	{
		Registry.register(Registry.PARTICLE_TYPE, new Identifier(StarflightMod.MOD_ID, "thruster"), THRUSTER);
		Registry.register(Registry.PARTICLE_TYPE, new Identifier(StarflightMod.MOD_ID, "air_fill"), AIR_FILL);
		Registry.register(Registry.PARTICLE_TYPE, new Identifier(StarflightMod.MOD_ID, "mars_dust"), MARS_DUST);
		
		ParticleFactoryRegistry.getInstance().register(StarflightParticles.THRUSTER, ThrusterParticle.Factory::new);
		ParticleFactoryRegistry.getInstance().register(StarflightParticles.AIR_FILL, AirParticle.Factory::new);
		ParticleFactoryRegistry.getInstance().register(StarflightParticles.MARS_DUST, MarsDustParticle.Factory::new);
	}
}