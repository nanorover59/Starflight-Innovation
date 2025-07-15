package space.client.render.entity;

import java.util.BitSet;
import java.util.List;

import org.joml.Quaternionf;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
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
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.World;
import space.craft.MovingCraftBlock;
import space.entity.MovingCraftEntity;
import space.mixin.client.BlockModelRendererInvokerMixin;

@Environment(value=EnvType.CLIENT)
public class MovingCraftEntityRenderer extends EntityRenderer<MovingCraftEntity>
{
	public MovingCraftEntityRenderer(EntityRendererFactory.Context context)
	{
        super(context);
        this.shadowRadius = 0.0f;
    }
	
	@Override
    public void render(MovingCraftEntity entity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i)
	{
		if(entity.clientQuaternionPrevious != null && entity.clientQuaternion != null)
			matrixStack.multiply(new Quaternionf(entity.clientQuaternionPrevious).slerp(entity.clientQuaternion, g));
		else if(entity.clientQuaternion != null)
			matrixStack.multiply(entity.clientQuaternion);
		
		World world = entity.getEntityWorld();
		Random random = Random.createLocal();
		BlockPos centerBlockPos = entity.getBlockPos();
		BlockPos centerBlockPosInitial = entity.getInitialBlockPos();
		int lightLevel = WorldRenderer.getLightmapCoordinates(world, Blocks.AIR.getDefaultState(), centerBlockPos);
		
		for(MovingCraftBlock block : entity.getExposedBlocks())
			renderBlock(world, matrixStack, vertexConsumerProvider, random, block, centerBlockPos, centerBlockPosInitial, lightLevel, entity.getYaw());
	}
	
	@Override
	public Identifier getTexture(MovingCraftEntity var1)
	{
		return PlayerScreenHandler.BLOCK_ATLAS_TEXTURE;
	}
	
	public static void renderBlock(BlockRenderView world, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, Random random, MovingCraftBlock block, BlockPos centerBlockPos, BlockPos centerBlockPosInitial, int lightLevel, float craftYaw)
	{
		BlockState blockState = block.getBlockState();
		BlockPos position = block.getPosition();
		
		// Block entity render.
		if(blockState.getRenderType() == BlockRenderType.ENTITYBLOCK_ANIMATED && blockState.getBlock() instanceof BlockEntityProvider)
		{
			BlockEntityProvider blockWithEntity = (BlockEntityProvider) blockState.getBlock();
			BlockEntity blockEntity = blockWithEntity.createBlockEntity(position.add(centerBlockPosInitial), blockState);
			blockEntity.setWorld((World) world);
			BlockEntityRenderDispatcher blockEntityRenderDispatcher = MinecraftClient.getInstance().getBlockEntityRenderDispatcher();
			int light = WorldRenderer.getLightmapCoordinates(world, blockState, position.add(centerBlockPos));
			matrixStack.push();
			matrixStack.translate(position.getX() - 0.5, position.getY() - 0.5, position.getZ() - 0.5);
			blockEntityRenderDispatcher.get(blockEntity).render(blockEntity, 0.0F, matrixStack, vertexConsumerProvider, light, OverlayTexture.DEFAULT_UV);
			matrixStack.pop();
			return;
		}
		
		// Block model render.
		if(blockState.getRenderType() != BlockRenderType.MODEL)
			return;
		
		BlockRenderManager blockRenderManager = MinecraftClient.getInstance().getBlockRenderManager();
		BlockModelRenderer blockModelRenderer = blockRenderManager.getModelRenderer();
		BitSet bitSet = new BitSet(3);
		VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayers.getMovingBlockLayer(blockState));
        BakedModel model = blockRenderManager.getModel(blockState);
        matrixStack.push();
        matrixStack.translate(position.getX() - 0.5, position.getY() - 0.5, position.getZ() - 0.5);
        long seed = blockState.getRenderingSeed(position.add(centerBlockPosInitial));
        random.setSeed(seed);
        
        for(Direction direction : Direction.values())
        {
            List<BakedQuad> list = model.getQuads(blockState, direction, random);
            
            if(list.isEmpty() || !canRenderSide(block, direction))
            	continue;
            
            ((BlockModelRendererInvokerMixin) blockModelRenderer).callRenderQuadsFlat(world, blockState, position, lightLevel, OverlayTexture.DEFAULT_UV, false, matrixStack, vertexConsumer, list, bitSet);
        }
        
        List<BakedQuad> list = model.getQuads(blockState, null, random);
        
        if(!list.isEmpty())
        	((BlockModelRendererInvokerMixin) blockModelRenderer).callRenderQuadsFlat(world, blockState, position, lightLevel, OverlayTexture.DEFAULT_UV, false, matrixStack, vertexConsumer, list, bitSet);
        
        matrixStack.pop();
	}
	
	public static boolean canRenderSide(MovingCraftBlock block, Direction direction)
	{
		switch(direction)
		{
			case NORTH:
				return block.getSidesShowing()[0];
			case EAST:
				return block.getSidesShowing()[1];
			case SOUTH:
				return block.getSidesShowing()[2];
			case WEST:
				return block.getSidesShowing()[3];
			case UP:
				return block.getSidesShowing()[4];
			case DOWN:
				return block.getSidesShowing()[5];
			default:
				return false;
		}
	}
}