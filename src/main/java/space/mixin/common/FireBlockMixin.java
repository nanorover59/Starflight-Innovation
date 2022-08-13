package space.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.block.Block;
import net.minecraft.block.FireBlock;

@Mixin(FireBlock.class)
public interface FireBlockMixin
{
	@Invoker()
	public void callRegisterFlammableBlock(Block block, int burnChance, int spreadChance);
}