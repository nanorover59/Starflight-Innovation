package space.client.particle;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import space.StarflightMod;

public class StarflightParticles
{
	public static final DefaultParticleType THRUSTER = FabricParticleTypes.simple();
	
	public static void initializeParticles()
	{
		Registry.register(Registry.PARTICLE_TYPE, new Identifier(StarflightMod.MOD_ID, "thruster"), THRUSTER);
	}
}