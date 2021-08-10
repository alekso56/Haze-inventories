package io.alekso56.bukkit.hazeinv;

import java.nio.file.Path;
import java.util.logging.Level;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class Core extends JavaPlugin {
    private static Core instance;

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
