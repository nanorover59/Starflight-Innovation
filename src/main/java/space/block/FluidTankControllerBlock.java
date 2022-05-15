package space.block;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.BaseText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.explosion.Explosion.DestructionType;
import space.block.entity.FluidTankControllerBlockEntity;
import space.block.entity.FluidTankInterfaceBlockEntity;
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
			world.createExplosion(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 2.0F, DestructionType.DESTROY);
		
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
					TranslatableText text = new TranslatableText("");
					
					if(result < 4)
						text.append(new TranslatableText("block.space.fluid_tank_" + result));
					
					if(text != BaseText.EMPTY)
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
		int limit = 2048;
		boolean valid = false;
		fluidTankController.setStorageCapacity(0);
		fluidTankController.setStoredFluid(0);

		for(Direction direction : Direction.values())
		{
			ArrayList<BlockPos> checkList = new ArrayList<BlockPos>(); // Check list to avoid ensure each block is only checked once.
			ArrayList<BlockPos> actionList = new ArrayList<BlockPos>(); // List of all fluid tank controller and interface blocks found.
			checkInterior(world, position.offset(direction), checkList, actionList, limit);

			if(checkList.size() > 0 && checkList.size() < limit)
			{
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
				
				// Finish defining the fluid tank arrangement.
				double cx = 0;
				double cy = 0;
				double cz = 0;
				int count = 0;

				for(BlockPos p : checkList)
				{
					if(world.getBlockState(p).getBlock() == Blocks.AIR)
					{
						world.setBlockState(p, StarflightBlocks.FLUID_TANK_INSIDE.getDefaultState(), Block.FORCE_STATE);
						fluidTankController.setStorageCapacity(fluidTankController.getStorageCapacity() + capacity);
						cx += p.getX();
						cy += p.getY();
						cz += p.getZ();
						count++;
					}
				}
				
				cx /= count;
				cy /= count;
				cz /= count;
				valid = true;
				fluidTankController.setCenterOfMass(new BlockPos(cx, cy, cz));
				fluidTankController.setActive(true);
			}
		}

		return valid ? 0 : 1;
	}

	private static void checkInterior(WorldAccess world, BlockPos position, ArrayList<BlockPos> checkList, ArrayList<BlockPos> actionList, int limit)
	{
		BlockEntity blockEntity = world.getBlockEntity(position);

		if(blockEntity != null && !actionList.contains(position) && (blockEntity instanceof FluidTankControllerBlockEntity || blockEntity instanceof FluidTankInterfaceBlockEntity))
		{
			actionList.add(position);
			return;
		} else if(world.getBlockState(position).isIn(StarflightBlocks.FLUID_TANK_BLOCK_TAG) || checkList.contains(position))
			return;

		checkList.add(position);

		if(checkList.size() >= limit)
			return;

		for(Direction direction : Direction.values())
			checkInterior(world, position.offset(direction), checkList, actionList, limit);
	}
}
