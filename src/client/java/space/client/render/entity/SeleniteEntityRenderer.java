package space.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;
import space.StarflightMod;
import space.client.StarflightModClient;
import space.client.render.entity.model.SeleniteEntityModel;
import space.entity.SeleniteEntity;

@Environment(value = EnvType.CLIENT)
public class SeleniteEntityRenderer extends MobEntityRenderer<SeleniteEntity, SeleniteEntityModel<SeleniteEntity>>
{
	private static final Identifier TEXTURE = Identifier.of(StarflightMod.MOD_ID, "textures/entity/selenite.png");

	public SeleniteEntityRenderer(EntityRendererFactory.Context context)
	{
		super(context, new SeleniteEntityModel<SeleniteEntity>(context.getPart(StarflightModClient.MODEL_SELENITE_LAYER)), 0.5f);
	}

	@Override
	public Identifier getTexture(SeleniteEntity seleniteEntity)
	{
		return TEXTURE;
	}
}