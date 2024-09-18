package space.item;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.component.ComponentType;
import net.minecraft.component.type.FoodComponents;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.AxeItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
import net.minecraft.item.Item.Settings;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.Util;
import net.minecraft.util.dynamic.Codecs;
import space.StarflightMod;
import space.block.StarflightBlocks;
import space.entity.StarflightEntities;
import space.util.FluidResourceType;

public class StarflightItems
{
	// Item Group
	public static final RegistryKey<ItemGroup> ITEM_GROUP = RegistryKey.of(RegistryKeys.ITEM_GROUP, Identifier.of(StarflightMod.MOD_ID, "general"));
	
	// Item Components
	public static final ComponentType<Float> ENERGY = ComponentType.<Float>builder().codec(Codecs.POSITIVE_FLOAT).packetCodec(PacketCodecs.FLOAT).build();
	public static final ComponentType<Float> MAX_ENERGY = ComponentType.<Float>builder().codec(Codecs.POSITIVE_FLOAT).packetCodec(PacketCodecs.FLOAT).build();
	public static final ComponentType<Float> OXYGEN = ComponentType.<Float>builder().codec(Codecs.POSITIVE_FLOAT).packetCodec(PacketCodecs.FLOAT).build();
	public static final ComponentType<Float> MAX_OXYGEN = ComponentType.<Float>builder().codec(Codecs.POSITIVE_FLOAT).packetCodec(PacketCodecs.FLOAT).build();
	public static final ComponentType<String> PART_DRAWING_GROUPS = ComponentType.<String>builder().codec(Codecs.NON_EMPTY_STRING).packetCodec(PacketCodecs.STRING).build();
	public static final ComponentType<String> PLANET_NAME = ComponentType.<String>builder().codec(Codecs.NON_EMPTY_STRING).packetCodec(PacketCodecs.STRING).build();
	public static final ComponentType<Integer> PRIMARY_COLOR = ComponentType.<Integer>builder().codec(Codecs.NONNEGATIVE_INT).packetCodec(PacketCodecs.VAR_INT).build();
	public static final ComponentType<Integer> SECONDARY_COLOR = ComponentType.<Integer>builder().codec(Codecs.NONNEGATIVE_INT).packetCodec(PacketCodecs.VAR_INT).build();
	
	// Armor Material
	public static final RegistryEntry<ArmorMaterial> SPACE_SUIT_ARMOR_MATERIAL = registerArmorMaterial("space_suit", Util.make(new EnumMap<ArmorItem.Type, Integer>(ArmorItem.Type.class), map -> {
        map.put(ArmorItem.Type.BOOTS, 1);
        map.put(ArmorItem.Type.LEGGINGS, 2);
        map.put(ArmorItem.Type.CHESTPLATE, 3);
        map.put(ArmorItem.Type.HELMET, 1);
        map.put(ArmorItem.Type.BODY, 3);
    }), 15, SoundEvents.ITEM_ARMOR_EQUIP_CHAIN, 0.0f, 0.0f, () -> Ingredient.ofItems(StarflightBlocks.REINFORCED_FABRIC), List.of(new ArmorMaterial.Layer(Identifier.ofVanilla("space_suit"), "", true), new ArmorMaterial.Layer(Identifier.ofVanilla("space_suit"), "_overlay", false)));
	
	public static final RegistryEntry<ArmorMaterial> THERMAL_ARMOR_MATERIAL = registerArmorMaterial("thermal", Util.make(new EnumMap<ArmorItem.Type, Integer>(ArmorItem.Type.class), map -> {
		map.put(ArmorItem.Type.BOOTS, 2);
		map.put(ArmorItem.Type.LEGGINGS, 5);
		map.put(ArmorItem.Type.CHESTPLATE, 6);
		map.put(ArmorItem.Type.HELMET, 2);
		map.put(ArmorItem.Type.BODY, 5);
    }), 15, SoundEvents.ITEM_ARMOR_EQUIP_CHAIN, 0.0f, 0.0f, () -> Ingredient.ofItems(StarflightItems.AEROGEL), List.of(new ArmorMaterial.Layer(Identifier.ofVanilla("thermal"), "", false)));
	
	// Items
	public static final Item ALUMINUM_INGOT = new Item(new Settings());
	public static final Item BAUXITE = new Item(new Settings());
	public static final Item TITANIUM_INGOT = new Item(new Settings());
	public static final Item ILMENITE = new Item(new Settings());
	public static final Item SULFUR = new Item(new Settings());
	public static final Item LUNALIGHT = new Item(new Settings());
	public static final Item HEMATITE = new Item(new Settings());
	public static final Item RUBBER_SAP = new Item(new Settings());
	public static final Item RUBBER_RESIN = new Item(new Settings());
	public static final Item RUBBER = new Item(new Settings());
	public static final Item AEROGEL = new Item(new Settings());
	public static final Item CHEESE = new Item(new Settings().food(FoodComponents.APPLE));
	public static final Item IRON_PLATE = new Item(new Settings());
	public static final Item ALUMINUM_PLATE = new Item(new Settings());
	public static final Item TITANIUM_PLATE = new Item(new Settings());
	public static final Item ALUMINUM_SHAFT = new Item(new Settings());
	public static final Item COIL = new Item(new Settings());
	public static final Item ELECTRIC_MOTOR = new Item(new Settings());
	public static final Item SUBSTRATE = new Item(new Settings());
	public static final Item CIRCUIT = new Item(new Settings());
	public static final Item SOLAR_CELL = new Item(new Settings());
	public static final Item HEAVY_CYLINDER = new Item(new Settings());
	public static final Item MINI_THRUSTER = new Item(new Settings());
	public static final Item COOLED_COPPER = new Item(new Settings());
	public static final Item COPPER_INJECTOR = new Item(new Settings());
	public static final Item IMPELLER = new Item(new Settings());
	public static final Item TURBO_PUMP = new Item(new Settings());
	public static final Item ENGINE_1_ASSEMBLY = new Item(new Settings());
	public static final Item ENGINE_1_NOZZLE = new Item(new Settings());
	public static final Item ENGINE_1_EXTENSION = new Item(new Settings());
	public static final Item GUIDE_BOOK = new GuideBookItem(new Settings().maxCount(1));
	public static final Item BATTERY_CELL = new BatteryCellItem(new Settings().maxCount(1).component(ENERGY, 0.0f).component(MAX_ENERGY, 2048.0f));
	public static final Item OXYGEN_TANK_ITEM = new OxygenTankItem(new Settings().maxCount(1).component(OXYGEN, 0.0f).component(MAX_OXYGEN, 2.0f));
	public static final Item PART_DRAWINGS = new PartDrawingsItem(new Settings().maxCount(1).rarity(Rarity.RARE).component(PART_DRAWING_GROUPS, ""));
	public static final Item PLANETARIUM_CARD = new PlanetariumCardItem(new Settings().maxCount(1).rarity(Rarity.EPIC).component(PLANET_NAME, "").component(PRIMARY_COLOR, -1).component(SECONDARY_COLOR, -1));
	public static final Item MULTIMETER = new MultimeterItem(new Settings().maxCount(1));
	public static final Item WRENCH = new WrenchItem(new Settings().maxCount(1));
	public static final Item DIAMOND_END_MILL = new Item(new Settings().maxCount(1).maxDamage(ToolMaterials.DIAMOND.getDurability() * 2));
	public static final Item TITANIUM_SWORD = new SwordItem(StarflightToolMaterials.TITANIUM, new Item.Settings().attributeModifiers(SwordItem.createAttributeModifiers(StarflightToolMaterials.TITANIUM, 3, -2.4F)));
    public static final Item TITANIUM_SHOVEL = new ShovelItem(StarflightToolMaterials.TITANIUM, new Item.Settings().attributeModifiers(ShovelItem.createAttributeModifiers(StarflightToolMaterials.TITANIUM, 1.5F, -3.0F)));
    public static final Item TITANIUM_PICKAXE = new PickaxeItem(StarflightToolMaterials.TITANIUM, new Item.Settings().attributeModifiers(PickaxeItem.createAttributeModifiers(StarflightToolMaterials.TITANIUM, 1.0F, -2.8F)));
    public static final Item TITANIUM_AXE = new AxeItem(StarflightToolMaterials.TITANIUM, new Item.Settings().attributeModifiers(AxeItem.createAttributeModifiers(StarflightToolMaterials.TITANIUM, 6.0F, -3.1F)));
    public static final Item TITANIUM_HOE = new HoeItem(StarflightToolMaterials.TITANIUM, new Item.Settings().attributeModifiers(HoeItem.createAttributeModifiers(StarflightToolMaterials.TITANIUM, -2.0F, -1.0F)));
    public static final Item HEMATITE_SWORD = new SwordItem(StarflightToolMaterials.HEMATITE, new Item.Settings().attributeModifiers(SwordItem.createAttributeModifiers(StarflightToolMaterials.HEMATITE, 3, -2.4F)));
    public static final Item HEMATITE_SHOVEL = new ShovelItem(StarflightToolMaterials.HEMATITE, new Item.Settings().attributeModifiers(ShovelItem.createAttributeModifiers(StarflightToolMaterials.HEMATITE, 1.5F, -3.0F)));
    public static final Item HEMATITE_PICKAXE = new PickaxeItem(StarflightToolMaterials.HEMATITE, new Item.Settings().attributeModifiers(PickaxeItem.createAttributeModifiers(StarflightToolMaterials.HEMATITE, 1.0F, -2.8F)));
    public static final Item HEMATITE_AXE = new AxeItem(StarflightToolMaterials.HEMATITE, new Item.Settings().attributeModifiers(AxeItem.createAttributeModifiers(StarflightToolMaterials.HEMATITE, 5.0F, -3.0F)));
    public static final Item HEMATITE_HOE = new HoeItem(StarflightToolMaterials.HEMATITE, new Item.Settings().attributeModifiers(HoeItem.createAttributeModifiers(StarflightToolMaterials.HEMATITE, -3.0F, 0.0F)));
	public static final Item OXYGEN_LOADER = new LoaderItem(new Settings().maxCount(1), FluidResourceType.OXYGEN);
	public static final Item HYDROGEN_LOADER = new LoaderItem(new Settings().maxCount(1), FluidResourceType.HYDROGEN);
	public static final Item DIVIDER = new DividerItem(new Settings().maxCount(1));
	public static final Item WAND = new MovingCraftWandItem(new Settings());
	
	// Armor Items
    public static final Item SPACE_SUIT_HELMET = new SpaceSuitItem(SPACE_SUIT_ARMOR_MATERIAL, ArmorItem.Type.HELMET, new Item.Settings().maxDamage(ArmorItem.Type.HELMET.getMaxDamage(15)));
    public static final Item SPACE_SUIT_CHESTPLATE = new SpaceSuitItem(SPACE_SUIT_ARMOR_MATERIAL, ArmorItem.Type.CHESTPLATE, new Item.Settings().maxDamage(ArmorItem.Type.CHESTPLATE.getMaxDamage(15)).component(OXYGEN, 0.0f).component(MAX_OXYGEN, 4.0f));
    public static final Item SPACE_SUIT_LEGGINGS = new SpaceSuitItem(SPACE_SUIT_ARMOR_MATERIAL, ArmorItem.Type.LEGGINGS, new Item.Settings().maxDamage(ArmorItem.Type.LEGGINGS.getMaxDamage(15)));
    public static final Item SPACE_SUIT_BOOTS = new SpaceSuitItem(SPACE_SUIT_ARMOR_MATERIAL, ArmorItem.Type.BOOTS, new Item.Settings().maxDamage(ArmorItem.Type.BOOTS.getMaxDamage(15)));
    public static final Item THERMAL_BOOTS = new SpaceSuitItem(THERMAL_ARMOR_MATERIAL, ArmorItem.Type.BOOTS, new Item.Settings().maxDamage(ArmorItem.Type.BOOTS.getMaxDamage(15)));
    
    // Structure Placer Items
    public static final Item ROCKET_1 = new StructurePlacerItem(new Settings().maxCount(1), 7, 20, 7, "rocket_1");
    
    // Spawn Egg Items
    public static final Item CERULEAN_SPAWN_EGG = new SpawnEggItem(StarflightEntities.CERULEAN, 0x1485AD, 0x000000, new Item.Settings());
	public static final Item DUST_SPAWN_EGG = new SpawnEggItem(StarflightEntities.DUST, 0xBE673F, 0x82342B, new Item.Settings());
	public static final Item ANCIENT_HUMANOID_SPAWN_EGG = new SpawnEggItem(StarflightEntities.ANCIENT_HUMANOID, 0xF8F9F9, 0x154360, new Item.Settings());
	public static final Item SOLAR_SPECTRE_SPAWN_EGG = new SpawnEggItem(StarflightEntities.SOLAR_SPECTRE, 0xE9F3FD, 0xC8E2F9, new Item.Settings());
	public static final Item STRATOFISH_SPAWN_EGG = new SpawnEggItem(StarflightEntities.STRATOFISH, 0xFFE8FA, 0x7B6D9E, new Item.Settings());
	public static final Item CLOUD_SHARK_SPAWN_EGG = new SpawnEggItem(StarflightEntities.CLOUD_SHARK, 0xBFE7F7, 0xECF8FD, new Item.Settings());
	
	// Item Tags
	public static final TagKey<Item> SPACE_SUIT_ARMOR_ITEM_TAG = TagKey.of(RegistryKeys.ITEM, Identifier.of(StarflightMod.MOD_ID, "space_suit_armor"));
	public static final TagKey<Item> MAGNETIC_TOOL_ITEM_TAG = TagKey.of(RegistryKeys.ITEM, Identifier.of(StarflightMod.MOD_ID, "magnetic_tool"));
	public static final TagKey<Item> NO_OXYGEN_FUEL_ITEM_TAG = TagKey.of(RegistryKeys.ITEM, Identifier.of(StarflightMod.MOD_ID, "no_oxygen_fuel"));
	public static final TagKey<Item> COMBUSTION_ITEM_TAG = TagKey.of(RegistryKeys.ITEM, Identifier.of(StarflightMod.MOD_ID, "combustion"));
    
	// Track the state of the hidden tooltip key.
	public static boolean tooltipKey = false;
	
	public static void initializeItems()
	{
		// Item Group
		Registry.register(Registries.ITEM_GROUP, ITEM_GROUP, FabricItemGroup.builder().icon(() -> new ItemStack(StarflightBlocks.PLANETARIUM)).displayName(Text.translatable("itemGroup.space.general")).build());
		
		// Item Components
		Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(StarflightMod.MOD_ID, "energy"), ENERGY);
		Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(StarflightMod.MOD_ID, "max_energy"), MAX_ENERGY);
		Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(StarflightMod.MOD_ID, "oxygen"), OXYGEN);
		Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(StarflightMod.MOD_ID, "max_oxygen"), MAX_OXYGEN);
		Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(StarflightMod.MOD_ID, "part_drawing_groups"), PART_DRAWING_GROUPS);
		Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(StarflightMod.MOD_ID, "planet_name"), PLANET_NAME);
		Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(StarflightMod.MOD_ID, "primary_color"), PRIMARY_COLOR);
		Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(StarflightMod.MOD_ID, "secondary_color"), SECONDARY_COLOR);
		
		// Items
		registerItem(ALUMINUM_INGOT, "aluminum_ingot");
		registerItem(BAUXITE, "bauxite");
		registerItem(TITANIUM_INGOT, "titanium_ingot");
		registerItem(ILMENITE, "ilmenite");
		registerItem(SULFUR, "sulfur");
		registerItem(LUNALIGHT, "lunalight");
		registerItem(HEMATITE, "hematite");
		registerItem(RUBBER_SAP, "rubber_sap");
		registerItem(RUBBER_RESIN, "rubber_resin");
		registerItem(RUBBER, "rubber");
		registerItem(AEROGEL, "aerogel");
		registerItem(CHEESE, "cheese");
		registerItem(IRON_PLATE, "iron_plate");
		registerItem(ALUMINUM_PLATE, "aluminum_plate");
		registerItem(TITANIUM_PLATE, "titanium_plate");
		registerItem(ALUMINUM_SHAFT, "aluminum_shaft");
		registerItem(COIL, "coil");
		registerItem(ELECTRIC_MOTOR, "electric_motor");
		registerItem(SUBSTRATE, "substrate");
		registerItem(CIRCUIT, "circuit");
		registerItem(SOLAR_CELL, "solar_cell");
		registerItem(HEAVY_CYLINDER, "heavy_cylinder");
		registerItem(MINI_THRUSTER, "mini_thruster");
		registerItem(COOLED_COPPER, "cooled_copper");
		registerItem(COPPER_INJECTOR, "copper_injector");
		registerItem(IMPELLER, "impeller");
		registerItem(TURBO_PUMP, "turbo_pump");
		registerItem(ENGINE_1_ASSEMBLY, "engine_1_assembly");
		registerItem(ENGINE_1_NOZZLE, "engine_1_nozzle");
		registerItem(ENGINE_1_EXTENSION, "engine_1_extension");
		registerItemHidden(GUIDE_BOOK, "guide_book");
		registerItem(BATTERY_CELL, "battery_cell");
		registerItem(OXYGEN_TANK_ITEM, "oxygen_tank_item");
		registerItem(MULTIMETER, "multimeter");
		registerItem(WRENCH, "wrench");
		registerItem(DIAMOND_END_MILL, "diamond_end_mill");
		registerItem(TITANIUM_SWORD, "titanium_sword");
		registerItem(TITANIUM_SHOVEL, "titanium_shovel");
		registerItem(TITANIUM_PICKAXE, "titanium_pickaxe");
		registerItem(TITANIUM_AXE, "titanium_axe");
		registerItem(TITANIUM_HOE, "titanium_hoe");
		registerItem(HEMATITE_SWORD, "hematite_sword");
		registerItem(HEMATITE_SHOVEL, "hematite_shovel");
		registerItem(HEMATITE_PICKAXE, "hematite_pickaxe");
		registerItem(HEMATITE_AXE, "hematite_axe");
		registerItem(HEMATITE_HOE, "hematite_hoe");
		registerItemHidden(WAND, "item_wand");
		
		// Armor Items
		registerItem(SPACE_SUIT_HELMET, "space_suit_helmet");
		registerItem(SPACE_SUIT_CHESTPLATE, "space_suit_chestplate");
		registerItem(SPACE_SUIT_LEGGINGS, "space_suit_leggings");
		registerItem(SPACE_SUIT_BOOTS, "space_suit_boots");
		registerItem(THERMAL_BOOTS, "thermal_boots");
		
		// Part Drawings
		registerItemHidden(PART_DRAWINGS, "part_drawings");
		enterPartDrawingsItem("item.space.cooled_copper,item.space.copper_injector,item.space.impeller,item.space.engine_1_nozzle,item.space.engine_1_extension");
		
		// Planetarium Cards
		registerItemHidden(PLANETARIUM_CARD, "planetarium_card");
		//enterPlanetariumCardItem("mercury", 0x6F6076, 0x605C75);
		//enterPlanetariumCardItem("venus", 0xFFF980, 0xFFAA00);
		//enterPlanetariumCardItem("mars", 0xBF6A41, 0xDC4D18);
		
		// Creative Items
		registerItem(OXYGEN_LOADER, "oxygen_loader");
		registerItem(HYDROGEN_LOADER, "hydrogen_loader");
		registerItem(DIVIDER, "divider");
		registerItem(ROCKET_1, "rocket_1");
		
		// Spawn Egg Items
		registerItem(CERULEAN_SPAWN_EGG, "cerulean_spawn_egg");
		registerItem(DUST_SPAWN_EGG, "dust_spawn_egg");
		registerItem(ANCIENT_HUMANOID_SPAWN_EGG, "ancient_humanoid_spawn_egg");
		registerItem(SOLAR_SPECTRE_SPAWN_EGG, "solar_spectre_spawn_egg");
		registerItem(STRATOFISH_SPAWN_EGG, "stratofish_spawn_egg");
		registerItem(CLOUD_SHARK_SPAWN_EGG, "cloud_shark_spawn_egg");
	}
	
	private static void registerItem(Item item, String name)
	{
		Registry.register(Registries.ITEM, Identifier.of(StarflightMod.MOD_ID, name), item);
		ItemGroupEvents.modifyEntriesEvent(ITEM_GROUP).register(content -> content.add(item));
	}
	
	private static void registerItemHidden(Item item, String name)
	{
		Registry.register(Registries.ITEM, Identifier.of(StarflightMod.MOD_ID, name), item);
	}
	
	private static void enterPartDrawingsItem(String parts)
	{
		ItemStack stack = new ItemStack(PART_DRAWINGS);
		stack.set(PART_DRAWING_GROUPS, parts);
		ItemGroupEvents.modifyEntriesEvent(ITEM_GROUP).register(content -> content.add(stack));
	}
	
	/*private static void enterPlanetariumCardItem(String name, int primaryColor, int secondaryColor)
	{
		ItemStack stack = new ItemStack(PLANETARIUM_CARD);
		stack.set(PLANET_NAME, name);
		stack.set(PRIMARY_COLOR, primaryColor);
		stack.set(SECONDARY_COLOR, secondaryColor);
		ItemGroupEvents.modifyEntriesEvent(ITEM_GROUP).register(content -> content.add(stack));
	}*/
	
	private static RegistryEntry<ArmorMaterial> registerArmorMaterial(String id, EnumMap<ArmorItem.Type, Integer> defense, int enchantability, RegistryEntry<SoundEvent> equipSound, float toughness, float knockbackResistance, Supplier<Ingredient> repairIngredient, List<ArmorMaterial.Layer> layers)
	{
		EnumMap<ArmorItem.Type, Integer> enumMap = new EnumMap<ArmorItem.Type, Integer>(ArmorItem.Type.class);
		
		for(ArmorItem.Type type : ArmorItem.Type.values())
			enumMap.put(type, defense.get(type));
		
		return Registry.registerReference(Registries.ARMOR_MATERIAL, Identifier.of(StarflightMod.MOD_ID, id), new ArmorMaterial(enumMap, enchantability, equipSound, repairIngredient, layers, toughness, knockbackResistance));
	}
	
	public static void hiddenItemTooltip(List<Text> tooltip, Text ... texts)
	{
		if(tooltipKey)
		{
			for(Text text : texts)
				tooltip.add(text);
		}
		else
			tooltip.add(Text.translatable("item.space.press_for_more").formatted(Formatting.ITALIC, Formatting.DARK_GRAY));
	}
	
	public static void hiddenItemTooltip(List<Text> tooltip, List<Text> texts)
	{
		if(tooltipKey)
		{
			for(Text text : texts)
				tooltip.add(text);
		}
		else
			tooltip.add(Text.translatable("item.space.press_for_more").formatted(Formatting.ITALIC, Formatting.DARK_GRAY));
	}
}