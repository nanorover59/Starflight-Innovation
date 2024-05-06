package space.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;
import space.entity.SolarEyesEntity;

@Environment(value=EnvType.CLIENT)
public class SolarEyesEntityRenderer extends EntityRenderer<SolarEyesEntity>
{
	public SolarEyesEntityRenderer(EntityRendererFactory.Context context)
	{
        super(context);
        this.shadowRadius = 0.0f;
    }

	@Override
	public Identifier getTexture(SolarEyesEntity entity)
	{
		return null;
	}
}