package space.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import space.StarflightMod;
import space.client.StarflightModClient;
import space.client.render.entity.feature.AncientHumanoidEyesFeatureRenderer;
import space.client.render.entity.model.AncientHumanoidEntityModel;
import space.entity.AncientHumanoidEntity;

@Environment(value = EnvType.CLIENT)
public class AncientHumanoidEntityRenderer extends BipedEntityRenderer<AncientHumanoidEntity, AncientHumanoidEntityModel<AncientHumanoidEntity>>
{
	private static final Identifier TEXTURE = new Identifier(StarflightMod.MOD_ID, "textures/entity/ancient_humanoid.png");

	public AncientHumanoidEntityRenderer(EntityRendererFactory.Context context)
	{
		super(context, new AncientHumanoidEntityModel<AncientHumanoidEntity>(context.getPart(StarflightModClient.MODEL_ANCIENT_HUMANOID_LAYER)), 0.5f);
		this.addFeature(new AncientHumanoidEyesFeatureRenderer<AncientHumanoidEntity>(this));
		this.addFeature(new ArmorFeatureRenderer<>(this, new AncientHumanoidEntityModel<AncientHumanoidEntity>(context.getPart(StarflightModClient.MODEL_ANCIENT_HUMANOID_INNER_ARMOR_LAYER)), new AncientHumanoidEntityModel<AncientHumanoidEntity>(context.getPart(StarflightModClient.MODEL_ANCIENT_HUMANOID_OUTER_ARMOR_LAYER)), context.getModelManager()));
	}
	
	@Override
	protected void scale(AncientHumanoidEntity entity, MatrixStack matrixStack, float f)
	{
		float g = 1.0f;
		matrixStack.scale(g, g, g);
		super.scale(entity, matrixStack, f);
	}

	@Override
	public Identifier getTexture(AncientHumanoidEntity entity)
	{
		return TEXTURE;
	}
}