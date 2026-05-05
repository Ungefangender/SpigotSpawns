package einfachfabioo.dev.spigotSpawns.listeners;

import einfachfabioo.dev.spigotSpawns.commands.SpawnCommand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class ProtectionListener implements Listener {

    private final SpawnCommand spawnCommand;

    public ProtectionListener(SpawnCommand spawnCommand) {
        this.spawnCommand = spawnCommand;
    }

    // 🔥 ALLE DAMAGE ARTEN (Fall, Lava, etc.)
    @EventHandler
    public void onDamage(EntityDamageEvent event) {

        Entity entity = event.getEntity();

        if (!(entity instanceof Player player)) return;

        if (spawnCommand.isProtected(player)) {
            event.setCancelled(true);
        }
    }

    // 🔥 EXTRA: PvP (Spieler vs Spieler)
    @EventHandler
    public void onPvP(EntityDamageByEntityEvent event) {

        if (!(event.getEntity() instanceof Player victim)) return;

        if (spawnCommand.isProtected(victim)) {
            event.setCancelled(true);
            return;
        }

        if (event.getDamager() instanceof Player attacker) {
            if (spawnCommand.isProtected(attacker)) {
                event.setCancelled(true);
            }
        }
    }
}
