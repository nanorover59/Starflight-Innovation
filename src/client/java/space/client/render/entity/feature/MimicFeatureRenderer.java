package space.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import space.client.render.entity.model.MimicEntityModel;
import space.entity.MimicEntity;

@Environment(EnvType.CLIENT)
public class MimicFeatureRenderer extends FeatureRenderer<MimicEntity, MimicEntityModel<MimicEntity>>
{
	private final BlockRenderManager blockRenderManager;
	private final ModelPart body;

	public MimicFeatureRenderer(FeatureRendererContext<MimicEntity, MimicEntityModel<MimicEntity>> context, BlockRenderManager blockRenderManager)
	{
		super(context);
		this.blockRenderManager = blockRenderManager;
		this.body = context.getModel().getPart().getChild("body");
	}

	public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, MimicEntity mimicEntity, float f, float g, float h, float j, float k, float l)
	{
		BlockState blockState = mimicEntity.getWearingBlock();
		
		if(blockState != null)
		{
			matrixStack.push();
			matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180.0F));
			matrixStack.translate(-0.5F, -body.pivotY / 16.0f, -0.5F);
			this.blockRenderManager.renderBlockAsEntity(blockState, matrixStack, vertexConsumerProvider, i, OverlayTexture.DEFAULT_UV);
			matrixStack.pop();
		}
	}
}