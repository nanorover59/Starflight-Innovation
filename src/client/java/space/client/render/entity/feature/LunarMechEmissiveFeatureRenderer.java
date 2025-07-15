package space.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.feature.EyesFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.util.Identifier;
import space.StarflightMod;
import space.client.render.entity.model.LunarMechEntityModel;
import space.entity.LunarMechEntity;

@Environment(value = EnvType.CLIENT)
public class LunarMechEmissiveFeatureRenderer<T extends LunarMechEntity> extends EyesFeatureRenderer<T, LunarMechEntityModel<T>>
{
	private static final RenderLayer SKIN = RenderLayer.getEyes(Identifier.of(StarflightMod.MOD_ID, "textures/entity/lunar_mech_emissive.png"));

	public LunarMechEmissiveFeatureRenderer(FeatureRendererContext<T, LunarMechEntityModel<T>> featureRendererContext)
	{
		super(featureRendererContext);
	}

	@Override
	public RenderLayer getEyesTexture()
	{
		return SKIN;
	}
}