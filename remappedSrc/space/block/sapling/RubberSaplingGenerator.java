package space.block.sapling;

import java.util.Random;

import net.minecraft.block.sapling.SaplingGenerator;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import space.world.StarflightWorldGeneration;

public class RubberSaplingGenerator extends SaplingGenerator
{
	@Override
	protected RegistryEntry<? extends ConfiguredFeature<?, ?>> getTreeFeature(Random random, boolean bees)
	{
		return random.nextInt(4) == 0 ? StarflightWorldGeneration.TALL_RUBBER_TREE : StarflightWorldGeneration.RUBBER_TREE;
	}
}