package space.craft;

import java.util.ArrayList;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import space.block.FluidTankInsideBlock;
import space.block.StarflightBlocks;
import space.block.entity.FluidTankControllerBlockEntity;
import space.block.entity.RocketControllerBlockEntity;
import space.util.BooleanByteUtil;

public class MovingCraftBlock
{
	private BlockState blockState;
	private BlockPos position;
	private NbtCompound blockEntityData;
	private boolean[] sidesShowing = new boolean[6];
	private boolean placeFirst;
	private boolean redstone;
	private long storedFluid;
	
	public MovingCraftBlock(BlockState blockState, BlockPos position, NbtCompound blockEntityData, boolean[] sidesShowing, boolean placeFirst, boolean redstone, long storedFluid)
	{
		this.blockState = blockState;
		this.position = position;
		this.blockEntityData = blockEntityData == null ? null : blockEntityData.copy();
		
		for(int i = 0; i < 6; i++)
			this.sidesShowing[i] = sidesShowing[i];
		
		this.placeFirst = placeFirst;
		this.redstone = redstone;
		this.storedFluid = storedFluid;
	}
	
	public MovingCraftBlock(PacketByteBuf buffer)
    {
    	this(readBlockState(buffer), buffer.readBlockPos(), buffer.readNbt(), unpackBooleans(buffer.readInt()), buffer.readBoolean(), buffer.readBoolean(), buffer.readLong());
    }
	
	public static MovingCraftBlock fromBlock(World world, ArrayList<BlockPos> positionList, BlockPos blockPos, BlockPos centerPos, boolean placeFirst)
	{
		BlockState blockState = world.getBlockState(blockPos);
		boolean[] sidesShowing = new boolean[6];
		Direction[] directions = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN};
		BlockPos[] offsets = {blockPos.north(), blockPos.east(), blockPos.south(), blockPos.west(), blockPos.up(), blockPos.down()};
		NbtCompound blockEntityData = null;
		long storedFluid = 0;
		
		for(int i  = 0; i < 6; i++)
		{
			BlockState otherBlockState = world.getBlockState(offsets[i]);
			
			if(otherBlockState.getBlock() == StarflightBlocks.FLUID_TANK_INSIDE)
				sidesShowing[i] = false;
			else if(!otherBlockState.isOpaqueFullCube(world, offsets[i]) || otherBlockState.getBlock() == StarflightBlocks.BUFFER)
				sidesShowing[i] = true;
			else if(blockState.isSideSolidFullSquare(world, blockPos, directions[i]) && otherBlockState.isSideSolidFullSquare(world, offsets[i], directions[i].getOpposite()))
				sidesShowing[i] = false;
			else
				sidesShowing[i] = true;
		}
		
		if(blockState.getBlock() instanceof BlockEntityProvider)
		{
			BlockEntity blockEntity = world.getBlockEntity(blockPos);
			
			if(blockEntity != null && blockEntity instanceof FluidTankControllerBlockEntity)
				storedFluid = ((FluidTankControllerBlockEntity) blockEntity).getStoredFluid();
			
			if(blockEntity != null && !(blockEntity instanceof RocketControllerBlockEntity))
				blockEntityData = blockEntity.createNbt((RegistryWrapper.WrapperLookup) world.getRegistryManager());
		}
		
		return new MovingCraftBlock(blockState, blockPos.subtract(centerPos), blockEntityData, sidesShowing, placeFirst, world.isReceivingRedstonePower(blockPos), storedFluid);
	}
	
	public void toBlock(World world, BlockPos centerPos, Quaternionf quaternion)
	{
		Matrix4f rotationMatrix = new Matrix4f().rotation(quaternion);
		
		if(blockState.getProperties().contains(Properties.FACING))
		{
			Direction direction = blockState.get(Properties.FACING);
			direction = Direction.transform(rotationMatrix, direction);
			blockState = blockState.with(Properties.FACING, direction);
		}
		else if(blockState.getProperties().contains(Properties.HORIZONTAL_FACING))
		{
			Direction direction = blockState.get(Properties.HORIZONTAL_FACING);
			direction = Direction.transform(rotationMatrix, direction);
			
			if(direction != Direction.UP && direction != Direction.DOWN)
				blockState = blockState.with(Properties.HORIZONTAL_FACING, direction);
		}
		else
		{
			Direction direction = Direction.transform(rotationMatrix, Direction.NORTH);
			BlockRotation rotation = BlockRotation.NONE;
			
			if(direction == Direction.SOUTH)
	        	rotation = BlockRotation.CLOCKWISE_180;
			else if(direction == Direction.EAST)
				rotation = BlockRotation.CLOCKWISE_90;
	        else if(direction == Direction.WEST)
	        	rotation = BlockRotation.COUNTERCLOCKWISE_90;
			
			blockState = blockState.rotate(rotation);
		}
		
        Vector3f offset = new Vector3f(position.getX(), position.getY(), position.getZ()).rotate(quaternion);
		BlockPos blockPos = centerPos.add(MathHelper.floor(offset.x()), MathHelper.floor(offset.y()), MathHelper.floor(offset.z()));
		BlockState conflictState = world.getBlockState(blockPos);
		
		// Do not overwrite existing solid world blocks.
		if(conflictState.blocksMovement())
		{
			if(conflictState.getBlock().getHardness() > blockState.getBlock().getHardness())
			{
				BlockEntity blockEntity = blockState.hasBlockEntity() ? ((BlockEntityProvider) blockState.getBlock()).createBlockEntity(blockPos, blockState) : null;
				
				if(blockEntity != null && blockEntityData != null)
					blockEntity.readComponentlessNbt(blockEntityData, (RegistryWrapper.WrapperLookup) world.getRegistryManager());
				
	            Block.dropStacks(blockState, world, blockPos, blockEntity, null, ItemStack.EMPTY);
	            return;
			}
			else
			{
				BlockEntity blockEntity = world.getBlockEntity(blockPos);
	            Block.dropStacks(conflictState, world, blockPos, blockEntity, null, ItemStack.EMPTY);
			}
		}
		
		if(blockState.getBlock() instanceof FluidTankInsideBlock)
		{
			world.setBlockState(blockPos, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
			return;
		}
		else if(blockState.getBlock() instanceof Waterloggable)
		{
			FluidState fluidState = world.getFluidState(blockPos);
			blockState = blockState.with(Properties.WATERLOGGED, fluidState.getFluid() == Fluids.WATER && fluidState.isStill());
		}
		
		world.setBlockState(blockPos, blockState, Block.NOTIFY_LISTENERS);
		BlockEntity blockEntity = world.getBlockEntity(blockPos);
		
		if(blockEntity != null && blockEntityData != null)
			blockEntity.readComponentlessNbt(blockEntityData, (RegistryWrapper.WrapperLookup)world.getRegistryManager());
	}

	public BlockState getBlockState()
	{
		return blockState;
	}

	public BlockPos getPosition()
	{
		return position;
	}

	public NbtCompound getBlockEntityData()
	{
		return blockEntityData;
	}
	
	public boolean[] getSidesShowing()
	{
		return sidesShowing;
	}
	
	public boolean placeFirst()
	{
		return placeFirst;
	}
	
	public boolean redstonePower()
	{
		return redstone;
	}
	
	public long getStoredFluid()
	{
		return storedFluid;
	}
	
	public NbtCompound saveData(NbtCompound data)
	{
		data.put("blockState", NbtHelper.fromBlockState(blockState));
		data.put("position", NbtHelper.fromBlockPos(position));
		
		if(blockEntityData != null)
			data.put("blockEntityData", blockEntityData);
		
		data.putByte("sidesShowing", BooleanByteUtil.toByte(sidesShowing));
		data.putBoolean("placeFirst", placeFirst);
		data.putBoolean("redstone", redstone);
		
		if(storedFluid > 0)
			data.putDouble("storedFluid", storedFluid);
		
		return data;
	}
	
	public static MovingCraftBlock loadData(NbtCompound data)
	{
		BlockState blockState = NbtHelper.toBlockState(Registries.BLOCK.getReadOnlyWrapper(), data.getCompound("blockState"));
		BlockPos position = NbtHelper.toBlockPos(data, "position").get();
		NbtCompound blockEntityData = data.contains("blockEntityData") ? data.getCompound("blockEntityData") : null;
		boolean[] booleanArray = BooleanByteUtil.toBooleanArray(data.getByte("sidesShowing"));
		boolean[] sidesShowing = new boolean[6];
		
		for(int i = 0; i < 6; i++)
			sidesShowing[i] = booleanArray[i];
		
		boolean placeFirst = data.getBoolean("placeFirst");
		boolean redstone = data.getBoolean("redstone");
		long storedFluid = data.contains("storedFluid") ? data.getLong("storedFluid") : 0;
		return new MovingCraftBlock(blockState, position, blockEntityData, sidesShowing, placeFirst, redstone, storedFluid);
	}
	
	public static void writeBlockData(PacketByteBuf buffer, MovingCraftBlock blockData)
	{
		writeBlockState(buffer, blockData.getBlockState());
		buffer.writeBlockPos(blockData.getPosition());
		buffer.writeNbt(blockData.getBlockEntityData());
		buffer.writeInt(packBooleans(blockData.getSidesShowing()));
		buffer.writeBoolean(blockData.placeFirst());
		buffer.writeBoolean(blockData.redstonePower());
		buffer.writeLong(blockData.getStoredFluid());
	}
	
	private static void writeBlockState(PacketByteBuf buffer, BlockState state)
	{
		buffer.writeIdentifier(Registries.BLOCK.getId(state.getBlock()));
		buffer.writeInt(state.getProperties().size());

		for(Property<?> property : state.getProperties())
		{
			buffer.writeString(property.getName());
			buffer.writeString(getPropertyValueAsString(state, property));
		}
	}

	public static BlockState readBlockState(PacketByteBuf buffer)
	{
		Identifier blockId = buffer.readIdentifier();
		Block block = Registries.BLOCK.get(blockId);
		BlockState state = block.getDefaultState();
		int propertyCount = buffer.readInt();

		for(int i = 0; i < propertyCount; i++)
		{
			String propertyName = buffer.readString();
			String propertyValue = buffer.readString();
			Property<?> property = block.getStateManager().getProperty(propertyName);

			if(property != null)
				state = setPropertyValue(state, property, propertyValue);
		}

		return state;
	}
	
	private static <T extends Comparable<T>> String getPropertyValueAsString(BlockState state, Property<T> property)
	{
		return property.name(state.get(property));
	}

	private static <T extends Comparable<T>> BlockState setPropertyValue(BlockState state, Property<T> property, String value)
	{
		return state.with(property, property.parse(value).orElse(state.get(property)));
	}
	
	private static int packBooleans(boolean ... booleans)
	{
		int packed = 0;
		
		for(int i = 0; i < booleans.length; i++)
        {
            if(booleans[i])
            	packed |= (1 << i);
        }
		
		return packed;
	}
	
	private static boolean[] unpackBooleans(int packed)
	{
		boolean[] sidesShowing = new boolean[6];
		
		for (int i = 0; i < 6; i++)
			sidesShowing[i] = (packed & (1 << i)) != 0;
		
		return sidesShowing;
	}
	
	public static boolean isBlockSolid(World world, BlockPos blockPos)
	{
		BlockState blockState = world.getBlockState(blockPos);
		return blockState.isSolidBlock(world, blockPos) || blockState.getBlock() instanceof SlabBlock || blockState.getBlock() instanceof StairsBlock;
	}
	
	public static double volumeForBlock(BlockState blockState, World world, BlockPos pos)
	{
		if(blockState.isAir() || blockState.getBlock() == StarflightBlocks.FLUID_TANK_INSIDE)
			return 0.0;
		
		double volume = 0;
		
		for(Box box : blockState.getOutlineShape(world, pos).getBoundingBoxes())
			volume += box.getLengthX() * box.getLengthY() * box.getLengthZ();
		
		return volume;
	}
	
	public static double getMassForBlock(World world, BlockPos pos)
	{
		BlockState blockState = world.getBlockState(pos);
		
		double density = 50; // Assume a density of 50kg per cubic meter by default.
		
		//if(blockState.isIn(StarflightBlocks.FLUID_TANK_BLOCK_TAG))
		//	density = 10;
		
		double mass = volumeForBlock(blockState, world, pos) * density;
		BlockEntity blockEntity = world.getBlockEntity(pos);
	
		// If the block has a block entity, check for an inventory using the same function as hoppers.
		if(blockEntity != null)
    	{
			Inventory inventory = HopperBlockEntity.getInventoryAt(world, pos);
			
			if(inventory != null)
			{
				mass *= 0.25;
				
	    		for(int i = 0; i < inventory.size(); i++)
	    		{
	    			ItemStack stack = inventory.getStack(i);
	    			
	    			if(stack.getItem() instanceof BlockItem)
	    				mass += stack.getCount() * volumeForBlock(((BlockItem) stack.getItem()).getBlock().getDefaultState(), world, pos) * 100.0;
	    			else
	    				mass += stack.getCount() * 10.0;
	    		}
			}
    	}
		
		return mass;
	}
}