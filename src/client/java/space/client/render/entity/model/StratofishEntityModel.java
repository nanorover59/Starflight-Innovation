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
import space.entity.StratofishEntity;

public class StratofishEntityModel<T extends StratofishEntity> extends SinglePartEntityModel<T>
{
	private final ModelPart root;
	private final ModelPart left;
	private final ModelPart right;
	
	public StratofishEntityModel(ModelPart root)
	{
		this.root = root;
		this.left = root.getChild("left");
		this.right = root.getChild("right");
	}
	
	public static TexturedModelData getTexturedModelData()
	{
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		modelPartData.addChild("left", ModelPartBuilder.create().uv(0, 8).cuboid(1.0F, 0.0F, -3.0F, 15.0F, 0.0F, 8.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 24.0F, 0.0F));
		modelPartData.addChild("right", ModelPartBuilder.create().uv(0, 0).cuboid(-16.0F, 0.0F, -3.0F, 15.0F, 0.0F, 8.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 24.0F, 0.0F));
		modelPartData.addChild("base", ModelPartBuilder.create().uv(0, 16).cuboid(-1.0F, -2.0F, -7.0F, 2.0F, 4.0F, 14.0F, new Dilation(0.0F))
		.uv(0, 20).cuboid(-1.0F, -1.0F, -11.0F, 2.0F, 3.0F, 4.0F, new Dilation(0.0F))
		.uv(18, 16).cuboid(-5.0F, 0.0F, 6.0F, 4.0F, 0.0F, 8.0F, new Dilation(0.0F))
		.uv(10, 16).cuboid(1.0F, 0.0F, 6.0F, 4.0F, 0.0F, 8.0F, new Dilation(0.0F))
		.uv(0, 12).cuboid(0.0F, -4.0F, 3.0F, 0.0F, 2.0F, 6.0F, new Dilation(0.0F))
		.uv(0, 0).cuboid(-1.0F, -1.0F, 7.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 24.0F, 0.0F));
		return TexturedModelData.of(modelData, 64, 64);
	}
	
	@Override
    public ModelPart getPart()
	{
        return this.root;
    }
	
	@Override
	public void setAngles(T stratofishEntity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
	{
		float flap = ((float) stratofishEntity.getWingFlapTickOffset() + ageInTicks) * 7.448451f * ((float) Math.PI / 180);
        this.left.roll = MathHelper.cos(flap) * 16.0f * ((float) Math.PI / 180);
        this.right.roll = -this.left.roll;
	}
}