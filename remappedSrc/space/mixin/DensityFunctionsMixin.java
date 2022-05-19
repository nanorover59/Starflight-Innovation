package space.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.gen.chunk.GenerationShapeConfig;
import net.minecraft.world.gen.densityfunction.DensityFunctions;
import net.minecraft.world.gen.noise.SimpleNoiseRouter;

@Mixin(DensityFunctions.class)
public interface DensityFunctionsMixin
{
	@Invoker("method_41209")
	public static SimpleNoiseRouter invokeMethod_41209(GenerationShapeConfig generationShapeConfig, boolean bl)
	{
		return null;
	}
}
