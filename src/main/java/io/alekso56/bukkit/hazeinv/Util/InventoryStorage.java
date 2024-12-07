package io.alekso56.bukkit.hazeinv.Util;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ListIterator;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_21_R1.CraftRegistry;
import org.bukkit.craftbukkit.v1_21_R1.CraftServer;
import org.bukkit.craftbukkit.v1_21_R1.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_21_R1.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.Inventory;

import io.alekso56.bukkit.hazeinv.Core;
import io.alekso56.bukkit.hazeinv.Enums.Flag;
import io.alekso56.bukkit.hazeinv.Enums.LabelTag;
import io.alekso56.bukkit.hazeinv.Models.Circle;
import io.alekso56.bukkit.hazeinv.Models.Settings;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.PlayerDataStorage;

public class InventoryStorage {

	public static final String inventory_tag = "Inventory";

	public static final String ender_inventory_tag = "EnderItems";

	public enum slotmapping {
		HEAD(103, 0), CHEST(102, 1), LEG(101, 2), FEET(100, 3), SHIELD(-106, 4), INVENTORY_START(9, 9),
		HOTBAR_START(0, 36);

		private final int nbtSlot;
		private final int chestSlot;

		slotmapping(int nbtSlot, int chestSlot) {
			this.nbtSlot = nbtSlot;
			this.chestSlot = chestSlot;
		}

		public int getNbtSlot() {
			return nbtSlot;
		}

		public int getChestSlot() {
			return chestSlot;
		}

		public static boolean isInInventory(int slot, boolean useNbt) {
			int startSlot = useNbt ? INVENTORY_START.nbtSlot : INVENTORY_START.chestSlot;
			int endSlot = useNbt ? FEET.nbtSlot : HOTBAR_START.chestSlot;
			return slot >= startSlot && slot < endSlot;
		}

		public static boolean isInHotbar(int slot, boolean useNbt) {
			int startSlot = useNbt ? HOTBAR_START.nbtSlot : HOTBAR_START.chestSlot;
			int endSlot = useNbt ? INVENTORY_START.nbtSlot : 45;
			return slot >= startSlot && slot < endSlot;
		}

		public static boolean isInArmor(int slot, boolean useNbt) {
			int startSlot = useNbt ? FEET.nbtSlot : FEET.chestSlot;
			int endSlot = useNbt ? HEAD.nbtSlot : HEAD.chestSlot;
			return slot >= startSlot && slot <= endSlot;
		}

		public boolean isInShield() {
			return nbtSlot == SHIELD.nbtSlot;
		}

		public static slotmapping getMapping(int slot, boolean useNbt) {
			for (slotmapping value : slotmapping.values()) {
				if (slot == (useNbt ? value.getNbtSlot() : value.getChestSlot())) {
					return value;
				}
			}
			// inbetween
			if (isInInventory(slot, useNbt))
				return slotmapping.INVENTORY_START;
			if (isInHotbar(slot, useNbt))
				return slotmapping.HOTBAR_START;
			return slotmapping.INVENTORY_START;
		}
	}

	static final String Slotname = "Slot";

	static final String Last_location_Tag = "LastLocation";
	static final String MaxairTag = "MaxAir";
	static final String EconomyTag = "Economy";
	static final String airTag = "Air";
	static final String EXHAUSTIONtag = "foodExhaustionLevel";
	static final String xpLeveltag = "XpLevel";
	static final String xpProgresstag = "XpP"; // float
	static final String xpTotaltag = "XpTotal";
	static final String EnchantmentSeedtag = "XpSeed";
	static final String distancetag = "FallDistance";
	static final String firetag = "Fire";
	static final String foodtag = "foodLevel";
	static final String foodTicktag = "foodTickTimer";
	static final String healthtag = "Health";
	static final String effecttag = "ActiveEffects";
	static final String foodSaturationLevelTag = "foodSaturationLevel";

	private static String attributesTag = "Attributes";

	static File getFileForPlayer(Circle circle, UUID player,LabelTag type) {
		File PlayerFolder = new File(Core.instance.getDataFolder(),
				"saveData" + File.separatorChar + player);
		if (!PlayerFolder.exists())
			PlayerFolder.mkdirs();

		
		return new File(PlayerFolder, circle.getCircleName().toString() +"_"+ type.name() + (type.equals(LabelTag.PLUGIN)?type.getName():"") + ".dat");
	}

	static File getGlobalForPlayer(UUID player,LabelTag type) {
		File GlobalFolder = new File(Core.instance.getDataFolder(), "saveData" + File.separatorChar + "GLOBAL");
		if (!GlobalFolder.exists())
			GlobalFolder.mkdirs();

		return new File(GlobalFolder, player.toString()  +"_"+type.name()+".dat");
	}

	public static Location loadPosition(Circle current_circle, UUID player,LabelTag type) {
		CraftServer server = (CraftServer) Bukkit.getServer();
		@Nullable
		PlayerDataStorage storage = server.getHandle().playerIo;
		if (storage == null) {
			Core.instance.log(Level.WARNING, "Failed to load player data, playerIo is not enabled.");
			return null;
		}
		try {
			@Nullable
			CompoundTag tag = NbtIo.read(InventoryStorage.getFileForPlayer(current_circle, player,type).toPath());
			if (tag == null || !containsAndExists(tag, "Pos")) {
				return null;
			}
			String worldname = tag.getString("Dimension").replace("minecraft:", "");
			ListTag Pos = tag.getList("Pos", CompoundTag.TAG_DOUBLE);
			double x = Pos.getDouble(0);
			double y = Pos.getDouble(1);
			double z = Pos.getDouble(2);
            return new Location(Bukkit.getWorld(worldname),x,y,z);
		} catch (IOException e) {
			Core.instance.log(Level.WARNING, "Failed to load player data for " + player.toString());
		}
		return null;
	}
	public static boolean savePosition(Circle current_circle, UUID player,Location loc,LabelTag type) {
		CraftServer server = (CraftServer) Bukkit.getServer();
		@Nullable
		PlayerDataStorage storage = server.getHandle().playerIo;
		if (storage == null) {
			Core.instance.log(Level.WARNING, "Failed to load player data, playerIo is not enabled.");
			return false;
		}
		try {
			@Nullable
			CompoundTag tag = NbtIo.read(InventoryStorage.getFileForPlayer(current_circle, player,type).toPath());
			if (tag == null) {
				return false;
			}
			ListTag Pos = new ListTag();
			Pos.add(DoubleTag.valueOf(loc.getX()));
			Pos.add(DoubleTag.valueOf(loc.getY()));
			Pos.add(DoubleTag.valueOf(loc.getZ()));
			tag.put("Pos", Pos);
			tag.putString("Dimension", "minecraft:"+loc.getWorld().getName());
            return true;
		} catch (IOException e) {
			Core.instance.log(Level.WARNING, "Failed to load player data for " + player.toString());
		}
		return false;
	}
	public static Inventory loadData(Circle current_circle, UUID player, boolean isEnderChest,LabelTag type) {
		CraftServer server = (CraftServer) Bukkit.getServer();
		@Nullable
		PlayerDataStorage storage = server.getHandle().playerIo;
		if (storage == null) {
			Core.instance.log(Level.WARNING, "Failed to load player data, playerIo is not enabled.");
			return null;
		}
		try {
			@Nullable
			CompoundTag tag = NbtIo.read(InventoryStorage.getFileForPlayer(current_circle, player,type).toPath());
			if (tag == null) {
				tag = InventoryStorage.CreateDefaultSave();
			}
			tag = InventoryStorage.FilterInventoryLoad(tag, current_circle, player,type);
			ListTag list = tag.getList(isEnderChest ? ender_inventory_tag : inventory_tag, CompoundTag.TAG_COMPOUND);

			Inventory inventory = Bukkit.createInventory(null, 45, "Example");

			ListIterator<Tag> listerator = list.listIterator();
			while (listerator.hasNext()) {

				CompoundTag realtag = (CompoundTag) listerator.next();

				if (realtag.contains(Slotname)) {

					int slot = realtag.getInt(Slotname);

					slotmapping mapping = slotmapping.getMapping(slot, true);
					Optional<ItemStack> item = ItemStack.parse(CraftRegistry.getMinecraftRegistry(), realtag);
					if (!item.isPresent())
						continue;

					switch (mapping) {
					case LEG:
					case SHIELD:
					case CHEST:
					case FEET:
					case HEAD:
						inventory.setItem(mapping.getChestSlot(), CraftItemStack.asBukkitCopy(item.get()));
						break;
					default:
						inventory.setItem(slot, CraftItemStack.asBukkitCopy(item.get()));
						break;
					}
				}
			}
			return inventory;
		} catch (IOException e) {
			Core.instance.log(Level.WARNING, "Failed to load player data for " + player.toString());
		}
		return null;
	}

	public static boolean saveData(Circle current_circle, UUID player, Inventory inv, boolean isEnderChest,LabelTag type) {
		CraftInventory inventory = (CraftInventory) inv;
		try {
			File playerFile = InventoryStorage.getFileForPlayer(current_circle, player,type);
			
			CompoundTag tag = playerFile.exists()?NbtIo.read(playerFile.toPath()):null;
			if (tag == null) {
				tag = InventoryStorage.CreateDefaultSave();
			}
			tag = InventoryStorage.FilterInventoryLoad(tag, current_circle, player,type);
			tag.put(isEnderChest ? ender_inventory_tag : inventory_tag, inventoryToNBTOffsetStartBy5(inventory,isEnderChest));
			tag = InventoryStorage.FilterInventorySave(tag, current_circle, current_circle, player,type);

			writeData(current_circle,player,type,tag);
            return true;
		} catch (Exception e) {
			Core.instance.log(Level.WARNING, "Failed to save player data for " + player,e);
			return false;
		}
	}
	
	public static void writeData(Circle current_circle,UUID player, LabelTag type,CompoundTag tag) {
		CraftServer server = (CraftServer) Bukkit.getServer();
		try {

			PlayerDataStorage worldNBTStorage = server.getHandle().playerIo;

			File file = new File(worldNBTStorage.getPlayerDir(), player +"_"+type.name()+".dat.tmp");
			if (file.exists()) {
				file.delete();
			}
			file.createNewFile();

			DataOutputStream output = new DataOutputStream(new FileOutputStream(file));
			File file1 = InventoryStorage.getFileForPlayer(current_circle, player,type);

			NbtIo.write(tag, output);
			output.close();

			if (file1.exists() && !file1.delete() || !file.renameTo(file1)) {
				Core.instance.log(Level.WARNING, "Failed to save player data for " + player);
			}else {
				Core.instance.log(Level.WARNING, "Saved " + file1.getName());
			}

		} catch (Exception e) {
			Core.instance.log(Level.WARNING, "Failed to save player data for " + player,e);
		}
	}
	  
	//Make sure inventory is offset by 5
	public static ListTag inventoryToNBTOffsetStartBy5(Inventory inventory, boolean enderchest) {
		ListTag itemsList = new ListTag();
		for (int i = 0; i < inventory.getSize(); i++) {
			org.bukkit.inventory.ItemStack item = inventory.getItem(i);

			if (item != null) {
				net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);

				CompoundTag itemTag = new CompoundTag();
				try {
					itemTag = (CompoundTag) nmsItem.save(CraftRegistry.getMinecraftRegistry());
					if (enderchest || !(nmsItem.getItem() instanceof ArmorItem)) {
						itemTag.putInt("Slot", i);
					} else {
						slotmapping mapping = slotmapping.getMapping(i, false);
						switch (mapping) {
						case LEG:
						case SHIELD:
						case CHEST:
						case FEET:
						case HEAD:
							itemTag.putInt("Slot", mapping.getNbtSlot());
							break;
						default:
							itemTag.putInt("Slot", i);
							break;
						}
					}
				} catch (Exception e) {
					Core.instance.log(Level.WARNING, "Slot invalid "+i,e);
				}
				itemsList.add(itemTag);
			}
		}
		return itemsList;
	}

	static boolean containsAndExists(CompoundTag tag, String input) {
		return tag != null && tag.contains(input);
	}

	public static CompoundTag CreateDefaultSave() {
		CompoundTag save = new CompoundTag();
		save.put(inventory_tag, new ListTag());
		save.put(ender_inventory_tag, new ListTag());
		save.putInt(airTag, 300);
		save.putFloat(EXHAUSTIONtag, 0); // max 4
		save.putInt(xpLeveltag, 0);
		save.putFloat(xpProgresstag, 0);
		save.putInt(xpTotaltag, 0);
		save.putInt(EnchantmentSeedtag, 0);
		save.putFloat(distancetag, 0);
		save.putShort(firetag, (short) -20);
		save.putInt(foodtag, 20);
		save.putInt(foodTicktag, 0);
		save.putFloat(healthtag, 20);
		save.putFloat(foodSaturationLevelTag, 5);
		// potion effects can be null
		return save;
	}

	// global filter LOAD ONLY!
	static CompoundTag FilterInventoryLoad(CompoundTag tag, Circle circle, UUID player,LabelTag type) {
		File GlobalFile = getGlobalForPlayer(player,type);
		@Nullable
		CompoundTag data = null;
		if (GlobalFile.exists() && !Settings.eraseInsteadOfGlobal) {
			try {
				data = NbtIo.read(GlobalFile.toPath());
			} catch (IOException e) {
				Core.instance.getLogger().log(Level.WARNING, e.getMessage());
			}
		} else {
			data = CreateDefaultSave();
			try {
				NbtIo.write(data, GlobalFile.toPath());
			} catch (IOException e) {
				Core.instance.getLogger().log(Level.WARNING, e.getMessage());
			}
		}
		// synced to circle, not global, meaning inventory is global.
		if (circle.isSyncArmorOnly() && tag.contains(inventory_tag)) {
			Core.instance.getLogger().log(Level.WARNING, "ArmorOnly");
			ListTag list = tag.getList(inventory_tag, CompoundTag.TAG_COMPOUND);
			ListTag replacements = data.contains(inventory_tag) ? data.getList(inventory_tag, CompoundTag.TAG_COMPOUND)
					: null;

			ListIterator<Tag> listerator = list.listIterator();
			while (listerator.hasNext()) {

				CompoundTag realtag = (CompoundTag) listerator.next();

				if (realtag.contains(Slotname)) {

					int slot = realtag.getInt(Slotname);

					slotmapping mapping = slotmapping.getMapping(slot, true);
					switch (mapping) {
					case LEG:
					case SHIELD:
					case CHEST:
					case FEET:
					case HEAD:
						// WHITELISTED
						break;
					case HOTBAR_START:
					case INVENTORY_START:
						// if slot is not in whitelist, take replacement with same slot from global save
						// instead.
						if (replacements != null && !replacements.isEmpty()) {
							boolean isReplaced = false;
							ListIterator<Tag> replerator = replacements.listIterator();
							while (replerator.hasNext()) {
								CompoundTag replacetag = (CompoundTag) replerator.next();
								if (replacetag.contains(Slotname)) {
									int slotrepl = replacetag.getInt(Slotname);
									if (slotrepl == slot) {
										listerator.set(replacetag);
										isReplaced = true;
										break;
									}
								}
							}
							if (!isReplaced) {
								// our item is not an armor item, and there wasn't any replacement from global
								listerator.remove();
							}
						} else {
							// our replacements are null, so this entry should be deleted
							listerator.remove();
						}
						break;
					}

				}
			}
			tag.remove(inventory_tag);
			tag.put(inventory_tag, list);
		} else if (!circle.canLoadMainInventory() && tag.contains(inventory_tag)) {
			Core.instance.getLogger().log(Level.WARNING, "Filter: CantLoadMain");
			// can't sync inventory with circle or armor items, therefore get all from
			// global.
			tag.remove(inventory_tag);

			tag.put(inventory_tag, containsAndExists(data, inventory_tag) ? data.get(inventory_tag) : new ListTag());
		}
		if (!circle.isSyncEnderChest() && tag.contains(ender_inventory_tag)) {
			Core.instance.getLogger().log(Level.WARNING, "Filter: CantLoadEchest");
			// can't sync inventory with circle or armor items, therefore get all from
			// global.
			tag.remove(ender_inventory_tag);
			tag.put(ender_inventory_tag,
					containsAndExists(data, ender_inventory_tag) ? data.get(ender_inventory_tag) : new ListTag());
		}

		for (Flag flag : Flag.values()) {
			// the flag is not allowed, get from global instead!
			if (!circle.checkFlag(flag)) {
				switch (flag) {
				case ATTRIBUTES:
					tag.remove(attributesTag);
					
					tag.put(attributesTag,
							containsAndExists(data, attributesTag) ? data.getList(attributesTag, CompoundTag.TAG_COMPOUND)
									: new ListTag());
					break;
				case AIR:
					tag.remove(airTag);

					tag.putInt(airTag, containsAndExists(data, airTag) ? data.getInt(airTag) : 300);
					break;
				case EXHAUSTION:
					tag.remove(EXHAUSTIONtag);

					tag.putFloat(EXHAUSTIONtag,
							containsAndExists(data, EXHAUSTIONtag) ? data.getFloat(EXHAUSTIONtag) : 0);
					break;
				case EXPERIENCE:
					tag.remove(xpLeveltag);
					tag.remove(xpProgresstag);
					tag.remove(xpTotaltag);
					tag.remove(EnchantmentSeedtag);

					tag.putInt(xpLeveltag, containsAndExists(data, xpLeveltag) ? data.getInt(xpLeveltag) : 0);
					tag.putFloat(xpProgresstag,
							containsAndExists(data, xpProgresstag) ? data.getFloat(xpProgresstag) : 0);
					tag.putInt(xpTotaltag, containsAndExists(data, xpTotaltag) ? data.getInt(xpTotaltag) : 0);
					tag.putInt(EnchantmentSeedtag,
							containsAndExists(data, EnchantmentSeedtag) ? data.getInt(EnchantmentSeedtag) : 0);
					break;
				case FALL_DISTANCE:
					tag.remove(distancetag);

					tag.putFloat(distancetag, containsAndExists(data, distancetag) ? data.getFloat(distancetag) : 0);
					break;
				case FIRE_TICKS:
					tag.remove(firetag);

					tag.putShort(firetag, containsAndExists(data, firetag) ? data.getShort(firetag) : (short) -20);
					break;
				case FOOD_LEVEL:
					tag.remove(foodtag);
					tag.remove(foodTicktag);

					tag.putInt(foodtag, containsAndExists(data, foodtag) ? data.getInt(foodtag) : 20);
					tag.putInt(foodTicktag, containsAndExists(data, foodTicktag) ? data.getInt(foodTicktag) : 0);
					break;
				case HEALTH:
					tag.remove(healthtag);

					tag.putFloat(healthtag, containsAndExists(data, healthtag) ? data.getFloat(healthtag) : 20);
					break;
				case POTIONS:
					tag.remove(effecttag);

					tag.put(effecttag,
							containsAndExists(data, effecttag) ? data.getList(effecttag, CompoundTag.TAG_COMPOUND)
									: new ListTag());
					break;
				case SATURATION:
					tag.remove(foodSaturationLevelTag);

					tag.putFloat(foodSaturationLevelTag,
							containsAndExists(data, foodSaturationLevelTag) ? data.getFloat(foodSaturationLevelTag)
									: 5);
					break;
				case ECONOMY:
					if (Core.getEcon().isEnabled()) {
						OfflinePlayer player2 = Bukkit.getOfflinePlayer(player);
						double past_money = Core.getEcon().getBalance(player2);
						Core.getEcon().withdrawPlayer(player2, past_money);
						if (containsAndExists(data, EconomyTag)) {
							Core.getEcon().depositPlayer(player2, data.getDouble(EconomyTag));
						}
					}
					break;
				case MAX_AIR:
					((LivingEntity) Bukkit.getOfflinePlayer(player))
							.setMaximumAir(containsAndExists(data, MaxairTag) ? data.getInt(MaxairTag) : 300);
					break;
				default:
					break;
				}
			}
		}
		return tag;
	}

	public static CompoundTag FilterInventorySave(CompoundTag tag, Circle current_circle, Circle previous_circle,
			UUID player,LabelTag type) {
		File GlobalFile = getGlobalForPlayer(player,type);
		@Nullable
		CompoundTag data = null;
		if (GlobalFile.exists()) {
			try {
				data = NbtIo.read(GlobalFile.toPath());
			} catch (IOException e) {
				Core.instance.getLogger().log(Level.WARNING, e.getMessage());
			}
		} else {
			data = CreateDefaultSave();
			try {
				NbtIo.write(data, GlobalFile.toPath());
			} catch (IOException e) {
				Core.instance.getLogger().log(Level.WARNING, e.getMessage());
			}
		}
		data.putUUID(Last_location_Tag, current_circle.getCircleName());
		// synced to circle, not global, meaning inventory is global.
		if (previous_circle.isSyncArmorOnly() && tag.contains(inventory_tag)) {

			ListTag list = tag.getList(inventory_tag, CompoundTag.TAG_COMPOUND);
			ListTag globalInv = data.contains(inventory_tag) ? data.getList(inventory_tag, CompoundTag.TAG_COMPOUND)
					: null;

			ListIterator<Tag> listerator = list.listIterator();
			while (listerator.hasNext()) {

				CompoundTag realtag = (CompoundTag) listerator.next();

				if (realtag.contains(Slotname)) {

					int slot = realtag.getInt(Slotname);
					// Optional to check for ranges instead, but prefer this method to create a
					// dynamic filter for future use.
					slotmapping mapping = slotmapping.getMapping(slot, true);
					switch (mapping) {
					case LEG:
					case SHIELD:
					case CHEST:
					case FEET:
					case HEAD:
						// WHITELISTED
						break;
					case HOTBAR_START:
					case INVENTORY_START:
						// if slot is not in whitelist, save in same slot from global save
						// instead.
						if (globalInv != null && !globalInv.isEmpty()) {
							boolean Exists = false;
							ListIterator<Tag> replerator = globalInv.listIterator();
							while (replerator.hasNext()) {
								CompoundTag replacetag = (CompoundTag) replerator.next();
								if (replacetag.contains(Slotname)) {
									int slotrepl = replacetag.getInt(Slotname);
									if (slotrepl == slot) {
										replerator.set(realtag);
										Exists = true;
										break;
									}
								}
							}
							if (!Exists) {
								globalInv.add(realtag);
							}
						} else {
							globalInv = new ListTag();
							globalInv.add(realtag);
						}
						break;
					}
				}
			}
			data.put(inventory_tag, globalInv);
			tag.put(inventory_tag, list);
		} else if (!previous_circle.canLoadMainInventory() && tag.contains(inventory_tag)) {
			// can't sync inventory with circle or armor items, therefore save all to
			// global.
			data.put(inventory_tag, containsAndExists(tag, inventory_tag) ? tag.get(inventory_tag) : new ListTag());
			tag.remove(inventory_tag);
		}
		if (!previous_circle.isSyncEnderChest() && tag.contains(ender_inventory_tag)) {
			// can't sync inventory with circle or armor items, therefore save all to
			// global.
			data.put(ender_inventory_tag,
					containsAndExists(tag, ender_inventory_tag) ? tag.get(ender_inventory_tag) : new ListTag());
			tag.remove(ender_inventory_tag);
		}
		for (Flag flag : Flag.values()) {
			// the flag is not allowed, save to global instead!
			if (!previous_circle.checkFlag(flag)) {
				switch (flag) {
				case ATTRIBUTES:
					data.put(attributesTag,
							containsAndExists(tag, attributesTag) ? tag.getList(attributesTag, CompoundTag.TAG_COMPOUND)
									: new ListTag());
					break;
				case AIR:
					data.putInt(airTag, containsAndExists(tag, airTag) ? tag.getInt(airTag) : 300);
					break;
				case EXHAUSTION:
					data.putFloat(EXHAUSTIONtag,
							containsAndExists(tag, EXHAUSTIONtag) ? tag.getFloat(EXHAUSTIONtag) : 0);
					break;
				case EXPERIENCE:
					data.putInt(xpLeveltag, containsAndExists(tag, xpLeveltag) ? tag.getInt(xpLeveltag) : 0);
					data.putFloat(xpProgresstag,
							containsAndExists(tag, xpProgresstag) ? tag.getFloat(xpProgresstag) : 0);
					data.putInt(xpTotaltag, containsAndExists(tag, xpTotaltag) ? tag.getInt(xpTotaltag) : 0);
					data.putInt(EnchantmentSeedtag,
							containsAndExists(tag, EnchantmentSeedtag) ? tag.getInt(EnchantmentSeedtag) : 0);
					break;
				case FALL_DISTANCE:
					data.putFloat(distancetag, containsAndExists(tag, distancetag) ? tag.getFloat(distancetag) : 0);
					break;
				case FIRE_TICKS:
					data.putShort(firetag, containsAndExists(tag, firetag) ? tag.getShort(firetag) : (short) -20);
					break;
				case FOOD_LEVEL:
					data.putInt(foodtag, containsAndExists(tag, foodtag) ? tag.getInt(foodtag) : 20);
					data.putInt(foodTicktag, containsAndExists(tag, foodTicktag) ? tag.getInt(foodTicktag) : 0);
					break;
				case HEALTH:
					data.putFloat(healthtag, containsAndExists(tag, healthtag) ? tag.getFloat(healthtag) : 20);
					break;
				case POTIONS:
					data.put(effecttag,
							containsAndExists(tag, effecttag) ? tag.getList(effecttag, CompoundTag.TAG_COMPOUND)
									: new ListTag());
					break;
				case SATURATION:
					data.putFloat(foodSaturationLevelTag,
							containsAndExists(tag, foodSaturationLevelTag) ? tag.getFloat(foodSaturationLevelTag) : 5);
					break;
				case ECONOMY:
					if (Core.getEcon().isEnabled()) {
						double past_money = Core.getEcon().getBalance(Bukkit.getOfflinePlayer(player));
						data.putDouble(EconomyTag, past_money);
					}
					break;
				case MAX_AIR:
					data.putInt(MaxairTag, ((LivingEntity) Bukkit.getOfflinePlayer(player)).getMaximumAir());
					break;
				default:
					break;
				}
			}
		}
		if(!Settings.eraseInsteadOfGlobal) {
		try {
			NbtIo.write(data, GlobalFile.toPath());
		} catch (IOException e) {
			Core.instance.getLogger().log(Level.WARNING, e.getMessage());
		}}
		return tag;
	}


}
