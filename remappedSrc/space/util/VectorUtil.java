package space.util;

import org.joml.Vector3f;

import net.minecraft.util.math.Vec3d;

public class VectorUtil
{
	/**
	 * Get the cross product of two 3D vectors.
	 */
	public static Vec3d getCrossProduct(Vec3d v1, Vec3d v2)
	{
		double v3x = (v1.getY() * v2.getZ()) - (v1.getZ() * v2.getY());
		double v3y = (v1.getZ() * v2.getX()) - (v1.getX() * v2.getZ());
		double v3z = (v1.getX() * v2.getY()) - (v1.getY() * v2.getX());
		return new Vec3d(v3x, v3y, v3z);
	}
	
	/**
	 * Get the angle between two 3D vectors in radians using their dot product.
	 */
	public static double getAngle(Vec3d v1, Vec3d v2)
	{
		double dotProduct = (v1.getX() * v2.getX()) + (v1.getY() + v2.getY()) + (v1.getZ() + v2.getZ());
		double v1l = v1.length();
		double v2l = v2.length();
		return Math.acos(dotProduct / (v1l * v2l));
	}
	
	/**
	 * Rotate one vector about the axis of another by a given angle in radians.
	 */
	public static Vec3d rotateAboutAxis(Vec3d input, Vec3d axis, double theta)
	{
		double x = input.getX();
		double y = input.getY();
		double z = input.getZ();
		double u = axis.getX();
		double v = axis.getY();
		double w = axis.getZ();
		double xPrime = u * ((u * x) + (v * y) + (w * z)) * (1.0d - Math.cos(theta)) + (x * Math.cos(theta)) + ((-w * y) + (v * z)) * Math.sin(theta);
	    double yPrime = v * ((u * x) + (v * y) + (w * z)) * (1.0d - Math.cos(theta)) + (y * Math.cos(theta)) + ((w * x) - (u * z)) * Math.sin(theta);
	    double zPrime = w * ((u * x) + (v * y) + (w * z)) * (1.0d - Math.cos(theta)) + (z * Math.cos(theta)) + ((-v * x) + (u * y)) * Math.sin(theta);
	    return new Vec3d(xPrime, yPrime, zPrime);
	}
	
	/**
	 * Find the angle in radians between a projection onto the XZ plane of the line formed by the first two vectors and a projection onto the XZ plane of the line formed by the last two vectors.
	 */
	public static double angleBetweenLines(Vec3d in1, Vec3d in2, Vec3d in3, Vec3d in4)
	{
		double angle1 = Math.atan2(in1.getZ() - in2.getZ(), in1.getX() - in2.getX());
		double angle2 = Math.atan2(in3.getZ() - in4.getZ(), in3.getX() - in4.getX());
		double result = angle1 - angle2;
		
		if(result < 0.0d)
			result = Math.abs(result);
	    else
	    	result = 2.0d * Math.PI - result;
		
		return result;
	}
	
	public static float lengthFloat(Vector3f v)
	{
		return (float) Math.sqrt(Math.pow(v.x, 2.0) + Math.pow(v.y, 2.0) + Math.pow(v.z, 2.0));
	}
	
	/**
	 * Find the point of intersection between a line and a plane.
	 * Returns null if the dot product of the plane normal and line direction is within some exclusion range.
	 */
	public static Vec3d linePlaneIntersection(Vec3d line1, Vec3d line2, Vec3d planePoint, Vec3d planeNormal)
	{
		Vec3d lineDirection = line2.subtract(line1);
		double dot = planeNormal.dotProduct(lineDirection);
		
		if(Math.abs(dot) > 1.0e-6)
		{
			Vec3d planeToPoint = planePoint.subtract(line1);
			double t = planeToPoint.dotProduct(planeNormal) / dot;
			return line1.add(lineDirection.multiply(t));
		}
		
		return null;
	}
}