package space.block;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import space.block.entity.FluidTankControllerBlockEntity;
import space.block.entity.FluidTankInterfaceBlockEntity;
import space.client.StarflightModClient;
import space.util.BlockSearch;
import space.util.StarflightEffects;

public class FluidTankControllerBlock extends BlockWithEntity
{
	double capacity;
	
	public FluidTankControllerBlock(Settings settings, double fluidTankCapacity)
	{
		super(settings);
		this.capacity = fluidTankCapacity;
	}
	
	@Override
	public void appendTooltip(ItemStack stack, @Nullable BlockView world, List<Text> tooltip, TooltipContext context)
	{
		StarflightModClient.hiddenItemTooltip(tooltip, Text.translatable("block.space.fluid_tank.description_1"), Text.translatable("block.space.fluid_tank.description_2"));
	}

	@Override
	public BlockRenderType getRenderType(BlockState state)
	{
		return BlockRenderType.MODEL;
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state)
	{
		return null;
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
					StarflightEffects.sendOutgas(world, pos, pos.offset(direction), true);
			}
		}
		
		if(fluidTankController.getStoredFluid() > capacity)
			world.createExplosion(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 2.0F, World.ExplosionSourceType.BLOCK);
		
		if(state.hasBlockEntity() && !state.isOf(newState.getBlock()))
			world.removeBlockEntity(pos);
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit)
	{
		if(!world.isClient)
		{
			BlockEntity blockEntity = world.getBlockEntity(pos);

			if(blockEntity instanceof FluidTankControllerBlockEntity)
			{
				FluidTankControllerBlockEntity fluidTankController = (FluidTankControllerBlockEntity) blockEntity;

				if(!fluidTankController.isActive())
				{
					int result = initializeFluidTank(world, pos, fluidTankController.getFluidName(), capacity, fluidTankController);
					MutableText text = Text.translatable("");
					
					if(result < 4)
						text.append(Text.translatable("block.space.fluid_tank_" + result));
					
					if(text != Text.EMPTY)
						player.sendMessage(text, true);
					
					return ActionResult.SUCCESS;
				}
			}
		}

		return ActionResult.PASS;
	}
	
	public int initializeFluidTank(World world, BlockPos position, FluidTankControllerBlockEntity fluidTankController)
	{
		return 0;
	}

	protected static int initializeFluidTank(World world, BlockPos position, String fluidName, double capacity, FluidTankControllerBlockEntity fluidTankController)
	{
		boolean valid = false;
		fluidTankController.setStorageCapacity(0);
		fluidTankController.setStoredFluid(0);

		for(Direction direction : Direction.values())
		{
			ArrayList<BlockPos> checkList = new ArrayList<BlockPos>(); // Check list to avoid ensure each block is only checked once.
			ArrayList<BlockPos> actionList = new ArrayList<BlockPos>(); // List of all fluid tank controller and interface blocks found.
			
			BiPredicate<WorldAccess, BlockPos> include = (w, p) -> {
				return !world.getBlockState(p).isIn(StarflightBlocks.FLUID_TANK_BLOCK_TAG);
			};
			
			BlockSearch.search(world, position.offset(direction), checkList, include, BlockSearch.MAX_VOLUME, true);
			
			if(checkList.size() > 0 && checkList.size() < BlockSearch.MAX_VOLUME)
			{
				double cx = 0;
				double cy = 0;
				double cz = 0;
				int count = 0;

				for(BlockPos p : checkList)
				{
					if(world.getBlockState(p).isAir())
					{
						world.setBlockState(p, StarflightBlocks.FLUID_TANK_INSIDE.getDefaultState(), Block.FORCE_STATE);
						fluidTankController.setStorageCapacity(fluidTankController.getStorageCapacity() + capacity);
						cx += p.getX();
						cy += p.getY();
						cz += p.getZ();
						count++;
						
						for(Direction direction1 : Direction.values())
						{
							BlockPos offset = p.offset(direction1);
							BlockEntity blockEntity = world.getBlockEntity(offset);

							if(blockEntity != null && (blockEntity instanceof FluidTankControllerBlockEntity || blockEntity instanceof FluidTankInterfaceBlockEntity))
								actionList.add(offset);
						}
					}
				}
				
				cx /= count;
				cy /= count;
				cz /= count;
				
				// Check for excess fluid tank controllers and a minimum of one inlet and one outlet valve.
				boolean inlet = false;
				boolean outlet = false;
				
				for(BlockPos p : actionList)
				{
					if(p.equals(position))
						continue;
					
					BlockState blockState = world.getBlockState(p);
					
					if(blockState.getBlock() instanceof FluidTankControllerBlock)
						return 2;
					if(blockState.getBlock().getName().getString().toLowerCase().contains("inlet"))
						inlet = true;
					else if(blockState.getBlock().getName().getString().toLowerCase().contains("outlet"))
						outlet = true;
				}
				
				if(!inlet || !outlet)
					return 3;
				
				// Activate inlet and outlet valves.
				for(BlockPos p : actionList)
				{
					if(p.equals(position))
						continue;

					BlockEntity blockEntityAction = world.getBlockEntity(p);
					
					if(blockEntityAction instanceof FluidTankInterfaceBlockEntity && ((FluidTankInterfaceBlockEntity) blockEntityAction).getFluidName() == fluidName)
					{
						FluidTankInterfaceBlockEntity fluidTankInterface = (FluidTankInterfaceBlockEntity) blockEntityAction;
						fluidTankInterface.setActive(true);
						fluidTankInterface.setControllerPosition(position);
					}
				}
				
				fluidTankController.setCenterOfMass(new BlockPos((int) cx, (int) cy, (int) cz));
				fluidTankController.setActive(true);
				valid = true;
			}
		}

		return valid ? 0 : 1;
	}
}