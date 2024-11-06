package space.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.util.math.MatrixStack;
import space.client.render.entity.model.BlockShellEntityModel;
import space.entity.BlockShellEntity;

@Environment(EnvType.CLIENT)
public class BlockShellFeatureRenderer extends FeatureRenderer<BlockShellEntity, BlockShellEntityModel<BlockShellEntity>>
{
	private final BlockRenderManager blockRenderManager;

	public BlockShellFeatureRenderer(FeatureRendererContext<BlockShellEntity, BlockShellEntityModel<BlockShellEntity>> context, BlockRenderManager blockRenderManager)
	{
		super(context);
		this.blockRenderManager = blockRenderManager;
	}

	public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, BlockShellEntity blockShellEntity, float f, float g, float h, float j, float k, float l)
	{
		BlockState blockState = blockShellEntity.getWearingBlock();
		
		if(blockState != null)
		{
			matrixStack.push();
			matrixStack.translate(-0.5F, blockShellEntity.isHiding() ? 0.5F : 0.25F, -0.5F);
			this.blockRenderManager.renderBlockAsEntity(blockState, matrixStack, vertexConsumerProvider, i, OverlayTexture.DEFAULT_UV);
			matrixStack.pop();
		}
	}
}