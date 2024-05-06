package space.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import space.particle.StarflightParticleTypes;

@Environment(EnvType.CLIENT)
public class StarflightParticleManager
{
	public static void initializeParticles()
	{
		ParticleFactoryRegistry.getInstance().register(StarflightParticleTypes.THRUSTER, ThrusterParticle.Factory::new);
		ParticleFactoryRegistry.getInstance().register(StarflightParticleTypes.RCS_THRUSTER, ReactionControlThrusterParticle.Factory::new);
		ParticleFactoryRegistry.getInstance().register(StarflightParticleTypes.AIR_FILL, AirParticle.Factory::new);
		ParticleFactoryRegistry.getInstance().register(StarflightParticleTypes.MARS_DUST, MarsDustParticle.Factory::new);
		ParticleFactoryRegistry.getInstance().register(StarflightParticleTypes.EYE, EyeParticle.Factory::new);
	}
}