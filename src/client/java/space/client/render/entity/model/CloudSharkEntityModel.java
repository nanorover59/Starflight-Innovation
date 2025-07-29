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
	private final ModelPart tail0;
	private final ModelPart tail1;
	private final ModelPart tail2;
	private final ModelPart tail3;
	private final ModelPart jaw;

	public CloudSharkEntityModel(ModelPart root)
	{
		this.root = root;
		this.tail0 = root.getChild("tail0");
		this.tail1 = root.getChild("tail1");
		this.tail2 = root.getChild("tail2");
		this.tail3 = root.getChild("tail3");
		this.jaw = root.getChild("jaw");
	}
	
	public static TexturedModelData getTexturedModelData()
	{
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		ModelPartData body = modelPartData.addChild("body", ModelPartBuilder.create().uv(52, 42).cuboid(-5.0F, -6.0F, 0.0F, 9.0F, 9.0F, 6.0F, new Dilation(0.0F))
		.uv(0, 0).cuboid(-5.5F, -6.5F, 6.0F, 10.0F, 10.0F, 16.0F, new Dilation(0.0F)), ModelTransform.pivot(0.5F, 20.5F, -14.0F));
		body.addChild("cube_r1", ModelPartBuilder.create().uv(50, 72).cuboid(-1.0F, -7.0F, -3.0F, 2.0F, 10.0F, 6.0F, new Dilation(0.0F)), ModelTransform.of(-0.5F, -6.5F, 15.0F, -0.7854F, 0.0F, 0.0F));
		modelPartData.addChild("tail0", ModelPartBuilder.create().uv(52, 57).cuboid(-4.5F, -4.5F, 0.0F, 9.0F, 9.0F, 6.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 19.0F, 8.0F));
		modelPartData.addChild("tail1", ModelPartBuilder.create().uv(0, 58).cuboid(-3.5F, -4.0F, 0.0F, 7.0F, 8.0F, 6.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 19.0F, 14.0F));
		modelPartData.addChild("tail2", ModelPartBuilder.create().uv(0, 72).cuboid(-2.0F, -3.5F, 0.0F, 4.0F, 7.0F, 6.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 19.0F, 20.0F));
		modelPartData.addChild("tail3", ModelPartBuilder.create().uv(26, 58).cuboid(-1.0F, -4.0F, 0.0F, 2.0F, 6.0F, 10.0F, new Dilation(0.0F))
		.uv(66, 72).cuboid(-1.0F, -10.0F, 0.0F, 2.0F, 8.0F, 6.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 17.0F, 25.0F, -0.7854F, 0.0F, 0.0F));
		ModelPartData left = modelPartData.addChild("left", ModelPartBuilder.create(), ModelTransform.of(0.0F, 23.0F, -6.0F, 0.035F, -0.2595F, -0.1355F));
		left.addChild("cube_r2", ModelPartBuilder.create().uv(52, 14).cuboid(-7.0F, -1.0F, 2.0F, 14.0F, 2.0F, 4.0F, new Dilation(0.0F))
		.uv(0, 26).cuboid(-7.0F, -1.0F, 12.0F, 26.0F, 2.0F, 6.0F, new Dilation(0.0F))
		.uv(0, 42).cuboid(-7.0F, -1.0F, 6.0F, 20.0F, 2.0F, 6.0F, new Dilation(0.0F)), ModelTransform.of(9.0F, -1.0F, -5.0F, 0.0F, 0.0F, 0.0436F));
		ModelPartData right = modelPartData.addChild("right", ModelPartBuilder.create(), ModelTransform.of(0.0F, 23.0F, -6.0F, 0.035F, 0.2595F, 0.1355F));
		right.addChild("cube_r3", ModelPartBuilder.create().uv(52, 20).cuboid(-7.0F, -1.0F, 2.0F, 14.0F, 2.0F, 4.0F, new Dilation(0.0F))
		.uv(0, 34).cuboid(-19.0F, -1.0F, 12.0F, 26.0F, 2.0F, 6.0F, new Dilation(0.0F))
		.uv(0, 50).cuboid(-13.0F, -1.0F, 6.0F, 20.0F, 2.0F, 6.0F, new Dilation(0.0F)), ModelTransform.of(-9.0F, -1.0F, -5.0F, 0.0F, 0.0F, -0.0436F));
		ModelPartData head = modelPartData.addChild("head", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 17.2324F, -16.0681F));
		head.addChild("cube_r4", ModelPartBuilder.create().uv(52, 0).cuboid(-4.0F, -2.0F, -6.0F, 8.0F, 5.0F, 9.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.2618F, 0.0F, 0.0F));
		modelPartData.addChild("jaw", ModelPartBuilder.create().uv(64, 26).cuboid(-3.0F, 0.0F, -6.0F, 6.0F, 3.0F, 6.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 19.5F, -14.0F));	
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
		this.tail0.yaw = MathHelper.cos(swing) * 8.0f * ((float) Math.PI / 180);
		this.tail1.pivotX = 8.0f * MathHelper.sin(this.tail0.yaw);
		this.tail1.pivotZ = this.tail0.pivotZ + 6.0f * MathHelper.cos(this.tail0.yaw);
		this.tail1.yaw = this.tail0.yaw + MathHelper.cos(swing) * 8.0f * ((float) Math.PI / 180);
		this.tail2.pivotX = this.tail1.pivotX + 8.0f * MathHelper.sin(this.tail1.yaw);
		this.tail2.pivotZ = this.tail1.pivotZ + 6.0f * MathHelper.cos(this.tail1.yaw);
		this.tail2.yaw = this.tail1.yaw + MathHelper.cos(swing) * 8.0f * ((float) Math.PI / 180);
		this.tail3.pivotX = this.tail2.pivotX + 8.0f * MathHelper.sin(this.tail2.yaw);
		this.tail3.pivotZ = this.tail2.pivotZ + 5.0f * MathHelper.cos(this.tail2.yaw);
		this.tail3.yaw = this.tail2.yaw + MathHelper.cos(swing) * 8.0f * ((float) Math.PI / 180);
		this.jaw.pitch = (MathHelper.cos(swing) * -20.0f + 20.0f) * ((float) Math.PI / 180);
	}
}