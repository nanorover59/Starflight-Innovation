package space.block;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import space.block.entity.BalloonControllerBlockEntity;
import space.block.entity.FluidTankControllerBlockEntity;
import space.block.entity.ValveBlockEntity;
import space.item.StarflightItems;
import space.network.s2c.OutgasS2CPacket;
import space.util.BlockSearch;
import space.util.FluidResourceType;

public class FluidTankControllerBlock extends BlockWithEntity
{
	public static final MapCodec<FluidTankControllerBlock> CODEC = FluidTankControllerBlock.createCodec(FluidTankControllerBlock::new);
	private final FluidResourceType fluid;
	private final double capacity;
	
	public FluidTankControllerBlock(Settings settings, FluidResourceType fluid, double capacity)
	{
		super(settings);
		this.fluid = fluid;
		this.capacity = capacity;
	}
	
	public FluidTankControllerBlock(Settings settings)
	{
		this(settings, FluidResourceType.WATER, FluidResourceType.WATER.getStorageDensity());
	}
	
	@Override
	public MapCodec<? extends FluidTankControllerBlock> getCodec()
	{
		return CODEC;
	}
	
	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType options)
	{
		StarflightItems.hiddenItemTooltip(tooltip, Text.translatable("block.space.fluid_tank.description_1"), Text.translatable("block.space.fluid_tank.description_2"));
	}

	@Override
	public BlockRenderType getRenderType(BlockState state)
	{
		return BlockRenderType.MODEL;
	}

	@Override
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved)
	{
		FluidTankControllerBlockEntity fluidTankController = (FluidTankControllerBlockEntity) world.getBlockEntity(pos);
		
		if(fluidTankController.getStoredFluid() > capacity)
		{
			for(Direction direction : Direction.values())
			{
				if(world.getBlockState(pos.offset(direction.getOpposite())).getBlock() == StarflightBlocks.FLUID_TANK_INSIDE)
					OutgasS2CPacket.sendOutgas(world, pos, pos.offset(direction), true);
			}
		}
		
		if(fluidTankController.getStoredFluid() > capacity)
			world.createExplosion(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 2.0F, World.ExplosionSourceType.BLOCK);
		
		if(state.hasBlockEntity() && !state.isOf(newState.getBlock()))
			world.removeBlockEntity(pos);
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit)
	{
		if(world.isClient)
			return ActionResult.PASS;
		
		BlockEntity blockEntity = world.getBlockEntity(pos);

		if(blockEntity instanceof FluidTankControllerBlockEntity)
		{
			FluidTankControllerBlockEntity fluidTankController = (FluidTankControllerBlockEntity) blockEntity;

			if(fluidTankController.getStorageCapacity() == 0)
			{
				int result = initializeFluidTank(world, pos, fluidTankController.getFluidType(), capacity, fluidTankController);
				MutableText text = Text.translatable("");

				if(result < 4)
					text.append(Text.translatable("block.space.fluid_tank_" + result));

				if(text != Text.EMPTY)
					player.sendMessage(text, true);

				return ActionResult.SUCCESS;
			}
		}

		return ActionResult.PASS;
	}
	
	public FluidResourceType getFluidType()
	{
		return fluid;
	}
	
	public int initializeFluidTank(World world, BlockPos position, FluidTankControllerBlockEntity fluidTankController)
	{
		return initializeFluidTank(world, position, fluid, capacity, fluidTankController);
	}
	
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state)
	{
		return new FluidTankControllerBlockEntity(pos, state);
	}

	protected static int initializeFluidTank(World world, BlockPos position, FluidResourceType fluid, double capacity, FluidTankControllerBlockEntity fluidTankController)
	{
		BiPredicate<World, BlockPos> include;
		BiPredicate<World, BlockPos> edgeCase;
		boolean valid = false;
		fluidTankController.setStorageCapacity(0);
		fluidTankController.setStoredFluid(0);
		
		if(fluidTankController instanceof BalloonControllerBlockEntity)
		{
			include = (w, p) -> {
				return !w.getBlockState(p).isIn(StarflightBlocks.BALLOON_BLOCK_TAG);
			};
			edgeCase = (w, p) -> false;
		}
		else
		{
			include = (w, p) -> {
				return !w.getBlockState(p).isIn(StarflightBlocks.FLUID_TANK_BLOCK_TAG);
			};
			edgeCase = (w, p) -> {
				return w.getBlockState(p).isIn(StarflightBlocks.FLUID_TANK_BLOCK_TAG);
			};
		}

		for(Direction direction : Direction.values())
		{
			if(!world.getBlockState(position.offset(direction)).isAir())
				continue;
			
			ArrayList<BlockPos> checkList = new ArrayList<BlockPos>(); // Check list to avoid ensure each block is only checked once.
			ArrayList<BlockPos> actionList = new ArrayList<BlockPos>(); // List of all fluid tank controller and interface blocks found.
			
			BlockSearch.search(world, position.offset(direction), checkList, include, edgeCase, BlockSearch.MAX_VOLUME, true);
			
			if(checkList.size() > 0 && checkList.size() < BlockSearch.MAX_VOLUME)
			{
				double cx = 0;
				double cy = 0;
				double cz = 0;
				double volume = 0;
				int count = 0;

				for(BlockPos p : checkList)
				{
					BlockState blockState = world.getBlockState(p);
					
					if(blockState.isAir())
					{
						volume += 1.0;
						world.setBlockState(p, StarflightBlocks.FLUID_TANK_INSIDE.getDefaultState(), Block.FORCE_STATE);
					}
					else if(edgeCase.test(world, p))
					{
						//volume += 0.75;

						if(blockState.getBlock() instanceof FluidTankControllerBlock || blockState.getBlock() instanceof ValveBlock)
							actionList.add(p);
					}
					
					cx += p.getX();
					cy += p.getY();
					cz += p.getZ();
					count++;
				}
				
				cx /= count;
				cy /= count;
				cz /= count;
				
				// Check for excess fluid tank controllers and a minimum of one interface valve.
				boolean valve = false;
				
				for(BlockPos p : actionList)
				{
					if(p.equals(position))
						continue;
					
					BlockState blockState = world.getBlockState(p);
					
					if(blockState.getBlock() instanceof FluidTankControllerBlock)
						return 2;
					if(blockState.getBlock() instanceof ValveBlock)
						valve = true;
				}
				
				if(!valve && !(world.getBlockState(position).getBlock() instanceof FluidUtilityBlock))
					return 3;

				for(BlockPos p : actionList)
				{
					if(p.equals(position))
						continue;

					BlockEntity blockEntityAction = world.getBlockEntity(p);

					if(blockEntityAction instanceof ValveBlockEntity)
					{
						ValveBlockEntity fluidTankInterface = (ValveBlockEntity) blockEntityAction;
						fluidTankInterface.setControllerPosition(position);
					}
				}
				
				fluidTankController.setStorageCapacity(capacity * volume);
				fluidTankController.setCenterOfMass(new BlockPos((int) cx, (int) cy, (int) cz));
				valid = true;
			}
		}

		return valid ? 0 : 1;
	}
}