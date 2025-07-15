package space.craft;

import org.joml.AxisAngle4f;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import space.particle.StarflightParticleTypes;
import space.util.CubicHermiteSpline;

public class Thruster
{
	private static final double SG = 9.80665; // Standard gravity for ISP calculations.
	private Vector3f position;
	private Vector3f direction;
	private double vacuumISP;
	private double atmISP;
	private double vacuumThrust;
	private double thrust;
	private double isp;
	private double gimbal;

	public Thruster(Vector3f position, Vector3f direction, double vacuumISP, double atmISP, double vacuumThrust, double gimbal)
	{
		this.position = position;
		this.direction = direction;
		this.vacuumISP = vacuumISP;
		this.atmISP = atmISP;
		this.vacuumThrust = vacuumThrust;
		this.isp = vacuumISP;
		this.thrust = vacuumThrust;
		this.gimbal = gimbal;
	}

	public Thruster(NbtCompound nbt)
	{
		readFromNbt(nbt);
	}
	
	public Thruster(PacketByteBuf buffer)
    {
		this(buffer.readVector3f(), buffer.readVector3f(), buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
		this.isp = buffer.readDouble();
		this.thrust = buffer.readDouble();
    }
	
	public void forAtmosphere(double pressure)
	{
		CubicHermiteSpline splineISP1 = new CubicHermiteSpline(0.0, 1.0, vacuumISP, atmISP, -10.0, -1.0);
		CubicHermiteSpline splineISP2 = new CubicHermiteSpline(1.0, 100.0, atmISP, 0.0001, -1.0, -0.0001);
		this.isp = pressure <= 1.0 ? splineISP1.get(pressure) : splineISP2.get(pressure);
		this.thrust = this.isp * (vacuumThrust / vacuumISP);
	}

	public Vector3f getPosition()
	{
		return new Vector3f(position);
	}
	
	public Vector3f getDirection()
	{
		return new Vector3f(direction);
	}

	public Vector3f getForce(Quaternionf quaternion, double throttle)
	{
		return new Vector3f(direction).mul((float) (-thrust * throttle));
	}

	public Vector3f getForceWithGimbal(Quaternionf quaternion, double throttle, float rollControl, float pitchControl, float yawControl, boolean rollOnZAxis)
	{
		Vector3f rotatedDirection = getDirectionWithGimbal(position, direction, (float) gimbal, rollControl, pitchControl, yawControl, rollOnZAxis);
		return rotatedDirection.mul((float) (-thrust * throttle));
	}
	
	private Vector3f getDirectionWithGimbal(Vector3f position, Vector3f direction, float gimbal, float rollControl, float pitchControl, float yawControl, boolean rollOnZAxis)
	{
		Vector3f rotatedDirection = new Vector3f(direction);
		Vector3f axis = new Vector3f(position.x(), 0.0f, position.z());

		if(axis.length() > 1.0f)
		{
			Matrix3f matrix = new Matrix3f().rotate(gimbal * yawControl, axis.normalize());
			
			if(Math.abs(yawControl) > 0.5)
				matrix.transform(rotatedDirection);
		}
		else
		{
			Vector3f rollAxis = rollOnZAxis ? new Vector3f(0.0f, 0.0f, 1.0f) : new Vector3f(1.0f, 0.0f, 0.0f);
			Vector3f pitchAxis = rollOnZAxis ? new Vector3f(1.0f, 0.0f, 0.0f) : new Vector3f(0.0f, 0.0f, 1.0f);
			Matrix3f rollMatrix = new Matrix3f().rotate(new AxisAngle4f(gimbal * rollControl, rollAxis));
			Matrix3f pitchMatrix = new Matrix3f().rotate(new AxisAngle4f(gimbal * pitchControl, pitchAxis));
			
			if(Math.abs(rollControl) > 0.5)
				rollMatrix.transform(rotatedDirection);
			
			if(Math.abs(pitchControl) > 0.5)
				pitchMatrix.transform(rotatedDirection);
		}
		
		return rotatedDirection.normalize();
	}
	
	public double getVacuumMassFlow()
	{
		return vacuumThrust / (SG * vacuumISP);
	}

	public double getMassFlow(double throttle)
	{
		return ((thrust * throttle) / (SG * isp));
	}
	
	public double getISP()
	{
		return isp;
	}
	
	public double getVacuumThrust()
	{
		return vacuumThrust;
	}
	
	public double getThrust()
	{
		return thrust;
	}
	
	public double getGimbal()
	{
		return gimbal;
	}
	
	public void createParticles(World world, Random random, Vec3d centerOfMass, Quaternionf quaternion, Vector3f craftVelocity, Vector3f craftAngularVelocity, Vector3f attitudeControl, Direction forwards, boolean rcs)
	{
		Vector3f rotatedDirection = new Vector3f(direction);
		
		if(gimbal > 0.0f)
		{
			boolean rollOnZAxis = forwards == Direction.NORTH || forwards == Direction.SOUTH;
			rotatedDirection = getDirectionWithGimbal(position, direction, (float) gimbal, attitudeControl.x(), attitudeControl.y(), attitudeControl.z(), rollOnZAxis);
		}

		Vector3f rotated = new Vector3f(position).rotate(quaternion);
		Vector3f rotationVelocity = new Vector3f(craftAngularVelocity).cross(rotated);
		rotatedDirection.rotate(quaternion);
		Vec3d velocity = new Vec3d(rotatedDirection.x(), rotatedDirection.y(), rotatedDirection.z()).multiply(0.5 + random.nextDouble() * 0.1).add(craftVelocity.x() + rotationVelocity.x(), craftVelocity.y() + rotationVelocity.y(), craftVelocity.z() + rotationVelocity.z());

		if(rcs)
			world.addParticle(StarflightParticleTypes.RCS_THRUSTER, true, (float) centerOfMass.getX() + rotated.x(), (float) centerOfMass.getY() + rotated.y(), (float) centerOfMass.getZ() + rotated.z(), velocity.getX(), velocity.getY(), velocity.getZ());
		else
		{
			for(int i = 0; i < 4; i++)
			{
				world.addParticle(StarflightParticleTypes.THRUSTER, true, (float) centerOfMass.getX() + rotated.x(), (float) centerOfMass.getY() + rotated.y(), (float) centerOfMass.getZ() + rotated.z(), velocity.getX(), velocity.getY(), velocity.getZ());
				rotated.add((random.nextFloat() - random.nextFloat()) * 0.1f, (random.nextFloat() - random.nextFloat()) * 0.1f, (random.nextFloat() - random.nextFloat()) * 0.1f);
			}
		}
	}

	public NbtCompound writeToNbt(NbtCompound nbt)
	{
		nbt.putFloat("px", position.x());
		nbt.putFloat("py", position.y());
		nbt.putFloat("pz", position.z());
		nbt.putFloat("dx", direction.x());
		nbt.putFloat("dy", direction.y());
		nbt.putFloat("dz", direction.z());
		nbt.putDouble("vacuumISP", vacuumISP);
		nbt.putDouble("atmISP", atmISP);
		nbt.putDouble("vacuumThrust", vacuumThrust);
		nbt.putDouble("isp", isp);
		nbt.putDouble("thrust", thrust);
		nbt.putDouble("gimbal", gimbal);
		return nbt;
	}

	public static Thruster readFromNbt(NbtCompound nbt)
	{
		Vector3f position = new Vector3f(nbt.getFloat("px"), nbt.getFloat("py"), nbt.getFloat("pz"));
		Vector3f direction = new Vector3f(nbt.getFloat("dx"), nbt.getFloat("dy"), nbt.getFloat("dz"));
		double vacuumISP = nbt.getDouble("vacuumISP");
		double atmISP = nbt.getDouble("atmISP");
		double vacuumThrust = nbt.getDouble("vacuumThrust");
		double isp = nbt.getDouble("isp");
		double thrust = nbt.getDouble("thrust");
		double gimbal = nbt.getDouble("gimbal");
		Thruster thruster = new Thruster(position, direction, vacuumISP, atmISP, vacuumThrust, gimbal);
		thruster.isp = isp;
		thruster.thrust = thrust;
		return thruster;
	}
	
	public static void writeToBuffer(PacketByteBuf buffer, Thruster thruster)
	{
		buffer.writeVector3f(thruster.position);
		buffer.writeVector3f(thruster.direction);
		buffer.writeDouble(thruster.vacuumISP);
		buffer.writeDouble(thruster.atmISP);
		buffer.writeDouble(thruster.vacuumThrust);
		buffer.writeDouble(thruster.gimbal);
		buffer.writeDouble(thruster.isp);
		buffer.writeDouble(thruster.thrust);
	}
}