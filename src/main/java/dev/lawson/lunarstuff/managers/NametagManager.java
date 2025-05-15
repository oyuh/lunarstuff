package dev.lawson.lunarstuff.managers;

import com.booksaw.betterTeams.customEvents.PlayerJoinTeamEvent;
import com.booksaw.betterTeams.customEvents.PlayerLeaveTeamEvent;
import com.lunarclient.apollo.Apollo;
import com.lunarclient.apollo.module.nametag.NametagModule;
import com.lunarclient.apollo.module.nametag.Nametag;
import com.lunarclient.apollo.recipients.Recipients;
import dev.lawson.lunarstuff.Lunarstuff;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.stream.Collectors;

public class NametagManager implements Listener {
    private final NametagModule nametagModule;
    private final List<String> nameTagLines;  // From config
    private BukkitTask updateTask;

    public NametagManager(Lunarstuff plugin) {
        // Get Apollo's Nametag module
        this.nametagModule = Apollo.getModuleManager().getModule(NametagModule.class);
        this.nameTagLines = plugin.getConfig().getStringList("nametag.lines");
    }

    // Applies the custom nametag for a specific player
    private void applyNametag(Player player) {
        if (!player.isOnline() || nameTagLines.isEmpty() || nametagModule == null) {
            return;
        }

        try {
            List<Component> lineComponents = nameTagLines.stream().map(line -> {
                // 1. Original config line

                String parsed = line;
                // 2. PlaceholderAPI replacement
                if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                    parsed = PlaceholderAPI.setPlaceholders(player, parsed);
                } else {
                    Bukkit.getLogger().info("[LunarStuff][Nametag DEBUG] PlaceholderAPI NOT enabled.");
                }
                // 3. Color code translation
                parsed = ChatColor.translateAlternateColorCodes('&', parsed);

                // 4. Adventure Component conversion
                Component comp = LegacyComponentSerializer.legacySection().deserialize(parsed);

                return comp;
            }).collect(Collectors.toList());

            // Build Nametag with these components
            Nametag customTag = Nametag.builder().lines(lineComponents).build();
            // Set nametag for everyone
            nametagModule.overrideNametag(Recipients.ofEveryone(), player.getUniqueId(), customTag);


        } catch (Exception e) {
            Bukkit.getLogger().warning("[LunarStuff] Failed to apply nametag for " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!Lunarstuff.getInstance().getConfig().getBoolean("nametag.enabled")) return;
        final Player player = event.getPlayer();
        // Short delay (Apollo and client handshake must finish)
        Bukkit.getScheduler().runTaskLater(Lunarstuff.getInstance(), () -> applyNametag(player), 40L); // 2 seconds
    }

    @EventHandler
    public void onTeamJoin(PlayerJoinTeamEvent event) {
        Player player = event.getPlayer().getPlayer();
        if (player != null) applyNametag(player);
    }

    @EventHandler
    public void onTeamLeave(PlayerLeaveTeamEvent event) {
        Player player = event.getPlayer().getPlayer();
        if (player != null) applyNametag(player);
    }

    /** Starts the repeating nametag update task (call from onEnable). */
    public void startNametagUpdateTask() {
        if (updateTask != null && !updateTask.isCancelled()) {
            updateTask.cancel();
        }
        updateTask = Bukkit.getScheduler().runTaskTimerAsynchronously(
                Lunarstuff.getInstance(),
                () -> {
                    if (!Lunarstuff.getInstance().getConfig().getBoolean("nametag.enabled")) return;
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        applyNametag(player);
                    }
                },
                0L,  // initial delay
                10L  // repeat every 10 ticks (0.5 seconds)
        );
        Bukkit.getLogger().info("[LunarStuff] Started nametag update task!");
    }

    /** Stops the repeating nametag update task (call from onDisable). */
    public void stopNametagUpdateTask() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
            Bukkit.getLogger().info("[LunarStuff] Stopped nametag update task!");
        }
    }

    /** Reset all nametags for all players (use on plugin disable). */
    public void resetAllNametags() {
        if (nametagModule == null) return;
        for (Player player : Bukkit.getOnlinePlayers()) {
            nametagModule.resetNametag(Recipients.ofEveryone(), player.getUniqueId());
        }
    }
}
