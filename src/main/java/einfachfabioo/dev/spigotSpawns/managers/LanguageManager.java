package einfachfabioo.dev.spigotSpawns.managers;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class LanguageManager {

    private final JavaPlugin plugin;
    private FileConfiguration messages;
    private String currentLanguage;

    public LanguageManager(JavaPlugin plugin) {
        this.plugin = plugin;

        saveDefaultLanguages();
        loadLanguage();
    }

    // ======================
    // 📂 CREATE FILES
    // ======================

    private void saveDefaultLanguages() {

        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        File langFolder = new File(plugin.getDataFolder(), "languages");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }

        saveIfNotExists("languages/EN.yml");
        saveIfNotExists("languages/DE.yml");
        saveIfNotExists("languages/FR.yml");
        saveIfNotExists("languages/ES.yml");
        saveIfNotExists("languages/IT.yml");
        saveIfNotExists("languages/NL.yml");
        saveIfNotExists("languages/PL.yml");
        saveIfNotExists("languages/PT.yml");
        saveIfNotExists("languages/RU.yml");
        saveIfNotExists("languages/TR.yml");
        saveIfNotExists("languages/CZ.yml");
        saveIfNotExists("languages/SK.yml");
        saveIfNotExists("languages/HU.yml");
        saveIfNotExists("languages/RO.yml");
        saveIfNotExists("languages/BG.yml");
        saveIfNotExists("languages/GR.yml");
        saveIfNotExists("languages/SE.yml");
        saveIfNotExists("languages/NO.yml");
        saveIfNotExists("languages/DK.yml");
        saveIfNotExists("languages/FI.yml");
    }

    private void saveIfNotExists(String path) {
        File file = new File(plugin.getDataFolder(), path);

        if (!file.exists()) {
            plugin.saveResource(path, false);
        }
    }

    // ======================
    // 🌍 LOAD LANGUAGE
    // ======================

    public void loadLanguage() {

        String lang = plugin.getConfig().getString("language", "EN");
        
        // Null-Check
        if (lang == null || lang.isEmpty()) {
            lang = "EN";
        }
        
        currentLanguage = lang.toUpperCase();

        String fileName = "languages/" + currentLanguage + ".yml";
        File file = new File(plugin.getDataFolder(), fileName);

        if (!file.exists()) {

            plugin.getLogger().warning("Language file not found: " + fileName + " -> fallback to EN");

            currentLanguage = "EN";
            file = new File(plugin.getDataFolder(), "languages/EN.yml");

            if (!file.exists()) {
                plugin.getLogger().severe("Fallback language EN.yml is missing!");
                return;
            }
        }

        messages = YamlConfiguration.loadConfiguration(file);
    }

    public void reload() {
        loadLanguage();
    }

    // ======================
    // 📊 GET CURRENT LANG
    // ======================

    public String getCurrentLanguage() {
        return currentLanguage;
    }

    // ======================
    // 💬 GET MESSAGE
    // ======================

    public String getMessage(String path) {

        if (messages == null) {
            return "§cLanguage not loaded!";
        }

        if (!messages.contains(path)) {
            return "§cMissing message: " + path;
        }

        String msg = messages.getString(path);

        if (msg == null) {
            return "§cMissing message: " + path;
        }

        msg = ChatColor.translateAlternateColorCodes('&', msg);

        String prefix = messages.getString("prefix", "");
        prefix = ChatColor.translateAlternateColorCodes('&', prefix);

        return prefix + msg;
    }

    // ======================
    // 💬 WITHOUT PREFIX
    // ======================

    public String getRawMessage(String path) {

        if (messages == null || !messages.contains(path)) {
            return "§cMissing message: " + path;
        }

        String msg = messages.getString(path);

        if (msg == null) {
            return "§cMissing message: " + path;
        }

        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}
