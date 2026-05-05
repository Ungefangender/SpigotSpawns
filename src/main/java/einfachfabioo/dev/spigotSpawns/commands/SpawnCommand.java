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
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class SpawnCommand implements CommandExecutor {

    private final SpigotSpawns plugin;
    private final LocationManager locationManager;
    private final PermissionManager permissionManager;

    // Task Management
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
        // Check if sender is player
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        // Check permissions
        if (!permissionManager.has(player, "use")) {
            player.sendMessage(getLanguageManager().getMessage("messages.no-permission"));
            return true;
        }

        // Check if spawn is set
        Location spawn = locationManager.getSpawn();
        if (spawn == null || spawn.getWorld() == null) {
            player.sendMessage(getLanguageManager().getMessage("messages.no-spawn"));
            return true;
        }

        UUID uuid = player.getUniqueId();

        // Check if player is already in warmup
        if (warmupTasks.containsKey(uuid)) {
            player.sendMessage(getLanguageManager().getMessage("messages.already-warmup"));
            return true;
        }

        // Check cooldown
        if (!checkCooldown(player, uuid)) {
            return true;
        }

        // Check if warmup is enabled
        boolean warmupEnabled = plugin.getConfig().getBoolean("settings.warmup.enabled");
        boolean hasBypassWarmup = permissionManager.has(player, "bypass.warmup");

        if (!warmupEnabled || hasBypassWarmup) {
            // Direct teleport without warmup
            performTeleport(player, spawn, uuid);
            return true;
        }

        // Start warmup
        startWarmup(player, spawn, uuid);
        return true;
    }

    private boolean checkCooldown(Player player, UUID uuid) {
        boolean cooldownEnabled = plugin.getConfig().getBoolean("settings.cooldown.enabled");
        
        if (!cooldownEnabled) {
            return true;
        }

        boolean hasBypassCooldown = permissionManager.has(player, "bypass.cooldown");
        if (hasBypassCooldown) {
            return true;
        }

        int cooldownSeconds = plugin.getConfig().getInt("settings.cooldown.seconds");
        String messageType = plugin.getConfig().getString("settings.cooldown.messageType");

        if (!cooldownMap.containsKey(uuid)) {
            return true;
        }

        long timePassed = (System.currentTimeMillis() - cooldownMap.get(uuid)) / 1000;
        long timeLeft = cooldownSeconds - timePassed;

        if (timeLeft > 0) {
            String msg = getLanguageManager().getMessage("messages.cooldown")
                    .replace("%seconds%", String.valueOf(timeLeft));
            sendMessage(player, msg, messageType);
            playSound(player, "settings.cooldown.sound");
            return false;
        }

        return true;
    }

    private void startWarmup(Player player, Location spawn, UUID uuid) {
        int warmupSeconds = plugin.getConfig().getInt("settings.warmup.seconds");
        String messageType = plugin.getConfig().getString("settings.warmup.messageType");

        int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            int secondsLeft = warmupSeconds;

            @Override
            public void run() {
                if (secondsLeft <= 0) {
                    // Warmup finished - perform teleport
                    performTeleport(player, spawn, uuid);
                    warmupTasks.remove(uuid);
                    Bukkit.getScheduler().cancelTask(this.hashCode());
                    return;
                }

                // Send warmup message
                String msg = getLanguageManager().getMessage("messages.warmup")
                        .replace("%seconds%", String.valueOf(secondsLeft));
                sendMessage(player, msg, messageType);
                playSound(player, "settings.warmup.sound");

                secondsLeft--;
            }
        }, 0L, 20L);

        warmupTasks.put(uuid, taskId);
    }

    private void performTeleport(Player player, Location spawn, UUID uuid) {
        // Teleport player
        player.teleport(spawn);

        // Effects
        playTeleportSound(player);
        spawnTeleportParticles(player);

        // Message
        player.sendMessage(getLanguageManager().getMessage("messages.teleport"));

        // Add to cooldown
        cooldownMap.put(uuid, System.currentTimeMillis());

        // Start protection
        startProtection(player, uuid);
    }

    private void startProtection(Player player, UUID uuid) {
        boolean protectionEnabled = plugin.getConfig().getBoolean("settings.spawn-protection.enabled");
        if (!protectionEnabled) {
            return;
        }

        int protectionSeconds = plugin.getConfig().getInt("settings.spawn-protection.seconds");
        String messageType = plugin.getConfig().getString("settings.spawn-protection.messageType");

        protectedPlayers.add(uuid);

        // Cancel existing protection task if any
        if (protectionTasks.containsKey(uuid)) {
            Bukkit.getScheduler().cancelTask(protectionTasks.get(uuid));
        }

        int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            int secondsLeft = protectionSeconds;

            @Override
            public void run() {
                if (secondsLeft <= 0) {
                    // Protection ended
                    protectedPlayers.remove(uuid);
                    player.sendMessage(getLanguageManager().getMessage("messages.protection-end"));
                    protectionTasks.remove(uuid);
                    Bukkit.getScheduler().cancelTask(this.hashCode());
                    return;
                }

                // Send protection message
                String msg = getLanguageManager().getMessage("messages.protection")
                        .replace("%seconds%", String.valueOf(secondsLeft));
                sendMessage(player, msg, messageType);

                secondsLeft--;
            }
        }, 0L, 20L);

        protectionTasks.put(uuid, taskId);
    }

    public boolean isProtected(Player player) {
        return protectedPlayers.contains(player.getUniqueId());
    }

    public void cancelWarmup(Player player) {
        UUID uuid = player.getUniqueId();

        if (warmupTasks.containsKey(uuid)) {
            Integer taskId = warmupTasks.remove(uuid);
            if (taskId != null) {
                Bukkit.getScheduler().cancelTask(taskId);
            }
            player.sendMessage(getLanguageManager().getMessage("messages.cancelled"));
        }
    }

    // ============================
    // HELPER METHODS
    // ============================

    private void sendMessage(Player player, String message, String type) {
        if (message == null || message.isEmpty()) {
            return;
        }

        if ("actionbar".equalsIgnoreCase(type)) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
        } else {
            player.sendMessage(message);
        }
    }

    private void playSound(Player player, String configPath) {
        boolean soundEnabled = plugin.getConfig().getBoolean(configPath + ".enabled");
        if (!soundEnabled) {
            return;
        }

        try {
            String soundName = plugin.getConfig().getString(configPath + ".type");
            double volume = plugin.getConfig().getDouble(configPath + ".volume");
            double pitch = plugin.getConfig().getDouble(configPath + ".pitch");

            if (soundName == null || soundName.isEmpty()) {
                return;
            }

            Sound sound = Sound.valueOf(soundName.toUpperCase());
            player.playSound(player.getLocation(), sound, (float) volume, (float) pitch);

        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid sound type at path: " + configPath);
        } catch (Exception e) {
            plugin.getLogger().warning("Error playing sound at path: " + configPath);
        }
    }

    private void playTeleportSound(Player player) {
        playSound(player, "settings.onTeleport.sound");
    }

    private void spawnTeleportParticles(Player player) {
        boolean particlesEnabled = plugin.getConfig().getBoolean("settings.onTeleport.particles.enabled");
        if (!particlesEnabled) {
            return;
        }

        try {
            String particleType = plugin.getConfig().getString("settings.onTeleport.particles.type");
            if (particleType == null || particleType.isEmpty()) {
                return;
            }

            Particle particle = Particle.valueOf(particleType.toUpperCase());
            int count = plugin.getConfig().getInt("settings.onTeleport.particles.count");
            double offsetX = plugin.getConfig().getDouble("settings.onTeleport.particles.offsetX");
            double offsetY = plugin.getConfig().getDouble("settings.onTeleport.particles.offsetY");
            double offsetZ = plugin.getConfig().getDouble("settings.onTeleport.particles.offsetZ");

            player.getWorld().spawnParticle(particle, player.getLocation(), count, offsetX, offsetY, offsetZ);

        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid particle type!");
        } catch (Exception e) {
            plugin.getLogger().warning("Error spawning particles!");
        }
    }

    private LanguageManager getLanguageManager() {
        return plugin.getLanguageManager();
    }
}
