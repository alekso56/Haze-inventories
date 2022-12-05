package io.alekso56.bukkit.hazeinv.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import io.alekso56.bukkit.hazeinv.Models.Circle;

public class PreInventoryChangeEvent extends Event {
	
	private static final HandlerList HANDLERS = new HandlerList();
	
	private final Player player;
	private final Circle from_circle;
	private final Circle to_circle;
	
	public PreInventoryChangeEvent(Player player2,  Circle from_circle, Circle to_circle) {
		this.player = player2;
		this.from_circle = from_circle;
		this.to_circle = to_circle;
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}

	public Circle getFrom_circle() {
		return from_circle;
	}

	public Circle getTo_circle() {
		return to_circle;
	}

	public Player getPlayer() {
		return player;
	}

}
