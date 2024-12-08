package io.alekso56.bukkit.hazeinv;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.onarandombox.MultiverseCore.MultiverseCore;

import io.alekso56.bukkit.hazeinv.Commands.CircleCommand;
import io.alekso56.bukkit.hazeinv.Commands.ConversionCommand;
import io.alekso56.bukkit.hazeinv.Commands.PurgeCommand;
import io.alekso56.bukkit.hazeinv.Commands.TestCommand;
import io.alekso56.bukkit.hazeinv.EventListeners.PlayerEventListener;
import io.alekso56.bukkit.hazeinv.Models.Circle;
import io.alekso56.bukkit.hazeinv.Models.PlayerMeta;
import io.alekso56.bukkit.hazeinv.Util.VanillaPlayer;
import net.milkbowl.vault.economy.Economy;


public class Core extends JavaPlugin {

	public static Core instance;

	private static Economy econ = null;
	
	private static Cache<Object, Object> timeouts = CacheBuilder.newBuilder().maximumSize(1000)
			.expireAfterWrite(2, TimeUnit.SECONDS).build();
    
	public static boolean isTimedOut(UUID id) {
		return timeouts.getIfPresent(id) != null;
	}
	

	public static void timeout(UUID uniqueId) {
		timeouts.put(uniqueId, Instant.now().getEpochSecond());
	}
	
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

	private File PlayerMetaDir;

	public static MultiverseCore mwcore;
	
	static Gson gson = new Gson();

    
    @Override
    public void onLoad() {
        instance = this;
        CircleDir = new File(getDataFolder().getPath()+File.separatorChar+"Circles");
		if(!CircleDir.exists()){
			CircleDir.mkdirs();
		}
		PlayerMetaDir = new File(getDataFolder().getPath()+File.separatorChar+"PlayerMeta");
		if(!PlayerMetaDir.exists()) {
			PlayerMetaDir.mkdirs();
		}
    }

    @Override
    public void onEnable() {
        setupEconomy();
        loadCircleData();
        getServer().getPluginManager().registerEvents(new PlayerEventListener(), this);
        CircleCommand circlecommand = new CircleCommand();
        getCommand("circle").setExecutor(circlecommand);
        getCommand("circle").setTabCompleter(circlecommand);
   	    getCommand("hazconvert").setExecutor(new ConversionCommand());
        if(Bukkit.getServer().getPluginManager().getPlugin("Magic") != null) {
        	 getServer().getPluginManager().registerEvents(new io.alekso56.bukkit.hazeinv.EventListeners.MagicEvent(), this);
        }
        getCommand("hazpurge").setExecutor(new PurgeCommand());
        getCommand("haztest").setExecutor(new TestCommand());
        mwcore = (MultiverseCore) getServer().getPluginManager().getPlugin("Multiverse-Core");
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
				Core.instance.circles.addAll(Collections.singleton(circle));
			} catch (JsonSyntaxException | JsonIOException | IOException e) {
				warning("Failed reading "+file.getName());
			}
		}
	}

    private void saveCircleData() {
    	for(File circle : CircleDir.listFiles()) {
    		circle.delete();
    	}
		for(Circle circle : Core.instance.circles) {
			try (FileWriter circleFile = new FileWriter(new File(CircleDir,circle.getCircleName().toString()+".json"))){
				gson.toJson(circle, circleFile);
			} catch (JsonIOException | IOException e) {
				warning("Failed writing "+circle.getCircleName());
			}
		}
	}
    
    public void saveLastLogoutCircle(UUID id,Circle circle) {
    	PlayerMeta meta = new PlayerMeta(circle.getCircleName());
    	File file = new File(PlayerMetaDir,id.toString()+".json");
    	if(!file.exists()) {
			try {
				if(!file.createNewFile())return;
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    	try (FileWriter metaFile = new FileWriter(file)){
			gson.toJson(meta, metaFile);
		} catch (JsonIOException | IOException e) {
			warning("Failed writing PlayerMeta: "+id.toString());
		}
    }
    
    public Circle getLastLogoutCircle(UUID id) {
    	File file = new File(PlayerMetaDir,id.toString()+".json");
    	if(!file.exists())return null;
    	try (FileReader playermeta = new FileReader(file)){
			PlayerMeta metadata = gson.fromJson(playermeta, PlayerMeta.class);
			if(metadata.lastCircle == null)return null;
			for(Circle circle : Core.instance.circles) {
				if(circle.getCircleName().equals(metadata.lastCircle)) {
					return circle;
				}
			}
		} catch (JsonSyntaxException | JsonIOException | IOException e) {
			warning("Failed reading "+file.getName());
		}
    	return null;
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
