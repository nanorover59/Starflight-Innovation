package space.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;
import space.StarflightMod;
import space.client.StarflightModClient;
import space.client.render.entity.feature.MimicFeatureRenderer;
import space.client.render.entity.model.MimicEntityModel;
import space.entity.MimicEntity;

@Environment(value = EnvType.CLIENT)
public class MimicEntityRenderer extends MobEntityRenderer<MimicEntity, MimicEntityModel<MimicEntity>>
{
	private static final Identifier TEXTURE = Identifier.of(StarflightMod.MOD_ID, "textures/entity/mimic.png");

	public MimicEntityRenderer(EntityRendererFactory.Context context)
	{
		super(context, new MimicEntityModel<MimicEntity>(context.getPart(StarflightModClient.MODEL_MIMIC_LAYER)), 0.5f);
		this.addFeature(new MimicFeatureRenderer(this, context.getBlockRenderManager()));
	}

	@Override
	protected RenderLayer getRenderLayer(MimicEntity entity, boolean showBody, boolean translucent, boolean showOutline)
	{
		Identifier identifier = this.getTexture(entity);
		
		if(showBody)
			return RenderLayer.getEntityTranslucentEmissive(identifier);
		else
			return showOutline ? RenderLayer.getOutline(identifier) : null;
	}

	@Override
	public Identifier getTexture(MimicEntity entity)
	{
		return TEXTURE;
	}
}