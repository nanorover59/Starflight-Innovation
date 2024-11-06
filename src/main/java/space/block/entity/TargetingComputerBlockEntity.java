package space.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.SingleStackInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Clearable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import space.block.StarflightBlocks;

public class TargetingComputerBlockEntity extends BlockEntity implements Clearable, SingleStackInventory.SingleStackBlockEntityInventory
{
	private ItemStack stack = ItemStack.EMPTY;
	
	public TargetingComputerBlockEntity(BlockPos pos, BlockState state)
	{
		super(StarflightBlocks.TARGETING_COMPUTER_BLOCK_ENTITY, pos, state);
	}

	public void dropCard()
	{
		BlockPos blockPos = this.getPos();
		ItemStack itemStack = this.getStack();

		if(!itemStack.isEmpty())
		{
			Vec3d vec3d = Vec3d.add(blockPos, 0.5, 1.01, 0.5).addRandom(this.world.random, 0.7F);
			ItemStack itemStack2 = itemStack.copy();
			ItemEntity itemEntity = new ItemEntity(this.world, vec3d.getX(), vec3d.getY(), vec3d.getZ(), itemStack2);
			itemEntity.setToDefaultPickupDelay();
			this.world.spawnEntity(itemEntity);
			this.emptyStack();
		}
	}

	@Override
	public ItemStack getStack()
	{
		return stack;
	}

	@Override
	public void setStack(ItemStack stack)
	{
		this.stack = stack;
	}

	@Override
	public BlockEntity asBlockEntity()
	{
		return this;
	}
	
	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup)
	{
		super.readNbt(nbt, registryLookup);
		
		if(nbt.contains("stack"))
			stack = (ItemStack) ItemStack.fromNbt(registryLookup, nbt.getCompound("stack")).orElse(ItemStack.EMPTY);
	}
	
	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup)
	{
		super.writeNbt(nbt, registryLookup);
		
		if(!stack.isEmpty())
			nbt.put("stack", stack.encode(registryLookup));
	}
}