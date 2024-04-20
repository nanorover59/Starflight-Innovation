package space.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import space.StarflightMod;
import space.client.StarflightModClient;
import space.client.render.entity.model.StratofishEntityModel;
import space.entity.StratofishEntity;

@Environment(value = EnvType.CLIENT)
public class StratofishEntityRenderer extends MobEntityRenderer<StratofishEntity, StratofishEntityModel<StratofishEntity>>
{
	private static final Identifier TEXTURE = new Identifier(StarflightMod.MOD_ID, "textures/entity/stratofish.png");

	public StratofishEntityRenderer(EntityRendererFactory.Context context)
	{
		super(context, new StratofishEntityModel<StratofishEntity>(context.getPart(StarflightModClient.MODEL_STRATOFISH_LAYER)), 0.5f);
	}
	
	@Override
	protected void scale(StratofishEntity StratofishEntity, MatrixStack matrixStack, float f)
	{
		matrixStack.scale(2.0f, 2.0f, 2.0f);
	}

	@Override
	public Identifier getTexture(StratofishEntity stratofishEntity)
	{
		return TEXTURE;
	}
}