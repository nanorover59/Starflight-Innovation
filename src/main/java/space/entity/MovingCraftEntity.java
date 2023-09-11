package space.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.joml.Quaternionf;
import org.joml.Vector3f;

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
import net.minecraft.block.Waterloggable;
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
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import space.StarflightMod;
import space.block.StarflightBlocks;
import space.inventory.ImplementedInventory;
import space.mixin.common.EntityMixin;
import space.vessel.MovingCraftBlockData;

public class MovingCraftEntity extends Entity
{
	private static final TrackedData<BlockPos> INITIAL_BLOCK_POS = DataTracker.registerData(MovingCraftEntity.class, TrackedDataHandlerRegistry.BLOCK_POS);
	private static final TrackedData<Integer> FORWARD = DataTracker.registerData(MovingCraftEntity.class, TrackedDataHandlerRegistry.INTEGER);
	private static final TrackedData<Float> CRAFT_QX = DataTracker.registerData(MovingCraftEntity.class, TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Float> CRAFT_QY = DataTracker.registerData(MovingCraftEntity.class, TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Float> CRAFT_QZ = DataTracker.registerData(MovingCraftEntity.class, TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Float> CRAFT_QW = DataTracker.registerData(MovingCraftEntity.class, TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Float> TRACKED_VX = DataTracker.registerData(MovingCraftEntity.class, TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Float> TRACKED_VY = DataTracker.registerData(MovingCraftEntity.class, TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Float> TRACKED_VZ = DataTracker.registerData(MovingCraftEntity.class, TrackedDataHandlerRegistry.FLOAT);
	protected ArrayList<MovingCraftBlockData> blockDataList = new ArrayList<MovingCraftBlockData>();
	protected ArrayList<ServerPlayerEntity> playersInRange = new ArrayList<ServerPlayerEntity>();
	protected HashMap<UUID, BlockPos> entityOffsets = new HashMap<UUID, BlockPos>();
	protected BlockPos centerOfMass;
	public Quaternionf clientQuaternion;
	public Quaternionf clientQuaternionPrevious;
	public int clientInterpolationSteps;
	private float craftRoll;
	private float craftPitch;
	private float craftYaw;
	private double clientX;
	private double clientY;
	private double clientZ;
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

		if(!blockDataList.isEmpty())
		{
			for(MovingCraftBlockData blockData : blockDataList)
				this.blockDataList.add(blockData);
		} else
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

	@Override
	public boolean shouldRender(double distance)
	{
		return true;
	}

	@Override
	protected void initDataTracker()
	{
		this.dataTracker.startTracking(INITIAL_BLOCK_POS, BlockPos.ORIGIN);
		this.dataTracker.startTracking(FORWARD, Integer.valueOf(0));
		this.dataTracker.startTracking(CRAFT_QX, Float.valueOf(0.0f));
		this.dataTracker.startTracking(CRAFT_QY, Float.valueOf(0.0f));
		this.dataTracker.startTracking(CRAFT_QZ, Float.valueOf(0.0f));
		this.dataTracker.startTracking(CRAFT_QW, Float.valueOf(0.0f));
		this.dataTracker.startTracking(TRACKED_VX, Float.valueOf(0.0f));
		this.dataTracker.startTracking(TRACKED_VY, Float.valueOf(0.0f));
		this.dataTracker.startTracking(TRACKED_VZ, Float.valueOf(0.0f));
	}

	public void setInitialBlockPos(BlockPos pos)
	{
		this.dataTracker.set(INITIAL_BLOCK_POS, pos);
	}

	public void setForwardDirection(int d)
	{
		this.dataTracker.set(FORWARD, d);
	}

	public void updateEulerAngles()
	{
		Vector3f angles = new Vector3f();
		getCraftQuaternion().getEulerAnglesXYZ(angles);
		this.craftPitch = angles.x();
		this.craftYaw = angles.y();
		this.craftRoll = angles.z();
	}

	public void setQuaternion(float x, float y, float z, float w)
	{
		this.dataTracker.set(CRAFT_QX, x);
		this.dataTracker.set(CRAFT_QY, y);
		this.dataTracker.set(CRAFT_QZ, z);
		this.dataTracker.set(CRAFT_QW, w);
	}

	public void setQuaternion(Quaternionf quaternion)
	{
		this.dataTracker.set(CRAFT_QX, quaternion.x());
		this.dataTracker.set(CRAFT_QY, quaternion.y());
		this.dataTracker.set(CRAFT_QZ, quaternion.z());
		this.dataTracker.set(CRAFT_QW, quaternion.w());
	}

	public void setTrackedVelocity(Vector3f velocity)
	{
		this.dataTracker.set(TRACKED_VX, velocity.x());
		this.dataTracker.set(TRACKED_VY, velocity.y());
		this.dataTracker.set(TRACKED_VZ, velocity.z());
	}

	public BlockPos getInitialBlockPos()
	{
		return this.dataTracker.get(INITIAL_BLOCK_POS);
	}

	public Direction getForwardDirection()
	{
		return Direction.fromHorizontal(this.dataTracker.get(FORWARD));
	}

	public Quaternionf getCraftQuaternion()
	{
		return new Quaternionf(this.dataTracker.get(CRAFT_QX).floatValue(), this.dataTracker.get(CRAFT_QY).floatValue(), this.dataTracker.get(CRAFT_QZ).floatValue(), this.dataTracker.get(CRAFT_QW).floatValue());
	}

	public float getCraftRoll()
	{
		return this.craftRoll;
	}

	public float getCraftPitch()
	{
		return this.craftPitch;
	}

	public float getCraftYaw()
	{
		return this.craftYaw;
	}

	public Vector3f getTrackedVelocity()
	{
		return new Vector3f(this.dataTracker.get(TRACKED_VX).floatValue(), this.dataTracker.get(TRACKED_VY).floatValue(), this.dataTracker.get(TRACKED_VZ).floatValue());
	}

	/**
	 * Return the number of horizontal rotation steps for rotation about the y-axis.
	 * Used for converting back to blocks.
	 */
	public int getRotationSteps()
	{
		double yaw = getCraftYaw();

		if(yaw < Math.PI * -0.25 && yaw >= Math.PI * -0.75)
			return 1;
		else if(yaw > Math.PI * 0.75 || yaw < Math.PI * -0.75)
			return 2;
		else if(yaw > Math.PI * 0.25 && yaw <= Math.PI * 0.75)
			return 3;
		else
			return 0;
	}

	public boolean clientMotion()
	{
		World world = getWorld();

		if(world.isClient)
		{
			if(this.clientInterpolationSteps > 0)
			{
				double a = this.getX() + (this.clientX - this.getX()) / (double) this.clientInterpolationSteps;
				double b = this.getY() + (this.clientY - this.getY()) / (double) this.clientInterpolationSteps;
				double c = this.getZ() + (this.clientZ - this.getZ()) / (double) this.clientInterpolationSteps;
				this.clientInterpolationSteps--;
				this.setPosition(a, b, c);
			} else
				this.refreshPosition();

			return true;
		}

		return false;
	}

	@Override
	public void updateTrackedPositionAndAngles(double x, double y, double z, float yaw, float pitch, int interpolationSteps, boolean interpolate)
	{
		this.clientInterpolationSteps = interpolationSteps + 2;
		this.clientX = x;
		this.clientY = y;
		this.clientZ = z;
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
	protected void updatePassengerPosition(Entity passenger, PositionUpdater positionUpdater)
	{
		if(!this.hasPassenger(passenger) || !this.entityOffsets.containsKey(passenger.getUuid()))
			return;
		
		BlockPos pos = entityOffsets.get(passenger.getUuid());
		Quaternionf quaternion = getCraftQuaternion();
		Vector3f offset = new Vector3f(pos.getX(), pos.getY() - 0.5f, pos.getZ());
		offset.rotate(quaternion);
		passenger.fallDistance = 0.0f;
		positionUpdater.accept(passenger, this.getX() + offset.x(), this.getY() + offset.y(), this.getZ() + offset.z());
	}

	public static ArrayList<MovingCraftBlockData> captureBlocks(World world, BlockPos centerPos, ArrayList<BlockPos> positionList)
	{
		ArrayList<MovingCraftBlockData> blockDataList = new ArrayList<MovingCraftBlockData>();

		// Fill the block data array list.
		for(BlockPos pos : positionList)
		{
			if(world.getBlockState(pos).getBlock() instanceof Waterloggable)
				world.setBlockState(pos, world.getBlockState(pos).with(Properties.WATERLOGGED, false));

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

	public void pickUpEntity(Entity passenger)
	{
		pickUpEntity(passenger, new BlockPos((int) Math.floor(passenger.getX()), (int) Math.floor(passenger.getY()), (int) Math.floor(passenger.getZ())).subtract(getInitialBlockPos()));
	}

	public void pickUpEntity(Entity passenger, BlockPos offset)
	{
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

	/**
	 * Change the world this entity and all of its passengers are located in.
	 */
	public void changeDimension(ServerWorld destination, Vec3d arrivalLocation, float arrivalYaw)
	{
		World world = getWorld();

		if(!(world instanceof ServerWorld) || this.isRemoved())
			return;

		this.setQuaternion(new Quaternionf().fromAxisAngleRad(0.0f, 1.0f, 0.0f, arrivalYaw));
		this.setPosition(arrivalLocation);

		ArrayList<Entity> passengerList = Lists.newArrayList(this.getPassengerList());
		ArrayList<BlockPos> offsetList = new ArrayList<BlockPos>();

		for(Entity entity : passengerList)
		{
			updatePassengerPosition(entity);
			offsetList.add(entityOffsets.get(entity.getUuid()));
		}

		this.detach();
		TeleportTarget movingCraftTarget = new TeleportTarget(arrivalLocation, this.getVelocity(), this.getYaw(), this.getPitch());
		Entity movingCraft = FabricDimensions.teleport(this, destination, movingCraftTarget);

		if(movingCraft != null)
		{
			((MovingCraftEntity) movingCraft).onDimensionChanged(destination, arrivalLocation, arrivalYaw);
			((MovingCraftEntity) movingCraft).entityOffsets.clear();

			for(int i = 0; i < passengerList.size(); i++)
			{
				Entity passenger = passengerList.get(i);
				TeleportTarget passengerTarget = new TeleportTarget(passenger.getPos(), this.getVelocity(), passenger.getYaw(), passenger.getPitch());
				Entity trasferred = FabricDimensions.teleport(passenger, destination, passengerTarget);

				if(trasferred != null)
					((MovingCraftEntity) movingCraft).pickUpEntity(trasferred, offsetList.get(i));
			}
		}
	}
	
	public void onDimensionChanged(ServerWorld destination, Vec3d arrivalLocation, float arrivalYaw)
	{
	}
	
	public void releaseBlocks()
	{
		// Snap the rotation of this entity into place and then dismount all passengers.
		updateEulerAngles();
		int rotationSteps = getRotationSteps();
		
		ArrayList<MovingCraftBlockData> toPlaceFirst = new ArrayList<MovingCraftBlockData>();
		ArrayList<MovingCraftBlockData> toPlaceLast = new ArrayList<MovingCraftBlockData>();
		BlockRotation rotation = BlockRotation.NONE;
		
		for(int i = 0; i < rotationSteps; i++)
			rotation = rotation.rotate(BlockRotation.CLOCKWISE_90);
		
		setQuaternion(new Quaternionf().fromAxisAngleRad(0.0f, 1.0f, 0.0f, (float) (rotationSteps * (Math.PI / 2.0))));
		fallDistance = 0.0f;
		
		for(Entity passenger : this.getPassengerList())
		{
			updatePassengerPosition(passenger);
			passenger.setPosition(passenger.getPos().add(0.0, 0.5, 0.0));
			passenger.setPosition(passenger.getPos().add(0.0, getVelocity().getY(), 0.0));
			passenger.setVelocity(Vec3d.ZERO);
			passenger.velocityModified = true;
		}
		
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
			onBlockReleased(blockData, rotation);
		
		this.setRemoved(RemovalReason.DISCARDED);
	}
	
	public void onBlockReleased(MovingCraftBlockData blockData, BlockRotation rotation)
	{
	}
	
	@Override
	protected void writeCustomDataToNbt(NbtCompound nbt)
	{
		nbt.put("initialBlockPos", NbtHelper.fromBlockPos(getInitialBlockPos()));
		nbt.putInt("forward", getForwardDirection().getHorizontal());
		nbt.putFloat("qx", this.dataTracker.get(CRAFT_QX).floatValue());
		nbt.putFloat("qy", this.dataTracker.get(CRAFT_QY).floatValue());
		nbt.putFloat("qz", this.dataTracker.get(CRAFT_QZ).floatValue());
		nbt.putFloat("qw", this.dataTracker.get(CRAFT_QW).floatValue());
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

			if(pos != null)
			{
				nbt.putUuid("pUUID" + i, pUUID);
				nbt.putInt("px" + i, pos.getX());
				nbt.putInt("py" + i, pos.getY());
				nbt.putInt("pz" + i, pos.getZ());
			}

			i++;
		}
	}

	@Override
	protected void readCustomDataFromNbt(NbtCompound nbt)
	{
		setInitialBlockPos(NbtHelper.toBlockPos(nbt.getCompound("initialBlockPos")));
		setForwardDirection(nbt.getInt("forward"));
		setQuaternion(nbt.getFloat("qx"), nbt.getFloat("qy"), nbt.getFloat("qz"), nbt.getFloat("qw"));
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
	public Packet<ClientPlayPacketListener> createSpawnPacket()
	{
		return new EntitySpawnS2CPacket(this);
	}

	public void sendRenderData(boolean forceUnload)
	{
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
					buffer.writeBoolean(data.redstonePower());

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