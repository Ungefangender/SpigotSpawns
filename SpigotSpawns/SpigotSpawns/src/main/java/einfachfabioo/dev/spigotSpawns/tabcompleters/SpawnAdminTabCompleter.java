package einfachfabioo.dev.spigotSpawns.tabcompleters;

import org.bukkit.command.TabCompleter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

public class SpawnAdminTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        if (args.length == 1) {
            return Arrays.asList("setspawn", "delspawn", "reload", "spawninfo");
        }

        return null;
    }
}
