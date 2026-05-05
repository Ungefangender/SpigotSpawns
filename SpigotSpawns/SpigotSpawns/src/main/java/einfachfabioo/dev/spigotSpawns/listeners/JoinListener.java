package einfachfabioo.dev.spigotSpawns.listeners;

import einfachfabioo.dev.spigotSpawns.SpigotSpawns;
import einfachfabioo.dev.spigotSpawns.managers.LanguageManager;
import einfachfabioo.dev.spigotSpawns.managers.LocationManager;
import einfachfabioo.dev.spigotSpawns.managers.PermissionManager;
import einfachfabioo.dev.spigotSpawns.utils.UpdateChecker;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    private final SpigotSpawns plugin;
    private final LocationManager locationManager;
    private final LanguageManager languageManager;
    private final PermissionManager permissionManager;
    private final UpdateChecker updateChecker;

    public JoinListener(SpigotSpawns plugin) {
        this.plugin = plugin;
        this.locationManager = plugin.getLocationManager();
        this.languageManager = plugin.getLanguageManager();
        this.permissionManager = plugin.getPermissionManager();
        this.updateChecker = plugin.getUpdateChecker();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();

        // ❌ Vanilla Join Message entfernen
        event.setJoinMessage(null);

        // ======================
        // 🌍 GLOBAL JOIN MESSAGE
        // ======================

        boolean globalEnabled = plugin.getConfig().getBoolean("server-site-settings.onJoin.global-joinmessage");

        if (globalEnabled && !permissionManager.has(player, "silent.join")) {

            String msg = languageManager.getMessage("messages.global-join")
                    .replace("%player%", player.getName());

            String type = plugin.getConfig().getString("server-site-settings.onJoin.messageType");

            for (Player all : Bukkit.getOnlinePlayers()) {

                if ("actionbar".equalsIgnoreCase(type)) {
                    all.spigot().sendMessage(
                            ChatMessageType.ACTION_BAR,
                            new TextComponent(msg)
                    );
                } else {
                    all.sendMessage(msg);
                }
            }

            // 🔊 GLOBAL SOUND
            playGlobalJoinSound();
        }

        // ======================
        // 🚀 JOIN TELEPORT
        // ======================

        if (!permissionManager.has(player, "bypass.jointeleport")) {

            boolean enabled = plugin.getConfig().getBoolean("settings.joinTeleport.enabled");

            if (enabled) {

                Location spawn = locationManager.getSpawn();

                if (spawn != null && spawn.getWorld() != null) {

                    player.teleport(spawn);

                    // Message
                    boolean sendMessage = plugin.getConfig().getBoolean("settings.joinTeleport.message");

                    if (sendMessage) {
                        sendJoinTeleportMessage(player);
                    }

                    // Sound
                    playJoinTeleportSound(player);
                }
            }
        }

        // ======================
        // 🔄 UPDATE CHECK (JOIN)
        // ======================

        if (plugin.getConfig().getBoolean("update-check.notify-on-join") && updateChecker != null) {
            updateChecker.notifyPlayer(player);
        }
    }

    // ======================
    // 🔊 GLOBAL JOIN SOUND
    // ======================

    private void playGlobalJoinSound() {

        boolean enabled = plugin.getConfig().getBoolean("server-site-settings.onJoin.sound.enabled");
        if (!enabled) return;

        try {
            String soundName = plugin.getConfig().getString("server-site-settings.onJoin.sound.type");
            float volume = (float) plugin.getConfig().getDouble("server-site-settings.onJoin.sound.volume");
            float pitch = (float) plugin.getConfig().getDouble("server-site-settings.onJoin.sound.pitch");

            Sound sound = Sound.valueOf(soundName.toUpperCase());

            for (Player all : Bukkit.getOnlinePlayers()) {
                all.playSound(all.getLocation(), sound, volume, pitch);
            }

        } catch (Exception e) {
            plugin.getLogger().warning("Invalid join sound!");
        }
    }

    // ======================
    // 💬 JOIN TELEPORT MESSAGE
    // ======================

    private void sendJoinTeleportMessage(Player player) {

        String msg = languageManager.getMessage("messages.join-teleport");
        String type = plugin.getConfig().getString("settings.joinTeleport.messageType");

        if ("actionbar".equalsIgnoreCase(type)) {
            player.spigot().sendMessage(
                    ChatMessageType.ACTION_BAR,
                    new TextComponent(msg)
            );
        } else {
            player.sendMessage(msg);
        }
    }

    // ======================
    // 🔊 JOIN TELEPORT SOUND
    // ======================

    private void playJoinTeleportSound(Player player) {

        boolean enabled = plugin.getConfig().getBoolean("settings.joinTeleport.sound.enabled");
        if (!enabled) return;

        try {
            String soundName = plugin.getConfig().getString("settings.joinTeleport.sound.type");
            float volume = (float) plugin.getConfig().getDouble("settings.joinTeleport.sound.volume");
            float pitch = (float) plugin.getConfig().getDouble("settings.joinTeleport.sound.pitch");

            Sound sound = Sound.valueOf(soundName.toUpperCase());

            player.playSound(player.getLocation(), sound, volume, pitch);

        } catch (Exception e) {
            plugin.getLogger().warning("Invalid join teleport sound!");
        }
    }
}
