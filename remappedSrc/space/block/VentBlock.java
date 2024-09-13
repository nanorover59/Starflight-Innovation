package space.block;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import space.client.StarflightModClient;
import space.util.StarflightEffects;

public class VentBlock extends Block implements FluidUtilityBlock
{
	public static final BooleanProperty VENTING = BooleanProperty.of("venting");

	public VentBlock(Settings settings)
	{
		super(settings);
		this.setDefaultState(this.getStateManager().getDefaultState().with(VENTING, false));
	}

	@Override
	public void appendTooltip(ItemStack stack, @Nullable BlockView world, List<Text> tooltip, TooltipContext context)
	{
		StarflightModClient.hiddenItemTooltip(tooltip, Text.translatable("block.space.vent.description_1"), Text.translatable("block.space.vent.description_2"));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager)
	{
		stateManager.add(VENTING);
	}

	@Override
	public String getFluidName()
	{
		return "hydrogen/oxygen";
	}

	@Override
	public boolean canPipeConnectToSide(WorldAccess world, BlockPos pos, BlockState state, Direction direction)
	{
		return true;
	}
	
	public static void particleEffect(World world, BlockPos pos)
	{
		for(Direction d : Direction.values())
		{
			if(world.getBlockState(pos.offset(d)).getBlock() == Blocks.AIR)
			{
				StarflightEffects.sendOutgas(world, pos.offset(d), pos.offset(d, 2), false);
				break;
			}
		}
	}
}
