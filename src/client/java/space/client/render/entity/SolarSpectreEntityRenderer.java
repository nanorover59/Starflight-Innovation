package space.client.render.entity;

import org.joml.Quaternionf;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import space.StarflightMod;
import space.client.StarflightModClient;
import space.client.render.entity.model.SolarSpectreEntityModel;
import space.entity.SolarSpectreEntity;
import space.util.QuaternionUtil;

@Environment(value = EnvType.CLIENT)
public class SolarSpectreEntityRenderer extends MobEntityRenderer<SolarSpectreEntity, SolarSpectreEntityModel<SolarSpectreEntity>>
{
	private static final Identifier TEXTURE = Identifier.of(StarflightMod.MOD_ID, "textures/entity/solar_spectre.png");

	public SolarSpectreEntityRenderer(EntityRendererFactory.Context context)
	{
		super(context, new SolarSpectreEntityModel<SolarSpectreEntity>(context.getPart(StarflightModClient.MODEL_SOLAR_SPECTRE_LAYER)), 0.5f);
	}

	@Override
	public void render(SolarSpectreEntity entity, float f, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light)
	{
		if(entity.isInvisible())
			return;
		
		this.model.setAngles(entity, entity.limbAnimator.getPos(tickDelta), entity.limbAnimator.getSpeed(tickDelta), getAnimationProgress(entity, tickDelta), entity.getHeadYaw(), entity.getPitch());
		VertexConsumer normalConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucentEmissive(getTexture(entity)));
		Vec3d translate = entity.getVelocity().multiply(-0.1);
		float spin = 1.0f;
		
		for(int i = 0; i < 4; i++)
		{
			matrices.push();
			float scale = 1.0f + (0.25f * (float) i);
			matrices.scale(scale, scale, scale); // Scale up slightly for glow halo
			matrices.translate(translate.getX() * (float) i, translate.getY() * (float) i, translate.getZ() * (float) i);
			
			// Apply rotation
			if(entity.clientQuaternionPrevious != null && entity.clientQuaternion != null)
			{
				Quaternionf q = QuaternionUtil.interpolate(entity.clientQuaternionPrevious.normalize(), entity.clientQuaternion.normalize(), tickDelta * (1.0f - (0.1f * (float) i)));
				matrices.multiply(q);
			}
			else if(entity.clientQuaternion != null)
				matrices.multiply(entity.clientQuaternion.normalize());

			matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(MathHelper.lerpAngleDegrees(tickDelta, entity.clientRollExtraPrevious * spin, entity.clientRollExtra * spin)));
			
			this.model.render(matrices, normalConsumer, light, OverlayTexture.DEFAULT_UV, ColorHelper.Argb.fromFloats(0.5f, 1.0f, 1.0f, 1.0f));
			matrices.pop();
			
			spin *= -1.0f;
		}
	}
	
	@Override
	protected void scale(SolarSpectreEntity entity, MatrixStack matrixStack, float f)
	{
		matrixStack.scale(2.0f, 2.0f, 2.0f);
	}

	@Override
	public Identifier getTexture(SolarSpectreEntity entity)
	{
		return TEXTURE;
	}
}