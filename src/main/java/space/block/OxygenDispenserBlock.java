package space.block;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import space.client.StarflightModClient;
import space.item.OxygenTankItem;
import space.item.SpaceSuitItem;
import space.item.StarflightItems;
import space.util.AirUtil;
import space.util.StarflightEffects;

public class OxygenDispenserBlock extends Block implements FluidUtilityBlock
{
	public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;

	protected OxygenDispenserBlock(Settings settings)
	{
		super(settings);
		this.setDefaultState((BlockState) ((BlockState) ((BlockState) this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)));
	}
	
	@Override
    public void appendTooltip(ItemStack stack, @Nullable BlockView world, List<Text> tooltip, TooltipContext context)
	{
		StarflightModClient.hiddenItemTooltip(tooltip, Text.translatable("block.space.oxygen_dispenser.description_1"), Text.translatable("block.space.oxygen_dispenser.description_2"));
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
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit)
	{
		if(world.isClient)
			return ActionResult.PASS;
		
		double requiredOxygen = 0; // Start by finding the amount of oxygen needed.
		ItemStack heldStack = player.getMainHandStack();

		if(heldStack.getItem() instanceof OxygenTankItem)
			requiredOxygen = ((OxygenTankItem) heldStack.getItem()).getMaxOxygen() - heldStack.getNbt().getDouble("oxygen");
		else
		{
			for(ItemStack armorStack : player.getArmorItems())
			{
				if(armorStack.getItem() == StarflightItems.SPACE_SUIT_CHESTPLATE && armorStack.getNbt() != null)
				{
					requiredOxygen = ((SpaceSuitItem) armorStack.getItem()).getMaxOxygen() - armorStack.getNbt().getDouble("oxygen");
					break;
				}
			}
		}
		
		// Return now if no oxygen is needed.
		if(requiredOxygen <= 0)
			return ActionResult.PASS;

		ArrayList<BlockPos> checkList = new ArrayList<BlockPos>();
		double availableOxygen = AirUtil.searchSupply(world, pos, checkList, AirUtil.MAX_VOLUME, StarflightBlocks.OXYGEN_DISPENSER);
		
		// Do effects and transfer oxygen.
		if(availableOxygen >= requiredOxygen)
		{
			StarflightEffects.sendFizz(world, pos);
			MutableText text = Text.translatable("block.space.oxygen_dispenser.message");
			player.sendMessage(text, true);

			if(heldStack.getItem() instanceof OxygenTankItem)
				heldStack.getNbt().putDouble("oxygen", ((OxygenTankItem) heldStack.getItem()).getMaxOxygen());
			else
			{
				for(ItemStack armorStack : player.getArmorItems())
				{
					if(armorStack.getItem() == StarflightItems.SPACE_SUIT_CHESTPLATE && armorStack.getNbt() != null)
					{
						armorStack.getNbt().putDouble("oxygen", ((SpaceSuitItem) armorStack.getItem()).getMaxOxygen());
						break;
					}
				}
			}

			AirUtil.useSupply(world, checkList, requiredOxygen);
			return ActionResult.SUCCESS;
		}
		
		return ActionResult.PASS;
	}
	
	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx)
	{
		return (BlockState) this.getDefaultState().with(FACING, ctx.getPlayerFacing().getOpposite());
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
	public String getFluidName()
	{
		return "oxygen";
	}

	@Override
	public boolean canPipeConnectToSide(WorldAccess world, BlockPos pos, BlockState state, Direction direction)
	{
		return direction != (Direction) state.get(FACING).getOpposite();
	}
}