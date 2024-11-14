package io.alekso56.bukkit.hazeinv.API;

import java.util.UUID;

import org.bukkit.World;
import org.bukkit.entity.Player;

import io.alekso56.bukkit.hazeinv.Core;
import io.alekso56.bukkit.hazeinv.Models.Circle;

public class CircleAPI {
	static UUID global_circle = UUID.fromString("49414d47-4c4f-4241-4c53-5741524d5748");
	
	public static Circle getFromWorld(World world) {
		UUID target = world.getUID();
		Circle global_circle_target = null;
		for(Circle circle : Core.instance.circles) {
			for(UUID worlds : circle.getWorlds()) {
				if(worlds.equals(target)) {
					return circle;
				}
			}
			if(circle.getCircleName().equals(global_circle)) {
				global_circle_target = circle;
			}
		}
		Circle circle = global_circle_target != null?global_circle_target:new Circle(world);
		Core.instance.circles.add(circle);
		return circle;
    }
	public static Circle getFromUser(Player user) {
		return Core.instance.players.get(user).getCurrent_circle();
    }
    public static Circle getFromName(UUID circlename) {
    	for(Circle circle : Core.instance.circles) {
    		if(circle.getCircleName().equals(circlename)) {
    			return circle;
    		}
    	}
		return null;
    }
}
