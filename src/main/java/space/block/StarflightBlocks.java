package space.block;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.ToIntFunction;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CarpetBlock;
import net.minecraft.block.ColoredFallingBlock;
import net.minecraft.block.ExperienceDroppingBlock;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.MapColor;
import net.minecraft.block.MossBlock;
import net.minecraft.block.PillarBlock;
import net.minecraft.block.RedstoneOreBlock;
import net.minecraft.block.SaplingBlock;
import net.minecraft.block.SaplingGenerator;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.Instrument;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ColorCode;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.BlockView;
import space.StarflightMod;
import space.block.entity.AtmosphereGeneratorBlockEntity;
import space.block.entity.BalloonControllerBlockEntity;
import space.block.entity.BatteryBlockEntity;
import space.block.entity.ElectricFurnaceBlockEntity;
import space.block.entity.ElectrolyzerBlockEntity;
import space.block.entity.ExtractorBlockEntity;
import space.block.entity.FluidPipeBlockEntity;
import space.block.entity.FluidTankControllerBlockEntity;
import space.block.entity.LeakBlockEntity;
import space.block.entity.PumpBlockEntity;
import space.block.entity.RocketControllerBlockEntity;
import space.block.entity.SolarPanelBlockEntity;
import space.block.entity.StirlingEngineBlockEntity;
import space.block.entity.StorageCubeBlockEntity;
import space.block.entity.ValveBlockEntity;
import space.block.entity.VentBlockEntity;
import space.block.entity.WaterTankBlockEntity;
import space.item.DescriptiveBlockItem;
import space.item.StarflightItems;
import space.mixin.common.FireBlockInvokerMixin;
import space.util.FluidResourceType;

public class StarflightBlocks
{
	// Blocks
	public static final Block ALUMINUM_BLOCK = new Block(FabricBlockSettings.create().mapColor(MapColor.IRON_GRAY).instrument(Instrument.IRON_XYLOPHONE).requiresTool().strength(4.0f, 5.0f).sounds(BlockSoundGroup.COPPER));
	public static final Block STRUCTURAL_ALUMINUM = new Block(FabricBlockSettings.copyOf(ALUMINUM_BLOCK));
	public static final Block RIVETED_ALUMINUM = new Block(FabricBlockSettings.copyOf(STRUCTURAL_ALUMINUM));
	public static final Block ALUMINUM_FRAME = new FrameBlock(FabricBlockSettings.copyOf(ALUMINUM_BLOCK).strength(3.0f, 3.0f).nonOpaque());
	public static final Block WALKWAY = new FrameBlock(FabricBlockSettings.copyOf(ALUMINUM_FRAME).nonOpaque());
	public static final Block STRUCTURAL_ALUMINUM_STAIRS = new StairsBlock(STRUCTURAL_ALUMINUM.getDefaultState(), FabricBlockSettings.copyOf(STRUCTURAL_ALUMINUM));
	public static final Block STRUCTURAL_ALUMINUM_SLAB = new SlabBlock(FabricBlockSettings.copyOf(STRUCTURAL_ALUMINUM));
	public static final Block REINFORCED_FABRIC = new Block(FabricBlockSettings.copyOf(Blocks.WHITE_WOOL).strength(2.0f, 3.0f));
	public static final Block BAUXITE_BLOCK = new Block(FabricBlockSettings.copyOf(Blocks.RAW_IRON_BLOCK));
	public static final Block BAUXITE_ORE = new ExperienceDroppingBlock(ConstantIntProvider.create(0), FabricBlockSettings.copyOf(Blocks.IRON_ORE));
	public static final Block DEEPSLATE_BAUXITE_ORE = new ExperienceDroppingBlock(ConstantIntProvider.create(0), AbstractBlock.Settings.copy(Blocks.DEEPSLATE_IRON_ORE));
	public static final Block TITANIUM_BLOCK = new Block(FabricBlockSettings.copyOf(ALUMINUM_BLOCK).strength(6.0f, 7.0f));
	public static final Block ILMENITE_BLOCK = new Block(FabricBlockSettings.copyOf(Blocks.RAW_GOLD_BLOCK));
	public static final Block ILMENITE_ORE = new ExperienceDroppingBlock(ConstantIntProvider.create(0), FabricBlockSettings.copyOf(Blocks.GOLD_ORE));
	public static final Block DEEPSLATE_ILMENITE_ORE = new ExperienceDroppingBlock(ConstantIntProvider.create(0), FabricBlockSettings.copyOf(Blocks.DEEPSLATE_GOLD_ORE));
	public static final Block SULFUR_BLOCK = new Block(FabricBlockSettings.copyOf(Blocks.COAL_BLOCK).strength(3.0f, 5.0f));
	public static final Block SULFUR_ORE = new ExperienceDroppingBlock(UniformIntProvider.create(0, 3), FabricBlockSettings.copyOf(Blocks.COAL_ORE));
	public static final Block DEEPSLATE_SULFUR_ORE = new ExperienceDroppingBlock(UniformIntProvider.create(0, 3), FabricBlockSettings.copyOf(Blocks.DEEPSLATE_COAL_ORE));
	public static final Block RUBBER_LOG = createLogBlock(MapColor.OAK_TAN, MapColor.SPRUCE_BROWN);
	public static final Block SAPPY_RUBBER_LOG = createLogBlock(MapColor.OAK_TAN, MapColor.SPRUCE_BROWN);
	public static final Block STRIPPED_RUBBER_LOG = createLogBlock(MapColor.OAK_TAN, MapColor.OAK_TAN);
	public static final Block RUBBER_LEAVES = createLeavesBlock(BlockSoundGroup.WET_GRASS);
	public static final Block RUBBER_SAPLING = new SaplingBlock(new SaplingGenerator("rubber", Optional.of(RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE, new Identifier(StarflightMod.MOD_ID, "rubber_tree"))), Optional.empty(), Optional.empty()), FabricBlockSettings.copyOf(Blocks.OAK_SAPLING));
	public static final Block CHEESE_BLOCK = new Block(FabricBlockSettings.copyOf(Blocks.WET_SPONGE).strength(1.0f, 1.0f));
	public static final Block REGOLITH = new ColoredFallingBlock(new ColorCode(-8356741), FabricBlockSettings.copyOf(Blocks.GRAVEL).mapColor(MapColor.LIGHT_GRAY).strength(0.5F));
	public static final Block BALSALTIC_REGOLITH = new ColoredFallingBlock(new ColorCode(-8356741), FabricBlockSettings.copyOf(Blocks.GRAVEL).mapColor(MapColor.STONE_GRAY).strength(0.5F));
	public static final Block ICY_REGOLITH = new ColoredFallingBlock(new ColorCode(-8356741), FabricBlockSettings.copyOf(Blocks.GRAVEL).mapColor(MapColor.LIGHT_BLUE_GRAY).strength(0.5F));
	public static final Block FERRIC_SAND = new ColoredFallingBlock(new ColorCode(0xB7633D), FabricBlockSettings.copyOf(Blocks.SAND).mapColor(MapColor.ORANGE));
	public static final Block FERRIC_STONE = new Block(FabricBlockSettings.copyOf(Blocks.STONE));
	public static final Block REDSLATE = new Block(FabricBlockSettings.copyOf(Blocks.SANDSTONE));
	public static final Block REDSLATE_BRICKS = new Block(FabricBlockSettings.copyOf(REDSLATE));
	public static final Block REDSLATE_BRICK_STAIRS = new StairsBlock(REDSLATE_BRICKS.getDefaultState(), FabricBlockSettings.copyOf(REDSLATE_BRICKS));
	public static final Block REDSLATE_BRICK_SLAB = new SlabBlock(FabricBlockSettings.copyOf(REDSLATE_BRICKS));
	public static final Block FERRIC_IRON_ORE = new ExperienceDroppingBlock(ConstantIntProvider.create(0), FabricBlockSettings.copyOf(Blocks.IRON_ORE));
	public static final Block FERRIC_COPPER_ORE = new ExperienceDroppingBlock(ConstantIntProvider.create(0), FabricBlockSettings.copyOf(Blocks.COPPER_ORE));
	public static final Block FERRIC_BAUXITE_ORE = new ExperienceDroppingBlock(ConstantIntProvider.create(0), FabricBlockSettings.copyOf(Blocks.IRON_ORE));
	public static final Block FERRIC_ILMENITE_ORE = new ExperienceDroppingBlock(ConstantIntProvider.create(0), FabricBlockSettings.copyOf(Blocks.GOLD_ORE));
	public static final Block FERRIC_SULFUR_ORE = new ExperienceDroppingBlock(UniformIntProvider.create(0, 3), FabricBlockSettings.copyOf(Blocks.COAL_ORE));
	public static final Block FERRIC_GOLD_ORE = new ExperienceDroppingBlock(ConstantIntProvider.create(0), FabricBlockSettings.copyOf(Blocks.GOLD_ORE));
	public static final Block FERRIC_DIAMOND_ORE = new ExperienceDroppingBlock(UniformIntProvider.create(3, 7), FabricBlockSettings.copyOf(Blocks.DIAMOND_ORE));
	public static final Block FERRIC_REDSTONE_ORE = new RedstoneOreBlock(FabricBlockSettings.copyOf(Blocks.REDSTONE_ORE));
	public static final Block HEMATITE_ORE = new ExperienceDroppingBlock(UniformIntProvider.create(0, 2), FabricBlockSettings.copyOf(Blocks.IRON_ORE));
	public static final Block HEMATITE_BLOCK = new Block(FabricBlockSettings.copyOf(Blocks.STONE).strength(3.0f, 8.0f));
	public static final Block DRY_SNOW_BLOCK = new Block(FabricBlockSettings.copyOf(Blocks.SNOW_BLOCK).strength(0.1F));
	public static final Block ARES_MOSS_CARPET = new CarpetBlock(FabricBlockSettings.copyOf(Blocks.MOSS_CARPET).mapColor(MapColor.PURPLE));
	public static final Block ARES_MOSS_BLOCK = new MossBlock(FabricBlockSettings.copyOf(Blocks.MOSS_BLOCK).mapColor(MapColor.PURPLE));
	public static final Block LYCOPHYTE_TOP = new LycophyteBlock(FabricBlockSettings.copyOf(Blocks.BIG_DRIPLEAF_STEM), true);
	public static final Block LYCOPHYTE_STEM = new LycophyteBlock(FabricBlockSettings.copyOf(Blocks.BIG_DRIPLEAF_STEM), false);
	public static final Block AEROPLANKTON = new Block(FabricBlockSettings.copyOf(Blocks.MOSS_BLOCK));
	public static final Block RED_AEROPLANKTON = new Block(FabricBlockSettings.copyOf(Blocks.MOSS_BLOCK));
	public static final Block PITCH_BLACK = new Block(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK).strength(256.0f, 256.0f));
	public static final Block SOLARIZED_REGOLITH = new ColoredFallingBlock(new ColorCode(-8356741), FabricBlockSettings.copyOf(Blocks.GRAVEL).mapColor(MapColor.STONE_GRAY).strength(0.5F));
	public static final Block SEARING_REGOLITH = new ColoredFallingBlock(new ColorCode(-8356741), FabricBlockSettings.copyOf(Blocks.GRAVEL).mapColor(MapColor.ORANGE).strength(0.5F));
	public static final Block FRIGID_STONE = new Block(FabricBlockSettings.copyOf(Blocks.STONE));
	public static final Block TREE_TAP = new TreeTapBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK).strength(1.0f, 1.0f));
	public static final Block PLANETARIUM = new PlanetariumBlock(FabricBlockSettings.copyOf(ALUMINUM_BLOCK));
	public static final Block COPPER_CABLE = new EnergyCableBlock(FabricBlockSettings.copyOf(Blocks.COPPER_BLOCK).strength(1.0f, 1.0f));
	public static final Block IRON_MACHINE_CASING = new Block(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK));
	public static final Block TITANIUM_MACHINE_CASING = new Block(FabricBlockSettings.copyOf(TITANIUM_BLOCK));
	public static final Block STIRLING_ENGINE = new StirlingEngineBlock(FabricBlockSettings.copyOf(ALUMINUM_BLOCK).luminance(createLightLevelFromLitBlockState(13)));
	public static final Block ELECTRIC_FURNACE = new ElectricFurnaceBlock(FabricBlockSettings.copyOf(ALUMINUM_BLOCK).luminance(createLightLevelFromLitBlockState(13)));
	public static final Block PUMP = new PumpBlock(FabricBlockSettings.copyOf(ALUMINUM_BLOCK));
	public static final Block ELECTROLYZER = new ElectrolyzerBlock(FabricBlockSettings.copyOf(ALUMINUM_BLOCK).luminance(createLightLevelFromLitBlockState(13)));
	public static final Block EXTRACTOR = new ExtractorBlock(FabricBlockSettings.copyOf(ALUMINUM_BLOCK).luminance(createLightLevelFromLitBlockState(13)));
	public static final Block SOLAR_PANEL = new SolarPanelBlock(FabricBlockSettings.copyOf(ALUMINUM_BLOCK).strength(1.0f, 1.0f));
	public static final Block BATTERY = new BatteryBlock(FabricBlockSettings.copyOf(ALUMINUM_BLOCK));
	public static final Block BREAKER_SWITCH = new BreakerSwitchBlock(FabricBlockSettings.copyOf(ALUMINUM_BLOCK));
	public static final Block WATER_PIPE = new FluidPipeBlock(FabricBlockSettings.copyOf(ALUMINUM_BLOCK).strength(1.0f, 1.0f), FluidResourceType.WATER);
	public static final Block OXYGEN_PIPE = new FluidPipeBlock(FabricBlockSettings.copyOf(ALUMINUM_BLOCK).strength(1.0f, 1.0f), FluidResourceType.OXYGEN);
	public static final Block HYDROGEN_PIPE = new FluidPipeBlock(FabricBlockSettings.copyOf(ALUMINUM_BLOCK).strength(1.0f, 1.0f), FluidResourceType.HYDROGEN);
	public static final Block WATER_TANK = new WaterTankBlock(FabricBlockSettings.copyOf(ALUMINUM_BLOCK).nonOpaque());
	public static final Block OXYGEN_TANK = new FluidTankControllerBlock(FabricBlockSettings.copyOf(ALUMINUM_BLOCK), FluidResourceType.OXYGEN);
	public static final Block HYDROGEN_TANK = new FluidTankControllerBlock(FabricBlockSettings.copyOf(ALUMINUM_BLOCK), FluidResourceType.HYDROGEN);
	public static final Block BALLOON_CONTROLLER = new BalloonControllerBlock(FabricBlockSettings.copyOf(REINFORCED_FABRIC));
	public static final Block VALVE = new ValveBlock(FabricBlockSettings.copyOf(ALUMINUM_BLOCK));
	public static final Block VENT = new VentBlock(FabricBlockSettings.copyOf(ALUMINUM_BLOCK));
	public static final Block COPPER_CABLE_AL = new EncasedEnergyCableBlock(FabricBlockSettings.copyOf(ALUMINUM_BLOCK));
	public static final Block WATER_PIPE_AL = new EncasedFluidPipeBlock(FabricBlockSettings.copyOf(ALUMINUM_BLOCK), FluidResourceType.WATER);
	public static final Block OXYGEN_PIPE_AL = new EncasedFluidPipeBlock(FabricBlockSettings.copyOf(ALUMINUM_BLOCK), FluidResourceType.OXYGEN);
	public static final Block HYDROGEN_PIPE_AL = new EncasedFluidPipeBlock(FabricBlockSettings.copyOf(ALUMINUM_BLOCK), FluidResourceType.HYDROGEN);
	public static final Block FLUID_TANK_INSIDE = new FluidTankInsideBlock(FabricBlockSettings.create().replaceable().dropsNothing());
	public static final Block OXYGEN_DISPENSER = new OxygenDispenserBlock(FabricBlockSettings.copyOf(ALUMINUM_BLOCK));
	public static final Block ATMOSPHERE_GENERATOR = new AtmosphereGeneratorBlock(FabricBlockSettings.copyOf(ALUMINUM_BLOCK));
	public static final Block OXYGEN_SENSOR = new OxygenSensorBlock(FabricBlockSettings.copyOf(ALUMINUM_BLOCK));
	public static final Block HABITABLE_AIR = new HabitableAirBlock(FabricBlockSettings.create().replaceable().noCollision().dropsNothing().air());
	public static final Block LEAK = new LeakBlock(FabricBlockSettings.create().replaceable().noCollision().dropsNothing().air());
	public static final Block DAMAGED_ALUMINUM = new Block(FabricBlockSettings.copyOf(STRUCTURAL_ALUMINUM));
	public static final Block LEVER_BLOCK = new SolidLeverBlock(FabricBlockSettings.copyOf(STRUCTURAL_ALUMINUM));
	public static final Block STORAGE_CUBE = new StorageCubeBlock(FabricBlockSettings.copyOf(ALUMINUM_BLOCK));
	public static final Block AIRLOCK_DOOR = new SealedDoorBlock(FabricBlockSettings.copyOf(ALUMINUM_BLOCK).nonOpaque());
	public static final Block AIRLOCK_TRAPDOOR = new SealedTrapdoorBlock(FabricBlockSettings.copyOf(ALUMINUM_BLOCK).nonOpaque());
	public static final Block IRON_LADDER = new LadderBlock(FabricBlockSettings.copyOf(ALUMINUM_FRAME).nonOpaque());
	public static final Block REACTION_WHEEL = new ReactionWheelBlock(FabricBlockSettings.copyOf(ALUMINUM_BLOCK), 100e3f);
	public static final Block RCS_BLOCK = new ReactionControlThrusterBlock(FabricBlockSettings.copyOf(ALUMINUM_BLOCK), ReactionControlThrusterBlock.DIAGONAL);
	public static final Block LANDING_LEG = new FrameBlock(FabricBlockSettings.copyOf(ALUMINUM_FRAME).nonOpaque());
	public static final Block ROCKET_CONTROLLER = new RocketControllerBlock(FabricBlockSettings.copyOf(ALUMINUM_BLOCK));
	public static final Block THRUSTER_INITIAL = new RocketThrusterBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK), Block.createCuboidShape(0.01, 0.01, 0.01, 15.99, 15.99, 15.99), 1.05e6, 400, 360, 10.0);
	public static final Block THRUSTER_SMALL = new RocketThrusterBlock(FabricBlockSettings.copyOf(TITANIUM_BLOCK), Block.createCuboidShape(1.0, 0.0, 1.0, 15.0, 15.99, 15.0), 0.8e6, 450, 400, 10.0);
	public static final Block THRUSTER_VACUUM = new RocketThrusterBlock(FabricBlockSettings.copyOf(TITANIUM_BLOCK), Block.createCuboidShape(0.0, -15.0, 0.0, 16.0, 15.99, 16.0), 0.5e6, 480, 200, 10.0);
	public static final Block AEROSPIKE = new RocketThrusterBlock(FabricBlockSettings.copyOf(TITANIUM_BLOCK), Block.createCuboidShape(1.0, 0.3, 1.0, 15.0, 15.99, 15.0), 0.8e6, 460, 440, 20.0);

	// Block Entities
	public static final BlockEntityType<StirlingEngineBlockEntity> STIRLING_ENGINE_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(StirlingEngineBlockEntity::new, STIRLING_ENGINE).build(null);
	public static final BlockEntityType<ElectricFurnaceBlockEntity> ELECTRIC_FURNACE_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(ElectricFurnaceBlockEntity::new, ELECTRIC_FURNACE).build(null);
	public static final BlockEntityType<SolarPanelBlockEntity> SOLAR_PANEL_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(SolarPanelBlockEntity::new, SOLAR_PANEL).build(null);
	public static final BlockEntityType<BatteryBlockEntity> BATTERY_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(BatteryBlockEntity::new, BATTERY).build(null);
	public static final BlockEntityType<FluidPipeBlockEntity> FLUID_PIPE_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(FluidPipeBlockEntity::new, WATER_PIPE, OXYGEN_PIPE, HYDROGEN_PIPE, WATER_PIPE_AL, OXYGEN_PIPE_AL, HYDROGEN_PIPE_AL).build(null);
	public static final BlockEntityType<FluidTankControllerBlockEntity> FLUID_TANK_CONTROLLER_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(FluidTankControllerBlockEntity::new, OXYGEN_TANK, HYDROGEN_TANK).build(null);
	public static final BlockEntityType<BalloonControllerBlockEntity> BALLOON_CONTROLLER_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(BalloonControllerBlockEntity::new, BALLOON_CONTROLLER).build(null);
	public static final BlockEntityType<ValveBlockEntity> VALVE_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(ValveBlockEntity::new, VALVE).build(null);
	public static final BlockEntityType<VentBlockEntity> VENT_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(VentBlockEntity::new, VENT).build(null);
	public static final BlockEntityType<WaterTankBlockEntity> WATER_TANK_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(WaterTankBlockEntity::new, WATER_TANK).build(null);
	public static final BlockEntityType<PumpBlockEntity> PUMP_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(PumpBlockEntity::new, PUMP).build(null);
	public static final BlockEntityType<ExtractorBlockEntity> EXTRACTOR_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(ExtractorBlockEntity::new, EXTRACTOR).build(null);
	public static final BlockEntityType<ElectrolyzerBlockEntity> ELECTROLYZER_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(ElectrolyzerBlockEntity::new, ELECTROLYZER).build(null);
	public static final BlockEntityType<RocketControllerBlockEntity> ROCKET_CONTROLLER_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(RocketControllerBlockEntity::new, ROCKET_CONTROLLER).build(null);
	public static final BlockEntityType<AtmosphereGeneratorBlockEntity> ATMOSPHERE_GENERATOR_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(AtmosphereGeneratorBlockEntity::new, ATMOSPHERE_GENERATOR).build(null);
	public static final BlockEntityType<LeakBlockEntity> LEAK_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(LeakBlockEntity::new, LEAK).build(null);
	public static final BlockEntityType<StorageCubeBlockEntity> STORAGE_CUBE_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(StorageCubeBlockEntity::new, STORAGE_CUBE).build(null);

	// Block Tags
	public static final TagKey<Block> FLUID_TANK_BLOCK_TAG = TagKey.of(RegistryKeys.BLOCK, new Identifier(StarflightMod.MOD_ID, "fluid_tank_blocks"));
	public static final TagKey<Block> BALLOON_BLOCK_TAG = TagKey.of(RegistryKeys.BLOCK, new Identifier(StarflightMod.MOD_ID, "balloon_blocks"));
	public static final TagKey<Block> EXCLUDED_BLOCK_TAG = TagKey.of(RegistryKeys.BLOCK, new Identifier(StarflightMod.MOD_ID, "excluded_blocks"));
	public static final TagKey<Block> EDGE_CASE_TAG = TagKey.of(RegistryKeys.BLOCK, new Identifier(StarflightMod.MOD_ID, "edge_case_blocks"));
	public static final TagKey<Block> AIR_UPDATE_TAG = TagKey.of(RegistryKeys.BLOCK, new Identifier(StarflightMod.MOD_ID, "air_update_blocks"));
	public static final TagKey<Block> INSTANT_REMOVE_TAG = TagKey.of(RegistryKeys.BLOCK, new Identifier(StarflightMod.MOD_ID, "instant_remove_blocks"));

	public static void initializeBlocks()
	{
		initializeBlock(ALUMINUM_BLOCK, "aluminum_block");
		initializeBlock(STRUCTURAL_ALUMINUM, "structural_aluminum");
		initializeBlock(RIVETED_ALUMINUM, "riveted_aluminum");
		initializeBlock(ALUMINUM_FRAME, "aluminum_frame");
		initializeBlock(WALKWAY, "walkway");
		initializeBlock(STRUCTURAL_ALUMINUM_STAIRS, "structural_aluminum_stairs");
		initializeBlock(STRUCTURAL_ALUMINUM_SLAB, "structural_aluminum_slab");
		initializeBlock(REINFORCED_FABRIC, "reinforced_fabric");
		initializeBlock(BAUXITE_BLOCK, "bauxite_block");
		initializeBlock(BAUXITE_ORE, "bauxite_ore");
		initializeBlock(DEEPSLATE_BAUXITE_ORE, "deepslate_bauxite_ore");
		initializeBlock(TITANIUM_BLOCK, "titanium_block");
		initializeBlock(ILMENITE_BLOCK, "ilmenite_block");
		initializeBlock(ILMENITE_ORE, "ilmenite_ore");
		initializeBlock(DEEPSLATE_ILMENITE_ORE, "deepslate_ilmenite_ore");
		initializeBlock(SULFUR_BLOCK, "sulfur_block");
		initializeBlock(SULFUR_ORE, "sulfur_ore");
		initializeBlock(DEEPSLATE_SULFUR_ORE, "deepslate_sulfur_ore");
		initializeBlock(RUBBER_LOG, "rubber_log");
		initializeBlock(SAPPY_RUBBER_LOG, "sappy_rubber_log", false, List.of(), List.of());
		initializeBlock(STRIPPED_RUBBER_LOG, "stripped_rubber_log");
		initializeBlock(RUBBER_LEAVES, "rubber_leaves");
		initializeBlock(RUBBER_SAPLING, "rubber_sapling");
		initializeBlock(CHEESE_BLOCK, "cheese_block");
		initializeBlock(REGOLITH, "regolith");
		initializeBlock(BALSALTIC_REGOLITH, "balsaltic_regolith");
		initializeBlock(ICY_REGOLITH, "icy_regolith");
		initializeBlock(FERRIC_SAND, "ferric_sand");
		initializeBlock(FERRIC_STONE, "ferric_stone");
		initializeBlock(REDSLATE, "redslate");
		initializeBlock(REDSLATE_BRICKS, "redslate_bricks");
		initializeBlock(REDSLATE_BRICK_STAIRS, "redslate_brick_stairs");
		initializeBlock(REDSLATE_BRICK_SLAB, "redslate_brick_slab");
		initializeBlock(FERRIC_IRON_ORE, "ferric_iron_ore");
		initializeBlock(FERRIC_COPPER_ORE, "ferric_copper_ore");
		initializeBlock(FERRIC_BAUXITE_ORE, "ferric_bauxite_ore");
		initializeBlock(FERRIC_ILMENITE_ORE, "ferric_ilmenite_ore");
		initializeBlock(FERRIC_SULFUR_ORE, "ferric_sulfur_ore");
		initializeBlock(FERRIC_GOLD_ORE, "ferric_gold_ore");
		initializeBlock(FERRIC_DIAMOND_ORE, "ferric_diamond_ore");
		initializeBlock(FERRIC_REDSTONE_ORE, "ferric_redstone_ore");
		initializeBlock(HEMATITE_ORE, "hematite_ore");
		initializeBlock(HEMATITE_BLOCK, "hematite_block");
		initializeBlock(DRY_SNOW_BLOCK, "dry_snow_block");
		initializeBlock(ARES_MOSS_CARPET, "mars_moss_carpet");
		initializeBlock(ARES_MOSS_BLOCK, "mars_moss_block");
		initializeBlock(LYCOPHYTE_TOP, "lycophyte_top");
		initializeBlock(LYCOPHYTE_STEM, "lycophyte_stem", false, List.of(), List.of());
		initializeBlock(AEROPLANKTON, "aeroplankton");
		initializeBlock(RED_AEROPLANKTON, "red_aeroplankton");
		initializeBlock(PITCH_BLACK, "pitch_black", false, List.of(), List.of());
		initializeBlock(SOLARIZED_REGOLITH, "solarized_regolith");
		initializeBlock(SEARING_REGOLITH, "searing_regolith");
		initializeBlock(FRIGID_STONE, "frigid_stone");
		initializeBlock(TREE_TAP, "tree_tap");
		initializeBlock(PLANETARIUM, "planetarium");
		initializeBlock(COPPER_CABLE, "copper_cable");
		initializeBlock(IRON_MACHINE_CASING, "iron_machine_casing");
		initializeBlock(TITANIUM_MACHINE_CASING, "titanium_machine_casing");
		initializeBlock(STIRLING_ENGINE, "stirling_engine");
		initializeBlock(ELECTRIC_FURNACE, "electric_furnace");
		initializeBlock(PUMP, "pump");
		initializeBlock(ELECTROLYZER, "electrolyzer");
		initializeBlock(EXTRACTOR, "extractor");
		initializeBlock(SOLAR_PANEL, "solar_panel");
		initializeBlock(BATTERY, "battery");
		initializeBlock(BREAKER_SWITCH, "breaker_switch", true, List.of(), List.of(Text.translatable("block.space.breaker_switch.description")));
		initializeBlock(WATER_PIPE, "water_pipe");
		initializeBlock(OXYGEN_PIPE, "oxygen_pipe");
		initializeBlock(HYDROGEN_PIPE, "hydrogen_pipe");
		initializeBlock(WATER_TANK, "water_tank");
		initializeBlock(OXYGEN_TANK, "oxygen_tank");
		initializeBlock(HYDROGEN_TANK, "hydrogen_tank");
		initializeBlock(BALLOON_CONTROLLER, "balloon_controller");
		initializeBlock(VALVE, "valve");
		initializeBlock(VENT, "vent");
		initializeBlock(COPPER_CABLE_AL, "copper_cable_al");
		initializeBlock(WATER_PIPE_AL, "water_pipe_al");
		initializeBlock(OXYGEN_PIPE_AL, "oxygen_pipe_al");
		initializeBlock(HYDROGEN_PIPE_AL, "hydrogen_pipe_al");
		initializeBlock(FLUID_TANK_INSIDE, "fluid_tank_inside", false, List.of(), List.of());
		initializeBlock(OXYGEN_DISPENSER, "oxygen_dispenser");
		initializeBlock(ATMOSPHERE_GENERATOR, "atmosphere_generator", true, List.of(), List.of(Text.translatable("block.space.atmosphere_generator.description_1"), Text.translatable("block.space.atmosphere_generator.description_2")));
		initializeBlock(OXYGEN_SENSOR, "oxygen_sensor", false, List.of(), List.of(Text.translatable("block.space.oxygen_sensor.description_1"), Text.translatable("block.space.oxygen_sensor.description_2")));
		initializeBlock(HABITABLE_AIR, "habitable_air", false, List.of(), List.of());
		initializeBlock(LEAK, "leak", false, List.of(), List.of());
		initializeBlock(DAMAGED_ALUMINUM, "damaged_aluminum");
		initializeBlock(LEVER_BLOCK, "lever_block");
		initializeBlock(STORAGE_CUBE, "storage_cube");
		initializeBlock(AIRLOCK_DOOR, "airlock_door");
		initializeBlock(AIRLOCK_TRAPDOOR, "airlock_trapdoor");
		initializeBlock(IRON_LADDER, "iron_ladder");
		initializeBlock(REACTION_WHEEL, "reaction_wheel");
		initializeBlock(RCS_BLOCK, "rcs_block");
		initializeBlock(LANDING_LEG, "landing_leg", true, List.of(), List.of(Text.translatable("block.space.landing_leg.description")));
		initializeBlock(ROCKET_CONTROLLER, "rocket_controller");
		initializeBlock(THRUSTER_INITIAL, "thruster_initial");
		initializeBlock(THRUSTER_SMALL, "thruster_small");
		initializeBlock(THRUSTER_VACUUM, "thruster_vacuum");
		initializeBlock(AEROSPIKE, "aerospike");
		
		Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(StarflightMod.MOD_ID, "stirling_engine"), STIRLING_ENGINE_BLOCK_ENTITY);
		Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(StarflightMod.MOD_ID, "electric_furnace"), ELECTRIC_FURNACE_BLOCK_ENTITY);
		Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(StarflightMod.MOD_ID, "solar_panel"), SOLAR_PANEL_BLOCK_ENTITY);
		Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(StarflightMod.MOD_ID, "battery"), BATTERY_BLOCK_ENTITY);
		Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(StarflightMod.MOD_ID, "fluid_pipe"), FLUID_PIPE_BLOCK_ENTITY);
		Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(StarflightMod.MOD_ID, "fluid_tank_controller"), FLUID_TANK_CONTROLLER_BLOCK_ENTITY);
		Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(StarflightMod.MOD_ID, "balloon_controller"), BALLOON_CONTROLLER_BLOCK_ENTITY);
		Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(StarflightMod.MOD_ID, "valve"), VALVE_BLOCK_ENTITY);
		Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(StarflightMod.MOD_ID, "vent"), VENT_BLOCK_ENTITY);
		Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(StarflightMod.MOD_ID, "water_tank"), WATER_TANK_BLOCK_ENTITY);
		Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(StarflightMod.MOD_ID, "pump"), PUMP_BLOCK_ENTITY);
		Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(StarflightMod.MOD_ID, "extractor"), EXTRACTOR_BLOCK_ENTITY);
		Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(StarflightMod.MOD_ID, "electrolyzer"), ELECTROLYZER_BLOCK_ENTITY);
		Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(StarflightMod.MOD_ID, "rocket_controller"), ROCKET_CONTROLLER_BLOCK_ENTITY);
		Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(StarflightMod.MOD_ID, "atmosphere_generator"), ATMOSPHERE_GENERATOR_BLOCK_ENTITY);
		Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(StarflightMod.MOD_ID, "leak"), LEAK_BLOCK_ENTITY);
		Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(StarflightMod.MOD_ID, "storage_cube"), STORAGE_CUBE_BLOCK_ENTITY);
		
		// Setup extra block behavior.
		FireBlockInvokerMixin fireBlock = (FireBlockInvokerMixin) Blocks.FIRE;
		fireBlock.callRegisterFlammableBlock(RUBBER_LOG, 5, 5);
		fireBlock.callRegisterFlammableBlock(SAPPY_RUBBER_LOG, 30, 5);
		fireBlock.callRegisterFlammableBlock(STRIPPED_RUBBER_LOG, 5, 5);
		fireBlock.callRegisterFlammableBlock(RUBBER_LEAVES, 30, 60);
	}

	private static void initializeBlock(Block block, String name)
	{
		initializeBlock(block, name, true, List.of(), List.of());
	}

	private static void initializeEnergyProducerBlock(Block block, String name, @Nullable BlockEntityType<?> blockEntity, double powerOutput, List<Text> hiddenTextList)
	{
		ArrayList<Text> textList = new ArrayList<Text>();
		DecimalFormat df = new DecimalFormat("#.##");
		textList.add(Text.translatable("block.space.energy_producer").append(String.valueOf(df.format(powerOutput))).append("kJ/s").formatted(Formatting.GOLD));
		initializeBlock(block, name, true, textList, hiddenTextList);
	}

	private static void initializeEnergyConsumerBlock(Block block, String name, double powerInput, List<Text> hiddenTextList)
	{
		ArrayList<Text> textList = new ArrayList<Text>();
		DecimalFormat df = new DecimalFormat("#.##");
		textList.add(Text.translatable("block.space.energy_consumer").append(String.valueOf(df.format(powerInput))).append("kJ/s").formatted(Formatting.LIGHT_PURPLE));
		initializeBlock(block, name, true, textList, hiddenTextList);
	}

	private static void initializeBlock(Block block, String name, boolean creativeInventory, List<Text> textList, List<Text> hiddenTextList)
	{
		String mod_id = StarflightMod.MOD_ID;
		Registry.register(Registries.BLOCK, new Identifier(mod_id, name), block);

		if(textList.isEmpty() && hiddenTextList.isEmpty())
			Registry.register(Registries.ITEM, new Identifier(mod_id, name), new BlockItem(block, new FabricItemSettings()));
		else
			Registry.register(Registries.ITEM, new Identifier(mod_id, name), new DescriptiveBlockItem(block, new FabricItemSettings(), textList, hiddenTextList));

		if(creativeInventory)
			ItemGroupEvents.modifyEntriesEvent(StarflightItems.ITEM_GROUP).register(content -> content.add(block));
	}

	private static ToIntFunction<BlockState> createLightLevelFromLitBlockState(int litLevel)
	{
		return (state) -> {
			return (Boolean) state.get(Properties.LIT) ? litLevel : 0;
		};
	}

	private static PillarBlock createLogBlock(MapColor topMapColor, MapColor sideMapColor)
	{
		return new PillarBlock(FabricBlockSettings.create().mapColor(state -> state.get(PillarBlock.AXIS) == Direction.Axis.Y ? topMapColor : sideMapColor).instrument(Instrument.BASS).strength(2.0f).sounds(BlockSoundGroup.WOOD).burnable());
	}

	private static LeavesBlock createLeavesBlock(BlockSoundGroup soundGroup)
	{
		return new LeavesBlock(FabricBlockSettings.create().mapColor(MapColor.DARK_GREEN).strength(0.2f).ticksRandomly().sounds(soundGroup).nonOpaque().allowsSpawning(StarflightBlocks::canSpawnOnLeaves).suffocates(StarflightBlocks::never).blockVision(StarflightBlocks::never).burnable().pistonBehavior(PistonBehavior.DESTROY).solidBlock(StarflightBlocks::never));
	}

	private static Boolean canSpawnOnLeaves(BlockState state, BlockView world, BlockPos pos, EntityType<?> type)
	{
		return type == EntityType.OCELOT || type == EntityType.PARROT;
	}

	private static boolean never(BlockState state, BlockView world, BlockPos pos)
	{
		return false;
	}
}