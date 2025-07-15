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
import net.minecraft.world.World;
import space.block.StarflightBlocks;
import space.util.FluidResourceType;

public class WaterTankBlockEntity extends BlockEntity implements FluidStorageBlockEntity
{
	private final FluidResourceType fluid = FluidResourceType.WATER;
	private long storedFluid;
	private boolean renderTop;
	private boolean renderBottom;
	
	public WaterTankBlockEntity(BlockPos pos, BlockState state)
	{
		super(StarflightBlocks.WATER_TANK_BLOCK_ENTITY, pos, state);
		storedFluid = 0;
		renderTop = false;
		renderBottom = false;
	}
	
	@Override
	public long getFluidCapacity(FluidResourceType fluidType)
	{
		return fluid.getStorageDensity();
	}
	
	@Override
	public long getFluid(FluidResourceType fluidType)
	{
		return storedFluid;
	}

	@Override
	public void setFluid(FluidResourceType fluidType, long fluid)
	{
		this.storedFluid = fluid;
		markDirty();
		world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_LISTENERS);
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
		this.storedFluid = nbt.getLong("storedFluid");
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
		boolean renderingTop = blockEntity.renderTop;
		boolean renderingBottom = blockEntity.renderBottom;
		
		if(world.getBlockState(pos.down()).isOf(state.getBlock()))
		{
			WaterTankBlockEntity blockEntityDown = (WaterTankBlockEntity) world.getBlockEntity(pos.down());
			long canFill = blockEntityDown.addFluid(blockEntity.fluid, blockEntity.storedFluid, false);
			
			if(canFill > 0)
			{
				blockEntity.removeFluid(blockEntity.fluid, canFill, true);
				blockEntityDown.addFluid(blockEntity.fluid, canFill, true);
			}
		}
		
		blockEntity.renderTop = blockEntity.storedFluid > 0;
		
		if(world.getBlockState(pos.up()).isOf(state.getBlock()))
		{
			WaterTankBlockEntity blockEntityUp = (WaterTankBlockEntity) world.getBlockEntity(pos.up());
			
			if(blockEntity.storedFluid == blockEntity.fluid.getStorageDensity() && blockEntityUp.storedFluid > 0)
				blockEntity.renderTop = false;
		}
		
		if(blockEntity.renderTop != renderingTop || blockEntity.renderBottom != renderingBottom)
		{
			blockEntity.markDirty();
			world.updateListeners(pos, state, state, Block.NOTIFY_LISTENERS);
		}
	}
}