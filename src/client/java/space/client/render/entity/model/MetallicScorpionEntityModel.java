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

@Environment(EnvType.CLIENT)
public class MetallicScorpionEntityModel<T extends Entity> extends SinglePartEntityModel<T>
{
	private static final String BODY0 = "body0";
	private static final String BODY1 = "body1";
	private static final String RIGHT_MIDDLE_FRONT_LEG = "right_middle_front_leg";
	private static final String LEFT_MIDDLE_FRONT_LEG = "left_middle_front_leg";
	private static final String RIGHT_MIDDLE_HIND_LEG = "right_middle_hind_leg";
	private static final String LEFT_MIDDLE_HIND_LEG = "left_middle_hind_leg";
	private final ModelPart root;
	private final ModelPart body;
	private final ModelPart head;
	private final ModelPart tail0;
	private final ModelPart tail1;
	private final ModelPart leftArm;
	private final ModelPart rightArm;
	private final ModelPart leftFrontLeg;
	private final ModelPart rightFrontLeg;
	private final ModelPart leftMiddleFrontLeg;
	private final ModelPart rightMiddleFrontLeg;
	private final ModelPart leftMiddleHindLeg;
	private final ModelPart rightMiddleHindLeg;
	private final ModelPart leftHindLeg;
	private final ModelPart rightHindLeg;

	public MetallicScorpionEntityModel(ModelPart root)
	{
		this.root = root;
		this.body = root.getChild("body");
		this.head = root.getChild("head");
		this.tail0 = root.getChild("tail_0");
		this.tail1 = root.getChild("tail_1");
		this.leftArm = root.getChild("left_arm");
		this.rightArm = root.getChild("right_arm");
		this.leftFrontLeg = root.getChild("left_front_leg");
		this.rightFrontLeg = root.getChild("right_front_leg");
		this.leftMiddleFrontLeg = root.getChild("left_middle_front_leg");
		this.rightMiddleFrontLeg = root.getChild("right_middle_front_leg");
		this.leftMiddleHindLeg = root.getChild("left_middle_hind_leg");
		this.rightMiddleHindLeg = root.getChild("right_middle_hind_leg");
		this.leftHindLeg = root.getChild("left_hind_leg");
		this.rightHindLeg = root.getChild("right_hind_leg");
	}

	public static TexturedModelData getTexturedModelData()
	{
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		modelPartData.addChild("body", ModelPartBuilder.create().uv(0, 0).cuboid(-4.0F, -8.0F, -7.0F, 8.0F, 4.0F, 14.0F, new Dilation(0.0F)).uv(16, 21).cuboid(4.0F, -7.0F, -6.0F, 2.0F, 3.0F, 12.0F, new Dilation(0.0F)).uv(0, 18).cuboid(-6.0F, -7.0F, -6.0F, 2.0F, 3.0F, 12.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 24.0F, 0.0F));
		ModelPartData head = modelPartData.addChild("head", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 19.0F, -8.0F));
		head.addChild("cube_r1", ModelPartBuilder.create().uv(0, 0).cuboid(-2.0F, -1.5F, -6.0F, 4.0F, 2.0F, 3.0F, new Dilation(0.0F)).uv(32, 18).cuboid(-3.0F, -2.5F, -3.0F, 6.0F, 3.0F, 5.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.2618F, 0.0F, 0.0F));
		modelPartData.addChild("tail_0", ModelPartBuilder.create().uv(30, 0).cuboid(-3.0F, -3.0F, -8.0F, 6.0F, 3.0F, 6.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 20.0F, 15.0F));
		modelPartData.addChild("tail_1", ModelPartBuilder.create().uv(24, 36).cuboid(-2.0F, -2.0F, -8.0F, 4.0F, 2.0F, 6.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 20.0F, 21.0F));
		ModelPartData leftArm = modelPartData.addChild("left_arm", ModelPartBuilder.create(), ModelTransform.pivot(8.9497F, 19.5F, -10.9497F));
		leftArm.addChild("cube_r2", ModelPartBuilder.create().uv(38, 33).cuboid(-1.0F, -1.0F, -5.0F, 2.0F, 1.0F, 6.0F, new Dilation(0.0F)).uv(10, 24).cuboid(-3.0F, -0.5F, -11.0F, 6.0F, 0.0F, 6.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.2618F, 0.0F));
		leftArm.addChild("cube_r3", ModelPartBuilder.create().uv(12, 36).cuboid(-1.0F, -1.0F, -7.0F, 2.0F, 1.0F, 8.0F, new Dilation(0.0F)), ModelTransform.of(-4.9497F, 0.0F, 4.9497F, 0.0F, -0.9163F, 0.0F));
		ModelPartData rightArm = modelPartData.addChild("right_arm", ModelPartBuilder.create(), ModelTransform.pivot(-8.9497F, 19.5F, -10.9497F));
		rightArm.addChild("cube_r4", ModelPartBuilder.create().uv(32, 26).cuboid(-1.0F, -1.0F, -5.0F, 2.0F, 1.0F, 6.0F, new Dilation(0.0F)).uv(10, 18).cuboid(-3.0F, -0.5F, -11.0F, 6.0F, 0.0F, 6.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, -0.2618F, 0.0F));
		rightArm.addChild("cube_r5", ModelPartBuilder.create().uv(0, 33).cuboid(-1.0F, -1.0F, -7.0F, 2.0F, 1.0F, 8.0F, new Dilation(0.0F)), ModelTransform.of(4.9497F, 0.0F, 4.9497F, 0.0F, 0.9163F, 0.0F));
		ModelPartData leftFrontLeg = modelPartData.addChild("left_front_leg", ModelPartBuilder.create(), ModelTransform.pivot(4.0F, 18.5F, -4.5F));
		leftFrontLeg.addChild("cube_r6", ModelPartBuilder.create().uv(20, 45).cuboid(0.0F, -1.0F, -1.0F, 8.0F, 1.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.2618F, 0.7854F));
		ModelPartData rightFrontLeg = modelPartData.addChild("right_front_leg", ModelPartBuilder.create(), ModelTransform.pivot(-4.0F, 18.5F, -4.5F));
		rightFrontLeg.addChild("cube_r7", ModelPartBuilder.create().uv(0, 45).cuboid(-8.0F, -1.0F, -1.0F, 8.0F, 1.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, -0.2618F, -0.7854F));
		ModelPartData leftMiddleFrontLeg = modelPartData.addChild("left_middle_front_leg", ModelPartBuilder.create(), ModelTransform.pivot(4.0F, 18.5F, -1.5F));
		leftMiddleFrontLeg.addChild("cube_r8", ModelPartBuilder.create().uv(44, 12).cuboid(0.0F, -1.0F, -1.0F, 8.0F, 1.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.1309F, 0.7854F));
		ModelPartData rightMiddleFrontLeg = modelPartData.addChild("right_middle_front_leg", ModelPartBuilder.create(), ModelTransform.pivot(-4.0F, 18.5F, -1.5F));
		rightMiddleFrontLeg.addChild("cube_r9", ModelPartBuilder.create().uv(44, 15).cuboid(-8.0F, -1.0F, -1.0F, 8.0F, 1.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, -0.1309F, -0.7854F));
		ModelPartData leftMiddleHindLeg = modelPartData.addChild("left_middle_hind_leg", ModelPartBuilder.create(), ModelTransform.pivot(4.0F, 18.5F, 1.5F));
		leftMiddleHindLeg.addChild("cube_r10", ModelPartBuilder.create().uv(42, 42).cuboid(0.0F, -1.0F, -1.0F, 8.0F, 1.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, -0.1309F, 0.7854F));
		ModelPartData rightMiddleHindLeg = modelPartData.addChild("right_middle_hind_leg", ModelPartBuilder.create(), ModelTransform.pivot(-4.0F, 18.5F, 1.5F));
		rightMiddleHindLeg.addChild("cube_r11", ModelPartBuilder.create().uv(42, 29).cuboid(-8.0F, -1.0F, -1.0F, 8.0F, 1.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.1309F, -0.7854F));
		ModelPartData leftHindLeg = modelPartData.addChild("left_hind_leg", ModelPartBuilder.create(), ModelTransform.pivot(4.0F, 18.5F, 4.5F));
		leftHindLeg.addChild("cube_r12", ModelPartBuilder.create().uv(42, 26).cuboid(0.0F, -1.0F, -1.0F, 8.0F, 1.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, -0.2618F, 0.7854F));
		ModelPartData rightHindLeg = modelPartData.addChild("right_hind_leg", ModelPartBuilder.create(), ModelTransform.pivot(-4.0F, 18.5F, 4.5F));
		rightHindLeg.addChild("cube_r13", ModelPartBuilder.create().uv(30, 9).cuboid(-8.0F, -1.0F, -1.0F, 8.0F, 1.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.2618F, -0.7854F));
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
		this.head.yaw = headYaw * (float) (Math.PI / 180.0);
		this.head.pitch = headPitch * (float) (Math.PI / 180.0);
		float i = -(MathHelper.cos(limbAngle * 0.6662F * 2.0F + 0.0F) * 0.4F) * limbDistance;
		float j = -(MathHelper.cos(limbAngle * 0.6662F * 2.0F + (float) Math.PI) * 0.4F) * limbDistance;
		float k = -(MathHelper.cos(limbAngle * 0.6662F * 2.0F + (float) (Math.PI / 2)) * 0.4F) * limbDistance;
		float l = -(MathHelper.cos(limbAngle * 0.6662F * 2.0F + (float) (Math.PI * 3.0 / 2.0)) * 0.4F) * limbDistance;
		float m = Math.abs(MathHelper.sin(limbAngle * 0.6662F + 0.0F) * 0.4F) * limbDistance;
		float n = Math.abs(MathHelper.sin(limbAngle * 0.6662F + (float) Math.PI) * 0.4F) * limbDistance;
		float o = Math.abs(MathHelper.sin(limbAngle * 0.6662F + (float) (Math.PI / 2)) * 0.4F) * limbDistance;
		float p = Math.abs(MathHelper.sin(limbAngle * 0.6662F + (float) (Math.PI * 3.0 / 2.0)) * 0.4F) * limbDistance;
		this.rightHindLeg.yaw = i;
		this.leftHindLeg.yaw = -i;
		this.rightMiddleHindLeg.yaw = j;
		this.leftMiddleHindLeg.yaw = -j;
		this.rightMiddleFrontLeg.yaw = k;
		this.leftMiddleFrontLeg.yaw = -k;
		this.rightFrontLeg.yaw = l;
		this.leftFrontLeg.yaw = -l;
		this.rightHindLeg.roll = m;
		this.leftHindLeg.roll = -m;
		this.rightMiddleHindLeg.roll = n;
		this.leftMiddleHindLeg.roll = -n;
		this.rightMiddleFrontLeg.roll = o;
		this.leftMiddleFrontLeg.roll = -o;
		this.rightFrontLeg.roll = p;
		this.leftFrontLeg.roll = -p;
	}
}