package space.mixin.common;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ConnectingBlock;
import net.minecraft.block.FireBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import space.block.StarflightBlocks;
import space.planet.Planet;
import space.planet.PlanetList;

@Mixin(FireBlock.class)
public abstract class FireBlockMixin
{
	private static final Map<Direction, BooleanProperty> DIRECTION_PROPERTIES = ConnectingBlock.FACING_PROPERTIES.entrySet().stream().filter(entry -> entry.getKey() != Direction.DOWN).collect(Util.toMap());
	
	@Inject(method = "scheduledTick(Lnet/minecraft/block/BlockState;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/random/Random;)V", at = @At("HEAD"), cancellable = true)
	public void scheduledTickInject(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo info)
	{
		Planet planet = PlanetList.getPlanetForWorld(world.getRegistryKey());
		
		if(planet != null && world.getRegistryKey() != World.OVERWORLD && world.getRegistryKey() != World.NETHER && world.getRegistryKey() != World.END)
		{
			boolean b = false;
			
			for(Direction direction : Direction.values())
			{
				if(world.getBlockState(pos.offset(direction)).getBlock() == StarflightBlocks.HABITABLE_AIR)
				{
					b = true;
					break;
				}
			}
			
			if((PlanetList.isOrbit(world.getRegistryKey()) || !planet.hasOxygen()) && !b)
			{
				world.removeBlock(pos, false);
				info.cancel();
			}
			else if(b && random.nextFloat() < 0.1f)
			{
				BlockPos spreadPos = pos.offset(Direction.values()[random.nextInt(Direction.values().length)]);
				
				int j = random.nextInt(5);
				BlockState fireState = Blocks.FIRE.getDefaultState();
				boolean b2 = false;

				for(Direction direction : Direction.values())
				{
					BooleanProperty booleanProperty = DIRECTION_PROPERTIES.get(direction);
					boolean wall = world.getBlockState(spreadPos.offset(direction)).getMaterial().blocksMovement();
					
					if(wall)
						b2 = true;

					if(booleanProperty == null)
						continue;
					
					fireState = fireState.with(booleanProperty, wall);
				}
				
				if(b2 && world.getBlockState(spreadPos).getBlock() == StarflightBlocks.HABITABLE_AIR)
					world.setBlockState(spreadPos, fireState.with(FireBlock.AGE, j), Block.NOTIFY_ALL);
			}
		}
	}
	
	@Inject(method = "areBlocksAroundFlammable(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)Z", at = @At("HEAD"), cancellable = true)
	public void areBlocksAroundFlammableInject(BlockView world, BlockPos pos, CallbackInfoReturnable<Boolean> info)
	{
		boolean b = false;
		
		for(Direction direction : Direction.values())
		{
			if(world.getBlockState(pos.offset(direction)).getBlock() == StarflightBlocks.HABITABLE_AIR)
			{
				b = true;
				break;
			}
		}
		
		if(b)
		{
			info.setReturnValue(true);
			info.cancel();
		}
	}
}