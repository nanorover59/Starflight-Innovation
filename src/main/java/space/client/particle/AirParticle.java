package space.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;

@Environment(value = EnvType.CLIENT)
public class AirParticle extends SpriteBillboardParticle
{
	private final SpriteProvider spriteProvider;
	
	protected AirParticle(ClientWorld world, double x, double y, double z, double vx, double vy, double vz, SpriteProvider spriteProvider)
	{
		super(world, x, y, z, vx, vy, vz);
		this.spriteProvider = spriteProvider;
		this.velocityMultiplier = 0.98f;
		this.gravityStrength = 0.0f;
		this.velocityX = vx + (random.nextDouble() - random.nextDouble()) * 0.1;
		this.velocityY = vy + (random.nextDouble() - random.nextDouble()) * 0.1;
		this.velocityZ = vz + (random.nextDouble() - random.nextDouble()) * 0.1;
		this.prevPosX = this.x - this.velocityX;
		this.prevPosY = this.y - this.velocityY;
		this.prevPosZ = this.z - this.velocityZ;
		this.scale = 0.5f - world.random.nextFloat() * 0.25f;
		this.setSpriteForAge(spriteProvider);
	}

	@Override
	public ParticleTextureSheet getType()
	{
		return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
	}

	@Override
	public void tick()
	{
		super.tick();
		this.setSpriteForAge(this.spriteProvider);
	}

	@Environment(value = EnvType.CLIENT)
	public static class Factory implements ParticleFactory<DefaultParticleType>
	{
		private final SpriteProvider spriteProvider;

		public Factory(SpriteProvider spriteProvider)
		{
			this.spriteProvider = spriteProvider;
		}

		@Override
		public Particle createParticle(DefaultParticleType defaultParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i)
		{
			return new AirParticle(clientWorld, d, e, f, g, h, i, this.spriteProvider);
		}
	}
}