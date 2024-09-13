package space.client.render.entity.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import space.entity.AncientHumanoidEntity;

@Environment(value = EnvType.CLIENT)
public class AncientHumanoidEntityModel<T extends AncientHumanoidEntity> extends BipedEntityModel<T>
{
	public AncientHumanoidEntityModel(ModelPart modelPart)
	{
		super(modelPart);
	}

	public static TexturedModelData getTexturedModelData()
	{
		Dilation dilation = Dilation.NONE;
		float pivotOffsetY = 0.0f;
		ModelData modelData = BipedEntityModel.getModelData(dilation, pivotOffsetY);
		return TexturedModelData.of(modelData, 64, 64);
	}
}