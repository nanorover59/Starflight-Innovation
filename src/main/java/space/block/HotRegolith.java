package space.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.ColoredFallingBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ColorCode;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import space.entity.AlienMobEntity;

public class HotRegolith extends ColoredFallingBlock
{
	public HotRegolith(ColorCode color, Settings settings)
	{
		super(color, settings);
	}
	
	@Override
    public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity)
	{
        if(entity instanceof LivingEntity && !(entity instanceof AlienMobEntity && ((AlienMobEntity) entity).isTemperatureSafe(3)))
            entity.damage(world.getDamageSources().hotFloor(), 0.5f);
        
        super.onSteppedOn(world, pos, state, entity);
    }
}