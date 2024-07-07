package space.util;

import org.joml.Quaternionf;

public class QuaternionUtil
{
	public static Quaternionf hamiltonProduct(Quaternionf q0, Quaternionf q1)
	{
		float x = q0.w() * q1.x() + q0.x() * q1.w() + q0.y() * q1.z() - q0.z() * q1.y();
		float y = q0.w() * q1.y() - q0.x() * q1.z() + q0.y() * q1.w() + q0.z() * q1.x();
		float z = q0.w() * q1.z() + q0.x() * q1.y() - q0.y() * q1.x() + q0.z() * q1.w();
		float w = q0.w() * q1.w() - q0.x() * q1.x() - q0.y() * q1.y() - q0.z() * q1.z();
		return new Quaternionf(x, y, z, w);
	}

	public static Quaternionf fromEulerXYZ(float x, float y, float z)
	{
		Quaternionf quaternion = new Quaternionf();
		quaternion = hamiltonProduct(quaternion, new Quaternionf((float) Math.sin(x / 2.0f), 0.0f, 0.0f, (float) Math.cos(x / 2.0f)));
		quaternion = hamiltonProduct(quaternion, new Quaternionf(0.0f, (float) Math.sin(y / 2.0f), 0.0f, (float) Math.cos(y / 2.0f)));
		quaternion = hamiltonProduct(quaternion, new Quaternionf(0.0f, 0.0f, (float) Math.sin(z / 2.0f), (float) Math.cos(z / 2.0f)));
		return quaternion;
	}

	public static Quaternionf interpolate(Quaternionf q0, Quaternionf q1, float t)
	{
		float dot = q0.dot(q1);
		
		// Ensure that the interpolation is taking the shortest path.
		if(dot < 0.0f)
		{
			q1 = new Quaternionf(-q1.x(), -q1.y(), -q1.z(), -q1.w());
			dot = -dot;
		}
		
		// Do not allow a dot product greater than 1.0 due to computational error.
		double theta = Math.acos(dot < 1.0 ? dot : 1.0);
		double sinTheta = Math.sin(theta);
		
		if(sinTheta != 0.0)
		{
			double x = (q0.x() * Math.sin((1.0 - t) * theta) + q1.x() * Math.sin(t * theta)) / sinTheta;
			double y = (q0.y() * Math.sin((1.0 - t) * theta) + q1.y() * Math.sin(t * theta)) / sinTheta;
			double z = (q0.z() * Math.sin((1.0 - t) * theta) + q1.z() * Math.sin(t * theta)) / sinTheta;
			double w = (q0.w() * Math.sin((1.0 - t) * theta) + q1.w() * Math.sin(t * theta)) / sinTheta;
			return new Quaternionf(x, y, z, w);
		}
		else
			return q0;
	}

	public static float difference(Quaternionf q0, Quaternionf q1)
	{
		double dot = q0.x() * q1.x() + q0.y() * q1.y() + q0.z() * q1.z() + q0.w() * q1.w();
		return (float) Math.acos(2.0 * dot * dot - 1.0);
	}
}