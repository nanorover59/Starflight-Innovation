package space.client.render.entity;

import java.util.ArrayList;
import java.util.UUID;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import space.entity.MovingCraftEntity;
import space.vessel.MovingCraftBlockRenderData;
import space.vessel.MovingCraftRenderList;

@Environment(value=EnvType.CLIENT)
public class MovingCraftEntityRenderer extends EntityRenderer<MovingCraftEntity>
{
	public MovingCraftEntityRenderer(EntityRendererFactory.Context context)
	{
        super(context);
        this.shadowRadius = 0.0f;
    }
	
	@Override
    public void render(MovingCraftEntity entity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i)
	{
		UUID entityUUID = entity.getUuid();
		
		if(!MovingCraftRenderList.hasBlocksForEntity(entityUUID))
			return;
		
		if(entity.clientQuaternionPrevious != null && entity.clientQuaternion != null)
		{
			Quaternion quaternion = interpolate(entity.clientQuaternionPrevious, entity.clientQuaternion, g);
			matrixStack.multiply(quaternion);
		}
		else if(entity.clientQuaternion != null)
			matrixStack.multiply(entity.clientQuaternion);
		
		ArrayList<MovingCraftBlockRenderData> blockList = MovingCraftRenderList.getBlocksForEntity(entityUUID);
		World world = entity.getEntityWorld();
		Random random = world.getRandom();
		BlockPos centerBlockPos = entity.getBlockPos();
		BlockPos centerBlockPosInitial = entity.getInitialBlockPos();
		int lightLevel = WorldRenderer.getLightmapCoordinates(world, Blocks.AIR.getDefaultState(), centerBlockPos);
		
		for(MovingCraftBlockRenderData blockData : blockList)
			blockData.renderBlock(world, entity, matrixStack, vertexConsumerProvider, random, centerBlockPos, centerBlockPosInitial, lightLevel, entity.getCraftYaw());
	}

	@SuppressWarnings("deprecation")
	@Override
	public Identifier getTexture(MovingCraftEntity var1)
	{
		return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
	}
	
	private Quaternion interpolate(Quaternion q0, Quaternion q1, float t)
	{
		float dot = q0.getX() * q1.getX() + q0.getY() * q1.getY() + q0.getZ() * q1.getZ() + q0.getW() * q1.getW();
		dot *= MathHelper.fastInverseSqrt(q0.getX() * q0.getX() + q0.getY() * q0.getY() + q0.getZ() * q0.getZ() + q0.getW() * q0.getW());
		dot *= MathHelper.fastInverseSqrt(q1.getX() * q1.getX() + q1.getY() * q1.getY() + q1.getZ() * q1.getZ() + q1.getW() * q1.getW());
		
		// Ensure that the interpolation is taking the shortest path.
		if(dot < 0.0f)
		{
			q1 = new Quaternion(-q1.getX(), -q1.getY(), -q1.getZ(), -q1.getW());
			dot = -dot;
		}
		
		float theta = (float) Math.acos(dot);
		float sinTheta = (float) Math.sin(theta);
		
		if(sinTheta != 0.0f)
		{
			float x = (q0.getX() * (float)Math.sin((1 - t) * theta) + q1.getX() * (float)Math.sin(t * theta)) / sinTheta;
			float y = (q0.getY() * (float)Math.sin((1 - t) * theta) + q1.getY() * (float)Math.sin(t * theta)) / sinTheta;
			float z = (q0.getZ() * (float)Math.sin((1 - t) * theta) + q1.getZ() * (float)Math.sin(t * theta)) / sinTheta;
			float w = (q0.getW() * (float)Math.sin((1 - t) * theta) + q1.getW() * (float)Math.sin(t * theta)) / sinTheta;
			return new Quaternion(x, y, z, w);
		}
		else
			return q0;
	}
}