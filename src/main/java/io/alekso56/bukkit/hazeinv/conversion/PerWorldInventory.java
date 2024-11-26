package io.alekso56.bukkit.hazeinv.conversion;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import io.alekso56.bukkit.hazeinv.Models.Circle;
import io.alekso56.bukkit.hazeinv.Util.InventoryStorage;
import io.alekso56.bukkit.hazeinv.Util.VanillaPlayer;
import me.ebonjaeger.perworldinventory.Group;
import me.ebonjaeger.perworldinventory.GroupManager;
import me.ebonjaeger.perworldinventory.configuration.PluginSettings;
import me.ebonjaeger.perworldinventory.configuration.Settings;
import me.ebonjaeger.perworldinventory.configuration.configme.SettingsManager;
import me.ebonjaeger.perworldinventory.configuration.configme.SettingsManagerImpl;
import me.ebonjaeger.perworldinventory.libs.json.JSONArray;
import me.ebonjaeger.perworldinventory.libs.json.JSONObject;
import me.ebonjaeger.perworldinventory.libs.json.parser.JSONParser;
import me.ebonjaeger.perworldinventory.libs.json.parser.ParseException;
import me.ebonjaeger.perworldinventory.serialization.InventorySerializer;

public class PerWorldInventory implements ConversionModule {

	@Override
	public boolean ToExternalSource() {
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

	static int ENDER_CHEST_SLOTS = 27;
	static int INVENTORY = 41;
	static int ARMOR = 4;
	private static final JSONParser PARSER = new JSONParser(JSONParser.USE_INTEGER_STORAGE);


	public static boolean convertPWI() throws FileNotFoundException, ParseException {
		me.ebonjaeger.perworldinventory.PerWorldInventory pwi = (me.ebonjaeger.perworldinventory.PerWorldInventory) Bukkit
				.getServer().getPluginManager().getPlugin("PerWorldInventory");
		HashSet<Group> groups = new HashSet<Group>();
		for(World world : Bukkit.getWorlds()) {
			Group group = pwi.getApi().getGroupFromWorld(world.getName());
			if(group != null) {
				groups.add(group);
			}
		}
		for(Group group : groups) {
			Circle circle = new Circle();
			circle.setFriendlyName(group.getName());
			for(String world : group.getWorlds()) {
				World hasWorld = Bukkit.getWorld(world);
				if(hasWorld != null)
					circle.getWorlds().add(hasWorld.getUID());
			}
            File ConversionDirectory = new File(pwi.getDataFolder().getAbsolutePath() + File.separatorChar + "data");
            for(File uuid: ConversionDirectory.listFiles()) {
            	if(uuid.isDirectory()) {
            		InventoryStorage.saveData(circle, null, null, false, null);
            		String inv = uuid.getAbsolutePath() + File.separatorChar +group.getName()+ ".json";
            		JSONObject out = (JSONObject) PARSER.parse(new FileInputStream(inv));

            		if ((!out.containsKey("data-format") || ((int) out.get("data-format")) < 2)) {
            			continue;
            		}
            		JSONArray json = (JSONArray) out.get("ender-chest");
            		if (json != null && !json.isEmpty()) {
            			InventorySerializer.INSTANCE.deserialize(json, ENDER_CHEST_SLOTS, (int) out.get("data-format"));
            			
            		}
            		
            		json = (JSONArray) out.get("inventory.inventory");
            		if (json != null && !json.isEmpty()) InventorySerializer.INSTANCE.deserialize(json, INVENTORY, (int) out.get("data-format"));
            		
            		json = (JSONArray) out.get("inventory.armor");
            		if (json != null && !json.isEmpty()) InventorySerializer.INSTANCE.deserialize(json, ARMOR, (int) out.get("data-format"));
            	}
            }
		}
		return true;
	}

}
