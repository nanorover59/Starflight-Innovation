package space.block.entity;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CrafterBlock;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import space.block.ElectricCrafterBlock;
import space.block.EnergyBlock;
import space.block.StarflightBlocks;
import space.screen.ElectricCrafterScreenHandler;

public class ElectricCrafterBlockEntity extends LockableContainerBlockEntity implements ExtendedScreenHandlerFactory<BlockPos>, SidedInventory, RecipeInputInventory, EnergyBlockEntity
{
	private long energy;
	private int time;
	private int totalTime;
	private ElectricCrafterScreenHandler screen;
	private DefaultedList<ItemStack> inventory = DefaultedList.ofSize(20, ItemStack.EMPTY);
	private ItemStack previewStack = ItemStack.EMPTY;
	private RecipeEntry<CraftingRecipe> recipe;
	
	protected final PropertyDelegate propertyDelegate;

	public ElectricCrafterBlockEntity(BlockPos blockPos, BlockState blockState)
	{
		super(StarflightBlocks.ELECTRIC_CRAFTER_BLOCK_ENTITY, blockPos, blockState);
		this.propertyDelegate = new PropertyDelegate()
		{
			public int get(int index)
			{
				switch(index)
				{
				case 0:
					return (int) ElectricCrafterBlockEntity.this.energy;
				case 1:
					return ElectricCrafterBlockEntity.this.time;
				case 2:
					return ElectricCrafterBlockEntity.this.totalTime;
				default:
					return 0;
				}
			}

			public void set(int index, int value)
			{
				switch(index)
				{
				case 0:
					ElectricCrafterBlockEntity.this.energy = value;
					break;
				case 1:
					ElectricCrafterBlockEntity.this.time = value;
					break;
				case 2:
					ElectricCrafterBlockEntity.this.totalTime = value;
					break;
				}
			}

			public int size()
			{
				return 3;
			}
		};
	}
	
	private static boolean canAcceptRecipeOutput(DynamicRegistryManager registryManager, CraftingRecipe recipe, DefaultedList<ItemStack> slots)
	{
		if(recipe != null)
		{
			ItemStack itemStack = recipe.getResult(registryManager);
			
			if(itemStack.isEmpty())
				return false;
			else if(findIngredients(slots, false))
			{
				ItemStack itemStack2 = slots.get(18);

				if(itemStack2.isEmpty())
					return true;
				else if(!ItemStack.areItemsEqual(itemStack2, itemStack))
					return false;
				else
					return itemStack2.getCount() + itemStack.getCount() <= itemStack.getMaxCount();
			}
		}
		
		return false;
	}

	private static boolean craftRecipe(DynamicRegistryManager registryManager, CraftingRecipe recipe, DefaultedList<ItemStack> slots)
	{
		if(recipe != null)
		{
			ItemStack itemStack = recipe.getResult(registryManager);
			ItemStack itemStack2 = slots.get(19);
			
			if(itemStack2.isEmpty())
				slots.set(19, itemStack.copy());
			else if(itemStack2.isOf(itemStack.getItem()))
				itemStack2.increment(itemStack.getCount());
			
			findIngredients(slots, true);
			return true;
		}
		else
			return false;
	}
	
	private static boolean findIngredients(DefaultedList<ItemStack> slots, boolean use)
	{
		List<ItemStack> inputSlots = slots.subList(1, 10).stream().map(stack -> stack.copy()).collect(Collectors.toList());
		List<ItemStack> storageSlots = use ? slots.subList(10, 19) : slots.subList(10, 19).stream().map(stack -> stack.copy()).collect(Collectors.toList());
		
		for(ItemStack inputStack : inputSlots)
		{
			if(inputStack.isEmpty())
				continue;
			
			boolean found = false;
			
			for(ItemStack storageStack : storageSlots)
			{
				if(storageStack.isEmpty())
					continue;
				
				if(ItemStack.areItemsEqual(inputStack, storageStack) && storageStack.getCount() > 0)
				{
					storageStack.decrement(1);
	                found = true;
	                break;
	            }
			}
			
			if(!found)
				return false;
		}
		
		return true;
	}
	
	/**
	 * Check for a valid recipe and update the result ItemStack.
	 */
	public void updateResult()
	{
		CraftingRecipeInput craftingRecipeInput = createRecipeInput();
		Optional<RecipeEntry<CraftingRecipe>> recipe = CrafterBlock.getCraftingRecipe(world, craftingRecipeInput);
		ItemStack itemStack = (ItemStack) recipe.map(recipeEntry -> ((CraftingRecipe) recipeEntry.value()).craft(craftingRecipeInput, world.getRegistryManager())).orElse(ItemStack.EMPTY);
		recipe.ifPresentOrElse(this::setRecipe, () -> setRecipe(null));
		setPreview(itemStack);
		markDirty();
		world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_LISTENERS);
	}
	
	public int getTime()
	{
		return 100;
	}
	
	@Override
	public int[] getAvailableSlots(Direction side)
	{
		int[] slots = new int[10];
		
		for(int i = 0; i < 10; i++)
			slots[i] = i + 10;
		
		return slots;
	}

	@Override
	public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir)
	{
		return slot > 9 && slot < 19;
	}

	@Override
	public boolean canExtract(int slot, ItemStack stack, Direction dir)
	{
		return slot == 19;
	}
	
	private List<ItemStack> getInputStacks()
	{
		return inventory.subList(1, 10);
	}

	@Override
	public void provideRecipeInputs(RecipeMatcher finder)
	{
		for(ItemStack itemStack : getInputStacks())
			finder.addUnenchantedInput(itemStack);
	}
	
	@Override
	public int size()
	{
		return 20;
	}

	@Override
	public int getWidth()
	{
		return 3;
	}

	@Override
	public int getHeight()
	{
		return 3;
	}
	
	@Override
	public CraftingRecipeInput.Positioned createPositionedRecipeInput()
	{
		return CraftingRecipeInput.createPositioned(this.getWidth(), this.getHeight(), this.getHeldStacks().subList(1, 10));
	}
	
	@Override
	public DefaultedList<ItemStack> getHeldStacks()
	{
		return this.inventory;
	}

	@Override
	protected void setHeldStacks(DefaultedList<ItemStack> inventory)
	{
		this.inventory = inventory;
	}

	@Override
	public boolean isEmpty()
	{
		for(ItemStack itemStack : this.inventory)
		{
			if(!itemStack.isEmpty())
				return false;
		}

		return true;
	}
	
	@Override
	public ItemStack getStack(int slot)
	{
		return this.inventory.get(slot);
	}

	public boolean canPlayerUse(PlayerEntity player)
	{
		if(this.world.getBlockEntity(this.pos) != this)
			return false;
		else
			return player.squaredDistanceTo((double) this.pos.getX() + 0.5, (double) this.pos.getY() + 0.5, (double) this.pos.getZ() + 0.5) <= 64.0;
	}

	public void clear()
	{
		this.inventory.clear();
	}
	
	public RecipeEntry<CraftingRecipe> getRecipe()
	{
		return recipe;
	}
	
	public void setRecipe(RecipeEntry<CraftingRecipe> recipe)
	{
		this.recipe = recipe;
	}
	
	public ItemStack getPreviewStack()
	{
		return previewStack;
	}
	
	public void setPreview(ItemStack previewStack)
	{
		this.previewStack = previewStack;
	}
	
	@Override
	public Text getContainerName()
	{
		return Text.translatable(getCachedState().getBlock().getTranslationKey());
	}
	
	@Override
	protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory)
	{
		screen = new ElectricCrafterScreenHandler(syncId, playerInventory, this, this.propertyDelegate, ScreenHandlerContext.create(this.world, this.getPos()));
		return screen;
	}
	
	@Override
	public long getEnergyCapacity()
	{
		return ((EnergyBlock) getCachedState().getBlock()).getEnergyCapacity();
	}
	
	@Override
	public long getEnergy()
	{
		return energy;
	}

	@Override
	public void setEnergy(long energy)
	{
		this.energy = energy;
	}
	
	public long getEnergyUse()
	{
		return ((EnergyBlock) getCachedState().getBlock()).getInput();
	}
	
	@Override
	public void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup)
	{
		super.writeNbt(nbt, registryLookup);
		nbt.putLong("energy", this.energy);
		nbt.putShort("time", (short) this.time);
		nbt.putShort("totalTime", (short) this.totalTime);
		Inventories.writeNbt(nbt, this.inventory, registryLookup);
		
		if(!previewStack.isEmpty())
			nbt.put("previewStack", previewStack.encode(registryLookup));
	}
	
	@Override
	public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup)
	{
		super.readNbt(nbt, registryLookup);
		this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
		Inventories.readNbt(nbt, this.inventory, registryLookup);
		this.energy = nbt.getLong("energy");
		this.time = nbt.getShort("time");
		this.totalTime = nbt.getShort("totalTime");
		
		if(nbt.contains("previewStack"))
			this.previewStack = ItemStack.fromNbt(registryLookup, nbt.get("previewStack")).orElse(ItemStack.EMPTY);
		else
			this.previewStack = ItemStack.EMPTY;
	}
	
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
	
	@Override
	public BlockPos getScreenOpeningData(ServerPlayerEntity player)
	{
		return pos;
	}

	public static void serverTick(World world, BlockPos pos, BlockState state, ElectricCrafterBlockEntity blockEntity)
	{
		ItemStack batteryStack = (ItemStack) blockEntity.inventory.get(0);
		long power = blockEntity.getEnergyUse();
		boolean isWorking = false;
		
		if(blockEntity.recipe != null && canAcceptRecipeOutput(world.getRegistryManager(), blockEntity.recipe.value(), blockEntity.inventory) && blockEntity.removeEnergy(power, true) == power)
		{
			blockEntity.time++;
			blockEntity.totalTime = blockEntity.getTime();
			isWorking = true;

			if(blockEntity.time == blockEntity.totalTime)
			{
				blockEntity.time = 0;
				craftRecipe(world.getRegistryManager(), blockEntity.recipe.value(), blockEntity.inventory);
			}
		}
		else if(!blockEntity.previewStack.isEmpty())
			blockEntity.updateResult();
		
		if(blockEntity.time > 0 && !isWorking)
			blockEntity.time = MathHelper.clamp((int) (blockEntity.time - 2), (int) 0, (int) blockEntity.totalTime);
		
		if(world.getBlockState(pos).get(ElectricCrafterBlock.LIT) != isWorking)
		{
			state = (BlockState) state.with(ElectricCrafterBlock.LIT, isWorking);
			world.setBlockState(pos, state, Block.NOTIFY_ALL);
			markDirty(world, pos, state);
		}
		
		blockEntity.dischargeItem(batteryStack, blockEntity.getEnergyUse() * 2);
	}
}