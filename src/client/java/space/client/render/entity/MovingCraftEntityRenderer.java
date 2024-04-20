package space.client.render.entity;

import java.util.ArrayList;
import java.util.UUID;

import org.joml.Quaternionf;

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
			matrixStack.multiply(new Quaternionf(entity.clientQuaternionPrevious).slerp(entity.clientQuaternion, g));
		else if(entity.clientQuaternion != null)
			matrixStack.multiply(entity.clientQuaternion);
		
		ArrayList<MovingCraftBlockRenderData> blockList = MovingCraftRenderList.getBlocksForEntity(entityUUID);
		World world = entity.getEntityWorld();
		Random random = world.getRandom();
		BlockPos centerBlockPos = entity.getBlockPos();
		BlockPos centerBlockPosInitial = entity.getInitialBlockPos();
		int lightLevel = WorldRenderer.getLightmapCoordinates(world, Blocks.AIR.getDefaultState(), centerBlockPos);
		
		for(MovingCraftBlockRenderData blockData : blockList)
			blockData.renderBlock(world, matrixStack, vertexConsumerProvider, random, centerBlockPos, centerBlockPosInitial, lightLevel, entity.getYaw());
	}

	@SuppressWarnings("deprecation")
	@Override
	public Identifier getTexture(MovingCraftEntity var1)
	{
		return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
	}
}