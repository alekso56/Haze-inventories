package io.alekso56.bukkit.hazeinv.conversion;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import io.alekso56.bukkit.hazeinv.Enums.LabelTag;
import io.alekso56.bukkit.hazeinv.Models.Circle;
import io.alekso56.bukkit.hazeinv.Util.InventoryStorage;
import me.ebonjaeger.perworldinventory.Group;
import me.ebonjaeger.perworldinventory.libs.json.JSONArray;
import me.ebonjaeger.perworldinventory.libs.json.JSONObject;
import me.ebonjaeger.perworldinventory.libs.json.parser.JSONParser;
import me.ebonjaeger.perworldinventory.libs.json.parser.ParseException;
import me.ebonjaeger.perworldinventory.serialization.InventorySerializer;
import net.minecraft.nbt.CompoundTag;

public class PerWorldInventory implements ConversionModule {

	@Override
	public boolean ToExternalSource() {
		//PWI is a dead plugin.
		return false;
	}

	@Override
	public boolean FromExternalSource() {
		try {
			return convertPWI();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public String getPluginName() {
		return "PWI";
	}

	static enum InventoryType {
		ENDER_CHEST(27, "ender-chest"), MAIN_INVENTORY(41, "inventory.inventory"),
		ARMOR_INVENTORY(4, "inventory.armor");

		private int slots;
		private String jsonTag;

		public String getjsonTag() {
			return jsonTag;
		}

		public int getSlots() {
			return slots;
		}

		InventoryType(int i, String string) {
			slots = i;
			jsonTag = string;
		}
	}

	private static final JSONParser PARSER = new JSONParser(JSONParser.USE_INTEGER_STORAGE);

	public static enum FileTypes {
		ADVENTURE("_adventure.json"), CREATIVE("_creative.json"), SPECTATOR("_spectator.json"), SURVIVAL(".json");

		private String fileEnding;

		FileTypes(String string) {
			this.fileEnding = string;
		}
	}

	public static boolean convertPWI() throws FileNotFoundException, ParseException {
		me.ebonjaeger.perworldinventory.PerWorldInventory pwi = (me.ebonjaeger.perworldinventory.PerWorldInventory) Bukkit
				.getServer().getPluginManager().getPlugin("PerWorldInventory");
		HashSet<Group> groups = new HashSet<Group>();
		for (World world : Bukkit.getWorlds()) {
			Group group = pwi.getApi().getGroupFromWorld(world.getName());
			if (group != null) {
				groups.add(group);
			}
		}
		for (Group group : groups) {
			Circle circle = new Circle();
			circle.setFriendlyName(group.getName());
			for (String world : group.getWorlds()) {
				World hasWorld = Bukkit.getWorld(world);
				if (hasWorld != null)
					circle.getWorlds().add(hasWorld.getName());
			}
			File ConversionDirectory = new File(pwi.getDataFolder().getAbsolutePath() + File.separatorChar + "data");
			for (File uuid : ConversionDirectory.listFiles()) {
				try {
					if (uuid.isDirectory()) {
						UUID uuid_converted = UUID.fromString(uuid.getName());
						for (FileTypes filetype : FileTypes.values()) {
							CompoundTag tag = InventoryStorage.CreateDefaultSave();

							String inv = uuid.getAbsolutePath() + File.separatorChar + group.getName() + filetype.fileEnding;
							File file = new File(inv);
							if(!file.exists())continue;
							try (FileInputStream inputstream = new FileInputStream(file);) {
								JSONObject out = (JSONObject) PARSER.parse(inputstream);

								if ((!out.containsKey("data-format") || ((int) out.get("data-format")) < 2)) {
									continue;
								}

								for (InventoryType value : InventoryType.values()) {
									if (value.equals(InventoryType.ARMOR_INVENTORY))
										continue;

									JSONArray json = (JSONArray) out.get(value.jsonTag);
									if (json != null && !json.isEmpty()) {
										ItemStack[] inventory = InventorySerializer.INSTANCE.deserialize(json,
												value.getSlots(), (int) out.get("data-format"));
										Inventory inventoryTemp = Bukkit.createInventory(null, value.getSlots());

										switch (value) {
										case ENDER_CHEST:
											inventoryTemp.addItem(inventory);
											tag.put(InventoryStorage.ender_inventory_tag,
													InventoryStorage.inventoryToNBTOffsetStartBy5(inventoryTemp, true));
											break;
										case MAIN_INVENTORY:
											// Load armor inventory in the first slots
											JSONArray json2 = (JSONArray) out.get(value.jsonTag);
											if (json2 != null && !json2.isEmpty()) {
												ItemStack[] inventory2 = InventorySerializer.INSTANCE.deserialize(json2,
														InventoryType.ARMOR_INVENTORY.slots,
														(int) out.get("data-format"));
												inventoryTemp.addItem(inventory2);
											} else {
												ItemStack loftyGoals = new ItemStack(Material.AIR);
												// head to shield 5
												inventoryTemp.addItem(loftyGoals, loftyGoals, loftyGoals, loftyGoals,
														loftyGoals);
											}
											// load normal inventory
											inventoryTemp.addItem(inventory);
											tag.put(InventoryStorage.inventory_tag, InventoryStorage
													.inventoryToNBTOffsetStartBy5(inventoryTemp, false));
											break;
										default:
											break;
										}
									}
								}

							} catch (Exception e) {
								e.printStackTrace();
							}
							InventoryStorage.writeData(circle, uuid_converted, LabelTag.getOf(filetype), tag);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return true;
	}

}
