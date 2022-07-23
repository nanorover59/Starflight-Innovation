package space.world;

import java.util.List;
import java.util.Map;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.structure.rule.BlockMatchRuleTest;
import net.minecraft.structure.rule.RuleTest;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
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
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.ConfiguredFeatures;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.PlacedFeatures;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.feature.VegetationPlacedFeatures;
import net.minecraft.world.gen.feature.size.TwoLayersFeatureSize;
import net.minecraft.world.gen.foliage.BlobFoliagePlacer;
import net.minecraft.world.gen.placementmodifier.BiomePlacementModifier;
import net.minecraft.world.gen.placementmodifier.CountPlacementModifier;
import net.minecraft.world.gen.placementmodifier.HeightRangePlacementModifier;
import net.minecraft.world.gen.placementmodifier.SquarePlacementModifier;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;
import net.minecraft.world.gen.trunk.StraightTrunkPlacer;
import space.StarflightMod;
import space.block.StarflightBlocks;

public class StarflightWorldGeneration
{
	public static final TagKey<Biome> IS_CRATERED = TagKey.of(Registry.BIOME_KEY, new Identifier(StarflightMod.MOD_ID, "is_cratered"));
	public static final TagKey<Biome> MORE_SCATTER = TagKey.of(Registry.BIOME_KEY, new Identifier(StarflightMod.MOD_ID, "more_scatter"));

	public static final RuleTest FERRIC_STONE_ORE_REPLACEABLES = new BlockMatchRuleTest(StarflightBlocks.FERRIC_STONE);
	public static final RuleTest FRIGID_STONE_ORE_REPLACEABLES = new BlockMatchRuleTest(StarflightBlocks.FRIGID_STONE);

	public static final StructurePieceType CRATER_PIECE = Registry.register(Registry.STRUCTURE_PIECE, new Identifier(StarflightMod.MOD_ID, "crater_piece"), CraterGenerator.Piece::new);
	 public static final StructureType<CraterStructure> CRATER_TYPE = Registry.register(Registry.STRUCTURE_TYPE, new Identifier(StarflightMod.MOD_ID, "crater"), () -> CraterStructure.CODEC);
	public static final RegistryEntry<Structure> CRATER = register(RegistryKey.of(Registry.STRUCTURE_KEY, new Identifier(StarflightMod.MOD_ID, "crater")), new CraterStructure(createConfig(IS_CRATERED, StructureTerrainAdaptation.NONE)));
	
	public static final Feature<DefaultFeatureConfig> SURFACE_ROCK = Registry.register(Registry.FEATURE, new Identifier(StarflightMod.MOD_ID, "surface_rock"), new SurfaceRockFeature(DefaultFeatureConfig.CODEC));
	public static final RegistryEntry<ConfiguredFeature<DefaultFeatureConfig, ?>> SURFACE_ROCK_CONFIGURED_FEATURE = ConfiguredFeatures.register(new Identifier(StarflightMod.MOD_ID, "surface_rock").toString(), SURFACE_ROCK);
	public static final RegistryEntry<PlacedFeature> SURFACE_ROCK_PLACED_FEATURE = PlacedFeatures.register(new Identifier(StarflightMod.MOD_ID, "surface_rock").toString(), SURFACE_ROCK_CONFIGURED_FEATURE, CountPlacementModifier.of(2), SquarePlacementModifier.of(), PlacedFeatures.OCEAN_FLOOR_WG_HEIGHTMAP, BiomePlacementModifier.of());

	public static final RegistryEntry<PlacedFeature> BAUXITE_ORE = orePlacedFeature("bauxite_ore", 10, 8, YOffset.BOTTOM, YOffset.fixed(128), StarflightBlocks.BAUXITE_ORE.getDefaultState(), Blocks.STONE);
	public static final RegistryEntry<PlacedFeature> BAUXITE_ORE_DEEPSLATE = orePlacedFeature("bauxite_ore_deepslate", 10, 8, YOffset.BOTTOM, YOffset.fixed(128), StarflightBlocks.DEEPSLATE_BAUXITE_ORE.getDefaultState(), Blocks.DEEPSLATE);
	public static final RegistryEntry<PlacedFeature> BAUXITE_ORE_FERRIC = orePlacedFeature("bauxite_ore_ferric", 10, 8, YOffset.BOTTOM, YOffset.fixed(128), StarflightBlocks.FERRIC_BAUXITE_ORE.getDefaultState(), StarflightBlocks.FERRIC_STONE);

	public static final RegistryEntry<PlacedFeature> SULFUR_ORE = orePlacedFeature("sulfur_ore", 10, 6, YOffset.BOTTOM, YOffset.fixed(128), StarflightBlocks.BAUXITE_ORE.getDefaultState(), Blocks.STONE);
	public static final RegistryEntry<PlacedFeature> SULFUR_ORE_DEEPSLATE = orePlacedFeature("sulfur_ore_deepslate", 10, 6, YOffset.BOTTOM, YOffset.fixed(128), StarflightBlocks.DEEPSLATE_BAUXITE_ORE.getDefaultState(), Blocks.DEEPSLATE);

	public static final RegistryEntry<PlacedFeature> REDSLATE_FERRIC = orePlacedFeature("redslate_ferric", 32, 8, YOffset.fixed(16), YOffset.TOP, StarflightBlocks.REDSLATE.getDefaultState(), StarflightBlocks.FERRIC_STONE);
	public static final RegistryEntry<PlacedFeature> BASALT_FERRIC = orePlacedFeature("basalt_ferric", 32, 6, YOffset.BOTTOM, YOffset.TOP, Blocks.SMOOTH_BASALT.getDefaultState(), StarflightBlocks.FERRIC_STONE);
	public static final RegistryEntry<PlacedFeature> IRON_ORE_FERRIC = orePlacedFeature("iron_ore_ferric", 12, 24, YOffset.BOTTOM, YOffset.TOP, StarflightBlocks.FERRIC_IRON_ORE.getDefaultState(), StarflightBlocks.FERRIC_STONE);
	public static final RegistryEntry<PlacedFeature> COPPER_ORE_FERRIC = orePlacedFeature("copper_ore_ferric", 12, 16, YOffset.BOTTOM, YOffset.fixed(64), StarflightBlocks.FERRIC_COPPER_ORE.getDefaultState(), StarflightBlocks.FERRIC_STONE);
	public static final RegistryEntry<PlacedFeature> GOLD_ORE_FERRIC = orePlacedFeature("gold_ore_ferric", 6, 6, YOffset.BOTTOM, YOffset.fixed(16), StarflightBlocks.FERRIC_GOLD_ORE.getDefaultState(), StarflightBlocks.FERRIC_STONE);
	public static final RegistryEntry<PlacedFeature> DIAMOND_ORE_FERRIC = orePlacedFeature("diamond_ore_ferric", 4, 4, YOffset.BOTTOM, YOffset.aboveBottom(32), StarflightBlocks.FERRIC_DIAMOND_ORE.getDefaultState(), StarflightBlocks.FERRIC_STONE);
	public static final RegistryEntry<PlacedFeature> REDSTONE_ORE_FERRIC = orePlacedFeature("redstone_ore_ferric", 8, 6, YOffset.BOTTOM, YOffset.fixed(24), StarflightBlocks.FERRIC_REDSTONE_ORE.getDefaultState(), StarflightBlocks.FERRIC_STONE);
	public static final RegistryEntry<PlacedFeature> SULFUR_ORE_FERRIC = orePlacedFeature("sulfur_ore_ferric", 10, 6, YOffset.BOTTOM, YOffset.fixed(128), StarflightBlocks.FERRIC_SULFUR_ORE.getDefaultState(), StarflightBlocks.FERRIC_STONE);

	public static final RegistryEntry<ConfiguredFeature<TreeFeatureConfig, ?>> RUBBER_TREE = ConfiguredFeatures.register(new Identifier(StarflightMod.MOD_ID, "rubber_tree").toString(), Feature.TREE, new TreeFeatureConfig.Builder(BlockStateProvider.of(StarflightBlocks.RUBBER_LOG), new StraightTrunkPlacer(5, 1, 2), BlockStateProvider.of(StarflightBlocks.RUBBER_LEAVES), new BlobFoliagePlacer(ConstantIntProvider.create(2), ConstantIntProvider.create(0), 3), new TwoLayersFeatureSize(1, 0, 1)).build());
	public static final RegistryEntry<PlacedFeature> RUBBER_TREE_CHECKED = PlacedFeatures.register(new Identifier(StarflightMod.MOD_ID, "rubber_tree_checked").toString(), RUBBER_TREE, VegetationPlacedFeatures.modifiersWithWouldSurvive(PlacedFeatures.createCountExtraModifier(0, 0.2f, 1), StarflightBlocks.RUBBER_SAPLING));
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
	}

	private static RegistryEntry<PlacedFeature> orePlacedFeature(String name, int size, int count, YOffset bottom, YOffset top, BlockState oreBlockState, Block ruleBlock)
	{
		List<OreFeatureConfig.Target> ores = List.of(OreFeatureConfig.createTarget(new BlockMatchRuleTest(ruleBlock), oreBlockState));
		RegistryEntry<ConfiguredFeature<OreFeatureConfig, ?>> configuredOreFeature = ConfiguredFeatures.register(new Identifier(StarflightMod.MOD_ID, name).toString(), Feature.ORE, new OreFeatureConfig(ores, size));
		return PlacedFeatures.register(new Identifier(StarflightMod.MOD_ID, name).toString(), configuredOreFeature, List.of(CountPlacementModifier.of(count), SquarePlacementModifier.of(), HeightRangePlacementModifier.uniform(bottom, top), BiomePlacementModifier.of()));
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