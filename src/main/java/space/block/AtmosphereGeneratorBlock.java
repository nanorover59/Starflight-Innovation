package space.block;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item.TooltipContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import space.block.entity.AtmosphereGeneratorBlockEntity;
import space.item.StarflightItems;
import space.network.s2c.OutgasS2CPacket;
import space.util.AirUtil;
import space.util.BlockSearch;
import space.util.FluidResourceType;

public class AtmosphereGeneratorBlock extends BlockWithEntity implements FluidUtilityBlock, EnergyBlock
{
	public static final MapCodec<AtmosphereGeneratorBlock> CODEC = AtmosphereGeneratorBlock.createCodec(AtmosphereGeneratorBlock::new);
	public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
	public static final BooleanProperty LIT = Properties.LIT;
	
	public AtmosphereGeneratorBlock(Settings settings)
	{
		super(settings);
		this.setDefaultState(this.getStateManager().getDefaultState().with(FACING, Direction.NORTH).with(LIT, false));
	}
	
	@Override
	public MapCodec<? extends AtmosphereGeneratorBlock> getCodec()
	{
		return CODEC;
	}
	
	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager)
	{
		stateManager.add(FACING);
		stateManager.add(LIT);
	}
	
	@Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType options)
	{
		ArrayList<Text> textList = new ArrayList<Text>();
		DecimalFormat df = new DecimalFormat("#.##");
		textList.add(Text.translatable("block.space.energy_consumer").append(String.valueOf(df.format(getInput()))).append("kJ/s").formatted(Formatting.LIGHT_PURPLE));
		textList.add(Text.translatable("block.space.atmosphere_generator.description_1"));
		textList.add(Text.translatable("block.space.atmosphere_generator.description_2"));
		StarflightItems.hiddenItemTooltip(tooltip, textList);
	}
	
	@Override
	public double getInput()
	{
		return 4.0;
	}
	
	@Override
	public double getEnergyCapacity()
	{
		return 64.0;
	}
	
	@Override
	public BlockEntity createBlockEntity(BlockPos blockPos, BlockState blockState)
	{
		return new AtmosphereGeneratorBlockEntity(blockPos, blockState);
	}
	
	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx)
	{
		return (BlockState) this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
	}
	
	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack)
	{
		if(!world.isClient)
			BlockSearch.energyConnectionSearch(world, pos);
	}
	
	@Override
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved)
	{
		if(!world.isClient && !state.isOf(newState.getBlock()))
			BlockSearch.energyConnectionSearch(world, pos);
		
		super.onStateReplaced(state, world, pos, newState, moved);
	}
	
	@Override
	public BlockRenderType getRenderType(BlockState state)
	{
		return BlockRenderType.MODEL;
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
	public FluidResourceType getFluidType()
	{
		return FluidResourceType.OXYGEN;
	}

	@Override
	public boolean canPipeConnectToSide(WorldAccess world, BlockPos pos, BlockState state, Direction direction)
	{
		return direction != (Direction) state.get(FACING).getOpposite();
	}
	
	@Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify)
    {
		if(world.isClient)
			return;
		
		BlockPos frontPos = pos.offset(state.get(FACING));
		BlockState frontState = world.getBlockState(frontPos);
		
		if(world.isReceivingRedstonePower(pos) && !state.get(LIT) && !fromPos.equals(frontPos) && frontState.getBlock() == Blocks.AIR)
		{
			//long time = System.currentTimeMillis();
			
			ArrayList<BlockPos> checkList = new ArrayList<BlockPos>();
			ArrayList<BlockPos> foundList = new ArrayList<BlockPos>();
			ArrayList<BlockPos> updateList = new ArrayList<BlockPos>();
			double supply = AirUtil.searchSupply(world, pos, checkList, BlockSearch.MAX_VOLUME, StarflightBlocks.ATMOSPHERE_GENERATOR);
			
			//System.out.println("searchSupply: " + (System.currentTimeMillis() - time));
			//time = System.currentTimeMillis();
			
			boolean tooLarge = !AirUtil.findVolume(world, frontPos, foundList, updateList, BlockSearch.MAX_VOLUME);
			double required = foundList.size() * HabitableAirBlock.DENSITY;
			
			//System.out.println("findVolume: " + (System.currentTimeMillis() - time));
			//time = System.currentTimeMillis();
			
			if(tooLarge || required > supply)
			{
				MutableText text = Text.translatable("block.space.atmosphere_generator.error_" + (tooLarge ? "volume" : "supply"));
				
				for(PlayerEntity player : world.getPlayers())
				{
		            if(player.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ()) < 1024.0)
		            	player.sendMessage(text, true);
		        }
			}
			else
			{
				AirUtil.useSupply(world, checkList, required);
				
				//System.out.println("useSupply: " + (System.currentTimeMillis() - time));
				//time = System.currentTimeMillis();
				
				AirUtil.fillVolume(world, foundList, updateList);
				
				//System.out.println("fillVolume: " + foundList.size() + " " + (System.currentTimeMillis() - time));
				//time = System.currentTimeMillis();
				
				world.setBlockState(pos, (BlockState) state.with(AtmosphereGeneratorBlock.LIT, true), Block.NOTIFY_ALL);
				OutgasS2CPacket.sendOutgas(world, pos, frontPos, true);
			}
		}
		else if(frontState.getBlock() == StarflightBlocks.HABITABLE_AIR)
			world.setBlockState(pos, (BlockState) state.with(AtmosphereGeneratorBlock.LIT, true), Block.NOTIFY_ALL);
		else if(frontState.getBlock() == Blocks.AIR)
			world.setBlockState(pos, (BlockState) state.with(AtmosphereGeneratorBlock.LIT, false), Block.NOTIFY_ALL);
    }
	
	@Override
	public boolean isInput(World world, BlockPos pos, BlockState state, Direction direction)
	{
		return direction != (Direction) state.get(FACING);
	}
	
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type)
	{
		return world.isClient ? null : validateTicker(type, StarflightBlocks.ATMOSPHERE_GENERATOR_BLOCK_ENTITY, AtmosphereGeneratorBlockEntity::serverTick);
	}
}