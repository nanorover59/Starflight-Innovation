package space.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.gen.feature.ConfiguredStructureFeature;
import net.minecraft.world.gen.feature.ConfiguredStructureFeatures;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.StructureFeature;

@Mixin(ConfiguredStructureFeatures.class)
public interface ConfiguredStructureFeaturesMixin
{
	@Invoker("register")
	public static <FC extends FeatureConfig, F extends StructureFeature<FC>> RegistryEntry<ConfiguredStructureFeature<?, ?>> invokeRegister(RegistryKey<ConfiguredStructureFeature<?, ?>> key, ConfiguredStructureFeature<FC, F> configuredStructureFeature)
	{
		return null;
	}
}