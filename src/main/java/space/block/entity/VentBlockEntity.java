package space.block.entity;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import space.block.FluidPipeBlock;
import space.block.StarflightBlocks;
import space.block.ValveBlock;
import space.block.VentBlock;
import space.util.FluidResourceType;
import space.util.StarflightEffects;

public class VentBlockEntity extends BlockEntity
{
	private boolean water;
	private BlockPos pump;
	private int particleTimer = 0;
	private int soundTimer = 0;
	
	public VentBlockEntity(BlockPos pos, BlockState state)
	{
		super(StarflightBlocks.VENT_BLOCK_ENTITY, pos, state);
		water = false;
		pump = pos;
	}
	
	public int getMode()
	{
		return world.getBlockState(getPos()).get(ValveBlock.MODE);
	}
	
	public PumpBlockEntity getPump()
	{
		BlockEntity blockEntity = getWorld().getBlockEntity(pump);
		
		if(blockEntity != null && blockEntity instanceof PumpBlockEntity)
			return (PumpBlockEntity) blockEntity;
		else
			return null;
	}
	
	public void updateWaterState(World world, BlockPos startPos)
	{
		Deque<BlockPos> stack = new ArrayDeque<BlockPos>();
		Set<BlockPos> set = new HashSet<BlockPos>();
		stack.push(startPos.offset(getCachedState().get(VentBlock.FACING)));
		
		while(stack.size() > 0)
		{
			if(set.size() >= 1024)
			{
				water = true;
				markDirty();
				return;
			}
			
			BlockPos blockPos = stack.pop();
			
			if(set.contains(blockPos))
				continue;
			
			if(world.getFluidState(blockPos).isOf(Fluids.WATER))
			{
				set.add(blockPos);
				
				for(Direction direction : Direction.values())
				{
					BlockPos offset = blockPos.offset(direction);
					stack.push(offset);
				}
			}
		}
		
		water = false;
		markDirty();
	}
	
	public void findPump(World world, BlockPos startPos)
	{
		Deque<BlockPos> stack = new ArrayDeque<BlockPos>();
		Set<BlockPos> set = new HashSet<BlockPos>();
		
		for(Direction direction : Direction.values())
		{
			if(direction != getCachedState().get(VentBlock.FACING))
			{
				BlockPos offset = startPos.offset(direction);
				stack.push(offset);
			}
		}
		
		while(stack.size() > 0 && set.size() < 2048)
		{
			BlockPos blockPos = stack.pop();
			
			if(set.contains(blockPos))
				continue;
			
			Block block = world.getBlockState(blockPos).getBlock();
			
			if(block instanceof FluidPipeBlock && ((FluidPipeBlock) block).getFluidType().getID() == FluidResourceType.WATER.getID())
			{
				set.add(blockPos);
				
				for(Direction direction : Direction.values())
				{
					BlockPos offset = blockPos.offset(direction);
					stack.push(offset);
				}
			}
			else if(block == StarflightBlocks.PUMP)
			{
				pump = blockPos;
				markDirty();
				return;
			}
		}
	}
	
	public void setPumpPosition(BlockPos blockPos)
	{
		pump = blockPos;
	}
	
	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup)
	{
		super.writeNbt(nbt, registryLookup);
		nbt.putBoolean("water", water);
		nbt.put("fluidTankController", NbtHelper.fromBlockPos(pump));
	}
	
	@Override
	public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup)
	{
		super.readNbt(nbt, registryLookup);
		this.water = nbt.getBoolean("water");
		this.pump = NbtHelper.toBlockPos(nbt, "pump").get();
	}
	
	public static void serverTick(World world, BlockPos pos, BlockState state, VentBlockEntity blockEntity)
	{
		if(world.isClient)
			return;
		
		if(blockEntity.water && blockEntity.getPump() != null)
		{
			PumpBlockEntity pumpBlockEntity = blockEntity.getPump();
			
			if(pumpBlockEntity.getEnergyStored() > 0 && pumpBlockEntity.getEnergyStored() > 0)
			{
				for(Direction direction : Direction.values())
				{
					BlockPos offsetPos = pos.offset(direction);
					BlockState adjacentState = world.getBlockState(offsetPos);
					
					if(adjacentState.getBlock() instanceof FluidPipeBlock)
					{
						FluidPipeBlockEntity adjacentBlockEntity = ((FluidPipeBlockEntity) world.getBlockEntity(offsetPos));
						
						if(adjacentBlockEntity.getFluidType().getID() == FluidResourceType.WATER.getID())
						{
							adjacentBlockEntity.changeStoredFluid(adjacentBlockEntity.getStorageCapacity());

							if(blockEntity.particleTimer == 0)
							{
								Direction ventDirection = state.get(VentBlock.FACING);
								BlockPos forwardPos = pos.offset(ventDirection);
								int count = 2 + world.random.nextInt(4);
								((ServerWorld) world).spawnParticles(ParticleTypes.BUBBLE, forwardPos.getX() + 0.5, forwardPos.getY() + 0.5, forwardPos.getZ() + 0.5, count, 0.5, 0.5, 0.5, 0.01);
								blockEntity.particleTimer = 10 + world.random.nextInt(5);
							}
							else
								blockEntity.particleTimer--;
						}
					}
				}
			}
		}
		else if(world.isReceivingRedstonePower(pos))
		{
			for(Direction direction : Direction.values())
			{
				BlockPos offsetPos = pos.offset(direction);
				BlockState adjacentState = world.getBlockState(offsetPos);
				
				if(adjacentState.getBlock() instanceof FluidPipeBlock)
				{
					FluidPipeBlockEntity adjacentBlockEntity = ((FluidPipeBlockEntity) world.getBlockEntity(offsetPos));
					
					if(adjacentBlockEntity.getFluidType().getID() == FluidResourceType.OXYGEN.getID() || adjacentBlockEntity.getFluidType().getID() == FluidResourceType.HYDROGEN.getID())
					{
						if(adjacentBlockEntity.getStoredFluid() > 0.1)
						{
							if(blockEntity.particleTimer == 0)
							{
								Direction ventDirection = state.get(VentBlock.FACING);
								BlockPos forwardPos = pos.offset(ventDirection);
								StarflightEffects.sendOutgas(world, forwardPos, forwardPos.offset(ventDirection), false);
								blockEntity.particleTimer = 5 + world.random.nextInt(5);
							}
							else
								blockEntity.particleTimer--;
							
							if(blockEntity.soundTimer == 0)
							{
								((ServerWorld) world).playSound(null, pos.getX(), pos.getY(), pos.getZ(), StarflightEffects.LEAK_SOUND_EVENT, SoundCategory.BLOCKS, 1.0f, 1.0f, world.random.nextLong());
								blockEntity.soundTimer = 70 + world.random.nextInt(10);
							}
							else
								blockEntity.soundTimer--;
						}
						
						adjacentBlockEntity.changeStoredFluid(-adjacentBlockEntity.getStoredFluid());
					}
				}
			}
		}
    }
}