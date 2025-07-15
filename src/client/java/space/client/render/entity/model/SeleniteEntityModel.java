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
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.entity.LivingEntity;
import space.entity.SeleniteEntity;

@Environment(value = EnvType.CLIENT)
public class SeleniteEntityModel<T extends LivingEntity> extends BipedEntityModel<T>
{
	public SeleniteEntityModel(ModelPart modelPart)
	{
		super(modelPart);
	}

	public static TexturedModelData getTexturedModelData()
	{
		ModelData modelData = BipedEntityModel.getModelData(Dilation.NONE, 0.0f);
		ModelPartData modelPartData = modelData.getRoot();
        modelPartData.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 0).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.pivot(0.0f, 8.0f, 0.0f));
        modelPartData.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(8, 16).cuboid(-2.0f, 0.0f, -1.0f, 4.0f, 8.0f, 2.0f), ModelTransform.pivot(0.0f, 8.0f, 0.0f));
        modelPartData.addChild(EntityModelPartNames.RIGHT_ARM, ModelPartBuilder.create().uv(0, 16).cuboid(-1.0f, -2.0f, -1.0f, 2.0f, 8.0f, 2.0f), ModelTransform.pivot(-3.0f, 10.0f, 0.0f));
        modelPartData.addChild(EntityModelPartNames.LEFT_ARM, ModelPartBuilder.create().uv(0, 16).mirrored().cuboid(-1.0f, -2.0f, -1.0f, 2.0f, 8.0f, 2.0f), ModelTransform.pivot(3.0f, 10.0f, 0.0f));
        modelPartData.addChild(EntityModelPartNames.RIGHT_LEG, ModelPartBuilder.create().uv(0, 16).cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 8.0f, 2.0f), ModelTransform.pivot(-1.0f, 16.0f, 0.0f));
        modelPartData.addChild(EntityModelPartNames.LEFT_LEG, ModelPartBuilder.create().uv(0, 16).mirrored().cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 8.0f, 2.0f), ModelTransform.pivot(1.0f, 16.0f, 0.0f));
		return TexturedModelData.of(modelData, 32, 32);
	}

	@Override
	public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch)
	{
		super.setAngles(entity, limbAngle, limbDistance, animationProgress, headYaw, headPitch);
		this.head.visible = true;
		
		boolean sitting = this.riding || ((SeleniteEntity) entity).isInSittingPose();
		float yOffset = sitting ? 4.15f : 0.0f;
		
		this.body.pivotY = 8.0f + yOffset;
        this.body.pivotZ = 0.0f;
        this.head.pivotY = 8.0f + yOffset;
        this.head.pivotZ = 0.0f;
        this.hat.visible = false;
        this.rightArm.setPivot(-3.0f, 10.0f + yOffset, 0.0f);
        this.leftArm.setPivot(3.0f, 10.0f + yOffset, 0.0f);
        this.rightLeg.setPivot(-1.0f, 16.0f + yOffset, 0.0f);
        this.leftLeg.setPivot(1.0f, 16.0f + yOffset, 0.0f);
        
        if(sitting)
        {
            this.rightArm.pitch += -0.62831855f;
            this.leftArm.pitch += -0.62831855f;
            this.rightLeg.pitch = -1.4137167f;
            this.rightLeg.yaw = 0.31415927f;
            this.rightLeg.roll = 0.07853982f;
            this.leftLeg.pitch = -1.4137167f;
            this.leftLeg.yaw = -0.31415927f;
            this.leftLeg.roll = -0.07853982f;
        }
	}
}