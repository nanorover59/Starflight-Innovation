package space.client.render.entity.model;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.util.math.MathHelper;
import space.entity.CloudSharkEntity;

public class CloudSharkEntityModel<T extends CloudSharkEntity> extends SinglePartEntityModel<T>
{
	private final ModelPart root;
	private final ModelPart tail1;
	private final ModelPart tail2;
	private final ModelPart tail3;

	public CloudSharkEntityModel(ModelPart root)
	{
		this.root = root;
		this.tail1 = root.getChild("tail1");
		this.tail2 = root.getChild("tail2");
		this.tail3 = root.getChild("tail3");
	}
	
	public static TexturedModelData getTexturedModelData()
	{
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		modelPartData.addChild("base", ModelPartBuilder.create().uv(0, 0).cuboid(-4.0F, -6.0F, -14.1F, 8.0F, 12.0F, 24.0F, new Dilation(0.0F))
		.uv(42, 68).cuboid(-4.0F, -4.0F, -24.1F, 8.0F, 8.0F, 10.0F, new Dilation(0.0F))
		.uv(38, 50).cuboid(4.0F, -2.0F, -6.1F, 12.0F, 4.0F, 14.0F, new Dilation(0.0F))
		.uv(54, 0).cuboid(16.0F, -1.0F, -4.1F, 12.0F, 2.0F, 14.0F, new Dilation(0.0F))
		.uv(24, 36).cuboid(28.0F, 0.0F, -2.1F, 14.0F, 0.0F, 14.0F, new Dilation(0.0F))
		.uv(0, 36).cuboid(-16.0F, -2.0F, -6.1F, 12.0F, 4.0F, 14.0F, new Dilation(0.0F))
		.uv(0, 54).cuboid(-28.0F, -1.0F, -4.1F, 12.0F, 2.0F, 14.0F, new Dilation(0.0F))
		.uv(26, 0).cuboid(-42.0F, 0.0F, -2.1F, 14.0F, 0.0F, 14.0F, new Dilation(0.0F))
		.uv(40, 4).cuboid(0.0F, -14.0F, -4.1F, 0.0F, 8.0F, 12.0F, new Dilation(0.0F))
		.uv(0, 0).cuboid(0.0F, 6.0F, -4.1F, 0.0F, 8.0F, 12.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 18.0F, 0.1F));
		modelPartData.addChild("tail1", ModelPartBuilder.create().uv(64, 16).cuboid(-3.0F, -4.0F, 0.0F, 6.0F, 8.0F, 12.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 18.0F, 10.0F));
		modelPartData.addChild("tail2", ModelPartBuilder.create().uv(66, 36).cuboid(-2.0F, -3.0F, 0.0F, 4.0F, 6.0F, 8.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 18.0F, 22.0F));
		modelPartData.addChild("tail3", ModelPartBuilder.create().uv(0, 0).cuboid(-1.0F, -2.0F, 0.0F, 2.0F, 4.0F, 4.0F, new Dilation(0.0F))
		.uv(0, 62).cuboid(0.0F, -10.0F, 2.0F, 0.0F, 20.0F, 8.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 18.0F, 30.0F));
		return TexturedModelData.of(modelData, 128, 128);
	}

	@Override
    public ModelPart getPart()
	{
        return this.root;
    }
	
	@Override
	public void setAngles(T cloudSharkEntity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
	{
		float swing = ((float) cloudSharkEntity.getWingFlapTickOffset() + ageInTicks) * 7.448451f * ((float) Math.PI / 180);
		this.tail1.yaw = MathHelper.cos(swing) * 16.0f * ((float) Math.PI / 180);
		this.tail2.pivotX = 8.0f * MathHelper.sin(this.tail1.yaw);
		this.tail2.pivotZ = this.tail1.pivotZ + 12.0f * MathHelper.cos(this.tail1.yaw);
		this.tail2.yaw = this.tail1.yaw + MathHelper.cos(swing) * 16.0f * ((float) Math.PI / 180);
		this.tail3.pivotX = this.tail2.pivotX + 8.0f * MathHelper.sin(this.tail2.yaw);
		this.tail3.pivotZ = this.tail2.pivotZ + 8.0f * MathHelper.cos(this.tail2.yaw);
		this.tail3.yaw = this.tail2.yaw + MathHelper.cos(swing) * 16.0f * ((float) Math.PI / 180);
	}
}