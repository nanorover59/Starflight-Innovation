package space.mixin.client;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.client.gl.PostEffectProcessor;

@Mixin(PostEffectProcessor.class)
public interface PostEffectProcessorAccessorMixin
{
	@Accessor("passes")
	List<PostEffectPass> getPasses();
}