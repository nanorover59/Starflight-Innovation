package space.item;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import space.StarflightMod;
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
			structureList.add(new Identifier(StarflightMod.MOD_ID, path));
	}
	
	@Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context)
	{
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
		
		MinecraftServer minecraftServer = context.getWorld().getServer();
		StructureTemplateManager templateManager = serverWorld.getStructureTemplateManager();
		
		for(Identifier structure : structureList)
		{
			StructureTemplate template = templateManager.getTemplate(structure).get();
			StructurePlacementData placementdata = new StructurePlacementData();
			template.place(serverWorld, placementPosition, placementPosition, placementdata, serverWorld.getRandom(), Block.NOTIFY_LISTENERS);
			placementPosition = placementPosition.up(32);
		}
		
		if(!context.getPlayer().isCreative())
            stack.decrement(1);
		
		return ActionResult.SUCCESS;
	}
	
	private boolean checkVolume(ServerWorld serverWorld, BlockPos start, BlockPos end)
	{
		for(int i = start.getX(); i < end.getX(); i++)
		{
			for(int j = start.getY(); j < end.getY(); j++)
			{
				for(int k = start.getZ(); k < end.getZ(); k++)
				{
					if(!serverWorld.getBlockState(new BlockPos(i, j, k)).getMaterial().isReplaceable())
						return true;
				}
			}
		}
		
		return false;
	}
}