package space.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.option.KeyBinding;

@Environment(value=EnvType.CLIENT)
@Mixin(KeyBinding.class)
public interface KeyBindingMixin
{
	@Invoker()
	public void callReset();
}