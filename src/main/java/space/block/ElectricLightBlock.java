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
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import space.block.entity.ElectricLightBlockEntity;
import space.item.StarflightItems;
import space.util.BlockSearch;

public class ElectricLightBlock extends BlockWithEntity implements BlockEntityProvider, EnergyBlock
{
	public static final MapCodec<ElectricLightBlock> CODEC = ElectricLightBlock.createCodec(ElectricLightBlock::new);
	public static final BooleanProperty LIT = Properties.LIT;

	public ElectricLightBlock(Settings settings)
	{
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState().with(LIT, false));
	}

	@Override
	public MapCodec<? extends ElectricLightBlock> getCodec()
	{
		return CODEC;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
	{
		builder.add(LIT);
	}
	
	@Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType options)
	{
		ArrayList<Text> textList = new ArrayList<Text>();
		DecimalFormat df = new DecimalFormat("#.##");
		textList.add(Text.translatable("block.space.energy_consumer", df.format(getInput())).formatted(Formatting.LIGHT_PURPLE));
		textList.add(Text.translatable("block.space.electric_light.description"));
		StarflightItems.hiddenItemTooltip(tooltip, textList);
	}
	
	@Override
	public long getInput()
	{
		return 1;
	}
	
	@Override
	public long getEnergyCapacity()
	{
		return 4;
	}
	
	@Override
	public BlockRenderType getRenderType(BlockState state)
	{
		return BlockRenderType.MODEL;
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
		return true;
	}
	
	@Override
	public boolean isPassThrough(World world, BlockPos pos, BlockState state, Direction direction)
	{
		return true;
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state)
	{
		return new ElectricLightBlockEntity(pos, state);
	}

	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type)
	{
		return world.isClient ? null : validateTicker(type, StarflightBlocks.ELECTRIC_LIGHT_BLOCK_ENTITY, ElectricLightBlockEntity::serverTick);
	}
}