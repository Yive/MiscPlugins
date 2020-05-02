package dev.yive.doorstuck;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class DoorStuck extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onLogin(PlayerJoinEvent e) {
        Block footBlock = e.getPlayer().getLocation().getBlock();
        Block eyeBlock = e.getPlayer().getEyeLocation().getBlock();
        if ((footBlock != null && (footBlock.getType() == Material.PORTAL || footBlock.getType() == Material.ENDER_PORTAL)) || (eyeBlock != null && (eyeBlock.getType() == Material.PORTAL || eyeBlock.getType() == Material.ENDER_PORTAL))) {
            getServer().dispatchCommand(Bukkit.getConsoleSender(), "/spawn " + e.getPlayer().getName());
        }
    }
}
