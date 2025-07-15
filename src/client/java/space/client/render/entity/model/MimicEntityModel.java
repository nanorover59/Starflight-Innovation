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
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import space.client.render.entity.animation.MimicAnimations;
import space.entity.MimicEntity;

public class MimicEntityModel<T extends MimicEntity> extends SinglePartEntityModel<T>
{
	private final ModelPart root;
	private final ModelPart leftFrontLeg;
	private final ModelPart rightFrontLeg;
	private final ModelPart leftMiddleFrontLeg;
	private final ModelPart rightMiddleFrontLeg;
	private final ModelPart leftHindLeg;
	private final ModelPart rightHindLeg;
	private final ModelPart leftMiddleLeg;
	private final ModelPart rightMiddleLeg;

	public MimicEntityModel(ModelPart root)
	{
		this.root = root;
		this.leftFrontLeg = root.getChild("leftFrontLeg");
		this.rightFrontLeg = root.getChild("rightFrontLeg");
		this.leftMiddleFrontLeg = root.getChild("leftMiddleFrontLeg");
		this.rightMiddleFrontLeg = root.getChild("rightMiddleFrontLeg");
		this.leftHindLeg = root.getChild("leftHindLeg");
		this.rightHindLeg = root.getChild("rightHindLeg");
		this.leftMiddleLeg = root.getChild("leftMiddleLeg");
		this.rightMiddleLeg = root.getChild("rightMiddleLeg");
	}

	public static TexturedModelData getTexturedModelData()
	{
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		modelPartData.addChild("body", ModelPartBuilder.create().uv(0, 0).cuboid(-8.0F, -2.0F, -8.0F, 16.0F, 2.0F, 16.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 20.0F, 0.0F));
		ModelPartData leftAntenna = modelPartData.addChild("leftAntenna", ModelPartBuilder.create(), ModelTransform.of(2.0F, 19.0F, -7.0F, 0.0F, 0.3491F, 0.0F));
		leftAntenna.addChild("leftAntenna", ModelPartBuilder.create().uv(-10, 24).cuboid(-2.0F, 0.0F, -9.7071F, 4.0F, 0.0F, 10.0F, new Dilation(0.0F)), ModelTransform.of(0.2929F, 0.0F, -0.2929F, 0.0F, -0.7854F, 0.0F));
		ModelPartData rightAntenna = modelPartData.addChild("rightAntenna", ModelPartBuilder.create(), ModelTransform.of(-2.0F, 19.0F, -7.0F, 0.0F, -0.3491F, 0.0F));
		rightAntenna.addChild("rightAntenna", ModelPartBuilder.create().uv(-10, 24).mirrored().cuboid(-2.0F, 0.0F, -9.7071F, 4.0F, 0.0F, 10.0F, new Dilation(0.0F)).mirrored(false), ModelTransform.of(-0.2929F, 0.0F, -0.2929F, 0.0F, 0.7854F, 0.0F));
		ModelPartData leftFrontLeg = modelPartData.addChild("leftFrontLeg", ModelPartBuilder.create(), ModelTransform.of(7.0F, 19.0F, -7.0F, 0.0F, 0.7418F, 0.0F));
		leftFrontLeg.addChild("leftFrontLegUpper", ModelPartBuilder.create().uv(20, 23).cuboid(0.0F, -1.0F, -3.0F, 6.0F, 1.0F, 4.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 1.0F, 1.0F, 0.0F, 0.0F, -0.7854F));
		leftFrontLeg.addChild("leftFrontLegLower", ModelPartBuilder.create().uv(0, 18).cuboid(0.0F, -1.0F, -3.0F, 10.0F, 1.0F, 4.0F, new Dilation(0.0F)), ModelTransform.of(3.5F, -3.4F, 1.0F, 0.0F, 0.0F, 1.0472F));
		ModelPartData rightFrontLeg = modelPartData.addChild("rightFrontLeg", ModelPartBuilder.create(), ModelTransform.of(-7.0F, 19.0F, -7.0F, 0.0F, -0.7418F, 0.0F));
		rightFrontLeg.addChild("rightFrontLegUpper", ModelPartBuilder.create().uv(20, 23).mirrored().cuboid(-6.0F, -1.0F, -3.0F, 6.0F, 1.0F, 4.0F, new Dilation(0.0F)).mirrored(false), ModelTransform.of(0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.7854F));
		rightFrontLeg.addChild("rightFrontLegLower", ModelPartBuilder.create().uv(0, 18).mirrored().cuboid(-10.0F, -1.0F, -3.0F, 10.0F, 1.0F, 4.0F, new Dilation(0.0F)).mirrored(false), ModelTransform.of(-3.5F, -3.4F, 1.0F, 0.0F, 0.0F, -1.0472F));
		ModelPartData leftMiddleFrontLeg = modelPartData.addChild("leftMiddleFrontLeg", ModelPartBuilder.create(), ModelTransform.of(8.0F, 19.0F, -2.0F, 0.0F, 0.2618F, 0.0F));
		leftMiddleFrontLeg.addChild("leftMiddleFrontLegUpper", ModelPartBuilder.create().uv(20, 23).cuboid(0.0F, -1.0F, -3.0F, 6.0F, 1.0F, 4.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 1.0F, 1.0F, 0.0F, 0.0F, -0.7854F));
		leftMiddleFrontLeg.addChild("leftMiddleFrontLegLower", ModelPartBuilder.create().uv(0, 18).cuboid(0.0F, -1.0F, -3.0F, 10.0F, 1.0F, 4.0F, new Dilation(0.0F)), ModelTransform.of(3.5F, -3.4F, 1.0F, 0.0F, 0.0F, 1.0472F));
		ModelPartData rightMiddleFrontLeg = modelPartData.addChild("rightMiddleFrontLeg", ModelPartBuilder.create(), ModelTransform.of(-8.0F, 19.0F, -2.0F, 0.0F, -0.2618F, 0.0F));
		rightMiddleFrontLeg.addChild("rightMiddleFrontLegUpper", ModelPartBuilder.create().uv(20, 23).mirrored().cuboid(-6.0F, -1.0F, -3.0F, 6.0F, 1.0F, 4.0F, new Dilation(0.0F)).mirrored(false), ModelTransform.of(0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.7854F));
		rightMiddleFrontLeg.addChild("rightMiddleFrontLegLower", ModelPartBuilder.create().uv(0, 18).mirrored().cuboid(-10.0F, -1.0F, -3.0F, 10.0F, 1.0F, 4.0F, new Dilation(0.0F)).mirrored(false), ModelTransform.of(-3.5F, -3.4F, 1.0F, 0.0F, 0.0F, -1.0472F));
		ModelPartData leftHindLeg = modelPartData.addChild("leftHindLeg", ModelPartBuilder.create(), ModelTransform.of(7.0F, 19.0F, 7.0F, 0.0F, -0.7418F, 0.0F));
		leftHindLeg.addChild("leftHindLegUpper", ModelPartBuilder.create().uv(20, 23).cuboid(0.0F, -1.0F, -1.0F, 6.0F, 1.0F, 4.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 1.0F, -1.0F, 0.0F, 0.0F, -0.7854F));
		leftHindLeg.addChild("leftHindLegLower", ModelPartBuilder.create().uv(0, 18).cuboid(0.0F, -1.0F, -1.0F, 10.0F, 1.0F, 4.0F, new Dilation(0.0F)), ModelTransform.of(3.5F, -3.4F, -1.0F, 0.0F, 0.0F, 1.0472F));
		ModelPartData rightHindLeg = modelPartData.addChild("rightHindLeg", ModelPartBuilder.create(), ModelTransform.of(-7.0F, 19.0F, 7.0F, 0.0F, 0.7418F, 0.0F));
		rightHindLeg.addChild("rightHindLegUpper", ModelPartBuilder.create().uv(20, 23).mirrored().cuboid(-6.0F, -1.0F, -1.0F, 6.0F, 1.0F, 4.0F, new Dilation(0.0F)).mirrored(false), ModelTransform.of(0.0F, 1.0F, -1.0F, 0.0F, 0.0F, 0.7854F));
		rightHindLeg.addChild("rightHindLegLower", ModelPartBuilder.create().uv(0, 18).mirrored().cuboid(-10.0F, -1.0F, -1.0F, 10.0F, 1.0F, 4.0F, new Dilation(0.0F)).mirrored(false), ModelTransform.of(-3.5F, -3.4F, -1.0F, 0.0F, 0.0F, -1.0472F));
		ModelPartData leftMiddleLeg = modelPartData.addChild("leftMiddleLeg", ModelPartBuilder.create(), ModelTransform.of(8.0F, 19.0F, 2.0F, 0.0F, -0.2618F, 0.0F));
		leftMiddleLeg.addChild("leftMiddleLegUpper", ModelPartBuilder.create().uv(20, 23).cuboid(0.0F, -1.0F, -1.0F, 6.0F, 1.0F, 4.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 1.0F, -1.0F, 0.0F, 0.0F, -0.7854F));
		leftMiddleLeg.addChild("leftMiddleLegLower", ModelPartBuilder.create().uv(0, 18).cuboid(0.0F, -1.0F, -1.0F, 10.0F, 1.0F, 4.0F, new Dilation(0.0F)), ModelTransform.of(3.5F, -3.4F, -1.0F, 0.0F, 0.0F, 1.0472F));
		ModelPartData rightMiddleLeg = modelPartData.addChild("rightMiddleLeg", ModelPartBuilder.create(), ModelTransform.of(-8.0F, 19.0F, 2.0F, 0.0F, 0.2618F, 0.0F));
		rightMiddleLeg.addChild("rightMiddleLegUpper", ModelPartBuilder.create().uv(20, 23).mirrored().cuboid(-6.0F, -1.0F, -1.0F, 6.0F, 1.0F, 4.0F, new Dilation(0.0F)).mirrored(false), ModelTransform.of(0.0F, 1.0F, -1.0F, 0.0F, 0.0F, 0.7854F));
		rightMiddleLeg.addChild("rightMiddleLegLower", ModelPartBuilder.create().uv(0, 18).mirrored().cuboid(-10.0F, -1.0F, -1.0F, 10.0F, 1.0F, 4.0F, new Dilation(0.0F)).mirrored(false), ModelTransform.of(-3.5F, -3.4F, -1.0F, 0.0F, 0.0F, -1.0472F));
		return TexturedModelData.of(modelData, 64, 64);
	}

	@Override
	public ModelPart getPart()
	{
		return this.root;
	}

	@Override
	public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch)
	{
		this.getPart().traverse().forEach(ModelPart::resetTransform);
		this.updateAnimation(entity.openAnimationState, MimicAnimations.OPEN, animationProgress);
		this.updateAnimation(entity.closeAnimationState, MimicAnimations.CLOSE, animationProgress);
		float i = -(MathHelper.cos(limbAngle * 0.6662F * 2.0F + 0.0F) * 0.4F) * limbDistance;
		float j = -(MathHelper.cos(limbAngle * 0.6662F * 2.0F + (float) Math.PI) * 0.4F) * limbDistance;
		float k = -(MathHelper.cos(limbAngle * 0.6662F * 2.0F + (float) (Math.PI / 2)) * 0.4F) * limbDistance;
		float l = -(MathHelper.cos(limbAngle * 0.6662F * 2.0F + (float) (Math.PI * 3.0 / 2.0)) * 0.4F) * limbDistance;
		float m = Math.abs(MathHelper.sin(limbAngle * 0.6662F + 0.0F) * 0.4F) * limbDistance;
		float n = Math.abs(MathHelper.sin(limbAngle * 0.6662F + (float) Math.PI) * 0.4F) * limbDistance;
		float o = Math.abs(MathHelper.sin(limbAngle * 0.6662F + (float) (Math.PI / 2)) * 0.4F) * limbDistance;
		float p = Math.abs(MathHelper.sin(limbAngle * 0.6662F + (float) (Math.PI * 3.0 / 2.0)) * 0.4F) * limbDistance;
		this.rightHindLeg.yaw += i;
		this.leftHindLeg.yaw += -i;
		this.rightMiddleLeg.yaw += j;
		this.leftMiddleLeg.yaw += -j;
		this.rightMiddleFrontLeg.yaw += k;
		this.leftMiddleFrontLeg.yaw += -k;
		this.rightFrontLeg.yaw += l;
		this.leftFrontLeg.yaw += -l;
		this.rightHindLeg.roll += m;
		this.leftHindLeg.roll += -m;
		this.rightMiddleLeg.roll += n;
		this.leftMiddleLeg.roll += -n;
		this.rightMiddleFrontLeg.roll += o;
		this.leftMiddleFrontLeg.roll += -o;
		this.rightFrontLeg.roll += p;
		this.leftFrontLeg.roll += -p;
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, int color)
	{
		super.render(matrices, vertexConsumer, light, overlay, ColorHelper.Argb.fromFloats(0.8f, 1.0f, 1.0f, 1.0f));
	}
}