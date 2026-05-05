package einfachfabioo.dev.spigotSpawns.managers;

import einfachfabioo.dev.spigotSpawns.SpigotSpawns;
import org.bukkit.entity.Player;

public class PermissionManager {

    private final SpigotSpawns plugin;

    public PermissionManager(SpigotSpawns plugin) {
        this.plugin = plugin;
    }

    public boolean has(Player player, String key) {
        String perm = plugin.getConfig().getString("permissions." + key);

        if (perm == null || perm.isEmpty()) return true;

        return player.hasPermission(perm);
    }

    public String get(String key) {
        return plugin.getConfig().getString("permissions." + key);
    }
}
