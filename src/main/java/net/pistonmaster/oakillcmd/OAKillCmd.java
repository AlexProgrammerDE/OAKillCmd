package net.pistonmaster.oakillcmd;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Some code stolen from: https://www.spigotmc.org/resources/oldschoolkill.4047/
 * :D
 */
public final class OAKillCmd extends JavaPlugin implements Listener {
    private final List<Player> onCoolDown = new ArrayList<>();

    @Override
    public void onEnable() {
        getLogger().info(ChatColor.AQUA + "Loading config");
        saveDefaultConfig();

        getLogger().info(ChatColor.AQUA + "Registering events");
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info(ChatColor.AQUA + "Done! :D");
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        String cmd = event.getMessage().toLowerCase();
        if ((!cmd.startsWith("/kill ")) && (!cmd.equals("/kill"))) {
            return;
        }

        Player player = event.getPlayer();
        if (onCoolDown.contains(player)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("cooldownmessage")));
            return;
        }

        if (player.hasPermission("minecraft.command.kill")) {
            return;
        }

        player.setHealth(0);
        event.setCancelled(true);

        onCoolDown.add(player);

        getServer().getScheduler().runTaskLaterAsynchronously(this, () -> onCoolDown.remove(player), getTimeForPlayer(player));
    }

    private long getTimeForPlayer(Player player) {
        if (player.getBedSpawnLocation() == null) {
            return getConfig().getLong("nospawntime") * 20;
        } else {
            return getConfig().getLong("spawntime") * 20;
        }
    }
}
