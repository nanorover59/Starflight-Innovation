package space.block;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CarpetBlock;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.MapColor;
import net.minecraft.block.Material;
import net.minecraft.block.MossBlock;
import net.minecraft.block.OreBlock;
import net.minecraft.block.PillarBlock;
import net.minecraft.block.RedstoneOreBlock;
import net.minecraft.block.SandBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.BlockView;
import space.StarflightMod;
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
import space.block.entity.OxygenInletValveBlockEntity;
import space.block.entity.OxygenOutletValveBlockEntity;
import space.block.entity.OxygenPipeBlockEntity;
import space.block.entity.OxygenTankBlockEntity;
import space.block.entity.PlanetariumBlockEntity;
import space.block.entity.RocketControllerBlockEntity;
import space.block.entity.StirlingEngineBlockEntity;
import space.block.sapling.RubberSaplingGenerator;
import space.item.DescriptiveBlockItem;
import space.mixin.common.FireBlockInvokerMixin;

public class StarflightBlocks
{
	// Blocks
	public static final Block ALUMINUM_BLOCK = new Block(FabricBlockSettings.of(Material.METAL).requiresTool().strength(3.0f, 5.0f));
	public static final Block STRUCTURAL_ALUMINUM = new Block(FabricBlockSettings.of(Material.METAL).sounds(BlockSoundGroup.COPPER).requiresTool().strength(3.0f, 4.0f));
	public static final Block RIVETED_ALUMINUM = new Block(AbstractBlock.Settings.copy(STRUCTURAL_ALUMINUM));
	public static final Block ALUMINUM_FRAME = new FrameBlock(FabricBlockSettings.of(Material.METAL).sounds(BlockSoundGroup.COPPER).requiresTool().strength(3.0f, 3.0f));
	public static final Block WALKWAY = new FrameBlock(AbstractBlock.Settings.copy(ALUMINUM_FRAME));
	public static final Block STRUCTURAL_ALUMINUM_STAIRS = new StairsBlock(STRUCTURAL_ALUMINUM.getDefaultState(), AbstractBlock.Settings.copy(STRUCTURAL_ALUMINUM));
	public static final Block STRUCTURAL_ALUMINUM_SLAB = new SlabBlock(AbstractBlock.Settings.copy(STRUCTURAL_ALUMINUM));
	public static final Block REINFORCED_FABRIC = new Block(FabricBlockSettings.of(Material.WOOL).sounds(BlockSoundGroup.WOOL).strength(2.0f, 3.0f));
	public static final Block BAUXITE_ORE = new OreBlock(AbstractBlock.Settings.copy(Blocks.IRON_ORE));
	public static final Block DEEPSLATE_BAUXITE_ORE = new OreBlock(AbstractBlock.Settings.copy(Blocks.DEEPSLATE_IRON_ORE));
	public static final Block BAUXITE_BLOCK = new Block(FabricBlockSettings.of(Material.STONE).requiresTool().strength(3.0f, 5.0f));
	public static final Block SULFUR_ORE = new OreBlock(AbstractBlock.Settings.copy(Blocks.COAL_ORE));
	public static final Block DEEPSLATE_SULFUR_ORE = new OreBlock(AbstractBlock.Settings.copy(Blocks.DEEPSLATE_COAL_ORE));
	public static final Block SULFUR_BLOCK = new Block(FabricBlockSettings.of(Material.STONE).requiresTool().strength(3.0f, 5.0f));
	public static final Block RUBBER_LOG = createLogBlock(MapColor.OAK_TAN, MapColor.SPRUCE_BROWN);
	public static final Block SAPPY_RUBBER_LOG = createLogBlock(MapColor.OAK_TAN, MapColor.SPRUCE_BROWN);
	public static final Block STRIPPED_RUBBER_LOG = createLogBlock(MapColor.OAK_TAN, MapColor.OAK_TAN);
	public static final Block RUBBER_LEAVES = createLeavesBlock(BlockSoundGroup.WET_GRASS);
	public static final Block RUBBER_SAPLING = new RubberSaplingBlock(new RubberSaplingGenerator(), FabricBlockSettings.copyOf(Blocks.OAK_SAPLING));
	public static final Block CHEESE_BLOCK = new Block(FabricBlockSettings.of(Material.ORGANIC_PRODUCT).strength(2.0f, 2.0f));
	public static final Block REGOLITH = new RegolithBlock(AbstractBlock.Settings.of(Material.AGGREGATE, MapColor.LIGHT_GRAY).sounds(BlockSoundGroup.GRAVEL).strength(0.5F));
	public static final Block BALSALTIC_REGOLITH = new RegolithBlock(AbstractBlock.Settings.of(Material.AGGREGATE, MapColor.STONE_GRAY).sounds(BlockSoundGroup.GRAVEL).strength(0.5F));
	public static final Block ICY_REGOLITH = new RegolithBlock(AbstractBlock.Settings.of(Material.AGGREGATE, MapColor.LIGHT_BLUE_GRAY).sounds(BlockSoundGroup.GRAVEL).strength(0.5F));
	public static final Block FERRIC_SAND = new SandBlock(0xB7633D, AbstractBlock.Settings.of(Material.AGGREGATE, MapColor.ORANGE).sounds(BlockSoundGroup.SAND).strength(0.5F));
	public static final Block FERRIC_STONE = new Block(FabricBlockSettings.of(Material.STONE).requiresTool().strength(1.5f, 6.0f));
	public static final Block REDSLATE = new Block(FabricBlockSettings.of(Material.STONE).requiresTool().strength(1.5f, 6.0f));
	public static final Block FERRIC_IRON_ORE = new OreBlock(FabricBlockSettings.of(Material.STONE).requiresTool().strength(3.0f, 3.0f));
	public static final Block FERRIC_COPPER_ORE = new OreBlock(FabricBlockSettings.of(Material.STONE).requiresTool().strength(3.0f, 3.0f));
	public static final Block FERRIC_BAUXITE_ORE = new OreBlock(FabricBlockSettings.of(Material.STONE).requiresTool().strength(3.0f, 3.0f));
	public static final Block FERRIC_SULFUR_ORE = new OreBlock(FabricBlockSettings.of(Material.STONE).requiresTool().strength(3.0f, 3.0f));
	public static final Block FERRIC_GOLD_ORE = new OreBlock(FabricBlockSettings.of(Material.STONE).requiresTool().strength(3.0f, 3.0f));
	public static final Block FERRIC_DIAMOND_ORE = new OreBlock(FabricBlockSettings.of(Material.STONE).requiresTool().strength(3.0f, 3.0f));
	public static final Block FERRIC_REDSTONE_ORE = new RedstoneOreBlock(AbstractBlock.Settings.of(Material.STONE).requiresTool().ticksRandomly().luminance(createLightLevelFromLitBlockState(9)).strength(3.0f, 3.0f));
	public static final Block HEMATITE_ORE = new OreBlock(FabricBlockSettings.of(Material.STONE).requiresTool().strength(3.0f, 3.0f));
	public static final Block HEMATITE_BLOCK = new Block(FabricBlockSettings.of(Material.METAL).requiresTool().strength(3.0f, 8.0f));
	public static final Block DRY_SNOW_BLOCK = new Block(AbstractBlock.Settings.of(Material.SNOW_BLOCK, MapColor.WHITE).sounds(BlockSoundGroup.SNOW).strength(0.1F));
	public static final Block ARES_MOSS_CARPET = new CarpetBlock(AbstractBlock.Settings.of(Material.PLANT, MapColor.PURPLE).strength(0.1f).sounds(BlockSoundGroup.MOSS_CARPET));
    public static final Block ARES_MOSS_BLOCK = new MossBlock(AbstractBlock.Settings.of(Material.MOSS_BLOCK, MapColor.PURPLE).strength(0.1f).sounds(BlockSoundGroup.MOSS_BLOCK));
    public static final Block LYCOPHYTE_TOP = new LycophyteBlock(AbstractBlock.Settings.of(Material.PLANT).noCollision().strength(0.1f).sounds(BlockSoundGroup.BIG_DRIPLEAF), true);
    public static final Block LYCOPHYTE_STEM = new LycophyteBlock(AbstractBlock.Settings.of(Material.PLANT).noCollision().strength(0.1f).sounds(BlockSoundGroup.BIG_DRIPLEAF), false);
	public static final Block SEARING_REGOLITH = new RegolithBlock(AbstractBlock.Settings.of(Material.AGGREGATE, MapColor.STONE_GRAY).sounds(BlockSoundGroup.GRAVEL).strength(0.5F));
	public static final Block FRIGID_STONE = new Block(FabricBlockSettings.of(Material.STONE).requiresTool().strength(2.0f, 5.0f));
	public static final Block TREE_TAP = new TreeTapBlock(FabricBlockSettings.of(Material.METAL).strength(2.0f, 2.0f));
	public static final Block PLANETARIUM = new PlanetariumBlock(FabricBlockSettings.of(Material.METAL).sounds(BlockSoundGroup.COPPER).requiresTool().strength(4.0f, 5.0f));
	public static final Block COPPER_CABLE = new EnergyCableBlock(FabricBlockSettings.of(Material.METAL).sounds(BlockSoundGroup.WOOL).strength(1.25f, 1.25f));
	public static final Block STIRLING_ENGINE = new StirlingEngineBlock(FabricBlockSettings.of(Material.METAL).sounds(BlockSoundGroup.COPPER).requiresTool().strength(4.0f, 5.0f).luminance(createLightLevelFromLitBlockState(13)));
	public static final Block ELECTRIC_FURNACE = new ElectricFurnaceBlock(FabricBlockSettings.of(Material.METAL).sounds(BlockSoundGroup.COPPER).requiresTool().strength(4.0f, 5.0f).luminance(createLightLevelFromLitBlockState(13)));
	public static final Block SOLAR_PANEL = new SolarPanelBlock(FabricBlockSettings.of(Material.METAL).sounds(BlockSoundGroup.COPPER).requiresTool().strength(4.0f, 5.0f));
	public static final Block BATTERY = new BatteryBlock(FabricBlockSettings.of(Material.METAL).sounds(BlockSoundGroup.COPPER).requiresTool().strength(4.0f, 5.0f));
	public static final Block OXYGEN_PIPE = new OxygenPipeBlock(FabricBlockSettings.of(Material.METAL).requiresTool().strength(2.0f, 2.0f));
	public static final Block HYDROGEN_PIPE = new HydrogenPipeBlock(FabricBlockSettings.of(Material.METAL).strength(2.0f, 2.0f));
	public static final Block OXYGEN_TANK = new OxygenTankBlock(FabricBlockSettings.of(Material.METAL).sounds(BlockSoundGroup.COPPER).strength(4.0f, 5.0f));
	public static final Block OXYGEN_INLET_VALVE = new OxygenInletValveBlock(FabricBlockSettings.of(Material.METAL).sounds(BlockSoundGroup.COPPER).requiresTool().strength(4.0f, 5.0f));
	public static final Block OXYGEN_OUTLET_VALVE = new OxygenOutletValveBlock(FabricBlockSettings.of(Material.METAL).sounds(BlockSoundGroup.COPPER).requiresTool().strength(4.0f, 5.0f));
	public static final Block HYDROGEN_TANK = new HydrogenTankBlock(FabricBlockSettings.of(Material.METAL).sounds(BlockSoundGroup.COPPER).requiresTool().strength(4.0f, 5.0f));
	public static final Block HYDROGEN_INLET_VALVE = new HydrogenInletValveBlock(FabricBlockSettings.of(Material.METAL).sounds(BlockSoundGroup.COPPER).requiresTool().strength(4.0f, 5.0f));
	public static final Block HYDROGEN_OUTLET_VALVE = new HydrogenOutletValveBlock(FabricBlockSettings.of(Material.METAL).sounds(BlockSoundGroup.COPPER).requiresTool().strength(4.0f, 5.0f));
	public static final Block VENT = new VentBlock(FabricBlockSettings.of(Material.METAL).sounds(BlockSoundGroup.COPPER).requiresTool().strength(4.0f, 5.0f));
	public static final Block COPPER_CABLE_AL = new EncasedEnergyCableBlock(FabricBlockSettings.of(Material.METAL).sounds(BlockSoundGroup.COPPER).requiresTool().strength(4.0f, 5.0f));
	public static final Block OXYGEN_PIPE_AL = new EncasedOxygenPipeALBlock(FabricBlockSettings.of(Material.METAL).sounds(BlockSoundGroup.COPPER).requiresTool().strength(4.0f, 5.0f));
	public static final Block HYDROGEN_PIPE_AL = new EncasedHydrogenPipeALBlock(FabricBlockSettings.of(Material.METAL).sounds(BlockSoundGroup.COPPER).requiresTool().strength(4.0f, 5.0f));
	public static final Block FLUID_TANK_INSIDE = new FluidTankInsideBlock(FabricBlockSettings.of(Material.AIR).strength(0.1f, 0.1f));
	public static final Block ELECTROLYZER = new ElectrolyzerBlock(FabricBlockSettings.of(Material.METAL).sounds(BlockSoundGroup.COPPER).requiresTool().strength(4.0f, 5.0f).luminance(createLightLevelFromLitBlockState(13)));
	public static final Block ICE_ELECTROLYZER = new IceElectrolyzerBlock(FabricBlockSettings.of(Material.METAL).sounds(BlockSoundGroup.COPPER).requiresTool().strength(4.0f, 5.0f).luminance(createLightLevelFromLitBlockState(13)));
	public static final Block OXYGEN_DISPENSER = new OxygenDispenserBlock(FabricBlockSettings.of(Material.METAL).sounds(BlockSoundGroup.COPPER).requiresTool().strength(4.0f, 5.0f));
	public static final Block ATMOSPHERE_GENERATOR = new AtmosphereGeneratorBlock(FabricBlockSettings.of(Material.METAL).sounds(BlockSoundGroup.COPPER).requiresTool().strength(4.0f, 5.0f));
	public static final Block HABITABLE_AIR = new HabitableAirBlock(FabricBlockSettings.of(Material.AIR).air());
	public static final Block AIRLOCK_DOOR = new SealedDoorBlock(FabricBlockSettings.of(Material.METAL, MapColor.IRON_GRAY).requiresTool().strength(4.0f).sounds(BlockSoundGroup.COPPER).nonOpaque());
	public static final Block AIRLOCK_TRAPDOOR = new SealedTrapdoorBlock(FabricBlockSettings.of(Material.METAL, MapColor.IRON_GRAY).requiresTool().strength(4.0f).sounds(BlockSoundGroup.COPPER).nonOpaque());
	public static final Block IRON_LADDER = new LadderBlock(FabricBlockSettings.of(Material.METAL, MapColor.IRON_GRAY).requiresTool().strength(4.0f).sounds(BlockSoundGroup.COPPER).nonOpaque());
	public static final Block LANDING_LEG = new FrameBlock(FabricBlockSettings.of(Material.METAL, MapColor.IRON_GRAY).requiresTool().strength(4.0f).sounds(BlockSoundGroup.COPPER).nonOpaque());
	public static final Block ROCKET_CONTROLLER = new RocketControllerBlock(FabricBlockSettings.of(Material.METAL).sounds(BlockSoundGroup.COPPER).requiresTool().strength(4.0f, 5.0f));
	public static final Block THRUSTER_SMALL = new RocketThrusterBlock(FabricBlockSettings.of(Material.METAL).sounds(BlockSoundGroup.ANVIL).requiresTool().strength(5.0f, 6.0f), 0.1e6, 330, 300, 12.0);
	
	// Block Entities
	public static final BlockEntityType<PlanetariumBlockEntity> PLANETARIUM_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(PlanetariumBlockEntity::new, PLANETARIUM).build(null);
	public static final BlockEntityType<StirlingEngineBlockEntity> STIRLING_ENGINE_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(StirlingEngineBlockEntity::new, STIRLING_ENGINE).build(null);
	public static final BlockEntityType<ElectricFurnaceBlockEntity> ELECTRIC_FURNACE_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(ElectricFurnaceBlockEntity::new, ELECTRIC_FURNACE).build(null);
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
	
	// Block Tags
	public static final TagKey<Block> FLUID_TANK_BLOCK_TAG = TagKey.of(Registry.BLOCK_KEY, new Identifier(StarflightMod.MOD_ID, "fluid_tank_blocks"));
	public static final TagKey<Block> EXCLUDED_BLOCK_TAG = TagKey.of(Registry.BLOCK_KEY, new Identifier(StarflightMod.MOD_ID, "excluded_blocks"));
	public static final TagKey<Block> NO_RECURSIVE_SEARCH_TAG = TagKey.of(Registry.BLOCK_KEY, new Identifier(StarflightMod.MOD_ID, "no_recursive_search_blocks"));
	
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
		initializeBlock(BAUXITE_ORE, "bauxite_ore");
		initializeBlock(DEEPSLATE_BAUXITE_ORE, "deepslate_bauxite_ore");
		initializeBlock(BAUXITE_BLOCK, "bauxite_block");
		initializeBlock(SULFUR_ORE, "sulfur_ore");
		initializeBlock(DEEPSLATE_SULFUR_ORE, "deepslate_sulfur_ore");
		initializeBlock(SULFUR_BLOCK, "sulfur_block");
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
		initializeBlock(FERRIC_IRON_ORE, "ferric_iron_ore");
		initializeBlock(FERRIC_COPPER_ORE, "ferric_copper_ore");
		initializeBlock(FERRIC_BAUXITE_ORE, "ferric_bauxite_ore");
		initializeBlock(FERRIC_SULFUR_ORE, "ferric_sulfur_ore");
		initializeBlock(FERRIC_GOLD_ORE, "ferric_gold_ore");
		initializeBlock(FERRIC_DIAMOND_ORE, "ferric_diamond_ore");
		initializeBlock(FERRIC_REDSTONE_ORE, "ferric_redstone_ore");
		initializeBlock(HEMATITE_ORE, "hematite_ore");
		initializeBlock(HEMATITE_BLOCK, "hematite_block");
		initializeBlock(DRY_SNOW_BLOCK, "dry_snow_block");
		initializeBlock(ARES_MOSS_CARPET, "ares_moss_carpet");
		initializeBlock(ARES_MOSS_BLOCK, "ares_moss_block");
		initializeBlock(LYCOPHYTE_TOP, "lycophyte_top");
		initializeBlock(LYCOPHYTE_STEM, "lycophyte_stem", null, false, List.of(), List.of());
		initializeBlock(SEARING_REGOLITH, "searing_regolith");
		initializeBlock(FRIGID_STONE, "frigid_stone");
		initializeBlock(TREE_TAP, "tree_tap");
		initializeBlock(PLANETARIUM, "planetarium", PLANETARIUM_BLOCK_ENTITY);
		initializeBlock(COPPER_CABLE, "copper_cable");
		initializeEnergyProducerBlock(STIRLING_ENGINE, "stirling_engine", STIRLING_ENGINE_BLOCK_ENTITY, 10, List.of());
		initializeEnergyConsumerBlock(ELECTRIC_FURNACE, "electric_furnace", ELECTRIC_FURNACE_BLOCK_ENTITY, 10, List.of());
		initializeEnergyProducerBlock(SOLAR_PANEL, "solar_panel", null, SolarPanelBlock.NOMINAL_OUTPUT, List.of());
		initializeBlock(BATTERY, "battery", BATTERY_BLOCK_ENTITY);
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
		initializeBlock(ATMOSPHERE_GENERATOR, "atmosphere_generator");
		initializeBlock(HABITABLE_AIR, "habitable_air", null, false, List.of(), List.of());
		initializeBlock(AIRLOCK_DOOR, "airlock_door");
		initializeBlock(AIRLOCK_TRAPDOOR, "airlock_trapdoor");
		initializeBlock(IRON_LADDER, "iron_ladder");
		initializeBlock(LANDING_LEG, "landing_leg");
		initializeBlock(ROCKET_CONTROLLER, "rocket_controller", ROCKET_CONTROLLER_BLOCK_ENTITY);
		initializeBlock(THRUSTER_SMALL, "thruster_small");
		
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
		textList.add(Text.translatable("block.space.energy_producer").append(String.valueOf(df.format(powerOutput))).append("kJ/s").formatted(Formatting.LIGHT_PURPLE));
		initializeBlock(block, name, blockEntity, true, textList, hiddenTextList);
	}
	
	private static void initializeEnergyConsumerBlock(Block block, String name, @Nullable BlockEntityType<?> blockEntity, double powerInput, List<Text> hiddenTextList)
	{
		ArrayList<Text> textList = new ArrayList<Text>();
		DecimalFormat df = new DecimalFormat("#.##");
		textList.add(Text.translatable("block.space.energy_consumer").append(String.valueOf(df.format(powerInput))).append("kJ/s").formatted(Formatting.GOLD));
		initializeBlock(block, name, blockEntity, true, textList, hiddenTextList);
	}
	
	private static void initializeBlock(Block block, String name, @Nullable BlockEntityType<?> blockEntity, boolean creativeInventory, List<Text> textList, List<Text> hiddenTextList)
	{
		String mod_id = StarflightMod.MOD_ID;
		ItemGroup itemGroup = StarflightMod.ITEM_GROUP;
		Registry.register(Registry.BLOCK, new Identifier(mod_id, name), block);
		
		if(textList.isEmpty())
			Registry.register(Registry.ITEM, new Identifier(mod_id, name), new BlockItem(block, new FabricItemSettings().group(creativeInventory ? itemGroup : null)));
		else
			Registry.register(Registry.ITEM, new Identifier(mod_id, name), new DescriptiveBlockItem(block, new FabricItemSettings().group(itemGroup), textList, hiddenTextList));
		
		if(blockEntity != null)
			Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(StarflightMod.MOD_ID, name), blockEntity);
	}

	private static ToIntFunction<BlockState> createLightLevelFromLitBlockState(int litLevel)
	{
		return (state) ->
		{
			return (Boolean) state.get(Properties.LIT) ? litLevel : 0;
		};
	}
	
	private static PillarBlock createLogBlock(MapColor topMapColor, MapColor sideMapColor)
	{
        return new PillarBlock(AbstractBlock.Settings.of(Material.WOOD, state -> state.get(PillarBlock.AXIS) == Direction.Axis.Y ? topMapColor : sideMapColor).strength(2.0f).sounds(BlockSoundGroup.WOOD));
    }
	
	private static LeavesBlock createLeavesBlock(BlockSoundGroup soundGroup)
	{
        return new LeavesBlock(AbstractBlock.Settings.of(Material.LEAVES).strength(0.2f).ticksRandomly().sounds(soundGroup).nonOpaque().allowsSpawning(StarflightBlocks::canSpawnOnLeaves).suffocates(StarflightBlocks::never).blockVision(StarflightBlocks::never));
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