package space.block;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item.TooltipContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import space.item.OxygenTankItem;
import space.item.StarflightItems;
import space.util.FluidResourceType;

public class OxygenDispenserBlock extends Block implements FluidUtilityBlock
{
	public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;

	protected OxygenDispenserBlock(Settings settings)
	{
		super(settings);
		this.setDefaultState((BlockState) ((BlockState) ((BlockState) this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)));
	}
	
	@Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType options)
	{
		StarflightItems.hiddenItemTooltip(tooltip, Text.translatable("block.space.oxygen_dispenser.description_1"), Text.translatable("block.space.oxygen_dispenser.description_2"));
	}
	
	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
	{
		builder.add(FACING);
	}
	
	@Override
	public BlockRenderType getRenderType(BlockState state)
	{
		return BlockRenderType.MODEL;
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit)
	{
		if(world.isClient)
			return ActionResult.PASS;
		
		ItemStack stackToFill = null;
		
		if(player.getMainHandStack().getItem() instanceof OxygenTankItem)
			stackToFill = player.getMainHandStack();
		else
		{
			for(ItemStack armorStack : player.getArmorItems())
			{
				if(armorStack.getItem() == StarflightItems.SPACE_SUIT_CHESTPLATE)
				{
					stackToFill = armorStack;
					break;
				}
			}
		}
		
		/*if(stackToFill != null && stackToFill.contains(StarflightItems.OXYGEN) && stackToFill.contains(StarflightItems.MAX_OXYGEN))
		{
			float oxygen = stackToFill.get(StarflightItems.OXYGEN);
			float maxOxygen = stackToFill.get(StarflightItems.MAX_OXYGEN);
			float requiredOxygen = maxOxygen - oxygen;
			
			// Return if no oxygen is needed.
			if(requiredOxygen <= 0.0f)
				return ActionResult.PASS;

			ArrayList<BlockPos> checkList = new ArrayList<BlockPos>();
			double availableOxygen = AirUtil.searchSupply(world, pos, checkList, BlockSearch.MAX_VOLUME, StarflightBlocks.OXYGEN_DISPENSER);
			
			// Do effects and transfer oxygen.
			if(availableOxygen >= requiredOxygen)
			{
				FizzS2CPacket.sendFizz(world, pos);
				MutableText text = Text.translatable("block.space.oxygen_dispenser.message");
				player.sendMessage(text, true);
				stackToFill.set(StarflightItems.OXYGEN, maxOxygen);
				AirUtil.useSupply(world, checkList, requiredOxygen);
				return ActionResult.SUCCESS;
			}
		}*/
		
		return ActionResult.PASS;
	}
	
	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx)
	{
		return (BlockState) this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
	}
	
	@Override
	public BlockState rotate(BlockState state, BlockRotation rotation)
	{
		return (BlockState) state.with(FACING, rotation.rotate((Direction) state.get(FACING)));
	}
	
	@Override
	public BlockState mirror(BlockState state, BlockMirror mirror)
	{
		return state.rotate(mirror.getRotation((Direction) state.get(FACING)));
	}

	@Override
	public boolean canPipeConnectToSide(WorldAccess world, BlockPos pos, BlockState state, Direction direction, FluidResourceType fluidType)
	{
		return fluidType == FluidResourceType.OXYGEN && direction != (Direction) state.get(FACING).getOpposite();
	}
}