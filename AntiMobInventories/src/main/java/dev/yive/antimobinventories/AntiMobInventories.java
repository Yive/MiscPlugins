package dev.yive.antimobinventories;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Donkey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Llama;
import org.bukkit.entity.Mule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.*;
import org.bukkit.plugin.java.JavaPlugin;

public final class AntiMobInventories extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    /**
     * Prevents the player from opening any inventory that is connected to a mule, donkey or llama.
     * A small side effect is they can't open their own inventory while riding said mobs.
     * Another side effect is that they can no longer remove items placed on the mob such as saddles
     * without killing the mob.
     *
     * This was mainly added so that if for whatever reason the exploiter finds a donkey or whatever with a chest,
     * they won't be able to actually dupe.
     */
    @EventHandler
    public void onOpen(InventoryOpenEvent e) {
        InventoryHolder inventory = e.getInventory().getHolder();
        if (inventory instanceof Donkey || inventory instanceof Llama || inventory instanceof Mule) e.setCancelled(true);
    }

    /**
     * Removes the ability to place chests on Llama, Donkey or Mule.
     *
     * There's a small bug with cancelling this where the player will get given the direction that
     * the mob is facing.
     *
     * A fix for that would be to change the player's direction and pitch a tick after this event,
     * but I won't be adding that due to it being a waste of CPU cycles.
     */
    @EventHandler
    public void onChest(PlayerInteractEntityEvent e) {
        EntityType type = e.getRightClicked().getType();
        ItemStack item = e.getPlayer().getInventory().getItemInMainHand();
        if (item != null && item.getType() == Material.CHEST && (type == EntityType.LLAMA || type == EntityType.DONKEY || type == EntityType.MULE)) {
            e.setCancelled(true);
        }
    }
}
