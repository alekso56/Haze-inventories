package io.alekso56.bukkit.hazeinv;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import io.alekso56.bukkit.hazeinv.EventListeners.PlayerEventListener;
import io.alekso56.bukkit.hazeinv.Models.Circle;
import io.alekso56.bukkit.hazeinv.Util.VanillaPlayer;
import net.milkbowl.vault.economy.Economy;


public class Core extends JavaPlugin {
	
   public static Core instance;
   
   private static Economy econ = null;
    
    public final List<Circle> circles = new ArrayList<Circle>() {
    	//instead of "i forgor" we do saving here.
    	@Override
		public boolean add(Circle circle){
    		boolean result = super.add(circle);
    		if(result)saveCircleData();
			return result;
    	}
    	
    	@Override
		public boolean remove(Object circle){
    		boolean result = super.remove(circle);
    		if(result)saveCircleData();
			return result;
    	}
    };
    
    public final HashMap<Player,VanillaPlayer> players = new  HashMap<Player,VanillaPlayer>();

	private File CircleDir;
	
	static Gson gson = new Gson();

    
    @Override
    public void onLoad() {
        instance = this;
        CircleDir = new File(getDataFolder().getPath()+File.separatorChar+"Circles");
		if(!CircleDir.exists()){
			CircleDir.mkdirs();
		}
    }

    @Override
    public void onEnable() {
        setupEconomy();
        loadCircleData();
        getServer().getPluginManager().registerEvents(new PlayerEventListener(), this);
    }

	@Override
    public void onDisable() {
		saveCircleData();
		Core.instance.circles.clear();
    }
	

    private void loadCircleData() {
    	Core.instance.circles.clear();
		for(File file : CircleDir.listFiles()) {
			try (FileReader circlefile = new FileReader(file)){
				Circle circle = gson.fromJson(circlefile, Circle.class);
				Core.instance.circles.add(circle);
			} catch (JsonSyntaxException | JsonIOException | IOException e) {
				warning("Failed reading "+file.getName());
			}
		}
	}

    private void saveCircleData() {
		for(Circle circle : Core.instance.circles) {
			try (FileWriter circleFile = new FileWriter(new File(CircleDir,circle.getCircleName().toString()+".json"))){
				gson.toJson(circle, circleFile);
			} catch (JsonIOException | IOException e) {
				warning("Failed writing "+circle.getCircleName());
			}
		}
	}

	private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return getEcon() != null;
    }
    
    public void log(@NotNull Level level, @NotNull String message, @Nullable Throwable e) {
        if (e == null) {
            getLogger().log(level, message);
        } else {
            getLogger().log(level, message, e);
        }
    }

    public void log(@NotNull Level level, @NotNull String message) {
        log(level, message, null);
    }

    public void warning(@NotNull String message) {
        log(Level.WARNING, message);
    }

    public void info(@NotNull String message) {
        log(Level.INFO, message);
    }

	public static Economy getEcon() {
		return econ;
	}


}
