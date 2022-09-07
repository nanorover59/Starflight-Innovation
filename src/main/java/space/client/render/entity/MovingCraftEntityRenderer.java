package space.client.render.entity;

import java.util.ArrayList;
import java.util.UUID;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
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
		
		float rotationRoll = entity.getCraftRoll();
		float rotationPitch = entity.getCraftPitch();
		float rotationYaw = entity.getCraftYaw();
		
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
		BlockPos centerBlockPos = entity.getBlockPos();
		BlockPos centerBlockPosInitial = entity.getInitialBlockPos();
		Random random = Random.create();
		
		for(MovingCraftBlockRenderData blockData : blockList)
			blockData.renderBlock(world, entity, matrixStack, vertexConsumerProvider, random, centerBlockPos, centerBlockPosInitial);
	}

	@SuppressWarnings("deprecation")
	@Override
	public Identifier getTexture(MovingCraftEntity var1)
	{
		return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
	}
}