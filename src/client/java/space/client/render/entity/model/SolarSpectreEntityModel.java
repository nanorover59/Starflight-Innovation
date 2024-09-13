package space.client.render.entity.model;

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
import net.minecraft.util.math.MathHelper;

@Environment(value = EnvType.CLIENT)
public class SolarSpectreEntityModel<T extends Entity> extends SinglePartEntityModel<T>
{
	private final ModelPart root;
	//private final ArrayList<ModelPart> fins;

	public SolarSpectreEntityModel(ModelPart root)
	{
		this.root = root;
		//this.fins = new ArrayList<ModelPart>();

		//for(int i = 0; i < 6; i++)
		//	fins.add(root.getChild(FIN_NAME + i));
	}

	public static TexturedModelData getTexturedModelData()
	{
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		modelPartData.addChild("base", ModelPartBuilder.create().uv(0, 20).cuboid(-3.0F, -3.0F, -9.0F, 6.0F, 6.0F, 18.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 0.0F, 0.0F));
		
		for(int i = 0; i < 4; i++)
		{
			float largeWingRotation = i * 90.0f * MathHelper.RADIANS_PER_DEGREE;
			float smallWingRotation = (i * 90.0f + 45.0f) * MathHelper.RADIANS_PER_DEGREE;
			modelPartData.addChild("large_wing_" + i, ModelPartBuilder.create().uv(0, 0).cuboid(0.0F, -6.0F, 0.0F, 40.0F, 12.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(3.0F * MathHelper.cos(largeWingRotation), 3.0F * MathHelper.sin(largeWingRotation), 0.0F, 0.0F, 0.0F, largeWingRotation));
			modelPartData.addChild("small_wing_" + i, ModelPartBuilder.create().uv(0, 12).cuboid(0.0F, -4.0F, 0.0F, 40.0F, 8.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(3.0F * MathHelper.cos(smallWingRotation), 3.0F * MathHelper.sin(smallWingRotation), 4.0F, 0.0F, 0.0F, smallWingRotation));
		}
		
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