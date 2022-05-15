package space.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import space.StarflightMod;
import space.materials.SpaceSuitArmorMaterial;

public class StarflightItems
{
	public static final Item ALUMINUM_INGOT = new Item(new FabricItemSettings().group(StarflightMod.ITEM_GROUP));
	public static final Item BAUXITE = new Item(new FabricItemSettings().group(StarflightMod.ITEM_GROUP));
	public static final Item SULFUR = new Item(new FabricItemSettings().group(StarflightMod.ITEM_GROUP));
	public static final Item RUBBER = new Item(new FabricItemSettings().group(StarflightMod.ITEM_GROUP));
	public static final Item RUBBER_SAP = new Item(new FabricItemSettings().group(StarflightMod.ITEM_GROUP));
	public static final Item RUBBER_RESIN = new Item(new FabricItemSettings().group(StarflightMod.ITEM_GROUP));
	public static final Item BATTERY_CELL = new BatteryCellItem(new FabricItemSettings().maxCount(1).group(StarflightMod.ITEM_GROUP), 256.0);
	public static final Item OXYGEN_TANK_ITEM = new OxygenTankItem(new FabricItemSettings().maxCount(1).group(StarflightMod.ITEM_GROUP));
	public static final Item SUBSTRATE = new Item(new FabricItemSettings().group(StarflightMod.ITEM_GROUP));
	public static final Item CONTROL_UNIT = new Item(new FabricItemSettings().group(StarflightMod.ITEM_GROUP));
	public static final Item SOLAR_CELL = new Item(new FabricItemSettings().group(StarflightMod.ITEM_GROUP));
	public static final Item MULTIMETER = new MultimeterItem(new FabricItemSettings().maxCount(1).group(StarflightMod.ITEM_GROUP));
	public static final Item WRENCH = new WrenchItem(new FabricItemSettings().maxCount(1).group(StarflightMod.ITEM_GROUP));
	public static final Item NAVIGATION_CARD = new NavigationCardItem(new FabricItemSettings().maxCount(1).group(StarflightMod.ITEM_GROUP));
	public static final Item ARRIVAL_CARD = new ArrivalCardItem(new FabricItemSettings().maxCount(1).group(StarflightMod.ITEM_GROUP));
	public static final Item WAND = new MovingCraftWandItem(new FabricItemSettings());
	
    public static final ArmorMaterial SPACE_SUIT_ARMOR_MATERIAL = new SpaceSuitArmorMaterial();
    public static final Item SPACE_SUIT_HELMET = new SpaceSuitItem(SPACE_SUIT_ARMOR_MATERIAL, EquipmentSlot.HEAD, new Item.Settings().group(StarflightMod.ITEM_GROUP));
    public static final Item SPACE_SUIT_CHESTPLATE = new SpaceSuitItem(SPACE_SUIT_ARMOR_MATERIAL, EquipmentSlot.CHEST, new Item.Settings().group(StarflightMod.ITEM_GROUP));
    public static final Item SPACE_SUIT_LEGGINGS = new SpaceSuitItem(SPACE_SUIT_ARMOR_MATERIAL, EquipmentSlot.LEGS, new Item.Settings().group(StarflightMod.ITEM_GROUP));
    public static final Item SPACE_SUIT_BOOTS = new SpaceSuitItem(SPACE_SUIT_ARMOR_MATERIAL, EquipmentSlot.FEET, new Item.Settings().group(StarflightMod.ITEM_GROUP));
    
	public static void initializeItems()
	{
		String mod_id = StarflightMod.MOD_ID;
		Registry.register(Registry.ITEM, new Identifier(mod_id, "aluminum_ingot"), ALUMINUM_INGOT);
		Registry.register(Registry.ITEM, new Identifier(mod_id, "bauxite"), BAUXITE);
		Registry.register(Registry.ITEM, new Identifier(mod_id, "sulfur"), SULFUR);
		Registry.register(Registry.ITEM, new Identifier(mod_id, "rubber"), RUBBER);
		Registry.register(Registry.ITEM, new Identifier(mod_id, "rubber_sap"), RUBBER_SAP);
		Registry.register(Registry.ITEM, new Identifier(mod_id, "rubber_resin"), RUBBER_RESIN);
		Registry.register(Registry.ITEM, new Identifier(mod_id, "battery_cell"), BATTERY_CELL);
		Registry.register(Registry.ITEM, new Identifier(mod_id, "oxygen_tank_item"), OXYGEN_TANK_ITEM);
		Registry.register(Registry.ITEM, new Identifier(mod_id, "substrate"), SUBSTRATE);
		Registry.register(Registry.ITEM, new Identifier(mod_id, "control_unit"), CONTROL_UNIT);
		Registry.register(Registry.ITEM, new Identifier(mod_id, "solar_cell"), SOLAR_CELL);
		Registry.register(Registry.ITEM, new Identifier(mod_id, "multimeter"), MULTIMETER);
		Registry.register(Registry.ITEM, new Identifier(mod_id, "wrench"), WRENCH);
		Registry.register(Registry.ITEM, new Identifier(mod_id, "navigation_card"), NAVIGATION_CARD);
		Registry.register(Registry.ITEM, new Identifier(mod_id, "arrival_card"), ARRIVAL_CARD);
		Registry.register(Registry.ITEM, new Identifier(mod_id, "item_wand"), WAND);
		
		Registry.register(Registry.ITEM, new Identifier(mod_id, "space_suit_helmet"), SPACE_SUIT_HELMET);
		Registry.register(Registry.ITEM, new Identifier(mod_id, "space_suit_chestplate"), SPACE_SUIT_CHESTPLATE);
		Registry.register(Registry.ITEM, new Identifier(mod_id, "space_suit_leggings"), SPACE_SUIT_LEGGINGS);
		Registry.register(Registry.ITEM, new Identifier(mod_id, "space_suit_boots"), SPACE_SUIT_BOOTS);
	}
}
