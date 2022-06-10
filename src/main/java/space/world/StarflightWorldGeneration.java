package space.world;

import java.util.List;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.block.Blocks;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.YOffset;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.ConfiguredFeatures;
import net.minecraft.world.gen.feature.ConfiguredStructureFeature;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreConfiguredFeatures;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.PlacedFeatures;
import net.minecraft.world.gen.feature.SingleStateFeatureConfig;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.feature.VegetationPlacedFeatures;
import net.minecraft.world.gen.feature.size.TwoLayersFeatureSize;
import net.minecraft.world.gen.foliage.BlobFoliagePlacer;
import net.minecraft.world.gen.placementmodifier.BiomePlacementModifier;
import net.minecraft.world.gen.placementmodifier.CountPlacementModifier;
import net.minecraft.world.gen.placementmodifier.HeightRangePlacementModifier;
import net.minecraft.world.gen.placementmodifier.SquarePlacementModifier;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.trunk.StraightTrunkPlacer;
import space.StarflightMod;
import space.block.StarflightBlocks;
import space.mixin.ConfiguredStructureFeaturesMixin;
import space.mixin.StructureFeatureMixin;

public class StarflightWorldGeneration
{
	public static final TagKey<Biome> IS_CRATERED = TagKey.of(Registry.BIOME_KEY, new Identifier(StarflightMod.MOD_ID, "is_cratered"));
	public static final StructurePieceType CRATER_PIECE = Registry.register(Registry.STRUCTURE_PIECE, new Identifier(StarflightMod.MOD_ID, "crater_piece"), CraterGenerator.Piece::new);
	public static final StructureFeature<DefaultFeatureConfig> CRATER_FEATURE = StructureFeatureMixin.invokeRegister(new Identifier(StarflightMod.MOD_ID, "crater").toString(), new CraterFeature(DefaultFeatureConfig.CODEC), GenerationStep.Feature.SURFACE_STRUCTURES);
	public static final RegistryKey<ConfiguredStructureFeature<?, ?>> CRATER_FEATURE_KEY = RegistryKey.of(Registry.CONFIGURED_STRUCTURE_FEATURE_KEY, new Identifier(StarflightMod.MOD_ID, "crater"));
	public static final RegistryEntry<ConfiguredStructureFeature<?, ?>> CRATER_CONFIGURED_FEATURE = ConfiguredStructureFeaturesMixin.invokeRegister(CRATER_FEATURE_KEY, CRATER_FEATURE.configure(DefaultFeatureConfig.DEFAULT, IS_CRATERED));
	
	public static final Feature<SingleStateFeatureConfig> BASALT_ROCK = Registry.register(Registry.FEATURE, new Identifier(StarflightMod.MOD_ID, "basalt_rock"), new SurfaceRockFeature(SingleStateFeatureConfig.CODEC));
	public static final RegistryEntry<ConfiguredFeature<SingleStateFeatureConfig, ?>> BASALT_ROCK_CONFIGURED_FEATURE = ConfiguredFeatures.register(new Identifier(StarflightMod.MOD_ID, "basalt_rock").toString(), BASALT_ROCK, new SingleStateFeatureConfig(Blocks.SMOOTH_BASALT.getDefaultState()));
	public static final RegistryEntry<PlacedFeature> BASALT_ROCK_PLACED_FEATURE = PlacedFeatures.register(new Identifier(StarflightMod.MOD_ID, "basalt_rock").toString(), BASALT_ROCK_CONFIGURED_FEATURE, CountPlacementModifier.of(2), SquarePlacementModifier.of(), PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP, BiomePlacementModifier.of());
	
	public static final List<OreFeatureConfig.Target> BAUXITE_ORES = List.of(OreFeatureConfig.createTarget(OreConfiguredFeatures.STONE_ORE_REPLACEABLES, StarflightBlocks.BAUXITE_ORE.getDefaultState()), OreFeatureConfig.createTarget(OreConfiguredFeatures.DEEPSLATE_ORE_REPLACEABLES, StarflightBlocks.DEEPSLATE_BAUXITE_ORE.getDefaultState()));
	public static final RegistryEntry<ConfiguredFeature<OreFeatureConfig, ?>> ORE_BAUXITE = ConfiguredFeatures.register(new Identifier(StarflightMod.MOD_ID, "ore_bauxite").toString(), Feature.ORE, new OreFeatureConfig(BAUXITE_ORES, 12));
	public static final RegistryEntry<PlacedFeature> PLACED_ORE_BAUXITE = PlacedFeatures.register(new Identifier(StarflightMod.MOD_ID, "ore_bauxite").toString(), ORE_BAUXITE, List.of(CountPlacementModifier.of(8), SquarePlacementModifier.of(), HeightRangePlacementModifier.uniform(YOffset.fixed(-24), YOffset.fixed(128)), BiomePlacementModifier.of()));
	public static final RegistryEntry<PlacedFeature> PLACED_ORE_BAUXITE_LOWER = PlacedFeatures.register(new Identifier(StarflightMod.MOD_ID, "ore_bauxite_lower").toString(), ORE_BAUXITE, List.of(CountPlacementModifier.of(12), SquarePlacementModifier.of(), HeightRangePlacementModifier.uniform(YOffset.getBottom(), YOffset.fixed(12)), BiomePlacementModifier.of()));
	
	public static final List<OreFeatureConfig.Target> SULFUR_ORES = List.of(OreFeatureConfig.createTarget(OreConfiguredFeatures.STONE_ORE_REPLACEABLES, StarflightBlocks.SULFUR_ORE.getDefaultState()), OreFeatureConfig.createTarget(OreConfiguredFeatures.DEEPSLATE_ORE_REPLACEABLES, StarflightBlocks.DEEPSLATE_SULFUR_ORE.getDefaultState()));
	public static final RegistryEntry<ConfiguredFeature<OreFeatureConfig, ?>> ORE_SULFUR = ConfiguredFeatures.register(new Identifier(StarflightMod.MOD_ID, "ore_sulfur").toString(), Feature.ORE, new OreFeatureConfig(SULFUR_ORES, 8));
	public static final RegistryEntry<PlacedFeature> PLACED_ORE_SULFUR = PlacedFeatures.register(new Identifier(StarflightMod.MOD_ID, "ore_sulfur").toString(), ORE_SULFUR, List.of(CountPlacementModifier.of(8), SquarePlacementModifier.of(), HeightRangePlacementModifier.uniform(YOffset.fixed(-24), YOffset.fixed(128)), BiomePlacementModifier.of()));
	public static final RegistryEntry<PlacedFeature> PLACED_ORE_SULFUR_LOWER = PlacedFeatures.register(new Identifier(StarflightMod.MOD_ID, "ore_sulfur_lower").toString(), ORE_SULFUR, List.of(CountPlacementModifier.of(12), SquarePlacementModifier.of(), HeightRangePlacementModifier.uniform(YOffset.getBottom(), YOffset.fixed(12)), BiomePlacementModifier.of()));
	
	public static final List<OreFeatureConfig.Target> IRON_ORES = List.of(OreFeatureConfig.createTarget(OreConfiguredFeatures.STONE_ORE_REPLACEABLES, Blocks.IRON_ORE.getDefaultState()), OreFeatureConfig.createTarget(OreConfiguredFeatures.DEEPSLATE_ORE_REPLACEABLES, Blocks.DEEPSLATE_IRON_ORE.getDefaultState()));
	public static final RegistryEntry<ConfiguredFeature<OreFeatureConfig, ?>> ORE_IRON_EXTRA = ConfiguredFeatures.register(new Identifier(StarflightMod.MOD_ID, "ore_iron_extra").toString(), Feature.ORE, new OreFeatureConfig(IRON_ORES, 16));
	public static final RegistryEntry<PlacedFeature> PLACED_ORE_IRON_EXTRA = PlacedFeatures.register(new Identifier(StarflightMod.MOD_ID, "ore_iron_extra").toString(), ORE_IRON_EXTRA, List.of(CountPlacementModifier.of(20), SquarePlacementModifier.of(), HeightRangePlacementModifier.uniform(YOffset.getBottom(), YOffset.getTop()), BiomePlacementModifier.of()));
	
	public static final RegistryEntry<ConfiguredFeature<TreeFeatureConfig, ?>> RUBBER_TREE = ConfiguredFeatures.register(new Identifier(StarflightMod.MOD_ID, "rubber_tree").toString(), Feature.TREE, new TreeFeatureConfig.Builder(BlockStateProvider.of(StarflightBlocks.RUBBER_LOG), new StraightTrunkPlacer(5, 1, 2), BlockStateProvider.of(StarflightBlocks.RUBBER_LEAVES), new BlobFoliagePlacer(ConstantIntProvider.create(2), ConstantIntProvider.create(0), 3), new TwoLayersFeatureSize(1, 0, 1)).build());
	public static final RegistryEntry<PlacedFeature> RUBBER_TREE_CHECKED = PlacedFeatures.register(new Identifier(StarflightMod.MOD_ID, "rubber_tree_checked").toString(), RUBBER_TREE, VegetationPlacedFeatures.modifiersWithWouldSurvive(PlacedFeatures.createCountExtraModifier(0, 0.2f, 1),StarflightBlocks.RUBBER_SAPLING));
	public static final RegistryEntry<ConfiguredFeature<TreeFeatureConfig, ?>> TALL_RUBBER_TREE = ConfiguredFeatures.register(new Identifier(StarflightMod.MOD_ID, "tall_rubber_tree").toString(), Feature.TREE, new TreeFeatureConfig.Builder(BlockStateProvider.of(StarflightBlocks.RUBBER_LOG), new StraightTrunkPlacer(7, 1, 2), BlockStateProvider.of(StarflightBlocks.RUBBER_LEAVES), new BlobFoliagePlacer(ConstantIntProvider.create(3), ConstantIntProvider.create(0), 3), new TwoLayersFeatureSize(1, 0, 1)).build());
	public static final RegistryEntry<PlacedFeature> TALL_RUBBER_TREE_CHECKED = PlacedFeatures.register(new Identifier(StarflightMod.MOD_ID, "tall_rubber_tree_checked").toString(), TALL_RUBBER_TREE, VegetationPlacedFeatures.modifiersWithWouldSurvive(PlacedFeatures.createCountExtraModifier(0, 0.05f, 1),StarflightBlocks.RUBBER_SAPLING));
	
	public static void initializeWorldGeneration()
	{
		// Chunk Generators
		Registry.register(Registry.CHUNK_GENERATOR, new Identifier(StarflightMod.MOD_ID, "space"), SpaceChunkGenerator.CODEC);
		
		// Ores
		BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Feature.UNDERGROUND_ORES, PLACED_ORE_BAUXITE.getKey().get());
		BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Feature.UNDERGROUND_ORES, PLACED_ORE_BAUXITE_LOWER.getKey().get());
		BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Feature.UNDERGROUND_ORES, PLACED_ORE_SULFUR.getKey().get());
		BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Feature.UNDERGROUND_ORES, PLACED_ORE_SULFUR_LOWER.getKey().get());
		
		// Trees
		BiomeModifications.addFeature(BiomeSelectors.includeByKey(BiomeKeys.FOREST, BiomeKeys.SWAMP, BiomeKeys.SPARSE_JUNGLE), GenerationStep.Feature.VEGETAL_DECORATION, RUBBER_TREE_CHECKED.getKey().get());
		BiomeModifications.addFeature(BiomeSelectors.includeByKey(BiomeKeys.FOREST, BiomeKeys.SWAMP, BiomeKeys.SPARSE_JUNGLE), GenerationStep.Feature.VEGETAL_DECORATION, TALL_RUBBER_TREE_CHECKED.getKey().get());
	}
}