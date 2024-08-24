package space.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import space.StarflightMod;
import space.client.StarflightModClient;
import space.client.render.entity.model.CloudSharkEntityModel;
import space.entity.CloudSharkEntity;

@Environment(value = EnvType.CLIENT)
public class CloudSharkEntityRenderer extends MobEntityRenderer<CloudSharkEntity, CloudSharkEntityModel<CloudSharkEntity>>
{
	private static final Identifier TEXTURE = Identifier.of(StarflightMod.MOD_ID, "textures/entity/cloud_shark.png");

	public CloudSharkEntityRenderer(EntityRendererFactory.Context context)
	{
		super(context, new CloudSharkEntityModel<CloudSharkEntity>(context.getPart(StarflightModClient.MODEL_CLOUD_SHARK_LAYER)), 0.5f);
	}
	
	@Override
	protected void scale(CloudSharkEntity entity, MatrixStack matrixStack, float f)
	{
		matrixStack.scale(2.0f, 2.0f, 2.0f);
	}

	@Override
	public Identifier getTexture(CloudSharkEntity entity)
	{
		return TEXTURE;
	}
}