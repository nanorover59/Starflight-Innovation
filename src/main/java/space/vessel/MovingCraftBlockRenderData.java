package space.vessel;

import java.util.BitSet;
import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import space.block.RocketThrusterBlock;
import space.entity.MovingCraftEntity;
import space.entity.RocketEntity;
import space.mixin.client.BlockModelRendererMixin;

@Environment(value=EnvType.CLIENT)
public class MovingCraftBlockRenderData
{
	private static final Identifier THRUSTER_PLUME_TEXTURE = new Identifier("space:textures/entity/thruster_plume.png");
	private static final Identifier MACH_DIAMOND_TEXTURE = new Identifier("space:textures/entity/mach_diamond.png");
	 private static final RenderLayer THRUSTER_PLUME_LAYER = RenderLayer.getEntityTranslucent(THRUSTER_PLUME_TEXTURE);
    private static final RenderLayer MACH_DIAMOND_LAYER = RenderLayer.getEntityTranslucent(MACH_DIAMOND_TEXTURE);
	
	private static final Direction[] DIRECTIONS = Direction.values();
	private BlockState blockState;
	private BlockPos position;
	private boolean[] sidesShowing = new boolean[6];
	
	public MovingCraftBlockRenderData(BlockState blockState, BlockPos position, boolean[] sidesShowing)
	{
		this.blockState = blockState;
		this.position = position;
		
		for(int i = 0; i < 6; i++)
			this.sidesShowing[i] = sidesShowing[i];
	}

	public BlockState getBlockState()
	{
		return blockState;
	}

	public BlockPos getPosition()
	{
		return position;
	}
	
	public boolean canRenderSide(Direction direction)
	{
		switch(direction)
		{
			case NORTH:
				return sidesShowing[0];
			case EAST:
				return sidesShowing[1];
			case SOUTH:
				return sidesShowing[2];
			case WEST:
				return sidesShowing[3];
			case UP:
				return sidesShowing[4];
			case DOWN:
				return sidesShowing[5];
			default:
				return false;
		}
	}
	
	public void renderBlock(BlockRenderView world, MovingCraftEntity entity, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, Random random, BlockPos centerBlockPos, BlockPos centerBlockPosInitial)
	{
		// Plume effects for rocket thrusters.
		if(entity instanceof RocketEntity)
		{
			RocketEntity rocketEntity = (RocketEntity) entity;
			
			if(blockState.getBlock() instanceof RocketThrusterBlock)
			{
				MinecraftClient client = MinecraftClient.getInstance();
				
				if(rocketEntity.getThrottle() > 0.0F)
				{
					// Render mach diamonds for under expanded thrust or a simple plume for over expanded thrust.
					if(rocketEntity.getThrustUnderexpanded())
					{
						boolean b = false;
						
						for(int i = 0; i < 5; i++)
						{
							matrixStack.push();
							matrixStack.translate(position.getX() + (b ? -0.01 : 0.01), position.getY() - i * 0.75, position.getZ() + (b ? -0.01 : 0.01));
							matrixStack.scale(1.0F - i * 0.1F, 1.25F, 1.0F - i * 0.1F);
							matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-client.gameRenderer.getCamera().getYaw() + 180.0F));
					        MatrixStack.Entry entry = matrixStack.peek();
					        Matrix4f matrix4f = entry.getPositionMatrix();
					        Matrix3f matrix3f = entry.getNormalMatrix();
					        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(MACH_DIAMOND_LAYER);
					        thrusterPlumeVertex(vertexConsumer, matrix4f, matrix3f, 0.0f, 0.0f, 0, 1, 220 - i * 10);
					        thrusterPlumeVertex(vertexConsumer, matrix4f, matrix3f, 1.0f, 0.0f, 1, 1, 220 - i * 10);
					        thrusterPlumeVertex(vertexConsumer, matrix4f, matrix3f, 1.0f, 1.0f, 1, 0, 220 - i * 10);
					        thrusterPlumeVertex(vertexConsumer, matrix4f, matrix3f, 0.0f, 1.0f, 0, 0, 220 - i * 10);
					        matrixStack.pop();
					        b ^= true;
						}
					}
					else
					{
						matrixStack.push();
						matrixStack.translate(position.getX(), position.getY() - 0.8, position.getZ());
						matrixStack.scale(1.75F, 3.0F, 1.75F);
						matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-client.gameRenderer.getCamera().getYaw() + 180.0F));
				        MatrixStack.Entry entry = matrixStack.peek();
				        Matrix4f matrix4f = entry.getPositionMatrix();
				        Matrix3f matrix3f = entry.getNormalMatrix();
				        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(THRUSTER_PLUME_LAYER);
				        thrusterPlumeVertex(vertexConsumer, matrix4f, matrix3f, 0.0f, 0.0f, 0, 1, 220);
				        thrusterPlumeVertex(vertexConsumer, matrix4f, matrix3f, 1.0f, 0.0f, 1, 1, 220);
				        thrusterPlumeVertex(vertexConsumer, matrix4f, matrix3f, 1.0f, 1.0f, 1, 0, 220);
				        thrusterPlumeVertex(vertexConsumer, matrix4f, matrix3f, 0.0f, 1.0f, 0, 0, 220);
				        matrixStack.pop();
					}
				}
			}
		}
		
		// Block entity render.
		if(blockState.getRenderType() == BlockRenderType.ENTITYBLOCK_ANIMATED && blockState.getBlock() instanceof BlockWithEntity)
		{
			BlockWithEntity blockWithEntity = (BlockWithEntity) blockState.getBlock();
			BlockEntity blockEntity = blockWithEntity.createBlockEntity(position.add(centerBlockPosInitial), blockState);
			BlockEntityRenderDispatcher blockEntityRenderDispatcher = MinecraftClient.getInstance().getBlockEntityRenderDispatcher();
			int light = WorldRenderer.getLightmapCoordinates(world, blockState, position.add(centerBlockPos));
			matrixStack.push();
			matrixStack.translate(position.getX(), position.getY(), position.getZ());
			
			if(blockState.getProperties().contains(HorizontalFacingBlock.FACING))
			{
				Direction direction = blockState.get(HorizontalFacingBlock.FACING).getOpposite();
				matrixStack.multiply(new Quaternion(0.0F, direction == Direction.NORTH || direction == Direction.SOUTH ? direction.asRotation() + 180.0F : direction.asRotation(), 0.0F, true));
			}
			
			matrixStack.translate(-0.5, 0.0, -0.5);
			blockEntityRenderDispatcher.get(blockEntity).render(blockEntity, 0.0F, matrixStack, vertexConsumerProvider, light, OverlayTexture.DEFAULT_UV);
			matrixStack.pop();
			return;
		}
		
		// Block model render.
		if(blockState.getRenderType() != BlockRenderType.MODEL || blockState.getRenderType() == BlockRenderType.INVISIBLE)
			return;
		
		BlockRenderManager blockRenderManager = MinecraftClient.getInstance().getBlockRenderManager();
		BlockModelRenderer blockModelRenderer = blockRenderManager.getModelRenderer();
		BitSet bitSet = new BitSet(3);
		VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayers.getMovingBlockLayer(blockState));
        BakedModel model = blockRenderManager.getModel(blockState);
        matrixStack.push();
        matrixStack.translate(position.getX() - 0.5, position.getY(), position.getZ() - 0.5);
        long seed = blockState.getRenderingSeed(position.add(centerBlockPosInitial));
        BlockPos.Mutable mutable = position.add(centerBlockPos).mutableCopy();
        
        for(Direction direction : DIRECTIONS)
        {
            random.setSeed(seed);
            List<BakedQuad> list = model.getQuads(blockState, direction, random);
            
            if(list.isEmpty() || !canRenderSide(direction))
            	continue;
            
            mutable.set((Vec3i) position.add(centerBlockPos), direction);
            int light = WorldRenderer.getLightmapCoordinates(world, blockState, mutable);
            ((BlockModelRendererMixin) blockModelRenderer).callRenderQuadsFlat(world, blockState, position, light, OverlayTexture.DEFAULT_UV, false, matrixStack, vertexConsumer, list, bitSet);
        }
        
        random.setSeed(seed);
        List<BakedQuad> list = model.getQuads(blockState, null, random);
        
        if(!list.isEmpty())
        	((BlockModelRendererMixin) blockModelRenderer).callRenderQuadsFlat(world, blockState, position, -1, OverlayTexture.DEFAULT_UV, true, matrixStack, vertexConsumer, list, bitSet);
        
        matrixStack.pop();
	}
	
	private static void thrusterPlumeVertex(VertexConsumer buffer, Matrix4f matrix, Matrix3f normalMatrix, float x, float y, int u, int v, int alpha)
	{
        buffer.vertex(matrix, x - 0.5F, y - 0.5F, 0.0F).color(255, 255, 255, alpha).texture(u, v).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE).normal(normalMatrix, 0.0f, 1.0f, 0.0f).next();
    }
}
