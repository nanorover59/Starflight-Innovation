package space.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import space.planet.Planet;
import space.planet.PlanetList;
import space.util.AirUtil;

@Mixin(CampfireBlock.class)
public class CampfireBlockMixin
{
	@Inject(method = "getPlacementState(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/block/BlockState;", at = @At("HEAD"), cancellable = true)
	public void getPlacementStateInject(ItemPlacementContext context, CallbackInfoReturnable<BlockState> info)
	{
		World world = context.getWorld();
		Planet planet = PlanetList.getPlanetForWorld(world.getRegistryKey());
		boolean b = AirUtil.getAirResistanceMultiplier(world, planet, context.getBlockPos().up()) != 0.0;
		
		if(planet != null && !planet.hasOxygen() && !b)
		{
			BlockState blockState = Block.getBlockFromItem(context.getStack().getItem()).getDefaultState().with(CampfireBlock.LIT, false);
			CampfireBlock.extinguish(null, world, context.getBlockPos(), blockState);
			info.setReturnValue(blockState);
			info.cancel();
		}
	}
	
	@Inject(method = "getStateForNeighborUpdate(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/Direction;Lnet/minecraft/block/BlockState;Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;", at = @At("HEAD"), cancellable = true)
	public void getStateForNeighborUpdateInject(BlockState state, Direction direction, BlockState neighborState, WorldAccess worldAccess, BlockPos pos, BlockPos neighborPos, CallbackInfoReturnable<BlockState> info)
	{
		BlockEntity blockEntity = worldAccess.getBlockEntity(pos);
		World world = null;
		
		if(blockEntity != null && blockEntity.getWorld() != null)
			world = blockEntity.getWorld();
		
		if(world != null)
		{
			Planet planet = PlanetList.getPlanetForWorld(world.getRegistryKey());
			boolean b = AirUtil.getAirResistanceMultiplier(world, planet, pos.up()) != 0.0;
			
			if(planet != null && !planet.hasOxygen() && !b)
			{
				CampfireBlock.extinguish(null, world, pos, state);
				info.setReturnValue(state.with(CampfireBlock.LIT, false));
				info.cancel();
			}
		}
	}
}