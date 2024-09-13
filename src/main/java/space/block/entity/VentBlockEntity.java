package space.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import space.block.FluidPipeBlock;
import space.block.StarflightBlocks;
import space.block.VentBlock;
import space.network.s2c.OutgasS2CPacket;
import space.util.FluidResourceType;
import space.util.StarflightSoundEvents;

public class VentBlockEntity extends BlockEntity
{
	private int particleTimer = 0;
	private int soundTimer = 0;
	
	public VentBlockEntity(BlockPos pos, BlockState state)
	{
		super(StarflightBlocks.VENT_BLOCK_ENTITY, pos, state);
	}
	
	public static void serverTick(World world, BlockPos pos, BlockState state, VentBlockEntity blockEntity)
	{
		if(!world.isClient && world.isReceivingRedstonePower(pos))
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
								OutgasS2CPacket.sendOutgas(world, forwardPos, forwardPos.offset(ventDirection), false);
								blockEntity.particleTimer = 5 + world.random.nextInt(5);
							}
							else
								blockEntity.particleTimer--;
							
							if(blockEntity.soundTimer == 0)
							{
								((ServerWorld) world).playSound(null, pos.getX(), pos.getY(), pos.getZ(), StarflightSoundEvents.LEAK_SOUND_EVENT, SoundCategory.BLOCKS, 1.0f, 1.0f, world.random.nextLong());
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