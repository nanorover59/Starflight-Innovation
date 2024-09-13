package space.world;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Consumer;

import com.mojang.serialization.Codec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.Thickness;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.floatprovider.ClampedNormalFloatProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.CaveSurface;
import net.minecraft.world.gen.feature.util.FeatureContext;
import space.block.IcicleBlock;
import space.block.StarflightBlocks;

public class CaveIceFeature extends Feature<CaveIceFeatureConfig>
{
	public CaveIceFeature(Codec<CaveIceFeatureConfig> codec)
	{
		super(codec);
	}

	@Override
	public boolean generate(FeatureContext<CaveIceFeatureConfig> context)
	{
		StructureWorldAccess structureWorldAccess = context.getWorld();
		BlockPos blockPos = context.getOrigin();
		CaveIceFeatureConfig iceClusterFeatureConfig = context.getConfig();
		Random random = context.getRandom();

		if(!canGenerate(structureWorldAccess, blockPos))
			return false;

		int i = iceClusterFeatureConfig.height.get(random);
		float f = iceClusterFeatureConfig.wetness.get(random);
		float g = iceClusterFeatureConfig.density.get(random);
		int j = iceClusterFeatureConfig.radius.get(random);
		int k = iceClusterFeatureConfig.radius.get(random);

		for(int l = -j; l <= j; ++l)
		{
			for(int m = -k; m <= k; ++m)
			{
				double d = this.iceChance(j, k, l, m, iceClusterFeatureConfig);
				BlockPos blockPos2 = blockPos.add(l, 0, m);
				this.generate(structureWorldAccess, random, blockPos2, l, m, f, d, i, g, iceClusterFeatureConfig);
			}
		}

		return true;
	}

	private void generate(StructureWorldAccess world, Random random, BlockPos pos, int localX, int localZ, float wetness, double iceChance, int height, float density, CaveIceFeatureConfig config)
	{
		boolean bl4;
		int t;
		int m;
		boolean bl3;
		int l;
		int j;
		boolean bl2;
		CaveSurface caveSurface;
		boolean bl;
		Optional<CaveSurface> optional = CaveSurface.create(world, pos, config.floorToCeilingSearchRange, CaveIceFeature::canGenerate, CaveIceFeature::cannotGenerate);

		if(optional.isEmpty())
			return;

		OptionalInt optionalInt = optional.get().getCeilingHeight();
		OptionalInt optionalInt2 = optional.get().getFloorHeight();

		if(optionalInt.isEmpty() && optionalInt2.isEmpty())
			return;

		bl = random.nextFloat() < wetness;

		if(bl && optionalInt2.isPresent() && this.canPowderSnowSpawn(world, pos.withY(optionalInt2.getAsInt())))
		{
			int i = optionalInt2.getAsInt();
			caveSurface = optional.get().withFloor(OptionalInt.of(i - 1));
			world.setBlockState(pos.withY(i), Blocks.POWDER_SNOW.getDefaultState(), Block.NOTIFY_LISTENERS);
		}
		else
			caveSurface = optional.get();

		OptionalInt optionalInt3 = caveSurface.getFloorHeight();
		bl2 = random.nextDouble() < iceChance;

		if(optionalInt.isPresent() && bl2 && !this.isLava(world, pos.withY(optionalInt.getAsInt())))
		{
			j = config.iceBlockLayerThickness.get(random);
			this.placeIceBlocks(world, pos.withY(optionalInt.getAsInt()), j, Direction.UP);
			int k = optionalInt3.isPresent() ? Math.min(height, optionalInt.getAsInt() - optionalInt3.getAsInt()) : height;
			l = this.getHeight(random, localX, localZ, density, k, config);
		}
		else
			l = 0;

		bl3 = random.nextDouble() < iceChance;

		if(optionalInt3.isPresent() && bl3 && !this.isLava(world, pos.withY(optionalInt3.getAsInt())))
		{
			m = config.iceBlockLayerThickness.get(random);
			this.placeIceBlocks(world, pos.withY(optionalInt3.getAsInt()), m, Direction.DOWN);
			j = optionalInt.isPresent() ? Math.max(0, l + MathHelper.nextBetween(random, -config.maxIcicleHeightDiff, config.maxIcicleHeightDiff)) : this.getHeight(random, localX, localZ, density, height, config);
		}
		else
			j = 0;

		if(optionalInt.isPresent() && optionalInt3.isPresent() && optionalInt.getAsInt() - l <= optionalInt3.getAsInt() + j)
		{
			int n = optionalInt3.getAsInt();
			int o = optionalInt.getAsInt();
			int p = Math.max(o - l, n + 1);
			int q = Math.min(n + j, o - 1);
			int r = MathHelper.nextBetween(random, p, q + 1);
			int s = r - 1;
			m = o - r;
			t = s - n;
		}
		else
		{
			m = l;
			t = j;
		}

		bl4 = random.nextBoolean() && m > 0 && t > 0 && caveSurface.getOptionalHeight().isPresent() && m + t == caveSurface.getOptionalHeight().getAsInt();

		if(optionalInt.isPresent())
			generateIcicles(world, pos.withY(optionalInt.getAsInt() - 1), Direction.DOWN, m, bl4);

		if(optionalInt3.isPresent())
			generateIcicles(world, pos.withY(optionalInt3.getAsInt() + 1), Direction.UP, t, bl4);
	}

	private boolean isLava(WorldView world, BlockPos pos)
	{
		return world.getBlockState(pos).isOf(Blocks.LAVA);
	}

	private int getHeight(Random random, int localX, int localZ, float density, int height, CaveIceFeatureConfig config)
	{
		if(random.nextFloat() > density)
		{
			return 0;
		}
		int i = Math.abs(localX) + Math.abs(localZ);
		float f = (float) MathHelper.clampedMap((double) i, 0.0, (double) config.maxDistanceFromCenterAffectingHeightBias, (double) height / 2.0, 0.0);
		return (int) CaveIceFeature.clampedGaussian(random, 0.0f, height, f, config.heightDeviation);
	}

	private boolean canPowderSnowSpawn(StructureWorldAccess world, BlockPos pos)
	{
		for(Direction direction : Direction.Type.HORIZONTAL)
		{
			BlockState blockState = world.getBlockState(pos.offset(direction));
			
			if(isStone(blockState))
				continue;
			
			return false;
		}
		
		return isStone(world.getBlockState(pos));
	}

	private void placeIceBlocks(StructureWorldAccess world, BlockPos pos, int height, Direction direction)
	{
		BlockPos.Mutable mutable = pos.mutableCopy();

		for(int i = 0; i < height; ++i)
		{
			if(!generateIceBlock(world, mutable))
				return;

			mutable.move(direction);
		}
	}

	private double iceChance(int radiusX, int radiusZ, int localX, int localZ, CaveIceFeatureConfig config)
	{
		int i = radiusX - Math.abs(localX);
		int j = radiusZ - Math.abs(localZ);
		int k = Math.min(i, j);
		return MathHelper.clampedMap(k, 0.0f, config.maxDistanceFromCenterAffectingChanceOfIceColumn, config.chanceOfIceColumnAtMaxDistanceFromCenter, 1.0f);
	}

	protected void generateIcicles(WorldAccess world, BlockPos pos, Direction direction, int height, boolean merge)
	{
		if(!canReplace(world.getBlockState(pos.offset(direction.getOpposite()))))
			return;

		BlockPos.Mutable mutable = pos.mutableCopy();
		getIceThickness(direction, height, merge, state -> {
			if(state.isOf(StarflightBlocks.ICICLE))
				state = (BlockState) state.with(IcicleBlock.WATERLOGGED, world.isWater(mutable));

			world.setBlockState(mutable, (BlockState) state, Block.NOTIFY_LISTENERS);
			mutable.move(direction);
		});
	}

	protected boolean generateIceBlock(WorldAccess world, BlockPos pos)
	{
		BlockState blockState = world.getBlockState(pos);

		if(blockState.isIn(BlockTags.DRIPSTONE_REPLACEABLE_BLOCKS))
		{
			world.setBlockState(pos, world.getRandom().nextInt(5) == 0 ? Blocks.BLUE_ICE.getDefaultState() : Blocks.PACKED_ICE.getDefaultState(), Block.NOTIFY_LISTENERS);
			return true;
		}

		return false;
	}
	
	protected void getIceThickness(Direction direction, int height, boolean merge, Consumer<BlockState> callback)
	{
		if(height >= 3)
		{
			callback.accept(getState(direction, Thickness.BASE));
			
			for(int i = 0; i < height - 3; ++i)
				callback.accept(getState(direction, Thickness.MIDDLE));
		}
		
		if(height >= 2)
			callback.accept(getState(direction, Thickness.FRUSTUM));
		
		if(height >= 1)
			callback.accept(getState(direction, merge ? Thickness.TIP_MERGE : Thickness.TIP));
	}
	
	private static BlockState getState(Direction direction, Thickness thickness)
	{
		return StarflightBlocks.ICICLE.getDefaultState().with(IcicleBlock.VERTICAL_DIRECTION, direction).with(IcicleBlock.THICKNESS, thickness);
	}
	
	public static boolean canReplace(BlockState state)
	{
        return !state.isAir() && state.getBlock() != StarflightBlocks.ICICLE;
    }

	protected static boolean canGenerate(WorldAccess world, BlockPos pos)
    {
        return world.testBlockState(pos, CaveIceFeature::canGenerate);
    }
	
	public static boolean canGenerate(BlockState state)
	{
        return state.isAir();
    }
	
	public static boolean cannotGenerate(BlockState state)
	{
		return !canGenerate(state);
	}

	private static float clampedGaussian(Random random, float min, float max, float mean, float deviation)
	{
		return ClampedNormalFloatProvider.get(random, mean, deviation, min, max);
	}
}