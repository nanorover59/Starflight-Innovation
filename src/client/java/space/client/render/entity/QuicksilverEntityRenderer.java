package space.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.MagmaCubeEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.MagmaCubeEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import space.StarflightMod;
import space.entity.QuicksilverEntity;

@Environment(EnvType.CLIENT)
public class QuicksilverEntityRenderer extends MobEntityRenderer<QuicksilverEntity, MagmaCubeEntityModel<QuicksilverEntity>>
{
	private static final Identifier TEXTURE = Identifier.of(StarflightMod.MOD_ID, "textures/entity/quicksilver.png");

	public QuicksilverEntityRenderer(EntityRendererFactory.Context context)
	{
		super(context, new MagmaCubeEntityModel<>(context.getPart(EntityModelLayers.MAGMA_CUBE)), 0.25F);
	}

	protected int getBlockLight(MagmaCubeEntity magmaCubeEntity, BlockPos blockPos)
	{
		return 15;
	}

	public Identifier getTexture(QuicksilverEntity magmaCubeEntity)
	{
		return TEXTURE;
	}

	public void render(QuicksilverEntity magmaCubeEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i)
	{
		this.shadowRadius = 0.25F * (float) magmaCubeEntity.getSize();
		super.render(magmaCubeEntity, f, g, matrixStack, vertexConsumerProvider, i);
	}

	protected void scale(QuicksilverEntity magmaCubeEntity, MatrixStack matrixStack, float f)
	{
		int i = magmaCubeEntity.getSize();
		float g = MathHelper.lerp(f, magmaCubeEntity.lastStretch, magmaCubeEntity.stretch) / ((float) i * 0.5F + 1.0F);
		float h = 1.0F / (g + 1.0F);
		matrixStack.scale(h * (float) i, 1.0F / h * (float) i, h * (float) i);
	}
}