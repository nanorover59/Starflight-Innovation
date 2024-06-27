package space.item;

import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import space.block.StarflightBlocks;

public class MovingCraftWandItem extends Item
{
	public MovingCraftWandItem(Settings settings)
	{
		super(settings);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand)
	{
        BlockPos pos = player.getBlockPos();
		ItemStack stack = player.getStackInHand(hand);
		NbtCompound nbt = stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt();
		
		if(world.isClient)
			return TypedActionResult.success(stack);
        
		if(!nbt.contains("center"))
			nbt.put("center", NbtHelper.fromBlockPos(pos));
		else if(!nbt.contains("radial"))
			nbt.put("radial", NbtHelper.fromBlockPos(pos));
		else
		{
			BlockPos centerPos = NbtHelper.toBlockPos(nbt, "center").get();
			BlockPos radialPos = NbtHelper.toBlockPos(nbt, "radial").get();
			int baseRadius = (int) Math.hypot(centerPos.getX() - radialPos.getX(), centerPos.getZ() - radialPos.getZ());
			int height = pos.getY() - centerPos.getY();
			buildRocket(world, centerPos, baseRadius, height);
			stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(new NbtCompound()));
			return TypedActionResult.success(stack);
		}
		
		stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
		return TypedActionResult.success(stack);
    }
	
	private void buildRocket(World world, BlockPos centerPos, int baseRadius, int height)
	{
		int yCone = (int) (height * 0.75);
		
		if(yCone % 2 > 0)
			yCone--;
		
		int yBarrier = yCone / 2;
		double sqpi = baseRadius / Math.sqrt(Math.PI);
		
		for(int y = 0; y <= height; y++)
		{
			int radius;
			
			if(y <= yCone)
				radius = baseRadius;
			else
			{
				double theta = Math.acos(1.0 - (2.0 * (height - y)) / (height - yCone));
				radius = (int) Math.round(sqpi * Math.sqrt(theta - Math.sin(theta * 2.0) / 2.0 + (2.0 / 3.0) * Math.pow(Math.sin(theta), 3)));
			}
			
			BlockState blockState;
			
			if(y == 0 || y == yBarrier || y == yCone)
				blockState = StarflightBlocks.RIVETED_ALUMINUM.getDefaultState();
			else
				blockState = StarflightBlocks.STRUCTURAL_ALUMINUM.getDefaultState();
			
			world.setBlockState(centerPos.add(radius, y, 0), blockState);
			world.setBlockState(centerPos.add(-radius, y, 0), blockState);
			world.setBlockState(centerPos.add(0, y, radius), blockState);
			world.setBlockState(centerPos.add(0, y, -radius), blockState);
			
			int x = 1;
			int z = radius;
			
			while(x < z)
			{
				world.setBlockState(centerPos.add(x, y, z), blockState);
				world.setBlockState(centerPos.add(x, y, -z), blockState);
				world.setBlockState(centerPos.add(-x, y, z), blockState);
				world.setBlockState(centerPos.add(-x, y, -z), blockState);
				world.setBlockState(centerPos.add(z, y, x), blockState);
				world.setBlockState(centerPos.add(z, y, -x), blockState);
				world.setBlockState(centerPos.add(-z, y, x), blockState);
				world.setBlockState(centerPos.add(-z, y, -x), blockState);
				z = (int) (Math.sqrt(radius * radius - x * x) + 0.5);
				x++;
			}
			
			if(x == z)
			{
				world.setBlockState(centerPos.add(x, y, z), blockState);
				world.setBlockState(centerPos.add(x, y, -z), blockState);
				world.setBlockState(centerPos.add(-x, y, z), blockState);
				world.setBlockState(centerPos.add(-x, y, -z), blockState);
			}
			
			if(y == 0 || y == yBarrier || y == yCone)
			{
				for(x = -radius; x <= radius; x++)
				{
					for(z = -radius; z <= radius; z++)
					{
						int r = (int) Math.floor(Math.hypot(x, z));
						
						if(r < radius)
							world.setBlockState(centerPos.add(x, y, z), StarflightBlocks.STRUCTURAL_ALUMINUM.getDefaultState());
					}
				}
			}
		}
	}
}