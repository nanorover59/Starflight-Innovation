package space.client.render;

import java.util.HashMap;
import java.util.Map;

import org.joml.Math;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BeaconBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public class BeamEffectRenderer
{
	private static final Identifier BEAM_TEXTURE = Identifier.ofVanilla("textures/entity/beacon_beam.png");
	public static Map<Integer, BeamEffectRenderer> entityBeamMap = new HashMap<>();
	Entity sourceEntity;
	Entity targetEntity;
	Vec3d sourcePos;
	Vec3d direction;
	private int color;
	public int duration;
	
	public BeamEffectRenderer(Entity sourceEntity, Entity targetEntity, Vec3d sourcePos, Vec3d direction, int color, int duration)
	{
		this.sourceEntity = sourceEntity;
		this.targetEntity = targetEntity;
		this.sourcePos = sourcePos;
		this.direction = direction;
		this.color = color;
		this.duration = duration;
	}
	
	public void renderBeam(World world, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, float delta)
	{
		MinecraftClient client = MinecraftClient.getInstance();
		Vec3d cam = client.gameRenderer.getCamera().getPos();
		Vec3d source = getSource(delta);
		Vec3d rayStart = sourceEntity != null ? fromLerpedPosition(sourceEntity, sourceEntity.getEyeHeight(sourceEntity.getPose()), delta) : source;
		Vec3d direction = getDirection(delta);
		HitResult ray = raycast(world, rayStart, direction, 256.0);
		Vec3d end = source.add(direction.multiply(ray.getPos().subtract(source).length() + 0.5));
		
		matrixStack.push();
		matrixStack.translate(source.getX() - cam.getX(), source.getY() - cam.getY(), source.getZ() - cam.getZ());
		Vec3d diff = end.subtract(source);
		float m = (float) diff.length();
		diff = diff.normalize();
		float n = (float) Math.acos(diff.y);
		float o = (float) Math.atan2(diff.z, diff.x);
		matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(((float) (Math.PI / 2) - o) * (180.0F / (float) Math.PI)));
		matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(n * (180.0F / (float) Math.PI)));
		matrixStack.translate(-0.5, 0.0, -0.5);
		BeaconBlockEntityRenderer.renderBeam(matrixStack, vertexConsumerProvider, BEAM_TEXTURE, delta, 1.0f, world.getTime(), 0, (int) m, color, 0.1f, 0.15f);
		matrixStack.pop();
	}
	
	private HitResult raycast(World world, Vec3d start, Vec3d direction, double maxDistance)
	{
		Vec3d end = start.add(direction.multiply(maxDistance));

		// 1. Block Raycast
		BlockHitResult blockHit = world.raycast(new RaycastContext(start, end, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, sourceEntity));
		double blockDist = blockHit != null ? blockHit.getPos().squaredDistanceTo(start) : Double.MAX_VALUE;

		// 2. Entity Raycast
		Box box = sourceEntity.getBoundingBox().stretch(direction.multiply(maxDistance)).expand(1.0);
		EntityHitResult entityHit = ProjectileUtil.getEntityCollision(world, sourceEntity, start, end, box, target -> !target.isSpectator() && target.isAlive());
		double entityDist = entityHit != null ? entityHit.getPos().squaredDistanceTo(start) : Double.MAX_VALUE;

		// 3. Return whichever is closer
		return entityDist < blockDist ? entityHit : blockHit;
	}
	
	private Vec3d getSource(float delta)
	{
		if(sourceEntity == null)
			return sourcePos;
		else
		{
			Vec3d offset = new Vec3d(sourcePos.getX(), sourcePos.getY(), sourcePos.getZ());
			
			if(sourceEntity instanceof PlayerEntity)
			{
				MinecraftClient client = MinecraftClient.getInstance();
				double xOffset = 0.0;
				
				if(sourceEntity.getUuid() == client.player.getUuid() && client.options.getPerspective() == Perspective.FIRST_PERSON)
					xOffset = -0.35;
				
				offset = new Vec3d(xOffset, 0.0, 0.4);
				offset = offset.rotateX(sourceEntity.getPitch(delta) * -MathHelper.RADIANS_PER_DEGREE);
				offset = offset.rotateY(sourceEntity.getYaw(delta) * -MathHelper.RADIANS_PER_DEGREE);
				offset = offset.add(0.0, sourceEntity.getHeight() * 0.85, 0.0);
			}
			else if(sourceEntity instanceof LivingEntity)
				offset = offset.rotateY(MathHelper.lerpAngleDegrees(delta, ((LivingEntity) sourceEntity).prevBodyYaw, ((LivingEntity) sourceEntity).bodyYaw) * -MathHelper.RADIANS_PER_DEGREE);
			
			return fromLerpedPosition(sourceEntity, 0.0, delta).add(offset);
		}
	}
	
	private Vec3d getDirection(float delta)
	{
		if(targetEntity != null)
			return fromLerpedPosition(targetEntity, targetEntity.getHeight() * 0.5f, delta).subtract(getSource(delta)).normalize();
		else if(sourceEntity != null)
			return sourceEntity.getRotationVec(delta);
		else
			return direction;
	}
	
	private Vec3d fromLerpedPosition(Entity entity, double yOffset, float delta)
	{
		double d = MathHelper.lerp((double) delta, entity.lastRenderX, entity.getX());
		double e = MathHelper.lerp((double) delta, entity.lastRenderY, entity.getY()) + yOffset;
		double f = MathHelper.lerp((double) delta, entity.lastRenderZ, entity.getZ());
		return new Vec3d(d, e, f);
	}
}