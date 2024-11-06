package space.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;
import space.StarflightMod;
import space.client.StarflightModClient;
import space.client.render.entity.feature.BlockShellFeatureRenderer;
import space.client.render.entity.model.BlockShellEntityModel;
import space.entity.BlockShellEntity;

@Environment(value = EnvType.CLIENT)
public class BlockShellEntityRenderer extends MobEntityRenderer<BlockShellEntity, BlockShellEntityModel<BlockShellEntity>>
{
	private static final Identifier TEXTURE = Identifier.of(StarflightMod.MOD_ID, "textures/entity/block_shell.png");

	public BlockShellEntityRenderer(EntityRendererFactory.Context context)
	{
		super(context, new BlockShellEntityModel<BlockShellEntity>(context.getPart(StarflightModClient.MODEL_BLOCK_SHELL_LAYER)), 0.5f);
		this.addFeature(new BlockShellFeatureRenderer(this, context.getBlockRenderManager()));
	}

	@Override
	protected RenderLayer getRenderLayer(BlockShellEntity entity, boolean showBody, boolean translucent, boolean showOutline)
	{
		Identifier identifier = this.getTexture(entity);
		
		if(showBody)
			return RenderLayer.getEntityTranslucentEmissive(identifier);
		else
			return showOutline ? RenderLayer.getOutline(identifier) : null;
	}

	@Override
	public Identifier getTexture(BlockShellEntity entity)
	{
		return TEXTURE;
	}
}