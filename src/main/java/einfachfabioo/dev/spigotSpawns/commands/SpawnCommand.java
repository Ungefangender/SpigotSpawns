package einfachfabioo.dev.spigotSpawns.commands;

import einfachfabioo.dev.spigotSpawns.SpigotSpawns;
import einfachfabioo.dev.spigotSpawns.managers.LanguageManager;
import einfachfabioo.dev.spigotSpawns.managers.LocationManager;
import einfachfabioo.dev.spigotSpawns.managers.PermissionManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;

public class SpawnCommand implements CommandExecutor {

    private final SpigotSpawns plugin;
    private final LocationManager locationManager;
    private final PermissionManager permissionManager;

    private final Map<UUID, Integer> warmupTasks = new HashMap<>();
    private final Map<UUID, Long> cooldownMap = new HashMap<>();

    private final Map<UUID, Integer> protectionTasks = new HashMap<>();
    private final Set<UUID> protectedPlayers = new HashSet<>();

    public SpawnCommand(SpigotSpawns plugin) {
        this.plugin = plugin;
        this.locationManager = plugin.getLocationManager();
        this.permissionManager = plugin.getPermissionManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players!");
            return true;
        }

        if (!permissionManager.has(player, "use")) {
            player.sendMessage(getLanguageManager().getMessage("messages.no-permission"));
            return true;
        }

        Location spawn = locationManager.getSpawn();

        if (spawn == null || spawn.getWorld() == null) {
            player.sendMessage(getLanguageManager().getMessage("messages.no-spawn"));
            return true;
        }

        UUID uuid = player.getUniqueId();

        // ❗ Already Warmup (WICHTIG: muss ganz oben sein)
        if (warmupTasks.containsKey(uuid)) {
            player.sendMessage(getLanguageManager().getMessage("messages.already-warmup"));
            return true;
        }

        // ⏳ COOLDOWN
        if (plugin.getConfig().getBoolean("settings.cooldown.enabled")
                && !permissionManager.has(player, "bypass.cooldown")) {

            int cooldownSeconds = plugin.getConfig().getInt("settings.cooldown.seconds");
            String type = plugin.getConfig().getString("settings.cooldown.messageType");

            if (cooldownMap.containsKey(uuid)) {
                long timeLeft = cooldownSeconds - ((System.currentTimeMillis() - cooldownMap.get(uuid)) / 1000);

                if (timeLeft > 0) {
                    String msg = getLanguageManager().getMessage("messages.cooldown")
                            .replace("%seconds%", String.valueOf(timeLeft));

                    send(player, msg, type);
                    playSound(player, "settings.cooldown.sound");
                    return true;
                }
            }
        }

        // ⏱ Warmup starten
        boolean warmupEnabled = plugin.getConfig().getBoolean("settings.warmup.enabled");

        // ⚡ Bypass oder deaktiviert → sofort TP
        if (!warmupEnabled || permissionManager.has(player, "bypass.warmup")) {
            teleport(player, spawn, uuid);
            return true;
        }

        // ❗ Ab hier ist Warmup garantiert aktiv
        int seconds = plugin.getConfig().getInt("settings.warmup.seconds");
        String type = plugin.getConfig().getString("settings.warmup.messageType");

        int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {

            int timeLeft = seconds;

            @Override
            public void run() {

                if (timeLeft <= 0) {

                    teleport(player, spawn, uuid);

                    Integer id = warmupTasks.remove(uuid);
                    if (id != null) Bukkit.getScheduler().cancelTask(id);

                    return;
                }

                String msg = getLanguageManager().getMessage("messages.warmup")
                        .replace("%seconds%", String.valueOf(timeLeft));

                send(player, msg, type);
                playSound(player, "settings.warmup.sound");

                timeLeft--;
            }

        }, 0L, 20L);

        warmupTasks.put(uuid, taskId);

        return true;
    }

    private void teleport(Player player, Location spawn, UUID uuid) {

        player.teleport(spawn);

        playTeleportSound(player);
        spawnParticles(player);

        player.sendMessage(getLanguageManager().getMessage("messages.teleport"));

        cooldownMap.put(uuid, System.currentTimeMillis());

        startProtection(player);
    }

    private void startProtection(Player player) {

        if (!plugin.getConfig().getBoolean("settings.spawn-protection.enabled")) return;

        UUID uuid = player.getUniqueId();

        int seconds = plugin.getConfig().getInt("settings.spawn-protection.seconds");
        String type = plugin.getConfig().getString("settings.spawn-protection.messageType");

        protectedPlayers.add(uuid);

        if (protectionTasks.containsKey(uuid)) {
            Bukkit.getScheduler().cancelTask(protectionTasks.get(uuid));
        }

        int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {

            int timeLeft = seconds;

            @Override
            public void run() {

                if (timeLeft <= 0) {

                    protectedPlayers.remove(uuid);

                    player.sendMessage(getLanguageManager().getMessage("messages.protection-end"));

                    Integer id = protectionTasks.remove(uuid);
                    if (id != null) Bukkit.getScheduler().cancelTask(id);

                    return;
                }

                String msg = getLanguageManager().getMessage("messages.protection")
                        .replace("%seconds%", String.valueOf(timeLeft));

                send(player, msg, type);

                timeLeft--;
            }

        }, 0L, 20L);

        protectionTasks.put(uuid, taskId);
    }

    public boolean isProtected(Player player) {
        return protectedPlayers.contains(player.getUniqueId());
    }

    public void cancelWarmup(Player player) {
        UUID uuid = player.getUniqueId();

        Integer id = warmupTasks.remove(uuid);

        if (id != null) {
            Bukkit.getScheduler().cancelTask(id);
            player.sendMessage(getLanguageManager().getMessage("messages.cancelled"));
        }
    }

    private void playSound(Player player, String path) {

        if (!plugin.getConfig().getBoolean(path + ".enabled")) return;

        try {
            String soundName = plugin.getConfig().getString(path + ".type");
            float volume = (float) plugin.getConfig().getDouble(path + ".volume");
            float pitch = (float) plugin.getConfig().getDouble(path + ".pitch");

            Sound sound = Sound.valueOf(soundName.toUpperCase());

            player.playSound(player.getLocation(), sound, volume, pitch);

        } catch (Exception e) {
            plugin.getLogger().warning("Invalid sound: " + path);
        }
    }

    private void playTeleportSound(Player player) {
        playSound(player, "settings.onTeleport.sound");
    }

    private void spawnParticles(Player player) {

        if (!plugin.getConfig().getBoolean("settings.onTeleport.particles.enabled")) return;

        try {
            Particle particle = Particle.valueOf(
                    plugin.getConfig().getString("settings.onTeleport.particles.type").toUpperCase()
            );

            player.getWorld().spawnParticle(
                    particle,
                    player.getLocation(),
                    plugin.getConfig().getInt("settings.onTeleport.particles.count"),
                    plugin.getConfig().getDouble("settings.onTeleport.particles.offsetX"),
                    plugin.getConfig().getDouble("settings.onTeleport.particles.offsetY"),
                    plugin.getConfig().getDouble("settings.onTeleport.particles.offsetZ")
            );

        } catch (Exception e) {
            plugin.getLogger().warning("Invalid particle type!");
        }
    }

    private void send(Player player, String msg, String type) {

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
    // 🔄 GET LANGUAGE MANAGER
    // ======================

    private LanguageManager getLanguageManager() {
        return plugin.getLanguageManager();
    }
}
