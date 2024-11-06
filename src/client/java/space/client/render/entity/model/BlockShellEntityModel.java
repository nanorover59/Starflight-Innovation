package space.client.render.entity.model;

import java.util.ArrayList;

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
import net.minecraft.entity.Entity;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;

public class BlockShellEntityModel<T extends Entity> extends SinglePartEntityModel<T>
{
	private final ModelPart root;
	private final ArrayList<ModelPart> leftLegs;
	private final ArrayList<ModelPart> rightLegs;
	
	public BlockShellEntityModel(ModelPart root)
	{
		this.root = root;
		this.leftLegs = new ArrayList<ModelPart>();
		this.rightLegs = new ArrayList<ModelPart>();
		
		for(int i = 0; i < 6; i++)
		{
			leftLegs.add(root.getChild("legl" + i));
			rightLegs.add(root.getChild("legr" + i));
		}
	}

	public static TexturedModelData getTexturedModelData()
	{
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		modelPartData.addChild("body", ModelPartBuilder.create().uv(0, 0).cuboid(-6.0F, -1.0F, -6.0F, 12.0F, 2.0F, 12.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 21.0F, 0.0F));
		ModelPartData eyes = modelPartData.addChild("eyes", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 24.0F, 0.0F));
		eyes.addChild("cube_r1", ModelPartBuilder.create().uv(0, 14).mirrored().cuboid(-1.0F, -1.0F, -6.0F, 2.0F, 2.0F, 6.0F, new Dilation(0.0F)).mirrored(false), ModelTransform.of(1.0F, -3.0F, -6.0F, -0.2706F, -0.2527F, 0.0692F));
		eyes.addChild("cube_r2", ModelPartBuilder.create().uv(0, 14).mirrored().cuboid(-1.0F, -1.0F, -6.0F, 2.0F, 2.0F, 6.0F, new Dilation(0.0F)).mirrored(false), ModelTransform.of(3.0F, -3.0F, -6.0F, -0.3365F, -0.6699F, 0.2139F));
		eyes.addChild("cube_r3", ModelPartBuilder.create().uv(0, 14).cuboid(-1.0F, -1.0F, -6.0F, 2.0F, 2.0F, 6.0F, new Dilation(0.0F)), ModelTransform.of(-3.0F, -3.0F, -6.0F, -0.3365F, 0.6699F, -0.2139F));
		eyes.addChild("cube_r4", ModelPartBuilder.create().uv(0, 14).cuboid(-1.0F, -1.0F, -6.0F, 2.0F, 2.0F, 6.0F, new Dilation(0.0F)), ModelTransform.of(-1.0F, -3.0F, -6.0F, -0.2706F, 0.2527F, -0.0692F));
		
		for(int i = 0; i < 6; i++)
		{
			modelPartData.addChild("legl" + i, ModelPartBuilder.create().uv(16, 14).cuboid(0.0F, -1.0F, -1.0F, 6.0F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(6.0F, 22.0F, -5.0F + i * 2.0F));
			modelPartData.addChild("legr" + i, ModelPartBuilder.create().uv(16, 14).mirrored().cuboid(-6.0F, -1.0F, -1.0F, 6.0F, 2.0F, 2.0F, new Dilation(0.0F)).mirrored(false), ModelTransform.pivot(-6.0F, 22.0F, -5.0F + i * 2.0F));
		}
		
		return TexturedModelData.of(modelData, 64, 64);
	}
	
	@Override
	public ModelPart getPart()
	{
		return this.root;
	}

	@Override
	public void setAngles(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
	{
		for(int i = 0; i < 6; i++)
		{
			leftLegs.get(i).roll = MathHelper.cos((float) (limbSwing + i * (Math.PI / 6.0)));
			rightLegs.get(i).roll = -MathHelper.cos((float) (limbSwing + i * (Math.PI / 6.0)));
		}
	}
	
	@Override
	public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, int color)
	{
		super.render(matrices, vertexConsumer, light, overlay, ColorHelper.Argb.fromFloats(0.8f, 1.0f, 1.0f, 1.0f));
	}
}