package space.block.entity;

import java.util.List;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import space.block.StarflightBlocks;
import space.block.VolcanicVentBlock;
import space.network.s2c.VolcanicVentS2CPacket;

public class VolcanicVentBlockEntity extends BlockEntity
{
	private boolean active = false;
	private int timer;
	private int soundTimer = 0;

	public VolcanicVentBlockEntity(BlockPos blockPos, BlockState blockState)
	{
		super(StarflightBlocks.VOLCANIC_VENT_BLOCK_ENTITY, blockPos, blockState);
	}
	
	public static void serverTick(World world, BlockPos pos, BlockState state, VolcanicVentBlockEntity blockEntity)
	{
		Direction direction = blockEntity.getCachedState().get(VolcanicVentBlock.FACING);
		
		if(blockEntity.active && !world.getBlockState(pos.offset(direction)).blocksMovement())
		{
			Vec3d vector = new Vec3d(direction.getVector().getX(), direction.getVector().getY(), direction.getVector().getZ()).multiply(0.1);
			Box box = Box.enclosing(pos, pos.offset(direction, 8)).expand(1);
			List<Entity> entities = world.getEntitiesByClass(Entity.class, box, entity -> true);
			
			for(Entity entity : entities)
			{
				entity.addVelocity(vector);
				entity.velocityModified = true;
				entity.setOnFireFor(4);
			}
			
			VolcanicVentS2CPacket.sendVolcanicVent(world, pos.offset(direction), pos.offset(direction, 2));
			
			// Play the sound effect.
			if(blockEntity.soundTimer == 0)
			{
				((ServerWorld) world).playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.BLOCKS, 2.0f, 1.0f, world.random.nextLong());
				blockEntity.soundTimer = 15;
			}
			else
				blockEntity.soundTimer--;
		}
		
		// Update the timer.
		if(blockEntity.timer > 0)
			blockEntity.timer--;
		else
		{
			blockEntity.active = !blockEntity.active;
			blockEntity.timer = 60 + world.getRandom().nextInt(120);
		}
	}
	
	@Override
	public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup)
	{
		super.readNbt(nbt, registryLookup);
		this.active = nbt.getBoolean("active");
		this.timer = nbt.getInt("timer");
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup)
	{
		super.writeNbt(nbt, registryLookup);
		nbt.putBoolean("active", active);
		nbt.putInt("timer", timer);
	}
}