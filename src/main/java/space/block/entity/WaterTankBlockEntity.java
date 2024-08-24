package space.block.entity;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import space.block.FluidPipeBlock;
import space.block.StarflightBlocks;
import space.util.FluidResourceType;

public class WaterTankBlockEntity extends BlockEntity
{
	private final FluidResourceType fluid = FluidResourceType.WATER;
	private double storedFluid;
	private boolean renderTop;
	private boolean renderBottom;
	
	public WaterTankBlockEntity(BlockPos pos, BlockState state)
	{
		super(StarflightBlocks.WATER_TANK_BLOCK_ENTITY, pos, state);
		storedFluid = 0.0;
		renderTop = false;
		renderBottom = false;
	}
	
	public double getStorageCapacity()
	{
		return fluid.getStorageDensity();
	}
	
	public double getStoredFluid()
	{
		return storedFluid;
	}
	
	public void changeStoredFluid(double d)
	{
		storedFluid += d;
		
		if(storedFluid < 0.0)
			storedFluid = 0.0;
		else if(storedFluid > getStorageCapacity())
			storedFluid = getStorageCapacity();
	}
	
	public boolean canRenderTop()
	{
		return renderTop;
	}
	
	public boolean canRenderBottom()
	{
		return renderBottom;
	}
	
	@Override
	public void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup)
	{
		super.writeNbt(nbt, registryLookup);
		nbt.putDouble("storedFluid", storedFluid);
		nbt.putBoolean("top", renderTop);
		nbt.putBoolean("bottom", renderBottom);
	}
	
	@Override
	public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup)
	{
		super.readNbt(nbt, registryLookup);
		this.storedFluid = nbt.getDouble("storedFluid");
		this.renderTop = nbt.getBoolean("top");
		this.renderBottom = nbt.getBoolean("bottom");
	}
	
	@Nullable
	@Override
	public Packet<ClientPlayPacketListener> toUpdatePacket()
	{
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup)
	{
		return createNbt(registryLookup);
	}
	
	public static void serverTick(World world, BlockPos pos, BlockState state, WaterTankBlockEntity blockEntity)
	{
		double storedFluid = blockEntity.getStoredFluid();
		
		for(Direction direction : Direction.values())
		{
			if(direction == Direction.UP || (direction != Direction.DOWN && blockEntity.getStoredFluid() < blockEntity.getStorageCapacity() / 2.0))
				continue;
			
			Block offsetBlock = world.getBlockState(pos.offset(direction)).getBlock();
			
			if(offsetBlock instanceof FluidPipeBlock)
			{
				FluidPipeBlockEntity adjacentBlockEntity = ((FluidPipeBlockEntity) world.getBlockEntity(pos.offset(direction)));
				double deltaFluid = Math.min(blockEntity.getStoredFluid(), adjacentBlockEntity.getStorageCapacity() - adjacentBlockEntity.getStoredFluid());
				blockEntity.changeStoredFluid(-deltaFluid);
				adjacentBlockEntity.changeStoredFluid(deltaFluid);
			}
		}
		
		if(world.getBlockState(pos.down()).isOf(state.getBlock()))
		{
			WaterTankBlockEntity blockEntityDown = (WaterTankBlockEntity) world.getBlockEntity(pos.down());
			double deltaFluid = Math.min(blockEntity.getStoredFluid(), blockEntityDown.getStorageCapacity() - blockEntityDown.getStoredFluid());
			
			if(deltaFluid > 0)
			{
				blockEntity.changeStoredFluid(-deltaFluid);
				blockEntityDown.changeStoredFluid(deltaFluid);
			}
		}
		
		blockEntity.renderTop = blockEntity.getStoredFluid() > 0;
		blockEntity.renderBottom = blockEntity.getStoredFluid() > 0 && !world.getBlockState(pos.down()).isOf(state.getBlock());
		
		if(world.getBlockState(pos.up()).isOf(state.getBlock()))
		{
			WaterTankBlockEntity blockEntityUp = (WaterTankBlockEntity) world.getBlockEntity(pos.up());
			
			if(blockEntityUp.renderTop || blockEntityUp.getStoredFluid() == blockEntityUp.getStorageCapacity())
				blockEntity.renderTop = false;
		}
		
		if(storedFluid != blockEntity.getStoredFluid())
		{
			blockEntity.markDirty();
			world.updateListeners(pos, state, state, Block.NOTIFY_LISTENERS);
		}
	}
}