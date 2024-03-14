package space.client.render.entity.model;

import java.util.ArrayList;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.entity.Entity;

@Environment(value = EnvType.CLIENT)
public class SolarSpectreEntityModel<T extends Entity> extends SinglePartEntityModel<T>
{
	private static final String FIN_NAME = "fin_";
	private final ModelPart root;
	private final ArrayList<ModelPart> fins;

	public SolarSpectreEntityModel(ModelPart root)
	{
		this.root = root;
		this.fins = new ArrayList<ModelPart>();

		for(int i = 0; i < 6; i++)
			fins.add(root.getChild(FIN_NAME + i));
	}

	public static TexturedModelData getTexturedModelData()
	{
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		int i = 0;
		modelPartData.addChild("core", ModelPartBuilder.create().uv(108, 108).cuboid(-3.0F, -3.0F, -10.0F, 6.0F, 6.0F, 20.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 0.0F, 0.0F));
		modelPartData.addChild(FIN_NAME + i++, ModelPartBuilder.create().uv(0, 0).cuboid(-64.0F, -8.0F, 0.0F, 64.0F, 16.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -3.0F, 10.0F, 0.0F, 0.3927F, 1.5708F));
		modelPartData.addChild(FIN_NAME + i++, ModelPartBuilder.create().uv(0, 16).mirrored().cuboid(0.0F, -8.0F, 0.0F, 64.0F, 16.0F, 0.0F, new Dilation(0.0F)).mirrored(false), ModelTransform.of(0.0F, 3.0F, 10.0F, 0.0F, -0.3927F, 1.5708F));
		modelPartData.addChild(FIN_NAME + i++, ModelPartBuilder.create().uv(0, 128).cuboid(-32.0F, -2.0F, 0.0F, 32.0F, 4.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(-3.0F, 3.0F, -10.0F, 0.0F, 0.1745F, -0.3927F));
		modelPartData.addChild(FIN_NAME + i++, ModelPartBuilder.create().uv(128, 0).mirrored().cuboid(0.0F, -2.0F, 0.0F, 32.0F, 4.0F, 0.0F, new Dilation(0.0F)).mirrored(false), ModelTransform.of(3.0F, 3.0F, -10.0F, 0.0F, -0.1745F, 0.3927F));
		modelPartData.addChild(FIN_NAME + i++, ModelPartBuilder.create().uv(128, 4).mirrored().cuboid(0.0F, -2.0F, 0.0F, 32.0F, 4.0F, 0.0F, new Dilation(0.0F)).mirrored(false), ModelTransform.of(3.0F, -3.0F, -10.0F, 0.0F, -0.1745F, -0.3927F));
		modelPartData.addChild(FIN_NAME + i++, ModelPartBuilder.create().uv(128, 8).cuboid(-32.0F, -2.0F, 0.0F, 32.0F, 4.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(-3.0F, -3.0F, -10.0F, 0.0F, 0.1745F, 0.3927F));
		modelPartData.addChild(FIN_NAME + i++, ModelPartBuilder.create().uv(0, 32).cuboid(-64.0F, -8.0F, 0.0F, 64.0F, 16.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(-3.0F, 3.0F, 0.0F, 0.0F, 0.1745F, -0.7854F));
		modelPartData.addChild(FIN_NAME + i++, ModelPartBuilder.create().uv(0, 48).mirrored().cuboid(0.0F, -8.0F, 0.0F, 64.0F, 16.0F, 0.0F, new Dilation(0.0F)).mirrored(false), ModelTransform.of(3.0F, 0.0F, 10.0F, 0.0F, -0.3927F, 0.0F));
		modelPartData.addChild(FIN_NAME + i++, ModelPartBuilder.create().uv(0, 64).cuboid(-64.0F, -8.0F, 0.0F, 64.0F, 16.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(-3.0F, -3.0F, 0.0F, 0.0F, 0.1745F, 0.7854F));
		modelPartData.addChild(FIN_NAME + i++, ModelPartBuilder.create().uv(0, 80).mirrored().cuboid(0.0F, -8.0F, 0.0F, 64.0F, 16.0F, 0.0F, new Dilation(0.0F)).mirrored(false), ModelTransform.of(3.0F, -3.0F, 0.0F, 0.0F, -0.1745F, -0.7854F));
		modelPartData.addChild(FIN_NAME + i++, ModelPartBuilder.create().uv(0, 96).cuboid(-64.0F, -8.0F, 0.0F, 64.0F, 16.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(-3.0F, 0.0F, 10.0F, 0.0F, 0.3927F, 0.0F));
		modelPartData.addChild(FIN_NAME + i++, ModelPartBuilder.create().uv(0, 112).mirrored().cuboid(0.0F, -8.0F, 0.0F, 64.0F, 16.0F, 0.0F, new Dilation(0.0F)).mirrored(false), ModelTransform.of(3.0F, 3.0F, 0.0F, 0.0F, -0.1745F, 0.7854F));
		return TexturedModelData.of(modelData, 256, 256);
	}

	@Override
	public ModelPart getPart()
	{
		return this.root;
	}

	@Override
	public void setAngles(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
	{
	}
}