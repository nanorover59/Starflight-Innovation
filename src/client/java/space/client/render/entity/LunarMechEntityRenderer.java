package space.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BeaconBlockEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import space.StarflightMod;
import space.client.StarflightModClient;
import space.client.render.StarflightRenderEffects;
import space.client.render.entity.feature.LunarMechEmissiveFeatureRenderer;
import space.client.render.entity.model.LunarMechEntityModel;
import space.entity.LunarMechEntity;

@Environment(EnvType.CLIENT)
public class LunarMechEntityRenderer extends MobEntityRenderer<LunarMechEntity, LunarMechEntityModel<LunarMechEntity>>
{
	private static final Identifier TEXTURE = Identifier.of(StarflightMod.MOD_ID, "textures/entity/lunar_mech.png");
	private static final Identifier BEAM_TEXTURE = Identifier.ofVanilla("textures/entity/beacon_beam.png");

	public LunarMechEntityRenderer(EntityRendererFactory.Context context)
	{
		super(context, new LunarMechEntityModel<LunarMechEntity>(context.getPart(StarflightModClient.MODEL_LUNAR_MECH_LAYER)), 0.8f);
		this.addFeature(new LunarMechEmissiveFeatureRenderer<LunarMechEntity>(this));
	}
	
	private Vec3d fromLerpedPosition(LivingEntity entity, double yOffset, float delta)
	{
		double d = MathHelper.lerp((double) delta, entity.lastRenderX, entity.getX());
		double e = MathHelper.lerp((double) delta, entity.lastRenderY, entity.getY()) + yOffset;
		double f = MathHelper.lerp((double) delta, entity.lastRenderZ, entity.getZ());
		return new Vec3d(d, e, f);
	}
	
	public void render(LunarMechEntity entity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i)
	{
		super.render(entity, f, g, matrixStack, vertexConsumerProvider, i);
		LivingEntity livingEntity = entity.getBeamTarget();
		
		if(livingEntity != null)
		{
			if(livingEntity instanceof ClientPlayerEntity)
				StarflightRenderEffects.radiation = 4.0f;
			
			long worldTime = entity.getWorld().getTime();
			Vec3d sourceOffset = new Vec3d(-1.2, 3.25, 2.0).rotateY(MathHelper.lerpAngleDegrees(g, entity.prevBodyYaw, entity.bodyYaw) * -MathHelper.RADIANS_PER_DEGREE);
			matrixStack.push();
			matrixStack.translate(sourceOffset.getX(), sourceOffset.getY(), sourceOffset.getZ());
			Vec3d vec3d = this.fromLerpedPosition(livingEntity, (double) livingEntity.getHeight() * 0.5, g);
			Vec3d vec3d2 = this.fromLerpedPosition(entity, 0.0, g).add(sourceOffset);
			Vec3d vec3d3 = vec3d.subtract(vec3d2);
			float m = (float) (vec3d3.length() + 1.0);
			vec3d3 = vec3d3.normalize();
			float n = (float) Math.acos(vec3d3.y);
			float o = (float) Math.atan2(vec3d3.z, vec3d3.x);
			matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(((float) (Math.PI / 2) - o) * (180.0F / (float) Math.PI)));
			matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(n * (180.0F / (float) Math.PI)));
			matrixStack.translate(-0.5, 0.0, -0.5);
			BeaconBlockEntityRenderer.renderBeam(matrixStack, vertexConsumerProvider, BEAM_TEXTURE, f, 1.0f, worldTime, 0, (int) m, 0xFFFFFF, 0.2f, 0.25f);
			matrixStack.pop();
		}
	}

	public Identifier getTexture(LunarMechEntity entity)
	{
		return TEXTURE;
	}
}