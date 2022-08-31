package space.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import net.fabricmc.fabric.api.dimension.v1.FabricDimensions;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import space.StarflightMod;
import space.block.FluidTankControllerBlock;
import space.block.StarflightBlocks;
import space.block.entity.FluidTankControllerBlockEntity;
import space.inventory.ImplementedInventory;
import space.mixin.common.EntityMixin;
import space.vessel.MovingCraftBlockData;

public class MovingCraftEntity extends Entity
{
	private static final Direction[] DIRECTIONS = Direction.values();
	private static final TrackedData<BlockPos> INITIAL_BLOCK_POS = DataTracker.registerData(MovingCraftEntity.class, TrackedDataHandlerRegistry.BLOCK_POS);
	private static final TrackedData<Integer> FORWARD = DataTracker.registerData(MovingCraftEntity.class, TrackedDataHandlerRegistry.INTEGER);
	private static final TrackedData<Float> CRAFT_ROLL = DataTracker.registerData(MovingCraftEntity.class, TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Float> CRAFT_PITCH = DataTracker.registerData(MovingCraftEntity.class, TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Float> CRAFT_YAW = DataTracker.registerData(MovingCraftEntity.class, TrackedDataHandlerRegistry.FLOAT);
	protected ArrayList<MovingCraftBlockData> blockDataList = new ArrayList<MovingCraftBlockData>();
	protected ArrayList<ServerPlayerEntity> playersInRange = new ArrayList<ServerPlayerEntity>();
	protected HashMap<UUID, BlockPos> entityOffsets = new HashMap<UUID, BlockPos>();
	protected BlockPos centerOfMass;
	protected float craftRoll;
	protected float craftPitch;
	protected float craftYaw;
	private int clientInterpolationSteps;
    private double clientX;
    private double clientY;
    private double clientZ;
    private double clientYaw;
    private double clientPitch;
    private double clientXVelocity;
    private double clientYVelocity;
    private double clientZVelocity;
	
	public MovingCraftEntity(EntityType<? extends MovingCraftEntity> entityType, World world)
	{
        super(entityType, world);
    }
	
	public MovingCraftEntity(World world, BlockPos position, ArrayList<MovingCraftBlockData> blockDataList)
	{
		this((EntityType<? extends MovingCraftEntity>) StarflightEntities.MOVING_CRAFT, world);
		this.setPosition(position.getX() + 0.5, position.getY(), position.getZ() + 0.5);
		this.setInitialBlockPos(this.getBlockPos());
		this.craftYaw = 0.0f;
		
		if(!blockDataList.isEmpty())
		{
			for(MovingCraftBlockData blockData : blockDataList)
				this.blockDataList.add(blockData);
		}
		else
			this.setRemoved(RemovalReason.DISCARDED);
	}
	
	@Override
    public boolean isAttackable()
	{
        return false;
    }
	
	@Override
	public boolean collidesWith(Entity other)
	{
		return false;
	}
	
	/**
	 * Should this entity be rendered given a squared distance to the client camera viewpoint.
	 * Define a limit distance of 256 meters.
	 */
	@Override
	public boolean shouldRender(double distance)
	{
        return distance < 65536.0;
    }
	
	@Override
	protected void initDataTracker()
	{
		this.dataTracker.startTracking(INITIAL_BLOCK_POS, BlockPos.ORIGIN);
		this.dataTracker.startTracking(FORWARD, Integer.valueOf(0));
		this.dataTracker.startTracking(CRAFT_ROLL, Float.valueOf(0.0f));
		this.dataTracker.startTracking(CRAFT_PITCH, Float.valueOf(0.0f));
		this.dataTracker.startTracking(CRAFT_YAW, Float.valueOf(0.0f));
	}
	
    public void setInitialBlockPos(BlockPos pos)
    {
        this.dataTracker.set(INITIAL_BLOCK_POS, pos);
    }
    
    public void setForwardDirection(int d)
    {
    	this.dataTracker.set(FORWARD, d);
    }
    
    public void setCraftRoll(float f)
    {
        this.dataTracker.set(CRAFT_ROLL, Float.valueOf(craftRoll));
    }
    
    public void setCraftPitch(float f)
    {
        this.dataTracker.set(CRAFT_PITCH, Float.valueOf(craftPitch));
    }
    
    public void setCraftYaw(float f)
    {
        this.dataTracker.set(CRAFT_YAW, Float.valueOf(craftYaw));
    }

    public BlockPos getInitialBlockPos()
    {
        return this.dataTracker.get(INITIAL_BLOCK_POS);
    }
    
    public Direction getForwardDirection()
    {
    	return Direction.fromHorizontal(this.dataTracker.get(FORWARD));
    }

    public float getCraftRoll()
    {
        return this.dataTracker.get(CRAFT_ROLL).floatValue();
    }
    
    public float getCraftPitch()
    {
        return this.dataTracker.get(CRAFT_PITCH).floatValue();
    }

    public float getCraftYaw()
    {
        return this.dataTracker.get(CRAFT_YAW).floatValue();
    }
	
	public boolean clientMotion()
	{
		if(this.world.isClient)
		{
            if(this.clientInterpolationSteps > 0)
            {
                double a = this.getX() + (this.clientX - this.getX()) / (double) this.clientInterpolationSteps;
                double b = this.getY() + (this.clientY - this.getY()) / (double) this.clientInterpolationSteps;
                double c = this.getZ() + (this.clientZ - this.getZ()) / (double) this.clientInterpolationSteps;
                double d = MathHelper.wrapDegrees(this.clientYaw - (double) this.getYaw());
                this.setYaw(this.getYaw() + (float) d / (float) this.clientInterpolationSteps);
                this.setPitch(this.getPitch() + (float) (this.clientPitch - (double) this.getPitch()) / (float) this.clientInterpolationSteps);
                this.clientInterpolationSteps--;
                this.setPosition(a, b, c);
                this.setRotation(this.getYaw(), this.getPitch());
            }
            else
            {
                this.refreshPosition();
                this.setRotation(this.getYaw(), this.getPitch());
            }
            
            return true;
        }
		
		return false;
	}
	
	@Override
    public void updateTrackedPositionAndAngles(double x, double y, double z, float yaw, float pitch, int interpolationSteps, boolean interpolate)
	{
        this.clientX = x;
        this.clientY = y;
        this.clientZ = z;
        this.clientYaw = yaw;
        this.clientPitch = pitch;
        this.clientInterpolationSteps = interpolationSteps + 2;
        this.setVelocity(this.clientXVelocity, this.clientYVelocity, this.clientZVelocity);
    }

    @Override
    public void setVelocityClient(double x, double y, double z)
    {
        this.clientXVelocity = x;
        this.clientYVelocity = y;
        this.clientZVelocity = z;
        this.setVelocity(this.clientXVelocity, this.clientYVelocity, this.clientZVelocity);
    }
	
	@Override
    public void updatePassengerPosition(Entity passenger)
	{
        if(!this.entityOffsets.containsKey(passenger.getUuid()))
            return;
        
        BlockPos pos = entityOffsets.get(passenger.getUuid());
        Vec3f offset = new Vec3f(pos.getX(), pos.getY() - 0.5f, pos.getZ());
        float rotationRoll = getCraftRoll();
		float rotationPitch = getCraftPitch();
		float rotationYaw = getCraftYaw();

		switch(getForwardDirection())
		{
		case NORTH:
			offset.rotate(Vec3f.NEGATIVE_Z.getDegreesQuaternion(rotationRoll));
			offset.rotate(Vec3f.NEGATIVE_X.getDegreesQuaternion(rotationPitch));
			offset.rotate(Vec3f.NEGATIVE_Y.getDegreesQuaternion(rotationYaw));
			break;
		case EAST:
			offset.rotate(Vec3f.POSITIVE_X.getDegreesQuaternion(rotationRoll));
			offset.rotate(Vec3f.POSITIVE_Z.getDegreesQuaternion(rotationPitch));
			offset.rotate(Vec3f.POSITIVE_Y.getDegreesQuaternion(rotationYaw));
			break;
		case SOUTH:
			offset.rotate(Vec3f.POSITIVE_Z.getDegreesQuaternion(rotationRoll));
			offset.rotate(Vec3f.POSITIVE_X.getDegreesQuaternion(rotationPitch));
			offset.rotate(Vec3f.POSITIVE_Y.getDegreesQuaternion(rotationYaw));
			break;
		case WEST:
			offset.rotate(Vec3f.NEGATIVE_X.getDegreesQuaternion(rotationRoll));
			offset.rotate(Vec3f.NEGATIVE_Z.getDegreesQuaternion(rotationPitch));
			offset.rotate(Vec3f.NEGATIVE_Y.getDegreesQuaternion(rotationYaw));
			break;
		default:
			break;
		}

        passenger.setPosition(this.getX() + offset.getX(), this.getY() + offset.getY(), this.getZ() + offset.getZ());
        passenger.fallDistance = 0.0f;
	}
	
	public static ArrayList<MovingCraftBlockData> captureBlocks(World world, BlockPos centerPos, ArrayList<BlockPos> positionList)
	{
		ArrayList<MovingCraftBlockData> blockDataList = new ArrayList<MovingCraftBlockData>();
		
		// Fill the block data array list.
		for(BlockPos pos : positionList)
		{
			blockDataList.add(MovingCraftBlockData.fromBlock(world, pos, centerPos, isBlockSolid(world, pos)));	
			BlockEntity blockEntity = world.getBlockEntity(pos);
			
			if(blockEntity != null)
			{
				if(blockEntity instanceof ImplementedInventory)
					((ImplementedInventory) blockEntity).clear();
				
				blockEntity.readNbt(new NbtCompound());
			}
		}
		
		// Remove blocks from the world.
		for(BlockPos pos : positionList)
		{
			if(world.getBlockState(pos).getBlock() == StarflightBlocks.FLUID_TANK_INSIDE)
			{
				world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
				continue;
			}
			
			if(world.getBlockState(pos).getBlock() == StarflightBlocks.HABITABLE_AIR)
				world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
		}
		
		for(BlockPos pos : positionList)
		{
			if(!isBlockSolid(world, pos))
			{
				world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
				
				for(Entity item : world.getEntitiesByClass(ItemEntity.class, new Box(pos.add(-1, -1, -1), pos.add(1, 1, 1)), b -> true))
					item.remove(RemovalReason.DISCARDED);
			}
		}
		
		for(BlockPos pos : positionList)
		{
			if(isBlockSolid(world, pos))
				world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
		}
		
		return blockDataList;
	}
	
	private static boolean isBlockSolid(World world, BlockPos blockPos)
	{
		BlockState blockState = world.getBlockState(blockPos);
		return blockState.isSolidBlock(world, blockPos) || blockState.getBlock() instanceof SlabBlock || blockState.getBlock() instanceof StairsBlock;
	}
	
	public static void searchForBlocks(World world, BlockPos pos, ArrayList<BlockPos> positionList, int limit)
	{
		if(positionList.size() > limit || positionList.contains(pos) || !isBlockAllowed(world, pos))
			return;
		
		positionList.add(pos);
		
		if(world.getBlockState(pos).isIn(StarflightBlocks.NO_RECURSIVE_SEARCH_TAG))
			return;
		
		for(Direction direction : DIRECTIONS)
			searchForBlocks(world, pos.offset(direction), positionList, limit);
	}
	
	private static boolean isBlockAllowed(World world, BlockPos pos)
	{
		if(world.getBlockState(pos).getBlock() == Blocks.AIR)
			return false;
		else if(world.getBlockState(pos).isIn(StarflightBlocks.EXCLUDED_BLOCK_TAG))
			return false;
		else
			return true;
	}
	
	protected void releaseBlocks()
	{
		float deltaYaw = this.getYaw();
		int rotationSteps = (int) Math.round(deltaYaw / 90.0f);
		ArrayList<MovingCraftBlockData> toPlaceFirst = new ArrayList<MovingCraftBlockData>();
		ArrayList<MovingCraftBlockData> toPlaceLast = new ArrayList<MovingCraftBlockData>();
		
		// Snap the rotation of this entity into place and then dismount all passengers.
		this.setCraftRoll(0.0f);
		this.setCraftPitch(0.0f);
		this.setCraftYaw(rotationSteps * 90.0f);
		
		for(Entity passenger : this.getPassengerList())
		{
			this.updatePassengerPosition(passenger);
			passenger.stopRiding();
		}
		
		// Place all blocks back into the world.
		for(MovingCraftBlockData blockData : blockDataList)
		{
			if(blockData.placeFirst())
				toPlaceFirst.add(blockData);
			else
				toPlaceLast.add(blockData);
		}
		
		for(MovingCraftBlockData blockData : toPlaceFirst)
			blockData.toBlock(this.getWorld(), this.getBlockPos(), rotationSteps);
		
		for(MovingCraftBlockData blockData : toPlaceLast)
			blockData.toBlock(this.getWorld(), this.getBlockPos(), rotationSteps);
		
		for(MovingCraftBlockData blockData : toPlaceFirst)
		{
			if(blockData.getStoredFluid() > 0)
			{
				BlockRotation rotation = BlockRotation.NONE;
				
				for(int i = 0; i < rotationSteps; i++)
					rotation = rotation.rotate(BlockRotation.CLOCKWISE_90);
				
				BlockPos blockPos = this.getBlockPos().add(blockData.getPosition().rotate(rotation));
				BlockEntity blockEntity = world.getBlockEntity(blockPos);
				
				if(blockEntity != null && blockEntity instanceof FluidTankControllerBlockEntity)
				{
					FluidTankControllerBlockEntity fluidTank = (FluidTankControllerBlockEntity) blockEntity;
					
					if(blockData.getBlockState().getBlock() instanceof FluidTankControllerBlock)
						((FluidTankControllerBlock) blockData.getBlockState().getBlock()).initializeFluidTank(world, blockPos, fluidTank);
					
					fluidTank.setStoredFluid(blockData.getStoredFluid());
				}
			}
		}
		
		this.setRemoved(RemovalReason.DISCARDED);
	}
	
	public void pickUpEntity(Entity passenger)
	{
		pickUpEntity(passenger, new BlockPos(Math.floor(passenger.getX()), Math.floor(passenger.getY()), Math.floor(passenger.getZ())).subtract(getInitialBlockPos()));
	}
	
	public void pickUpEntity(Entity passenger, BlockPos offset)
	{
		if(passenger.hasVehicle())
			return;
		
		if(this.getPassengerList().isEmpty())
			((EntityMixin) this).setPassengerList(ImmutableList.of(passenger));
		else
		{
			 ArrayList<Entity> list = Lists.newArrayList(this.getPassengerList());
			 list.add(passenger);
			 ((EntityMixin) this).setPassengerList(ImmutableList.copyOf(list));
		}
		
		((EntityMixin) passenger).setVehicle(this);
		entityOffsets.put(passenger.getUuid(), offset);
	}

	public void changeDimension(ServerWorld destination, Vec3d arrivalLocation, float arrivalYaw)
	{
		if(!(this.world instanceof ServerWorld) || this.isRemoved())
			return;

		ArrayList<Entity> passengerList = Lists.newArrayList(this.getPassengerList());
		ArrayList<BlockPos> offsetList = new ArrayList<BlockPos>();
		
		for(Entity entity : passengerList)
			offsetList.add(entityOffsets.get(entity.getUuid()));
		
		this.detach();
		TeleportTarget movingCraftTarget = new TeleportTarget(arrivalLocation, this.getVelocity(), arrivalYaw, this.getPitch());
		Entity movingCraft = FabricDimensions.teleport(this, destination, movingCraftTarget);
		
		if(movingCraft != null)
		{
			((MovingCraftEntity) movingCraft).entityOffsets.clear();
			
			for(int i = 0; i < passengerList.size(); i++)
			{
				Entity passenger = passengerList.get(i);
				TeleportTarget passengerTarget = new TeleportTarget(passenger.getPos(), this.getVelocity(), passenger.getYaw(), passenger.getPitch());
				Entity trasferred = FabricDimensions.teleport(passenger, destination, passengerTarget);

				if(trasferred != null)
					((MovingCraftEntity) movingCraft).pickUpEntity(trasferred, offsetList.get(i));
			}
			
			if(movingCraft instanceof RocketEntity)
				((RocketEntity) movingCraft).storeGravity();
		}
	}

	@Override
	protected void writeCustomDataToNbt(NbtCompound nbt)
	{
		nbt.put("initialBlockPos", NbtHelper.fromBlockPos(getInitialBlockPos()));
		nbt.putInt("forward", getForwardDirection().getHorizontal());
		nbt.putFloat("roll", craftRoll);
		nbt.putFloat("pitch", craftPitch);
		nbt.putFloat("yaw", craftYaw);
		int blockCount = blockDataList.size();
		nbt.putInt("blockCount", blockCount);
		int[] x = new int[blockCount];
		int[] y = new int[blockCount];
		int[] z = new int[blockCount];
		
		for(int i = 0; i < blockCount; i++)
		{
			MovingCraftBlockData blockData = blockDataList.get(i);
			x[i] = blockData.getPosition().getX();
			y[i] = blockData.getPosition().getY();
			z[i] = blockData.getPosition().getZ();
			blockData.saveData(nbt);
		}
		
		nbt.putIntArray("x", x);
		nbt.putIntArray("y", y);
		nbt.putIntArray("z", z);
		
		nbt.putInt("passengerCount", entityOffsets.size());
		int i = 0;
		
		for(UUID pUUID : entityOffsets.keySet())
		{
			BlockPos pos = entityOffsets.get(pUUID);
			nbt.putUuid("pUUID" + i, pUUID);
			nbt.putInt("px" + i, pos.getX());
			nbt.putInt("py" + i, pos.getY());
			nbt.putInt("pz" + i, pos.getZ());
			i++;
		}
	}

	@Override
	protected void readCustomDataFromNbt(NbtCompound nbt)
	{
		setInitialBlockPos(NbtHelper.toBlockPos(nbt.getCompound("initialBlockPos")));
		setForwardDirection(nbt.getInt("forward"));
		craftRoll = nbt.getFloat("roll");
		craftPitch = nbt.getFloat("pitch");
		craftYaw = nbt.getFloat("yaw");
		int blockCount = nbt.getInt("blockCount");
		int[] x = nbt.getIntArray("x");
		int[] y = nbt.getIntArray("y");
		int[] z = nbt.getIntArray("z");
		
		for(int i = 0; i < blockCount; i++)
		{
			BlockPos dataPos = new BlockPos(x[i], y[i], z[i]);
			blockDataList.add(MovingCraftBlockData.loadData(nbt.getCompound(dataPos.toShortString())));
		}
		
		int passengerCount = nbt.getInt("passengerCount");
		
		for(int i = 0; i < passengerCount; i++)
			entityOffsets.put(nbt.getUuid("pUUID" + i), new BlockPos(nbt.getInt("px" + i), nbt.getInt("py" + i), nbt.getInt("pz" + i)));
	}

	@Override
	public Packet<?> createSpawnPacket()
	{
		 return new EntitySpawnS2CPacket(this);
	}
	
	public void sendRenderData(boolean forceUnload)
	{
		shouldRender(clientInterpolationSteps);
		MinecraftServer server = this.getServer();
		
		for(ServerPlayerEntity player : server.getPlayerManager().getPlayerList())
		{
			boolean inRange = this.getBlockPos().isWithinDistance(player.getBlockPos(), 1024);
			
			if(!forceUnload && !this.playersInRange.contains(player) && inRange)
			{
				PacketByteBuf buffer = PacketByteBufs.create();
				this.playersInRange.add(player);
				buffer.writeBoolean(true);
				buffer.writeUuid(this.getUuid());
				buffer.writeInt(this.blockDataList.size());
				
				for(MovingCraftBlockData data : this.blockDataList)
				{
					buffer.writeNbt(NbtHelper.fromBlockState(data.getBlockState()));
					buffer.writeBlockPos(data.getPosition());
					
					for(int i = 0; i < 6; i++)
						buffer.writeBoolean(data.getSidesShowing()[i]);
				}
				
				ServerPlayNetworking.send(player, new Identifier(StarflightMod.MOD_ID, "moving_craft_render_data"), buffer);
			}
			else if(forceUnload && this.playersInRange.contains(player) && !inRange)
			{	
				PacketByteBuf buffer = PacketByteBufs.create();
				this.playersInRange.remove(player);
				buffer.writeBoolean(false);
				buffer.writeUuid(this.getUuid());
				ServerPlayNetworking.send(player, new Identifier(StarflightMod.MOD_ID, "moving_craft_render_data"), buffer);
			}
			else if(this.age < 8 && !forceUnload && inRange)
				sendEntityOffsets(player);
		}
	}
	
	public void sendEntityOffsets(ServerPlayerEntity player)
	{
		PacketByteBuf buffer = PacketByteBufs.create();
		buffer.writeInt(getId());
		int passengerCount = entityOffsets.keySet().size();
		buffer.writeInt(passengerCount);
		
		for(UUID pUUID : entityOffsets.keySet())
		{
			buffer.writeUuid(pUUID);
			buffer.writeBlockPos(entityOffsets.get(pUUID));
		}
		
		ServerPlayNetworking.send(player, new Identifier(StarflightMod.MOD_ID, "moving_craft_entity_offsets"), buffer);
	}
	
	public static void receiveEntityOffsets(ClientPlayNetworkHandler handler, PacketSender sender, MinecraftClient client, PacketByteBuf buffer)
	{
		if(client.world == null)
			return;
		
		int entityID = buffer.readInt();
		Entity entity = client.world.getEntityById(entityID);
		
		if(entity == null || !(entity instanceof MovingCraftEntity))
			return;
		
		MovingCraftEntity movingCraft = (MovingCraftEntity) entity;
		int passengerCount = buffer.readInt();
		movingCraft.entityOffsets.clear();
		
		for(int i = 0; i < passengerCount; i++)
		{
			UUID pUUID = buffer.readUuid();
			BlockPos pos = buffer.readBlockPos();
			movingCraft.entityOffsets.put(pUUID, pos);
		}
	}
}