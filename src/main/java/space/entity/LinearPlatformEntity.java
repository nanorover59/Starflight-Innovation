package space.entity;

import java.util.ArrayList;

import org.joml.Vector3f;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.math.BlockPos;
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
		// Run client-side actions and then return.
		if(this.clientMotion())
		{
			this.clientQuaternionPrevious = this.clientQuaternion;
			this.clientQuaternion = this.getQuaternion();
			this.clientAnglesPrevious = this.clientAngles;
			this.clientAngles = this.getTrackedAngles();
			return;
		}
		
		if(blockDataList.isEmpty())
		{
			setRemoved(RemovalReason.DISCARDED);
			return;
		}
		
		if(soundEffectTimer <= 0)
		{
			playSound(StarflightEffects.ELECTRIC_MOTOR_SOUND_EVENT, 1e3F, 1.0f);
			soundEffectTimer = 12;
		}
		else
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
		}
		else
			sendRenderData(false);
		
		updateTrackedBox();
		setTrackedVelocity(getVelocity().toVector3f());
		setTrackedAngularVelocity(getAngularVelocity());
		setBoundingBox(calculateBoundingBox());
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
		targetPos = NbtHelper.toBlockPos(nbt.getCompound("targetPos"));
	}
}