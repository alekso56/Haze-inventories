package io.alekso56.bukkit.hazeinv;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.alekso56.bukkit.hazeinv.Models.PlayerData;


public class Core extends JavaPlugin {
    public static Core instance;
    //during world transition, deny any 
    private static final HashMap<UUID, PlayerData> syncingPlayers = new HashMap<UUID, PlayerData>();
    
    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        Path basePath = getDataFolder().toPath();
        
    }

    @Override
    public void onDisable() {
       syncingPlayers.clear();
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
}
