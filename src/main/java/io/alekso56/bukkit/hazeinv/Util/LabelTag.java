package io.alekso56.bukkit.hazeinv.Util;

import org.bukkit.GameMode;
import org.bukkit.plugin.Plugin;

public enum LabelTag {
	CIRCLE, 
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
		case SURVIVAL:
			return LabelTag.CIRCLE_SURVIVAL;
		default:
			return LabelTag.CIRCLE;
		}
	}
}
