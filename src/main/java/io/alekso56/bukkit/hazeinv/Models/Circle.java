package io.alekso56.bukkit.hazeinv.Models;

import org.bukkit.World;

import io.alekso56.bukkit.hazeinv.Enums.Flag;

public class Circle {
	String worldName;
	boolean isPerGameMode = false;
	boolean syncEnderChest = true;
	boolean syncMainInventory = true;
	boolean syncArmorOnly = false;
	int flags = 0;

	Circle(World name) {
		worldName = name.getName();
		resetFlags();
	}

	public String getWorldName() {
		return worldName;
	}

	public boolean isPerGameMode() {
		return isPerGameMode;
	}

	public boolean isSyncEnderChest() {
		return syncEnderChest;
	}

	public boolean isSyncMainInventory() {
		return syncMainInventory;
	}

	public boolean isSyncArmorOnly() {
		return syncArmorOnly;
	}

	public boolean canLoadMainInventory() {
		return !syncArmorOnly && syncMainInventory;
	}

	// enables all flags
	public void resetFlags() {
		int total = 0;
		for (Flag target : Flag.values()) {
			total |= (1 << target.ordinal());
		}
		flags = total;
	}

	public void addFlag(Flag type) {
		boolean[] changeSet = getFlags_int();
		changeSet[type.ordinal()] = true;
		saveFlags(changeSet);
	}

	public void removeFlag(Flag type) {
		boolean[] changeSet = getFlags_int();
		changeSet[type.ordinal()] = false;
		saveFlags(changeSet);
	}
	
	public boolean checkFlag(Flag type) {
		return (flags & (1 << type.ordinal())) != 0;
	}

	private void saveFlags(boolean[] input) {
		int total = 0;
		for (int i = 0; i < input.length; ++i) {
			if (input[i])
				total |= (1 << i);
		}
		flags = total;
	}

	private boolean[] getFlags_int() {
		boolean[] int_flags = new boolean[Flag.values().length];
		for (int i = 0; i < Flag.values().length; ++i) {
			int_flags[i] = (flags & (1 << i)) != 0;
		}
		return int_flags;
	}
}
