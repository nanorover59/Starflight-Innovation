package space.util;

public class CubicHermiteSpline
{
	private final double startX;
	private final double endX;
	private final double startY;
	private final double endY;
	private final double startSlope;
	private final double endSlope;
	
	public CubicHermiteSpline(double startX, double endX, double startY, double endY, double startSlope, double endSlope)
	{
		this.startX = startX;
		this.endX = endX;
		this.startY = startY;
		this.endY = endY;
		this.startSlope = startSlope;
		this.endSlope = endSlope;
	}
	
	private double hermite00(double x)
	{
		return (2.0 * Math.pow(x, 3)) - (3.0 * Math.pow(x, 2)) + 1.0;
	}
	
	private double hermite10(double x)
	{
		return Math.pow(x, 3) - (2.0 * Math.pow(x, 2)) + x;
	}
	
	private double hermite01(double x)
	{
		return (-2.0 * Math.pow(x, 3)) + (3.0 * Math.pow(x, 2));
	}
	
	private double hermite11(double x)
	{
		return Math.pow(x, 3) - Math.pow(x, 2);
	}
	
	/**
	 * Get the Y value of this spline segment for the given X value.
	 * The given X value must be between the start and end X values or else the returned Y value will be constrained.
	 */
	public double get(double x)
	{
		if(x <= startX)
			return startY;
		else if(x > endX)
			return endY;
		
		return (hermite00(x) * startY) + (hermite10(x) * (endX - startX) * startSlope) + (hermite01(x) * endY) + (hermite11(x) * (endX - startX) * endSlope);
	}
}
