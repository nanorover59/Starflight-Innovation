package space.client.render.entity.model;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import space.client.render.entity.animation.LunarMechAnimations;
import space.entity.LunarMechEntity;

public class LunarMechEntityModel<T extends LunarMechEntity> extends SinglePartEntityModel<T>
{
	private final ModelPart root;
	private final ModelPart body;
	private final ModelPart head;
	private final ModelPart leftArm0;
	private final ModelPart leftArm1;
	private final ModelPart rightArm0;
	private final ModelPart rightArm1;
	private final ModelPart leftLeg0;
	private final ModelPart leftLeg1;
	private final ModelPart leftLeg2;
	private final ModelPart rightLeg0;
	private final ModelPart rightLeg1;
	private final ModelPart rightLeg2;
	
	public LunarMechEntityModel(ModelPart root)
	{
		this.root = root;
		this.body = root.getChild("body");
		this.head = root.getChild("head");
		this.leftArm0 = root.getChild("left_arm_0");
		this.leftArm1 = root.getChild("left_arm_1");
		this.rightArm0 = root.getChild("right_arm_0");
		this.rightArm1 = root.getChild("right_arm_1");
		this.leftLeg0 = root.getChild("left_leg_0");
		this.leftLeg1 = root.getChild("left_leg_1");
		this.leftLeg2 = root.getChild("left_leg_2");
		this.rightLeg0 = root.getChild("right_leg_0");
		this.rightLeg1 = root.getChild("right_leg_1");
		this.rightLeg2 = root.getChild("right_leg_2");
	}

	public static TexturedModelData getTexturedModelData()
	{
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		ModelPartData body = modelPartData.addChild("body", ModelPartBuilder.create().uv(0, 0).cuboid(-12.0F, -9.3333F, -6.0F, 24.0F, 32.0F, 12.0F, new Dilation(0.0F)).uv(0, 128).cuboid(12.0F, -9.3333F, -6.0F, 4.0F, 12.0F, 12.0F, new Dilation(0.0F)).uv(32, 128).cuboid(-16.0F, -9.3333F, -6.0F, 4.0F, 12.0F, 12.0F, new Dilation(0.0F)).uv(144, 80).cuboid(-10.0F, -7.25F, 6.0F, 20.0F, 20.0F, 6.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, -34.6667F, 0.0F));
		ModelPartData head = modelPartData.addChild("head", ModelPartBuilder.create().uv(104, 44).cuboid(-8.0F, -14.0F, -4.0F, 12.0F, 12.0F, 12.0F, new Dilation(0.0F)).uv(64, 128).cuboid(4.0F, -26.0F, -4.0F, 0.0F, 12.0F, 12.0F, new Dilation(0.0F)).uv(132, 0).cuboid(-8.0F, -26.0F, -4.0F, 0.0F, 12.0F, 12.0F, new Dilation(0.0F)), ModelTransform.pivot(2.0F, -42.0F, -2.0F));
		ModelPartData leftArm0 = modelPartData.addChild("left_arm_0", ModelPartBuilder.create(), ModelTransform.pivot(16.0F, -38.0F, 0.0F));
		leftArm0.addChild("cube_r1", ModelPartBuilder.create().uv(96, 74).cuboid(-4.0F, -4.0F, -12.0F, 6.0F, 8.0F, 18.0F, new Dilation(0.0F)), ModelTransform.of(4.0F, 0.0F, 0.0F, 1.5708F, 0.0F, 0.0F));
		ModelPartData leftArm1 = modelPartData.addChild("left_arm_1", ModelPartBuilder.create().uv(88, 128).cuboid(-3.1F, 14.0F, 4.6F, 2.0F, 10.0F, 2.0F, new Dilation(0.0F)).uv(88, 140).cuboid(-1.1F, 14.0F, -3.4F, 2.0F, 10.0F, 2.0F, new Dilation(0.0F)).uv(136, 143).cuboid(1.9F, 14.0F, -3.4F, 2.0F, 10.0F, 2.0F, new Dilation(0.0F)).uv(144, 68).cuboid(-4.1F, 14.0F, -3.4F, 2.0F, 10.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(19.1F, -26.0F, -1.6F, -0.5236F, 0.0F, 0.0F));
		leftArm1.addChild("cube_r2", ModelPartBuilder.create().uv(96, 100).cuboid(-3.0F, -4.0F, -17.0F, 6.0F, 8.0F, 18.0F, new Dilation(0.0F)), ModelTransform.of(-0.1F, 0.0F, 1.6F, 1.5708F, 0.0F, 0.0F));
		ModelPartData rightArm0 = modelPartData.addChild("right_arm_0", ModelPartBuilder.create(), ModelTransform.pivot(-16.0F, -38.0F, 0.0F));
		rightArm0.addChild("cube_r3", ModelPartBuilder.create().uv(0, 102).cuboid(-4.0F, -4.0F, -12.0F, 6.0F, 8.0F, 18.0F, new Dilation(0.0F)), ModelTransform.of(-2.0F, 0.0F, 0.0F, 1.5708F, 0.0F, 0.0F));
		ModelPartData rightArm1 = modelPartData.addChild("right_arm_1", ModelPartBuilder.create().uv(96, 126).cuboid(-5.0F, 14.0F, -5.0F, 10.0F, 14.0F, 10.0F, new Dilation(0.0F)).uv(132, 24).cuboid(-2.0F, 10.0F, -7.0F, 4.0F, 14.0F, 3.0F, new Dilation(0.0F)).uv(136, 126).cuboid(-2.0F, 10.0F, 4.0F, 4.0F, 14.0F, 3.0F, new Dilation(0.0F)), ModelTransform.of(-19.0F, -26.0F, 0.0F, -0.5236F, 0.0F, 0.0F));
		rightArm1.addChild("cube_r4", ModelPartBuilder.create().uv(48, 102).cuboid(-3.0F, -4.0F, -18.0F, 6.0F, 8.0F, 18.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 1.5708F, 0.0F, 0.0F));
		ModelPartData leftLeg0 = modelPartData.addChild("left_leg_0", ModelPartBuilder.create(), ModelTransform.pivot(8.0F, -12.0F, 0.0F));
		leftLeg0.addChild("cube_r5", ModelPartBuilder.create().uv(0, 44).cuboid(-6.0F, -6.0F, -12.0F, 8.0F, 12.0F, 18.0F, new Dilation(0.0F)), ModelTransform.of(2.0F, 6.0F, 0.0F, 1.4399F, 0.0F, 0.0F));
		ModelPartData leftLeg1 = modelPartData.addChild("left_leg_1", ModelPartBuilder.create(), ModelTransform.pivot(8.0F, 6.0F, 0.0F));
		leftLeg1.addChild("cube_r6", ModelPartBuilder.create().uv(0, 74).cuboid(-6.0F, -7.0F, -9.0F, 8.0F, 12.0F, 16.0F, new Dilation(0.0F)), ModelTransform.of(2.0F, 6.0F, 0.0F, 1.7017F, 0.0F, 0.0F));
		modelPartData.addChild("left_leg_2", ModelPartBuilder.create().uv(72, 0).cuboid(2.0F, -4.0F, -10.0F, 12.0F, 4.0F, 18.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 24.0F, 0.0F));
		ModelPartData rightLeg0 = modelPartData.addChild("right_leg_0", ModelPartBuilder.create(), ModelTransform.pivot(-8.0F, -12.0F, 0.0F));
		rightLeg0.addChild("cube_r7", ModelPartBuilder.create().uv(52, 44).cuboid(-6.0F, -7.0F, -12.0F, 8.0F, 12.0F, 18.0F, new Dilation(0.0F)), ModelTransform.of(2.0F, 6.0F, 0.0F, 1.4399F, 0.0F, 0.0F));
		ModelPartData rightLeg1 = modelPartData.addChild("right_leg_1", ModelPartBuilder.create(), ModelTransform.pivot(-8.0F, 6.0F, 0.0F));
		rightLeg1.addChild("cube_r8", ModelPartBuilder.create().uv(48, 74).cuboid(-6.0F, -7.0F, -9.0F, 8.0F, 12.0F, 16.0F, new Dilation(0.0F)), ModelTransform.of(2.0F, 6.0F, 0.0F, 1.7017F, 0.0F, 0.0F));
		modelPartData.addChild("right_leg_2", ModelPartBuilder.create().uv(72, 22).cuboid(-14.0F, -4.0F, -10.0F, 12.0F, 4.0F, 18.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 24.0F, 0.0F));
		return TexturedModelData.of(modelData, 256, 256);
	}
	
	@Override
	public ModelPart getPart()
	{
		return this.root;
	}

	@Override
	public void setAngles(LunarMechEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
	{
		this.getPart().traverse().forEach(ModelPart::resetTransform);
		this.updateAnimation(entity.walkingAnimationState, LunarMechAnimations.WALKING, ageInTicks);
		this.updateAnimation(entity.flipAnimationState, LunarMechAnimations.FLIP, ageInTicks);
		this.updateAnimation(entity.armAnimationState, LunarMechAnimations.ARM, ageInTicks);
		this.updateAnimation(entity.disarmAnimationState, LunarMechAnimations.DISARM, ageInTicks);
	}
}