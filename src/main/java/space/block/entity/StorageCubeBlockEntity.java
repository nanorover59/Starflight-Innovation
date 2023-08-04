package space.block.entity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.block.entity.ViewerCountManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import space.block.StarflightBlocks;
import space.block.StorageCubeBlock;

public class StorageCubeBlockEntity extends LootableContainerBlockEntity
{
	private DefaultedList<ItemStack> inventory = DefaultedList.ofSize(27, ItemStack.EMPTY);
	private ViewerCountManager stateManager = new ViewerCountManager()
	{
		@Override
		protected void onContainerOpen(World world, BlockPos pos, BlockState state)
		{
			StorageCubeBlockEntity.this.playSound(state, SoundEvents.BLOCK_PISTON_EXTEND);
			StorageCubeBlockEntity.this.setOpen(state, true);
		}

		@Override
		protected void onContainerClose(World world, BlockPos pos, BlockState state)
		{
			StorageCubeBlockEntity.this.playSound(state, SoundEvents.BLOCK_PISTON_CONTRACT);
			StorageCubeBlockEntity.this.setOpen(state, false);
		}

		@Override
		protected void onViewerCountUpdate(World world, BlockPos pos, BlockState state, int oldViewerCount, int newViewerCount)
		{
		}

		@Override
		protected boolean isPlayerViewing(PlayerEntity player)
		{
			if(player.currentScreenHandler instanceof GenericContainerScreenHandler)
			{
				Inventory inventory = ((GenericContainerScreenHandler) player.currentScreenHandler).getInventory();
				return inventory == StorageCubeBlockEntity.this;
			}
			
			return false;
		}
	};

	public StorageCubeBlockEntity(BlockPos pos, BlockState state)
	{
		super(StarflightBlocks.STORAGE_CUBE_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void writeNbt(NbtCompound nbt)
	{
		super.writeNbt(nbt);
		
		if(!this.serializeLootTable(nbt))
			Inventories.writeNbt(nbt, this.inventory);
	}

	@Override
	public void readNbt(NbtCompound nbt)
	{
		super.readNbt(nbt);
		this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
		
		if(!this.deserializeLootTable(nbt))
			Inventories.readNbt(nbt, this.inventory);
	}

	@Override
	public int size()
	{
		return 27;
	}

	@Override
	protected DefaultedList<ItemStack> getInvStackList()
	{
		return this.inventory;
	}

	@Override
	protected void setInvStackList(DefaultedList<ItemStack> list)
	{
		this.inventory = list;
	}

	@Override
	protected Text getContainerName()
	{
		return Text.translatable(getCachedState().getBlock().getTranslationKey());
	}

	@Override
	protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory)
	{
		return GenericContainerScreenHandler.createGeneric9x3(syncId, playerInventory, this);
	}

	@Override
	public void onOpen(PlayerEntity player)
	{
		if(!this.removed && !player.isSpectator())
			this.stateManager.openContainer(player, this.getWorld(), this.getPos(), this.getCachedState());
	}

	@Override
	public void onClose(PlayerEntity player)
	{
		if(!this.removed && !player.isSpectator())
			this.stateManager.closeContainer(player, this.getWorld(), this.getPos(), this.getCachedState());
	}

	public void tick()
	{
		if(!this.removed)
			this.stateManager.updateViewerCount(this.getWorld(), this.getPos(), this.getCachedState());
	}

	void setOpen(BlockState state, boolean open)
	{
		this.world.setBlockState(this.getPos(), (BlockState) state.with(StorageCubeBlock.OPEN, open), Block.NOTIFY_ALL);
	}

	void playSound(BlockState state, SoundEvent soundEvent)
	{
		Vec3i vec3i = state.get(StorageCubeBlock.FACING).getVector();
		double d = (double) this.pos.getX() + 0.5 + (double) vec3i.getX() / 2.0;
		double e = (double) this.pos.getY() + 0.5 + (double) vec3i.getY() / 2.0;
		double f = (double) this.pos.getZ() + 0.5 + (double) vec3i.getZ() / 2.0;
		this.world.playSound(null, d, e, f, soundEvent, SoundCategory.BLOCKS, 0.5f, this.world.random.nextFloat() * 0.1f + 0.9f);
	}
}