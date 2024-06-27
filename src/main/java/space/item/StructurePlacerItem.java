package space.item;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import space.StarflightMod;
import space.block.FluidTankControllerBlock;
import space.block.entity.FluidTankControllerBlockEntity;
import space.client.StarflightModClient;

public class StructurePlacerItem extends Item
{
	ArrayList<Identifier> structureList = new ArrayList<Identifier>();
	BlockPos size;
	
	public StructurePlacerItem(Settings settings, int xSize, int ySize, int zSize, String ... structurePaths) 
	{
		super(settings);
		this.size = new BlockPos(xSize, ySize, zSize);
		
		for(String path : structurePaths)
			structureList.add(Identifier.of(StarflightMod.MOD_ID, path));
	}
	
	@Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType options)
	{
		tooltip.add(Text.translatable("item.space.creative").formatted(Formatting.ITALIC, Formatting.RED));
		StarflightModClient.hiddenItemTooltip(tooltip, Text.translatable("item.space.structure_placer.description"), Text.translatable("item.space.structure_placer.size").append("X: " + size.getX() + "  Y: " + size.getY() + "  Z: " + size.getZ()));
	}
	
	@Override
	public ActionResult useOnBlock(ItemUsageContext context)
	{
		if(context.getWorld().isClient())
			return ActionResult.PASS;
		
		BlockPos placementPosition = context.getBlockPos().up();
		ItemStack stack = context.getStack();
		ServerWorld serverWorld = (ServerWorld) context.getWorld();
		
		if(checkVolume(serverWorld, placementPosition, placementPosition.add(size)))
		{
			MutableText text = Text.translatable("item.space.structure_placer.obstructed");
			context.getPlayer().sendMessage(text, true);
			return ActionResult.PASS;
		}
		
		StructureTemplateManager templateManager = serverWorld.getStructureTemplateManager();
		
		for(Identifier structure : structureList)
		{
			StructureTemplate template = templateManager.getTemplate(structure).get();
			StructurePlacementData placementdata = new StructurePlacementData();
			template.place(serverWorld, placementPosition, placementPosition, placementdata, serverWorld.getRandom(), Block.NOTIFY_LISTENERS);
			postPlacement(serverWorld, placementPosition, placementPosition.add(size));
			placementPosition = placementPosition.up(32);
		}
		
		if(!context.getPlayer().isCreative())
            stack.decrement(1);
		
		return ActionResult.SUCCESS;
	}
	
	private boolean checkVolume(ServerWorld world, BlockPos start, BlockPos end)
	{
		for(int i = start.getX(); i < end.getX(); i++)
		{
			for(int j = start.getY(); j < end.getY(); j++)
			{
				for(int k = start.getZ(); k < end.getZ(); k++)
				{
					if(!world.getBlockState(new BlockPos(i, j, k)).isReplaceable())
						return true;
				}
			}
		}
		
		return false;
	}
	
	private void postPlacement(ServerWorld world, BlockPos start, BlockPos end)
	{
		ArrayList<BlockPos> checkList = new ArrayList<BlockPos>();
		
		for(int i = start.getX(); i < end.getX(); i++)
		{
			for(int j = start.getY(); j < end.getY(); j++)
			{
				for(int k = start.getZ(); k < end.getZ(); k++)
				{
					BlockPos blockPos = new BlockPos(i, j, k);
					Block block = world.getBlockState(blockPos).getBlock();
					
					if(block instanceof FluidTankControllerBlock)
						checkList.add(blockPos);
				}
			}
		}
		
		for(BlockPos blockPos : checkList)
		{
			BlockEntity blockEntity = world.getBlockEntity(blockPos);
			
			if(blockEntity != null && blockEntity instanceof FluidTankControllerBlockEntity)
			{
				FluidTankControllerBlockEntity fluidTank = (FluidTankControllerBlockEntity) blockEntity;
				
				if(world.getBlockState(blockPos).getBlock() instanceof FluidTankControllerBlock)
					((FluidTankControllerBlock) world.getBlockState(blockPos).getBlock()).initializeFluidTank(world, blockPos, fluidTank);
				
				fluidTank.setStoredFluid(fluidTank.getStorageCapacity());
			}
		}
	}
}