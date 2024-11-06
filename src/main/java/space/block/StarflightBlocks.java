package space.block;

import java.util.List;
import java.util.Optional;
import java.util.function.ToIntFunction;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AmethystClusterBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.BulbBlock;
import net.minecraft.block.CarpetBlock;
import net.minecraft.block.ColoredFallingBlock;
import net.minecraft.block.CraftingTableBlock;
import net.minecraft.block.ExperienceDroppingBlock;
import net.minecraft.block.HangingRootsBlock;
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
import net.minecraft.block.TransparentBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item.Settings;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ColorCode;
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
import space.block.entity.ElectricLightBlockEntity;
import space.block.entity.ElectrolyzerBlockEntity;
import space.block.entity.ExtractorBlockEntity;
import space.block.entity.FluidPipeBlockEntity;
import space.block.entity.FluidTankControllerBlockEntity;
import space.block.entity.LeakBlockEntity;
import space.block.entity.MetalFabricatorBlockEntity;
import space.block.entity.PumpBlockEntity;
import space.block.entity.RocketControllerBlockEntity;
import space.block.entity.SolarPanelBlockEntity;
import space.block.entity.StirlingEngineBlockEntity;
import space.block.entity.StorageCubeBlockEntity;
import space.block.entity.TargetingComputerBlockEntity;
import space.block.entity.VacuumFurnaceBlockEntity;
import space.block.entity.ValveBlockEntity;
import space.block.entity.VentBlockEntity;
import space.block.entity.VolcanicVentBlockEntity;
import space.block.entity.WaterTankBlockEntity;
import space.item.DescriptiveBlockItem;
import space.item.StarflightItems;
import space.mixin.common.FireBlockInvokerMixin;
import space.util.FluidResourceType;

public class StarflightBlocks
{
	// Blocks
	public static final Block ALUMINUM_BLOCK = new Block(AbstractBlock.Settings.create().mapColor(MapColor.IRON_GRAY).instrument(NoteBlockInstrument.IRON_XYLOPHONE).requiresTool().strength(4.0f, 5.0f).sounds(BlockSoundGroup.COPPER));
	public static final Block STRUCTURAL_ALUMINUM = new Block(AbstractBlock.Settings.copy(ALUMINUM_BLOCK));
	public static final Block RIVETED_ALUMINUM = new Block(AbstractBlock.Settings.copy(STRUCTURAL_ALUMINUM));
	public static final Block DAMAGED_ALUMINUM = new Block(AbstractBlock.Settings.copy(STRUCTURAL_ALUMINUM));
	public static final Block STRUCTURAL_ALUMINUM_STAIRS = new StairsBlock(STRUCTURAL_ALUMINUM.getDefaultState(), AbstractBlock.Settings.copy(STRUCTURAL_ALUMINUM));
	public static final Block STRUCTURAL_ALUMINUM_SLAB = new SlabBlock(AbstractBlock.Settings.copy(STRUCTURAL_ALUMINUM));
	public static final Block ALUMINUM_WALL = new WallBlock(AbstractBlock.Settings.copy(STRUCTURAL_ALUMINUM));
	public static final Block ALUMINUM_FRAME = new FrameBlock(AbstractBlock.Settings.copy(ALUMINUM_BLOCK).strength(3.0f, 3.0f).nonOpaque());
	public static final Block WALKWAY = new FrameBlock(AbstractBlock.Settings.copy(ALUMINUM_FRAME).nonOpaque());
	public static final Block BAUXITE_BLOCK = new Block(AbstractBlock.Settings.copy(Blocks.RAW_IRON_BLOCK));
	public static final Block BAUXITE_ORE = new ExperienceDroppingBlock(ConstantIntProvider.create(0), AbstractBlock.Settings.copy(Blocks.IRON_ORE));
	public static final Block DEEPSLATE_BAUXITE_ORE = new ExperienceDroppingBlock(ConstantIntProvider.create(0), AbstractBlock.Settings.copy(Blocks.DEEPSLATE_IRON_ORE));
	public static final Block REINFORCED_FABRIC = new Block(AbstractBlock.Settings.copy(Blocks.WHITE_WOOL).strength(2.0f, 3.0f));
	public static final Block IRON_FRAME = new FrameBlock(AbstractBlock.Settings.copy(Blocks.IRON_BLOCK).strength(4.0f, 4.0f).sounds(BlockSoundGroup.COPPER).nonOpaque());
	public static final Block STRUCTURAL_IRON = new Block(AbstractBlock.Settings.copy(Blocks.IRON_BLOCK).sounds(BlockSoundGroup.COPPER));
	public static final Block STRUCTURAL_IRON_STAIRS = new StairsBlock(STRUCTURAL_IRON.getDefaultState(), AbstractBlock.Settings.copy(STRUCTURAL_IRON));
	public static final Block STRUCTURAL_IRON_SLAB = new SlabBlock(AbstractBlock.Settings.copy(STRUCTURAL_IRON));
	public static final Block STRUCTURAL_IRON_WALL = new WallBlock(AbstractBlock.Settings.copy(STRUCTURAL_IRON));
	public static final Block IRON_ROUND_DECO = new Block(AbstractBlock.Settings.copy(STRUCTURAL_IRON));
	public static final Block IRON_BULB = new BulbBlock(AbstractBlock.Settings.copy(Blocks.COPPER_BULB).luminance(createLightLevelFromLitBlockState(10)));
	public static final Block BUFFER = new SimpleFacingBlock(AbstractBlock.Settings.copy(Blocks.IRON_BLOCK));
	public static final Block IRON_MACHINE_CASING = new Block(AbstractBlock.Settings.copy(Blocks.IRON_BLOCK).sounds(BlockSoundGroup.COPPER));
	public static final Block TITANIUM_BLOCK = new Block(AbstractBlock.Settings.copy(ALUMINUM_BLOCK).strength(6.0f, 7.0f).sounds(BlockSoundGroup.COPPER));
	public static final Block TITANIUM_FRAME = new FrameBlock(AbstractBlock.Settings.copy(TITANIUM_BLOCK).strength(4.0f, 4.0f).nonOpaque());
	public static final Block STRUCTURAL_TITANIUM = new Block(AbstractBlock.Settings.copy(TITANIUM_BLOCK));
	public static final Block STRUCTURAL_TITANIUM_STAIRS = new StairsBlock(STRUCTURAL_TITANIUM.getDefaultState(), AbstractBlock.Settings.copy(STRUCTURAL_TITANIUM));
	public static final Block STRUCTURAL_TITANIUM_SLAB = new SlabBlock(AbstractBlock.Settings.copy(STRUCTURAL_TITANIUM));
	public static final Block STRUCTURAL_TITANIUM_WALL = new WallBlock(AbstractBlock.Settings.copy(STRUCTURAL_TITANIUM));
	public static final Block DAMAGED_STRUCTURAL_TITANIUM = new Block(AbstractBlock.Settings.copy(TITANIUM_BLOCK));
	public static final Block TITANIUM_MACHINE_CASING = new Block(AbstractBlock.Settings.copy(TITANIUM_BLOCK));
	public static final Block TITANIUM_GLASS = new TransparentBlock(AbstractBlock.Settings.copy(TITANIUM_BLOCK).nonOpaque().allowsSpawning(Blocks::never).solidBlock(Blocks::never).suffocates(Blocks::never).blockVision(Blocks::never));
	public static final Block ILMENITE_BLOCK = new Block(AbstractBlock.Settings.copy(Blocks.RAW_GOLD_BLOCK));
	public static final Block ILMENITE_ORE = new ExperienceDroppingBlock(ConstantIntProvider.create(0), AbstractBlock.Settings.copy(Blocks.GOLD_ORE));
	public static final Block DEEPSLATE_ILMENITE_ORE = new ExperienceDroppingBlock(ConstantIntProvider.create(0), AbstractBlock.Settings.copy(Blocks.DEEPSLATE_GOLD_ORE));
	public static final Block SULFUR_BLOCK = new Block(AbstractBlock.Settings.copy(Blocks.COAL_BLOCK).strength(3.0f, 5.0f));
	public static final Block SULFUR_ORE = new ExperienceDroppingBlock(UniformIntProvider.create(0, 3), AbstractBlock.Settings.copy(Blocks.COAL_ORE));
	public static final Block DEEPSLATE_SULFUR_ORE = new ExperienceDroppingBlock(UniformIntProvider.create(0, 3), AbstractBlock.Settings.copy(Blocks.DEEPSLATE_COAL_ORE));
	public static final Block BASALT_SULFUR_ORE = new ExperienceDroppingBlock(UniformIntProvider.create(0, 3), AbstractBlock.Settings.copy(Blocks.COAL_ORE));
	public static final Block RUBBER_LOG = createLogBlock(MapColor.OAK_TAN, MapColor.SPRUCE_BROWN);
	public static final Block SAPPY_RUBBER_LOG = createLogBlock(MapColor.OAK_TAN, MapColor.SPRUCE_BROWN);
	public static final Block STRIPPED_RUBBER_LOG = createLogBlock(MapColor.OAK_TAN, MapColor.OAK_TAN);
	public static final Block RUBBER_LEAVES = createLeavesBlock(BlockSoundGroup.WET_GRASS);
	public static final Block RUBBER_SAPLING = new SaplingBlock(new SaplingGenerator("rubber", Optional.empty(), Optional.of(RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE, Identifier.of(StarflightMod.MOD_ID, "rubber_tree"))), Optional.empty()), AbstractBlock.Settings.copy(Blocks.OAK_SAPLING));
	public static final Block ICICLE = new IcicleBlock(AbstractBlock.Settings.copy(Blocks.POINTED_DRIPSTONE).sounds(BlockSoundGroup.GLASS));
	public static final Block CHEESE_BLOCK = new Block(AbstractBlock.Settings.copy(Blocks.WET_SPONGE).strength(1.0f, 1.0f));
	public static final Block REGOLITH = new ColoredFallingBlock(new ColorCode(-8356741), AbstractBlock.Settings.copy(Blocks.GRAVEL).mapColor(MapColor.LIGHT_GRAY).strength(0.5F));
	public static final Block BALSALTIC_REGOLITH = new ColoredFallingBlock(new ColorCode(-8356741), AbstractBlock.Settings.copy(Blocks.GRAVEL).mapColor(MapColor.STONE_GRAY).strength(0.5F));
	public static final Block ICY_REGOLITH = new ColoredFallingBlock(new ColorCode(-8356741), AbstractBlock.Settings.copy(Blocks.GRAVEL).mapColor(MapColor.LIGHT_BLUE_GRAY).strength(0.5F));
	public static final Block LUNALIGHT_BLOCK = new Block(AbstractBlock.Settings.copy(Blocks.AMETHYST_BLOCK).mapColor(MapColor.CYAN).luminance(state -> 13));
	public static final Block LUNALIGHT_ORE = new LunalightOreBlock(AbstractBlock.Settings.copy(Blocks.REDSTONE_ORE));
	public static final Block DEEPSLATE_LUNALIGHT_ORE = new LunalightOreBlock(AbstractBlock.Settings.copy(Blocks.DEEPSLATE_REDSTONE_ORE));
	public static final Block FERRIC_SAND = new ColoredFallingBlock(new ColorCode(0xB7633D), AbstractBlock.Settings.copy(Blocks.SAND).mapColor(MapColor.ORANGE));
	public static final Block FERRIC_STONE = new Block(AbstractBlock.Settings.copy(Blocks.STONE));
	public static final Block REDSLATE = new Block(AbstractBlock.Settings.copy(Blocks.SANDSTONE));
	public static final Block REDSLATE_BRICKS = new Block(AbstractBlock.Settings.copy(REDSLATE));
	public static final Block REDSLATE_BRICK_STAIRS = new StairsBlock(REDSLATE_BRICKS.getDefaultState(), AbstractBlock.Settings.copy(REDSLATE_BRICKS));
	public static final Block REDSLATE_BRICK_SLAB = new SlabBlock(AbstractBlock.Settings.copy(REDSLATE_BRICKS));
	public static final Block FERRIC_IRON_ORE = new ExperienceDroppingBlock(ConstantIntProvider.create(0), AbstractBlock.Settings.copy(Blocks.IRON_ORE));
	public static final Block FERRIC_COPPER_ORE = new ExperienceDroppingBlock(ConstantIntProvider.create(0), AbstractBlock.Settings.copy(Blocks.COPPER_ORE));
	public static final Block FERRIC_BAUXITE_ORE = new ExperienceDroppingBlock(ConstantIntProvider.create(0), AbstractBlock.Settings.copy(Blocks.IRON_ORE));
	public static final Block FERRIC_ILMENITE_ORE = new ExperienceDroppingBlock(ConstantIntProvider.create(0), AbstractBlock.Settings.copy(Blocks.GOLD_ORE));
	public static final Block FERRIC_SULFUR_ORE = new ExperienceDroppingBlock(UniformIntProvider.create(0, 3), AbstractBlock.Settings.copy(Blocks.COAL_ORE));
	public static final Block FERRIC_GOLD_ORE = new ExperienceDroppingBlock(ConstantIntProvider.create(0), AbstractBlock.Settings.copy(Blocks.GOLD_ORE));
	public static final Block FERRIC_DIAMOND_ORE = new ExperienceDroppingBlock(UniformIntProvider.create(3, 7), AbstractBlock.Settings.copy(Blocks.DIAMOND_ORE));
	public static final Block FERRIC_REDSTONE_ORE = new RedstoneOreBlock(AbstractBlock.Settings.copy(Blocks.REDSTONE_ORE));
	public static final Block HEMATITE_ORE = new ExperienceDroppingBlock(UniformIntProvider.create(0, 2), AbstractBlock.Settings.copy(Blocks.IRON_ORE));
	public static final Block HEMATITE_BLOCK = new Block(AbstractBlock.Settings.copy(Blocks.STONE).strength(3.0f, 8.0f));
	public static final Block REDSTONE_GLASS = new RedstoneDopedBlock(AbstractBlock.Settings.copy(Blocks.GLASS).strength(0.6f).luminance(createLightLevelFromLitBlockState(5)));
	public static final Block DRY_SNOW_BLOCK = new Block(AbstractBlock.Settings.copy(Blocks.SNOW_BLOCK).strength(0.1F));
	public static final Block ARES_MOSS_CARPET = new CarpetBlock(AbstractBlock.Settings.copy(Blocks.MOSS_CARPET).mapColor(MapColor.PURPLE));
	public static final Block ARES_MOSS_BLOCK = new MossBlock(AbstractBlock.Settings.copy(Blocks.MOSS_BLOCK).mapColor(MapColor.PURPLE));
	public static final Block LYCOPHYTE_TOP = new LycophyteBlock(AbstractBlock.Settings.copy(Blocks.BIG_DRIPLEAF_STEM), true);
	public static final Block LYCOPHYTE_STEM = new LycophyteBlock(AbstractBlock.Settings.copy(Blocks.BIG_DRIPLEAF_STEM), false);
	public static final Block REDSTONE_BLOSSOM = new RedstoneBlossomBlock(StatusEffects.STRENGTH, 8.0f, AbstractBlock.Settings.copy(Blocks.TORCHFLOWER).mapColor(MapColor.BRIGHT_RED).luminance(state -> 7).offset(AbstractBlock.OffsetType.NONE));
	public static final Block MARS_ROOTS = new HangingRootsBlock(AbstractBlock.Settings.copy(Blocks.HANGING_ROOTS).mapColor(MapColor.PURPLE));
	public static final Block MARS_POTATOES = new MarsPotatoesBlock(AbstractBlock.Settings.copy(Blocks.POTATOES).mapColor(MapColor.PURPLE));
	public static final Block REDSTONE_CLUSTER = new AmethystClusterBlock(7.0f, 3.0f, AbstractBlock.Settings.copy(Blocks.AMETHYST_CLUSTER).mapColor(MapColor.RED));
	public static final Block DENSE_CLOUD = new DenseCloudBlock(AbstractBlock.Settings.copy(Blocks.POWDER_SNOW).solidBlock(Blocks::never).suffocates(Blocks::never).blockVision(Blocks::always));
	public static final Block VOLCANIC_SLATE = new Block(AbstractBlock.Settings.copy(Blocks.DEEPSLATE));
	public static final Block VOLCANIC_VENT = new VolcanicVentBlock(AbstractBlock.Settings.copy(Blocks.MAGMA_BLOCK));
	public static final Block AEROPLANKTON = new Block(AbstractBlock.Settings.copy(Blocks.MOSS_BLOCK));
	public static final Block RED_AEROPLANKTON = new Block(AbstractBlock.Settings.copy(Blocks.MOSS_BLOCK));
	public static final Block PITCH_BLACK = new Block(AbstractBlock.Settings.copy(Blocks.IRON_BLOCK).strength(256.0f, 256.0f));
	public static final Block SOLARIZED_REGOLITH = new HotRegolith(new ColorCode(-8356741), AbstractBlock.Settings.copy(Blocks.GRAVEL).mapColor(MapColor.STONE_GRAY).strength(0.5F));
	public static final Block SEARING_REGOLITH = new HotRegolith(new ColorCode(-8356741), AbstractBlock.Settings.copy(Blocks.GRAVEL).mapColor(MapColor.ORANGE).strength(0.5F));
	public static final Block DIAMOND_REGOLITH = new HotRegolith(new ColorCode(MapColor.DIAMOND_BLUE.color), AbstractBlock.Settings.copy(Blocks.GRAVEL).mapColor(MapColor.DIAMOND_BLUE).strength(3.0F, 3.0F));
	public static final Block FRIGID_STONE = new Block(AbstractBlock.Settings.copy(Blocks.STONE));
	public static final Block TREE_TAP = new TreeTapBlock(AbstractBlock.Settings.copy(Blocks.IRON_BLOCK).strength(1.0f, 1.0f).ticksRandomly());
	public static final Block IRON_LADDER = new LadderBlock(AbstractBlock.Settings.copy(ALUMINUM_FRAME).nonOpaque());
	public static final Block PLANETARIUM = new PlanetariumBlock(AbstractBlock.Settings.copy(ALUMINUM_BLOCK));
	public static final Block ALUMINUM_SHELF = new Block(AbstractBlock.Settings.copy(STRUCTURAL_ALUMINUM));
	public static final Block METAL_CRAFTING_TABLE = new CraftingTableBlock(AbstractBlock.Settings.copy(ALUMINUM_BLOCK));
	public static final Block STORAGE_CUBE = new StorageCubeBlock(AbstractBlock.Settings.copy(ALUMINUM_BLOCK));
	public static final Block STIRLING_ENGINE = new StirlingEngineBlock(AbstractBlock.Settings.copy(ALUMINUM_BLOCK).luminance(createLightLevelFromLitBlockState(13)));
	public static final Block ELECTRIC_FURNACE = new ElectricFurnaceBlock(AbstractBlock.Settings.copy(ALUMINUM_BLOCK).luminance(createLightLevelFromLitBlockState(13)));
	public static final Block PUMP = new PumpBlock(AbstractBlock.Settings.copy(ALUMINUM_BLOCK));
	public static final Block ELECTROLYZER = new ElectrolyzerBlock(AbstractBlock.Settings.copy(ALUMINUM_BLOCK).luminance(createLightLevelFromLitBlockState(13)));
	public static final Block EXTRACTOR = new ExtractorBlock(AbstractBlock.Settings.copy(ALUMINUM_BLOCK).luminance(createLightLevelFromLitBlockState(13)));
	public static final Block VACUUM_FURNACE = new VacuumFurnaceBlock(AbstractBlock.Settings.copy(TITANIUM_BLOCK).luminance(createLightLevelFromLitBlockState(13)));
	public static final Block METAL_FABRICATOR = new MetalFabricatorBlock(AbstractBlock.Settings.copy(TITANIUM_BLOCK).luminance(createLightLevelFromLitBlockState(7)));
	public static final Block ATMOSPHERE_GENERATOR = new AtmosphereGeneratorBlock(AbstractBlock.Settings.copy(ALUMINUM_BLOCK));
	public static final Block SOLAR_PANEL = new SolarPanelBlock(AbstractBlock.Settings.copy(ALUMINUM_BLOCK).strength(1.0f, 1.0f));
	public static final Block BATTERY = new BatteryBlock(AbstractBlock.Settings.copy(ALUMINUM_BLOCK));
	public static final Block LIGHT_COLUMN = new LightColumnBlock(AbstractBlock.Settings.copy(ALUMINUM_BLOCK).luminance(createLightLevelFromLitBlockState(15)));
	public static final Block LUNAR_LAMP = new ElectricLightBlock(AbstractBlock.Settings.copy(ALUMINUM_BLOCK).luminance(createLightLevelFromLitBlockState(15)));
	public static final Block BREAKER_SWITCH = new BreakerSwitchBlock(AbstractBlock.Settings.copy(ALUMINUM_BLOCK));
	public static final Block WATER_TANK = new WaterTankBlock(AbstractBlock.Settings.copy(ALUMINUM_BLOCK).nonOpaque());
	public static final Block OXYGEN_TANK = new FluidTankControllerBlock(AbstractBlock.Settings.copy(ALUMINUM_BLOCK), FluidResourceType.OXYGEN, FluidResourceType.OXYGEN.getStorageDensity());
	public static final Block HYDROGEN_TANK = new FluidTankControllerBlock(AbstractBlock.Settings.copy(ALUMINUM_BLOCK), FluidResourceType.HYDROGEN, FluidResourceType.HYDROGEN.getStorageDensity());
	public static final Block BALLOON_CONTROLLER = new BalloonControllerBlock(AbstractBlock.Settings.copy(REINFORCED_FABRIC));
	public static final Block VALVE = new ValveBlock(AbstractBlock.Settings.copy(ALUMINUM_BLOCK));
	public static final Block COPPER_CABLE_AL = new EncasedEnergyCableBlock(AbstractBlock.Settings.copy(ALUMINUM_BLOCK));
	public static final Block WATER_PIPE_AL = new EncasedFluidPipeBlock(AbstractBlock.Settings.copy(ALUMINUM_BLOCK), FluidResourceType.WATER);
	public static final Block OXYGEN_PIPE_AL = new EncasedFluidPipeBlock(AbstractBlock.Settings.copy(ALUMINUM_BLOCK), FluidResourceType.OXYGEN);
	public static final Block HYDROGEN_PIPE_AL = new EncasedFluidPipeBlock(AbstractBlock.Settings.copy(ALUMINUM_BLOCK), FluidResourceType.HYDROGEN);
	public static final Block OXYGEN_DISPENSER = new OxygenDispenserBlock(AbstractBlock.Settings.copy(ALUMINUM_BLOCK));
	public static final Block VENT = new VentBlock(AbstractBlock.Settings.copy(ALUMINUM_BLOCK));
	public static final Block OXYGEN_SENSOR = new OxygenSensorBlock(AbstractBlock.Settings.copy(ALUMINUM_BLOCK));
	public static final Block AIRWAY = new AirwayBlock(AbstractBlock.Settings.copy(STRUCTURAL_ALUMINUM));
	public static final Block AIRLOCK_DOOR = new SealedDoorBlock(AbstractBlock.Settings.copy(ALUMINUM_BLOCK).nonOpaque());
	public static final Block AIRLOCK_TRAPDOOR = new SealedTrapdoorBlock(AbstractBlock.Settings.copy(ALUMINUM_BLOCK).nonOpaque());
	public static final Block TITANIUM_DOOR = new SealedDoorBlock(AbstractBlock.Settings.copy(TITANIUM_BLOCK).nonOpaque());
	public static final Block LEVER_BLOCK = new SolidLeverBlock(AbstractBlock.Settings.copy(STRUCTURAL_ALUMINUM));
	public static final Block POINTER_COLUMN = new PointerColumnBlock(AbstractBlock.Settings.copy(STRUCTURAL_IRON).luminance(createLightLevelFromLitBlockState(7)));
	public static final Block LINEAR_ACTUATOR = new LinearActuatorBlock(AbstractBlock.Settings.copy(TITANIUM_MACHINE_CASING));
	public static final Block LINEAR_TRACK = new PillarBlock(AbstractBlock.Settings.copy(TITANIUM_MACHINE_CASING));
	public static final Block CALL_TRACK = new CallTrackBlock(AbstractBlock.Settings.copy(TITANIUM_MACHINE_CASING));
	public static final Block AIRSHIP_CONTROLLER = new AirshipControllerBlock(AbstractBlock.Settings.copy(ALUMINUM_BLOCK));
	public static final Block AIRSHIP_MOTOR = new SimpleFacingBlock(AbstractBlock.Settings.copy(ALUMINUM_BLOCK));
	public static final Block REACTION_WHEEL = new ReactionWheelBlock(AbstractBlock.Settings.copy(ALUMINUM_BLOCK), 100e3f);
	public static final Block RCS_BLOCK = new ReactionControlThrusterBlock(AbstractBlock.Settings.copy(ALUMINUM_BLOCK), ReactionControlThrusterBlock.DIAGONAL);
	public static final Block LANDING_LEG = new FrameBlock(AbstractBlock.Settings.copy(ALUMINUM_FRAME).nonOpaque());
	public static final Block ROCKET_CONTROLLER = new RocketControllerBlock(AbstractBlock.Settings.copy(ALUMINUM_BLOCK));
	public static final Block TARGETING_COMPUTER = new TargetingComputerBlock(AbstractBlock.Settings.copy(ALUMINUM_BLOCK));
	public static final Block ENGINE_0 = new RocketThrusterBlock(AbstractBlock.Settings.copy(Blocks.IRON_BLOCK).sounds(BlockSoundGroup.ANVIL), 1.25e6, 380, 340, 100.0, 0.0);
	public static final Block ENGINE_1 = new RocketThrusterBlock(AbstractBlock.Settings.copy(TITANIUM_BLOCK), 1.0e6, 400, 380, 100.0, 0.0);
	public static final Block ENGINE_1_GIMBAL = new RocketThrusterBlock(AbstractBlock.Settings.copy(TITANIUM_BLOCK), 1.0e6, 415, 380, 100.0, Math.PI / 16.0);
	public static final Block ENGINE_1_VACUUM = new RocketThrusterBlock(AbstractBlock.Settings.copy(TITANIUM_BLOCK), 0.5e6, 455, 250, 100.0, 0.0);
	public static final Block COPPER_CABLE = new EnergyCableBlock(AbstractBlock.Settings.copy(Blocks.COPPER_BLOCK).strength(1.0f, 1.0f));
	public static final Block WATER_PIPE = new FluidPipeBlock(AbstractBlock.Settings.copy(ALUMINUM_BLOCK).strength(1.0f, 1.0f), FluidResourceType.WATER);
	public static final Block OXYGEN_PIPE = new FluidPipeBlock(AbstractBlock.Settings.copy(ALUMINUM_BLOCK).strength(1.0f, 1.0f), FluidResourceType.OXYGEN);
	public static final Block HYDROGEN_PIPE = new FluidPipeBlock(AbstractBlock.Settings.copy(ALUMINUM_BLOCK).strength(1.0f, 1.0f), FluidResourceType.HYDROGEN);
	public static final Block FLUID_TANK_INSIDE = new FluidTankInsideBlock(AbstractBlock.Settings.create().dropsNothing());
	public static final Block HABITABLE_AIR = new HabitableAirBlock(AbstractBlock.Settings.create().replaceable().noCollision().dropsNothing().air());
	public static final Block LEAK = new LeakBlock(AbstractBlock.Settings.create().replaceable().noCollision().dropsNothing().air());
	
	// Block Entities
	public static final BlockEntityType<StirlingEngineBlockEntity> STIRLING_ENGINE_BLOCK_ENTITY = BlockEntityType.Builder.create(StirlingEngineBlockEntity::new, STIRLING_ENGINE).build(null);
	public static final BlockEntityType<ElectricFurnaceBlockEntity> ELECTRIC_FURNACE_BLOCK_ENTITY = BlockEntityType.Builder.create(ElectricFurnaceBlockEntity::new, ELECTRIC_FURNACE, VACUUM_FURNACE).build(null);
	public static final BlockEntityType<SolarPanelBlockEntity> SOLAR_PANEL_BLOCK_ENTITY = BlockEntityType.Builder.create(SolarPanelBlockEntity::new, SOLAR_PANEL).build(null);
	public static final BlockEntityType<BatteryBlockEntity> BATTERY_BLOCK_ENTITY = BlockEntityType.Builder.create(BatteryBlockEntity::new, BATTERY).build(null);
	public static final BlockEntityType<ElectricLightBlockEntity> ELECTRIC_LIGHT_BLOCK_ENTITY = BlockEntityType.Builder.create(ElectricLightBlockEntity::new, LIGHT_COLUMN, LUNAR_LAMP).build(null);
	public static final BlockEntityType<FluidPipeBlockEntity> FLUID_PIPE_BLOCK_ENTITY = BlockEntityType.Builder.create(FluidPipeBlockEntity::new, WATER_PIPE, OXYGEN_PIPE, HYDROGEN_PIPE, WATER_PIPE_AL, OXYGEN_PIPE_AL, HYDROGEN_PIPE_AL).build(null);
	public static final BlockEntityType<FluidTankControllerBlockEntity> FLUID_TANK_CONTROLLER_BLOCK_ENTITY = BlockEntityType.Builder.create(FluidTankControllerBlockEntity::new, OXYGEN_TANK, HYDROGEN_TANK).build(null);
	public static final BlockEntityType<BalloonControllerBlockEntity> BALLOON_CONTROLLER_BLOCK_ENTITY = BlockEntityType.Builder.create(BalloonControllerBlockEntity::new, BALLOON_CONTROLLER).build(null);
	public static final BlockEntityType<ValveBlockEntity> VALVE_BLOCK_ENTITY = BlockEntityType.Builder.create(ValveBlockEntity::new, VALVE).build(null);
	public static final BlockEntityType<VentBlockEntity> VENT_BLOCK_ENTITY = BlockEntityType.Builder.create(VentBlockEntity::new, VENT).build(null);
	public static final BlockEntityType<WaterTankBlockEntity> WATER_TANK_BLOCK_ENTITY = BlockEntityType.Builder.create(WaterTankBlockEntity::new, WATER_TANK).build(null);
	public static final BlockEntityType<PumpBlockEntity> PUMP_BLOCK_ENTITY = BlockEntityType.Builder.create(PumpBlockEntity::new, PUMP).build(null);
	public static final BlockEntityType<ExtractorBlockEntity> EXTRACTOR_BLOCK_ENTITY = BlockEntityType.Builder.create(ExtractorBlockEntity::new, EXTRACTOR).build(null);
	public static final BlockEntityType<ElectrolyzerBlockEntity> ELECTROLYZER_BLOCK_ENTITY = BlockEntityType.Builder.create(ElectrolyzerBlockEntity::new, ELECTROLYZER).build(null);
	public static final BlockEntityType<VacuumFurnaceBlockEntity> VACUUM_FURNACE_BLOCK_ENTITY = BlockEntityType.Builder.create(VacuumFurnaceBlockEntity::new, VACUUM_FURNACE).build(null);
	public static final BlockEntityType<MetalFabricatorBlockEntity> METAL_FABRICATOR_BLOCK_ENTITY = BlockEntityType.Builder.create(MetalFabricatorBlockEntity::new, METAL_FABRICATOR).build(null);
	public static final BlockEntityType<RocketControllerBlockEntity> ROCKET_CONTROLLER_BLOCK_ENTITY = BlockEntityType.Builder.create(RocketControllerBlockEntity::new, ROCKET_CONTROLLER).build(null);
	public static final BlockEntityType<TargetingComputerBlockEntity> TARGETING_COMPUTER_BLOCK_ENTITY = BlockEntityType.Builder.create(TargetingComputerBlockEntity::new, TARGETING_COMPUTER).build(null);
	public static final BlockEntityType<AtmosphereGeneratorBlockEntity> ATMOSPHERE_GENERATOR_BLOCK_ENTITY = BlockEntityType.Builder.create(AtmosphereGeneratorBlockEntity::new, ATMOSPHERE_GENERATOR).build(null);
	public static final BlockEntityType<LeakBlockEntity> LEAK_BLOCK_ENTITY = BlockEntityType.Builder.create(LeakBlockEntity::new, LEAK).build(null);
	public static final BlockEntityType<VolcanicVentBlockEntity> VOLCANIC_VENT_BLOCK_ENTITY = BlockEntityType.Builder.create(VolcanicVentBlockEntity::new, VOLCANIC_VENT).build(null);
	public static final BlockEntityType<StorageCubeBlockEntity> STORAGE_CUBE_BLOCK_ENTITY = BlockEntityType.Builder.create(StorageCubeBlockEntity::new, STORAGE_CUBE).build(null);

	// Block Tags
	public static final TagKey<Block> FLUID_TANK_BLOCK_TAG = TagKey.of(RegistryKeys.BLOCK, Identifier.of(StarflightMod.MOD_ID, "fluid_tank_blocks"));
	public static final TagKey<Block> BALLOON_BLOCK_TAG = TagKey.of(RegistryKeys.BLOCK, Identifier.of(StarflightMod.MOD_ID, "balloon_blocks"));
	public static final TagKey<Block> EDGE_CASE_TAG = TagKey.of(RegistryKeys.BLOCK, Identifier.of(StarflightMod.MOD_ID, "edge_case_blocks"));
	public static final TagKey<Block> INSTANT_REMOVE_TAG = TagKey.of(RegistryKeys.BLOCK, Identifier.of(StarflightMod.MOD_ID, "instant_remove_blocks"));
	public static final TagKey<Block> WORLD_STONE_BLOCK_TAG = TagKey.of(RegistryKeys.BLOCK, Identifier.of(StarflightMod.MOD_ID, "world_stone_blocks"));

	public static void initializeBlocks()
	{
		initializeBlock(ALUMINUM_BLOCK, "aluminum_block");
		initializeBlock(STRUCTURAL_ALUMINUM, "structural_aluminum");
		initializeBlock(RIVETED_ALUMINUM, "riveted_aluminum");
		initializeBlock(DAMAGED_ALUMINUM, "damaged_aluminum", false, List.of(), List.of());
		initializeBlock(STRUCTURAL_ALUMINUM_STAIRS, "structural_aluminum_stairs");
		initializeBlock(STRUCTURAL_ALUMINUM_SLAB, "structural_aluminum_slab");
		initializeBlock(ALUMINUM_WALL, "aluminum_wall");
		initializeBlock(ALUMINUM_FRAME, "aluminum_frame");
		initializeBlock(WALKWAY, "walkway");
		initializeBlock(BAUXITE_BLOCK, "bauxite_block");
		initializeBlock(BAUXITE_ORE, "bauxite_ore");
		initializeBlock(DEEPSLATE_BAUXITE_ORE, "deepslate_bauxite_ore");
		initializeBlock(REINFORCED_FABRIC, "reinforced_fabric");
		initializeBlock(IRON_FRAME, "iron_frame");
		initializeBlock(STRUCTURAL_IRON, "structural_iron");
		initializeBlock(STRUCTURAL_IRON_STAIRS, "structural_iron_stairs");
		initializeBlock(STRUCTURAL_IRON_SLAB, "structural_iron_slab");
		initializeBlock(STRUCTURAL_IRON_WALL, "structural_iron_wall");
		initializeBlock(IRON_ROUND_DECO, "iron_round_deco");
		initializeBlock(IRON_BULB, "iron_bulb");
		initializeBlock(BUFFER, "buffer");
		initializeBlock(IRON_MACHINE_CASING, "iron_machine_casing");
		initializeBlock(TITANIUM_BLOCK, "titanium_block");
		initializeBlock(TITANIUM_FRAME, "titanium_frame");
		initializeBlock(STRUCTURAL_TITANIUM, "structural_titanium");
		initializeBlock(DAMAGED_STRUCTURAL_TITANIUM, "damaged_structural_titanium", false, List.of(), List.of());
		initializeBlock(STRUCTURAL_TITANIUM_STAIRS, "structural_titanium_stairs");
		initializeBlock(STRUCTURAL_TITANIUM_SLAB, "structural_titanium_slab");
		initializeBlock(STRUCTURAL_TITANIUM_WALL, "structural_titanium_wall");
		initializeBlock(TITANIUM_MACHINE_CASING, "titanium_machine_casing");
		initializeBlock(TITANIUM_GLASS, "titanium_glass");
		initializeBlock(ILMENITE_BLOCK, "ilmenite_block");
		initializeBlock(ILMENITE_ORE, "ilmenite_ore");
		initializeBlock(DEEPSLATE_ILMENITE_ORE, "deepslate_ilmenite_ore");
		initializeBlock(SULFUR_BLOCK, "sulfur_block");
		initializeBlock(SULFUR_ORE, "sulfur_ore");
		initializeBlock(DEEPSLATE_SULFUR_ORE, "deepslate_sulfur_ore");
		initializeBlock(BASALT_SULFUR_ORE, "basalt_sulfur_ore");
		initializeBlock(RUBBER_LOG, "rubber_log");
		initializeBlock(SAPPY_RUBBER_LOG, "sappy_rubber_log", false, List.of(), List.of());
		initializeBlock(STRIPPED_RUBBER_LOG, "stripped_rubber_log", false, List.of(), List.of());
		initializeBlock(RUBBER_LEAVES, "rubber_leaves");
		initializeBlock(RUBBER_SAPLING, "rubber_sapling");
		initializeBlock(ICICLE, "icicle");
		initializeBlock(CHEESE_BLOCK, "cheese_block");
		initializeBlock(REGOLITH, "regolith");
		initializeBlock(BALSALTIC_REGOLITH, "balsaltic_regolith");
		initializeBlock(ICY_REGOLITH, "icy_regolith");
		initializeBlock(LUNALIGHT_BLOCK, "lunalight_block");
		initializeBlock(LUNALIGHT_ORE, "lunalight_ore");
		initializeBlock(DEEPSLATE_LUNALIGHT_ORE, "deepslate_lunalight_ore");
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
		initializeBlock(REDSTONE_GLASS, "redstone_glass", false, List.of(), List.of());
		initializeBlock(DRY_SNOW_BLOCK, "dry_snow_block");
		initializeBlock(ARES_MOSS_CARPET, "mars_moss_carpet");
		initializeBlock(ARES_MOSS_BLOCK, "mars_moss_block");
		initializeBlock(LYCOPHYTE_TOP, "lycophyte_top");
		initializeBlock(LYCOPHYTE_STEM, "lycophyte_stem", false, List.of(), List.of());
		initializeBlock(REDSTONE_BLOSSOM, "redstone_blossom");
		initializeBlock(MARS_ROOTS, "mars_roots");
		initializeBlock(REDSTONE_CLUSTER, "redstone_cluster");
		initializeBlock(DENSE_CLOUD, "dense_cloud");
		initializeBlock(VOLCANIC_SLATE, "volcanic_slate");
		initializeBlock(VOLCANIC_VENT, "volcanic_vent");
		initializeBlock(AEROPLANKTON, "aeroplankton");
		initializeBlock(RED_AEROPLANKTON, "red_aeroplankton");
		initializeBlock(PITCH_BLACK, "pitch_black", false, List.of(), List.of());
		initializeBlock(SOLARIZED_REGOLITH, "solarized_regolith");
		initializeBlock(SEARING_REGOLITH, "searing_regolith");
		initializeBlock(DIAMOND_REGOLITH, "diamond_regolith");
		initializeBlock(FRIGID_STONE, "frigid_stone");
		initializeBlock(TREE_TAP, "tree_tap");
		initializeBlock(IRON_LADDER, "iron_ladder");
		initializeBlock(PLANETARIUM, "planetarium");
		initializeBlock(ALUMINUM_SHELF, "aluminum_shelf");
		initializeBlock(METAL_CRAFTING_TABLE, "metal_crafting_table");
		initializeBlock(STORAGE_CUBE, "storage_cube");
		initializeBlock(STIRLING_ENGINE, "stirling_engine");
		initializeBlock(ELECTRIC_FURNACE, "electric_furnace");
		initializeBlock(PUMP, "pump");
		initializeBlock(ELECTROLYZER, "electrolyzer");
		initializeBlock(EXTRACTOR, "extractor");
		initializeBlock(VACUUM_FURNACE, "vacuum_furnace");
		initializeBlock(METAL_FABRICATOR, "metal_fabricator");
		initializeBlock(ATMOSPHERE_GENERATOR, "atmosphere_generator");
		initializeBlock(SOLAR_PANEL, "solar_panel");
		initializeBlock(BATTERY, "battery");
		initializeBlock(LIGHT_COLUMN, "light_column");
		initializeBlock(LUNAR_LAMP, "lunar_lamp");
		initializeBlock(BREAKER_SWITCH, "breaker_switch", true, List.of(), List.of(Text.translatable("block.space.breaker_switch.description")));
		initializeBlock(WATER_TANK, "water_tank");
		initializeBlock(OXYGEN_TANK, "oxygen_tank");
		initializeBlock(HYDROGEN_TANK, "hydrogen_tank");
		initializeBlock(BALLOON_CONTROLLER, "balloon_controller");
		initializeBlock(VALVE, "valve");
		initializeBlock(COPPER_CABLE_AL, "copper_cable_al");
		initializeBlock(WATER_PIPE_AL, "water_pipe_al");
		initializeBlock(OXYGEN_PIPE_AL, "oxygen_pipe_al");
		initializeBlock(HYDROGEN_PIPE_AL, "hydrogen_pipe_al");
		initializeBlock(OXYGEN_DISPENSER, "oxygen_dispenser");
		initializeBlock(VENT, "vent");
		initializeBlock(OXYGEN_SENSOR, "oxygen_sensor", true, List.of(), List.of(Text.translatable("block.space.oxygen_sensor.description_1"), Text.translatable("block.space.oxygen_sensor.description_2")));
		initializeBlock(AIRWAY, "airway");
		initializeBlock(AIRLOCK_DOOR, "airlock_door");
		initializeBlock(AIRLOCK_TRAPDOOR, "airlock_trapdoor");
		initializeBlock(TITANIUM_DOOR, "titanium_door");
		initializeBlock(LEVER_BLOCK, "lever_block");
		initializeBlock(POINTER_COLUMN, "pointer_column");
		initializeBlock(LINEAR_ACTUATOR, "linear_actuator");
		initializeBlock(LINEAR_TRACK, "linear_track");
		initializeBlock(CALL_TRACK, "call_track");
		initializeBlock(AIRSHIP_CONTROLLER, "airship_controller");
		initializeBlock(AIRSHIP_MOTOR, "airship_motor");
		initializeBlock(REACTION_WHEEL, "reaction_wheel");
		initializeBlock(RCS_BLOCK, "rcs_block");
		initializeBlock(LANDING_LEG, "landing_leg", true, List.of(), List.of(Text.translatable("block.space.landing_leg.description")));
		initializeBlock(ROCKET_CONTROLLER, "rocket_controller");
		initializeBlock(TARGETING_COMPUTER, "targeting_computer");
		initializeBlock(ENGINE_0, "engine_0");
		initializeBlock(ENGINE_1, "engine_1");
		initializeBlock(ENGINE_1_GIMBAL, "engine_1_gimbal");
		initializeBlock(ENGINE_1_VACUUM, "engine_1_vacuum");
		initializeBlock(COPPER_CABLE, "copper_cable");
		initializeBlock(WATER_PIPE, "water_pipe");
		initializeBlock(OXYGEN_PIPE, "oxygen_pipe");
		initializeBlock(HYDROGEN_PIPE, "hydrogen_pipe");
		initializeBlock(FLUID_TANK_INSIDE, "fluid_tank_inside", false, List.of(), List.of());
		initializeBlock(HABITABLE_AIR, "habitable_air", false, List.of(), List.of());
		initializeBlock(LEAK, "leak", false, List.of(), List.of());
		
		Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(StarflightMod.MOD_ID, "stirling_engine"), STIRLING_ENGINE_BLOCK_ENTITY);
		Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(StarflightMod.MOD_ID, "electric_furnace"), ELECTRIC_FURNACE_BLOCK_ENTITY);
		Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(StarflightMod.MOD_ID, "solar_panel"), SOLAR_PANEL_BLOCK_ENTITY);
		Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(StarflightMod.MOD_ID, "battery"), BATTERY_BLOCK_ENTITY);
		Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(StarflightMod.MOD_ID, "electric_light"), ELECTRIC_LIGHT_BLOCK_ENTITY);
		Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(StarflightMod.MOD_ID, "fluid_pipe"), FLUID_PIPE_BLOCK_ENTITY);
		Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(StarflightMod.MOD_ID, "fluid_tank_controller"), FLUID_TANK_CONTROLLER_BLOCK_ENTITY);
		Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(StarflightMod.MOD_ID, "balloon_controller"), BALLOON_CONTROLLER_BLOCK_ENTITY);
		Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(StarflightMod.MOD_ID, "valve"), VALVE_BLOCK_ENTITY);
		Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(StarflightMod.MOD_ID, "vent"), VENT_BLOCK_ENTITY);
		Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(StarflightMod.MOD_ID, "water_tank"), WATER_TANK_BLOCK_ENTITY);
		Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(StarflightMod.MOD_ID, "pump"), PUMP_BLOCK_ENTITY);
		Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(StarflightMod.MOD_ID, "extractor"), EXTRACTOR_BLOCK_ENTITY);
		Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(StarflightMod.MOD_ID, "electrolyzer"), ELECTROLYZER_BLOCK_ENTITY);
		Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(StarflightMod.MOD_ID, "vacuum_furnace"), VACUUM_FURNACE_BLOCK_ENTITY);
		Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(StarflightMod.MOD_ID, "metal_fabricator"), METAL_FABRICATOR_BLOCK_ENTITY);
		Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(StarflightMod.MOD_ID, "rocket_controller"), ROCKET_CONTROLLER_BLOCK_ENTITY);
		Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(StarflightMod.MOD_ID, "targeting_computer"), TARGETING_COMPUTER_BLOCK_ENTITY);
		Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(StarflightMod.MOD_ID, "atmosphere_generator"), ATMOSPHERE_GENERATOR_BLOCK_ENTITY);
		Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(StarflightMod.MOD_ID, "leak"), LEAK_BLOCK_ENTITY);
		Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(StarflightMod.MOD_ID, "volcanic_vent"), VOLCANIC_VENT_BLOCK_ENTITY);
		Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(StarflightMod.MOD_ID, "storage_cube"), STORAGE_CUBE_BLOCK_ENTITY);
		
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

	private static void initializeBlock(Block block, String name, boolean creativeInventory, List<Text> textList, List<Text> hiddenTextList)
	{
		String mod_id = StarflightMod.MOD_ID;
		Registry.register(Registries.BLOCK, Identifier.of(mod_id, name), block);

		if(textList.isEmpty() && hiddenTextList.isEmpty())
			Registry.register(Registries.ITEM, Identifier.of(mod_id, name), new BlockItem(block, new Settings()));
		else
			Registry.register(Registries.ITEM, Identifier.of(mod_id, name), new DescriptiveBlockItem(block, new Settings(), textList, hiddenTextList));

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
		return new PillarBlock(AbstractBlock.Settings.create().mapColor(state -> state.get(PillarBlock.AXIS) == Direction.Axis.Y ? topMapColor : sideMapColor).instrument(NoteBlockInstrument.BASS).strength(2.0f).sounds(BlockSoundGroup.WOOD).burnable());
	}

	private static LeavesBlock createLeavesBlock(BlockSoundGroup soundGroup)
	{
		return new LeavesBlock(AbstractBlock.Settings.create().mapColor(MapColor.DARK_GREEN).strength(0.2f).ticksRandomly().sounds(soundGroup).nonOpaque().allowsSpawning(StarflightBlocks::canSpawnOnLeaves).suffocates(StarflightBlocks::never).blockVision(StarflightBlocks::never).burnable().pistonBehavior(PistonBehavior.DESTROY).solidBlock(StarflightBlocks::never));
	}

	private static Boolean canSpawnOnLeaves(BlockState state, BlockView world, BlockPos pos, EntityType<?> type)
	{
		return type == EntityType.OCELOT || type == EntityType.PARROT;
	}

	private static boolean never(BlockState state, BlockView world, BlockPos pos)
	{
		return false;
	}
	
	@Nullable
    public static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> validateTicker(BlockEntityType<A> givenType, BlockEntityType<E> expectedType, @Nullable BlockEntityTicker<A> ticker)
	{
        return expectedType == givenType ? ticker : null;
    }
}