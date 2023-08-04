package space.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.AxeItem;
import net.minecraft.item.FoodComponents;
import net.minecraft.item.Item;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import space.StarflightMod;
import space.entity.StarflightEntities;
import space.materials.SpaceSuitArmorMaterial;

public class StarflightItems
{
	// Armor Material
	public static final ArmorMaterial SPACE_SUIT_ARMOR_MATERIAL = new SpaceSuitArmorMaterial();
	
	// Tool Material
	
	
	// Items
	public static final Item ALUMINUM_INGOT = new Item(new FabricItemSettings().group(StarflightMod.ITEM_GROUP));
	public static final Item BAUXITE = new Item(new FabricItemSettings().group(StarflightMod.ITEM_GROUP));
	public static final Item TITANIUM_INGOT = new Item(new FabricItemSettings().group(StarflightMod.ITEM_GROUP));
	public static final Item ILMENITE = new Item(new FabricItemSettings().group(StarflightMod.ITEM_GROUP));
	public static final Item SULFUR = new Item(new FabricItemSettings().group(StarflightMod.ITEM_GROUP));
	public static final Item HEMATITE = new Item(new FabricItemSettings().group(StarflightMod.ITEM_GROUP));
	public static final Item RUBBER = new Item(new FabricItemSettings().group(StarflightMod.ITEM_GROUP));
	public static final Item RUBBER_SAP = new Item(new FabricItemSettings().group(StarflightMod.ITEM_GROUP));
	public static final Item RUBBER_RESIN = new Item(new FabricItemSettings().group(StarflightMod.ITEM_GROUP));
	public static final Item CHEESE = new Item(new FabricItemSettings().food(FoodComponents.APPLE).group(StarflightMod.ITEM_GROUP));
	public static final Item ALUMINUM_SHAFT = new Item(new FabricItemSettings().group(StarflightMod.ITEM_GROUP));
	public static final Item TITANIUM_SWORD = new SwordItem(ToolMaterials.DIAMOND, 3, -2.4f, new Item.Settings().group(StarflightMod.ITEM_GROUP));
    public static final Item TITANIUM_SHOVEL = new ShovelItem(ToolMaterials.DIAMOND, 1.5f, -3.0f, new Item.Settings().group(StarflightMod.ITEM_GROUP));
    public static final Item TITANIUM_PICKAXE = new PickaxeItem(ToolMaterials.DIAMOND, 1, -2.8f, new Item.Settings().group(StarflightMod.ITEM_GROUP));
    public static final Item TITANIUM_AXE = new AxeItem(ToolMaterials.DIAMOND, 5.0f, -3.1f, new Item.Settings().group(StarflightMod.ITEM_GROUP));
    public static final Item TITANIUM_HOE = new CustomHoeItem(ToolMaterials.DIAMOND, -3, -1.0f, new Item.Settings().group(StarflightMod.ITEM_GROUP));
	public static final Item BATTERY_CELL = new BatteryCellItem(new FabricItemSettings().maxCount(1).group(StarflightMod.ITEM_GROUP), 2048.0);
	public static final Item OXYGEN_TANK_ITEM = new OxygenTankItem(new FabricItemSettings().maxCount(1).group(StarflightMod.ITEM_GROUP));
	public static final Item SUBSTRATE = new Item(new FabricItemSettings().group(StarflightMod.ITEM_GROUP));
	public static final Item CONTROL_UNIT = new Item(new FabricItemSettings().group(StarflightMod.ITEM_GROUP));
	public static final Item SOLAR_CELL = new Item(new FabricItemSettings().group(StarflightMod.ITEM_GROUP));
	public static final Item ROCKET_CORE_1 = new Item(new FabricItemSettings().group(StarflightMod.ITEM_GROUP));
	public static final Item ROCKET_CORE_2 = new Item(new FabricItemSettings().group(StarflightMod.ITEM_GROUP));
	public static final Item MULTIMETER = new MultimeterItem(new FabricItemSettings().maxCount(1).group(StarflightMod.ITEM_GROUP));
	public static final Item WRENCH = new WrenchItem(new FabricItemSettings().maxCount(1).group(StarflightMod.ITEM_GROUP));
	public static final Item NAVIGATION_CARD = new NavigationCardItem(new FabricItemSettings().maxCount(1).group(StarflightMod.ITEM_GROUP));
	public static final Item ARRIVAL_CARD = new ArrivalCardItem(new FabricItemSettings().maxCount(1).group(StarflightMod.ITEM_GROUP));
	public static final Item GUIDE_BOOK = new GuideBookItem(new FabricItemSettings().maxCount(1).group(StarflightMod.ITEM_GROUP));
	public static final Item OXYGEN_LOADER = new LoaderItem(new FabricItemSettings().maxCount(1).group(StarflightMod.ITEM_GROUP), "oxygen");
	public static final Item HYDROGEN_LOADER = new LoaderItem(new FabricItemSettings().maxCount(1).group(StarflightMod.ITEM_GROUP), "hydrogen");
	public static final Item DIVIDER = new DividerItem(new FabricItemSettings().maxCount(1).group(StarflightMod.ITEM_GROUP));
	public static final Item WAND = new MovingCraftWandItem(new FabricItemSettings());
	
	// Armor Items
    public static final Item SPACE_SUIT_HELMET = new SpaceSuitItem(SPACE_SUIT_ARMOR_MATERIAL, EquipmentSlot.HEAD, new Item.Settings().group(StarflightMod.ITEM_GROUP));
    public static final Item SPACE_SUIT_CHESTPLATE = new SpaceSuitItem(SPACE_SUIT_ARMOR_MATERIAL, EquipmentSlot.CHEST, new Item.Settings().group(StarflightMod.ITEM_GROUP));
    public static final Item SPACE_SUIT_LEGGINGS = new SpaceSuitItem(SPACE_SUIT_ARMOR_MATERIAL, EquipmentSlot.LEGS, new Item.Settings().group(StarflightMod.ITEM_GROUP));
    public static final Item SPACE_SUIT_BOOTS = new SpaceSuitItem(SPACE_SUIT_ARMOR_MATERIAL, EquipmentSlot.FEET, new Item.Settings().group(StarflightMod.ITEM_GROUP));
    
    // Structure Placer Items
    public static final Item ROCKET_1 = new StructurePlacerItem(new FabricItemSettings().maxCount(1).group(StarflightMod.ITEM_GROUP), 7, 20, 7, "rocket_1");
    
    // Spawn Egg Items
    public static final Item CERULEAN_SPAWN_EGG = new SpawnEggItem(StarflightEntities.CERULEAN, 0x1485AD, 0x000000, new Item.Settings().group(StarflightMod.ITEM_GROUP));
	public static final Item DUST_SPAWN_EGG = new SpawnEggItem(StarflightEntities.DUST, 0xBE673F, 0x82342B, new Item.Settings().group(StarflightMod.ITEM_GROUP));
	public static final Item ANCIENT_HUMANOID_SPAWN_EGG = new SpawnEggItem(StarflightEntities.ANCIENT_HUMANOID, 0xF8F9F9, 0x154360, new Item.Settings().group(StarflightMod.ITEM_GROUP));
	public static final Item SOLAR_SPECTRE_SPAWN_EGG = new SpawnEggItem(StarflightEntities.SOLAR_SPECTRE, 0xE9F3FD, 0xC8E2F9, new Item.Settings().group(StarflightMod.ITEM_GROUP));
	
	// Item Tags
	public static final TagKey<Item> NO_OXYGEN_FUEL_ITEM_TAG = TagKey.of(Registry.ITEM_KEY, new Identifier(StarflightMod.MOD_ID, "no_oxygen_fuel"));
	public static final TagKey<Item> COMBUSTION_ITEM_TAG = TagKey.of(Registry.ITEM_KEY, new Identifier(StarflightMod.MOD_ID, "combustion"));
    
	public static void initializeItems()
	{
		String mod_id = StarflightMod.MOD_ID;
		
		// Items
		Registry.register(Registry.ITEM, new Identifier(mod_id, "aluminum_ingot"), ALUMINUM_INGOT);
		Registry.register(Registry.ITEM, new Identifier(mod_id, "bauxite"), BAUXITE);
		Registry.register(Registry.ITEM, new Identifier(mod_id, "titanium_ingot"), TITANIUM_INGOT);
		Registry.register(Registry.ITEM, new Identifier(mod_id, "ilmenite"), ILMENITE);
		Registry.register(Registry.ITEM, new Identifier(mod_id, "sulfur"), SULFUR);
		Registry.register(Registry.ITEM, new Identifier(mod_id, "hematite"), HEMATITE);
		Registry.register(Registry.ITEM, new Identifier(mod_id, "rubber"), RUBBER);
		Registry.register(Registry.ITEM, new Identifier(mod_id, "rubber_sap"), RUBBER_SAP);
		Registry.register(Registry.ITEM, new Identifier(mod_id, "rubber_resin"), RUBBER_RESIN);
		Registry.register(Registry.ITEM, new Identifier(mod_id, "cheese"), CHEESE);
		Registry.register(Registry.ITEM, new Identifier(mod_id, "aluminum_shaft"), ALUMINUM_SHAFT);
		Registry.register(Registry.ITEM, new Identifier(mod_id, "titanium_sword"), TITANIUM_SWORD);
		Registry.register(Registry.ITEM, new Identifier(mod_id, "titanium_shovel"), TITANIUM_SHOVEL);
		Registry.register(Registry.ITEM, new Identifier(mod_id, "titanium_pickaxe"), TITANIUM_PICKAXE);
		Registry.register(Registry.ITEM, new Identifier(mod_id, "titanium_axe"), TITANIUM_AXE);
		Registry.register(Registry.ITEM, new Identifier(mod_id, "titanium_hoe"), TITANIUM_HOE);
		Registry.register(Registry.ITEM, new Identifier(mod_id, "battery_cell"), BATTERY_CELL);
		Registry.register(Registry.ITEM, new Identifier(mod_id, "oxygen_tank_item"), OXYGEN_TANK_ITEM);
		Registry.register(Registry.ITEM, new Identifier(mod_id, "substrate"), SUBSTRATE);
		Registry.register(Registry.ITEM, new Identifier(mod_id, "control_unit"), CONTROL_UNIT);
		Registry.register(Registry.ITEM, new Identifier(mod_id, "solar_cell"), SOLAR_CELL);
		Registry.register(Registry.ITEM, new Identifier(mod_id, "rocket_core_1"), ROCKET_CORE_1);
		Registry.register(Registry.ITEM, new Identifier(mod_id, "rocket_core_2"), ROCKET_CORE_2);
		Registry.register(Registry.ITEM, new Identifier(mod_id, "multimeter"), MULTIMETER);
		Registry.register(Registry.ITEM, new Identifier(mod_id, "wrench"), WRENCH);
		Registry.register(Registry.ITEM, new Identifier(mod_id, "navigation_card"), NAVIGATION_CARD);
		Registry.register(Registry.ITEM, new Identifier(mod_id, "arrival_card"), ARRIVAL_CARD);
		Registry.register(Registry.ITEM, new Identifier(mod_id, "guide_book"), GUIDE_BOOK);
		Registry.register(Registry.ITEM, new Identifier(mod_id, "oxygen_loader"), OXYGEN_LOADER);
		Registry.register(Registry.ITEM, new Identifier(mod_id, "hydrogen_loader"), HYDROGEN_LOADER);
		Registry.register(Registry.ITEM, new Identifier(mod_id, "divider"), DIVIDER);
		Registry.register(Registry.ITEM, new Identifier(mod_id, "item_wand"), WAND);
		
		// Armor Items
		Registry.register(Registry.ITEM, new Identifier(mod_id, "space_suit_helmet"), SPACE_SUIT_HELMET);
		Registry.register(Registry.ITEM, new Identifier(mod_id, "space_suit_chestplate"), SPACE_SUIT_CHESTPLATE);
		Registry.register(Registry.ITEM, new Identifier(mod_id, "space_suit_leggings"), SPACE_SUIT_LEGGINGS);
		Registry.register(Registry.ITEM, new Identifier(mod_id, "space_suit_boots"), SPACE_SUIT_BOOTS);
		
		// Structure Placer Items
		Registry.register(Registry.ITEM, new Identifier(mod_id, "rocket_1"), ROCKET_1);
		
		// Spawn Egg Items
		Registry.register(Registry.ITEM, new Identifier(mod_id, "cerulean_spawn_egg"), CERULEAN_SPAWN_EGG);
		Registry.register(Registry.ITEM, new Identifier(mod_id, "dust_spawn_egg"), DUST_SPAWN_EGG);
		Registry.register(Registry.ITEM, new Identifier(mod_id, "ancient_humanoid_spawn_egg"), ANCIENT_HUMANOID_SPAWN_EGG);
		Registry.register(Registry.ITEM, new Identifier(mod_id, "solar_spectre_spawn_egg"), SOLAR_SPECTRE_SPAWN_EGG);
	}
}