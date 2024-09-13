package space.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.DimensionEffects;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

@Environment(value = EnvType.CLIENT)
public class MarsDimensionEffect extends DimensionEffects
{
	public MarsDimensionEffect()
	{
		super(192.0f, true, SkyType.NORMAL, false, false);
	}

	@Override
	public float[] getFogColorOverride(float skyAngle, float tickDelta)
	{
		float g = MathHelper.cos(skyAngle * (float) (Math.PI * 2.0));
		
		if(g >= -0.4f && g <= 0.4f)
		{
			float i = (g - -0.0f) / 0.4f * 0.5f + 0.5f;
			float j = 1.0f - (1.0f - MathHelper.sin(i * (float) Math.PI)) * 0.99f;
			float[] rgba = new float[4];
			rgba[0] = 0.3f;
			rgba[1] = i * i * 0.7f + 0.3f;
			rgba[2] = i * 0.3f + 0.7f;
			rgba[3] = j * j;
			return rgba;
		}
		
		return null;
	}
	
	@Override
	public Vec3d adjustFogColor(Vec3d color, float sunHeight)
	{
		return color.multiply(sunHeight * 0.94f + 0.06f, sunHeight * 0.94f + 0.06f, sunHeight * 0.91f + 0.09f);
	}

	@Override
	public boolean useThickFog(int camX, int camY)
	{
		return false;
	}
}