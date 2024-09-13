package space.block.entity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import space.block.StarflightBlocks;
import space.util.AirUtil;
import space.util.BlockSearch;
import space.util.StarflightEffects;

public class LeakBlockEntity extends BlockEntity
{
	private int timer;
	private int soundTimer = 0;

	public LeakBlockEntity(BlockPos blockPos, BlockState blockState)
	{
		super(StarflightBlocks.LEAK_BLOCK_ENTITY, blockPos, blockState);
	}
	
	public static void serverTick(World world, BlockPos pos, BlockState state, LeakBlockEntity blockEntity)
	{
		// Play the sound effect.
		if(blockEntity.soundTimer == 0)
		{
			((ServerWorld) world).playSound(null, pos.getX(), pos.getY(), pos.getZ(), StarflightEffects.LEAK_SOUND_EVENT, SoundCategory.BLOCKS, 1.0f, 1.0f, world.random.nextLong());
			blockEntity.soundTimer = 70 + world.random.nextInt(20);
		}
		else
			blockEntity.soundTimer--;
		
		// Spread the damage.
		if(blockEntity.timer % 10 == 0 && world.random.nextInt(10) == 0)
		{
			BlockPos pos1 = pos.add(world.random.nextInt(2) - world.random.nextInt(2), world.random.nextInt(2) - world.random.nextInt(2), world.random.nextInt(2) - world.random.nextInt(2));
			boolean air = false;
			boolean habitableAir = false;

			for(Direction direction2 : Direction.values())
			{
				Block block = world.getBlockState(pos1.offset(direction2)).getBlock();

				if(block == Blocks.AIR)
					air = true;
				else if(block == StarflightBlocks.HABITABLE_AIR)
					habitableAir = true;
			}

			if(air && habitableAir)
			{
				blockEntity.timer /= 2;
				AirUtil.createLeak(world, pos1, blockEntity.timer);
			}
		}
		
		// Update the timer and remove when finished.
		if(blockEntity.timer > 0)
			blockEntity.timer--;
		else
		{
			AirUtil.remove(world, pos, BlockSearch.MAX_VOLUME);
			world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
		}
	}
	
	public void setLeakTime(int ticks)
	{
		timer = ticks;
	}
	
	@Override
	public void readNbt(NbtCompound nbt)
	{
		super.readNbt(nbt);
		this.timer = nbt.getInt("timer");
	}

	@Override
	protected void writeNbt(NbtCompound nbt)
	{
		super.writeNbt(nbt);
		nbt.putInt("timer", timer);
	}
}