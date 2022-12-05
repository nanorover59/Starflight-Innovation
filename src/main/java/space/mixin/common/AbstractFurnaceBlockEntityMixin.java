package space.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import space.item.StarflightItems;
import space.planet.PlanetDimensionData;
import space.planet.PlanetList;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin
{
	/**
	 * Ensure that furnaces only work in the correct atmosphere.
	 */
	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	private static void tickInject(World world, BlockPos pos, BlockState state, AbstractFurnaceBlockEntity blockEntity, CallbackInfo info)
	{
		ItemStack fuel = blockEntity.getStack(1);
		
		if(((AbstractFurnaceBlockEntityAccessorMixin) blockEntity).getBurnTime() <= 1 && !fuel.isEmpty() && !fuel.isIn(StarflightItems.NO_OXYGEN_FUEL_ITEM_TAG))
		{
			PlanetDimensionData data = PlanetList.getDimensionDataForWorld(world);
			
			if((data != null && !data.hasOxygen()) || data.isOrbit())
			{
				((AbstractFurnaceBlockEntityAccessorMixin) blockEntity).setBurnTime(0);
				info.cancel();
			}
		}
	}
}