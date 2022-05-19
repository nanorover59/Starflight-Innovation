package space.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.block.BlockState;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.GenerationShapeConfig;
import net.minecraft.world.gen.noise.SimpleNoiseRouter;
import net.minecraft.world.gen.surfacebuilder.MaterialRules;

@Mixin(ChunkGeneratorSettings.class)
public interface ChunkGeneratorSettingsMixin
{
	@Invoker("<init>")
	static ChunkGeneratorSettings init(GenerationShapeConfig generationShapeConfig, BlockState defaultBlock, BlockState defaultFluid, SimpleNoiseRouter noiseRouter, MaterialRules.MaterialRule surfaceRule, int seaLevel, boolean mobGenerationDisabled, boolean aquifers, boolean oreVeins, boolean usesLegacyRandom)
	{
		return null;
	}
}