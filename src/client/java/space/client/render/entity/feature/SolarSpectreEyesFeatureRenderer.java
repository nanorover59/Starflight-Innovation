package space.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.feature.EyesFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.util.Identifier;
import space.StarflightMod;
import space.client.render.entity.model.SolarSpectreEntityModel;
import space.entity.SolarSpectreEntity;

@Environment(value = EnvType.CLIENT)
public class SolarSpectreEyesFeatureRenderer<T extends SolarSpectreEntity> extends EyesFeatureRenderer<T, SolarSpectreEntityModel<T>>
{
	private static final RenderLayer SKIN = RenderLayer.getEyes(Identifier.of(StarflightMod.MOD_ID, "textures/entity/solar_spectre_eyes.png"));

	public SolarSpectreEyesFeatureRenderer(FeatureRendererContext<T, SolarSpectreEntityModel<T>> featureRendererContext)
	{
		super(featureRendererContext);
	}

	@Override
	public RenderLayer getEyesTexture()
	{
		return SKIN;
	}
}