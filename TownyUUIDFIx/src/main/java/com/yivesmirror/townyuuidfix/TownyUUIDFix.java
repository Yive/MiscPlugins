package com.yivesmirror.townyuuidfix;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.h2.jdbcx.JdbcConnectionPool;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public final class TownyUUIDFix extends JavaPlugin implements Listener {

    private File uuidStorage;
    private boolean h2Mode;
    private JdbcConnectionPool pool;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        if (!getDataFolder().exists()) getDataFolder().mkdirs();
        uuidStorage = new File(getDataFolder() + File.separator + "storage");
        if (!uuidStorage.exists()) uuidStorage.mkdirs();
        h2Mode = getConfig().getString("mode").equalsIgnoreCase("h2");
        if (!h2Mode){
            getLogger().log(Level.WARNING, "If you have low inodes, use TownyNameFix instead. This plugin is meant for large servers that would have lag upon login due to that plugin.");
        } else {
            getLogger().log(Level.WARNING, "H2 mode detected, note that this mode does not save previous igns.");
            pool = JdbcConnectionPool.create("jdbc:h2:." + File.separatorChar + getDataFolder().getPath() + File.separatorChar + "h2storage", getConfig().getString("database.user"), getConfig().getString("database.pass"));
            try (Connection connection =  pool.getConnection()) {
                connection.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS UUIDS (UUID VARCHAR(36) NOT NULL, CURRENT VARCHAR(16) NOT NULL, CONSTRAINT uuid PRIMARY KEY (UUID))");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent e) {
        if (!h2Mode) return;
        try (Connection connection = pool.getConnection()) {
            handlePlayerEntry(e.getName(), e.getUniqueId().toString(), connection);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void handlePlayerEntry(String name, String uuid, Connection connection) {
        if (connection == null) {
            File uuidFile = new File(uuidStorage, uuid + ".yml");
            YamlConfiguration data = YamlConfiguration.loadConfiguration(uuidFile);

            if (uuidFile.exists()) {
                String currentName = data.getString("current-ign");

                if (!currentName.equals(name)) {
                    List<String> previousIgns = data.getStringList("previous-igns");
                    previousIgns.add(currentName);

                    try {
                        data.set("current-ign", name);
                        data.set("previous-igns", previousIgns);
                        data.save(uuidFile);
                    } catch (IOException ex) {
                        getLogger().log(Level.SEVERE, "Unable to save data for " + name);
                        ex.printStackTrace();
                        return;
                    }

                    getServer().dispatchCommand(Bukkit.getConsoleSender(), "ta resident " + currentName + " rename " + name);
                    getLogger().log(Level.INFO, currentName + " changed their IGN to " + name);
                    return;
                }
                return;
            }

            try {
                data.set("current-ign", name);
                data.set("previous-igns", new ArrayList<String>());
                data.save(uuidFile);
            } catch (IOException ex) {
                getLogger().log(Level.SEVERE, "Unable to create data for " + name);
                ex.printStackTrace();
            }
        } else {
            try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM UUIDS WHERE UUID = ?")) {
                ps.setString(1, uuid);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String currentName = rs.getString("CURRENT");
                        if (!currentName.equals(name)) {
                            try (PreparedStatement ps1 = connection.prepareStatement("UPDATE UUIDS SET CURRENT = ? WHERE UUID = ?")) {
                                ps1.setString(1, name);
                                ps1.setString(2, uuid);
                                ps1.executeUpdate();

                                getServer().getScheduler().runTask(this, () -> getServer().dispatchCommand(getServer().getConsoleSender(), "ta resident " + currentName + " rename " + name));
                                getLogger().log(Level.INFO, currentName + " changed their IGN to " + name);
                            }
                        }
                    } else {
                        try (PreparedStatement ps1 = connection.prepareStatement("INSERT INTO UUIDS (UUID, CURRENT) VALUES (?,?)")) {
                            ps1.setString(1, uuid);
                            ps1.setString(2, name);
                            ps1.executeUpdate();
                        }
                    }
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (h2Mode) return;
        Player player = e.getPlayer();
        handlePlayerEntry(player.getName(), player.getUniqueId().toString(), null);
    }
}
