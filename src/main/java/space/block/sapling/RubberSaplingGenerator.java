package space.block.sapling;

import net.minecraft.block.sapling.SaplingGenerator;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import space.StarflightMod;

public class RubberSaplingGenerator extends SaplingGenerator
{
	@Override
	protected RegistryKey<ConfiguredFeature<?, ?>> getTreeFeature(Random random, boolean bees)
	{
		return RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE, new Identifier(StarflightMod.MOD_ID, "rubber_tree"));
	}
}