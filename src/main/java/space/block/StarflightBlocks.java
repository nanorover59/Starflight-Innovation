package space.block;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
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
import net.minecraft.block.ExperienceDroppingBlock;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.MapColor;
import net.minecraft.block.MossBlock;
import net.minecraft.block.PillarBlock;
import net.minecraft.block.RedstoneOreBlock;
import net.minecraft.block.SandBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.Instrument;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import space.StarflightMod;
import space.block.entity.AtmosphereGeneratorBlockEntity;
import space.block.entity.BatteryBlockEntity;
import space.block.entity.ElectricFurnaceBlockEntity;
import space.block.entity.ElectrolyzerBlockEntity;
import space.block.entity.EncasedHydrogenPipeALBlockEntity;
import space.block.entity.EncasedOxygenPipeALBlockEntity;
import space.block.entity.HydrogenInletValveBlockEntity;
import space.block.entity.HydrogenOutletValveBlockEntity;
import space.block.entity.HydrogenPipeBlockEntity;
import space.block.entity.HydrogenTankBlockEntity;
import space.block.entity.IceElectrolyzerBlockEntity;
import space.block.entity.LeakBlockEntity;
import space.block.entity.OxygenInletValveBlockEntity;
import space.block.entity.OxygenOutletValveBlockEntity;
import space.block.entity.OxygenPipeBlockEntity;
import space.block.entity.OxygenTankBlockEntity;
import space.block.entity.RocketControllerBlockEntity;
import space.block.entity.SolarHubBlockEntity;
import space.block.entity.StirlingEngineBlockEntity;
import space.block.entity.StorageCubeBlockEntity;
import space.block.sapling.RubberSaplingGenerator;
import space.item.DescriptiveBlockItem;
import space.item.StarflightItems;
import space.mixin.common.FireBlockInvokerMixin;

public class StarflightBlocks
{
	// Blocks
	public static final Block ALUMINUM_BLOCK = new Block(FabricBlockSettings.create().mapColor(MapColor.IRON_GRAY).instrument(Instrument.IRON_XYLOPHONE).requiresTool().strength(4.0f, 5.0f).sounds(BlockSoundGroup.COPPER));
	public static final Block STRUCTURAL_ALUMINUM = new Block(FabricBlockSettings.copyOf(ALUMINUM_BLOCK));
	public static final Block RIVETED_ALUMINUM = new Block(FabricBlockSettings.copyOf(STRUCTURAL_ALUMINUM));
	public static final Block ALUMINUM_FRAME = new FrameBlock(FabricBlockSettings.copyOf(ALUMINUM_BLOCK).strength(3.0f, 3.0f));
	public static final Block WALKWAY = new FrameBlock(FabricBlockSettings.copyOf(ALUMINUM_FRAME));
	public static final Block STRUCTURAL_ALUMINUM_STAIRS = new StairsBlock(STRUCTURAL_ALUMINUM.getDefaultState(), FabricBlockSettings.copyOf(STRUCTURAL_ALUMINUM));
	public static final Block STRUCTURAL_ALUMINUM_SLAB = new SlabBlock(FabricBlockSettings.copyOf(STRUCTURAL_ALUMINUM));
	public static final Block REINFORCED_FABRIC = new Block(FabricBlockSettings.copyOf(Blocks.WHITE_WOOL).strength(2.0f, 3.0f));
	public static final Block BAUXITE_BLOCK = new Block(FabricBlockSettings.copyOf(Blocks.RAW_IRON_BLOCK));
	public static final Block BAUXITE_ORE = new ExperienceDroppingBlock(FabricBlockSettings.copyOf(Blocks.IRON_ORE));
	public static final Block DEEPSLATE_BAUXITE_ORE = new ExperienceDroppingBlock(AbstractBlock.Settings.copy(Blocks.DEEPSLATE_IRON_ORE));
	public static final Block TITANIUM_BLOCK = new Block(FabricBlockSettings.copyOf(ALUMINUM_BLOCK).strength(6.0f, 7.0f));
	public static final Block ILMENITE_BLOCK = new Block(FabricBlockSettings.copyOf(Blocks.RAW_GOLD_BLOCK));
	public static final Block ILMENITE_ORE = new ExperienceDroppingBlock(FabricBlockSettings.copyOf(Blocks.GOLD_ORE));
	public static final Block DEEPSLATE_ILMENITE_ORE = new ExperienceDroppingBlock(FabricBlockSettings.copyOf(Blocks.DEEPSLATE_GOLD_ORE));
	public static final Block SULFUR_BLOCK = new Block(FabricBlockSettings.copyOf(Blocks.COAL_BLOCK).strength(3.0f, 5.0f));
	public static final Block SULFUR_ORE = new ExperienceDroppingBlock(FabricBlockSettings.copyOf(Blocks.COAL_ORE));
	public static final Block DEEPSLATE_SULFUR_ORE = new ExperienceDroppingBlock(FabricBlockSettings.copyOf(Blocks.DEEPSLATE_COAL_ORE));
	public static final Block RUBBER_LOG = createLogBlock(MapColor.OAK_TAN, MapColor.SPRUCE_BROWN);
	public static final Block SAPPY_RUBBER_LOG = createLogBlock(MapColor.OAK_TAN, MapColor.SPRUCE_BROWN);
	public static final Block STRIPPED_RUBBER_LOG = createLogBlock(MapColor.OAK_TAN, MapColor.OAK_TAN);
	public static final Block RUBBER_LEAVES = createLeavesBlock(BlockSoundGroup.WET_GRASS);
	public static final Block RUBBER_SAPLING = new RubberSaplingBlock(new RubberSaplingGenerator(), FabricBlockSettings.copyOf(Blocks.OAK_SAPLING));
	public static final Block CHEESE_BLOCK = new Block(FabricBlockSettings.copyOf(Blocks.WET_SPONGE).strength(1.0f, 1.0f));
	public static final Block REGOLITH = new RegolithBlock(FabricBlockSettings.copyOf(Blocks.GRAVEL).mapColor(MapColor.LIGHT_GRAY).strength(0.5F));
	public static final Block BALSALTIC_REGOLITH = new RegolithBlock(FabricBlockSettings.copyOf(Blocks.GRAVEL).mapColor(MapColor.STONE_GRAY).strength(0.5F));
	public static final Block ICY_REGOLITH = new RegolithBlock(FabricBlockSettings.copyOf(Blocks.GRAVEL).mapColor(MapColor.LIGHT_BLUE_GRAY).strength(0.5F));
	public static final Block FERRIC_SAND = new SandBlock(0xB7633D, FabricBlockSettings.copyOf(Blocks.SAND).mapColor(MapColor.ORANGE));
	public static final Block FERRIC_STONE = new Block(FabricBlockSettings.copyOf(Blocks.STONE));
	public static final Block REDSLATE = new Block(FabricBlockSettings.copyOf(Blocks.SANDSTONE));
	public static final Block REDSLATE_BRICKS = new Block(FabricBlockSettings.copyOf(REDSLATE));
	public static final Block REDSLATE_BRICK_STAIRS = new StairsBlock(REDSLATE_BRICKS.getDefaultState(), FabricBlockSettings.copyOf(REDSLATE_BRICKS));
	public static final Block REDSLATE_BRICK_SLAB = new SlabBlock(FabricBlockSettings.copyOf(REDSLATE_BRICKS));
	public static final Block FERRIC_IRON_ORE = new ExperienceDroppingBlock(FabricBlockSettings.copyOf(Blocks.IRON_ORE));
	public static final Block FERRIC_COPPER_ORE = new ExperienceDroppingBlock(FabricBlockSettings.copyOf(Blocks.COPPER_ORE));
	public static final Block FERRIC_BAUXITE_ORE = new ExperienceDroppingBlock(FabricBlockSettings.copyOf(Blocks.IRON_ORE));
	public static final Block FERRIC_ILMENITE_ORE = new ExperienceDroppingBlock(FabricBlockSettings.copyOf(Blocks.GOLD_ORE));
	public static final Block FERRIC_SULFUR_ORE = new ExperienceDroppingBlock(FabricBlockSettings.copyOf(Blocks.COAL_ORE));
	public static final Block FERRIC_GOLD_ORE = new ExperienceDroppingBlock(FabricBlockSettings.copyOf(Blocks.GOLD_ORE));
	public static final Block FERRIC_DIAMOND_ORE = new ExperienceDroppingBlock(FabricBlockSettings.copyOf(Blocks.DIAMOND_ORE));
	public static final Block FERRIC_REDSTONE_ORE = new RedstoneOreBlock(FabricBlockSettings.copyOf(Blocks.REDSTONE_ORE));
	public static final Block HEMATITE_ORE = new ExperienceDroppingBlock(FabricBlockSettings.copyOf(Blocks.IRON_ORE));
	public static final Block HEMATITE_BLOCK = new Block(FabricBlockSettings.copyOf(Blocks.STONE).strength(3.0f, 8.0f));
	public static final Block DRY_SNOW_BLOCK = new Block(FabricBlockSettings.copyOf(Blocks.SNOW_BLOCK).strength(0.1F));
	public static final Block ARES_MOSS_CARPET = new CarpetBlock(FabricBlockSettings.copyOf(Blocks.MOSS_CARPET).mapColor(MapColor.PURPLE));
	public static final Block ARES_MOSS_BLOCK = new MossBlock(FabricBlockSettings.copyOf(Blocks.MOSS_BLOCK).mapColor(MapColor.PURPLE));
	public static final Block LYCOPHYTE_TOP = new LycophyteBlock(FabricBlockSettings.copyOf(Blocks.BIG_DRIPLEAF_STEM), true);
	public static final Block LYCOPHYTE_STEM = new LycophyteBlock(FabricBlockSettings.copyOf(Blocks.BIG_DRIPLEAF_STEM), false);
	public static final Block PITCH_BLACK = new Block(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK).strength(256.0f, 256.0f));
	public static final Block SOLARIZED_REGOLITH = new RegolithBlock(FabricBlockSettings.copyOf(Blocks.GRAVEL).mapColor(MapColor.STONE_GRAY).strength(0.5F));
	public static final Block SEARING_REGOLITH = new RegolithBlock(FabricBlockSettings.copyOf(Blocks.GRAVEL).mapColor(MapColor.ORANGE).strength(0.5F));
	public static final Block FRIGID_STONE = new Block(FabricBlockSettings.copyOf(Blocks.STONE));
	public static final Block TREE_TAP = new TreeTapBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK).strength(1.0f, 1.0f));
	public static final Block PLANETARIUM = new PlanetariumBlock(FabricBlockSettings.copyOf(ALUMINUM_BLOCK));
	public static final Block COPPER_CABLE = new EnergyCableBlock(FabricBlockSettings.copyOf(Blocks.COPPER_BLOCK).strength(1.0f, 1.0f));
	public static final Block STIRLING_ENGINE = new StirlingEngineBlock(FabricBlockSettings.copyOf(ALUMINUM_BLOCK).luminance(createLightLevelFromLitBlockState(13)));
	public static final Block ELECTRIC_FURNACE = new ElectricFurnaceBlock(FabricBlockSettings.copyOf(ALUMINUM_BLOCK).luminance(createLightLevelFromLitBlockState(13)));
	public static final Block SOLAR_PANEL = new SolarPanelBlock(FabricBlockSettings.copyOf(ALUMINUM_BLOCK).strength(1.0f, 1.0f));
	public static final Block SOLAR_HUB = new SolarHubBlock(FabricBlockSettings.copyOf(ALUMINUM_BLOCK));
	public static final Block BATTERY = new BatteryBlock(FabricBlockSettings.copyOf(ALUMINUM_BLOCK));
	public static final Block BREAKER_SWITCH = new BreakerSwitchBlock(FabricBlockSettings.copyOf(ALUMINUM_BLOCK));
	public static final Block OXYGEN_PIPE = new OxygenPipeBlock(FabricBlockSettings.copyOf(ALUMINUM_BLOCK).strength(1.0f, 1.0f));
	public static final Block HYDROGEN_PIPE = new HydrogenPipeBlock(FabricBlockSettings.copyOf(ALUMINUM_BLOCK).strength(1.0f, 1.0f));
	public static final Block OXYGEN_TANK = new OxygenTankBlock(FabricBlockSettings.copyOf(ALUMINUM_BLOCK));
	public static final Block OXYGEN_INLET_VALVE = new OxygenInletValveBlock(FabricBlockSettings.copyOf(ALUMINUM_BLOCK));
	public static final Block OXYGEN_OUTLET_VALVE = new OxygenOutletValveBlock(FabricBlockSettings.copyOf(ALUMINUM_BLOCK));
	public static final Block HYDROGEN_TANK = new HydrogenTankBlock(FabricBlockSettings.copyOf(ALUMINUM_BLOCK));
	public static final Block HYDROGEN_INLET_VALVE = new HydrogenInletValveBlock(FabricBlockSettings.copyOf(ALUMINUM_BLOCK));
	public static final Block HYDROGEN_OUTLET_VALVE = new HydrogenOutletValveBlock(FabricBlockSettings.copyOf(ALUMINUM_BLOCK));
	public static final Block VENT = new VentBlock(FabricBlockSettings.copyOf(ALUMINUM_BLOCK));
	public static final Block COPPER_CABLE_AL = new EncasedEnergyCableBlock(FabricBlockSettings.copyOf(ALUMINUM_BLOCK));
	public static final Block OXYGEN_PIPE_AL = new EncasedOxygenPipeALBlock(FabricBlockSettings.copyOf(ALUMINUM_BLOCK));
	public static final Block HYDROGEN_PIPE_AL = new EncasedHydrogenPipeALBlock(FabricBlockSettings.copyOf(ALUMINUM_BLOCK));
	public static final Block FLUID_TANK_INSIDE = new FluidTankInsideBlock(FabricBlockSettings.create().replaceable().dropsNothing());
	public static final Block ELECTROLYZER = new ElectrolyzerBlock(FabricBlockSettings.copyOf(ALUMINUM_BLOCK).luminance(createLightLevelFromLitBlockState(13)));
	public static final Block ICE_ELECTROLYZER = new IceElectrolyzerBlock(FabricBlockSettings.copyOf(ALUMINUM_BLOCK).luminance(createLightLevelFromLitBlockState(13)));
	public static final Block OXYGEN_DISPENSER = new OxygenDispenserBlock(FabricBlockSettings.copyOf(ALUMINUM_BLOCK));
	public static final Block ATMOSPHERE_GENERATOR = new AtmosphereGeneratorBlock(FabricBlockSettings.copyOf(ALUMINUM_BLOCK));
	public static final Block OXYGEN_SENSOR = new OxygenSensorBlock(FabricBlockSettings.copyOf(ALUMINUM_BLOCK));
	public static final Block HABITABLE_AIR = new HabitableAirBlock(FabricBlockSettings.create().replaceable().noCollision().dropsNothing().air());
	public static final Block LEAK = new LeakBlock(FabricBlockSettings.create().replaceable().noCollision().dropsNothing().air());
	public static final Block STORAGE_CUBE = new StorageCubeBlock(FabricBlockSettings.copyOf(ALUMINUM_BLOCK));
	public static final Block AIRLOCK_DOOR = new SealedDoorBlock(FabricBlockSettings.copyOf(ALUMINUM_BLOCK).nonOpaque());
	public static final Block AIRLOCK_TRAPDOOR = new SealedTrapdoorBlock(FabricBlockSettings.copyOf(ALUMINUM_BLOCK).nonOpaque());
	public static final Block IRON_LADDER = new LadderBlock(FabricBlockSettings.copyOf(ALUMINUM_FRAME).nonOpaque());
	public static final Block LANDING_LEG = new FrameBlock(FabricBlockSettings.copyOf(ALUMINUM_FRAME).nonOpaque());
	public static final Block ROCKET_CONTROLLER = new RocketControllerBlock(FabricBlockSettings.copyOf(ALUMINUM_BLOCK));
	public static final Block THRUSTER_INITIAL = new RocketThrusterBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK), Block.createCuboidShape(0.01, 0.01, 0.01, 15.99, 15.99, 15.99), 0.25e6, 260, 240, 12.0);
	public static final Block THRUSTER_SMALL = new RocketThrusterBlock(FabricBlockSettings.copyOf(TITANIUM_BLOCK), Block.createCuboidShape(1.0, 0.0, 1.0, 15.0, 15.99, 15.0), 0.15e6, 300, 260, 12.0);
	public static final Block THRUSTER_VACUUM = new RocketThrusterBlock(FabricBlockSettings.copyOf(TITANIUM_BLOCK), Block.createCuboidShape(0.0, -15.0, 0.0, 16.0, 15.99, 16.0), 0.075e6, 400, 100, 4.0);
	public static final Block AEROSPIKE = new RocketThrusterBlock(FabricBlockSettings.copyOf(TITANIUM_BLOCK), Block.createCuboidShape(1.0, 0.3, 1.0, 15.0, 15.99, 15.0), 0.15e6, 320, 315, 20.0);

	// Block Entities
	public static final BlockEntityType<StirlingEngineBlockEntity> STIRLING_ENGINE_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(StirlingEngineBlockEntity::new, STIRLING_ENGINE).build(null);
	public static final BlockEntityType<ElectricFurnaceBlockEntity> ELECTRIC_FURNACE_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(ElectricFurnaceBlockEntity::new, ELECTRIC_FURNACE).build(null);
	public static final BlockEntityType<SolarHubBlockEntity> SOLAR_HUB_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(SolarHubBlockEntity::new, SOLAR_HUB).build(null);
	public static final BlockEntityType<BatteryBlockEntity> BATTERY_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(BatteryBlockEntity::new, BATTERY).build(null);
	public static final BlockEntityType<OxygenPipeBlockEntity> OXYGEN_PIPE_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(OxygenPipeBlockEntity::new, OXYGEN_PIPE).build(null);
	public static final BlockEntityType<HydrogenPipeBlockEntity> HYDROGEN_PIPE_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(HydrogenPipeBlockEntity::new, HYDROGEN_PIPE).build(null);
	public static final BlockEntityType<OxygenTankBlockEntity> OXYGEN_TANK_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(OxygenTankBlockEntity::new, OXYGEN_TANK).build(null);
	public static final BlockEntityType<OxygenInletValveBlockEntity> OXYGEN_INLET_VALVE_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(OxygenInletValveBlockEntity::new, OXYGEN_INLET_VALVE).build(null);
	public static final BlockEntityType<OxygenOutletValveBlockEntity> OXYGEN_OUTLET_VALVE_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(OxygenOutletValveBlockEntity::new, OXYGEN_OUTLET_VALVE).build(null);
	public static final BlockEntityType<HydrogenTankBlockEntity> HYDROGEN_TANK_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(HydrogenTankBlockEntity::new, HYDROGEN_TANK).build(null);
	public static final BlockEntityType<HydrogenInletValveBlockEntity> HYDROGEN_INLET_VALVE_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(HydrogenInletValveBlockEntity::new, HYDROGEN_INLET_VALVE).build(null);
	public static final BlockEntityType<HydrogenOutletValveBlockEntity> HYDROGEN_OUTLET_VALVE_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(HydrogenOutletValveBlockEntity::new, HYDROGEN_OUTLET_VALVE).build(null);
	public static final BlockEntityType<EncasedOxygenPipeALBlockEntity> OXYGEN_PIPE_AL_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(EncasedOxygenPipeALBlockEntity::new, OXYGEN_PIPE_AL).build(null);
	public static final BlockEntityType<EncasedHydrogenPipeALBlockEntity> HYDROGEN_PIPE_AL_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(EncasedHydrogenPipeALBlockEntity::new, HYDROGEN_PIPE_AL).build(null);
	public static final BlockEntityType<ElectrolyzerBlockEntity> ELECTROLYZER_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(ElectrolyzerBlockEntity::new, ELECTROLYZER).build(null);
	public static final BlockEntityType<IceElectrolyzerBlockEntity> ICE_ELECTROLYZER_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(IceElectrolyzerBlockEntity::new, ICE_ELECTROLYZER).build(null);
	public static final BlockEntityType<RocketControllerBlockEntity> ROCKET_CONTROLLER_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(RocketControllerBlockEntity::new, ROCKET_CONTROLLER).build(null);
	public static final BlockEntityType<AtmosphereGeneratorBlockEntity> ATMOSPHERE_GENERATOR_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(AtmosphereGeneratorBlockEntity::new, ATMOSPHERE_GENERATOR).build(null);
	public static final BlockEntityType<LeakBlockEntity> LEAK_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(LeakBlockEntity::new, LEAK).build(null);
	public static final BlockEntityType<StorageCubeBlockEntity> STORAGE_CUBE_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(StorageCubeBlockEntity::new, STORAGE_CUBE).build(null);

	// Block Tags
	public static final TagKey<Block> FLUID_TANK_BLOCK_TAG = TagKey.of(RegistryKeys.BLOCK, new Identifier(StarflightMod.MOD_ID, "fluid_tank_blocks"));
	public static final TagKey<Block> EXCLUDED_BLOCK_TAG = TagKey.of(RegistryKeys.BLOCK, new Identifier(StarflightMod.MOD_ID, "excluded_blocks"));
	public static final TagKey<Block> EDGE_CASE_TAG = TagKey.of(RegistryKeys.BLOCK, new Identifier(StarflightMod.MOD_ID, "edge_case_blocks"));
	public static final TagKey<Block> AIR_UPDATE_TAG = TagKey.of(RegistryKeys.BLOCK, new Identifier(StarflightMod.MOD_ID, "air_update_blocks"));
	public static final TagKey<Block> INSTANT_REMOVE_TAG = TagKey.of(RegistryKeys.BLOCK, new Identifier(StarflightMod.MOD_ID, "instant_remove_blocks"));

	// Fluid Storage Values
	public static final double HYDROGEN_TANK_CAPACITY = 64.0;
	public static final double HYDROGEN_PIPE_CAPACITY = 8.0;
	public static final double OXYGEN_TANK_CAPACITY = HYDROGEN_TANK_CAPACITY * 8.0;
	public static final double OXYGEN_PIPE_CAPACITY = HYDROGEN_PIPE_CAPACITY * 8.0;

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
		initializeBlock(SAPPY_RUBBER_LOG, "sappy_rubber_log", null, false, List.of(), List.of());
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
		initializeBlock(LYCOPHYTE_STEM, "lycophyte_stem", null, false, List.of(), List.of());
		initializeBlock(PITCH_BLACK, "pitch_black", null, false, List.of(), List.of());
		initializeBlock(SOLARIZED_REGOLITH, "solarized_regolith");
		initializeBlock(SEARING_REGOLITH, "searing_regolith");
		initializeBlock(FRIGID_STONE, "frigid_stone");
		initializeBlock(TREE_TAP, "tree_tap");
		initializeBlock(PLANETARIUM, "planetarium");
		initializeBlock(COPPER_CABLE, "copper_cable");
		initializeEnergyProducerBlock(STIRLING_ENGINE, "stirling_engine", STIRLING_ENGINE_BLOCK_ENTITY, 10, List.of());
		initializeEnergyConsumerBlock(ELECTRIC_FURNACE, "electric_furnace", ELECTRIC_FURNACE_BLOCK_ENTITY, 10, List.of());
		initializeBlock(SOLAR_PANEL, "solar_panel");
		initializeBlock(SOLAR_HUB, "solar_hub", SOLAR_HUB_BLOCK_ENTITY);
		initializeBlock(BATTERY, "battery", BATTERY_BLOCK_ENTITY);
		initializeBlock(BREAKER_SWITCH, "breaker_switch", null, false, List.of(), List.of(Text.translatable("block.space.breaker_switch.description")));
		initializeBlock(OXYGEN_PIPE, "oxygen_pipe", OXYGEN_PIPE_BLOCK_ENTITY);
		initializeBlock(HYDROGEN_PIPE, "hydrogen_pipe", HYDROGEN_PIPE_BLOCK_ENTITY);
		initializeBlock(OXYGEN_TANK, "oxygen_tank", OXYGEN_TANK_BLOCK_ENTITY);
		initializeBlock(OXYGEN_INLET_VALVE, "oxygen_inlet_valve", OXYGEN_INLET_VALVE_BLOCK_ENTITY);
		initializeBlock(OXYGEN_OUTLET_VALVE, "oxygen_outlet_valve", OXYGEN_OUTLET_VALVE_BLOCK_ENTITY);
		initializeBlock(HYDROGEN_TANK, "hydrogen_tank", HYDROGEN_TANK_BLOCK_ENTITY);
		initializeBlock(HYDROGEN_INLET_VALVE, "hydrogen_inlet_valve", HYDROGEN_INLET_VALVE_BLOCK_ENTITY);
		initializeBlock(HYDROGEN_OUTLET_VALVE, "hydrogen_outlet_valve", HYDROGEN_OUTLET_VALVE_BLOCK_ENTITY);
		initializeBlock(VENT, "vent");
		initializeBlock(COPPER_CABLE_AL, "copper_cable_al");
		initializeBlock(OXYGEN_PIPE_AL, "oxygen_pipe_al", OXYGEN_PIPE_AL_BLOCK_ENTITY);
		initializeBlock(HYDROGEN_PIPE_AL, "hydrogen_pipe_al", HYDROGEN_PIPE_AL_BLOCK_ENTITY);
		initializeBlock(FLUID_TANK_INSIDE, "fluid_tank_inside", null, false, List.of(), List.of());
		initializeBlock(ELECTROLYZER, "electrolyzer", ELECTROLYZER_BLOCK_ENTITY);
		initializeBlock(ICE_ELECTROLYZER, "ice_electrolyzer", ICE_ELECTROLYZER_BLOCK_ENTITY);
		initializeBlock(OXYGEN_DISPENSER, "oxygen_dispenser");
		initializeEnergyConsumerBlock(ATMOSPHERE_GENERATOR, "atmosphere_generator", ATMOSPHERE_GENERATOR_BLOCK_ENTITY, 5, List.of(Text.translatable("block.space.atmosphere_generator.description_1"), Text.translatable("block.space.atmosphere_generator.description_2")));
		initializeBlock(OXYGEN_SENSOR, "oxygen_sensor", null, false, List.of(), List.of(Text.translatable("block.space.oxygen_sensor.description_1"), Text.translatable("block.space.oxygen_sensor.description_2")));
		initializeBlock(HABITABLE_AIR, "habitable_air", null, false, List.of(), List.of());
		initializeBlock(LEAK, "leak", LEAK_BLOCK_ENTITY, false, List.of(), List.of());
		initializeBlock(STORAGE_CUBE, "storage_cube", STORAGE_CUBE_BLOCK_ENTITY);
		initializeBlock(AIRLOCK_DOOR, "airlock_door");
		initializeBlock(AIRLOCK_TRAPDOOR, "airlock_trapdoor");
		initializeBlock(IRON_LADDER, "iron_ladder");
		initializeBlock(LANDING_LEG, "landing_leg", null, true, List.of(), List.of(Text.translatable("block.space.landing_leg.description")));
		initializeBlock(ROCKET_CONTROLLER, "rocket_controller", ROCKET_CONTROLLER_BLOCK_ENTITY);
		initializeBlock(THRUSTER_INITIAL, "thruster_initial");
		initializeBlock(THRUSTER_SMALL, "thruster_small");
		initializeBlock(THRUSTER_VACUUM, "thruster_vacuum");
		initializeBlock(AEROSPIKE, "aerospike");

		// Setup extra block behavior.
		FireBlockInvokerMixin fireBlock = (FireBlockInvokerMixin) Blocks.FIRE;
		fireBlock.callRegisterFlammableBlock(RUBBER_LOG, 5, 5);
		fireBlock.callRegisterFlammableBlock(SAPPY_RUBBER_LOG, 30, 5);
		fireBlock.callRegisterFlammableBlock(STRIPPED_RUBBER_LOG, 5, 5);
		fireBlock.callRegisterFlammableBlock(RUBBER_LEAVES, 30, 60);
	}

	private static void initializeBlock(Block block, String name)
	{
		initializeBlock(block, name, null, true, List.of(), List.of());
	}

	private static void initializeBlock(Block block, String name, @Nullable BlockEntityType<?> blockEntity)
	{
		initializeBlock(block, name, blockEntity, true, List.of(), List.of());
	}

	private static void initializeEnergyProducerBlock(Block block, String name, @Nullable BlockEntityType<?> blockEntity, double powerOutput, List<Text> hiddenTextList)
	{
		ArrayList<Text> textList = new ArrayList<Text>();
		DecimalFormat df = new DecimalFormat("#.##");
		textList.add(Text.translatable("block.space.energy_producer").append(String.valueOf(df.format(powerOutput))).append("kJ/s").formatted(Formatting.GOLD));
		initializeBlock(block, name, blockEntity, true, textList, hiddenTextList);
	}

	private static void initializeEnergyConsumerBlock(Block block, String name, @Nullable BlockEntityType<?> blockEntity, double powerInput, List<Text> hiddenTextList)
	{
		ArrayList<Text> textList = new ArrayList<Text>();
		DecimalFormat df = new DecimalFormat("#.##");
		textList.add(Text.translatable("block.space.energy_consumer").append(String.valueOf(df.format(powerInput))).append("kJ/s").formatted(Formatting.LIGHT_PURPLE));
		initializeBlock(block, name, blockEntity, true, textList, hiddenTextList);
	}

	private static void initializeBlock(Block block, String name, @Nullable BlockEntityType<?> blockEntity, boolean creativeInventory, List<Text> textList, List<Text> hiddenTextList)
	{
		String mod_id = StarflightMod.MOD_ID;
		// ItemGroup itemGroup = StarflightMod.ITEM_GROUP;
		Registry.register(Registries.BLOCK, new Identifier(mod_id, name), block);

		if(textList.isEmpty() && hiddenTextList.isEmpty())
			Registry.register(Registries.ITEM, new Identifier(mod_id, name), new BlockItem(block, new FabricItemSettings()));
		else
			Registry.register(Registries.ITEM, new Identifier(mod_id, name), new DescriptiveBlockItem(block, new FabricItemSettings(), textList, hiddenTextList));

		if(blockEntity != null)
			Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(StarflightMod.MOD_ID, name), blockEntity);
		
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