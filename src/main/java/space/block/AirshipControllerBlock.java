package space.block;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiPredicate;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import space.block.entity.BalloonControllerBlockEntity;
import space.block.entity.FluidTankControllerBlockEntity;
import space.entity.AirshipEntity;
import space.entity.MovingCraftEntity;
import space.planet.PlanetDimensionData;
import space.planet.PlanetList;
import space.util.BlockSearch;
import space.vessel.BlockMass;
import space.vessel.MovingCraftBlockData;

public class AirshipControllerBlock extends Block
{
	public static final MapCodec<AirshipControllerBlock> CODEC = AirshipControllerBlock.createCodec(AirshipControllerBlock::new);
	public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
	
	public AirshipControllerBlock(Settings settings)
	{
		super(settings);
	}

	@Override
	public MapCodec<? extends AirshipControllerBlock> getCodec()
	{
		return CODEC;
	}
	
	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
	{
		builder.add(FACING);
	}
	
	@Override
	public BlockState rotate(BlockState state, BlockRotation rotation)
	{
		return (BlockState) state.with(FACING, rotation.rotate((Direction) state.get(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, BlockMirror mirror)
	{
		return state.rotate(mirror.getRotation((Direction) state.get(FACING)));
	}
	
	@Override
	public BlockState getPlacementState(ItemPlacementContext context)
	{
		return (BlockState) this.getDefaultState().with(FACING, context.getHorizontalPlayerFacing().getOpposite());
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos position, PlayerEntity player, BlockHitResult hit)
	{
		if(world.isClient())
			return ActionResult.SUCCESS;
		
		// Detect blocks to be included in the craft construction.
        ArrayList<BlockPos> positionList = new ArrayList<BlockPos>();
        Set<BlockPos> set = new HashSet<BlockPos>();
        BlockSearch.movingCraftSearch(world, position, positionList, set, BlockSearch.MAX_VOLUME, BlockSearch.MAX_DISTANCE);
        
        // Find the center of mass in world coordinates.
        Vec3d centerOfMass = Vec3d.ZERO;
        Vec3d momentOfInertia1 = Vec3d.ZERO;
        Vec3d momentOfInertia2 = Vec3d.ZERO;
        double mass = 0.0;
        double volume = 0.0;
        double balloonVolume = 0.0;
        
        for(BlockPos pos : positionList)
        {
        	double blockMass = BlockMass.getMass(world, pos);
        	double blockVolume = BlockMass.volumeForBlock(world.getBlockState(pos), world, pos);
        	BlockEntity blockEntity = world.getBlockEntity(pos);
        	mass += blockMass;
        	volume += blockVolume;
        	Vec3d centerPos = pos.toCenterPos();
        	centerOfMass = centerOfMass.add(centerPos.getX() * blockMass, centerPos.getY() * blockMass, centerPos.getZ() * blockMass);
        	
        	if(blockEntity != null)
        	{
				if(blockEntity instanceof FluidTankControllerBlockEntity)
				{
					FluidTankControllerBlockEntity fluidTank = (FluidTankControllerBlockEntity) blockEntity;
					
					if(fluidTank.getCenterOfMass() != null)
					{
						double fluidTankMass = fluidTank.getStoredFluid();
						mass += fluidTankMass;
						centerPos = fluidTank.getCenterOfMass().toCenterPos();
						centerOfMass = centerOfMass.add(centerPos.getX() * fluidTankMass, centerPos.getY() * fluidTankMass, centerPos.getZ() * fluidTankMass);
					}
				}
        	}
        }
        
        centerOfMass = centerOfMass.multiply(1.0 / mass);
        
        // Find the components of the moment of inertia.
        for(BlockPos pos : positionList)
        {
        	double blockMass = BlockMass.getMass(world, pos);
        	BlockEntity blockEntity = world.getBlockEntity(pos);
        	Vec3d centerPos = pos.toCenterPos().subtract(centerOfMass);
        	// Square distance to the center of mass coordinates.
        	double sqyz = centerPos.getY() * centerPos.getY() + centerPos.getZ() * centerPos.getZ();
        	double sqxz = centerPos.getX() * centerPos.getX() + centerPos.getZ() * centerPos.getZ();
        	double sqxy = centerPos.getX() * centerPos.getX() + centerPos.getY() * centerPos.getY();
        	// Sum components for the moment of inertia tensor.
        	momentOfInertia1 = momentOfInertia1.subtract(blockMass * sqyz, blockMass * sqxz, blockMass * sqxy);
        	momentOfInertia2 = momentOfInertia2.subtract(-blockMass * centerPos.getX() * centerPos.getY(), -blockMass * centerPos.getX() * centerPos.getZ(), -blockMass * centerPos.getY() * centerPos.getZ());
        	
			if(blockEntity != null && blockEntity instanceof FluidTankControllerBlockEntity)
			{
				FluidTankControllerBlockEntity fluidTank = (FluidTankControllerBlockEntity) blockEntity;

				for(Direction direction : Direction.values())
				{
					ArrayList<BlockPos> checkList = new ArrayList<BlockPos>(); // Check list to avoid ensure each block is only checked once.
					ArrayList<BlockPos> foundList = new ArrayList<BlockPos>(); // Balloon walls are counted as balloon volume and not other volume.
					
					BiPredicate<World, BlockPos> include = (w, p) -> {
						return world.getBlockState(p).getBlock() == StarflightBlocks.FLUID_TANK_INSIDE;
					};
					
					BiPredicate<World, BlockPos> edgeCase = (w, p) -> {
						return world.getBlockState(p).getBlock() == StarflightBlocks.REINFORCED_FABRIC;
					};

					BlockSearch.search(world, pos.offset(direction), checkList, foundList, include, edgeCase, BlockSearch.MAX_VOLUME, true);

					if(checkList.size() > 0 && checkList.size() < BlockSearch.MAX_VOLUME)
					{
						double unitMass = fluidTank.getStoredFluid() / checkList.size();

						for(BlockPos fluidPos : checkList)
						{
							centerPos = fluidPos.toCenterPos().subtract(centerOfMass);
							// Square distance to the center of mass coordinates.
							sqyz = centerPos.getY() * centerPos.getY() + centerPos.getZ() * centerPos.getZ();
							sqxz = centerPos.getX() * centerPos.getX() + centerPos.getZ() * centerPos.getZ();
							sqxy = centerPos.getX() * centerPos.getX() + centerPos.getY() * centerPos.getY();
							// Sum components for the moment of inertia tensor.
							momentOfInertia1 = momentOfInertia1.subtract(unitMass * sqyz, unitMass * sqxz, unitMass * sqxy);
							momentOfInertia2 = momentOfInertia2.subtract(-unitMass * centerPos.getX() * centerPos.getY(), -unitMass * centerPos.getX() * centerPos.getZ(), -unitMass * centerPos.getY() * centerPos.getZ());
						}
						
						if(fluidTank instanceof BalloonControllerBlockEntity && ((BalloonControllerBlockEntity) fluidTank).getStoredFluid() > ((BalloonControllerBlockEntity) fluidTank).getStorageCapacity() * 0.9)
						{
							balloonVolume += checkList.size() + foundList.size();
							volume -= foundList.size();
						}
					}
				}
			}
        }
        
        PlanetDimensionData data = PlanetList.getDimensionDataForWorld(world);
        
        if(data.getPressure() < 0.75)
        {
			MutableText text = Text.translatable("block.space.airship_controller.error_atmosphere");
			player.sendMessage(text, true);
			return ActionResult.SUCCESS;
        }
        else if(balloonVolume < volume)
        {
			MutableText text = Text.translatable("block.space.airship_controller.error_balloon").append(" " + balloonVolume + " / " + volume);
			player.sendMessage(text, true);
			return ActionResult.SUCCESS;
        }
        
        BlockPos centerPos = BlockPos.ofFloored(centerOfMass);
        ArrayList<MovingCraftBlockData> blockDataList = MovingCraftEntity.captureBlocks(world, new BlockPos(MathHelper.floor(centerOfMass.getX()), MathHelper.floor(centerOfMass.getY()), MathHelper.floor(centerOfMass.getZ())), positionList);
        AirshipEntity entity = new AirshipEntity(world, centerPos, blockDataList, world.getBlockState(position).get(FACING), mass, volume, momentOfInertia1.toVector3f(), momentOfInertia2.toVector3f());
        MovingCraftEntity.removeBlocksFromWorld(world, centerPos, blockDataList);
        world.spawnEntity(entity);
		return ActionResult.SUCCESS;
	}
}