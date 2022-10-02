package io.alekso56.bukkit.hazeinv.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.util.logging.Level;

import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;

import io.alekso56.bukkit.hazeinv.Core;
import net.minecraft.nbt.NBTCompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.level.storage.WorldNBTStorage;

public class VanillaPlayer extends CraftPlayer {

	public VanillaPlayer(CraftServer server, EntityPlayer entity) {
		super(server, entity);
	}
	
	@Override
    public void loadData() {
        NBTTagCompound player = this.server.getHandle().r.load(this.getHandle());
        if (player != null) {
            readExtraData(player);
        }
    }
	
	@Override
    public void saveData() {
        EntityPlayer player = this.getHandle();
        
        try {
            WorldNBTStorage worldNBTStorage = player.c.getPlayerList().r;

            NBTTagCompound playerData = player.save(new NBTTagCompound());
            setExtraData(playerData);

            File file = new File(worldNBTStorage.getPlayerDir(), player.getUniqueIDString() + ".dat.tmp");
            File file1 = new File(worldNBTStorage.getPlayerDir(), player.getUniqueIDString() + ".dat");

            NBTCompressedStreamTools.a(playerData, new FileOutputStream(file));

            if (file1.exists() && !file1.delete() || !file.renameTo(file1)) {
                Core.instance.log(Level.WARNING, "Failed to save player data for "+player.getDisplayName().getString());
            }

        } catch (Exception e) {
        	Core.instance.log(Level.WARNING, "Failed to save player data for "+player.getDisplayName().getString());
        }
    }
        
}
