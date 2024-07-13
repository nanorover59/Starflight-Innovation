package space.entity;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import space.util.StarflightEffects;
import space.vessel.MovingCraftBlockData;

public class LinearPlatformEntity extends MovingCraftEntity
{
	private BlockPos targetPos;
	private int soundEffectTimer;
	private int stopTimer;

	public LinearPlatformEntity(EntityType<? extends LinearPlatformEntity> entityType, World world)
	{
		super(entityType, world);
	}

	public LinearPlatformEntity(World world, BlockPos blockPos, ArrayList<MovingCraftBlockData> blockDataList, double mass, double volume, Vector3f momentOfInertia1, Vector3f momentOfInertia2, BlockPos targetPos)
	{
		super(StarflightEntities.LINEAR_PLATFORM, world, blockPos, blockDataList, mass, volume, momentOfInertia1, momentOfInertia2);
		this.targetPos = targetPos;
		soundEffectTimer = 0;
		stopTimer = 0;
	}

	@Override
	public void tick()
	{
		handleCollisions();
		
		// Run client-side actions and then return.
		if(this.clientMotion())
		{
			this.clientQuaternionPrevious = this.clientQuaternion;
			this.clientQuaternion = this.getQuaternion();
			this.clientAnglesPrevious = this.clientAngles;
			this.clientAngles = this.getTrackedAngles();
			return;
		}

		if(blocks.isEmpty())
		{
			setRemoved(RemovalReason.DISCARDED);
			return;
		}

		if(soundEffectTimer <= 0)
		{
			playSound(StarflightEffects.ELECTRIC_MOTOR_SOUND_EVENT, 1e3F, 1.0f);
			soundEffectTimer = 12;
		} else
			soundEffectTimer--;

		double speed = 0.1;
		Vec3d difference = targetPos.toCenterPos().subtract(getPos());
		Vec3d v = difference.normalize().multiply(speed);
		setVelocity(v);

		if(stopTimer == 0)
			move(MovementType.SELF, getVelocity());

		if(v.length() == 0 || horizontalCollision || verticalCollision)
			stopTimer++;
		else
			stopTimer = 0;

		if(stopTimer == 5)
		{
			sendRenderData(true);
			this.releaseBlocks();
		} else
			sendRenderData(false);
		
		updateTrackedBox();
		setTrackedVelocity(getVelocity().toVector3f());
		setTrackedAngularVelocity(getAngularVelocity());
		setBoundingBox(calculateBoundingBox());
	}

	private void handleCollisions()
	{
		Box box = getBoundingBox();
		List<Entity> entities = getWorld().getOtherEntities(this, getBoundingBox());

		for(Entity entity : entities)
		{
			Vec3d offset = new Vec3d(0.0, 0.0, 0.0);
			Box otherBox = entity.getBoundingBox();
			Box intersection = box.intersection(otherBox);
			double ix = intersection.getLengthX();
			double iy = intersection.getLengthY();
			double iz = intersection.getLengthZ();
			
			if(ix < iy && ix < iz)
			{
				if(otherBox.minX <= box.maxX && otherBox.maxX > box.maxX)
					offset = offset.add(ix, 0.0, 0.0);
				else if(otherBox.maxX >= box.minX && otherBox.minX < box.minX)
					offset = offset.add(-ix, 0.0, 0.0);
			}
			else if(iy < ix && iy < iz)
			{
				if(otherBox.minY <= box.maxY && otherBox.maxY > box.maxY)
				{
					offset = offset.add(0.0, iy, 0.0);
					//entity.verticalCollision = true;
					//entity.groundCollision = true;
					entity.setOnGround(true);
					
					if(iy > 0.005)
						entity.addVelocity(0.0, entity.getFinalGravity(), 0.0);
				}
				else if(otherBox.maxY >= box.minY && otherBox.minY < box.minY)
				{
					offset = offset.add(0.0, -iy, 0.0);
					entity.verticalCollision = true;
				}
			}
			else if(iz < ix && iz < iy)
			{
				if(otherBox.minZ <= box.maxZ && otherBox.maxZ > box.maxZ)
					offset = offset.add(0.0, 0.0, iz);
				else if(otherBox.maxZ >= box.minZ && otherBox.minZ < box.minZ)
					offset = offset.add(0.0, 0.0, -iz);
			}
			
			if(!offset.equals(Vec3d.ZERO))
			{
				System.out.println(ix + "   " + iy + "   " + iz);
				Vec3d moved = entity.getPos().add(offset).add(getVelocity());
				entity.setPosition(moved.getX(), moved.getY(), moved.getZ());
			}
		}
	}

	private Vec3d getOverlapDepth(Box box1, Box box2)
	{
		double minOverlapX = Math.min(box1.maxX - box2.minX, box2.maxX - box1.minX);
	    double minOverlapY = Math.min(box1.maxY - box2.minY, box2.maxY - box1.minY);
	    double minOverlapZ = Math.min(box1.maxZ - box2.minZ, box2.maxZ - box1.minZ);

	    // Determine the sign based on the positions of the boxes
	    double overlapX = (box1.maxX - box2.minX < box2.maxX - box1.minX) ? -minOverlapX : minOverlapX;
	    double overlapY = (box1.maxY - box2.minY < box2.maxY - box1.minY) ? -minOverlapY : minOverlapY;
	    double overlapZ = (box1.maxZ - box2.minZ < box2.maxZ - box1.minZ) ? -minOverlapZ : minOverlapZ;
		return new Vec3d(overlapX, overlapY, overlapZ);
	}

	private Vec3d calculateMinimumTranslationVector(Vec3d overlapDepth)
	{
		// Get the absolute values of the overlap depths
		double absX = Math.abs(overlapDepth.x);
		double absY = Math.abs(overlapDepth.y);
		double absZ = Math.abs(overlapDepth.z);

		// Find the axis with the smallest overlap
		if(absX < absY && absX < absZ)
			return new Vec3d(overlapDepth.x, 0, 0);
		else if(absY < absX && absY < absZ)
			return new Vec3d(0, overlapDepth.y, 0);
		else
			return new Vec3d(0, 0, overlapDepth.z);
	}

	@Override
	protected void writeCustomDataToNbt(NbtCompound nbt)
	{
		super.writeCustomDataToNbt(nbt);
		nbt.put("targetPos", NbtHelper.fromBlockPos(targetPos));

	}

	@Override
	protected void readCustomDataFromNbt(NbtCompound nbt)
	{
		super.readCustomDataFromNbt(nbt);
		targetPos = NbtHelper.toBlockPos(nbt, "targetPos").get();
	}
}