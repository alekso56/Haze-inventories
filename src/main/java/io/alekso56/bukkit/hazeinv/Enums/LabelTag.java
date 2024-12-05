package io.alekso56.bukkit.hazeinv.Enums;

import org.bukkit.GameMode;
import org.bukkit.plugin.Plugin;

import io.alekso56.bukkit.hazeinv.conversion.PerWorldInventory.FileTypes;

public enum LabelTag {
	CIRCLE_CREATIVE, 
	CIRCLE_ADVENTURE, 
	CIRCLE_SURVIVAL, 
	PLUGIN;

	String Name = "";

	public String getName() {
		return Name;
	}

	public void setName(Plugin plugin) {
		Name = plugin.getName();
	}

	public static LabelTag getOf(GameMode targetGameMode) {
		switch(targetGameMode) {
		case ADVENTURE:
			return LabelTag.CIRCLE_ADVENTURE;
		case CREATIVE:
			return LabelTag.CIRCLE_CREATIVE;
		default:
			return LabelTag.CIRCLE_SURVIVAL;
		}
	}

	public static LabelTag getOf(FileTypes filetype) {
		switch(filetype) {
		case ADVENTURE:
			return LabelTag.CIRCLE_ADVENTURE;
		case CREATIVE:
			return LabelTag.CIRCLE_CREATIVE;
		default:
			return LabelTag.CIRCLE_SURVIVAL;
		}
	}
}