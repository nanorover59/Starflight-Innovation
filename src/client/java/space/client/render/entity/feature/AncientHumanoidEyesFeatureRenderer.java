package space.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.feature.EyesFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.util.Identifier;
import space.StarflightMod;
import space.client.render.entity.model.AncientHumanoidEntityModel;
import space.entity.AncientHumanoidEntity;

@Environment(value = EnvType.CLIENT)
public class AncientHumanoidEyesFeatureRenderer<T extends AncientHumanoidEntity> extends EyesFeatureRenderer<T, AncientHumanoidEntityModel<T>>
{
	private static final RenderLayer SKIN = RenderLayer.getEyes(Identifier.of(StarflightMod.MOD_ID, "textures/entity/ancient_humanoid_eyes.png"));

	public AncientHumanoidEyesFeatureRenderer(FeatureRendererContext<T, AncientHumanoidEntityModel<T>> featureRendererContext)
	{
		super(featureRendererContext);
	}

	@Override
	public RenderLayer getEyesTexture()
	{
		return SKIN;
	}
}