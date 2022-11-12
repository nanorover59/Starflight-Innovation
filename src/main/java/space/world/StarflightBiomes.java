package space.world;

import java.util.ArrayList;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.SpawnGroup;
import net.minecraft.sound.BiomeAdditionsSound;
import net.minecraft.sound.BiomeMoodSound;
import net.minecraft.sound.MusicSound;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeEffects;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.carver.ConfiguredCarvers;
import net.minecraft.world.gen.feature.DefaultBiomeFeatures;
import net.minecraft.world.gen.feature.OrePlacedFeatures;
import net.minecraft.world.gen.feature.UndergroundPlacedFeatures;
import space.StarflightMod;
import space.entity.StarflightEntities;
import space.util.StarflightEffects;

public class StarflightBiomes
{
	private static ArrayList<CustomSurfaceBuilder> surfaceBuilders = new ArrayList<CustomSurfaceBuilder>();
	public static final RegistryKey<Biome> SPACE = registerBiomeKey("space");
	public static final RegistryKey<Biome> MOON_LOWLANDS = registerBiomeKey("moon_lowlands");
	public static final RegistryKey<Biome> MOON_MIDLANDS = registerBiomeKey("moon_midlands");
	public static final RegistryKey<Biome> MOON_HIGHLANDS = registerBiomeKey("moon_highlands");
	public static final RegistryKey<Biome> MOON_ICE = registerBiomeKey("moon_ice");
	public static final RegistryKey<Biome> MOON_ROCKS = registerBiomeKey("moon_rocks");
	public static final RegistryKey<Biome> MARS_LOWLANDS = registerBiomeKey("mars_lowlands");
	public static final RegistryKey<Biome> MARS_MIDLANDS = registerBiomeKey("mars_midlands");
	public static final RegistryKey<Biome> MARS_HIGHLANDS = registerBiomeKey("mars_highlands");
	public static final RegistryKey<Biome> MARS_ICE = registerBiomeKey("mars_ice");
	public static final RegistryKey<Biome> MARS_DRIPSTONE = registerBiomeKey("mars_dripstone");
	public static final RegistryKey<Biome> MARS_LUSH_CAVES = registerBiomeKey("mars_lush_caves");
	
	private static final BiomeAdditionsSound MARS_WIND = new BiomeAdditionsSound(StarflightEffects.MARS_WIND_SOUND_EVENT, 0.0005);
	
	public static void initializeBiomes()
	{
		createSpaceBiome(SPACE);
		
		createMoonBiome(MOON_LOWLANDS);
		createMoonBiome(MOON_MIDLANDS);
		createMoonBiome(MOON_HIGHLANDS);
		createMoonBiome(MOON_ICE);
		createMoonBiome(MOON_ROCKS);
		
		createMarsBiome(MARS_LOWLANDS, true);
		createMarsBiome(MARS_MIDLANDS, true);
		createMarsBiome(MARS_HIGHLANDS, false);
		createMarsBiome(MARS_ICE, false);
		createMarsBiome(MARS_DRIPSTONE, false);
		createMarsBiome(MARS_LUSH_CAVES, false);
		
		surfaceBuilders.add(new MoonSurfaceBuilder());
		surfaceBuilders.add(new MarsSurfaceBuilder());
	}
	
	private static RegistryKey<Biome> registerBiomeKey(String name)
	{
        return RegistryKey.of(Registry.BIOME_KEY, new Identifier(StarflightMod.MOD_ID, name));
    }
	
	private static Biome createBiome(Biome.Precipitation precipitation, float temperature, float downfall, int waterColor, int waterFogColor, int fogColor, int skyColor, SpawnSettings.Builder spawnSettings, GenerationSettings.Builder generationSettings, @Nullable MusicSound music)
	{
        return new Biome.Builder().precipitation(precipitation).temperature(temperature).downfall(downfall).effects(new BiomeEffects.Builder().waterColor(waterColor).waterFogColor(waterFogColor).fogColor(fogColor).skyColor(skyColor).moodSound(BiomeMoodSound.CAVE).music(music).build()).spawnSettings(spawnSettings.build()).generationSettings(generationSettings.build()).build();
    }
	
	private static Biome createBiome(Biome.Precipitation precipitation, float temperature, float downfall, int waterColor, int waterFogColor, int fogColor, int skyColor, SpawnSettings.Builder spawnSettings, GenerationSettings.Builder generationSettings, BiomeMoodSound moodSound, BiomeAdditionsSound additionsSound, @Nullable MusicSound music)
	{
        return new Biome.Builder().precipitation(precipitation).temperature(temperature).downfall(downfall).effects(new BiomeEffects.Builder().waterColor(waterColor).waterFogColor(waterFogColor).fogColor(fogColor).skyColor(skyColor).moodSound(moodSound).additionsSound(additionsSound).music(music).build()).spawnSettings(spawnSettings.build()).generationSettings(generationSettings.build()).build();
    }
	
	public static CustomSurfaceBuilder getSurfaceBuilder(RegistryKey<Biome> biome)
	{
		for(CustomSurfaceBuilder surfaceBuilder : surfaceBuilders)
		{
			if(surfaceBuilder.isForBiome(biome))
				return surfaceBuilder;
		}
		
		return null;
	}
	
	/*private static void addDefaultOres(GenerationSettings.Builder generationSettings)
	{
		generationSettings.feature(GenerationStep.Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_IRON_UPPER);
		generationSettings.feature(GenerationStep.Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_IRON_MIDDLE);
		generationSettings.feature(GenerationStep.Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_IRON_SMALL);
		generationSettings.feature(GenerationStep.Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_GOLD);
		generationSettings.feature(GenerationStep.Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_GOLD_LOWER);
		generationSettings.feature(GenerationStep.Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_REDSTONE);
		generationSettings.feature(GenerationStep.Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_REDSTONE_LOWER);
		generationSettings.feature(GenerationStep.Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_DIAMOND);
		generationSettings.feature(GenerationStep.Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_DIAMOND_LARGE);
		generationSettings.feature(GenerationStep.Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_DIAMOND_BURIED);
		generationSettings.feature(GenerationStep.Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_LAPIS);
		generationSettings.feature(GenerationStep.Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_LAPIS_BURIED);
		generationSettings.feature(GenerationStep.Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_COPPER);
		generationSettings.feature(GenerationStep.Feature.UNDERGROUND_ORES, StarflightWorldGeneration.BAUXITE_ORE);
		generationSettings.feature(GenerationStep.Feature.UNDERGROUND_ORES, StarflightWorldGeneration.BAUXITE_ORE_DEEPSLATE);
		generationSettings.feature(GenerationStep.Feature.UNDERGROUND_ORES, StarflightWorldGeneration.SULFUR_ORE);
		generationSettings.feature(GenerationStep.Feature.UNDERGROUND_ORES, StarflightWorldGeneration.SULFUR_ORE_DEEPSLATE);
	}*/
	
	private static void addMoonOres(GenerationSettings.Builder generationSettings)
	{
		generationSettings.feature(GenerationStep.Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_IRON_UPPER);
		generationSettings.feature(GenerationStep.Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_IRON_MIDDLE);
		generationSettings.feature(GenerationStep.Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_IRON_SMALL);
		generationSettings.feature(GenerationStep.Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_GOLD);
		generationSettings.feature(GenerationStep.Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_GOLD_LOWER);
		generationSettings.feature(GenerationStep.Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_REDSTONE_LOWER);
		generationSettings.feature(GenerationStep.Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_DIAMOND);
		generationSettings.feature(GenerationStep.Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_DIAMOND_BURIED);
		generationSettings.feature(GenerationStep.Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_LAPIS);
		generationSettings.feature(GenerationStep.Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_LAPIS_BURIED);
		generationSettings.feature(GenerationStep.Feature.UNDERGROUND_ORES, StarflightWorldGeneration.LESS_COPPER_ORE);
		generationSettings.feature(GenerationStep.Feature.UNDERGROUND_ORES, StarflightWorldGeneration.BAUXITE_ORE);
		generationSettings.feature(GenerationStep.Feature.UNDERGROUND_ORES, StarflightWorldGeneration.SULFUR_ORE);
	}
	
	private static void addMarsOres(GenerationSettings.Builder generationSettings)
	{
		generationSettings.feature(GenerationStep.Feature.RAW_GENERATION, StarflightWorldGeneration.REDSLATE_FERRIC);
		generationSettings.feature(GenerationStep.Feature.RAW_GENERATION, StarflightWorldGeneration.BASALT_FERRIC);
		generationSettings.feature(GenerationStep.Feature.UNDERGROUND_ORES, StarflightWorldGeneration.IRON_ORE_FERRIC);
		generationSettings.feature(GenerationStep.Feature.UNDERGROUND_ORES, StarflightWorldGeneration.COPPER_ORE_FERRIC);
		generationSettings.feature(GenerationStep.Feature.UNDERGROUND_ORES, StarflightWorldGeneration.BAUXITE_ORE_FERRIC);
		generationSettings.feature(GenerationStep.Feature.UNDERGROUND_ORES, StarflightWorldGeneration.GOLD_ORE_FERRIC);
		generationSettings.feature(GenerationStep.Feature.UNDERGROUND_ORES, StarflightWorldGeneration.DIAMOND_ORE_FERRIC);
		generationSettings.feature(GenerationStep.Feature.UNDERGROUND_ORES, StarflightWorldGeneration.REDSTONE_ORE_FERRIC);
		generationSettings.feature(GenerationStep.Feature.UNDERGROUND_ORES, StarflightWorldGeneration.SULFUR_ORE_FERRIC);
		generationSettings.feature(GenerationStep.Feature.UNDERGROUND_ORES, StarflightWorldGeneration.HEMATITE_ORE);
	}
	
	private static void createSpaceBiome(RegistryKey<Biome> biomeKey)
	{
		Biome.Precipitation precipitation = Biome.Precipitation.NONE;
		float temperature = 0.5f;
		float downfall = 0.0f;
		int waterColor = 4159204;
		int waterFogColor = 329011;
		int fogColor = 0x000000;
		int skyColor = 0x000000;
		SpawnSettings.Builder spawnSettings = new SpawnSettings.Builder();
		GenerationSettings.Builder generationSettings = new GenerationSettings.Builder();
		Biome biome = createBiome(precipitation, temperature, downfall, waterColor, waterFogColor, fogColor, skyColor, spawnSettings, generationSettings, null);
		Registry.register(BuiltinRegistries.BIOME, biomeKey, biome);
	}
	
	private static void createMoonBiome(RegistryKey<Biome> biomeKey)
	{
		Biome.Precipitation precipitation = Biome.Precipitation.NONE;
		float temperature = 0.5f;
		float downfall = 0.0f;
		int waterColor = 0x3f76e4;
		int waterFogColor = 0x050533;
		int fogColor = 0x000000;
		int skyColor = 0x000000;
		SpawnSettings.Builder spawnSettings = new SpawnSettings.Builder();
		spawnSettings.spawn(SpawnGroup.MONSTER, new SpawnSettings.SpawnEntry(StarflightEntities.CERULEAN, 10, 1, 4));
		GenerationSettings.Builder generationSettings = new GenerationSettings.Builder();
		addMoonOres(generationSettings);
		generationSettings.feature(GenerationStep.Feature.TOP_LAYER_MODIFICATION, StarflightWorldGeneration.SURFACE_ROCK_PLACED_FEATURE);
		
		if(biomeKey.getValue().getPath().contains("ice"))
			generationSettings.feature(GenerationStep.Feature.UNDERGROUND_DECORATION, StarflightWorldGeneration.ICE_BLADE_PLACED_FEATURE);
		
		Biome biome = createBiome(precipitation, temperature, downfall, waterColor, waterFogColor, fogColor, skyColor, spawnSettings, generationSettings, null);
		Registry.register(BuiltinRegistries.BIOME, biomeKey, biome);
	}
	
	private static void createMarsBiome(RegistryKey<Biome> biomeKey, boolean surfaceRocks)
	{
		Biome.Precipitation precipitation = Biome.Precipitation.NONE;
		float temperature = 0.5f;
		float downfall = 0.0f;
		int waterColor = 0x3f76e4;
		int waterFogColor = 0x050533;
		int fogColor = 0xfed48c;
		int skyColor = 0xfed48c;
		SpawnSettings.Builder spawnSettings = new SpawnSettings.Builder();
		GenerationSettings.Builder generationSettings = new GenerationSettings.Builder();
		addMarsOres(generationSettings);
		generationSettings.carver(GenerationStep.Carver.AIR, ConfiguredCarvers.CAVE);
		generationSettings.carver(GenerationStep.Carver.AIR, ConfiguredCarvers.CAVE_EXTRA_UNDERGROUND);
		generationSettings.carver(GenerationStep.Carver.AIR, ConfiguredCarvers.CANYON);
		generationSettings.feature(GenerationStep.Feature.UNDERGROUND_DECORATION, UndergroundPlacedFeatures.UNDERWATER_MAGMA);
		
		if(surfaceRocks)
			generationSettings.feature(GenerationStep.Feature.TOP_LAYER_MODIFICATION, StarflightWorldGeneration.SURFACE_ROCK_PLACED_FEATURE);
		
		if(biomeKey.getValue().getPath().contains("dripstone"))
			DefaultBiomeFeatures.addDripstone(generationSettings);
		else if(biomeKey.getValue().getPath().contains("lush_caves"))
		{
			generationSettings.feature(GenerationStep.Feature.VEGETAL_DECORATION, StarflightWorldGeneration.MARS_GLOW_LICHEN_PLACED_FEATURE);
			generationSettings.feature(GenerationStep.Feature.VEGETAL_DECORATION, StarflightWorldGeneration.ARES_MOSS_PATCH_PLACED_FEATURE);
			generationSettings.feature(GenerationStep.Feature.VEGETAL_DECORATION, StarflightWorldGeneration.ARES_MOSS_PATCH_CEILING_PLACED_FEATURE);
			generationSettings.feature(GenerationStep.Feature.VEGETAL_DECORATION, StarflightWorldGeneration.MARS_CAVE_POOL_PLACED_FEATURE);
		}
		
		Biome biome = createBiome(precipitation, temperature, downfall, waterColor, waterFogColor, fogColor, skyColor, spawnSettings, generationSettings, BiomeMoodSound.CAVE, MARS_WIND, null);
		Registry.register(BuiltinRegistries.BIOME, biomeKey, biome);
	}
}