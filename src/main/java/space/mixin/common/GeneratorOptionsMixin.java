package space.mixin.common;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.util.registry.Registry;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.gen.GeneratorOptions;

@Mixin(GeneratorOptions.class)
public abstract class GeneratorOptionsMixin extends GeneratorOptions
{
	@Shadow long seed;
	
	public GeneratorOptionsMixin(long seed, boolean generateStructures, boolean bonusChest, Registry<DimensionOptions> options)
	{
		super(seed, generateStructures, bonusChest, options);
	}

	@Inject(method = "<init>", at = @At("HEAD"))
	private void init(long seed, boolean generateStructures, boolean bonusChest, Registry<DimensionOptions> options, Optional<String> legacyCustomOptions, CallbackInfo info)
	{
		
	}
}
