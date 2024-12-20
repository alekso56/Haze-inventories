package io.alekso56.bukkit.hazeinv.conversion;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import io.alekso56.bukkit.hazeinv.Core;
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
		ENDER_CHEST(27, "ender-chest"), MAIN_INVENTORY(41, "inventory"),
		ARMOR_INVENTORY(4, "inventory");

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

	private static final JSONParser PARSER = new JSONParser(JSONParser.USE_INTEGER_STORAGE|JSONParser.MODE_PERMISSIVE);
    
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
		Core.instance.circles.clear();
		HashSet<Group> groups = new HashSet<Group>();
		Instant start_time = Instant.now();
		for (World world : Bukkit.getWorlds()) {
			Group group = pwi.getApi().getGroupFromWorld(world.getName());
			if (group.getConfigured()) {
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
			Core.instance.circles.add(circle);
			File ConversionDirectory = new File(pwi.getDataFolder().getAbsolutePath() + File.separatorChar + "data");
			int i = 0;
			File[] files = ConversionDirectory.listFiles();
			for (File uuid : files) {
				i++;
				try {
					if (uuid.isDirectory()) {
						UUID uuid_converted = UUID.fromString(uuid.getName());
						for (FileTypes filetype : FileTypes.values()) {
							String inv = uuid.getAbsolutePath() + File.separatorChar + group.getName() + filetype.fileEnding;
							File file = new File(inv);
							if(!file.exists())continue;
							try (FileInputStream inputstream = new FileInputStream(file);) {
								JSONObject out = (JSONObject) PARSER.parse(inputstream);

								if ((!out.containsKey("data-format") || ((int) out.get("data-format")) < 2)) {
									continue;
								}
								CompoundTag tag = null;
								JSONObject stats = (JSONObject) out.get("stats");
								if(stats != null && !stats.isEmpty()) {
									int level = (int) stats.getOrDefault("level",0);
									double falldistance = (double) stats.getOrDefault("fallDistance",0.0);
									double exhaustion = (double) stats.getOrDefault("exhaustion",0.0);
									double maxhealth = (double) stats.getOrDefault("max-health",20.0);
									int maxair  = (int)  stats.getOrDefault("maxAir",300);
									double health = (double) stats.getOrDefault("health",20.0);
									int remainingair = (int) stats.getOrDefault("remainingAir",300);
									int food = (int) stats.getOrDefault("food",20);
									double saturation = (double) stats.getOrDefault("saturation",5.0);
									int fireticks = (int)stats.getOrDefault("fireTicks",-20);
									double exp = (double) stats.getOrDefault("exp",0.0);
									String displayname = (String) stats.getOrDefault("display-name","");
								    
									tag = InventoryStorage.CreateDefaultSave(level,falldistance,exhaustion,maxhealth,maxair,health,remainingair,food,saturation,fireticks,exp,displayname);
								}else {
									tag = InventoryStorage.CreateDefaultSave();
								}

								JSONObject json = (JSONObject) out.get("inventory");
								if (json != null && !json.isEmpty()) {
									ItemStack[] inventory = InventorySerializer.INSTANCE.deserialize((JSONArray) json.get("inventory"),
											InventoryType.MAIN_INVENTORY.slots, (int) out.get("data-format"));
									
									Inventory inventoryTemp = Bukkit.createInventory(null, InventoryType.MAIN_INVENTORY.slots+4);
									
									JSONArray json2 = (JSONArray) json.get("armor");
									if (json2 != null && !json2.isEmpty()) {
										ItemStack[] inventory2 = InventorySerializer.INSTANCE.deserialize(json2,
												InventoryType.ARMOR_INVENTORY.slots,
												(int) out.get("data-format"));
										inventoryTemp.addItem(inventory2);
									}
									// load normal inventory
									inventoryTemp.addItem(inventory);
									tag.put(InventoryStorage.inventory_tag, InventoryStorage
											.inventoryToNBTOffsetStartBy5(inventoryTemp, false));
									//load ender chest
									JSONArray echesttag = (JSONArray) out.get(InventoryType.ENDER_CHEST.jsonTag);
									if(echesttag != null) {
										ItemStack[] echest = InventorySerializer.INSTANCE.deserialize(echesttag,
												InventoryType.ENDER_CHEST.slots, (int) out.get("data-format"));
										inventoryTemp = Bukkit.createInventory(null, InventoryType.ENDER_CHEST.slots);
										inventoryTemp.addItem(echest);
										tag.put(InventoryStorage.ender_inventory_tag,
												InventoryStorage.inventoryToNBTOffsetStartBy5(inventoryTemp, true));
									}
								}
								Core.instance.log(Level.WARNING, "Saved "+i+":"+files.length+" " + uuid_converted+" @ "+circle.getFriendlyName()+":"+LabelTag.getOf(filetype).name());
								InventoryStorage.writeData(circle, uuid_converted, LabelTag.getOf(filetype), tag);
							} catch (Exception e) {
								Core.instance.log(Level.WARNING, "Failed decoding json "+uuid.getName(),e);
							}

						}
					}
				} catch (Exception e) {
					Core.instance.log(Level.WARNING, "Failed outer try fail "+uuid.getName(),e);
				}
			}
		}
		Core.instance.log(Level.INFO, Duration.between(start_time, Instant.now()).toSeconds()+" seconds elapsed since start.");
		return true;
	}

}
