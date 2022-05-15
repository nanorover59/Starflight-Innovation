package space.vessel;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.World;
import space.block.EnergyBlock;
import space.block.entity.FluidTankControllerBlockEntity;
import space.util.BooleanByteUtil;

public class MovingCraftBlockData
{
	private BlockState blockState;
	private BlockPos position;
	private NbtCompound blockEntityData;
	private boolean[] sidesShowing = new boolean[6];
	private boolean placeFirst;
	private double storedFluid;
	
	private MovingCraftBlockData(BlockState blockState, BlockPos position, NbtCompound blockEntityData, boolean[] sidesShowing, boolean placeFirst, double storedFluid)
	{
		this.blockState = blockState;
		this.position = position;
		this.blockEntityData = blockEntityData == null ? null : blockEntityData.copy();
		
		for(int i = 0; i < 6; i++)
			this.sidesShowing[i] = sidesShowing[i];
		
		this.placeFirst = placeFirst;
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
			
			if(!otherBlockState.isOpaqueFullCube(world, offsets[i]))
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
		
		return new MovingCraftBlockData(blockState, blockPos.subtract(centerPos), blockEntityData, sidesShowing, placeFirst, storedFluid);
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
		world.setBlockState(blockPos, blockState, Block.NOTIFY_LISTENERS);
		BlockEntity blockEntity = world.getBlockEntity(blockPos);
		
		if(blockEntity != null && blockEntityData != null)
			blockEntity.readNbt(blockEntityData);
		
		if(blockState.getBlock() instanceof EnergyBlock)
			((EnergyBlock) blockState.getBlock()).addNode(world, blockPos);
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
		double storedFluid = data.contains("storedFluid") ? data.getDouble("storedFluid") : 0.0;
		return new MovingCraftBlockData(blockState, position, blockEntityData, sidesShowing, placeFirst, storedFluid);
	}
}
