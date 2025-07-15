package space.client.render.entity.animation;

import net.minecraft.client.render.entity.animation.Animation;
import net.minecraft.client.render.entity.animation.AnimationHelper;
import net.minecraft.client.render.entity.animation.Keyframe;
import net.minecraft.client.render.entity.animation.Transformation;

public class MimicAnimations
{
	public static final Animation OPEN = Animation.Builder.create(1.5F)
	.addBoneAnimation("body", new Transformation(Transformation.Targets.TRANSLATE, 
		new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, -4.0F, 0.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(0.5F, AnimationHelper.createTranslationalVector(0.0F, 0.84F, 0.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.0F, AnimationHelper.createTranslationalVector(0.0F, 2.67F, 0.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.5F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
	))
	.addBoneAnimation("leftFrontLeg", new Transformation(Transformation.Targets.ROTATE, 
		new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, -42.5F, 45.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.5F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
	))
	.addBoneAnimation("leftFrontLeg", new Transformation(Transformation.Targets.TRANSLATE, 
		new Keyframe(0.0F, AnimationHelper.createTranslationalVector(-5.0F, -4.0F, 1.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(0.5F, AnimationHelper.createTranslationalVector(-3.33F, 0.33F, 0.67F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.0F, AnimationHelper.createTranslationalVector(-1.66F, 2.17F, 0.34F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.5F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
	))
	.addBoneAnimation("leftFrontLegLower", new Transformation(Transformation.Targets.ROTATE, 
		new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 75.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.5F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
	))
	.addBoneAnimation("leftMiddleFrontLeg", new Transformation(Transformation.Targets.ROTATE, 
		new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, -15.0F, 45.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.5F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
	))
	.addBoneAnimation("leftMiddleFrontLeg", new Transformation(Transformation.Targets.TRANSLATE, 
		new Keyframe(0.0F, AnimationHelper.createTranslationalVector(-6.0F, -4.0F, 0.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(0.5F, AnimationHelper.createTranslationalVector(-4.0F, 0.33F, 0.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.0F, AnimationHelper.createTranslationalVector(-2.0F, 2.17F, 0.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.5F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
	))
	.addBoneAnimation("leftMiddleFrontLegLower", new Transformation(Transformation.Targets.ROTATE, 
		new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 75.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.5F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
	))
	.addBoneAnimation("leftHindLeg", new Transformation(Transformation.Targets.ROTATE, 
		new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 42.5F, 45.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.5F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
	))
	.addBoneAnimation("leftHindLeg", new Transformation(Transformation.Targets.TRANSLATE, 
		new Keyframe(0.0F, AnimationHelper.createTranslationalVector(-5.0F, -4.0F, -1.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(0.5F, AnimationHelper.createTranslationalVector(-3.33F, 0.33F, -0.67F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.0F, AnimationHelper.createTranslationalVector(-1.66F, 2.17F, -0.33F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.5F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
	))
	.addBoneAnimation("leftHindLegLower", new Transformation(Transformation.Targets.ROTATE, 
		new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 75.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.5F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
	))
	.addBoneAnimation("leftAntenna", new Transformation(Transformation.Targets.ROTATE, 
		new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 25.0F, 0.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.5F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
	))
	.addBoneAnimation("leftAntenna", new Transformation(Transformation.Targets.TRANSLATE, 
		new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, -4.0F, 9.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(0.5F, AnimationHelper.createTranslationalVector(0.0F, 0.83F, 5.33F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.0F, AnimationHelper.createTranslationalVector(0.0F, 2.67F, 2.67F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.5F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
	))
	.addBoneAnimation("rightFrontLeg", new Transformation(Transformation.Targets.ROTATE, 
		new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 42.5F, -45.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.5F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
	))
	.addBoneAnimation("rightFrontLeg", new Transformation(Transformation.Targets.TRANSLATE, 
		new Keyframe(0.0F, AnimationHelper.createTranslationalVector(5.0F, -4.0F, 1.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(0.5F, AnimationHelper.createTranslationalVector(3.33F, 0.33F, 0.67F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.0F, AnimationHelper.createTranslationalVector(1.67F, 2.17F, 0.34F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.5F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
	))
	.addBoneAnimation("rightFrontLegLower", new Transformation(Transformation.Targets.ROTATE, 
		new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, -75.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.5F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
	))
	.addBoneAnimation("rightMiddleFrontLeg", new Transformation(Transformation.Targets.ROTATE, 
		new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 15.0F, -45.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.5F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
	))
	.addBoneAnimation("rightMiddleFrontLeg", new Transformation(Transformation.Targets.TRANSLATE, 
		new Keyframe(0.0F, AnimationHelper.createTranslationalVector(6.0F, -4.0F, 0.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(0.5F, AnimationHelper.createTranslationalVector(4.0F, 0.33F, 0.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.0F, AnimationHelper.createTranslationalVector(2.0F, 2.17F, 0.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.5F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
	))
	.addBoneAnimation("rightMiddleFrontLegLower", new Transformation(Transformation.Targets.ROTATE, 
		new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, -75.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.5F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
	))
	.addBoneAnimation("rightHindLeg", new Transformation(Transformation.Targets.ROTATE, 
		new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, -42.5F, -45.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.5F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
	))
	.addBoneAnimation("rightHindLeg", new Transformation(Transformation.Targets.TRANSLATE, 
		new Keyframe(0.0F, AnimationHelper.createTranslationalVector(5.0F, -4.0F, -1.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(0.5F, AnimationHelper.createTranslationalVector(3.33F, 0.33F, -0.67F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.0F, AnimationHelper.createTranslationalVector(1.67F, 2.17F, -0.33F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.5F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
	))
	.addBoneAnimation("rightHindLegLower", new Transformation(Transformation.Targets.ROTATE, 
		new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, -75.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.5F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
	))
	.addBoneAnimation("leftMiddleLeg", new Transformation(Transformation.Targets.ROTATE, 
		new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 15.0F, 45.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.5F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
	))
	.addBoneAnimation("leftMiddleLeg", new Transformation(Transformation.Targets.TRANSLATE, 
		new Keyframe(0.0F, AnimationHelper.createTranslationalVector(-6.0F, -4.0F, 0.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(0.5F, AnimationHelper.createTranslationalVector(-4.0F, 0.33F, 0.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.0F, AnimationHelper.createTranslationalVector(-2.0F, 2.17F, 0.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.5F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
	))
	.addBoneAnimation("leftMiddleLegLower", new Transformation(Transformation.Targets.ROTATE, 
		new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 75.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.5F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
	))
	.addBoneAnimation("rightMiddleLeg", new Transformation(Transformation.Targets.ROTATE, 
		new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, -15.0F, -45.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.5F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
	))
	.addBoneAnimation("rightMiddleLeg", new Transformation(Transformation.Targets.TRANSLATE, 
		new Keyframe(0.0F, AnimationHelper.createTranslationalVector(6.0F, -4.0F, 0.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(0.5F, AnimationHelper.createTranslationalVector(4.0F, 0.33F, 0.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.0F, AnimationHelper.createTranslationalVector(2.0F, 2.17F, 0.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.5F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
	))
	.addBoneAnimation("rightMiddleLegLower", new Transformation(Transformation.Targets.ROTATE, 
		new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, -75.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.5F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
	))
	.addBoneAnimation("rightAntenna", new Transformation(Transformation.Targets.ROTATE, 
		new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, -25.0F, 0.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.5F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
	))
	.addBoneAnimation("rightAntenna", new Transformation(Transformation.Targets.TRANSLATE, 
		new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, -4.0F, 9.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(0.5F, AnimationHelper.createTranslationalVector(0.0F, 0.83F, 5.33F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.0F, AnimationHelper.createTranslationalVector(0.0F, 2.67F, 2.67F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.5F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
	))
	.build();

public static final Animation CLOSE = Animation.Builder.create(1.5F)
	.addBoneAnimation("body", new Transformation(Transformation.Targets.TRANSLATE, 
		new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(0.5F, AnimationHelper.createTranslationalVector(0.0F, 2.67F, 0.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.0F, AnimationHelper.createTranslationalVector(0.0F, 0.84F, 0.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.5F, AnimationHelper.createTranslationalVector(0.0F, -4.0F, 0.0F), Transformation.Interpolations.LINEAR)
	))
	.addBoneAnimation("leftFrontLeg", new Transformation(Transformation.Targets.ROTATE, 
		new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.5F, AnimationHelper.createRotationalVector(0.0F, -42.5F, 45.0F), Transformation.Interpolations.LINEAR)
	))
	.addBoneAnimation("leftFrontLeg", new Transformation(Transformation.Targets.TRANSLATE, 
		new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(0.5F, AnimationHelper.createTranslationalVector(-1.66F, 2.17F, 0.34F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.0F, AnimationHelper.createTranslationalVector(-3.33F, 0.33F, 0.67F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.5F, AnimationHelper.createTranslationalVector(-5.0F, -4.0F, 1.0F), Transformation.Interpolations.LINEAR)
	))
	.addBoneAnimation("leftFrontLegLower", new Transformation(Transformation.Targets.ROTATE, 
		new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.5F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 75.0F), Transformation.Interpolations.LINEAR)
	))
	.addBoneAnimation("leftMiddleFrontLeg", new Transformation(Transformation.Targets.ROTATE, 
		new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.5F, AnimationHelper.createRotationalVector(0.0F, -15.0F, 45.0F), Transformation.Interpolations.LINEAR)
	))
	.addBoneAnimation("leftMiddleFrontLeg", new Transformation(Transformation.Targets.TRANSLATE, 
		new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(0.5F, AnimationHelper.createTranslationalVector(-2.0F, 2.17F, 0.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.0F, AnimationHelper.createTranslationalVector(-4.0F, 0.33F, 0.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.5F, AnimationHelper.createTranslationalVector(-6.0F, -4.0F, 0.0F), Transformation.Interpolations.LINEAR)
	))
	.addBoneAnimation("leftMiddleFrontLegLower", new Transformation(Transformation.Targets.ROTATE, 
		new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.5F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 75.0F), Transformation.Interpolations.LINEAR)
	))
	.addBoneAnimation("leftHindLeg", new Transformation(Transformation.Targets.ROTATE, 
		new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.5F, AnimationHelper.createRotationalVector(0.0F, 42.5F, 45.0F), Transformation.Interpolations.LINEAR)
	))
	.addBoneAnimation("leftHindLeg", new Transformation(Transformation.Targets.TRANSLATE, 
		new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(0.5F, AnimationHelper.createTranslationalVector(-1.66F, 2.17F, -0.33F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.0F, AnimationHelper.createTranslationalVector(-3.33F, 0.33F, -0.67F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.5F, AnimationHelper.createTranslationalVector(-5.0F, -4.0F, -1.0F), Transformation.Interpolations.LINEAR)
	))
	.addBoneAnimation("leftHindLegLower", new Transformation(Transformation.Targets.ROTATE, 
		new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.5F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 75.0F), Transformation.Interpolations.LINEAR)
	))
	.addBoneAnimation("leftAntenna", new Transformation(Transformation.Targets.ROTATE, 
		new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.5F, AnimationHelper.createRotationalVector(0.0F, 25.0F, 0.0F), Transformation.Interpolations.LINEAR)
	))
	.addBoneAnimation("leftAntenna", new Transformation(Transformation.Targets.TRANSLATE, 
		new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(0.5F, AnimationHelper.createTranslationalVector(0.0F, 2.67F, 2.67F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.0F, AnimationHelper.createTranslationalVector(0.0F, 0.83F, 5.33F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.5F, AnimationHelper.createTranslationalVector(0.0F, -4.0F, 9.0F), Transformation.Interpolations.LINEAR)
	))
	.addBoneAnimation("rightFrontLeg", new Transformation(Transformation.Targets.ROTATE, 
		new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.5F, AnimationHelper.createRotationalVector(0.0F, 42.5F, -45.0F), Transformation.Interpolations.LINEAR)
	))
	.addBoneAnimation("rightFrontLeg", new Transformation(Transformation.Targets.TRANSLATE, 
		new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(0.5F, AnimationHelper.createTranslationalVector(1.67F, 2.17F, 0.34F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.0F, AnimationHelper.createTranslationalVector(3.33F, 0.33F, 0.67F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.5F, AnimationHelper.createTranslationalVector(5.0F, -4.0F, 1.0F), Transformation.Interpolations.LINEAR)
	))
	.addBoneAnimation("rightFrontLegLower", new Transformation(Transformation.Targets.ROTATE, 
		new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.5F, AnimationHelper.createRotationalVector(0.0F, 0.0F, -75.0F), Transformation.Interpolations.LINEAR)
	))
	.addBoneAnimation("rightMiddleFrontLeg", new Transformation(Transformation.Targets.ROTATE, 
		new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.5F, AnimationHelper.createRotationalVector(0.0F, 15.0F, -45.0F), Transformation.Interpolations.LINEAR)
	))
	.addBoneAnimation("rightMiddleFrontLeg", new Transformation(Transformation.Targets.TRANSLATE, 
		new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(0.5F, AnimationHelper.createTranslationalVector(2.0F, 2.17F, 0.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.0F, AnimationHelper.createTranslationalVector(4.0F, 0.33F, 0.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.5F, AnimationHelper.createTranslationalVector(6.0F, -4.0F, 0.0F), Transformation.Interpolations.LINEAR)
	))
	.addBoneAnimation("rightMiddleFrontLegLower", new Transformation(Transformation.Targets.ROTATE, 
		new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.5F, AnimationHelper.createRotationalVector(0.0F, 0.0F, -75.0F), Transformation.Interpolations.LINEAR)
	))
	.addBoneAnimation("rightHindLeg", new Transformation(Transformation.Targets.ROTATE, 
		new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.5F, AnimationHelper.createRotationalVector(0.0F, -42.5F, -45.0F), Transformation.Interpolations.LINEAR)
	))
	.addBoneAnimation("rightHindLeg", new Transformation(Transformation.Targets.TRANSLATE, 
		new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(0.5F, AnimationHelper.createTranslationalVector(1.67F, 2.17F, -0.33F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.0F, AnimationHelper.createTranslationalVector(3.33F, 0.33F, -0.67F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.5F, AnimationHelper.createTranslationalVector(5.0F, -4.0F, -1.0F), Transformation.Interpolations.LINEAR)
	))
	.addBoneAnimation("rightHindLegLower", new Transformation(Transformation.Targets.ROTATE, 
		new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.5F, AnimationHelper.createRotationalVector(0.0F, 0.0F, -75.0F), Transformation.Interpolations.LINEAR)
	))
	.addBoneAnimation("leftMiddleLeg", new Transformation(Transformation.Targets.ROTATE, 
		new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.5F, AnimationHelper.createRotationalVector(0.0F, 15.0F, 45.0F), Transformation.Interpolations.LINEAR)
	))
	.addBoneAnimation("leftMiddleLeg", new Transformation(Transformation.Targets.TRANSLATE, 
		new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(0.5F, AnimationHelper.createTranslationalVector(-2.0F, 2.17F, 0.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.0F, AnimationHelper.createTranslationalVector(-4.0F, 0.33F, 0.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.5F, AnimationHelper.createTranslationalVector(-6.0F, -4.0F, 0.0F), Transformation.Interpolations.LINEAR)
	))
	.addBoneAnimation("leftMiddleLegLower", new Transformation(Transformation.Targets.ROTATE, 
		new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.5F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 75.0F), Transformation.Interpolations.LINEAR)
	))
	.addBoneAnimation("rightMiddleLeg", new Transformation(Transformation.Targets.ROTATE, 
		new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.5F, AnimationHelper.createRotationalVector(0.0F, -15.0F, -45.0F), Transformation.Interpolations.LINEAR)
	))
	.addBoneAnimation("rightMiddleLeg", new Transformation(Transformation.Targets.TRANSLATE, 
		new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(0.5F, AnimationHelper.createTranslationalVector(2.0F, 2.17F, 0.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.0F, AnimationHelper.createTranslationalVector(4.0F, 0.33F, 0.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.5F, AnimationHelper.createTranslationalVector(6.0F, -4.0F, 0.0F), Transformation.Interpolations.LINEAR)
	))
	.addBoneAnimation("rightMiddleLegLower", new Transformation(Transformation.Targets.ROTATE, 
		new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.5F, AnimationHelper.createRotationalVector(0.0F, 0.0F, -75.0F), Transformation.Interpolations.LINEAR)
	))
	.addBoneAnimation("rightAntenna", new Transformation(Transformation.Targets.ROTATE, 
		new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.5F, AnimationHelper.createRotationalVector(0.0F, -25.0F, 0.0F), Transformation.Interpolations.LINEAR)
	))
	.addBoneAnimation("rightAntenna", new Transformation(Transformation.Targets.TRANSLATE, 
		new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
		new Keyframe(0.5F, AnimationHelper.createTranslationalVector(0.0F, 2.67F, 2.67F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.0F, AnimationHelper.createTranslationalVector(0.0F, 0.83F, 5.33F), Transformation.Interpolations.LINEAR),
		new Keyframe(1.5F, AnimationHelper.createTranslationalVector(0.0F, -4.0F, 9.0F), Transformation.Interpolations.LINEAR)
	))
	.build();
}