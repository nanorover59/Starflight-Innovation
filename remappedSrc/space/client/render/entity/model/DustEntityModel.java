package space.client.render.entity.model;

import java.util.ArrayList;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class DustEntityModel<T extends Entity> extends SinglePartEntityModel<T>
{
	private final ModelPart root;
	private final ArrayList<ModelPart> rods;
	private final ModelPart head;
	private static int finalRodCount = 0;

	public DustEntityModel(ModelPart root)
	{
		this.root = root;
		this.head = root.getChild(EntityModelPartNames.HEAD);
		this.rods = new ArrayList<ModelPart>();
		
		for(int i = 0; i < finalRodCount; i++)
			rods.add(root.getChild(getRodName(i)));
	}

	private static String getRodName(int index)
	{
		return "rod_" + index;
	}

	public static TexturedModelData getTexturedModelData()
	{
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		modelPartData.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 0).cuboid(-4.0f, -4.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.NONE);
		float f = 0.0f;
		int rodCount = 12;
		finalRodCount = 0;
		ModelPartBuilder modelPartBuilder = ModelPartBuilder.create().uv(0, 16).cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 8.0f, 2.0f);
		
		for(int i = 0; i < 5; i++)
		{
			for(int j = 0; j < rodCount; j++)
			{
				float r = 2.0f + (float) Math.pow((30.0 - i * 5.0) / 8.0, 2);
				float g = MathHelper.cos(f) * r;
				float h = 32.0f - (i * 8.0f) + MathHelper.cos((float) f * 0.5f);
				float k = MathHelper.sin(f) * r;
				modelPartData.addChild(getRodName(finalRodCount), modelPartBuilder, ModelTransform.pivot(g, h, k));
				finalRodCount++;
				f += (Math.PI * 2.0f) / rodCount;
			}
			
			rodCount -= 2;
			f = 0.0f;
		}
		
		return TexturedModelData.of(modelData, 64, 32);
	}

	@Override
	public ModelPart getPart()
	{
		return this.root;
	}

	@Override
	public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch)
	{
		float f = animationProgress * 0.05f * (float) Math.PI;
		int rodCount = 12;
		int index = 0;
		
		for(int i = 0; i < 5; i++)
		{
			for(int j = 0; j < rodCount; j++)
			{
				float r = 2.0f + (float) Math.pow((30.0 - i * 5.0) / 8.0, 2);
				this.rods.get(index).pivotX = MathHelper.cos(f) * r;
				this.rods.get(index).pivotY = 32.0f - (i * 8.0f) + MathHelper.cos((float) f * 0.5f);
				this.rods.get(index).pivotZ = MathHelper.sin(f) * r;
				f += (Math.PI * 2.0f) / rodCount;
				index++;
			}
			
			rodCount -= 2;
			f = animationProgress * 0.05f * (float) (Math.PI * Math.pow(-1.0, i + 1));
		}
		
		this.head.pivotY = 40.0f;
		this.head.yaw = headYaw * (float) (Math.PI / 180.0);
		this.head.pitch = headPitch * (float) (Math.PI / 180.0);
	}
}