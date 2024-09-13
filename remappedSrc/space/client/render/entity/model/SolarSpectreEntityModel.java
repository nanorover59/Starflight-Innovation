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
	private static final String WHISKER_NAME = "whisker_";
	private final ModelPart root;
	private final ArrayList<ModelPart> fins;
	private final ArrayList<ModelPart> whiskers;

	public SolarSpectreEntityModel(ModelPart root)
	{
		this.root = root;
		this.fins = new ArrayList<ModelPart>();
		this.whiskers = new ArrayList<ModelPart>();

		for(int i = 0; i < 6; i++)
			fins.add(root.getChild(FIN_NAME + i));

		for(int i = 0; i < 4; i++)
			whiskers.add(root.getChild(WHISKER_NAME + i));
	}

	public static TexturedModelData getTexturedModelData()
	{
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		float offset = -3.0f;
		int i = 0;
		modelPartData.addChild("core", ModelPartBuilder.create().uv(0, 0).cuboid(-3.0f, 0.0f, -12.0f, 6.0f, 6.0f, 24.0f, new Dilation(0.0f)), ModelTransform.pivot(0.0f, offset, 0.0f));
		modelPartData.addChild(FIN_NAME + i++, ModelPartBuilder.create().uv(0, 12).cuboid(0.0f, -6.0f, -48.0f, 0.0f, 12.0f, 48.0f, new Dilation(0.0f)), ModelTransform.of(-3.0f, 6.0f + offset, 8.0f, 3.0543f, -0.6981f, -0.8727f));
		modelPartData.addChild(FIN_NAME + i++, ModelPartBuilder.create().uv(0, 0).cuboid(0.0f, -6.0f, -48.0f, 0.0f, 12.0f, 48.0f, new Dilation(0.0f)), ModelTransform.of(3.0f, 6.0f + offset, 8.0f, -3.0543f, -0.6981f, -2.2689f));
		modelPartData.addChild(FIN_NAME + i++, ModelPartBuilder.create().uv(0, 0).cuboid(0.0f, -6.0f, -48.0f, 0.0f, 12.0f, 48.0f, new Dilation(0.0f)), ModelTransform.of(3.0f, offset, 8.0f, 3.0543f, -0.6981f, 2.2689f));
		modelPartData.addChild(FIN_NAME + i++, ModelPartBuilder.create().uv(0, 12).cuboid(0.0f, -6.0f, -48.0f, 0.0f, 12.0f, 48.0f, new Dilation(0.0f)), ModelTransform.of(-3.0f, offset, 8.0f, -3.0543f, -0.6981f, 0.8727f));
		modelPartData.addChild(FIN_NAME + i++, ModelPartBuilder.create().uv(0, 0).cuboid(0.0f, -6.0f, -48.0f, 0.0f, 12.0f, 48.0f, new Dilation(0.0f)), ModelTransform.of(3.0f, 3.0f + offset, 0.0f, -3.1416f, -0.7854f, 3.1416f));
		modelPartData.addChild(FIN_NAME + i++, ModelPartBuilder.create().uv(0, 12).cuboid(0.0f, -6.0f, -48.0f, 0.0f, 12.0f, 48.0f, new Dilation(0.0f)), ModelTransform.of(-3.0f, 3.0f + offset, 0.0f, 3.1416f, -0.7854f, 0.0f));
		modelPartData.addChild(FIN_NAME + i++, ModelPartBuilder.create().uv(0, 24).cuboid(0.0f, -6.0f, -48.0f, 0.0f, 12.0f, 48.0f, new Dilation(0.0f)), ModelTransform.of(3.0f, 6.0f + offset, -4.0f, -3.0543f, -1.0472f, -2.618f));
		modelPartData.addChild(FIN_NAME + i++, ModelPartBuilder.create().uv(0, 36).cuboid(0.0f, -6.0f, -48.0f, 0.0f, 12.0f, 48.0f, new Dilation(0.0f)), ModelTransform.of(-3.0f, 6.0f + offset, -4.0f, 3.0543f, -1.0472f, -0.5236f));
		modelPartData.addChild(FIN_NAME + i++, ModelPartBuilder.create().uv(0, 48).cuboid(0.0f, -6.0f, -48.0f, 0.0f, 12.0f, 48.0f, new Dilation(0.0f)), ModelTransform.of(-3.0f, offset, -4.0f, -3.0543f, -1.0472f, 0.5236f));
		modelPartData.addChild(FIN_NAME + i++, ModelPartBuilder.create().uv(0, 60).cuboid(0.0f, -6.0f, -48.0f, 0.0f, 12.0f, 48.0f, new Dilation(0.0f)), ModelTransform.of(3.0f, offset, -4.0f, 3.0543f, -1.0472f, 2.618f));
		i = 0;
		modelPartData.addChild(WHISKER_NAME + i++, ModelPartBuilder.create().uv(0, 30).cuboid(-12.0f, 0.0f, -2.0f, 12.0f, 0.0f, 4.0f, new Dilation(0.0f)), ModelTransform.of(-3.0f, offset, -8.0f, 1.4934f, -0.2443f, 0.7543f));
		modelPartData.addChild(WHISKER_NAME + i++, ModelPartBuilder.create().uv(24, 30).cuboid(-12.0f, 0.0f, -2.0f, 12.0f, 0.0f, 4.0f, new Dilation(0.0f)), ModelTransform.of(3.0f, offset, -8.0f, 1.6482f, -0.2443f, 2.3873f));
		modelPartData.addChild(WHISKER_NAME + i++, ModelPartBuilder.create().uv(32, 0).cuboid(-12.0f, 0.0f, -2.0f, 12.0f, 0.0f, 4.0f, new Dilation(0.0f)), ModelTransform.of(-3.0f, 6.0f + offset, -8.0f, 1.4934f, -0.2443f, -0.7543f));
		modelPartData.addChild(WHISKER_NAME + i++, ModelPartBuilder.create().uv(32, 4).cuboid(-12.0f, 0.0f, -2.0f, 12.0f, 0.0f, 4.0f, new Dilation(0.0f)), ModelTransform.of(3.0f, 6.0f + offset, -8.0f, 1.6482f, -0.2443f, -2.3873f));
		return TexturedModelData.of(modelData, 128, 128);
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