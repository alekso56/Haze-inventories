package io.alekso56.bukkit.hazeinv.Models;

import java.util.UUID;

public class PlayerMeta {
	public PlayerMeta(UUID circleName) {
		lastCircle = circleName;
	}

	public UUID lastCircle;
}
