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
import net.minecraft.util.math.Vec3f;
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
		
		float rotationRoll = lerpAngleRadians(g, entity.clientCraftRollPrevious, entity.clientCraftRoll);
		float rotationPitch = lerpAngleRadians(g, entity.clientCraftPitchPrevious, entity.clientCraftPitch);
		float rotationYaw = lerpAngleRadians(g, entity.clientCraftYawPrevious, entity.clientCraftYaw);
		
		switch(entity.getForwardDirection())
		{
		case NORTH:
			matrixStack.multiply(Vec3f.NEGATIVE_Y.getRadialQuaternion(rotationYaw));
			matrixStack.multiply(Vec3f.NEGATIVE_X.getRadialQuaternion(rotationPitch));
			matrixStack.multiply(Vec3f.NEGATIVE_Z.getRadialQuaternion(rotationRoll));
			break;
		case EAST:
			matrixStack.multiply(Vec3f.POSITIVE_Y.getRadialQuaternion(rotationYaw));
			matrixStack.multiply(Vec3f.POSITIVE_Z.getRadialQuaternion(rotationPitch));
			matrixStack.multiply(Vec3f.POSITIVE_X.getRadialQuaternion(rotationRoll));
			break;
		case SOUTH:
			matrixStack.multiply(Vec3f.POSITIVE_Y.getRadialQuaternion(rotationYaw));
			matrixStack.multiply(Vec3f.POSITIVE_X.getRadialQuaternion(rotationPitch));
			matrixStack.multiply(Vec3f.POSITIVE_Z.getRadialQuaternion(rotationRoll));
			break;
		case WEST:
			matrixStack.multiply(Vec3f.NEGATIVE_Y.getRadialQuaternion(rotationYaw));
			matrixStack.multiply(Vec3f.NEGATIVE_Z.getRadialQuaternion(rotationPitch));
			matrixStack.multiply(Vec3f.NEGATIVE_X.getRadialQuaternion(rotationRoll));
			break;
		default:
			break;
		}
		
		ArrayList<MovingCraftBlockRenderData> blockList = MovingCraftRenderList.getBlocksForEntity(entityUUID);
		World world = entity.getEntityWorld();
		Random random = world.getRandom();
		BlockPos centerBlockPos = entity.getBlockPos();
		BlockPos centerBlockPosInitial = entity.getInitialBlockPos();
		int lightLevel = WorldRenderer.getLightmapCoordinates(world, Blocks.AIR.getDefaultState(), centerBlockPos);
		
		for(MovingCraftBlockRenderData blockData : blockList)
			blockData.renderBlock(world, entity, matrixStack, vertexConsumerProvider, random, centerBlockPos, centerBlockPosInitial, lightLevel, rotationYaw);
	}

	@SuppressWarnings("deprecation")
	@Override
	public Identifier getTexture(MovingCraftEntity var1)
	{
		return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
	}
	
	private float lerpAngleRadians(float delta, float start, float end)
	{
		float d = end - start;
		
		if(Math.abs(d) > MathHelper.PI)
		{
			if(start > end)
				d += MathHelper.PI * 2.0f;
			else
				d -= MathHelper.PI * 2.0f;
		}
		
        return start + delta * d;
    }
}