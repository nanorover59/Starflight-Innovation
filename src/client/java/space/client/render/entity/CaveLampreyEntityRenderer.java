package space.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;
import space.StarflightMod;
import space.client.StarflightModClient;
import space.client.render.entity.model.CaveLampreyEntityModel;
import space.entity.CaveLampreyEntity;

@Environment(value = EnvType.CLIENT)
public class CaveLampreyEntityRenderer extends MobEntityRenderer<CaveLampreyEntity, CaveLampreyEntityModel<CaveLampreyEntity>>
{
	private static final Identifier TEXTURE = Identifier.of(StarflightMod.MOD_ID, "textures/entity/cave_lamprey.png");

	public CaveLampreyEntityRenderer(EntityRendererFactory.Context context)
	{
		super(context, new CaveLampreyEntityModel<CaveLampreyEntity>(context.getPart(StarflightModClient.MODEL_CAVE_LAMPREY_LAYER)), 0.5f);
	}

	@Override
	public Identifier getTexture(CaveLampreyEntity entity)
	{
		return TEXTURE;
	}
}