package space.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;
import space.StarflightMod;
import space.client.StarflightModClient;
import space.client.render.entity.model.CeruleanEntityModel;
import space.entity.CeruleanEntity;

@Environment(value = EnvType.CLIENT)
public class CeruleanEntityRenderer extends MobEntityRenderer<CeruleanEntity, CeruleanEntityModel<CeruleanEntity>>
{
	private static final Identifier TEXTURE = new Identifier(StarflightMod.MOD_ID, "textures/entity/cerulean.png");

	public CeruleanEntityRenderer(EntityRendererFactory.Context context)
	{
		super(context, new CeruleanEntityModel<CeruleanEntity>(context.getPart(StarflightModClient.MODEL_CERULEAN_LAYER)), 0.5f);
	}

	@Override
	public Identifier getTexture(CeruleanEntity ceruleanEntity)
	{
		return TEXTURE;
	}
}