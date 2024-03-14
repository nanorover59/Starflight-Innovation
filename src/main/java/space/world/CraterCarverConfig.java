package space.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.Block;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.math.floatprovider.FloatProvider;
import net.minecraft.world.gen.YOffset;
import net.minecraft.world.gen.carver.CarverConfig;
import net.minecraft.world.gen.carver.CarverDebugConfig;
import net.minecraft.world.gen.heightprovider.HeightProvider;

public class CraterCarverConfig extends CarverConfig
{
	public static final Codec<CraterCarverConfig> CRATER_CODEC = RecordCodecBuilder.create(instance -> instance.group(CarverConfig.CONFIG_CODEC.forGetter(config -> config)).apply(instance, CraterCarverConfig::new));

	public CraterCarverConfig(float probability, HeightProvider y, FloatProvider yScale, YOffset lavaLevel, CarverDebugConfig debugConfig, RegistryEntryList<Block> replaceable)
	{
		super(probability, y, yScale, lavaLevel, debugConfig, replaceable);
	}

	public CraterCarverConfig(CarverConfig config)
	{
		this(config.probability, config.y, config.yScale, config.lavaLevel, config.debugConfig, config.replaceable);
	}
}