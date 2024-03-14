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

@Environment(EnvType.CLIENT)
public class ReactionControlThrusterParticle extends SpriteBillboardParticle
{
	private final SpriteProvider spriteProvider;

	protected ReactionControlThrusterParticle(ClientWorld world, double x, double y, double z, double vx, double vy, double vz, float scale, SpriteProvider spriteProvider)
	{
		super(world, x, y, z, vx, vy, vz);
		this.maxAge = 2;
		this.scale = scale;
		this.spriteProvider = spriteProvider;
		this.setSpriteForAge(spriteProvider);
		this.velocityX = vx;
		this.velocityY = vy;
		this.velocityZ = vz;
		this.prevPosX = this.x;
		this.prevPosY = this.y;
		this.prevPosZ = this.z;
	}

	@Override
	public int getBrightness(float tint)
	{
		return 0xF000F0;
	}

	@Override
	public void tick()
	{
		this.prevPosX = this.x;
        this.prevPosY = this.y;
        this.prevPosZ = this.z;
        
        if(this.age++ >= this.maxAge)
        {
            this.markDead();
            return;
        }
        
        scale *= 2.0;
        alpha *= 0.75;
        
        this.move(this.velocityX, this.velocityY, this.velocityZ);
		this.setSpriteForAge(this.spriteProvider);
	}

	@Override
	public ParticleTextureSheet getType()
	{
		return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
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
		public Particle createParticle(DefaultParticleType defaultParticleType, ClientWorld clientWorld, double x, double y, double z, double vx, double vy, double vz)
		{
			return new ReactionControlThrusterParticle(clientWorld, x, y, z, vx, vy, vz, 0.25f, this.spriteProvider);
		}
	}
}