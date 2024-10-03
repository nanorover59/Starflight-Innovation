package space.util;

import java.util.Random;

public class PerlinNoise
{
	private static final int PERMUTATION_SIZE = 256;
	private final int[] permutation;
	
	public PerlinNoise(long ... seeds)
	{
		permutation = new int[PERMUTATION_SIZE * 2]; // Double the array to avoid overflow.
		Random random = new Random(combineSeeds());

		// Fill permutation array with values 0 to 255 and shuffle.
		int[] perm = new int[PERMUTATION_SIZE];
		
		for(int i = 0; i < PERMUTATION_SIZE; i++)
			perm[i] = i;
		
		for(int i = PERMUTATION_SIZE - 1; i >= 0; i--)
		{
			int j = random.nextInt(i + 1);
			int swap = perm[i];
			perm[i] = perm[j];
			perm[j] = swap;
		}

		// Copy the permutation array twice for ease of overflow handling.
		for(int i = 0; i < PERMUTATION_SIZE; i++)
		{
			permutation[i] = perm[i];
			permutation[PERMUTATION_SIZE + i] = perm[i];
		}
	}
	
	public double get(double x, double z)
	{
		// Determine grid cell coordinates.
		int x0 = (int) Math.floor(x) & 255;
		int z0 = (int) Math.floor(z) & 255;
		int x1 = (x0 + 1) & 255;
		int z1 = (z0 + 1) & 255;

		// Relative coordinates within the grid cell.
		double xf = x - Math.floor(x);
		double zf = z - Math.floor(z);

		// Fade curves for X and Z.
		double u = fade(xf);
		double v = fade(zf);

		// Hash coordinates of the 4 grid corners.
		int aa = permutation[permutation[x0] + z0];
		int ab = permutation[permutation[x0] + z1];
		int ba = permutation[permutation[x1] + z0];
		int bb = permutation[permutation[x1] + z1];

		// Calculate the contribution from each corner.
		double gradAA = grad(aa, xf, zf);
		double gradBA = grad(ba, xf - 1.0, zf);
		double gradAB = grad(ab, xf, zf - 1.0);
		double gradBB = grad(bb, xf - 1.0, zf - 1.0);

		// Linearly interpolate between the corner contributions.
		double lerpX1 = lerp(u, gradAA, gradBA);
		double lerpX2 = lerp(u, gradAB, gradBB);
		return lerp(v, lerpX1, lerpX2); // Final interpolation along Z.
	}

	private double fade(double t)
	{
		return t * t * t * (t * (t * 6 - 15.0) + 10.0);
	}

	private double lerp(double t, double a, double b)
	{
		return a + t * (b - a);
	}

	// Compute dot product between random gradient and distance vector.
	private double grad(int hash, double x, double z)
	{
		int h = hash & 15; // Take the lower 4 bits of hash code.
		double u = (h < 8 || h == 12 || h == 13) ? x : z;
		double v = (h < 4 || h == 12 || h == 13) ? z : x;
		return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
	}
	
	private long combineSeeds(long ... seeds)
	{
		long mix = 0;
		
		for(int i = 0; i < seeds.length; i++)
		{
			long seed = seeds[i];
			seed ^= (seed << 21);
			seed ^= (seed >>> 35);
			seed ^= (seed << 4);
			mix += seed;
		}
		
		mix ^= (mix << 21);
		mix ^= (mix >>> 35);
		mix ^= (mix << 4);
		return mix;
	}
}