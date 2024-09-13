package space.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.joml.Matrix3f;
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
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import space.StarflightMod;
import space.block.StarflightBlocks;
import space.inventory.ImplementedInventory;
import space.mixin.common.EntityInvokerMixin;
import space.vessel.MovingCraftBlockData;

public class MovingCraftEntity extends Entity
{
	private static final TrackedData<BlockPos> INITIAL_BLOCK_POS = DataTracker.registerData(MovingCraftEntity.class, TrackedDataHandlerRegistry.BLOCK_POS);
	private static final TrackedData<Integer> FORWARD = DataTracker.registerData(MovingCraftEntity.class, TrackedDataHandlerRegistry.INTEGER);
	private static final TrackedData<Quaternionf> CRAFT_QUATERNION = DataTracker.registerData(MovingCraftEntity.class, TrackedDataHandlerRegistry.QUATERNIONF);
	private static final TrackedData<Vector3f> TRACKED_VELOCITY = DataTracker.registerData(MovingCraftEntity.class, TrackedDataHandlerRegistry.VECTOR3F);
	private static final TrackedData<Vector3f> TRACKED_ANGULAR_VELOCITY = DataTracker.registerData(MovingCraftEntity.class, TrackedDataHandlerRegistry.VECTOR3F);
	protected ArrayList<MovingCraftBlockData> blockDataList = new ArrayList<MovingCraftBlockData>();
	protected ArrayList<ServerPlayerEntity> playersInRange = new ArrayList<ServerPlayerEntity>();
	protected HashMap<UUID, BlockPos> entityOffsets = new HashMap<UUID, BlockPos>();
	private Vector3f momentOfInertia1;
	private Vector3f momentOfInertia2;
	private Vector3f angularVelocity;
	public Quaternionf clientQuaternion;
	public Quaternionf clientQuaternionPrevious;
	public int clientInterpolationSteps;
	private double clientX;
	private double clientY;
	private double clientZ;
	private double clientXVelocity;
	private double clientYVelocity;
	private double clientZVelocity;
	private double craftMass;
	private double craftMassInitial;

	public MovingCraftEntity(EntityType<? extends MovingCraftEntity> entityType, World world)
	{
		super(entityType, world);
	}

	public MovingCraftEntity(EntityType<? extends MovingCraftEntity> type, World world, BlockPos blockPos, ArrayList<MovingCraftBlockData> blockDataList, double mass, Vector3f momentOfInertia1, Vector3f momentOfInertia2)
	{
		this(type, world);
		Vec3d position = blockPos.toCenterPos();
		this.blockDataList = blockDataList;
		this.craftMass = mass;
		this.craftMassInitial = mass;
		this.momentOfInertia1 = momentOfInertia1;
		this.momentOfInertia2 = momentOfInertia2;
		this.angularVelocity = new Vector3f();
		this.setPosition(position.getX() + 0.5, position.getY(), position.getZ() + 0.5);
		this.setRotation(0.0f, 0.0f);
		this.setQuaternion(new Quaternionf());
		this.setInitialBlockPos(this.getBlockPos());
		
		if(blockDataList.isEmpty())
			setRemoved(RemovalReason.DISCARDED);
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
		this.dataTracker.startTracking(CRAFT_QUATERNION, new Quaternionf());
		this.dataTracker.startTracking(TRACKED_VELOCITY, new Vector3f());
		this.dataTracker.startTracking(TRACKED_ANGULAR_VELOCITY, new Vector3f());
	}

	public void setInitialBlockPos(BlockPos pos)
	{
		this.dataTracker.set(INITIAL_BLOCK_POS, pos);
	}

	public void setForwardDirection(int d)
	{
		this.dataTracker.set(FORWARD, d);
	}
	
	public void setQuaternion(Quaternionf quaternion)
	{
		this.dataTracker.set(CRAFT_QUATERNION, quaternion);
	}

	public void setTrackedVelocity(Vector3f velocity)
	{
		this.dataTracker.set(TRACKED_VELOCITY, velocity);
	}
	
	public void setTrackedAngularVelocity(Vector3f angularVelocity)
	{
		this.dataTracker.set(TRACKED_ANGULAR_VELOCITY, angularVelocity);
	}

	public BlockPos getInitialBlockPos()
	{
		return this.dataTracker.get(INITIAL_BLOCK_POS);
	}

	public Direction getForwardDirection()
	{
		return Direction.fromHorizontal(this.dataTracker.get(FORWARD));
	}

	public Quaternionf getQuaternion()
	{
		return this.dataTracker.get(CRAFT_QUATERNION);
	}

	public Vector3f getTrackedVelocity()
	{
		return this.dataTracker.get(TRACKED_VELOCITY);
	}
	
	public Vector3f getTrackedAngularVelocity()
	{
		return this.dataTracker.get(TRACKED_ANGULAR_VELOCITY);
	}
	
	public void setMass(double craftMass)
	{
		this.craftMass = craftMass;
	}
	
	public void setInitialMass(double craftMass)
	{
		this.craftMassInitial = craftMass;
	}
	
	public void changeMass(double delta)
	{
		this.craftMass += delta;
	}
	
	public double getMass()
	{
		return craftMass;
	}
	
	public double getInitialMass()
	{
		return craftMassInitial;
	}
	
	public float getIXX()
	{
		return momentOfInertia1.x();
	}
	
	public float getIYY()
	{
		return momentOfInertia1.y();
	}
	
	public float getIZZ()
	{
		return momentOfInertia1.z();
	}
	
	public Matrix3f getMomentOfInertiaTensor()
	{
		Matrix3f tensor = new Matrix3f(momentOfInertia1.x(), momentOfInertia2.x(), momentOfInertia2.y(),
									   momentOfInertia2.x(), momentOfInertia1.y(), momentOfInertia2.z(),
									   momentOfInertia2.y(), momentOfInertia2.z(), momentOfInertia1.z());
		//Matrix3f rotation = new Matrix3f().rotation(getQuaternion());
		//tensor.mulLocal(rotation).mul(rotation.transpose());
		return tensor;
	}
	
	/**
	 * Apply a force in Newtons at a position relative to the center of mass.
	 */
	public void forceAtPosition(Vector3f force, Vector3f position, Vector3f netForce, Vector3f netMoment)
	{
		netForce.add(force);
		
		if(position.length() == 0.0f)
			return;
		
		netMoment.add(force.cross(position));
	}
	
	/**
	 * Apply a force to change the velocity.
	 */
	public void applyForce(Vector3f force)
	{
		addVelocity((force.x() / getMass()) * 0.0025, (force.y() / getMass()) * 0.0025, (force.z() / getMass()) * 0.0025);
	}
	
	/**
	 * Apply a moment in Newton meters about the local X, Y, and Z axes of the craft.
	 */
	public void applyMomentXYZ(Vector3f moment)
	{
		Matrix3f mit = getMomentOfInertiaTensor();
		// Get the angular velocity converted back to rad/s.
		Vector3f av = new Vector3f(angularVelocity).mul(20.0f);
		// Calculate the angular acceleration from Euler's rotation equation.
		Vector3f acc = moment.sub(av.cross(av.mul(mit))).mul(mit.invert());
		// Convert the angular acceleration to rad/tick^2 and add it to the angular velocity.
		angularVelocity.add(acc.mul(0.0025f));
	}
	
	public Vector3f getAngularVelocity()
	{
		return angularVelocity;
	}

	public boolean clientMotion()
	{
		World world = method_48926();

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
	public void updateTrackedPositionAndAngles(double x, double y, double z, float yaw, float pitch, int interpolationSteps)
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
		
		if(entityOffsets.get(passenger.getUuid()) != null)
		{
			Vector3f offset = entityOffsets.get(passenger.getUuid()).toCenterPos().add(-0.5, -1.0, -0.5).toVector3f();
			Quaternionf quaternion = getQuaternion();
			offset.rotate(quaternion);
			passenger.fallDistance = 0.0f;
			positionUpdater.accept(passenger, this.getX() + offset.x(), this.getY() + offset.y(), this.getZ() + offset.z());
		}
	}

	public static ArrayList<MovingCraftBlockData> captureBlocks(World world, BlockPos centerPos, ArrayList<BlockPos> positionList)
	{
		ArrayList<MovingCraftBlockData> blockDataList = new ArrayList<MovingCraftBlockData>();
		
		// Fill the block data array list.
		for(BlockPos pos : positionList)
			blockDataList.add(MovingCraftBlockData.fromBlock(world, pos, centerPos, isBlockSolid(world, pos)));

		return blockDataList;
	}
	
	public static void removeBlocksFromWorld(World world, BlockPos centerPos, ArrayList<MovingCraftBlockData> blockDataList)
	{
		for(MovingCraftBlockData blockData : blockDataList)
		{
			BlockPos pos = centerPos.add(blockData.getPosition());
			Block block = world.getBlockState(pos).getBlock();
			
			if(block == StarflightBlocks.FLUID_TANK_INSIDE || block == StarflightBlocks.HABITABLE_AIR)
			{
				world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.FORCE_STATE);
				continue;
			}
			
			BlockEntity blockEntity = world.getBlockEntity(pos);

			if(blockEntity != null)
			{
				if(blockEntity instanceof ImplementedInventory)
					((ImplementedInventory) blockEntity).clear();

				blockEntity.readNbt(new NbtCompound());
			}
		}
		
		for(MovingCraftBlockData blockData : blockDataList)
		{
			BlockPos pos = centerPos.add(blockData.getPosition());
			
			if(!isBlockSolid(world, pos))
			{
				world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);

				for(Entity item : world.getEntitiesByClass(ItemEntity.class, new Box(pos.add(-1, -1, -1), pos.add(1, 1, 1)), b -> true))
					item.remove(RemovalReason.DISCARDED);
			}
		}

		for(MovingCraftBlockData blockData : blockDataList)
		{
			BlockPos pos = centerPos.add(blockData.getPosition());
			
			if(isBlockSolid(world, pos))
				world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
		}
	}

	private static boolean isBlockSolid(World world, BlockPos blockPos)
	{
		BlockState blockState = world.getBlockState(blockPos);
		return blockState.isSolidBlock(world, blockPos) || blockState.getBlock() instanceof SlabBlock || blockState.getBlock() instanceof StairsBlock;
	}
	
	public void pickUpEntity(Entity passenger, BlockPos offset)
	{
		if(this.getPassengerList().isEmpty())
			((EntityInvokerMixin) this).setPassengerList(ImmutableList.of(passenger));
		else
		{
			ArrayList<Entity> list = Lists.newArrayList(this.getPassengerList());
			list.add(passenger);
			((EntityInvokerMixin) this).setPassengerList(ImmutableList.copyOf(list));
		}

		((EntityInvokerMixin) passenger).setVehicle(this);
		entityOffsets.put(passenger.getUuid(), offset);
	}

	/**
	 * Change the world this entity and all of its passengers are located in.
	 */
	public void changeDimension(ServerWorld destination, Vec3d arrivalLocation, float arrivalYaw)
	{
		World world = method_48926();

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
		Quaternionf quaternion = getQuaternion();
		Vector3f forward = getForwardDirection().getUnitVector().rotate(quaternion);
		Direction facing = Direction.getFacing(forward.x(), forward.y(), forward.z());
		
		if(facing != Direction.UP && facing != Direction.DOWN)
			quaternion.rotationY((getForwardDirection().asRotation() - facing.asRotation()) * (MathHelper.PI / 180.0f));
		
		ArrayList<MovingCraftBlockData> toPlaceFirst = new ArrayList<MovingCraftBlockData>();
		ArrayList<MovingCraftBlockData> toPlaceLast = new ArrayList<MovingCraftBlockData>();
		fallDistance = 0.0f;
		
		for(Entity passenger : this.getPassengerList())
		{
			UUID pUUID = passenger.getUuid();
			
			if(entityOffsets.containsKey(pUUID))
			{
				if(entityOffsets.get(passenger.getUuid()) != null)
				{
					Vector3f offset = entityOffsets.get(passenger.getUuid()).toCenterPos().add(-0.5, 0.0, -0.5).toVector3f();
					offset.rotate(quaternion);
					passenger.setPosition(this.getX() + offset.x(), this.getY() + offset.y(), this.getZ() + offset.z());
				}
			}
			
			passenger.setVelocity(Vec3d.ZERO);
			passenger.velocityModified = true;
			passenger.dismountVehicle();
		}
		
		for(MovingCraftBlockData blockData : blockDataList)
		{
			if(blockData.placeFirst())
				toPlaceFirst.add(blockData);
			else
				toPlaceLast.add(blockData);
		}
		
		for(MovingCraftBlockData blockData : toPlaceFirst)
			blockData.toBlock(this.method_48926(), this.getBlockPos(), quaternion);
		
		for(MovingCraftBlockData blockData : toPlaceLast)
			blockData.toBlock(this.method_48926(), this.getBlockPos(), quaternion);
		
		for(MovingCraftBlockData blockData : toPlaceFirst)
		{
			Vector3f offset = new Vector3f(blockData.getPosition().getX(), blockData.getPosition().getY(), blockData.getPosition().getZ()).rotate(quaternion);
			BlockPos blockPos = this.getBlockPos().add(MathHelper.floor(offset.x()), MathHelper.floor(offset.y()), MathHelper.floor(offset.z()));
			onBlockReleased(blockData, blockPos);
		}
		
		this.setRemoved(RemovalReason.DISCARDED);
	}
	
	public void onBlockReleased(MovingCraftBlockData blockData, BlockPos worldPos)
	{
	}
	
	@Override
	protected void writeCustomDataToNbt(NbtCompound nbt)
	{
		nbt.put("initialBlockPos", NbtHelper.fromBlockPos(getInitialBlockPos()));
		nbt.putInt("forward", getForwardDirection().getHorizontal());
		nbt.putFloat("qx", this.dataTracker.get(CRAFT_QUATERNION).x());
		nbt.putFloat("qy", this.dataTracker.get(CRAFT_QUATERNION).y());
		nbt.putFloat("qz", this.dataTracker.get(CRAFT_QUATERNION).z());
		nbt.putFloat("qw", this.dataTracker.get(CRAFT_QUATERNION).w());
		nbt.putFloat("avx", angularVelocity.x());
		nbt.putFloat("avy", angularVelocity.y());
		nbt.putFloat("avz", angularVelocity.z());
		nbt.putFloat("mix1", momentOfInertia1.x());
		nbt.putFloat("miy1", momentOfInertia1.y());
		nbt.putFloat("miz1", momentOfInertia1.z());
		nbt.putFloat("mix2", momentOfInertia2.x());
		nbt.putFloat("miy2", momentOfInertia2.y());
		nbt.putFloat("miz2", momentOfInertia2.z());
		nbt.putDouble("mass", craftMass);
		nbt.putDouble("massInitial", craftMassInitial);
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
			BlockPos offset = entityOffsets.get(pUUID);

			if(offset != null)
			{
				nbt.putUuid("pUUID" + i, pUUID);
				nbt.putInt("px" + i, offset.getX());
				nbt.putInt("py" + i, offset.getY());
				nbt.putInt("pz" + i, offset.getZ());
			}

			i++;
		}
	}

	@Override
	protected void readCustomDataFromNbt(NbtCompound nbt)
	{
		setInitialBlockPos(NbtHelper.toBlockPos(nbt.getCompound("initialBlockPos")));
		setForwardDirection(nbt.getInt("forward"));
		setQuaternion(new Quaternionf(nbt.getFloat("qx"), nbt.getFloat("qy"), nbt.getFloat("qz"), nbt.getFloat("qw")));
		angularVelocity = new Vector3f(nbt.getFloat("avx"), nbt.getFloat("avy"), nbt.getFloat("avz"));
		momentOfInertia1 = new Vector3f(nbt.getFloat("mix1"), nbt.getFloat("miy1"), nbt.getFloat("miz1"));
		momentOfInertia2 = new Vector3f(nbt.getFloat("mix2"), nbt.getFloat("miy2"), nbt.getFloat("miz2"));
		craftMass = nbt.getDouble("mass");
		craftMassInitial = nbt.getDouble("massInitial");
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

	/*@Override
	public Packet<ClientPlayPacketListener> createSpawnPacket()
	{
		return new EntitySpawnS2CPacket(this);
	}*/

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
			else if(!forceUnload && inRange)
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
			if(entityOffsets.get(pUUID) != null)
			{
				buffer.writeUuid(pUUID);
				buffer.writeBlockPos(entityOffsets.get(pUUID));
			}
		}

		ServerPlayNetworking.send(player, new Identifier(StarflightMod.MOD_ID, "moving_craft_entity_offsets"), buffer);
	}

	public static void receiveEntityOffsets(ClientPlayNetworkHandler handler, PacketSender sender, MinecraftClient client, PacketByteBuf buffer)
	{
		int entityID = buffer.readInt();
		int passengerCount = buffer.readInt();
		HashMap<UUID, BlockPos> entityOffsets = new HashMap<UUID, BlockPos>();
		
		for(int i = 0; i < passengerCount; i++)
		{
			UUID pUUID = buffer.readUuid();
			BlockPos offset = buffer.readBlockPos();
			entityOffsets.put(pUUID, offset);
		}
		
		client.execute(() -> {
			if(client.world == null)
				return;
	
			Entity entity = client.world.getEntityById(entityID);
	
			if(entity == null || !(entity instanceof MovingCraftEntity))
				return;
	
			((MovingCraftEntity) entity).entityOffsets = entityOffsets;
		});
	}
}