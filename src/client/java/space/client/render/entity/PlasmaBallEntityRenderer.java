package space.client.render.entity;

import org.joml.Quaternionf;

import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.RotationAxis;
import space.StarflightMod;
import space.client.StarflightModClient;
import space.entity.PlasmaBallEntity;

public class PlasmaBallEntityRenderer extends EntityRenderer<PlasmaBallEntity>
{
	private static final Identifier TEXTURE = Identifier.of(StarflightMod.MOD_ID, "textures/entity/plasma_ball.png");
	private static final RenderLayer PLASMA_BALL = RenderLayer.getEntityTranslucentEmissive(TEXTURE);
	private static final float SINE_45_DEGREES = (float) Math.sin(0.7853981633974483);
	private static final String GLASS = "glass";
	private final ModelPart core;
	private final ModelPart frame;

	public PlasmaBallEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.shadowRadius = 0.25f;
        ModelPart modelPart = context.getPart(StarflightModClient.MODEL_PLASMA_BALL_LAYER);
        this.frame = modelPart.getChild(GLASS);
        this.core = modelPart.getChild(EntityModelPartNames.CUBE);
    }

	public static TexturedModelData getTexturedModelData()
	{
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		modelPartData.addChild(GLASS, ModelPartBuilder.create().uv(0, 0).cuboid(-8.0f, -8.0f, -8.0f, 16.0f, 16.0f, 16.0f), ModelTransform.NONE);
		modelPartData.addChild(EntityModelPartNames.CUBE, ModelPartBuilder.create().uv(0, 0).cuboid(-8.0f, -8.0f, -8.0f, 16.0f, 16.0f, 16.0f), ModelTransform.NONE);
		return TexturedModelData.of(modelData, 64, 32);
	}

	@Override
	public void render(PlasmaBallEntity entity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i)
	{
		matrixStack.push();
		int argb = ColorHelper.Argb.fromFloats(0.5f, 1.0f, 1.0f, 1.0f);
		float j = ((float) entity.age + g) * 8.0f;
		VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(PLASMA_BALL);
		matrixStack.push();
		int k = OverlayTexture.DEFAULT_UV;
		matrixStack.translate(0.0f, 0.1f, 0.0f);
		matrixStack.scale(0.5f, 0.5f, 0.5f);
		matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(j));
		matrixStack.multiply(new Quaternionf().setAngleAxis(1.0471976f, SINE_45_DEGREES, 0.0f, SINE_45_DEGREES));
		this.frame.render(matrixStack, vertexConsumer, i, k, argb);
		matrixStack.scale(0.875f, 0.875f, 0.875f);
		matrixStack.multiply(new Quaternionf().setAngleAxis(1.0471976f, SINE_45_DEGREES, 0.0f, SINE_45_DEGREES));
		matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(j));
		this.frame.render(matrixStack, vertexConsumer, i, k, argb);
		matrixStack.scale(0.875f, 0.875f, 0.875f);
		matrixStack.multiply(new Quaternionf().setAngleAxis(1.0471976f, SINE_45_DEGREES, 0.0f, SINE_45_DEGREES));
		matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(j));
		this.core.render(matrixStack, vertexConsumer, i, k, argb);
		matrixStack.pop();
		matrixStack.pop();
		super.render(entity, f, g, matrixStack, vertexConsumerProvider, i);
	}

	@Override
	public Identifier getTexture(PlasmaBallEntity entity)
	{
		return TEXTURE;
	}
}