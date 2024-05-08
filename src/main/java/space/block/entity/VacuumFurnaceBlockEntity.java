package space.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import space.block.ElectricFurnaceBlock;
import space.block.EnergyBlock;
import space.block.StarflightBlocks;
import space.planet.PlanetDimensionData;
import space.planet.PlanetList;

public class VacuumFurnaceBlockEntity extends ElectricFurnaceBlockEntity
{
	private double pressure;
	
	public VacuumFurnaceBlockEntity(BlockPos blockPos, BlockState blockState)
	{
		super(blockPos, blockState);
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
	
	/*@Override
	public int getCookTime()
	{
		//return (Integer) world.getRecipeManager().getFirstMatch(RecipeType.BLASTING, inventory, world).map(AbstractCookingRecipe::getCookTime).orElse(world.getRecipeManager().getFirstMatch(RecipeType.SMOKING, inventory, world).map(AbstractCookingRecipe::getCookTime).orElse(world.getRecipeManager().getFirstMatch(RecipeType.SMELTING, inventory, world).map(AbstractCookingRecipe::getCookTime).orElse(200)));
		int localCookTime = (int) (64.0 * (1.0 + Math.min(pressure * pressure, 1.0)));
		return localCookTime;
	}*/
	
	@Override
	public double getInput()
	{
		double localPower = ((EnergyBlock) getCachedState().getBlock()).getInput() * (1.0 + 3.0 * pressure);
		return localPower / world.getTickManager().getTickRate();
	}
	
	public void writeNbt(NbtCompound nbt)
	{
		super.writeNbt(nbt);
		nbt.putDouble("pressure", this.pressure);
	}
	
	public void readNbt(NbtCompound nbt)
	{
		super.readNbt(nbt);
		this.pressure = nbt.getDouble("pressure");
	}
}