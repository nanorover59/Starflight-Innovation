package space.client.render.entity.model;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import space.entity.CaveLampreyEntity;

public class CaveLampreyEntityModel<T extends CaveLampreyEntity> extends SinglePartEntityModel<T>
{
	private final ModelPart root;
	private final ModelPart head;
	private final ModelPart tail1;
	private final ModelPart tail2;
	private final ModelPart tail3;

	public CaveLampreyEntityModel(ModelPart root)
	{
		this.root = root;
		this.head = root.getChild("head");
		this.tail1 = root.getChild("tail1");
		this.tail2 = root.getChild("tail2");
		this.tail3 = root.getChild("tail3");
	}

	public static TexturedModelData getTexturedModelData()
	{
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		modelPartData.addChild("head", ModelPartBuilder.create().uv(0, 0).cuboid(-2.0F, -4.0F, -8.0F, 4.0F, 4.0F, 6.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 24.0F, 0.0F));
		modelPartData.addChild("tail1", ModelPartBuilder.create().uv(14, 4).cuboid(-1.0F, -4.0F, -2.0F, 2.0F, 4.0F, 6.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 24.0F, 0.0F));
		modelPartData.addChild("tail2", ModelPartBuilder.create().uv(0, 10).cuboid(-1.0F, -4.0F, 4.0F, 2.0F, 4.0F, 6.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 24.0F, 0.0F));
		modelPartData.addChild("tail3", ModelPartBuilder.create().uv(0, 14).cuboid(0.0F, -4.0F, 10.0F, 0.0F, 4.0F, 6.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 24.0F, 0.0F));
		return TexturedModelData.of(modelData, 32, 32);
	}
	
	@Override
	public ModelPart getPart()
	{
		return this.root;
	}

	@Override
	public void setAngles(T caveLampreyEntity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
	{
		float swing = ((float) caveLampreyEntity.getSwimTickOffset() + ageInTicks) * 7.448451f * ((float) Math.PI / 180);
		this.tail1.yaw = MathHelper.cos(swing) * 12.0f * ((float) Math.PI / 180);
		this.tail2.pivotX = 6.0f * MathHelper.sin(this.tail1.yaw);
		this.tail2.pivotZ = this.tail1.pivotZ * MathHelper.cos(this.tail1.yaw);
		this.tail2.yaw = this.tail1.yaw - MathHelper.cos(swing) * 6.0f * ((float) Math.PI / 180);
		this.tail3.pivotX = this.tail2.pivotX + 6.0f * MathHelper.sin(this.tail2.yaw);
		this.tail3.pivotZ = this.tail2.pivotZ * MathHelper.cos(this.tail2.yaw);
		this.tail3.yaw = this.tail2.yaw - MathHelper.cos(swing) * 6.0f * ((float) Math.PI / 180);
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, int color)
	{
		head.render(matrices, vertexConsumer, light, overlay, color);
		tail1.render(matrices, vertexConsumer, light, overlay, color);
		tail2.render(matrices, vertexConsumer, light, overlay, color);
		tail3.render(matrices, vertexConsumer, light, overlay, color);
	}
}