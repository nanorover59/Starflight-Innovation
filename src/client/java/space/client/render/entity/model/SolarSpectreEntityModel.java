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
import net.minecraft.util.math.MathHelper;
import space.entity.SolarSpectreEntity;

@Environment(value = EnvType.CLIENT)
public class SolarSpectreEntityModel<T extends SolarSpectreEntity> extends SinglePartEntityModel<T>
{
	private final ModelPart root;
	private final ArrayList<ModelPart> parts0;
	private final ArrayList<ModelPart> parts1;
	
	public SolarSpectreEntityModel(ModelPart root)
	{
		this.root = root;
		this.parts0 = new ArrayList<ModelPart>();
		this.parts1 = new ArrayList<ModelPart>();
		
		for(int i = 0; i < 4; i++)
			parts0.add(root.getChild("parts0_" + i));
		
		for(int i = 0; i < 4; i++)
			parts1.add(root.getChild("parts1_" + i));
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
			float pivotX = 3.0f * MathHelper.cos(largeWingRotation);
			float pivotY = 3.0f * MathHelper.sin(largeWingRotation);
			modelPartData.addChild("parts0_" + i, ModelPartBuilder.create().uv(0, 0).cuboid(0.0F, -6.0F, 0.0F, 40.0F, 12.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(pivotX, pivotY, 0.0F, 0.0F, 0.0F, largeWingRotation));
			pivotX = 3.0f * MathHelper.cos(smallWingRotation);
			pivotY = 3.0f * MathHelper.sin(smallWingRotation);
			modelPartData.addChild("parts1_" + i, ModelPartBuilder.create().uv(0, 12).cuboid(0.0F, -4.0F, 0.0F, 40.0F, 8.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(pivotX, pivotY, 8.0F, 0.0F, 0.0F, smallWingRotation));
		}
		
		return TexturedModelData.of(modelData, 128, 128);
	}

	@Override
	public ModelPart getPart()
	{
		return this.root;
	}
	
	public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch)
	{
		float angle = MathHelper.map(MathHelper.lerp(animationProgress - (float) entity.age, entity.limbAnglePrevious, entity.limbAngle), 0.0f, 1.0f, MathHelper.PI / 16.0f, MathHelper.PI / 2.0f);
		
		for(ModelPart part : parts0)
			part.yaw = -angle;
		
		for(ModelPart part : parts1)
			part.yaw = -angle;
	}
}