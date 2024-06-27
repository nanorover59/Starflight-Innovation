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
import net.minecraft.world.gen.carver.Carver;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.structure.StructureType;
import space.StarflightMod;
import space.block.StarflightBlocks;

public class StarflightWorldGeneration
{
	public static final TagKey<Biome> LIQUID_WATER = TagKey.of(RegistryKeys.BIOME, Identifier.of(StarflightMod.MOD_ID, "liquid_water"));
	public static final TagKey<Biome> ICE_CRATERS = TagKey.of(RegistryKeys.BIOME, Identifier.of(StarflightMod.MOD_ID, "ice_craters"));
	public static final RuleTest FERRIC_STONE_ORE_REPLACEABLES = new BlockMatchRuleTest(StarflightBlocks.FERRIC_STONE);
	public static final RuleTest FRIGID_STONE_ORE_REPLACEABLES = new BlockMatchRuleTest(StarflightBlocks.FRIGID_STONE);
	
	// Surface Rock Feature
	public static final Feature<DefaultFeatureConfig> SURFACE_ROCK = Registry.register(Registries.FEATURE, Identifier.of(StarflightMod.MOD_ID, "surface_rock"), new SurfaceRockFeature(DefaultFeatureConfig.CODEC));
	
	// Freeze Water Feature
	public static final Feature<DefaultFeatureConfig> FREEZE_WATER = Registry.register(Registries.FEATURE, Identifier.of(StarflightMod.MOD_ID, "freeze_water"), new FreezeWaterFeature(DefaultFeatureConfig.CODEC));
	
	// Aeroplankton Feature
	public static final Feature<DefaultFeatureConfig> AEROPLANKTON = Registry.register(Registries.FEATURE, Identifier.of(StarflightMod.MOD_ID, "aeroplankton"), new AeroplanktonFeature(DefaultFeatureConfig.CODEC));
	
	// Impact Crater Structure
	public static final StructurePieceType CRATER_PIECE = Registry.register(Registries.STRUCTURE_PIECE, Identifier.of(StarflightMod.MOD_ID, "crater_piece"), CraterGenerator.Piece::new);
	public static final StructureType<CraterStructure> CRATER_TYPE = Registry.register(Registries.STRUCTURE_TYPE, Identifier.of(StarflightMod.MOD_ID, "crater"), () -> CraterStructure.CODEC);
	
	// Bio Dome Structure
	public static final StructurePieceType OUTPOST_PIECE = Registry.register(Registries.STRUCTURE_PIECE, Identifier.of(StarflightMod.MOD_ID, "biodome_piece"), BioDomeGenerator.Piece::new);
	public static final StructureType<BioDomeStructure> OUTPOST_TYPE = Registry.register(Registries.STRUCTURE_TYPE, Identifier.of(StarflightMod.MOD_ID, "biodome"), () -> BioDomeStructure.CODEC);
	
	// Moonshaft Structure
	public static final StructurePieceType MOONSHAFT_CORRIDOR = Registry.register(Registries.STRUCTURE_PIECE, Identifier.of(StarflightMod.MOD_ID, "moonshaft_corridor"), MoonshaftGenerator.MoonshaftCorridor::new);
    public static final StructurePieceType MOONSHAFT_CROSSING = Registry.register(Registries.STRUCTURE_PIECE, Identifier.of(StarflightMod.MOD_ID, "moonshaft_crossing"), MoonshaftGenerator.MoonshaftCrossing::new);
	public static final StructureType<MoonshaftStructure> MOONSHAFT_TYPE = Registry.register(Registries.STRUCTURE_TYPE, Identifier.of(StarflightMod.MOD_ID, "moonshaft"), () -> MoonshaftStructure.CODEC);

	// Airship Structure
	public static final StructurePieceType AIRSHIP_PIECE = Registry.register(Registries.STRUCTURE_PIECE, Identifier.of(StarflightMod.MOD_ID, "airship_piece"), AirshipGenerator.Piece::new);
	public static final StructureType<AirshipStructure> AIRSHIP_TYPE = Registry.register(Registries.STRUCTURE_TYPE, Identifier.of(StarflightMod.MOD_ID, "airship"), () -> AirshipStructure.CODEC);
	
	// Impact Crater Carver
	public static final Carver<CraterCarverConfig> CRATER = Registry.register(Registries.CARVER, Identifier.of(StarflightMod.MOD_ID, "crater"), new CraterCarver(CraterCarverConfig.CRATER_CODEC));
	
	public static void initializeWorldGeneration()
	{
		// Chunk Generators
		Registry.register(Registries.CHUNK_GENERATOR, Identifier.of(StarflightMod.MOD_ID, "space"), SpaceChunkGenerator.CODEC);
		
		// Overworld Ores
		BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Feature.UNDERGROUND_ORES, RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier.of(StarflightMod.MOD_ID, "ore_bauxite")));
		BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Feature.UNDERGROUND_ORES, RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier.of(StarflightMod.MOD_ID, "ore_sulfur")));

		// Trees
		//BiomeModifications.addFeature(BiomeSelectors.includeByKey(BiomeKeys.FOREST, BiomeKeys.SWAMP, BiomeKeys.SPARSE_JUNGLE), GenerationStep.Feature.VEGETAL_DECORATION, RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier.of(StarflightMod.MOD_ID, "rubber_tree")));
		BiomeModifications.addFeature(BiomeSelectors.includeByKey(BiomeKeys.FOREST, BiomeKeys.SWAMP, BiomeKeys.SPARSE_JUNGLE), GenerationStep.Feature.VEGETAL_DECORATION, RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier.of(StarflightMod.MOD_ID, "rubber_tree")));
	}
}