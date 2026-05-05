package einfachfabioo.dev.spigotSpawns.utils;

import einfachfabioo.dev.spigotSpawns.SpigotSpawns;
import einfachfabioo.dev.spigotSpawns.managers.LanguageManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class UpdateChecker {

    private final SpigotSpawns plugin;
    private final LanguageManager languageManager;

    private String latestVersion = null;

    private final String versionUrl = "https://pastebin.com/raw/a38z7S1D";

    public UpdateChecker(SpigotSpawns plugin) {
        this.plugin = plugin;
        this.languageManager = plugin.getLanguageManager();
    }

    public void start() {

        if (!plugin.getConfig().getBoolean("update-check.enabled")) return;

        // sofort check
        check();

        int interval = plugin.getConfig().getInt("update-check.interval-minutes") * 60 * 20;

        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::check, interval, interval);

        // Nachricht zyklisch senden
        Bukkit.getScheduler().runTaskTimer(plugin, this::notifyAllPlayers, interval, interval);
    }

    private void check() {

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(versionUrl).openConnection();
            connection.setRequestMethod("GET");

            Scanner scanner = new Scanner(new InputStreamReader(connection.getInputStream()));
            latestVersion = scanner.nextLine().trim();
            scanner.close();

        } catch (Exception e) {
            plugin.getLogger().warning("Update check failed!");
        }
    }

    public void notifyAllPlayers() {

        if (latestVersion == null) return;

        String current = plugin.getDescription().getVersion();

        if (current.equalsIgnoreCase(latestVersion)) return;

        String msg = languageManager.getMessage("messages.update-available")
                .replace("%latest%", latestVersion)
                .replace("%current%", current);

        send(msg);
    }

    public void notifyPlayer(Player player) {

        if (latestVersion == null) return;

        String current = plugin.getDescription().getVersion();

        if (current.equalsIgnoreCase(latestVersion)) return;

        String perm = plugin.getConfig().getString("permissions.update-check");

        if (perm != null && !perm.isEmpty() && !player.hasPermission(perm)) return;

        String msg = languageManager.getMessage("messages.update-available")
                .replace("%latest%", latestVersion)
                .replace("%current%", current);

        send(player, msg);
    }

    private void send(String msg) {

        String type = plugin.getConfig().getString("update-check.messageType");

        if ("console".equalsIgnoreCase(type)) {
            Bukkit.getConsoleSender().sendMessage(msg);
            return;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            send(player, msg);
        }
    }

    private void send(Player player, String msg) {

        String type = plugin.getConfig().getString("update-check.messageType");

        if ("actionbar".equalsIgnoreCase(type)) {
            player.spigot().sendMessage(
                    ChatMessageType.ACTION_BAR,
                    new TextComponent(msg)
            );
        } else {
            player.sendMessage(msg);
        }
    }
}
