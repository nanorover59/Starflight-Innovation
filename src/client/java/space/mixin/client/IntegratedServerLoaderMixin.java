package space.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.integrated.IntegratedServerLoader;

@Environment(value=EnvType.CLIENT)
@Mixin(IntegratedServerLoader.class)
public abstract class IntegratedServerLoaderMixin
{
	@ModifyVariable(method = "start(Lnet/minecraft/world/level/storage/LevelStorage$Session;Lcom/mojang/serialization/Dynamic;ZZLjava/lang/Runnable;)V", at = @At("HEAD"), index = 4, argsOnly = true)
	private boolean modifyCanShowBackupPrompt(boolean b)
	{
		return false;
	}
	
	@ModifyVariable(method = "tryLoad(Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/gui/screen/world/CreateWorldScreen;Lcom/mojang/serialization/Lifecycle;Ljava/lang/Runnable;Z)V", at = @At("HEAD"), index = 4, argsOnly = true)
	private static boolean modifyBypassWarnings(boolean b)
	{
		return true;
	}
}