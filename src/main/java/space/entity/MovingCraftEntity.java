package space.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import space.block.FluidTankControllerBlock;
import space.block.StarflightBlocks;
import space.block.entity.FluidTankControllerBlockEntity;
import space.mixin.common.EntityInvokerMixin;
import space.network.s2c.MovingCraftBlocksS2CPacket;
import space.network.s2c.MovingCraftEntityOffsetsS2CPacket;
import space.vessel.MovingCraftBlockData;

public class MovingCraftEntity extends Entity
{
	private static final TrackedData<BlockPos> INITIAL_BLOCK_POS = DataTracker.registerData(MovingCraftEntity.class, TrackedDataHandlerRegistry.BLOCK_POS);
	private static final TrackedData<Integer> FORWARD = DataTracker.registerData(MovingCraftEntity.class, TrackedDataHandlerRegistry.INTEGER);
	private static final TrackedData<Quaternionf> CRAFT_QUATERNION = DataTracker.registerData(MovingCraftEntity.class, TrackedDataHandlerRegistry.QUATERNIONF);
	private static final TrackedData<Vector3f> TRACKED_VELOCITY = DataTracker.registerData(MovingCraftEntity.class, TrackedDataHandlerRegistry.VECTOR3F);
	private static final TrackedData<Vector3f> TRACKED_ANGULAR_VELOCITY = DataTracker.registerData(MovingCraftEntity.class, TrackedDataHandlerRegistry.VECTOR3F);
	private static final TrackedData<Vector3f> TRACKED_ANGLES = DataTracker.registerData(MovingCraftEntity.class, TrackedDataHandlerRegistry.VECTOR3F);
	private static final TrackedData<Vector3f> TRACKED_BOX_MIN = DataTracker.registerData(MovingCraftEntity.class, TrackedDataHandlerRegistry.VECTOR3F);
	private static final TrackedData<Vector3f> TRACKED_BOX_MAX = DataTracker.registerData(MovingCraftEntity.class, TrackedDataHandlerRegistry.VECTOR3F);
	protected ArrayList<MovingCraftBlockData> blocks = new ArrayList<MovingCraftBlockData>();
	protected ArrayList<ServerPlayerEntity> playersInRange = new ArrayList<ServerPlayerEntity>();
	protected HashMap<UUID, BlockPos> entityOffsets = new HashMap<UUID, BlockPos>();
	private Vector3f momentOfInertia1;
	private Vector3f momentOfInertia2;
	public Vector3f angularVelocity;
	public Quaternionf clientQuaternion;
	public Quaternionf clientQuaternionPrevious;
	public Vector3f clientAngles;
	public Vector3f clientAnglesPrevious;
	public int clientInterpolationSteps;
	private double clientX;
	private double clientY;
	private double clientZ;
	private double clientXVelocity;
	private double clientYVelocity;
	private double clientZVelocity;
	private double craftMass;
	private double craftMassInitial;
	private double craftVolume;
	private Vec3d boxMin;
	private Vec3d boxMax;

	public MovingCraftEntity(EntityType<? extends MovingCraftEntity> entityType, World world)
	{
		super(entityType, world);
	}

	public MovingCraftEntity(EntityType<? extends MovingCraftEntity> type, World world, BlockPos blockPos, ArrayList<MovingCraftBlockData> blocks, double mass, double volume, Vector3f momentOfInertia1, Vector3f momentOfInertia2)
	{
		this(type, world);
		this.blocks = blocks;
		this.craftMass = mass;
		this.craftMassInitial = mass;
		this.craftVolume = volume;
		this.momentOfInertia1 = momentOfInertia1;
		this.momentOfInertia2 = momentOfInertia2;
		this.angularVelocity = new Vector3f();
		this.setPosition(blockPos.toCenterPos());
		this.setRotation(0.0f, 0.0f);
		this.setQuaternion(new Quaternionf());
		this.setInitialBlockPos(this.getBlockPos());

		if(blocks.isEmpty())
			setRemoved(RemovalReason.DISCARDED);

		BlockPos min = new BlockPos(blocks.get(0).getPosition());
		BlockPos max = new BlockPos(blocks.get(0).getPosition());

		for(MovingCraftBlockData blockData : blocks)
		{
			BlockPos pos = blockData.getPosition();

			if(pos.getX() < min.getX())
				min = new BlockPos(pos.getX(), min.getY(), min.getZ());
			else if(pos.getX() > max.getX())
				max = new BlockPos(pos.getX(), max.getY(), max.getZ());

			if(pos.getY() < min.getY())
				min = new BlockPos(min.getX(), pos.getY(), min.getZ());
			else if(pos.getY() > max.getY())
				max = new BlockPos(max.getX(), pos.getY(), max.getZ());

			if(pos.getZ() < min.getZ())
				min = new BlockPos(min.getX(), min.getY(), pos.getZ());
			else if(pos.getZ() > max.getZ())
				max = new BlockPos(max.getX(), max.getY(), pos.getZ());
		}

		boxMin = new Vec3d(min.getX() - 0.5, min.getY() - 0.5, min.getZ() - 0.5);
		boxMax = new Vec3d(max.getX() + 0.5, max.getY() + 0.5, max.getZ() + 0.5);
	}

	@Override
	protected Box calculateBoundingBox()
	{
		World world = getWorld();

		if(world.isClient)
		{
			Vec3d min = new Vec3d(this.dataTracker.get(TRACKED_BOX_MIN));
			Vec3d max = new Vec3d(this.dataTracker.get(TRACKED_BOX_MAX));
			return new Box(getPos().add(min), getPos().add(max));
		} else if(boxMin != null && boxMax != null)
			return new Box(getPos().add(boxMin), getPos().add(boxMax));

		return Box.from(getPos());
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
	protected void initDataTracker(DataTracker.Builder builder)
	{
		builder.add(INITIAL_BLOCK_POS, BlockPos.ORIGIN);
		builder.add(FORWARD, Integer.valueOf(0));
		builder.add(CRAFT_QUATERNION, new Quaternionf());
		builder.add(TRACKED_VELOCITY, new Vector3f());
		builder.add(TRACKED_ANGULAR_VELOCITY, new Vector3f());
		builder.add(TRACKED_ANGLES, new Vector3f());
		builder.add(TRACKED_BOX_MIN, new Vector3f());
		builder.add(TRACKED_BOX_MAX, new Vector3f());
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

	public void setTrackedAngles(Vector3f angles)
	{
		this.dataTracker.set(TRACKED_ANGLES, angles);
	}

	public void updateTrackedBox()
	{
		this.dataTracker.set(TRACKED_BOX_MIN, boxMin.toVector3f());
		this.dataTracker.set(TRACKED_BOX_MAX, boxMax.toVector3f());
	}
	
	public ArrayList<MovingCraftBlockData> getBlocks()
	{
		return blocks;
	}

	public double getLowerHeight()
	{
		return boxMin.getY();
	}

	public double getUpperHeight()
	{
		return boxMax.getY();
	}

	public double getXWidth()
	{
		return boxMax.getX() - boxMin.getX();
	}

	public double getZWidth()
	{
		return boxMax.getZ() - boxMin.getZ();
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

	public Vector3f getTrackedAngles()
	{
		return this.dataTracker.get(TRACKED_ANGLES);
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

	public double getDisplacementVolume()
	{
		return craftVolume;
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
		Matrix3f tensor = new Matrix3f(momentOfInertia1.x(), momentOfInertia2.x(), momentOfInertia2.y(), momentOfInertia2.x(), momentOfInertia1.y(), momentOfInertia2.z(), momentOfInertia2.y(), momentOfInertia2.z(), momentOfInertia1.z());
		// Matrix3f rotation = new Matrix3f().rotation(getQuaternion());
		// tensor.mulLocal(rotation).mul(rotation.transpose());
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
		addVelocity((force.x() / getMass()) * 0.0025f, (force.y() / getMass()) * 0.0025f, (force.z() / getMass()) * 0.0025f);
	}

	/**
	 * Apply a moment in Newton meters about the local X, Y, and Z axes of the
	 * craft.
	 */
	public void applyMomentXYZ(Vector3f moment)
	{
		Matrix3f mit = getMomentOfInertiaTensor();
		// Get the angular velocity converted back to rad/s.
		Vector3f av = new Vector3f(angularVelocity).mul(20.0f);
		// Calculate the angular acceleration from Euler's rotation equation.
		Vector3f acc = moment.sub(av.cross(av.mul(mit))).mul(mit.invert());
		// Convert the angular acceleration to m/tick^2 and add it to the angular velocity.
		angularVelocity.add(acc.mul(0.0025f));
	}

	public void integrateLocalAngles(float avx, float avy, float avz)
	{
		boolean b = getForwardDirection() == Direction.NORTH || getForwardDirection() == Direction.SOUTH;
		Vector3f angles = getTrackedAngles();
		float ax = angles.x() + (b ? avx : avz);
		float ay = angles.y() + avy;
		float az = angles.z() + (b ? avz : avx);

		if(ax < -Math.PI)
			ax += Math.PI * 2.0f;
		else if(ax > Math.PI)
			ax -= Math.PI * 2.0f;

		if(ay < -Math.PI)
			ay += Math.PI * 2.0f;
		else if(ay > Math.PI)
			ay -= Math.PI * 2.0f;

		if(az < -Math.PI)
			az += Math.PI * 2.0f;
		else if(az > Math.PI)
			az -= Math.PI * 2.0f;

		setTrackedAngles(new Vector3f(ax, ay, az));
	}

	public Vector3f getAngularVelocity()
	{
		return angularVelocity;
	}
	
	/**
	 * Push away external entities taking into account the contained blocks and rotation.
	 */
	public void externalEntityCollisions()
	{
		List<Entity> entities = getWorld().getOtherEntities(this, getBoundingBox().expand(10.0));
		Quaternionf qinv = new Quaternionf(getQuaternion()).invert();

		for(Entity entity : entities)
		{
			if(getWorld().isClient() && !(entity instanceof PlayerEntity))
				continue;
			
			if(hasPassengerDeep(entity))
				continue;
			
			Vec3d offset = new Vec3d(0.0, 0.0, 0.0);
			Vector3f otherPos = entity.getPos().subtract(getPos()).toVector3f().rotate(qinv);
			Box otherBox = entity.getBoundingBox().offset(otherPos.sub(entity.getPos().toVector3f()));
			boolean velocity = false;
			
			for(MovingCraftBlockData blockData : blocks)
			{
				Box box = new Box(blockData.getPosition()).offset(-0.5, -0.5, -0.5);
				
				if(!velocity && box.intersects(otherBox.expand(0.001)))
					velocity = true;
				
				if(!box.intersects(otherBox))
					continue;
				
				Box intersection = box.intersection(otherBox);
				double ix = intersection.getLengthX();
				double iy = intersection.getLengthY();
				double iz = intersection.getLengthZ();
				Vec3d boxCenter = box.getCenter();
				Vec3d otherBoxCenter = otherBox.getCenter();
				
				//System.out.println(intersection.getAverageSideLength());
				
				if(ix < iy && ix < iz && offset.getX() == 0.0)
				{
					if(boxCenter.getX() < otherBoxCenter.getX())
						offset = offset.add(ix, 0.0, 0.0);
					else
						offset = offset.add(-ix, 0.0, 0.0);
				}
				else if(iy < ix && iy < iz && offset.getY() == 0.0)
				{
					if(boxCenter.getY() < otherBoxCenter.getY())
						offset = offset.add(0.0, iy, 0.0);
					else
						offset = offset.add(0.0, -iy, 0.0);
				}
				else if(iz < ix && iz < iy && offset.getZ() == 0.0)
				{
					if(boxCenter.getZ() < otherBoxCenter.getZ())
						offset = offset.add(0.0, 0.0, iz);
					else
						offset = offset.add(0.0, 0.0, -iz);
				}
			}
			
			if(!offset.equals(Vec3d.ZERO))
			{
				//System.out.println(entity.getType() + "  " + offset);
				Vector3f globalOffset = offset.toVector3f().rotate(getQuaternion());
				Vec3d moved = entity.getPos().add(globalOffset.x(), globalOffset.y(), globalOffset.z());
				entity.setPosition(moved.getX(), moved.getY(), moved.getZ());
				
				// "Ground" Collision
				if(globalOffset.y() > 0.0 && entity.getVelocity().getY() < 0.0)
				{
					entity.setVelocity(entity.getVelocity().getX(), 0.0, entity.getVelocity().getZ());
					entity.setOnGround(true);
					entity.verticalCollision = true;
					entity.groundCollision = true;
					entity.fallDistance = 0.0f;
				}
			}
			
			if(velocity)
				entity.setPosition(entity.getPos().add(getVelocity()));
		}
	}

	public boolean clientMotion()
	{
		if(getWorld().isClient())
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
		ArrayList<MovingCraftBlockData> blocks = new ArrayList<MovingCraftBlockData>();

		// Fill the block data array list.
		for(BlockPos pos : positionList)
			blocks.add(MovingCraftBlockData.fromBlock(world, positionList, pos, centerPos, isBlockSolid(world, pos)));

		return blocks;
	}

	public static void removeBlocksFromWorld(World world, BlockPos centerPos, ArrayList<MovingCraftBlockData> blocks)
	{
		for(MovingCraftBlockData blockData : blocks)
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
				blockEntity.read(new NbtCompound(), world.getRegistryManager());
		}

		for(MovingCraftBlockData blockData : blocks)
		{
			BlockPos pos = centerPos.add(blockData.getPosition());

			if(!isBlockSolid(world, pos))
			{
				world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);

				for(Entity item : world.getEntitiesByClass(ItemEntity.class, Box.enclosing(pos.add(-1, -1, -1), pos.add(1, 1, 1)), b -> true))
					item.remove(RemovalReason.DISCARDED);
			}
		}

		for(MovingCraftBlockData blockData : blocks)
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
	
	public void pickUpEntities()
	{
		Box box = calculateBoundingBox().expand(1.0);
		
		for(Entity entity : getWorld().getOtherEntities(this, box))
		{
			if(entity instanceof LivingEntity)
			{
				BlockPos offset = entity.getBlockPos().subtract(getBlockPos());
				pickUpEntity(entity, offset);
			}
		}
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
		TeleportTarget movingCraftTarget = new TeleportTarget(destination, arrivalLocation, this.getVelocity(), this.getYaw(), this.getPitch(), TeleportTarget.NO_OP);
		Entity movingCraft = this.teleportTo(movingCraftTarget);

		if(movingCraft != null)
		{
			((MovingCraftEntity) movingCraft).onDimensionChanged(destination, arrivalLocation, arrivalYaw);
			((MovingCraftEntity) movingCraft).entityOffsets.clear();

			for(int i = 0; i < passengerList.size(); i++)
			{
				Entity passenger = passengerList.get(i);
				TeleportTarget passengerTarget = new TeleportTarget(destination, passenger.getPos(), this.getVelocity(), passenger.getYaw(), passenger.getPitch(), TeleportTarget.NO_OP);
				Entity trasferred = passenger.teleportTo(passengerTarget);

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

		for(MovingCraftBlockData blockData : blocks)
		{
			if(blockData.placeFirst())
				toPlaceFirst.add(blockData);
			else
				toPlaceLast.add(blockData);
		}

		for(MovingCraftBlockData blockData : toPlaceFirst)
			blockData.toBlock(this.getWorld(), this.getBlockPos(), quaternion);

		for(MovingCraftBlockData blockData : toPlaceLast)
			blockData.toBlock(this.getWorld(), this.getBlockPos(), quaternion);

		for(MovingCraftBlockData blockData : toPlaceFirst)
		{
			Vector3f offset = new Vector3f(blockData.getPosition().getX(), blockData.getPosition().getY(), blockData.getPosition().getZ()).rotate(quaternion);
			BlockPos blockPos = this.getBlockPos().add(MathHelper.floor(offset.x()), MathHelper.floor(offset.y()), MathHelper.floor(offset.z()));
			onBlockReleased(blockData, blockPos);
		}

		blocks.clear();
	}

	public void onBlockReleased(MovingCraftBlockData blockData, BlockPos worldPos)
	{
		BlockEntity blockEntity = getWorld().getBlockEntity(worldPos);

		if(blockEntity != null && blockEntity instanceof FluidTankControllerBlockEntity)
		{
			FluidTankControllerBlockEntity fluidTank = (FluidTankControllerBlockEntity) blockEntity;
			((FluidTankControllerBlock) blockData.getBlockState().getBlock()).initializeFluidTank(getWorld(), worldPos, fluidTank);
			fluidTank.setStoredFluid(blockData.getStoredFluid());
		}
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
		nbt.putDouble("volume", craftVolume);
		nbt.putDouble("minX", boxMin.getX());
		nbt.putDouble("minY", boxMin.getY());
		nbt.putDouble("minZ", boxMin.getZ());
		nbt.putDouble("maxX", boxMax.getX());
		nbt.putDouble("maxY", boxMax.getY());
		nbt.putDouble("maxZ", boxMax.getZ());
		int blockCount = blocks.size();
		nbt.putInt("blockCount", blockCount);
		int[] x = new int[blockCount];
		int[] y = new int[blockCount];
		int[] z = new int[blockCount];

		for(int i = 0; i < blockCount; i++)
		{
			MovingCraftBlockData blockData = blocks.get(i);
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
		setInitialBlockPos(NbtHelper.toBlockPos(nbt, "initialBlockPos").get());
		setForwardDirection(nbt.getInt("forward"));
		setQuaternion(new Quaternionf(nbt.getFloat("qx"), nbt.getFloat("qy"), nbt.getFloat("qz"), nbt.getFloat("qw")));
		angularVelocity = new Vector3f(nbt.getFloat("avx"), nbt.getFloat("avy"), nbt.getFloat("avz"));
		momentOfInertia1 = new Vector3f(nbt.getFloat("mix1"), nbt.getFloat("miy1"), nbt.getFloat("miz1"));
		momentOfInertia2 = new Vector3f(nbt.getFloat("mix2"), nbt.getFloat("miy2"), nbt.getFloat("miz2"));
		craftMass = nbt.getDouble("mass");
		craftMassInitial = nbt.getDouble("massInitial");
		craftVolume = nbt.getDouble("volume");
		boxMin = new Vec3d(nbt.getDouble("minX"), nbt.getDouble("minY"), nbt.getDouble("minZ"));
		boxMax = new Vec3d(nbt.getDouble("maxX"), nbt.getDouble("maxY"), nbt.getDouble("maxZ"));
		int blockCount = nbt.getInt("blockCount");
		int[] x = nbt.getIntArray("x");
		int[] y = nbt.getIntArray("y");
		int[] z = nbt.getIntArray("z");

		for(int i = 0; i < blockCount; i++)
		{
			BlockPos dataPos = new BlockPos(x[i], y[i], z[i]);
			blocks.add(MovingCraftBlockData.loadData(nbt.getCompound(dataPos.toShortString())));
		}

		int passengerCount = nbt.getInt("passengerCount");

		for(int i = 0; i < passengerCount; i++)
			entityOffsets.put(nbt.getUuid("pUUID" + i), new BlockPos(nbt.getInt("px" + i), nbt.getInt("py" + i), nbt.getInt("pz" + i)));
	}

	public void sendRenderData(boolean forceUnload)
	{
		MinecraftServer server = this.getServer();

		for(ServerPlayerEntity player : server.getPlayerManager().getPlayerList())
		{
			boolean inRange = this.getBlockPos().isWithinDistance(player.getBlockPos(), 1024);

			if(!forceUnload && !this.playersInRange.contains(player) && inRange)
			{
				this.playersInRange.add(player);
				ServerPlayNetworking.send(player, new MovingCraftBlocksS2CPacket(getId(), blocks));
			}
			else if(forceUnload && this.playersInRange.contains(player) && !inRange)
				this.playersInRange.remove(player);
			else if(!forceUnload && inRange)
				sendEntityOffsets(player);
		}
	}
	
	public static void receiveBlockData(MovingCraftBlocksS2CPacket payload, ClientPlayNetworking.Context context)
	{
		int entityID = payload.entityID();
		ArrayList<MovingCraftBlockData> blockList = payload.blockDataList();
		MinecraftClient client = context.client();
		ClientWorld clientWorld = client.world;
		
		client.execute(() -> {
			if(clientWorld == null)
				return;

			Entity entity = clientWorld.getEntityById(entityID);

			if(entity == null || !(entity instanceof MovingCraftEntity))
				return;

			((MovingCraftEntity) entity).blocks.clear();
			((MovingCraftEntity) entity).blocks.addAll(blockList);
			
		});
	}

	public void sendEntityOffsets(ServerPlayerEntity player)
	{
		HashMap<Integer, BlockPos> passengerMap = new HashMap<Integer, BlockPos>();

		for(UUID uuid : entityOffsets.keySet())
		{
			if(entityOffsets.get(uuid) != null && player.getServerWorld().getEntity(uuid) != null)
				passengerMap.put(player.getServerWorld().getEntity(uuid).getId(), entityOffsets.get(uuid));
		}

		ServerPlayNetworking.send(player, new MovingCraftEntityOffsetsS2CPacket(getId(), passengerMap));
	}

	public static void receiveEntityOffsets(MovingCraftEntityOffsetsS2CPacket payload, ClientPlayNetworking.Context context)
	{
		int entityID = payload.entityID();
		HashMap<Integer, BlockPos> passengerMap = payload.passengerMap();
		MinecraftClient client = context.client();
		ClientWorld clientWorld = client.world;
		
		client.execute(() -> {
			if(clientWorld == null)
				return;

			Entity entity = clientWorld.getEntityById(entityID);

			if(entity == null || !(entity instanceof MovingCraftEntity))
				return;

			((MovingCraftEntity) entity).entityOffsets.clear();
			
			for(int id : passengerMap.keySet())
				((MovingCraftEntity) entity).entityOffsets.put(clientWorld.getEntityById(id).getUuid(), passengerMap.get(id));
		});
	}
}