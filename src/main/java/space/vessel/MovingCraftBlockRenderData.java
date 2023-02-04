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
import net.minecraft.client.render.OverlayTexture;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.World;
import space.entity.MovingCraftEntity;
import space.mixin.client.BlockModelRendererMixin;

@Environment(value=EnvType.CLIENT)
public class MovingCraftBlockRenderData
{
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
	
	public void renderBlock(BlockRenderView world, MovingCraftEntity entity, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, Random random, BlockPos centerBlockPos, BlockPos centerBlockPosInitial, int lightLevel, float craftYaw)
	{
		// Block entity render.
		if(blockState.getRenderType() == BlockRenderType.ENTITYBLOCK_ANIMATED && blockState.getBlock() instanceof BlockWithEntity)
		{
			BlockWithEntity blockWithEntity = (BlockWithEntity) blockState.getBlock();
			BlockEntity blockEntity = blockWithEntity.createBlockEntity(position.add(centerBlockPosInitial), blockState);
			blockEntity.setWorld((World) world);
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
        
        for(Direction direction : DIRECTIONS)
        {
            random.setSeed(seed);
            List<BakedQuad> list = model.getQuads(blockState, direction, random);
            
            if(list.isEmpty() || !canRenderSide(direction))
            	continue;
            
            ((BlockModelRendererMixin) blockModelRenderer).callRenderQuadsFlat(world, blockState, position, lightLevel, OverlayTexture.DEFAULT_UV, false, matrixStack, vertexConsumer, list, bitSet);
        }
        
        random.setSeed(seed);
        List<BakedQuad> list = model.getQuads(blockState, null, random);
        
        if(!list.isEmpty())
        	((BlockModelRendererMixin) blockModelRenderer).callRenderQuadsFlat(world, blockState, position, lightLevel, OverlayTexture.DEFAULT_UV, false, matrixStack, vertexConsumer, list, bitSet);
        
        matrixStack.pop();
	}
}
