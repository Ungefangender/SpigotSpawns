package einfachfabioo.dev.spigotSpawns.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LocationManager {

    private final JavaPlugin plugin;
    private File file;
    private FileConfiguration config;

    public LocationManager(JavaPlugin plugin) {
        this.plugin = plugin;
        load();
    }

    private void load() {
        file = new File(plugin.getDataFolder(), "locations.yml");

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create locations.yml!");
            }
        }

        config = YamlConfiguration.loadConfiguration(file);
    }

    public void reload() {
        config = YamlConfiguration.loadConfiguration(file);
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save locations.yml!");
        }
    }

    // ======================
    // 📍 SET SPAWN
    // ======================

    public void saveSpawn(Player player) {

        Location loc = player.getLocation();

        config.set("spawn.world", loc.getWorld().getName());
        config.set("spawn.x", loc.getX());
        config.set("spawn.y", loc.getY());
        config.set("spawn.z", loc.getZ());
        config.set("spawn.yaw", loc.getYaw());
        config.set("spawn.pitch", loc.getPitch());

        // 🔥 NEU
        config.set("spawn.setter", player.getName());
        config.set("spawn.time", System.currentTimeMillis());

        save();
    }

    // ======================
    // 📍 GET SPAWN
    // ======================

    public Location getSpawn() {

        if (!config.contains("spawn.world")) return null;

        String world = config.getString("spawn.world");

        if (Bukkit.getWorld(world) == null) return null;

        double x = config.getDouble("spawn.x");
        double y = config.getDouble("spawn.y");
        double z = config.getDouble("spawn.z");
        float yaw = (float) config.getDouble("spawn.yaw");
        float pitch = (float) config.getDouble("spawn.pitch");

        return new Location(
                Bukkit.getWorld(world),
                x,
                y,
                z,
                yaw,
                pitch
        );
    }

    // ======================
    // ❓ HAS SPAWN
    // ======================

    public boolean hasSpawn() {
        return config.contains("spawn.world");
    }

    // ======================
    // 🗑 DELETE SPAWN
    // ======================

    public void deleteSpawn() {
        config.set("spawn", null);
        save();
    }

    // ======================
    // 👤 GET SETTER
    // ======================

    public String getSetter() {
        return config.getString("spawn.setter", "Unknown");
    }

    // ======================
    // 🕒 GET TIME
    // ======================

    public String getTime() {

        long time = config.getLong("spawn.time");

        if (time == 0) return "Unknown";

        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        return format.format(new Date(time));
    }
}
