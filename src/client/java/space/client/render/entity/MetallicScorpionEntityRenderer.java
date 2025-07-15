package space.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;
import space.StarflightMod;
import space.client.StarflightModClient;
import space.client.render.entity.model.MetallicScorpionEntityModel;
import space.entity.MetallicScorpionEntity;

@Environment(EnvType.CLIENT)
public class MetallicScorpionEntityRenderer extends MobEntityRenderer<MetallicScorpionEntity, MetallicScorpionEntityModel<MetallicScorpionEntity>>
{
	private static final Identifier TEXTURE = Identifier.of(StarflightMod.MOD_ID, "textures/entity/metallic_scorpion.png");

	public MetallicScorpionEntityRenderer(EntityRendererFactory.Context context)
	{
		super(context, new MetallicScorpionEntityModel<MetallicScorpionEntity>(context.getPart(StarflightModClient.MODEL_METALLIC_SCORPION_LAYER)), 0.8f);
	}

	protected float getLyingAngle(MetallicScorpionEntity entity)
	{
		return 180.0F;
	}

	public Identifier getTexture(MetallicScorpionEntity entity)
	{
		return TEXTURE;
	}
}