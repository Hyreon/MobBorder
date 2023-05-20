package top.hyreon.mobBorder;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.HashMap;
import java.util.logging.Level;

public class LanguageLoader {

    Plugin plugin;
    HashMap<String, String> translationMap = new HashMap<>();

    public LanguageLoader(Plugin plugin) {

        this.plugin = plugin;
        plugin.getConfig().addDefault("default-color", "&f");

        File languageDirectory = new File(plugin.getDataFolder(), "lang/");
        if (!languageDirectory.isDirectory()) {
            languageDirectory.mkdir();
        }
        plugin.saveResource("lang/default.yml", true);
        File defaultLanguageFile = new File(plugin.getDataFolder(), "lang/default.yml");
        if (plugin.getConfig().getString("locale") != null && !plugin.getConfig().getString("locale").equals("default")) {
            Bukkit.getLogger().log(Level.INFO, "Loading custom lang file " + "lang/" + plugin.getConfig().getString("locale") + ".yml");
            FileConfiguration translations = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "lang/" + plugin.getConfig().getString("locale") + ".yml"));
            for (String translation : translations.getKeys(false)){
                translationMap.put(translation, translations.getString(translation));
            }
        } else {
            Bukkit.getLogger().log(Level.INFO, "Loading default lang file");
            FileConfiguration translations = YamlConfiguration.loadConfiguration(defaultLanguageFile);
            for (String translation : translations.getKeys(false)) {
                translationMap.put(translation, translations.getString(translation));
            }
        }

    }

    public String get(String path, boolean prefix) {
        String result = translationMap.get(path);
        if (prefix) result = "&x" + result;
        result = result.replace("&x", plugin.getConfig().getString("default-color", "&f"));
        return ChatColor.translateAlternateColorCodes('&', result);
    }

    public String get(String path) {
        return this.get(path, true);
    }
}