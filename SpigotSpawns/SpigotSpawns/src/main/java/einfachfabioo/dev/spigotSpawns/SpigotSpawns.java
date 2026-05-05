package einfachfabioo.dev.spigotSpawns;

import einfachfabioo.dev.spigotSpawns.commands.SpawnAdminCommand;
import einfachfabioo.dev.spigotSpawns.commands.SpawnCommand;
import einfachfabioo.dev.spigotSpawns.listeners.JoinListener;
import einfachfabioo.dev.spigotSpawns.listeners.ProtectionListener;
import einfachfabioo.dev.spigotSpawns.listeners.MoveListener;
import einfachfabioo.dev.spigotSpawns.managers.LanguageManager;
import einfachfabioo.dev.spigotSpawns.managers.LocationManager;
import einfachfabioo.dev.spigotSpawns.managers.PermissionManager;
import einfachfabioo.dev.spigotSpawns.tabcompleters.SpawnAdminTabCompleter;
import einfachfabioo.dev.spigotSpawns.utils.UpdateChecker;
import org.bukkit.plugin.java.JavaPlugin;

public class SpigotSpawns extends JavaPlugin {

    private LanguageManager languageManager;
    private LocationManager locationManager;
    private PermissionManager permissionManager;

    private SpawnCommand spawnCommand;
    private UpdateChecker updateChecker;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        languageManager = new LanguageManager(this);
        locationManager = new LocationManager(this);
        permissionManager = new PermissionManager(this);

        // UPDATE CHECKER
        updateChecker = new UpdateChecker(this);
        updateChecker.start();

        getLogger().info("Language loaded: " + languageManager.getCurrentLanguage());

        // COMMANDS
        spawnCommand = new SpawnCommand(this);

        if (getCommand("spawn") != null) {
            getCommand("spawn").setExecutor(spawnCommand);
        }

        SpawnAdminCommand adminCommand = new SpawnAdminCommand(this);

        if (getCommand("spawnadmin") != null) {
            getCommand("spawnadmin").setExecutor(adminCommand);
            getCommand("spawnadmin").setTabCompleter(new SpawnAdminTabCompleter());
        }

        // LISTENER

        getServer().getPluginManager().registerEvents(
                new JoinListener(this),
                this
        );

        boolean cancelOnMove = getConfig().getBoolean("settings.warmup.cancelOnMove");

        getServer().getPluginManager().registerEvents(
                new MoveListener(spawnCommand, cancelOnMove, permissionManager),
                this
        );

        getServer().getPluginManager().registerEvents(
                new ProtectionListener(spawnCommand),
                this
        );
    }
    public UpdateChecker getUpdateChecker() {
        return updateChecker;
    }

    @Override
    public void onDisable() {
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public LocationManager getLocationManager() {
        return locationManager;
    }

    public PermissionManager getPermissionManager() {
        return permissionManager;
    }
}
