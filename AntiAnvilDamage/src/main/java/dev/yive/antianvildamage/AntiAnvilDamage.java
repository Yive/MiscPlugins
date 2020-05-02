package dev.yive.antianvildamage;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class AntiAnvilDamage extends JavaPlugin implements Listener {

    private Set<String> worlds;

    @Override
    public void onEnable() {
        getDataFolder().mkdirs();
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
        worlds = new HashSet<>();
        List<String> worlds1 = getConfig().getStringList("worlds");
        for (int i = 0; i < worlds1.size(); i++) {
            worlds.add(worlds1.get(i).toLowerCase());
        }
    }

    @EventHandler
    public void onPrepare(PrepareAnvilEvent e) {
        Location location = e.getInventory().getLocation();
        if (location != null && worlds.contains(location.getWorld().getName().toLowerCase())) {
            Block block = location.getBlock();

            byte data = block.getData();
            // Preserve direction.
            if (data == 4 || data == 8) block.setData((byte) 0);
            if (data == 5 || data == 9) block.setData((byte) 1);
            if (data == 6 || data == 10) block.setData((byte) 2);
            if (data == 7 || data == 11) block.setData((byte) 3);
        }
    }
}
