package net.pistonmaster.oakillcmd;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Some code stolen from: https://www.spigotmc.org/resources/oldschoolkill.4047/
 * :D
 */
public final class OAKillCmd extends JavaPlugin implements Listener {
    private final Map<UUID, Instant> onCoolDown = new HashMap<>();

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
        if (player.hasPermission("minecraft.command.kill")) {
            return;
        }

        event.setCancelled(true);
        if (onCoolDown.containsKey(player.getUniqueId())) {
            if (Duration.between(Instant.now(), onCoolDown.get(player.getUniqueId())).getSeconds() <= 0) {
                onCoolDown.remove(player.getUniqueId());
            } else {
                sendCoolDownMessage(player);
                return;
            }
        }

        player.setHealth(0);
        long timeToWait = getTimeForPlayer(player);

        onCoolDown.put(player.getUniqueId(), Instant.now().plusSeconds(player.getBedSpawnLocation() == null ? getNoSpawnTime() : getSpawnTime()));

        getServer().getScheduler().runTaskLaterAsynchronously(this, () -> onCoolDown.remove(player.getUniqueId()), timeToWait);
    }

    private long getTimeForPlayer(Player player) {
        if (player.getBedSpawnLocation() == null) {
            return getNoSpawnTime() * 20;
        } else {
            return getSpawnTime() * 20;
        }
    }

    private long getNoSpawnTime() {
        return getConfig().getLong("nospawntime");
    }

    private long getSpawnTime() {
        return getConfig().getLong("spawntime");
    }

    private void sendCoolDownMessage(Player player) {
        String msg = ChatColor.translateAlternateColorCodes('&', player.getBedSpawnLocation() == null ? getConfig().getString("nospawnmessage") : getConfig().getString("spawnmessage"));

        player.sendMessage(msg.replace("%time%", String.valueOf(Duration.between(Instant.now(), onCoolDown.get(player.getUniqueId())).getSeconds())));
    }
}
