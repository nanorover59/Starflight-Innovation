package space.client.render.block.entity;

import org.joml.Matrix4f;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.MathHelper;
import space.block.entity.WaterTankBlockEntity;
import space.util.FluidResourceType;

@Environment(EnvType.CLIENT)
public class WaterTankBlockEntityRenderer implements BlockEntityRenderer<WaterTankBlockEntity>
{
	private final Sprite WATER_SPRITE = MinecraftClient.getInstance().getBakedModelManager().getBlockModels().getModel(Blocks.WATER.getDefaultState()).getParticleSprite();
	
	public WaterTankBlockEntityRenderer(BlockEntityRendererFactory.Context ctx)
	{
		
	}

	@Override
	public void render(WaterTankBlockEntity blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay)
	{
		float b = 0.001f;
        float l = blockEntity.canRenderBottom() ? b : 0.0f;
        float h = (float) blockEntity.getFluid(FluidResourceType.WATER) / (float) blockEntity.getFluidCapacity(FluidResourceType.WATER);
        float u1 = WATER_SPRITE.getMinU();
        float v1 = WATER_SPRITE.getMinV();
        float u2 = WATER_SPRITE.getMaxU();
        float v2 = WATER_SPRITE.getMaxV();
        
        if(blockEntity.getFluid(FluidResourceType.WATER) == 0.0f || h == 0.0f)
        	return;
		
		matrices.push();
        MatrixStack.Entry entry = matrices.peek();
        Matrix4f matrix4f = entry.getPositionMatrix();
        VertexConsumer vertexConsumer = vertices.getBuffer(RenderLayers.getFluidLayer(Fluids.WATER.getDefaultState()));
        
        // Up
        if(blockEntity.canRenderTop())
        {
        	if(h == 1.0f)
        		h -= b;
        	
        	vertex(matrix4f, entry, vertexConsumer, light, overlay, b, h, b, u1, v1);
        	vertex(matrix4f, entry, vertexConsumer, light, overlay, b, h, 1.0f - b, u2, v1);
        	vertex(matrix4f, entry, vertexConsumer, light, overlay, 1.0f - b, h, 1.0f - b, u2, v2);
        	vertex(matrix4f, entry, vertexConsumer, light, overlay, 1.0f - b, h, b, u1, v2);
        }
        
        // Down
        if(blockEntity.canRenderBottom())
        {
	        vertex(matrix4f, entry, vertexConsumer, light, overlay, b, b, b, u1, v1);
	        vertex(matrix4f, entry, vertexConsumer, light, overlay, 1.0f - b, b, b, u2, v1);
	        vertex(matrix4f, entry, vertexConsumer, light, overlay, 1.0f - b, b, 1.0f - b, u2, v2);
	        vertex(matrix4f, entry, vertexConsumer, light, overlay, b, b, 1.0f - b, u1, v2);
        }
        
        v1 = MathHelper.lerp(h, v2, v1);
        
        // East
        vertex(matrix4f, entry, vertexConsumer, light, overlay, 1.0f - b, h, b, u1, v1);
        vertex(matrix4f, entry, vertexConsumer, light, overlay, 1.0f - b, h, 1.0f - b, u2, v1);
        vertex(matrix4f, entry, vertexConsumer, light, overlay, 1.0f - b, l, 1.0f - b, u2, v2);
        vertex(matrix4f, entry, vertexConsumer, light, overlay, 1.0f - b, l, b, u1, v2);
        
        // West
        vertex(matrix4f, entry, vertexConsumer, light, overlay, b, h, 1.0f - b, u1, v1);
        vertex(matrix4f, entry, vertexConsumer, light, overlay, b, h, b, u2, v1);
        vertex(matrix4f, entry, vertexConsumer, light, overlay, b, l, b, u2, v2);
        vertex(matrix4f, entry, vertexConsumer, light, overlay, b, l, 1.0f - b, u1, v2);
        
        // North
        vertex(matrix4f, entry, vertexConsumer, light, overlay, b, h, b, u1, v1);
        vertex(matrix4f, entry, vertexConsumer, light, overlay, 1.0f - b, h, b, u2, v1);
        vertex(matrix4f, entry, vertexConsumer, light, overlay, 1.0f - b, l, b, u2, v2);
        vertex(matrix4f, entry, vertexConsumer, light, overlay, b, l, b, u1, v2);
        
        // South
        vertex(matrix4f, entry, vertexConsumer, light, overlay, 1.0f - b, h, 1.0f - b, u1, v1);
        vertex(matrix4f, entry, vertexConsumer, light, overlay, b, h, 1.0f - b, u2, v1);
        vertex(matrix4f, entry, vertexConsumer, light, overlay, b, l, 1.0f - b, u2, v2);
        vertex(matrix4f, entry, vertexConsumer, light, overlay, 1.0f - b, l, 1.0f - b, u1, v2);
        
        matrices.pop();
	}
	
	private void vertex(Matrix4f positionMatrix, MatrixStack.Entry entry, VertexConsumer vertexConsumer, int light, int overlay, float x, float y, float z, float u, float v)
	{
		vertexConsumer.vertex(positionMatrix, x, y, z).color(0.2471f, 0.4627f, 0.8941f, 1.0f).texture(u, v).overlay(overlay).light(light).normal(entry, 0.0f, 1.0f, 0.0f);
    }
}