package space.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import space.block.AtmosphereGeneratorBlock;
import space.block.HabitableAirBlock;
import space.block.StarflightBlocks;
import space.energy.EnergyNet;
import space.energy.EnergyNode;

public class AtmosphereGeneratorBlockEntity extends BlockEntity implements PoweredBlockEntity
{
	private int powerState;
	
	public AtmosphereGeneratorBlockEntity(BlockPos blockPos, BlockState blockState)
	{
		super(StarflightBlocks.ATMOSPHERE_GENERATOR_BLOCK_ENTITY, blockPos, blockState);
	}
	
	@Override
	public void setPowerState(int i)
	{
		powerState = i;
		
		if(i == 0)
		{
			BlockState state = world.getBlockState(pos);
			Direction direction = state.get(AtmosphereGeneratorBlock.FACING);
			BlockPos frontPos = pos.offset(direction);
			BlockState frontState = world.getBlockState(frontPos);
			
			if(frontState.getBlock() == StarflightBlocks.HABITABLE_AIR && !frontState.get(HabitableAirBlock.UNSTABLE))
			{
				EnergyNode consumer = EnergyNet.getConsumer(pos, world.getRegistryKey());
				boolean producersLoaded = true; // Avoid powering off the atmosphere generator because sources are unloaded.
				
				for(EnergyNode producer : consumer.getInputs())
				{
					ChunkPos chunkPos = new ChunkPos(producer.getPosition());
					
					if(!world.isChunkLoaded(chunkPos.x, chunkPos.z))
					{
						producersLoaded = false;
						break;
					}
				}
				
				if(producersLoaded)
				{
					HabitableAirBlock.setUnstable(world, frontPos, frontState);
					MutableText text = Text.translatable("block.space.atmosphere_generator.error_power");
					
					for(PlayerEntity player : world.getPlayers())
					{
			            if(player.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ()) < 1024.0)
			            	player.sendMessage(text, true);
			        }
				}
			}
		}
	}
	
	@Override
	public int getPowerState()
	{
		return powerState;
	}
	
	@Override
	public void readNbt(NbtCompound nbt)
	{
		super.readNbt(nbt);
	}

	@Override
	protected void writeNbt(NbtCompound nbt)
	{
		super.writeNbt(nbt);
	}
}