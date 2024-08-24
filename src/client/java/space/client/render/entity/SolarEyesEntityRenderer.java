package space.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import space.StarflightMod;
import space.entity.SolarEyesEntity;

@Environment(value=EnvType.CLIENT)
public class SolarEyesEntityRenderer extends EntityRenderer<SolarEyesEntity>
{
	private static final Identifier TEXTURE = Identifier.of(StarflightMod.MOD_ID, "textures/entity/solar_eyes.png");
	private static final RenderLayer SOLAR_EYES = RenderLayer.getEntityTranslucentEmissive(TEXTURE);
	
	public SolarEyesEntityRenderer(EntityRendererFactory.Context context)
	{
        super(context);
        this.shadowRadius = 0.0f;
    }
	
	@Override
    public void render(SolarEyesEntity solarEyesEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i)
	{
		if(solarEyesEntity.getAngerTime() > 0)
		{
			matrixStack.push();
	        int j = solarEyesEntity.getAngerTime() / 4;
	        float h = (float)(j % 4 * 16 + 0) / 64.0f;
	        float k = (float)(j % 4 * 16 + 16) / 64.0f;
	        float l = (float)(j / 4 * 16 + 0) / 64.0f;
	        float m = (float)(j / 4 * 16 + 16) / 64.0f;
	        //matrixStack.translate(0.0f, 0.1f, 0.0f);
	        matrixStack.multiply(this.dispatcher.getRotation());
	        //matrixStack.scale(0.5f, 0.5f, 0.5f);
	        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(SOLAR_EYES);
	        MatrixStack.Entry entry = matrixStack.peek();
	        vertex(vertexConsumer, entry, -0.5f, -0.25f, h, m, i);
	        vertex(vertexConsumer, entry, 0.5f, -0.25f, k, m, i);
	        vertex(vertexConsumer, entry, 0.5f, 0.75f, k, l, i);
	        vertex(vertexConsumer, entry, -0.5f, 0.75f, h, l, i);
	        matrixStack.pop();
		}
		
		super.render(solarEyesEntity, f, g, matrixStack, vertexConsumerProvider, i);
	}
	
	private static void vertex(VertexConsumer vertexConsumer, MatrixStack.Entry matrix, float x, float y, float u, float v, int light)
	{
        vertexConsumer.vertex(matrix, x, y, 0.0f).color(255, 255, 255, 255).texture(u, v).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(matrix, 0.0f, 1.0f, 0.0f);
    }

	@Override
	public Identifier getTexture(SolarEyesEntity entity)
	{
		return null;
	}
}