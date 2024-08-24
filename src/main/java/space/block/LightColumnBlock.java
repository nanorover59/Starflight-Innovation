package space.block;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.PillarBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item.TooltipContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.World;
import space.block.entity.LightColumnBlockEntity;
import space.client.StarflightModClient;
import space.util.BlockSearch;

public class LightColumnBlock extends BlockWithEntity implements BlockEntityProvider, EnergyBlock
{
	public static final MapCodec<LightColumnBlock> CODEC = LightColumnBlock.createCodec(LightColumnBlock::new);
	public static final EnumProperty<Direction.Axis> AXIS = Properties.AXIS;
	public static final BooleanProperty LIT = Properties.LIT;

	public LightColumnBlock(Settings settings)
	{
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState().with(AXIS, Axis.Y).with(LIT, false));
	}

	@Override
	public MapCodec<? extends LightColumnBlock> getCodec()
	{
		return CODEC;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
	{
		builder.add(AXIS);
		builder.add(LIT);
	}
	
	@Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType options)
	{
		ArrayList<Text> textList = new ArrayList<Text>();
		DecimalFormat df = new DecimalFormat("#.##");
		textList.add(Text.translatable("block.space.energy_consumer").append(String.valueOf(df.format(getInput()))).append("kJ/s").formatted(Formatting.LIGHT_PURPLE));
		textList.add(Text.translatable("block.space.light_column.description"));
		StarflightModClient.hiddenItemTooltip(tooltip, textList);
	}
	
	@Override
	public double getInput()
	{
		return 0.25;
	}
	
	@Override
	public double getEnergyCapacity()
	{
		return 0.5;
	}
	
	@Override
	public BlockRenderType getRenderType(BlockState state)
	{
		return BlockRenderType.MODEL;
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext context)
	{
		return this.getDefaultState().with(AXIS, context.getSide().getAxis());
	}
	
	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack)
	{
		if(world.isClient)
			return;
	
		BlockSearch.energyConnectionSearch(world, pos);
	}
	
	@Override
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved)
	{
		if(world.isClient() || state.isOf(newState.getBlock()))
			return;
		
		world.removeBlockEntity(pos);
		BlockSearch.energyConnectionSearch(world, pos);
	}

	@Override
	public BlockState rotate(BlockState state, BlockRotation rotation)
	{
		return PillarBlock.changeRotation(state, rotation);
	}
	
	@Override
	public boolean isOutput(World world, BlockPos pos, BlockState state, Direction direction)
	{
		return false;
	}

	@Override
	public boolean isInput(World world, BlockPos pos, BlockState state, Direction direction)
	{
		return direction.getAxis() == state.get(AXIS);
	}
	
	@Override
	public boolean isPassThrough(World world, BlockPos pos, BlockState state, Direction direction)
	{
		return direction.getAxis() == state.get(AXIS);
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state)
	{
		return new LightColumnBlockEntity(pos, state);
	}

	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type)
	{
		return world.isClient ? null : validateTicker(type, StarflightBlocks.LIGHT_COLUMN_BLOCK_ENTITY, LightColumnBlockEntity::serverTick);
	}
}