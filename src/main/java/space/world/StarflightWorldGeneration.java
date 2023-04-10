package space.world;

import java.util.List;
import java.util.Map;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.MultifaceGrowthBlock;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.fluid.Fluids;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.structure.rule.BlockMatchRuleTest;
import net.minecraft.structure.rule.RuleTest;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DataPool;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.VerticalSurfaceType;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.util.math.intprovider.WeightedListIntProvider;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryEntryList;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.StructureSpawns;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureTerrainAdaptation;
import net.minecraft.world.gen.YOffset;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;
import net.minecraft.world.gen.feature.BlockColumnFeatureConfig;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.ConfiguredFeatures;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.MultifaceGrowthFeatureConfig;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.PlacedFeatures;
import net.minecraft.world.gen.feature.RandomFeatureConfig;
import net.minecraft.world.gen.feature.RandomFeatureEntry;
import net.minecraft.world.gen.feature.SimpleBlockFeatureConfig;
import net.minecraft.world.gen.feature.SpringFeature;
import net.minecraft.world.gen.feature.SpringFeatureConfig;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.feature.UndergroundConfiguredFeatures;
import net.minecraft.world.gen.feature.VegetationPatchFeatureConfig;
import net.minecraft.world.gen.feature.VegetationPlacedFeatures;
import net.minecraft.world.gen.feature.size.TwoLayersFeatureSize;
import net.minecraft.world.gen.foliage.BlobFoliagePlacer;
import net.minecraft.world.gen.heightprovider.VeryBiasedToBottomHeightProvider;
import net.minecraft.world.gen.placementmodifier.BiomePlacementModifier;
import net.minecraft.world.gen.placementmodifier.CountPlacementModifier;
import net.minecraft.world.gen.placementmodifier.EnvironmentScanPlacementModifier;
import net.minecraft.world.gen.placementmodifier.HeightRangePlacementModifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifier;
import net.minecraft.world.gen.placementmodifier.RandomOffsetPlacementModifier;
import net.minecraft.world.gen.placementmodifier.SquarePlacementModifier;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.stateprovider.WeightedBlockStateProvider;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;
import net.minecraft.world.gen.trunk.StraightTrunkPlacer;
import space.StarflightMod;
import space.block.StarflightBlocks;

public class StarflightWorldGeneration
{
	public static final TagKey<Biome> LIGHT_CRATERING = TagKey.of(Registry.BIOME_KEY, new Identifier(StarflightMod.MOD_ID, "light_cratering"));
	public static final TagKey<Biome> MEDIUM_CRATERING = TagKey.of(Registry.BIOME_KEY, new Identifier(StarflightMod.MOD_ID, "medium_cratering"));
	public static final TagKey<Biome> HEAVY_CRATERING = TagKey.of(Registry.BIOME_KEY, new Identifier(StarflightMod.MOD_ID, "heavy_cratering"));
	public static final TagKey<Biome> SCATTER = TagKey.of(Registry.BIOME_KEY, new Identifier(StarflightMod.MOD_ID, "scatter"));
	public static final TagKey<Biome> MORE_SCATTER = TagKey.of(Registry.BIOME_KEY, new Identifier(StarflightMod.MOD_ID, "more_scatter"));
	public static final TagKey<Biome> LIQUID_WATER = TagKey.of(Registry.BIOME_KEY, new Identifier(StarflightMod.MOD_ID, "liquid_water"));
	public static final TagKey<Biome> OUTPOST_STRUCTURES = TagKey.of(Registry.BIOME_KEY, new Identifier(StarflightMod.MOD_ID, "outpost_structures"));
	public static final RuleTest FERRIC_STONE_ORE_REPLACEABLES = new BlockMatchRuleTest(StarflightBlocks.FERRIC_STONE);
	public static final RuleTest FRIGID_STONE_ORE_REPLACEABLES = new BlockMatchRuleTest(StarflightBlocks.FRIGID_STONE);

	// Impact Crater Structure
	public static final StructurePieceType CRATER_PIECE = Registry.register(Registry.STRUCTURE_PIECE, new Identifier(StarflightMod.MOD_ID, "crater_piece"), CraterGenerator.Piece::new);
	public static final StructureType<CraterStructure> CRATER_TYPE = Registry.register(Registry.STRUCTURE_TYPE, new Identifier(StarflightMod.MOD_ID, "crater"), () -> CraterStructure.CODEC);
	public static final RegistryEntry<Structure> CRATER_0 = register(RegistryKey.of(Registry.STRUCTURE_KEY, new Identifier(StarflightMod.MOD_ID, "crater_0")), new CraterStructure(createConfig(LIGHT_CRATERING, StructureTerrainAdaptation.NONE)));
	public static final RegistryEntry<Structure> CRATER_1 = register(RegistryKey.of(Registry.STRUCTURE_KEY, new Identifier(StarflightMod.MOD_ID, "crater_1")), new CraterStructure(createConfig(MEDIUM_CRATERING, StructureTerrainAdaptation.NONE)));
	public static final RegistryEntry<Structure> CRATER_2 = register(RegistryKey.of(Registry.STRUCTURE_KEY, new Identifier(StarflightMod.MOD_ID, "crater_2")), new CraterStructure(createConfig(HEAVY_CRATERING, StructureTerrainAdaptation.NONE)));
	
	// Surface Outpost Structure
	public static final StructurePieceType OUTPOST_PIECE = Registry.register(Registry.STRUCTURE_PIECE, new Identifier(StarflightMod.MOD_ID, "outpost_piece"), OutpostGenerator.Piece::new);
	public static final StructureType<OutpostStructure> OUTPOST_TYPE = Registry.register(Registry.STRUCTURE_TYPE, new Identifier(StarflightMod.MOD_ID, "outpost"), () -> OutpostStructure.CODEC);
	public static final RegistryEntry<Structure> OUTPOST = register(RegistryKey.of(Registry.STRUCTURE_KEY, new Identifier(StarflightMod.MOD_ID, "outpost")), new OutpostStructure(createConfig(OUTPOST_STRUCTURES, StructureTerrainAdaptation.BURY)));
	
	// Moonshaft Structure 
	public static final StructurePieceType MOONSHAFT_CORRIDOR = Registry.register(Registry.STRUCTURE_PIECE, new Identifier(StarflightMod.MOD_ID, "moonshaft_corridor"), MoonshaftGenerator.MoonshaftCorridor::new);
    public static final StructurePieceType MOONSHAFT_CROSSING = Registry.register(Registry.STRUCTURE_PIECE, new Identifier(StarflightMod.MOD_ID, "moonshaft_crossing"), MoonshaftGenerator.MoonshaftCrossing::new);
    public static final StructurePieceType MOONSHAFT_ROOM = Registry.register(Registry.STRUCTURE_PIECE, new Identifier(StarflightMod.MOD_ID, "moonshaft_room"), MoonshaftGenerator.MoonshaftRoom::new);
	public static final StructureType<MoonshaftStructure> MOONSHAFT_TYPE = Registry.register(Registry.STRUCTURE_TYPE, new Identifier(StarflightMod.MOD_ID, "moonshaft"), () -> MoonshaftStructure.CODEC);
	public static final RegistryEntry<Structure> MOONSHAFT = register(RegistryKey.of(Registry.STRUCTURE_KEY, new Identifier(StarflightMod.MOD_ID, "moonshaft")), new MoonshaftStructure(createConfig(OUTPOST_STRUCTURES, StructureTerrainAdaptation.NONE)));
	
	// Impact Crater Feature
	public static final Feature<DefaultFeatureConfig> SMALL_CRATER = Registry.register(Registry.FEATURE, new Identifier(StarflightMod.MOD_ID, "small_crater"), new SmallCraterFeature(DefaultFeatureConfig.CODEC));
	public static final RegistryEntry<ConfiguredFeature<DefaultFeatureConfig, ?>> SMALL_CRATER_CONFIGURED_FEATURE = ConfiguredFeatures.register(new Identifier(StarflightMod.MOD_ID, "small_crater").toString(), SMALL_CRATER);
	public static final RegistryEntry<PlacedFeature> SMALL_CRATER_PLACED_FEATURE= PlacedFeatures.register(new Identifier(StarflightMod.MOD_ID, "small_crater").toString(), SMALL_CRATER_CONFIGURED_FEATURE, CountPlacementModifier.of(1), PlacedFeatures.OCEAN_FLOOR_WG_HEIGHTMAP, BiomePlacementModifier.of());
	
	// Surface Rock Feature
	public static final Feature<DefaultFeatureConfig> SURFACE_ROCK = Registry.register(Registry.FEATURE, new Identifier(StarflightMod.MOD_ID, "surface_rock"), new SurfaceRockFeature(DefaultFeatureConfig.CODEC));
	public static final RegistryEntry<ConfiguredFeature<DefaultFeatureConfig, ?>> SURFACE_ROCK_CONFIGURED_FEATURE = ConfiguredFeatures.register(new Identifier(StarflightMod.MOD_ID, "surface_rock").toString(), SURFACE_ROCK);
	public static final RegistryEntry<PlacedFeature> SURFACE_ROCK_PLACED_FEATURE = PlacedFeatures.register(new Identifier(StarflightMod.MOD_ID, "surface_rock").toString(), SURFACE_ROCK_CONFIGURED_FEATURE, CountPlacementModifier.of(2), SquarePlacementModifier.of(), PlacedFeatures.OCEAN_FLOOR_WG_HEIGHTMAP, BiomePlacementModifier.of());

	// Rock Patch Feature
	public static final Feature<DefaultFeatureConfig> ROCK_PATCH = Registry.register(Registry.FEATURE, new Identifier(StarflightMod.MOD_ID, "rock_patch"), new RockPatchFeature(DefaultFeatureConfig.CODEC));
	public static final RegistryEntry<ConfiguredFeature<DefaultFeatureConfig, ?>> ROCK_PATCH_CONFIGURED_FEATURE = ConfiguredFeatures.register(new Identifier(StarflightMod.MOD_ID, "rock_patch").toString(), ROCK_PATCH);
	public static final RegistryEntry<PlacedFeature> ROCK_PATCH_PLACED_FEATURE = PlacedFeatures.register(new Identifier(StarflightMod.MOD_ID, "rock_patch").toString(), ROCK_PATCH_CONFIGURED_FEATURE, CountPlacementModifier.of(2), SquarePlacementModifier.of(), PlacedFeatures.OCEAN_FLOOR_WG_HEIGHTMAP, BiomePlacementModifier.of());
	
	// Underground Lava Spring Feature
	public static final Feature<SpringFeatureConfig> UNDERGROUND_LAVA_SPRING = Registry.register(Registry.FEATURE, new Identifier(StarflightMod.MOD_ID, "underground_lava_spring"), new SpringFeature(SpringFeatureConfig.CODEC));
	public static final RegistryEntry<ConfiguredFeature<SpringFeatureConfig, ?>> UNDERGROUND_LAVA_SPRING_CONFIGURED_FEATURE = ConfiguredFeatures.register("underground_lava_spring", UNDERGROUND_LAVA_SPRING, new SpringFeatureConfig(Fluids.LAVA.getDefaultState(), true, 4, 1, RegistryEntryList.of(Block::getRegistryEntry, Blocks.STONE, Blocks.DEEPSLATE, Blocks.SMOOTH_BASALT, Blocks.TUFF, StarflightBlocks.FERRIC_STONE)));
	public static final RegistryEntry<PlacedFeature> UNDERGROUND_LAVA_SPRING_PLACED_FEATURE = PlacedFeatures.register("underground_lava_spring", UNDERGROUND_LAVA_SPRING_CONFIGURED_FEATURE, CountPlacementModifier.of(20), SquarePlacementModifier.of(), HeightRangePlacementModifier.of(VeryBiasedToBottomHeightProvider.create(YOffset.getBottom(), YOffset.fixed(0), 8)), BiomePlacementModifier.of());
	
	// Moon "Ice Blade" Feature
	public static final Feature<DefaultFeatureConfig> ICE_BLADE = Registry.register(Registry.FEATURE, new Identifier(StarflightMod.MOD_ID, "ice_blade"), new IceBladeFeature(DefaultFeatureConfig.CODEC));
	public static final RegistryEntry<ConfiguredFeature<DefaultFeatureConfig, ?>> ICE_BLADE_CONFIGURED_FEATURE = ConfiguredFeatures.register(new Identifier(StarflightMod.MOD_ID, "ice_blade").toString(), ICE_BLADE);
	public static final RegistryEntry<PlacedFeature> ICE_BLADE_PLACED_FEATURE = PlacedFeatures.register(new Identifier(StarflightMod.MOD_ID, "ice_blade").toString(), ICE_BLADE_CONFIGURED_FEATURE, CountPlacementModifier.of(2), SquarePlacementModifier.of(), PlacedFeatures.OCEAN_FLOOR_WG_HEIGHTMAP, BiomePlacementModifier.of());

	// Mars Underground Features
	public static final Feature<VegetationPatchFeatureConfig> MARS_PATCH = Registry.register(Registry.FEATURE, new Identifier(StarflightMod.MOD_ID, "mars_patch"), new MarsPatchFeature(VegetationPatchFeatureConfig.CODEC));
	public static final RegistryEntry<ConfiguredFeature<MultifaceGrowthFeatureConfig, ?>> MARS_GLOW_LICHEN_CONFIGURED_FEATURE = ConfiguredFeatures.register(new Identifier(StarflightMod.MOD_ID, "mars_glow_lichen").toString(), Feature.MULTIFACE_GROWTH, new MultifaceGrowthFeatureConfig((MultifaceGrowthBlock)Blocks.GLOW_LICHEN, 20, false, true, true, 0.5f, RegistryEntryList.of(Block::getRegistryEntry, StarflightBlocks.FERRIC_STONE, StarflightBlocks.REDSLATE)));
	public static final RegistryEntry<ConfiguredFeature<BlockColumnFeatureConfig, ?>> LYCOPHYTE_CONFIGURED_FEATURE = ConfiguredFeatures.register(new Identifier(StarflightMod.MOD_ID, "lycophyte").toString(), Feature.BLOCK_COLUMN, new BlockColumnFeatureConfig(List.of(BlockColumnFeatureConfig.createLayer(new WeightedListIntProvider(DataPool.of(UniformIntProvider.create(1, 4))), BlockStateProvider.of(StarflightBlocks.LYCOPHYTE_STEM)), BlockColumnFeatureConfig.createLayer(ConstantIntProvider.create(1), BlockStateProvider.of((BlockState) StarflightBlocks.LYCOPHYTE_TOP.getDefaultState()))), Direction.UP, BlockPredicate.IS_AIR_OR_WATER, true));
	public static final RegistryEntry<ConfiguredFeature<SimpleBlockFeatureConfig, ?>> ARES_MOSS_VEGETATION_CONFIGURED_FEATURE = ConfiguredFeatures.register(new Identifier(StarflightMod.MOD_ID, "ares_moss_vegatation").toString(), Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(new WeightedBlockStateProvider((new DataPool.Builder<BlockState>()).add(StarflightBlocks.ARES_MOSS_CARPET.getDefaultState(), 8))));
	public static final RegistryEntry<ConfiguredFeature<RandomFeatureConfig, ?>> MARS_VEGETATION_CONFIGURED_FEATURE = ConfiguredFeatures.register("mars_vegetation", Feature.RANDOM_SELECTOR, new RandomFeatureConfig(List.of(new RandomFeatureEntry(PlacedFeatures.createEntry(LYCOPHYTE_CONFIGURED_FEATURE, new PlacementModifier[0]), 0.25f)), PlacedFeatures.createEntry(ARES_MOSS_VEGETATION_CONFIGURED_FEATURE, new PlacementModifier[0])));
	public static final RegistryEntry<ConfiguredFeature<VegetationPatchFeatureConfig, ?>> ARES_MOSS_PATCH_CONFIGURED_FEATURE = ConfiguredFeatures.register(new Identifier(StarflightMod.MOD_ID, "ares_moss_patch").toString(), Feature.VEGETATION_PATCH, new VegetationPatchFeatureConfig(BlockTags.MOSS_REPLACEABLE, BlockStateProvider.of(StarflightBlocks.ARES_MOSS_BLOCK), PlacedFeatures.createEntry(MARS_VEGETATION_CONFIGURED_FEATURE, new PlacementModifier[0]), VerticalSurfaceType.FLOOR, ConstantIntProvider.create(1), 0.0f, 5, 0.8f, UniformIntProvider.create(3, 7), 0.3f));
	public static final RegistryEntry<ConfiguredFeature<VegetationPatchFeatureConfig, ?>> ARES_MOSS_PATCH_CEILING_CONFIGURED_FEATURE = ConfiguredFeatures.register(new Identifier(StarflightMod.MOD_ID, "ares_moss_patch_ceiling").toString(), Feature.VEGETATION_PATCH, new VegetationPatchFeatureConfig(BlockTags.MOSS_REPLACEABLE, BlockStateProvider.of(StarflightBlocks.ARES_MOSS_BLOCK), PlacedFeatures.createEntry(UndergroundConfiguredFeatures.GLOW_LICHEN, new PlacementModifier[0]), VerticalSurfaceType.CEILING, UniformIntProvider.create(1, 2), 0.0f, 5, 0.08f, UniformIntProvider.create(3, 7), 0.3f));
	public static final RegistryEntry<ConfiguredFeature<VegetationPatchFeatureConfig, ?>> MARS_CAVE_POOL_CONFIGURED_FEATURE = ConfiguredFeatures.register(new Identifier(StarflightMod.MOD_ID, "mars_cave_pool").toString(), MARS_PATCH, new VegetationPatchFeatureConfig(BlockTags.LUSH_GROUND_REPLACEABLE, BlockStateProvider.of(StarflightBlocks.FERRIC_SAND.getDefaultState()), PlacedFeatures.createEntry(LYCOPHYTE_CONFIGURED_FEATURE, new PlacementModifier[0]), VerticalSurfaceType.FLOOR, ConstantIntProvider.create(3), 0.8f, 5, 0.1f, UniformIntProvider.create(3, 7), 0.7f));
	public static final RegistryEntry<PlacedFeature> ARES_MOSS_PATCH_PLACED_FEATURE = PlacedFeatures.register(new Identifier(StarflightMod.MOD_ID, "ares_moss_patch").toString(), ARES_MOSS_PATCH_CONFIGURED_FEATURE, CountPlacementModifier.of(125), SquarePlacementModifier.of(), PlacedFeatures.BOTTOM_TO_120_RANGE, EnvironmentScanPlacementModifier.of(Direction.DOWN, BlockPredicate.solid(), BlockPredicate.IS_AIR, 12), RandomOffsetPlacementModifier.vertically(ConstantIntProvider.create(1)), BiomePlacementModifier.of());
	public static final RegistryEntry<PlacedFeature> ARES_MOSS_PATCH_CEILING_PLACED_FEATURE = PlacedFeatures.register(new Identifier(StarflightMod.MOD_ID, "ares_moss_patch_ceiling").toString(), ARES_MOSS_PATCH_CEILING_CONFIGURED_FEATURE, CountPlacementModifier.of(125), SquarePlacementModifier.of(), PlacedFeatures.BOTTOM_TO_120_RANGE, EnvironmentScanPlacementModifier.of(Direction.UP, BlockPredicate.solid(), BlockPredicate.IS_AIR, 12), RandomOffsetPlacementModifier.vertically(ConstantIntProvider.create(-1)), BiomePlacementModifier.of());
	public static final RegistryEntry<PlacedFeature> MARS_CAVE_POOL_PLACED_FEATURE = PlacedFeatures.register(new Identifier(StarflightMod.MOD_ID, "mars_cave_pool").toString(), MARS_CAVE_POOL_CONFIGURED_FEATURE, CountPlacementModifier.of(62), SquarePlacementModifier.of(), PlacedFeatures.BOTTOM_TO_120_RANGE, EnvironmentScanPlacementModifier.of(Direction.DOWN, BlockPredicate.solid(), BlockPredicate.IS_AIR, 12), RandomOffsetPlacementModifier.vertically(ConstantIntProvider.create(1)), BiomePlacementModifier.of());
	
	// Earth Ores
	public static final RegistryEntry<PlacedFeature> BAUXITE_ORE = orePlacedFeature("bauxite_ore", 10, 8, YOffset.BOTTOM, YOffset.fixed(128), StarflightBlocks.BAUXITE_ORE.getDefaultState(), Blocks.STONE);
	public static final RegistryEntry<PlacedFeature> BAUXITE_ORE_DEEPSLATE = orePlacedFeature("bauxite_ore_deepslate", 10, 8, YOffset.BOTTOM, YOffset.fixed(128), StarflightBlocks.DEEPSLATE_BAUXITE_ORE.getDefaultState(), Blocks.DEEPSLATE);
	public static final RegistryEntry<PlacedFeature> BAUXITE_ORE_FERRIC = orePlacedFeature("bauxite_ore_ferric", 10, 8, YOffset.BOTTOM, YOffset.fixed(128), StarflightBlocks.FERRIC_BAUXITE_ORE.getDefaultState(), StarflightBlocks.FERRIC_STONE);
	public static final RegistryEntry<PlacedFeature> SULFUR_ORE = orePlacedFeature("sulfur_ore", 10, 6, YOffset.BOTTOM, YOffset.fixed(128), StarflightBlocks.BAUXITE_ORE.getDefaultState(), Blocks.STONE);
	public static final RegistryEntry<PlacedFeature> SULFUR_ORE_DEEPSLATE = orePlacedFeature("sulfur_ore_deepslate", 10, 6, YOffset.BOTTOM, YOffset.fixed(128), StarflightBlocks.DEEPSLATE_BAUXITE_ORE.getDefaultState(), Blocks.DEEPSLATE);
	
	// Moon Ores
	public static final RegistryEntry<PlacedFeature> LESS_COPPER_ORE = orePlacedFeature("less_copper_ore", 8, 6, YOffset.BOTTOM, YOffset.fixed(112), Blocks.COPPER_ORE.getDefaultState(), Blocks.STONE);
	
	// Mars Ores
	public static final RegistryEntry<PlacedFeature> REDSLATE_FERRIC = orePlacedFeature("redslate_ferric", 64, 12, YOffset.fixed(16), YOffset.TOP, StarflightBlocks.REDSLATE.getDefaultState(), StarflightBlocks.FERRIC_STONE);
	public static final RegistryEntry<PlacedFeature> BASALT_FERRIC = orePlacedFeature("basalt_ferric", 32, 8, YOffset.BOTTOM, YOffset.TOP, Blocks.SMOOTH_BASALT.getDefaultState(), StarflightBlocks.FERRIC_STONE);
	public static final RegistryEntry<PlacedFeature> IRON_ORE_FERRIC = orePlacedFeature("iron_ore_ferric", 12, 24, YOffset.BOTTOM, YOffset.TOP, StarflightBlocks.FERRIC_IRON_ORE.getDefaultState(), StarflightBlocks.FERRIC_STONE);
	public static final RegistryEntry<PlacedFeature> COPPER_ORE_FERRIC = orePlacedFeature("copper_ore_ferric", 8, 12, 0.25f, YOffset.BOTTOM, YOffset.fixed(72), StarflightBlocks.FERRIC_COPPER_ORE.getDefaultState(), StarflightBlocks.FERRIC_STONE);
	public static final RegistryEntry<PlacedFeature> GOLD_ORE_FERRIC = orePlacedFeatureTrapezoid("gold_ore_ferric", 6, 4, 0.7f, YOffset.fixed(-64), YOffset.fixed(32), StarflightBlocks.FERRIC_GOLD_ORE.getDefaultState(), StarflightBlocks.FERRIC_STONE);
	public static final RegistryEntry<PlacedFeature> DIAMOND_ORE_FERRIC = orePlacedFeatureTrapezoid("diamond_ore_ferric", 4, 3, 0.8f, YOffset.aboveBottom(-80), YOffset.aboveBottom(80), StarflightBlocks.FERRIC_DIAMOND_ORE.getDefaultState(), StarflightBlocks.FERRIC_STONE);
	public static final RegistryEntry<PlacedFeature> REDSTONE_ORE_FERRIC = orePlacedFeatureTrapezoid("redstone_ore_ferric", 8, 8, 0.0f, YOffset.aboveBottom(-32), YOffset.aboveBottom(32), StarflightBlocks.FERRIC_REDSTONE_ORE.getDefaultState(), StarflightBlocks.FERRIC_STONE);
	public static final RegistryEntry<PlacedFeature> SULFUR_ORE_FERRIC = orePlacedFeature("sulfur_ore_ferric", 10, 6, YOffset.BOTTOM, YOffset.fixed(128), StarflightBlocks.FERRIC_SULFUR_ORE.getDefaultState(), StarflightBlocks.FERRIC_STONE);
	public static final RegistryEntry<PlacedFeature> HEMATITE_ORE = orePlacedFeature("hematite_ore", 12, 32, 0.5f, YOffset.BOTTOM, YOffset.fixed(256), StarflightBlocks.HEMATITE_ORE.getDefaultState(), StarflightBlocks.REDSLATE);

	// Tree Features
	public static final RegistryEntry<ConfiguredFeature<TreeFeatureConfig, ?>> RUBBER_TREE = ConfiguredFeatures.register(new Identifier(StarflightMod.MOD_ID, "rubber_tree").toString(), Feature.TREE, new TreeFeatureConfig.Builder(BlockStateProvider.of(StarflightBlocks.RUBBER_LOG), new StraightTrunkPlacer(5, 1, 2), BlockStateProvider.of(StarflightBlocks.RUBBER_LEAVES), new BlobFoliagePlacer(ConstantIntProvider.create(2), ConstantIntProvider.create(0), 3), new TwoLayersFeatureSize(1, 0, 1)).build());
	public static final RegistryEntry<PlacedFeature> RUBBER_TREE_CHECKED = PlacedFeatures.register(new Identifier(StarflightMod.MOD_ID, "rubber_tree_checked").toString(), RUBBER_TREE, VegetationPlacedFeatures.modifiersWithWouldSurvive(PlacedFeatures.createCountExtraModifier(0, 0.1f, 1), StarflightBlocks.RUBBER_SAPLING));
	public static final RegistryEntry<ConfiguredFeature<TreeFeatureConfig, ?>> TALL_RUBBER_TREE = ConfiguredFeatures.register(new Identifier(StarflightMod.MOD_ID, "tall_rubber_tree").toString(), Feature.TREE, new TreeFeatureConfig.Builder(BlockStateProvider.of(StarflightBlocks.RUBBER_LOG), new StraightTrunkPlacer(7, 1, 2), BlockStateProvider.of(StarflightBlocks.RUBBER_LEAVES), new BlobFoliagePlacer(ConstantIntProvider.create(3), ConstantIntProvider.create(0), 3), new TwoLayersFeatureSize(1, 0, 1)).build());
	public static final RegistryEntry<PlacedFeature> TALL_RUBBER_TREE_CHECKED = PlacedFeatures.register(new Identifier(StarflightMod.MOD_ID, "tall_rubber_tree_checked").toString(), TALL_RUBBER_TREE, VegetationPlacedFeatures.modifiersWithWouldSurvive(PlacedFeatures.createCountExtraModifier(0, 0.05f, 1), StarflightBlocks.RUBBER_SAPLING));

	public static void initializeWorldGeneration()
	{
		// Chunk Generators
		Registry.register(Registry.CHUNK_GENERATOR, new Identifier(StarflightMod.MOD_ID, "space"), SpaceChunkGenerator.CODEC);

		// Overworld Ores
		BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Feature.UNDERGROUND_ORES, BAUXITE_ORE.getKey().get());
		BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Feature.UNDERGROUND_ORES, BAUXITE_ORE_DEEPSLATE.getKey().get());
		BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Feature.UNDERGROUND_ORES, SULFUR_ORE.getKey().get());
		BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Feature.UNDERGROUND_ORES, SULFUR_ORE_DEEPSLATE.getKey().get());

		// Trees
		BiomeModifications.addFeature(BiomeSelectors.includeByKey(BiomeKeys.FOREST, BiomeKeys.SWAMP, BiomeKeys.SPARSE_JUNGLE), GenerationStep.Feature.VEGETAL_DECORATION, RUBBER_TREE_CHECKED.getKey().get());
		BiomeModifications.addFeature(BiomeSelectors.includeByKey(BiomeKeys.FOREST, BiomeKeys.SWAMP, BiomeKeys.SPARSE_JUNGLE), GenerationStep.Feature.VEGETAL_DECORATION, TALL_RUBBER_TREE_CHECKED.getKey().get());
		
		// Small Craters
		BiomeModifications.addFeature(BiomeSelectors.tag(LIGHT_CRATERING).or(BiomeSelectors.tag(MEDIUM_CRATERING)).or(BiomeSelectors.tag(HEAVY_CRATERING)), GenerationStep.Feature.TOP_LAYER_MODIFICATION, SMALL_CRATER_PLACED_FEATURE.getKey().get());
		
		// Scatter
		BiomeModifications.addFeature(BiomeSelectors.tag(SCATTER).or(BiomeSelectors.tag(MORE_SCATTER)), GenerationStep.Feature.VEGETAL_DECORATION, SURFACE_ROCK_PLACED_FEATURE.getKey().get());
		BiomeModifications.addFeature(BiomeSelectors.tag(SCATTER).or(BiomeSelectors.tag(MORE_SCATTER)), GenerationStep.Feature.VEGETAL_DECORATION, ROCK_PATCH_PLACED_FEATURE.getKey().get());
	}

	/**
	 * Register an ore placed feature with uniform y-level distribution.
	 */
	private static RegistryEntry<PlacedFeature> orePlacedFeature(String name, int size, int count, float discardOnAirChance, YOffset bottom, YOffset top, BlockState oreBlockState, Block ruleBlock)
	{
		List<OreFeatureConfig.Target> ores = List.of(OreFeatureConfig.createTarget(new BlockMatchRuleTest(ruleBlock), oreBlockState));
		RegistryEntry<ConfiguredFeature<OreFeatureConfig, ?>> configuredOreFeature = ConfiguredFeatures.register(new Identifier(StarflightMod.MOD_ID, name).toString(), Feature.ORE, new OreFeatureConfig(ores, size));
		return PlacedFeatures.register(new Identifier(StarflightMod.MOD_ID, name).toString(), configuredOreFeature, List.of(CountPlacementModifier.of(count), SquarePlacementModifier.of(), HeightRangePlacementModifier.uniform(bottom, top), BiomePlacementModifier.of()));
	}
	
	/**
	 * Register an ore placed feature with trapezoid y-level distribution.
	 */
	private static RegistryEntry<PlacedFeature> orePlacedFeatureTrapezoid(String name, int size, int count, float discardOnAirChance, YOffset bottom, YOffset top, BlockState oreBlockState, Block ruleBlock)
	{
		List<OreFeatureConfig.Target> ores = List.of(OreFeatureConfig.createTarget(new BlockMatchRuleTest(ruleBlock), oreBlockState));
		RegistryEntry<ConfiguredFeature<OreFeatureConfig, ?>> configuredOreFeature = ConfiguredFeatures.register(new Identifier(StarflightMod.MOD_ID, name).toString(), Feature.ORE, new OreFeatureConfig(ores, size));
		return PlacedFeatures.register(new Identifier(StarflightMod.MOD_ID, name).toString(), configuredOreFeature, List.of(CountPlacementModifier.of(count), SquarePlacementModifier.of(), HeightRangePlacementModifier.trapezoid(bottom, top), BiomePlacementModifier.of()));
	}
	
	/**
	 * Register an ore placed feature with uniform y-level distribution and a discard on air chance of zero.
	 */
	private static RegistryEntry<PlacedFeature> orePlacedFeature(String name, int size, int count, YOffset bottom, YOffset top, BlockState oreBlockState, Block ruleBlock)
	{
		return orePlacedFeature(name, size, count, 0.0f, bottom, top, oreBlockState, ruleBlock);
	}

	private static Structure.Config createConfig(TagKey<Biome> biomeTag, Map<SpawnGroup, StructureSpawns> spawns, GenerationStep.Feature featureStep, StructureTerrainAdaptation terrainAdaptation)
	{
		return new Structure.Config(getOrCreateBiomeTag(biomeTag), spawns, featureStep, terrainAdaptation);
	}

	private static Structure.Config createConfig(TagKey<Biome> biomeTag, StructureTerrainAdaptation terrainAdaptation)
	{
		return createConfig(biomeTag, Map.of(), GenerationStep.Feature.SURFACE_STRUCTURES, terrainAdaptation);
	}

	private static RegistryEntry<Structure> register(RegistryKey<Structure> key, Structure structure)
	{
		return BuiltinRegistries.add(BuiltinRegistries.STRUCTURE, key, structure);
	}

	private static RegistryEntryList<Biome> getOrCreateBiomeTag(TagKey<Biome> key)
	{
		return BuiltinRegistries.BIOME.getOrCreateEntryList(key);
	}
}