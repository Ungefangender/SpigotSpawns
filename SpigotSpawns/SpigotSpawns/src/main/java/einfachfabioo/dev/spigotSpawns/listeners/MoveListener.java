package einfachfabioo.dev.spigotSpawns.listeners;

import einfachfabioo.dev.spigotSpawns.commands.SpawnCommand;
import einfachfabioo.dev.spigotSpawns.managers.PermissionManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class MoveListener implements Listener {

    private final SpawnCommand spawnCommand;
    private final PermissionManager permissionManager;
    private final boolean cancelOnMove;

    public MoveListener(SpawnCommand spawnCommand, boolean cancelOnMove, PermissionManager permissionManager) {
        this.spawnCommand = spawnCommand;
        this.cancelOnMove = cancelOnMove;
        this.permissionManager = permissionManager;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {

        if (!cancelOnMove) return;

        Player player = event.getPlayer();

        // 🔐 Bypass Permission
        if (permissionManager.has(player, "bypass-move")) return;

        // Nur echte Bewegung (nicht Kopf drehen)
        if (event.getFrom().getX() != event.getTo().getX()
                || event.getFrom().getY() != event.getTo().getY()
                || event.getFrom().getZ() != event.getTo().getZ()) {

            spawnCommand.cancelWarmup(player);
        }
    }
}
