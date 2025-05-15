package dev.lawson.lunarstuff.managers;

import com.lunarclient.apollo.Apollo;
import com.lunarclient.apollo.module.richpresence.RichPresenceModule;
import com.lunarclient.apollo.module.richpresence.*;
import dev.lawson.lunarstuff.Lunarstuff;
import com.booksaw.betterTeams.Team;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Optional;

public class RichPresenceManager {
    private final Lunarstuff plugin;
    private final RichPresenceModule richPresenceModule;
    private final long intervalTicks;
    private final String gameName, gameVariant, gameState, playerState, mapName, subServerName;
    private final boolean showTeam;

    public RichPresenceManager(Lunarstuff plugin) {
        this.plugin = plugin;
        this.richPresenceModule = Apollo.getModuleManager().getModule(RichPresenceModule.class);
        // Load config values
        intervalTicks = plugin.getConfig().getLong("richpresence.update-interval", 100L);
        gameName = plugin.getConfig().getString("richpresence.gameName", "");
        gameVariant = plugin.getConfig().getString("richpresence.gameVariantName", "");
        gameState = plugin.getConfig().getString("richpresence.gameState", "");
        playerState = plugin.getConfig().getString("richpresence.playerState", "");
        mapName = plugin.getConfig().getString("richpresence.mapName", "");
        subServerName = plugin.getConfig().getString("richpresence.subServerName", "");
        showTeam = plugin.getConfig().getBoolean("richpresence.showTeam", true);
    }

    /** Starts the periodic rich presence update task (if enabled in config). */
    public void start() {
        if (!plugin.getConfig().getBoolean("richpresence.enabled")) {
            return;
        }
        // Schedule an asynchronous repeating task to update Discord Rich Presence
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> updateAllPlayers(), intervalTicks, intervalTicks);
    }

    private void updateAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Optional<com.lunarclient.apollo.player.ApolloPlayer> apolloOpt = Apollo.getPlayerManager().getPlayer(player.getUniqueId());
            if (!apolloOpt.isPresent()) continue; // Only update Lunar Client players
            com.lunarclient.apollo.player.ApolloPlayer apolloPlayer = apolloOpt.get();
            // Build the rich presence data for this player
            ServerRichPresence.ServerRichPresenceBuilder presenceBuilder = ServerRichPresence.builder();
            // Fill in each field if configured (and replace placeholders)
            if (!gameName.isEmpty()) {
                presenceBuilder.gameName(parsePlaceholders(player, gameName));
            }
            if (!gameVariant.isEmpty()) {
                presenceBuilder.gameVariantName(parsePlaceholders(player, gameVariant));
            }
            if (!gameState.isEmpty()) {
                presenceBuilder.gameState(parsePlaceholders(player, gameState));
            }
            if (!playerState.isEmpty()) {
                presenceBuilder.playerState(parsePlaceholders(player, playerState));
            }
            if (!mapName.isEmpty()) {
                presenceBuilder.mapName(parsePlaceholders(player, mapName));
            }
            if (!subServerName.isEmpty()) {
                presenceBuilder.subServerName(parsePlaceholders(player, subServerName));
            }
            if (showTeam) {
                // If showing team sizes, include team info if player is in a BetterTeams team
                Team team = Team.getTeam(player);
                if (team != null) {
                    // current size = number of players *online* in team (or use total members if preferred)
                    int teamSize = team.getMembers().size(); // (This assumes BetterTeams Team.getMembers() returns a list of all members)
                    int teamMaxSize = 5;  // (Assuming BetterTeams API provides max team size config)
                    presenceBuilder.teamCurrentSize(teamSize);
                    presenceBuilder.teamMaxSize(teamMaxSize);
                }
            }
            // Override the player's rich presence on Lunar Client
            richPresenceModule.overrideServerRichPresence(apolloPlayer, presenceBuilder.build());
        }
    }

    // Utility to parse PAPI placeholders in a string for a given player
    private String parsePlaceholders(Player player, String text) {
        String result = text;
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            result = PlaceholderAPI.setPlaceholders(player, text);
        }
        return result;
    }

    /** Reset rich presence for all Apollo players (on disable). */
    public void resetAllPresence() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Apollo.getPlayerManager().getPlayer(player.getUniqueId())
                    .ifPresent(richPresenceModule::resetServerRichPresence);
        }
    }
}