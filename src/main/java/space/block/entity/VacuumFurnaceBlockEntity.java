package space.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import space.block.ElectricFurnaceBlock;
import space.block.EnergyBlock;
import space.block.StarflightBlocks;
import space.planet.PlanetDimensionData;
import space.planet.PlanetList;
import space.recipe.StarflightRecipes;

public class VacuumFurnaceBlockEntity extends ElectricFurnaceBlockEntity
{
	private double pressure = -1.0;
	
	public VacuumFurnaceBlockEntity(BlockPos blockPos, BlockState blockState)
	{
		super(blockPos, blockState);
		this.recipeTypes.add(StarflightRecipes.VACUUM_FURNACE);
	}
	
	/**
	 * Use less power in lower ambient pressure.
	 */
	public void updateExternalPressure()
	{
		BlockPos frontPos = getPos().offset(getCachedState().get(ElectricFurnaceBlock.FACING));
		
		if(world.getBlockState(frontPos).isOf(StarflightBlocks.HABITABLE_AIR))
		{
			pressure = 1.0;
			return;
		}
		
		PlanetDimensionData data = PlanetList.getDimensionDataForWorld(this.getWorld());
		
		// Update the gravity and air resistance multiplier variables.
		if(data != null && data.overridePhysics())
		{
			pressure = data.getPressure();
			return;
		}
		
		pressure = 1.0;
	}
	
	@Override
	public int getCookTime()
	{
		return (Integer) getFirstMatchRecipeOptional().map(recipe -> ((AbstractCookingRecipe) recipe.value()).getCookingTime()).orElse(200) / 4;
	}
	
	@Override
	public double getInput()
	{
		if(pressure < 0.0)
			updateExternalPressure();
		
		double localPower = ((EnergyBlock) getCachedState().getBlock()).getInput() * (1.0 + 3.0 * pressure);
		return localPower / world.getTickManager().getTickRate();
	}
	
	@Override
	public void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup)
	{
		super.writeNbt(nbt, registryLookup);
		nbt.putDouble("pressure", this.pressure);
	}
	
	@Override
	public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup)
	{
		super.readNbt(nbt, registryLookup);
		this.pressure = nbt.getDouble("pressure");
	}
}