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
import space.planet.PlanetDimensionData;
import space.planet.PlanetList;

@Environment(EnvType.CLIENT)
public class ThrusterParticle extends SpriteBillboardParticle
{
	private final SpriteProvider spriteProvider;

	protected ThrusterParticle(ClientWorld world, double x, double y, double z, double vx, double vy, double vz, float scale, double pressure, SpriteProvider spriteProvider)
	{
		super(world, x, y, z, vx, vy, vz);
		float f = random.nextFloat() * 0.6f + 0.4f;
		this.maxAge = 10 + Math.min((int) (pressure * 10), 10) + this.random.nextInt(2);
		this.red = f;
		this.green = f;
		this.blue = f;
		this.scale = scale;
		this.spriteProvider = spriteProvider;
		this.setSpriteForAge(spriteProvider);
		double m = Math.max(-0.5 * pressure + 0.5, 0.25);
		this.velocityX = vx + (random.nextDouble() - random.nextDouble()) * m;
		this.velocityY = vy + (random.nextDouble() - random.nextDouble()) * m;
		this.velocityZ = vz + (random.nextDouble() - random.nextDouble()) * m;
		this.prevPosX = this.x - this.velocityX;
		this.prevPosY = this.y - this.velocityY;
		this.prevPosZ = this.z - this.velocityZ;
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
        
        this.move(this.velocityX, this.velocityY, this.velocityZ);
        
        if(this.onGround)
        {
        	this.velocityX *= 1.5;
        	this.velocityZ *= 1.5;
        }
		
		this.setSpriteForAge(this.spriteProvider);
	}

	@Override
	public ParticleTextureSheet getType()
	{
		return ParticleTextureSheet.PARTICLE_SHEET_LIT;
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
			double pressure = 1.0;
			PlanetDimensionData data = PlanetList.getDimensionDataForWorld(clientWorld);
			
			if(data != null)
				pressure = data.getPressure();
			
			return new ThrusterParticle(clientWorld, x, y, z, vx, vy, vz, 1.0f, pressure, this.spriteProvider);
		}
	}
}