package space.client.render.entity;

import org.joml.Quaternionf;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import space.StarflightMod;
import space.client.StarflightModClient;
import space.client.render.entity.model.SolarSpectreEntityModel;
import space.entity.SolarSpectreEntity;
import space.entity.StratofishEntity;
import space.util.QuaternionUtil;

@Environment(value = EnvType.CLIENT)
public class SolarSpectreEntityRenderer extends MobEntityRenderer<SolarSpectreEntity, SolarSpectreEntityModel<SolarSpectreEntity>>
{
	private static final Identifier TEXTURE = new Identifier(StarflightMod.MOD_ID, "textures/entity/solar_spectre.png");
	private static final Identifier EYES_TEXTURE = new Identifier(StarflightMod.MOD_ID, "textures/entity/solar_spectre_eyes.png");

	public SolarSpectreEntityRenderer(EntityRendererFactory.Context context)
	{
		super(context, new SolarSpectreEntityModel<SolarSpectreEntity>(context.getPart(StarflightModClient.MODEL_SOLAR_SPECTRE_LAYER)), 0.5f);
	}

	@Override
	public void render(SolarSpectreEntity entity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i)
	{
        if(((Entity) entity).isInvisible())
            return;
        
        if(entity.clientQuaternionPrevious != null && entity.clientQuaternion != null)
		{
			Quaternionf quaternion = QuaternionUtil.interpolate(entity.clientQuaternionPrevious, entity.clientQuaternion, g);
			matrixStack.multiply(quaternion);
		}
		else if(entity.clientQuaternion != null)
			matrixStack.multiply(entity.clientQuaternion);
        
        matrixStack.multiply(RotationAxis.POSITIVE_Z.rotation((MathHelper.lerp(g, entity.clientRollExtraPrevious, entity.clientRollExtra))));   
        float alpha = 0.8f;
        
        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getEntityTranslucentEmissive(this.getTexture(entity)));
        //((SolarSpectreEntityModel<SolarSpectreEntity>) this.getModel()).copyStateTo(this.model);
        //this.model.setAngles(entity, entity.limbAngle, entity.limbDistance, getAnimationProgress(entity, g), 0.0f, 0.0f);
        this.model.render(matrixStack, vertexConsumer, i, LivingEntityRenderer.getOverlay(entity, 0.0f), 1.0f, 1.0f, 1.0f, alpha);
        vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getEyes(EYES_TEXTURE));
        this.model.render(matrixStack, vertexConsumer, i, LivingEntityRenderer.getOverlay(entity, 0.0f), 1.0f, 1.0f, 1.0f, alpha);
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