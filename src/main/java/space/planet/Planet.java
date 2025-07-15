package space.planet;

import java.util.ArrayList;
import java.util.HashMap;

import net.darkhax.ess.DataCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import space.util.VectorUtil;

public class Planet implements Comparable<Planet>
{
	public static final double G = 6.67430e-11;
	public static final double EARTH_MASS = 5.97e24;
	public static final double EARTH_RADIUS = 6.3780e6;
	
	public static final int EXTRA_COLD = 0;
	public static final int COLD = 1;
	public static final int TEMPERATE = 2;
	public static final int HOT = 3;
	public static final int EXTRA_HOT = 4;
	
	//private PlanetList system;
	private String name;
	private String dimension;
	private String parentName;
	private Planet parent;
	private ArrayList<Planet> satellites;
	private int satelliteLevel;
	private boolean isTidallyLocked;
	private double mass;
	private double radius;
	private double lowOrbitAltitude;
	private double periapsis;
	private double apoapsis;
	private double argumentOfPeriapsis;
	private double trueAnomaly;
	private double ascendingNode;
	private double inclination;
	private double obliquity;
	private double rotation;
	private double rotationRate;
	private double precession;
	private double precessionRate;
	private double surfaceGravity;
	private double parkingOrbitAngularSpeed;
	private double parkingOrbitAngle;
	private Vec3d position;
	private Vec3d velocity;
	private Vec3d acceleration;
	private Vec3d surfaceViewpoint;
	private Vec3d parkingOrbitViewpoint;
	private Vec3d positionPrevious;
	private Vec3d surfaceViewpointPrevious;
	private Vec3d parkingOrbitViewpointPrevious;
	private int temperatureCategory;
	private boolean simpleTexture;
	private boolean drawClouds;
	private double cloudRotation;
	private double cloudRotationRate;
	private int cloudLevel;
	private int cloudTimer;
	public double sunAngle;
	public double sunAngleOrbit;
	private PlanetDimensionData orbit;
	private PlanetDimensionData surface;
	private PlanetDimensionData sky;
	
	public Planet(String name_, String parentName_, double mass_, double radius_, double lowOrbitAltitude_)
	{
		name = name_;
		parentName = parentName_;
		mass = mass_;
		radius = radius_;
		lowOrbitAltitude = lowOrbitAltitude_;
		surfaceGravity = ((G * mass) / (radius * radius) / ((G * EARTH_MASS) / (EARTH_RADIUS * EARTH_RADIUS)));
		parkingOrbitAngularSpeed = Math.sqrt((G * mass) / Math.pow(radius + lowOrbitAltitude, 3.0));
		position = new Vec3d(0.0, 0.0, 0.0);
		velocity = new Vec3d(0.0, 0.0, 0.0);
		acceleration = new Vec3d(0.0, 0.0, 0.0);
		surfaceViewpoint = new Vec3d(0.0, 0.0, 0.0);
		parkingOrbitViewpoint = new Vec3d(0.0, 0.0, 0.0);
		positionPrevious = new Vec3d(0.0, 0.0, 0.0);
		surfaceViewpointPrevious = new Vec3d(0.0, 0.0, 0.0);
		parkingOrbitViewpointPrevious = new Vec3d(0.0, 0.0, 0.0);
		satellites = new ArrayList<Planet>();
	}
	
	@Override
    public boolean equals(Object object)
	{
		if(object == null || object.getClass() != this.getClass())
			return false;
		
		Planet other = (Planet) object;
		return other.getName().equals(name);	
	}
	
	@Override
    public int compareTo(Planet other)
	{
		if(PlanetList.getClient().getViewpointPlanet() == null)
			return 0;
		
		double r1 = getPosition().subtract(PlanetList.getClient().getViewpointPlanet().getPosition()).length();
		double r2 = other.getPosition().subtract(PlanetList.getClient().getViewpointPlanet().getPosition()).length();
        return (int) (r2 - r1);
    }
	
	public void setOrbitParameters(double periapsis_, double apoapsis_, double argumentOfPeriapsis_, double trueAnomaly_, double ascendingNode_, double inclination_)
	{
		periapsis = periapsis_;
		apoapsis = apoapsis_;
		argumentOfPeriapsis = argumentOfPeriapsis_;
		trueAnomaly = trueAnomaly_;
		ascendingNode = ascendingNode_;
		inclination = inclination_;
	}
	
	public void setRotationParameters(boolean isTidallyLocked_, double obliquity_, double rotationRate_, double precessionRate_)
	{
		isTidallyLocked = isTidallyLocked_;
		obliquity = obliquity_;
		rotationRate = rotationRate_;
		precessionRate = precessionRate_;
		rotation = 0.0;
		precession = 0.0;
		parkingOrbitAngle = 0.0;
	}
	
	public void setDecorativeParameters(boolean simpleTexture_, boolean drawClouds_, double cloudRotationRate_)
	{
		simpleTexture = simpleTexture_;
		drawClouds = drawClouds_;
		cloudRotationRate = cloudRotationRate_;
		rotation = 0.0;
		cloudLevel = 0;
		cloudTimer = 6000;
	}
	
	public PlanetDimensionData getOrbit()
	{
		return orbit;
	}

	public void setOrbit(PlanetDimensionData orbit)
	{
		this.orbit = orbit.forPlanet(this);
	}

	public PlanetDimensionData getSurface()
	{
		return surface;
	}

	public void setSurface(PlanetDimensionData surface)
	{
		this.surface = surface.forPlanet(this);
	}

	public PlanetDimensionData getSky()
	{
		return sky;
	}

	public void setSky(PlanetDimensionData sky)
	{
		this.sky = sky.forPlanet(this);
	}
	
	public void setSatelliteLevel(int level)
	{
		this.satelliteLevel = level;
	}
	
	public void linkSatellites(ArrayList<Planet> planetList)
	{
		for(Planet p : planetList)
		{
			if(p.parentName.equals(name))
			{
				satellites.add(p);
				p.parent = this;
			}
		}
	}
	
	/**
	 * The object's database name.
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * The parent object's database name.
	 */
	public String getParentName()
	{
		return parentName;
	}
	
	/**
	 * The parent object this one orbits.
	 */
	public Planet getParent()
	{
		return parent;
	}
	
	/**
	 * The list of objects that orbit this one.
	 */
	public ArrayList<Planet> getSatellites()
	{
		return satellites;
	}
	
	/**
	 * The string id of the dimension to be used as this object's surface.
	 */
	public String getDimension()
	{
		return dimension;
	}
	
	/**
	 * The satellite level value of this object. 0 = Star, 1 = Planet, 2 = Moon, 3 = Sub-Satellite, etc...
	 */
	public int getSatelliteLevel()
	{
		return satelliteLevel;
	}
	
	/**
	 * Surface gravity of this object as a multiplier of Earth's surface gravity.
	 */
	public double getSurfaceGravity()
	{
		return surfaceGravity;
	}
	
	/**
	 * Radius of this object in meters.
	 */
	public double getRadius()
	{
		return radius;
	}
	
	/**
	 * Obliquity of this object's rotation to the ecliptic.
	 */
	public double getObliquity()
	{
		return obliquity;
	}
	
	/**
	 * Periapsis of this object's orbit in meters.
	 */
	public double getPeriapsis()
	{
		return periapsis;
	}
	
	/**
	 * Apoapsis of this object's orbit in meters.
	 */
	public double getApoapsis()
	{
		return apoapsis;
	}
	
	/**
	 * Argument of periapsis in radians.
	 */
	public double getArgumentOfPeriapsis()
	{
		return argumentOfPeriapsis;
	}
	
	/**
	 * True anomaly in radians.
	 */
	public double getTrueAnomaly()
	{
		return trueAnomaly;
	}
	
	/**
	 * Ascending node in radians.
	 */
	public double getAscendingNode()
	{
		return ascendingNode;
	}
	
	/**
	 * Inclination in radians.
	 */
	public double getInclination()
	{
		return inclination;
	}
	
	/**
	 * Precession of this object's rotation.
	 */
	public double getPrecession()
	{
		return precession;
	}
	
	/**
	 * Get the simulated velocity of this object measured in meters per second.
	 */
	public Vec3d getVelocity()
	{
		return velocity;
	}
	
	/**
	 * Set the simulated position of this object measured in meters per second.
	 */
	public void setVelocity(Vec3d velocity_)
	{
		velocity = velocity_;
	}
	
	/**
	 * Set the simulated position of this object relative to its parent object measured in meters.
	 */
	public void setPosition(Vec3d position_)
	{
		position = position_;
	}
	
	/**
	 * Get the simulated absolute position of this object measured in meters.
	 */
	public Vec3d getPosition()
	{
		Vec3d absolutePosition = position;
		Planet p = parent;
		
		for(int i = satelliteLevel; i > 0; i--)
		{
			absolutePosition = absolutePosition.add(p.getPosition());
			p = p.parent;
		}
		
		return absolutePosition;
	}
	
	public void setRotation(double rotation_)
	{
		rotation = rotation_;
	}
	
	public double getRotation()
	{
		return rotation;
	}
	
	public void setParkingOrbitAngle(double parkingOrbitAngle_)
	{
		parkingOrbitAngle = parkingOrbitAngle_;
	}
	
	public double getParkingOrbitAngle()
	{
		return parkingOrbitAngle;
	}
	
	/**
	 * Get the simulated position of this object's surface viewpoint measured in meters.
	 */
	public Vec3d getSurfaceViewpoint()
	{
		return surfaceViewpoint;
	}
	
	/**
	 * Get the simulated position of this object's parking orbit viewpoint measured in meters.
	 */
	public Vec3d getParkingOrbitViewpoint()
	{
		return parkingOrbitViewpoint;
	}
	
	/**
	 * Get the temperature category of this object at the given sky angle.
	 */
	/*public int getTemperatureCategory(float skyAngle, boolean inOrbit)
	{
		//if(temperatureCategory >= TEMPERATE && (inOrbit || (getSurface() != null && getSurface().getPressure() < 0.01)))
		//	return skyAngle < 0.25f || skyAngle > 0.75f ? HOT : COLD;
		
		//System.out.println(temperatureCategory);
		
		return getSurface().getTemperatureCategory();
	}*/
	
	public double getSurfacePressure()
	{
		if(surface != null)
			return surface.getPressure();
		else
			return 1.0;
	}
	
	public boolean hasLowClouds()
	{
		if(surface != null)
			return surface.hasLowClouds();
		else
			return true;
	}
	
	public boolean hasCloudCover()
	{
		if(surface != null)
			return surface.isCloudy();
		else
			return false;
	}
	
	public boolean hasWeather()
	{
		if(surface != null)
			return surface.hasWeather();
		else
			return true;
	}
	
	public boolean hasSimpleTexture()
	{
		return simpleTexture;
	}
	
	public boolean drawClouds()
	{
		return drawClouds;
	}
	
	public double getCloudRotation()
	{
		return cloudRotation;
	}
	
	public int getCloudLevel()
	{
		return cloudLevel;
	}
	
	/**
	 * Get the multiplier for solar panel power depending on distance to the sun.
	 */
	public double getSolarMultiplier()
	{
		double d = getPosition().lengthSquared();
		
		if(d == 0.0)
			return 0.0;
		
		d /= 2.238016e22; // Convert the distance from meters to astronomical units.
		return (1.0 / d);
	}
	
	/**
	 * Get the interpolated position of this object measured in meters.
	 * Used for rendering on the client side.
	 */
	public Vec3d getInterpolatedPosition(float partialTicks)
	{
		double x = MathHelper.lerp(partialTicks, positionPrevious.getX(), position.getX());
		double y = MathHelper.lerp(partialTicks, positionPrevious.getY(), position.getY());
		double z = MathHelper.lerp(partialTicks, positionPrevious.getZ(), position.getZ());
		Vec3d absolutePosition = new Vec3d(x, y, z);
		Planet p = parent;
		
		for(int i = satelliteLevel; i > 0; i--)
		{
			absolutePosition = absolutePosition.add(p.getInterpolatedPosition(partialTicks));
			p = p.parent;
		}
		
		return absolutePosition;
	}
	
	/**
	 * Get the interpolated position of this object's surface viewpoint measured in meters.
	 * Used for rendering on the client side.
	 */
	public Vec3d getInterpolatedSurfaceViewpoint(float partialTicks)
	{
		double x = MathHelper.lerp(partialTicks, surfaceViewpointPrevious.getX(), surfaceViewpoint.getX());
		double y = MathHelper.lerp(partialTicks, surfaceViewpointPrevious.getY(), surfaceViewpoint.getY());
		double z = MathHelper.lerp(partialTicks, surfaceViewpointPrevious.getZ(), surfaceViewpoint.getZ());
		return new Vec3d(x, y, z);
	}
	
	/**
	 * Get the interpolated position of this object's parking orbit viewpoint measured in meters.
	 * Used for rendering on the client side.
	 */
	public Vec3d getInterpolatedParkingOrbitViewpoint(float partialTicks)
	{
		double x = MathHelper.lerp(partialTicks, parkingOrbitViewpointPrevious.getX(), parkingOrbitViewpoint.getX());
		double y = MathHelper.lerp(partialTicks, parkingOrbitViewpointPrevious.getY(), parkingOrbitViewpoint.getY());
		double z = MathHelper.lerp(partialTicks, parkingOrbitViewpointPrevious.getZ(), parkingOrbitViewpoint.getZ());
		return new Vec3d(x, y, z);
	}
	
	/**
	 * Get the angle in radians between this object's viewpoint and the sun. Used for overriding the world's time of day.
	 */
	public double getSunAngleXZ(boolean fromOrbit)
	{
		Vec3d starPosition = new Vec3d(0.0, 0.0, 0.0);
		Vec3d absolutePosition = getPosition();
		Vec3d viewpoint = fromOrbit ? parkingOrbitViewpoint : surfaceViewpoint;
		double azimuthOfViewpoint = Math.atan2(viewpoint.getZ() - absolutePosition.getZ(), viewpoint.getX() - absolutePosition.getX());
		double azimuthOfStar = Math.atan2(starPosition.getZ() - absolutePosition.getZ(), starPosition.getX() - absolutePosition.getX());
		double trueAzimuth = azimuthOfViewpoint - azimuthOfStar;
		
		if(trueAzimuth < 0.0)
			trueAzimuth += Math.PI * 2.0;
		else if(trueAzimuth > Math.PI * 2.0)
			trueAzimuth -= Math.PI * 2.0;
		
		return trueAzimuth;
	}
	
	public Vec3d getRelativePositionAtTrueAnomaly(double ta)
	{
		double ecc = (apoapsis - periapsis) / (apoapsis + periapsis); // Eccentricity
		double sma = (periapsis + apoapsis) / 2.0; // Semi-Major Axis
		double r = (sma * (1.0 - (ecc * ecc))) / (1.0 + (ecc * Math.cos(ta)));
		Vec3d lanAxis = new Vec3d(1.0, 0.0, 0.0).rotateY((float) ascendingNode); // Longitude of Ascending Node Axis
		Vec3d newPosition = new Vec3d(lanAxis.getX(), lanAxis.getY(), lanAxis.getZ()).rotateY((float) (argumentOfPeriapsis + ta));
		newPosition = VectorUtil.rotateAboutAxis(newPosition, lanAxis, inclination).multiply(r);
		return newPosition;
	}
	
	/**
	 * Set the initial position and velocity of this object such that the orbit matches given parameters. Then set these values for each satellite object.
	 */
	public void setInitialPositionAndVelocity(ArrayList<String> checkList)
	{
		if(checkList.contains(name))
			return;
		
		if(satelliteLevel > 0)
		{
			double sma = (periapsis + apoapsis) / 2.0; // Semi-Major Axis
			Vec3d relativePosition = getRelativePositionAtTrueAnomaly(trueAnomaly);
			Vec3d relativePositionStep = getRelativePositionAtTrueAnomaly(trueAnomaly - 1e-4);
			Vec3d tangent = relativePositionStep.subtract(relativePosition).normalize();
			double initialSpeed = Math.sqrt(G * parent.mass * ((2.0 / relativePosition.length()) - (1.0 / sma)));
			position = relativePosition;
			velocity = tangent.multiply(initialSpeed);
			checkList.add(name);
		}
		
		if(satellites.isEmpty())
			return;
		
		for(Planet p : satellites)
			p.setInitialPositionAndVelocity(checkList);
	}
	
	/**
	 * Calculate the acceleration of this object due to it's parent object's gravity.
	 */
	public void simulateGravityAcceleration()
	{
		if(satelliteLevel < 1)
			return;
		
		acceleration = new Vec3d(0.0, 0.0, 0.0);
		double squaredDistance = position.getX() * position.getX() + position.getY() * position.getY() + position.getZ() * position.getZ();
		double accelerationMagnitude = (G * parent.mass) / squaredDistance;
		acceleration = acceleration.add(position.normalize().multiply(-accelerationMagnitude));
	}
	
	/**
	 * Calculate the change in velocity of this object due to its acceleration over a given time interval.
	 */
	public void simulateVelocityChange(double timeStep)
	{
		if(satelliteLevel < 1)
			return;
		
		velocity = velocity.add(acceleration.multiply(timeStep));
	}
	
	/**
	 * Calculate the change in position and rotation of this object and its viewpoints over a given time interval.
	 */
	public void simulatePositionAndRotationChange(double timeStep)
	{
		if(satelliteLevel > 0)
			position = position.add(velocity.multiply(timeStep));
		
		rotation -= rotationRate * timeStep;
		
		if(rotation <= 0.0)
			rotation += 2.0 * Math.PI;
		
		precession -= precessionRate * timeStep;
		
		if(precession <= 2.0 * Math.PI)
			precession += 2.0 * Math.PI;
		
		parkingOrbitAngle -= parkingOrbitAngularSpeed * timeStep;
		
		if(parkingOrbitAngle <= 2.0 * Math.PI)
			parkingOrbitAngle += 2.0 * Math.PI;
		
		// Update the clouds animation if this planet has one.
		if(drawClouds)
		{
			if(cloudTimer <= 0)
			{
				Random random = Random.createLocal();
				cloudLevel = random.nextInt(4);
				cloudRotation = Math.PI * 2.0 * random.nextDouble();
				cloudTimer = 6000 + random.nextInt(3000);
			}
			else
				cloudTimer--;
			
			cloudRotation += cloudRotationRate * timeStep;
			
			if(cloudRotation >= 2.0 * Math.PI)
				cloudRotation -= 2.0 * Math.PI;
		}
		
		updateViewpoints();
	}
	
	private void updateViewpoints()
	{
		Vec3d absolutePosition = getPosition();
		Vec3d axisOfRotation = new Vec3d(0.0, 1.0, 0.0);
		axisOfRotation = axisOfRotation.rotateX((float) obliquity);
		axisOfRotation = axisOfRotation.rotateY((float) precession);
		surfaceViewpoint = absolutePosition.add(VectorUtil.rotateAboutAxis(new Vec3d(1.0, 0.0, 0.0), axisOfRotation, rotation).multiply(radius));
		parkingOrbitViewpoint = absolutePosition.add(VectorUtil.rotateAboutAxis(new Vec3d(1.0, 0.0, 0.0), axisOfRotation, parkingOrbitAngle).multiply(radius + lowOrbitAltitude));
		
		if(isTidallyLocked)
			surfaceViewpoint = absolutePosition.add(parent.getPosition().subtract(absolutePosition).normalize().multiply(radius));
		
		// Update sky angle variables.
		sunAngle = getSunAngleXZ(false);
		sunAngleOrbit = getSunAngleXZ(true);
	}
	
	public void movePreviousPositions()
	{
		positionPrevious = position;
		surfaceViewpointPrevious = surfaceViewpoint;
		parkingOrbitViewpointPrevious = parkingOrbitViewpoint;
	}
	
	/**
	 * Calculate the delta-v needed to reach the parking orbit of this planet from its surface.
	 */
	public double dVSurfaceToOrbit()
	{
		double pressure = getSurface() != null ? getSurface().getPressure() : 0.0;
		return (circularOrbitVelocity(radius + lowOrbitAltitude) * 1.08) + (1000.0 * Math.pow(pressure, 7.0 / 11.0));
	}
	
	/**
	 * Calculate the delta-v needed to reach the parking orbit of this planet from its sky.
	 */
	public double dVSkyToOrbit()
	{
		double pressure = getSky() != null ? getSky().getPressure() : 0.0;
		return (circularOrbitVelocity(radius + lowOrbitAltitude) * 1.08) + (1000.0 * Math.pow(pressure, 7.0 / 11.0));
	}
	
	/**
	 * Calculate the delta-v needed to land on the surface of this planet from its parking orbit.
	 */
	public double dVOrbitToSurface()
	{
		if(getSurface() != null && getSurface().getPressure() > 0.001)
			return 250.0;
		
		return dVSurfaceToOrbit();
	}
	
	/**
	 * Calculate the delta-v needed to move from an orbit of one distance to this planet's center to another following a basic Hohmann transfer.
	 * Start and end velocity offsets are non-zero in the case of transfers between orbiting planets.
	 */
	public double dVTransfer(double r1, double r2, double esc1, double esc2)
	{
		double gm = G * mass;
		double a = 2.0 / (r1 + r2);
		double vc1 = Math.sqrt(gm / r1);
		double vc2 = Math.sqrt(gm / r2);
		double ve1 = Math.sqrt(gm * ((2.0 / r1) - a));
		double ve2 = Math.sqrt(gm * ((2.0 / r2) - a));
		double dvf1 = Math.sqrt(Math.pow(ve1 - vc1, 2.0) + Math.pow(esc1, 2.0)) - esc1;
		double dvf2 = Math.sqrt(Math.pow(ve2 - vc2, 2.0) + Math.pow(esc2, 2.0)) - esc2;
		return dvf1 + dvf2;
	}
	
	/**
	 * Get the velocity of any object orbiting this planet at the given distance from its center.
	 */
	public double circularOrbitVelocity(double r)
	{
		return Math.sqrt((G * mass) / r);
	}
	
	/**
	 * Get escape velocity of this planet at the given distance from its center.
	 */
	public double escapeVelocity(double r)
	{
		return Math.sqrt((2.0 * G * mass) / r);
	}
	
	/**
	 * Recursively search through all of this planet's satellite's and sub-satellite's for the specified planet.
	 */
	public boolean recursiveSearch(Planet target, ArrayList<Planet> searchList)
	{
		boolean b = false;
		
		for(Planet s : getSatellites())
		{
			if(s.equals(target))
				b = true;
			else if(!s.satellites.isEmpty())
				b = s.recursiveSearch(target, searchList);
			
			if(b)
			{
				searchList.add(s);
				break;
			}
		}
		
		return b;
	}
	
	/**
	 * Find the amount of delta-v required for travel from a parking orbit around this planet to another.
	 */
	public double dVToPlanet(Planet other)
	{
		double esc1 = 0;
		double dvEsc1 = 0;
		double startV1 = circularOrbitVelocity(radius + lowOrbitAltitude);
		double orbitRadius = radius + lowOrbitAltitude;
		Planet p = this;
		
		while(true)
		{
			if(p.equals(other))
				return p.dVTransfer(orbitRadius, p.radius + p.lowOrbitAltitude, esc1, 0) + dvEsc1;
			
			ArrayList<Planet> searchList = new ArrayList<Planet>();
			boolean b = p.recursiveSearch(other, searchList);
			
			if(b)
			{
				
				double esc2 = 0;
				double dvEsc2 = 0;
				double startV2 = searchList.get(0).circularOrbitVelocity(searchList.get(0).radius + searchList.get(0).lowOrbitAltitude);
				
				for(int i = 0; i < searchList.size(); i++)
				{
					if(i == 0)
					{
						esc2 = searchList.get(i).escapeVelocity(searchList.get(i).radius + searchList.get(i).lowOrbitAltitude);
						
						if(esc2 > startV2)
							dvEsc2 += esc2 - startV2;
						else
							esc2 += startV2 - esc2;
						
						startV2 = searchList.get(i).parent.circularOrbitVelocity((searchList.get(i).periapsis + searchList.get(i).apoapsis) / 2.0) + esc2;
						continue;
					}
					
					Planet s = searchList.get(i - 1);
					esc2 = searchList.get(i).escapeVelocity((s.periapsis + s.apoapsis) / 2.0);
					
					if(esc2 > startV2)
						dvEsc2 += esc2 - startV2;
					else
						esc2 += startV2 - esc2;
					
					startV2 = searchList.get(i).parent.circularOrbitVelocity((searchList.get(i).periapsis + searchList.get(i).apoapsis) / 2.0) + esc2;
				}
				
				Planet next = searchList.get(searchList.size() - 1);
				double rEnd = (next.periapsis + next.apoapsis) / 2.0;
				return p.dVTransfer(orbitRadius, rEnd, esc1, esc2) + dvEsc1 + dvEsc2;
			}
			else if(p.parent != null)
			{
				esc1 = p.escapeVelocity(orbitRadius);
				
				if(esc1 > startV1)
					dvEsc1 += esc1 - startV1;
				else
					esc1 += startV1 - esc1;
				
				orbitRadius = (p.periapsis + p.apoapsis) / 2.0;
				startV1 = p.parent.circularOrbitVelocity(orbitRadius) + esc1;
				p = p.parent;
			}
		}
	}
	
	public StaticData getStaticData()
	{
		HashMap<String, PlanetDimensionData> dimensionDataMap = new HashMap<String, PlanetDimensionData>();
		
		if(orbit != null)
			dimensionDataMap.put("orbit", orbit);
		
		if(surface != null)
			dimensionDataMap.put("surface", surface);
		
		if(sky != null)
			dimensionDataMap.put("sky", sky);
		
		return new StaticData(name, parentName, mass, radius, lowOrbitAltitude, periapsis, apoapsis, argumentOfPeriapsis, trueAnomaly, ascendingNode, inclination, isTidallyLocked, obliquity, rotationRate, simpleTexture, drawClouds, cloudRotationRate, dimensionDataMap);
	}
	
	public void readStaticData(StaticData data)
	{
		name = data.name();
		parentName = data.parentName();
		mass = data.mass();
		radius = data.radius();
		lowOrbitAltitude = data.lowOrbitAltitude();
		periapsis = data.periapsis();
		apoapsis = data.apoapsis();
		argumentOfPeriapsis = data.argumentOfPeriapsis();
		trueAnomaly = data.trueAnomaly();
		ascendingNode = data.ascendingNode();
		inclination = data.inclination();
		isTidallyLocked = data.isTidallyLocked();
		obliquity = data.obliquity();
		rotationRate = data.rotationRate();
		simpleTexture = data.simpleTexture();
		drawClouds = data.simpleTexture();
		cloudRotationRate = data.cloudRotationRate();
		HashMap<String, PlanetDimensionData> dimensionDataMap = data.dimensionDataMap();
		
		if(dimensionDataMap.containsKey("orbit"))
			orbit = dimensionDataMap.get("orbit");
		
		if(dimensionDataMap.containsKey("surface"))
			surface = dimensionDataMap.get("surface");
		
		if(dimensionDataMap.containsKey("sky"))
			sky = dimensionDataMap.get("sky");
	}
	
	public DynamicData getDynamicData()
	{
		return new DynamicData(position, velocity, rotation, precession, parkingOrbitAngle, cloudRotation, cloudLevel, cloudTimer);
	}
	
	public void readDynamicData(DynamicData data)
	{
		position = data.position();
		velocity = data.velocity();
		rotation = data.rotation();
		precession = data.precession();
		parkingOrbitAngle = data.parkingOrbitAngle();
		cloudRotation = data.cloudRotation();
		cloudLevel = data.cloudLevel();
		cloudTimer = data.cloudTimer();
		updateViewpoints();
	}
	
	public DataCompound saveData(DataCompound data)
	{
		DataCompound planetData = new DataCompound();
		planetData.setValue("positionX", position.getX());
		planetData.setValue("positionY", position.getY());
		planetData.setValue("positionZ", position.getZ());
		planetData.setValue("velocityX", velocity.getX());
		planetData.setValue("velocityY", velocity.getY());
		planetData.setValue("velocityZ", velocity.getZ());
		planetData.setValue("rotation", rotation);
		planetData.setValue("precession", precession);
		planetData.setValue("parkingOrbitAngle", parkingOrbitAngle);
		planetData.setValue("cloudRotation", cloudRotation);
		planetData.setValue("cloudLevel", cloudLevel);
		planetData.setValue("cloudTimer", cloudTimer);
		data.setValue(name, planetData);
		return data;
	}
	
	public void loadData(DataCompound data, ArrayList<String> checkList)
	{
		if(!data.hasName(name) || checkList.contains(name))
			return;
			
		DataCompound planetData = data.getDataCompound(name);
		position = new Vec3d(planetData.getDouble("positionX"), planetData.getDouble("positionY"), planetData.getDouble("positionZ"));
		velocity = new Vec3d(planetData.getDouble("velocityX"), planetData.getDouble("velocityY"), planetData.getDouble("velocityZ"));
		rotation = planetData.getDouble("rotation");
		precession = planetData.getDouble("precession");
		parkingOrbitAngle = planetData.getDouble("parkingOrbitAngle");
		cloudRotation = planetData.getDouble("cloudRotation");
		cloudLevel = planetData.getInt("cloudLevel");
		cloudTimer = planetData.getInt("cloudTimer");
		checkList.add(name);
		
		if(satellites.isEmpty())
			return;
		
		for(Planet p : satellites)
			p.loadData(data, checkList);
	}
	
	public static void writeStaticData(PacketByteBuf buffer, StaticData data)
	{
		buffer.writeString(data.name());
		buffer.writeString(data.parentName());
		buffer.writeDouble(data.mass());
		buffer.writeDouble(data.radius());
		buffer.writeDouble(data.lowOrbitAltitude());
		buffer.writeDouble(data.periapsis());
		buffer.writeDouble(data.apoapsis());
		buffer.writeDouble(data.argumentOfPeriapsis());
		buffer.writeDouble(data.trueAnomaly());
		buffer.writeDouble(data.ascendingNode());
		buffer.writeDouble(data.inclination());
		buffer.writeBoolean(data.isTidallyLocked());
		buffer.writeDouble(data.obliquity());
		buffer.writeDouble(data.rotationRate());
		buffer.writeBoolean(data.simpleTexture());
		buffer.writeBoolean(data.drawClouds());
		buffer.writeDouble(data.cloudRotationRate());
		buffer.writeMap(data.dimensionDataMap(), PacketByteBuf::writeString, PlanetDimensionData::write);
	}
	
	public static void writeDynamicData(PacketByteBuf buffer, DynamicData data)
	{
		buffer.writeVec3d(data.position());
		buffer.writeVec3d(data.velocity());
		buffer.writeDouble(data.rotation());
		buffer.writeDouble(data.precession());
		buffer.writeDouble(data.parkingOrbitAngle());
		buffer.writeDouble(data.cloudRotation());
		buffer.writeInt(data.cloudLevel());
		buffer.writeInt(data.cloudTimer());
	}
	
	public record StaticData(String name, String parentName, double mass, double radius, double lowOrbitAltitude, double periapsis, double apoapsis, double argumentOfPeriapsis, double trueAnomaly, double ascendingNode, double inclination, boolean isTidallyLocked, double obliquity, double rotationRate, boolean simpleTexture, boolean drawClouds, double cloudRotationRate, HashMap<String, PlanetDimensionData> dimensionDataMap)
	{
		public StaticData(PacketByteBuf buffer)
	    {
	    	this(buffer.readString(), buffer.readString(), buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readBoolean(), buffer.readDouble(), buffer.readDouble(), buffer.readBoolean(), buffer.readBoolean(), buffer.readDouble(), (HashMap<String, PlanetDimensionData>) buffer.readMap(PacketByteBuf::readString, PlanetDimensionData::new));
	    }
	}
	
	public record DynamicData(Vec3d position, Vec3d velocity, double rotation, double precession, double parkingOrbitAngle, double cloudRotation, int cloudLevel, int cloudTimer)
	{
		public DynamicData(PacketByteBuf buffer)
	    {
	    	this(buffer.readVec3d(), buffer.readVec3d(), buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readInt(), buffer.readInt());
	    }
	}
}