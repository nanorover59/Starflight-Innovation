package space.world;

import com.mojang.datafixers.Products.P3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;

import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.TestableWorld;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.foliage.FoliagePlacer;
import net.minecraft.world.gen.foliage.FoliagePlacerType;

public class SpikeFoliagePlacer extends FoliagePlacer
{
	public static final MapCodec<SpikeFoliagePlacer> CODEC = RecordCodecBuilder.mapCodec(instance -> createCodec(instance).apply(instance, SpikeFoliagePlacer::new));
	protected final int height;

	protected static <P extends SpikeFoliagePlacer> P3<Mu<P>, IntProvider, IntProvider, Integer> createCodec(Instance<P> builder)
	{
		return fillFoliagePlacerFields(builder).and(Codec.intRange(0, 16).fieldOf("height").forGetter(placer -> placer.height));
	}

	public SpikeFoliagePlacer(IntProvider radius, IntProvider offset, int height)
	{
		super(radius, offset);
		this.height = height;
	}

	@Override
	protected FoliagePlacerType<?> getType()
	{
		return StarflightWorldGeneration.SPIKE_FOLIAGE_PLACER;
	}

	@Override
	protected void generate(TestableWorld world, BlockPlacer placer, Random random, TreeFeatureConfig config, int trunkHeight, TreeNode treeNode, int foliageHeight, int radius, int offset)
	{
		for(int i = offset + 1; i >= offset - foliageHeight; i--)
		{
			float layerIndex = Math.max(offset - i, 0);
			float fraction = layerIndex / (float) foliageHeight;
			float taper = (float) Math.pow(fraction, 2.0);
			int j = Math.max((int) Math.ceil((radius + treeNode.getFoliageRadius()) * taper), 0);
			this.generateSquare(world, placer, random, config, treeNode.getCenter(), j, i, treeNode.isGiantTrunk());
		}
	}

	@Override
	public int getRandomHeight(Random random, int trunkHeight, TreeFeatureConfig config)
	{
		return this.height;
	}

	@Override
	protected boolean isInvalidForLeaves(Random random, int dx, int y, int dz, int radius, boolean giantTrunk)
	{
		return dx * dx + dz * dz > radius * radius;
	}
}