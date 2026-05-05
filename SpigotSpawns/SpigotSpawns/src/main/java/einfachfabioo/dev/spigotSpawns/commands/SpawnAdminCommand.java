package einfachfabioo.dev.spigotSpawns.commands;

import einfachfabioo.dev.spigotSpawns.SpigotSpawns;
import einfachfabioo.dev.spigotSpawns.managers.LanguageManager;
import einfachfabioo.dev.spigotSpawns.managers.LocationManager;
import einfachfabioo.dev.spigotSpawns.managers.PermissionManager;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnAdminCommand implements CommandExecutor {

    private final SpigotSpawns plugin;
    private final LocationManager locationManager;
    private final LanguageManager languageManager;
    private final PermissionManager permissionManager;

    public SpawnAdminCommand(SpigotSpawns plugin) {
        this.plugin = plugin;
        this.locationManager = plugin.getLocationManager();
        this.languageManager = plugin.getLanguageManager();
        this.permissionManager = plugin.getPermissionManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        boolean hasPermission;

        if (sender instanceof Player player) {
            hasPermission = permissionManager.has(player, "admin");
        } else {
            hasPermission = true;
        }

        if (!hasPermission) {
            sender.sendMessage(languageManager.getMessage("messages.no-permission"));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§cUse: /spawnadmin <setspawn|delspawn|reload|spawninfo>");
            return true;
        }

        // SET SPAWN
        if (args[0].equalsIgnoreCase("setspawn")) {

            if (!(sender instanceof Player player)) {
                sender.sendMessage("§cOnly players!");
                return true;
            }

            locationManager.saveSpawn(player);
            sender.sendMessage(languageManager.getMessage("messages.set"));
            return true;
        }

        // DELETE SPAWN
        if (args[0].equalsIgnoreCase("delspawn")) {

            if (!locationManager.hasSpawn()) {
                sender.sendMessage(languageManager.getMessage("messages.no-spawn"));
                return true;
            }

            locationManager.deleteSpawn();
            sender.sendMessage(languageManager.getMessage("messages.delete"));
            return true;
        }

        // SPAWN INFO
        if (args[0].equalsIgnoreCase("spawninfo")) {

            if (!(sender instanceof Player player)) {
                sender.sendMessage("§cOnly players!");
                return true;
            }

            if (!locationManager.hasSpawn()) {
                player.sendMessage(languageManager.getMessage("messages.no-spawn"));
                return true;
            }

            Location loc = locationManager.getSpawn();

            player.sendMessage(" ");

            player.sendMessage(languageManager.getMessage("messages.spawninfo.header"));

            player.sendMessage(languageManager.getMessage("messages.spawninfo.world")
                    .replace("%world%", loc.getWorld().getName()));

            player.sendMessage(languageManager.getMessage("messages.spawninfo.coords")
                    .replace("%x%", String.valueOf(loc.getBlockX()))
                    .replace("%y%", String.valueOf(loc.getBlockY()))
                    .replace("%z%", String.valueOf(loc.getBlockZ())));

            player.sendMessage(languageManager.getMessage("messages.spawninfo.rotation")
                    .replace("%yaw%", String.valueOf((int) loc.getYaw()))
                    .replace("%pitch%", String.valueOf((int) loc.getPitch())));

            player.sendMessage(languageManager.getMessage("messages.spawninfo.setter")
                    .replace("%setter%", locationManager.getSetter()));

            player.sendMessage(languageManager.getMessage("messages.spawninfo.time")
                    .replace("%time%", locationManager.getTime()));

            player.sendMessage(" ");
            return true;
        }

        // RELOAD
        if (args[0].equalsIgnoreCase("reload")) {

            plugin.reloadConfig();
            languageManager.reload();
            locationManager.reload();

            sender.sendMessage(languageManager.getMessage("messages.reload"));
            return true;
        }

        sender.sendMessage("§cUse: /spawnadmin <setspawn|delspawn|reload|spawninfo>");
        return true;
    }
}