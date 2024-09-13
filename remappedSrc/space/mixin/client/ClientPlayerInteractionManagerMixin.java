package space.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import space.block.StarflightBlocks;
import space.item.StarflightItems;
import space.planet.PlanetDimensionData;
import space.planet.PlanetList;
import space.util.AirUtil;

@Environment(value=EnvType.CLIENT)
@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin
{
	@Inject(method = "interactBlock(Lnet/minecraft/client/network/ClientPlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;", at = @At(value = "HEAD"), cancellable = true)
	private void interactBlockInject(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> info)
	{
		World world = player.method_48926();
		PlanetDimensionData data = PlanetList.getDimensionDataForWorld(world);
		ItemStack stack = player.getStackInHand(hand);
		
		if(data != null && data.overridePhysics() && !AirUtil.canEntityBreathe(player, data))
		{
			MutableText text = null;
			
			if(stack.isIn(StarflightItems.COMBUSTION_ITEM_TAG))
				text = Text.translatable("item.space.combustion.message");
			else if(stack.getItem() instanceof BlockItem)
			{
				Block block = ((BlockItem) stack.getItem()).getBlock();
				
				if(block.getDefaultState().isIn(StarflightBlocks.INSTANT_REMOVE_TAG))
					text = Text.translatable("item.space.placement.message");
			}
				
			if(text != null)
			{
				player.sendMessage(text, false);
				info.setReturnValue(ActionResult.FAIL);
				info.cancel();
			}
		}
	}
}