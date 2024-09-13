package space.world;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.structure.rule.BlockMatchRuleTest;
import net.minecraft.structure.rule.RuleTest;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.structure.StructureType;
import space.StarflightMod;
import space.block.StarflightBlocks;

public class StarflightWorldGeneration
{
	public static final TagKey<Biome> SCATTER = TagKey.of(RegistryKeys.BIOME, new Identifier(StarflightMod.MOD_ID, "scatter"));
	public static final TagKey<Biome> MORE_SCATTER = TagKey.of(RegistryKeys.BIOME, new Identifier(StarflightMod.MOD_ID, "more_scatter"));
	public static final TagKey<Biome> LIQUID_WATER = TagKey.of(RegistryKeys.BIOME, new Identifier(StarflightMod.MOD_ID, "liquid_water"));
	public static final TagKey<Biome> ICE_CRATERS = TagKey.of(RegistryKeys.BIOME, new Identifier(StarflightMod.MOD_ID, "ice_craters"));
	public static final RuleTest FERRIC_STONE_ORE_REPLACEABLES = new BlockMatchRuleTest(StarflightBlocks.FERRIC_STONE);
	public static final RuleTest FRIGID_STONE_ORE_REPLACEABLES = new BlockMatchRuleTest(StarflightBlocks.FRIGID_STONE);
	
	// Surface Rock Feature
	public static final Feature<DefaultFeatureConfig> SURFACE_ROCK = Registry.register(Registries.FEATURE, new Identifier(StarflightMod.MOD_ID, "surface_rock"), new SurfaceRockFeature(DefaultFeatureConfig.CODEC));
	
	// Rock Patch Feature
	public static final Feature<DefaultFeatureConfig> ROCK_PATCH = Registry.register(Registries.FEATURE, new Identifier(StarflightMod.MOD_ID, "rock_patch"), new RockPatchFeature(DefaultFeatureConfig.CODEC));
	
	// Impact Crater Structure
	public static final StructurePieceType CRATER_PIECE = Registry.register(Registries.STRUCTURE_PIECE, new Identifier(StarflightMod.MOD_ID, "crater_piece"), CraterGenerator.Piece::new);
	public static final StructureType<CraterStructure> CRATER_TYPE = Registry.register(Registries.STRUCTURE_TYPE, new Identifier(StarflightMod.MOD_ID, "crater"), () -> CraterStructure.CODEC);
	
	// Landing Site Structure
	public static final StructurePieceType LANDING_SITE_PIECE = Registry.register(Registries.STRUCTURE_PIECE, new Identifier(StarflightMod.MOD_ID, "landing_site_piece"), LandingSiteGenerator.Piece::new);
	public static final StructureType<LandingSiteStructure> LANDING_SITE_TYPE = Registry.register(Registries.STRUCTURE_TYPE, new Identifier(StarflightMod.MOD_ID, "landing_site"), () -> LandingSiteStructure.CODEC);
	
	// Surface Outpost Structure
	public static final StructurePieceType OUTPOST_PIECE = Registry.register(Registries.STRUCTURE_PIECE, new Identifier(StarflightMod.MOD_ID, "outpost_piece"), OutpostGenerator.Piece::new);
	public static final StructureType<OutpostStructure> OUTPOST_TYPE = Registry.register(Registries.STRUCTURE_TYPE, new Identifier(StarflightMod.MOD_ID, "outpost"), () -> OutpostStructure.CODEC);
	
	// Moonshaft Structure 
	public static final StructurePieceType MOONSHAFT_CORRIDOR = Registry.register(Registries.STRUCTURE_PIECE, new Identifier(StarflightMod.MOD_ID, "moonshaft_corridor"), MoonshaftGenerator.MoonshaftCorridor::new);
    public static final StructurePieceType MOONSHAFT_CROSSING = Registry.register(Registries.STRUCTURE_PIECE, new Identifier(StarflightMod.MOD_ID, "moonshaft_crossing"), MoonshaftGenerator.MoonshaftCrossing::new);
	public static final StructureType<MoonshaftStructure> MOONSHAFT_TYPE = Registry.register(Registries.STRUCTURE_TYPE, new Identifier(StarflightMod.MOD_ID, "moonshaft"), () -> MoonshaftStructure.CODEC);

	public static void initializeWorldGeneration()
	{
		// Chunk Generators
		Registry.register(Registries.CHUNK_GENERATOR, new Identifier(StarflightMod.MOD_ID, "space"), SpaceChunkGenerator.CODEC);

		// Overworld Ores
		BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Feature.UNDERGROUND_ORES, RegistryKey.of(RegistryKeys.PLACED_FEATURE, new Identifier(StarflightMod.MOD_ID, "ore_bauxite")));
		BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Feature.UNDERGROUND_ORES, RegistryKey.of(RegistryKeys.PLACED_FEATURE, new Identifier(StarflightMod.MOD_ID, "ore_sulfur")));

		// Trees
		BiomeModifications.addFeature(BiomeSelectors.includeByKey(BiomeKeys.FOREST, BiomeKeys.SWAMP, BiomeKeys.SPARSE_JUNGLE), GenerationStep.Feature.VEGETAL_DECORATION, RegistryKey.of(RegistryKeys.PLACED_FEATURE, new Identifier(StarflightMod.MOD_ID, "rubber_tree")));
		
		// Terrain Scatter
		BiomeModifications.addFeature(BiomeSelectors.tag(SCATTER).or(BiomeSelectors.tag(MORE_SCATTER)), GenerationStep.Feature.SURFACE_STRUCTURES, RegistryKey.of(RegistryKeys.PLACED_FEATURE, new Identifier(StarflightMod.MOD_ID, "surface_rock")));
		BiomeModifications.addFeature(BiomeSelectors.tag(SCATTER).or(BiomeSelectors.tag(MORE_SCATTER)), GenerationStep.Feature.SURFACE_STRUCTURES, RegistryKey.of(RegistryKeys.PLACED_FEATURE, new Identifier(StarflightMod.MOD_ID, "rock_patch"))); 
	}
}