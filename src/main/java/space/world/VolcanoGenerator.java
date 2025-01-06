package space.world;

import java.util.HashMap;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.structure.StructureContext;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePiecesHolder;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.structure.Structure;
import space.block.StarflightBlocks;
import space.util.PerlinNoise;

public class VolcanoGenerator
{
	private static HashMap<String, Settings> volcanoSettings;
	
	public static void addPieces(Structure.Context context, BlockPos pos, StructurePiecesHolder holder)
	{
		ChunkGenerator chunkGenerator = context.chunkGenerator();
		ChunkRandom random = context.random();
		NoiseConfig noiseConfig = context.noiseConfig();
		double spawnChance = 0.05;
		int baseRadius = random.nextBetween(32, 128);
		double heightFactor = baseRadius * (1.0 + random.nextDouble() * 1.5);
		double calderaFraction = 0.15 + random.nextDouble() * 0.1;
		double noiseFraction = 0.1 + random.nextDouble() * 0.2;
		double activity = Math.clamp(random.nextDouble() * 2.0, 0.0, 1.0);
		double oreFactor = MathHelper.lerp(random.nextDouble(), 0.1, 0.25);
		int surfaceY = chunkGenerator.getHeightOnGround(pos.getX(), pos.getZ(), Heightmap.Type.OCEAN_FLOOR_WG, context.world(), noiseConfig);
		RegistryEntry<Biome> biome = context.biomeSource().getBiome(BiomeCoords.fromBlock(pos.getX()), BiomeCoords.fromBlock(surfaceY), BiomeCoords.fromBlock(pos.getZ()), noiseConfig.getMultiNoiseSampler());
		Settings settings = volcanoSettings.get(biome.getIdAsString());
		Mutable mutable = new Mutable(pos.getX(), surfaceY, pos.getZ());
		BlockState worldStone = null;
		
		if(settings != null)
		{
			spawnChance = settings.spawnChance();
			activity = Math.clamp(random.nextDouble() * settings.activityFactor(), 0.0, 1.0);
			oreFactor *= settings.oreFactor();
		}
		
		if(biome.isIn(BiomeTags.IS_MOUNTAIN) || random.nextDouble() > spawnChance)
			return;
		
		while(worldStone == null && mutable.getY() > chunkGenerator.getMinimumY())
		{
			BlockState blockState = chunkGenerator.getColumnSample(pos.getX(), pos.getZ(), context.world(), noiseConfig).getState(mutable.getY());
			
			if(blockState.isIn(StarflightBlocks.WORLD_STONE_BLOCK_TAG))
				worldStone = blockState;
			
			mutable.setY(mutable.getY() - 1);
		}
		
		for(int i = 0; i < 8; i++)
		{
			double theta = (i * Math.PI * 2.0) / 8.0;
			int x = (int) (pos.getX() + baseRadius * Math.cos(theta));
			int z = (int) (pos.getZ() + baseRadius * Math.sin(theta));
			int sy = chunkGenerator.getHeightOnGround(x, z, Heightmap.Type.OCEAN_FLOOR_WG, context.world(), noiseConfig);
			
			if(sy < surfaceY)
				surfaceY = sy;
		}
		
		if(worldStone == null || surfaceY < 32)
			return;
		
		BlockPos center = pos.add(random.nextInt(16), surfaceY - 8, random.nextInt(16));
		int chunkRadius = (baseRadius >> 4) + 2;
		
		for(int x = -chunkRadius; x <= chunkRadius; x++)
		{
			for(int z = -chunkRadius; z <= chunkRadius; z++)
			{
				if(MathHelper.hypot(x, z) <= chunkRadius)
				{
					BlockPos startPos = new BlockPos(pos.getX() + (x << 4), 0, pos.getZ() + (z << 4));
					holder.addPiece(new Piece(startPos, center.getX(), center.getY(), center.getZ(), baseRadius, heightFactor, calderaFraction, noiseFraction, oreFactor, activity, biome.getIdAsString(), worldStone));
				}
			}
		}
	}
	
	public static void initializeVolcanoSettings()
	{
		registerVolcanoSettings("space:mercury_plains", new Settings(0.01, 0.25, 1.5));
		registerVolcanoSettings("space:mercury_hotspot", new Settings(0.01, 0.25, 1.5));
		registerVolcanoSettings("space:mercury_ice", new Settings(0.01, 0.25, 1.5));
		
		registerVolcanoSettings("space:venus_lowlands", new Settings(0.25, 3.0, 1.0));
		registerVolcanoSettings("space:venus_midlands", new Settings(0.25, 3.0, 1.0));
		registerVolcanoSettings("space:venus_volcanic_plains", new Settings(1.0, 5.0, 1.0));
		
		registerVolcanoSettings("space:moon_lowlands", new Settings(0.01, 0.1, 1.0));
		registerVolcanoSettings("space:moon_midlands", new Settings(0.01, 0.1, 1.0));
		registerVolcanoSettings("space:moon_ice", new Settings(0.01, 0.1, 1.0));
		
		registerVolcanoSettings("space:mars_lowlands", new Settings(0.05, 0.5, 1.0));
		registerVolcanoSettings("space:mars_midlands", new Settings(0.05, 0.5, 1.0));
		registerVolcanoSettings("space:mars_ice", new Settings(0.05, 0.5, 1.0));
	}
	
	public static void registerVolcanoSettings(String biomeName, Settings settings)
	{
		if(volcanoSettings == null)
			volcanoSettings = new HashMap<String, Settings>();
		
		volcanoSettings.put(biomeName, settings);
	}
	
	public record Settings(double spawnChance, double activityFactor, double oreFactor) {}

	public static class Piece extends StructurePiece
	{
		private final int centerX;
		private final int centerY;
		private final int centerZ;
		private final int baseRadius;
		private final double heightFactor;
		private final double calderaFraction;
		private final double noiseFraction;
		private final double oreFactor;
		private final double activity;
		private final String biomeID;
		private final BlockState worldStone;
		
		public Piece(BlockPos start, int x, int y, int z, int baseRadius, double heightFactor, double calderaFraction, double noiseFraction, double oreFactor, double activity, String biomeID, BlockState worldStone)
		{
			super(StarflightWorldGeneration.VOLCANO_PIECE, 0, new BlockBox(start.getX(), 0, start.getZ(), start.getX() + 16, 128, start.getZ() + 16));
			this.centerX = x;
			this.centerY = y;
			this.centerZ = z;
			this.baseRadius = baseRadius;
			this.heightFactor = heightFactor;
			this.calderaFraction = calderaFraction;
			this.noiseFraction = noiseFraction;
			this.oreFactor = oreFactor;
			this.activity = activity;
			this.biomeID = biomeID;
			this.worldStone = worldStone;
		}

		public Piece(StructureContext context, NbtCompound nbt)
		{
			super(StarflightWorldGeneration.VOLCANO_PIECE, nbt);
			this.centerX  = nbt.getInt("x");
			this.centerY  = nbt.getInt("y");
			this.centerZ = nbt.getInt("z");
			this.baseRadius = nbt.getInt("baseRadius");
			this.heightFactor = nbt.getDouble("heightFactor");
			this.calderaFraction = nbt.getDouble("calderaFraction");
			this.noiseFraction = nbt.getDouble("noiseFraction");
			this.oreFactor = nbt.getDouble("oreFactor");
			this.activity = nbt.getDouble("activity");
			this.biomeID = nbt.getString("biomeID");
			this.worldStone = NbtHelper.toBlockState(Registries.BLOCK.getReadOnlyWrapper(), nbt.getCompound("worldStone"));
		}

		@Override
		protected void writeNbt(StructureContext context, NbtCompound nbt)
		{
			nbt.putInt("x", this.centerX);
			nbt.putInt("y", this.centerY);
			nbt.putInt("z", this.centerZ);
			nbt.putInt("baseRadius", baseRadius);
			nbt.putDouble("heightFactor", heightFactor);
			nbt.putDouble("calderaFraction", calderaFraction);
			nbt.putDouble("noiseFraction", noiseFraction);
			nbt.putDouble("oreFactor", oreFactor);
			nbt.putDouble("activity", activity);
			nbt.putString("biomeID", biomeID);
			nbt.put("worldStone", NbtHelper.fromBlockState(worldStone));
		}
		
		@Override
		protected boolean canReplace(BlockState state)
		{
			return true;
		}

		@Override
		public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot)
		{
			PerlinNoise noise = new PerlinNoise(centerX, centerY, centerZ);
			BlockPos.Mutable mutable = new BlockPos.Mutable();
			int lavaY = centerY + getVolcanoHeight(0.0, 0.0, 0.0, noise);
			
			for(int x = 0; x < 16; x++)
			{
				for(int z = 0; z < 16; z++)
				{
					mutable.set(chunkPos.getStartX() + x, 0, chunkPos.getStartZ() + z);
					double distanceFromCenter = MathHelper.hypot(mutable.getX() - centerX, mutable.getZ() - centerZ);
					int surfaceY = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, mutable.getX(), mutable.getZ());
					mutable.setY(surfaceY);
					
					while(!world.getBlockState(mutable).blocksMovement() && mutable.getY() > world.getBottomY())
						mutable.move(Direction.DOWN);
					
					BlockState surfaceState = world.getBlockState(mutable);
					
					if(distanceFromCenter < baseRadius + 16)
					{
						distanceFromCenter /= baseRadius;
						double nx = (double) (mutable.getX() - centerX) / baseRadius;
						double nz = (double) (mutable.getZ() - centerZ) / baseRadius;
						int volcanoHeight = getVolcanoHeight(nx, nz, distanceFromCenter, noise);
						int startY = world.getBottomY();
						int endY = centerY + volcanoHeight;
						
						for(int i = 0; i < 32; i++)
						{
							BlockState baseState = world.getBlockState(mutable.setY(startY));
							
							if(baseState.isAir() || baseState.isLiquid())
							{
								startY--;
								endY--;
							}
							else
								break;
						}
						
						for(int y = startY; y <= endY; y++)
						{
							BlockState volcanoState = getVolcanoState(random, distanceFromCenter, heightFactor, startY, endY, centerY, lavaY, y);
							mutable.setY(y);
							
							if(y == endY && activity < 0.75 && volcanoState == worldStone)
								volcanoState = surfaceState;
							
							if(world.getBlockState(mutable).getBlock() != Blocks.BEDROCK)
							{
								world.setBlockState(mutable, volcanoState, Block.NOTIFY_LISTENERS);
								
								if(volcanoState.getBlock() == Blocks.LAVA)
									world.scheduleFluidTick(mutable, Fluids.LAVA, 2);
							}
						}
					}
				}
			}
		}
		
		private int getVolcanoHeight(double x, double z, double distanceFromCenter, PerlinNoise noise)
	    {
	    	double d = Math.min((distanceFromCenter * distanceFromCenter) / MathHelper.SQUARE_ROOT_OF_TWO, 1.0);
	    	double noiseShape = noise.get(x * 4.0, z * 4.0) * noiseFraction;
	    	double shape = MathHelper.lerp(0.8, noiseShape, 1.0 - d);
	    	
	    	if(distanceFromCenter < calderaFraction)
	    	{
	    		double dc = Math.min((calderaFraction * calderaFraction) / MathHelper.SQUARE_ROOT_OF_TWO, 1.0);
	    		double calderaShape = shape - Math.pow((calderaFraction - distanceFromCenter) * 2.0, 1.5);
	    		double lavaShape = MathHelper.lerp(0.8, MathHelper.lerp(activity, -1.0, -0.5) * noiseFraction, 1.0 - dc);
	    		shape = Math.max(calderaShape, lavaShape);
	    	}
	    	
	    	return (int) (Math.pow(shape, 2.0) * heightFactor);
	    }
		
		private BlockState getVolcanoState(Random random, double distanceFromCenter, double heightFactor, int startY, int endY, int centerY, int lavaY, int y)
		{
			double distanceFactor = Math.pow(1.0 - distanceFromCenter, 2.0);
			double basaltFactor = Math.pow((1.0 - distanceFromCenter) * 0.75 + ((endY - centerY) / heightFactor) * 0.75, 2.0) * (activity + 0.5);
			double randomSelector = random.nextDouble();
			
			if(distanceFromCenter < calderaFraction && endY <= lavaY)
			{
				int plugDepth = (int) ((endY - startY) * (0.75 - activity) * 0.5) + random.nextInt(4);
				
				if(plugDepth <= 0)
					return Blocks.LAVA.getDefaultState();
				
				if(plugDepth > 8 && endY - y < (plugDepth / 4) + random.nextInt(8) - random.nextInt(8))
					return basaltFactor * 0.25 > randomSelector ? Blocks.BASALT.getDefaultState() : Blocks.SMOOTH_BASALT.getDefaultState();
				else if(endY - y < plugDepth + random.nextInt(8) - random.nextInt(8))
					return Blocks.MAGMA_BLOCK.getDefaultState();
				
				return Blocks.LAVA.getDefaultState();
			}
			else if(randomSelector < basaltFactor)
			{
				if(y < endY - random.nextBetween(8, 16) && basaltFactor * oreFactor > randomSelector)
				{
					double deepOreSelector = random.nextDouble();
					
					if(y < centerY && distanceFactor * 0.01 > deepOreSelector)
						return Blocks.RAW_GOLD_BLOCK.getDefaultState();
					if(distanceFactor * 0.25 > deepOreSelector)
						return Blocks.RAW_IRON_BLOCK.getDefaultState();
					if(distanceFactor * 0.5 > deepOreSelector)
						return StarflightBlocks.ILMENITE_BLOCK.getDefaultState();
					if(distanceFactor * 0.75 > deepOreSelector)
						return StarflightBlocks.BAUXITE_BLOCK.getDefaultState();
				}
				
				if(distanceFromCenter < calderaFraction && basaltFactor * oreFactor * 0.1 > randomSelector)
					return StarflightBlocks.BASALT_SULFUR_ORE.getDefaultState();
				
				return basaltFactor * 0.25 > randomSelector ? Blocks.BASALT.getDefaultState() : Blocks.SMOOTH_BASALT.getDefaultState();
			}
				
			return worldStone;
		}
	}
}