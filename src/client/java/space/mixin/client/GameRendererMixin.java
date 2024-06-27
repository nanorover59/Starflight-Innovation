package space.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Uniform;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import space.client.render.StarflightClientEffects;

@Mixin(GameRenderer.class)
public class GameRendererMixin
{
	@Shadow @Final private MinecraftClient client;
	
	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;drawEntityOutlinesFramebuffer()V", shift = At.Shift.AFTER))
    private void injectAfterDrawEntityOutlinesFramebuffer(RenderTickCounter tickCounter, boolean tick, CallbackInfo info)
	{
		if(StarflightClientEffects.radiationShader != null && StarflightClientEffects.radiation > 0)
		{
			Uniform aberrationStrength = ((PostEffectProcessorAccessorMixin) StarflightClientEffects.radiationShader).getPasses().get(0).getProgram().getUniformByNameOrDummy("Strength");
			Uniform noiseSeed = ((PostEffectProcessorAccessorMixin) StarflightClientEffects.radiationShader).getPasses().get(1).getProgram().getUniformByNameOrDummy("Seed");
			Uniform noiseThreshold = ((PostEffectProcessorAccessorMixin) StarflightClientEffects.radiationShader).getPasses().get(1).getProgram().getUniformByNameOrDummy("Threshold");
			aberrationStrength.set(StarflightClientEffects.radiation);
			noiseSeed.set(client.world.random.nextFloat());
			noiseThreshold.set(1.0f - StarflightClientEffects.radiation * 0.001f);
			StarflightClientEffects.radiationShader.render(tickCounter.getTickDelta(false));
			client.getFramebuffer().beginWrite(false);
		}
    }
}