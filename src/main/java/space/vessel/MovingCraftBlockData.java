package space.vessel;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.World;
import space.block.EnergyBlock;
import space.block.FluidTankInsideBlock;
import space.block.SolarHubBlock;
import space.block.StarflightBlocks;
import space.block.entity.FluidTankControllerBlockEntity;
import space.util.BooleanByteUtil;

public class MovingCraftBlockData
{
	private BlockState blockState;
	private BlockPos position;
	private NbtCompound blockEntityData;
	private boolean[] sidesShowing = new boolean[6];
	private boolean placeFirst;
	private boolean redstone;
	private double storedFluid;
	
	private MovingCraftBlockData(BlockState blockState, BlockPos position, NbtCompound blockEntityData, boolean[] sidesShowing, boolean placeFirst, boolean redstone, double storedFluid)
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
	
	public static MovingCraftBlockData fromBlock(World world, BlockPos blockPos, BlockPos centerPos, boolean placeFirst)
	{
		BlockState blockState = world.getBlockState(blockPos);
		BlockEntity blockEntity = world.getBlockEntity(blockPos);
		NbtCompound blockEntityData = null;
		
		if(blockEntity != null)
			blockEntityData = blockEntity.createNbt();
		
		boolean[] sidesShowing = new boolean[6];
		Direction[] directions = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN};
		BlockPos[] offsets = {blockPos.north(), blockPos.east(), blockPos.south(), blockPos.west(), blockPos.up(), blockPos.down()};
		
		for(int i  = 0; i < 6; i++)
		{
			BlockState otherBlockState = world.getBlockState(offsets[i]);
			
			if(otherBlockState.getBlock() == StarflightBlocks.FLUID_TANK_INSIDE)
				sidesShowing[i] = false;
			else if(!otherBlockState.isOpaqueFullCube(world, offsets[i]))
				sidesShowing[i] = true;
			else if(blockState.isSideSolidFullSquare(world, blockPos, directions[i]) && otherBlockState.isSideSolidFullSquare(world, offsets[i], directions[i].getOpposite()))
				sidesShowing[i] = false;
			else
				sidesShowing[i] = true;
		}
		
		double storedFluid = 0.0;
		
		if(blockEntity != null && blockEntity instanceof FluidTankControllerBlockEntity)
		{
			storedFluid = ((FluidTankControllerBlockEntity) blockEntity).getStoredFluid();
			((FluidTankControllerBlockEntity) blockEntity).setStoredFluid(0.0);
		}
		
		return new MovingCraftBlockData(blockState, blockPos.subtract(centerPos), blockEntityData, sidesShowing, placeFirst, world.isReceivingRedstonePower(blockPos), storedFluid);
	}
	
	public void toBlock(World world, BlockPos centerPos, int rotationSteps)
	{
		if(blockState.getProperties().contains(HorizontalFacingBlock.FACING))
		{
			Direction direction = blockState.get(HorizontalFacingBlock.FACING);
			
			for(int i = 0; i < rotationSteps; i++)
				direction = direction.rotateClockwise(Axis.Y);
				
			blockState = blockState.with(HorizontalFacingBlock.FACING, direction);
		}
		
		BlockRotation rotation = BlockRotation.NONE;
		
		for(int i = 0; i < rotationSteps; i++)
			rotation = rotation.rotate(BlockRotation.CLOCKWISE_90);
		
		BlockPos blockPos = centerPos.add(position.rotate(rotation));
		
		// Do not overwrite existing solid world blocks.
		if(world.getBlockState(blockPos).getMaterial().blocksMovement())
		{
			BlockEntity blockEntity = blockState.hasBlockEntity() ? ((BlockEntityProvider) blockState.getBlock()).createBlockEntity(blockPos, blockState) : null;
			
			if(blockEntity != null && blockEntityData != null)
				blockEntity.readNbt(blockEntityData);
			
            Block.dropStacks(blockState, world, blockPos, blockEntity, null, ItemStack.EMPTY);
            return;
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
			blockEntity.readNbt(blockEntityData);
		
		if(blockState.getBlock() instanceof EnergyBlock)
		{
			((EnergyBlock) blockState.getBlock()).addNode(world, blockPos);
			
			if(blockState.getBlock() instanceof SolarHubBlock)
				SolarHubBlock.updateSolarPanels(world, blockPos);
		}
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
	
	public double getStoredFluid()
	{
		return storedFluid;
	}
	
	public NbtCompound saveData(NbtCompound data)
	{
		NbtCompound localData = new NbtCompound();
		localData.put("blockState", NbtHelper.fromBlockState(blockState));
		localData.put("position", NbtHelper.fromBlockPos(position));
		
		if(blockEntityData != null)
			localData.put("blockEntityData", blockEntityData);
		
		localData.putByte("sidesShowing", BooleanByteUtil.toByte(sidesShowing));
		localData.putBoolean("placeFirst", placeFirst);
		localData.putBoolean("redstone", redstone);
		
		if(storedFluid > 0)
			localData.putDouble("storedFluid", storedFluid);
		
		data.put(position.toShortString(), localData);
		return data;
	}
	
	public static MovingCraftBlockData loadData(NbtCompound data)
	{
		BlockState blockState = NbtHelper.toBlockState(data.getCompound("blockState"));
		BlockPos position = NbtHelper.toBlockPos(data.getCompound("position"));
		NbtCompound blockEntityData = data.contains("blockEntityData") ? data.getCompound("blockEntityData") : null;
		boolean[] booleanArray = BooleanByteUtil.toBooleanArray(data.getByte("sidesShowing"));
		boolean[] sidesShowing = new boolean[6];
		
		for(int i = 0; i < 6; i++)
			sidesShowing[i] = booleanArray[i];
		
		boolean placeFirst = data.getBoolean("placeFirst");
		boolean redstone = data.getBoolean("redstone");
		double storedFluid = data.contains("storedFluid") ? data.getDouble("storedFluid") : 0.0;
		return new MovingCraftBlockData(blockState, position, blockEntityData, sidesShowing, placeFirst, redstone, storedFluid);
	}
}