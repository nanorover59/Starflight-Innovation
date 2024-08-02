package space.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.math.floatprovider.FloatProvider;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.world.gen.feature.FeatureConfig;

public class CaveIceFeatureConfig implements FeatureConfig
{
    public static final Codec<CaveIceFeatureConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group((Codec.intRange(1, 512).fieldOf("floor_to_ceiling_search_range")).forGetter(config -> config.floorToCeilingSearchRange), (IntProvider.createValidatingCodec(1, 128).fieldOf("height")).forGetter(config -> config.height), (IntProvider.createValidatingCodec(1, 128).fieldOf("radius")).forGetter(config -> config.radius), (Codec.intRange(0, 64).fieldOf("max_icicle_height_diff")).forGetter(config -> config.maxIcicleHeightDiff), (Codec.intRange(1, 64).fieldOf("height_deviation")).forGetter(config -> config.heightDeviation), (IntProvider.createValidatingCodec(0, 128).fieldOf("ice_block_layer_thickness")).forGetter(config -> config.iceBlockLayerThickness), (FloatProvider.createValidatedCodec(0.0f, 2.0f).fieldOf("density")).forGetter(config -> config.density), (FloatProvider.createValidatedCodec(0.0f, 2.0f).fieldOf("wetness")).forGetter(config -> config.wetness), (Codec.floatRange(0.0f, 1.0f).fieldOf("chance_of_ice_column_at_max_distance_from_center")).forGetter(config -> Float.valueOf(config.chanceOfIceColumnAtMaxDistanceFromCenter)), (Codec.intRange(1, 64).fieldOf("max_distance_from_edge_affecting_chance_of_ice_column")).forGetter(config -> config.maxDistanceFromCenterAffectingChanceOfIceColumn), (Codec.intRange(1, 64).fieldOf("max_distance_from_center_affecting_height_bias")).forGetter(config -> config.maxDistanceFromCenterAffectingHeightBias)).apply(instance, CaveIceFeatureConfig::new));
    public final int floorToCeilingSearchRange;
    public final IntProvider height;
    public final IntProvider radius;
    public final int maxIcicleHeightDiff;
    public final int heightDeviation;
    public final IntProvider iceBlockLayerThickness;
    public final FloatProvider density;
    public final FloatProvider wetness;
    public final float chanceOfIceColumnAtMaxDistanceFromCenter;
    public final int maxDistanceFromCenterAffectingChanceOfIceColumn;
    public final int maxDistanceFromCenterAffectingHeightBias;

    public CaveIceFeatureConfig(int floorToCeilingSearchRange, IntProvider height, IntProvider radius, int maxIcicleHeightDiff, int heightDeviation, IntProvider iceBlockLayerThickness, FloatProvider density, FloatProvider wetness, float wetnessMean, int maxDistanceFromCenterAffectingChanceOfIceColumn, int maxDistanceFromCenterAffectingHeightBias)
    {
        this.floorToCeilingSearchRange = floorToCeilingSearchRange;
        this.height = height;
        this.radius = radius;
        this.maxIcicleHeightDiff = maxIcicleHeightDiff;
        this.heightDeviation = heightDeviation;
        this.iceBlockLayerThickness = iceBlockLayerThickness;
        this.density = density;
        this.wetness = wetness;
        this.chanceOfIceColumnAtMaxDistanceFromCenter = wetnessMean;
        this.maxDistanceFromCenterAffectingChanceOfIceColumn = maxDistanceFromCenterAffectingChanceOfIceColumn;
        this.maxDistanceFromCenterAffectingHeightBias = maxDistanceFromCenterAffectingHeightBias;
    }
}