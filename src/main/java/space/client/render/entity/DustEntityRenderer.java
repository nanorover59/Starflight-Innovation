package space.client.render.entity;

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
import space.StarflightMod;
import space.client.StarflightModClient;
import space.client.render.entity.model.DustEntityModel;
import space.entity.DustEntity;

@Environment(value = EnvType.CLIENT)
public class DustEntityRenderer extends MobEntityRenderer<DustEntity, DustEntityModel<DustEntity>>
{
	private static final Identifier TEXTURE = new Identifier(StarflightMod.MOD_ID, "textures/entity/dust.png");

	public DustEntityRenderer(EntityRendererFactory.Context context)
	{
		super(context, new DustEntityModel<DustEntity>(context.getPart(StarflightModClient.MODEL_DUST_LAYER)), 0.5f);
	}

	@Override
	public void render(DustEntity dustEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i)
	{
        if(((Entity) dustEntity).isInvisible())
            return;
        
        float yaw = -MathHelper.lerpAngleDegrees(g, dustEntity.prevHeadYaw, dustEntity.headYaw);
        float pitch = MathHelper.lerpAngleDegrees(g, dustEntity.prevPitch, dustEntity.getPitch()) + 180.0f;
        double x = (double) dustEntity.getStamina() / (double) DustEntity.INITIAL_STAMINA; 
        float alpha = (float) (1.0 - Math.pow((x * 2.0) + 1.0, -3.0));
        
        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getEntityTranslucent(this.getTexture(dustEntity)));
        ((DustEntityModel<DustEntity>) this.getModel()).copyStateTo(this.model);
        this.model.animateModel(dustEntity, dustEntity.limbAngle, dustEntity.limbDistance, g);
        this.model.setAngles(dustEntity, dustEntity.limbAngle, dustEntity.limbDistance, getAnimationProgress(dustEntity, g), yaw, pitch);
        this.model.render(matrixStack, vertexConsumer, i, LivingEntityRenderer.getOverlay(dustEntity, 0.0f), 1.0f, 1.0f, 1.0f, alpha);
	}

	@Override
	public Identifier getTexture(DustEntity dustEntity)
	{
		return TEXTURE;
	}
}