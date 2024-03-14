package space.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.AxeItem;
import net.minecraft.item.FoodComponents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import space.StarflightMod;
import space.block.StarflightBlocks;
import space.entity.StarflightEntities;
import space.materials.SpaceSuitArmorMaterial;
import space.util.FluidResourceType;

public class StarflightItems
{
	// Item Group
	public static final RegistryKey<ItemGroup> ITEM_GROUP = RegistryKey.of(RegistryKeys.ITEM_GROUP, new Identifier(StarflightMod.MOD_ID, "general"));
	
	// Armor Material
	public static final ArmorMaterial SPACE_SUIT_ARMOR_MATERIAL = new SpaceSuitArmorMaterial();
	
	// Tool Material
	
	
	// Items
	public static final Item ALUMINUM_INGOT = new Item(new FabricItemSettings());
	public static final Item BAUXITE = new Item(new FabricItemSettings());
	public static final Item TITANIUM_INGOT = new Item(new FabricItemSettings());
	public static final Item ILMENITE = new Item(new FabricItemSettings());
	public static final Item SULFUR = new Item(new FabricItemSettings());
	public static final Item HEMATITE = new Item(new FabricItemSettings());
	public static final Item RUBBER = new Item(new FabricItemSettings());
	public static final Item RUBBER_SAP = new Item(new FabricItemSettings());
	public static final Item RUBBER_RESIN = new Item(new FabricItemSettings());
	public static final Item CHEESE = new Item(new FabricItemSettings().food(FoodComponents.APPLE));
	public static final Item ALUMINUM_SHAFT = new Item(new FabricItemSettings());
	public static final Item TITANIUM_SWORD = new SwordItem(ToolMaterials.DIAMOND, 3, -2.4f, new Item.Settings());
    public static final Item TITANIUM_SHOVEL = new ShovelItem(ToolMaterials.DIAMOND, 1.5f, -3.0f, new Item.Settings());
    public static final Item TITANIUM_PICKAXE = new PickaxeItem(ToolMaterials.DIAMOND, 1, -2.8f, new Item.Settings());
    public static final Item TITANIUM_AXE = new AxeItem(ToolMaterials.DIAMOND, 5.0f, -3.1f, new Item.Settings());
    public static final Item TITANIUM_HOE = new CustomHoeItem(ToolMaterials.DIAMOND, -3, -1.0f, new Item.Settings());
	public static final Item BATTERY_CELL = new BatteryCellItem(new FabricItemSettings().maxCount(1), 2048.0);
	public static final Item OXYGEN_TANK_ITEM = new OxygenTankItem(new FabricItemSettings().maxCount(1));
	public static final Item SUBSTRATE = new Item(new FabricItemSettings());
	public static final Item CONTROL_UNIT = new Item(new FabricItemSettings());
	public static final Item SOLAR_CELL = new Item(new FabricItemSettings());
	public static final Item ROCKET_CORE_1 = new Item(new FabricItemSettings());
	public static final Item ROCKET_CORE_2 = new Item(new FabricItemSettings());
	public static final Item MULTIMETER = new MultimeterItem(new FabricItemSettings().maxCount(1));
	public static final Item WRENCH = new WrenchItem(new FabricItemSettings().maxCount(1));
	public static final Item GUIDE_BOOK = new GuideBookItem(new FabricItemSettings().maxCount(1));
	public static final Item OXYGEN_LOADER = new LoaderItem(new FabricItemSettings().maxCount(1), FluidResourceType.OXYGEN);
	public static final Item HYDROGEN_LOADER = new LoaderItem(new FabricItemSettings().maxCount(1), FluidResourceType.HYDROGEN);
	public static final Item DIVIDER = new DividerItem(new FabricItemSettings().maxCount(1));
	public static final Item WAND = new MovingCraftWandItem(new FabricItemSettings());
	
	// Armor Items
    public static final Item SPACE_SUIT_HELMET = new SpaceSuitItem(SPACE_SUIT_ARMOR_MATERIAL, ArmorItem.Type.HELMET, new Item.Settings());
    public static final Item SPACE_SUIT_CHESTPLATE = new SpaceSuitItem(SPACE_SUIT_ARMOR_MATERIAL, ArmorItem.Type.CHESTPLATE, new Item.Settings());
    public static final Item SPACE_SUIT_LEGGINGS = new SpaceSuitItem(SPACE_SUIT_ARMOR_MATERIAL, ArmorItem.Type.LEGGINGS, new Item.Settings());
    public static final Item SPACE_SUIT_BOOTS = new SpaceSuitItem(SPACE_SUIT_ARMOR_MATERIAL, ArmorItem.Type.BOOTS, new Item.Settings());
    
    // Structure Placer Items
    public static final Item ROCKET_1 = new StructurePlacerItem(new FabricItemSettings().maxCount(1), 7, 20, 7, "rocket_1");
    
    // Spawn Egg Items
    public static final Item CERULEAN_SPAWN_EGG = new SpawnEggItem(StarflightEntities.CERULEAN, 0x1485AD, 0x000000, new Item.Settings());
	public static final Item DUST_SPAWN_EGG = new SpawnEggItem(StarflightEntities.DUST, 0xBE673F, 0x82342B, new Item.Settings());
	public static final Item ANCIENT_HUMANOID_SPAWN_EGG = new SpawnEggItem(StarflightEntities.ANCIENT_HUMANOID, 0xF8F9F9, 0x154360, new Item.Settings());
	public static final Item SOLAR_SPECTRE_SPAWN_EGG = new SpawnEggItem(StarflightEntities.SOLAR_SPECTRE, 0xE9F3FD, 0xC8E2F9, new Item.Settings());
	public static final Item STRATOFISH_SPAWN_EGG = new SpawnEggItem(StarflightEntities.STRATOFISH, 0xFFE8FA, 0x7B6D9E, new Item.Settings());
	
	// Item Tags
	public static final TagKey<Item> NO_OXYGEN_FUEL_ITEM_TAG = TagKey.of(RegistryKeys.ITEM, new Identifier(StarflightMod.MOD_ID, "no_oxygen_fuel"));
	public static final TagKey<Item> COMBUSTION_ITEM_TAG = TagKey.of(RegistryKeys.ITEM, new Identifier(StarflightMod.MOD_ID, "combustion"));
    
	public static void initializeItems()
	{
		// Item Group
		Registry.register(Registries.ITEM_GROUP, ITEM_GROUP, FabricItemGroup.builder().icon(() -> new ItemStack(StarflightBlocks.PLANETARIUM)).displayName(Text.translatable("itemGroup.space.general")).build());
		
		// Items
		registerItem(ALUMINUM_INGOT, "aluminum_ingot");
		registerItem(BAUXITE, "bauxite");
		registerItem(TITANIUM_INGOT, "titanium_ingot");
		registerItem(ILMENITE, "ilmenite");
		registerItem(SULFUR, "sulfur");
		registerItem(HEMATITE, "hematite");
		registerItem(RUBBER, "rubber");
		registerItem(RUBBER_SAP, "rubber_sap");
		registerItem(RUBBER_RESIN, "rubber_resin");
		registerItem(CHEESE, "cheese");
		registerItem(ALUMINUM_SHAFT, "aluminum_shaft");
		registerItem(TITANIUM_SWORD, "titanium_sword");
		registerItem(TITANIUM_SHOVEL, "titanium_shovel");
		registerItem(TITANIUM_PICKAXE, "titanium_pickaxe");
		registerItem(TITANIUM_AXE, "titanium_axe");
		registerItem(TITANIUM_HOE, "titanium_hoe");
		registerItem(BATTERY_CELL, "battery_cell");
		registerItem(OXYGEN_TANK_ITEM, "oxygen_tank_item");
		registerItem(SUBSTRATE, "substrate");
		registerItem(CONTROL_UNIT, "control_unit");
		registerItem(SOLAR_CELL, "solar_cell");
		registerItem(ROCKET_CORE_1, "rocket_core_1");
		registerItem(ROCKET_CORE_2, "rocket_core_2");
		registerItem(MULTIMETER, "multimeter");
		registerItem(WRENCH, "wrench");
		registerItem(OXYGEN_LOADER, "oxygen_loader");
		registerItem(HYDROGEN_LOADER, "hydrogen_loader");
		registerItem(DIVIDER, "divider");
		registerItem(GUIDE_BOOK, "guide_book");
		registerItemHidden(WAND, "item_wand");
		
		// Armor Items
		registerItem(SPACE_SUIT_HELMET, "space_suit_helmet");
		registerItem(SPACE_SUIT_CHESTPLATE, "space_suit_chestplate");
		registerItem(SPACE_SUIT_LEGGINGS, "space_suit_leggings");
		registerItem(SPACE_SUIT_BOOTS, "space_suit_boots");
		
		// Structure Placer Items
		registerItem(ROCKET_1, "rocket_1");
		
		// Spawn Egg Items
		registerItem(CERULEAN_SPAWN_EGG, "cerulean_spawn_egg");
		registerItem(DUST_SPAWN_EGG, "dust_spawn_egg");
		registerItem(ANCIENT_HUMANOID_SPAWN_EGG, "ancient_humanoid_spawn_egg");
		registerItem(SOLAR_SPECTRE_SPAWN_EGG, "solar_spectre_spawn_egg");
		registerItem(STRATOFISH_SPAWN_EGG, "stratofish_spawn_egg");
	}
	
	private static void registerItem(Item item, String name)
	{
		Registry.register(Registries.ITEM, new Identifier(StarflightMod.MOD_ID, name), item);
		ItemGroupEvents.modifyEntriesEvent(ITEM_GROUP).register(content -> content.add(item));
	}
	
	private static void registerItemHidden(Item item, String name)
	{
		Registry.register(Registries.ITEM, new Identifier(StarflightMod.MOD_ID, name), item);
	}
}