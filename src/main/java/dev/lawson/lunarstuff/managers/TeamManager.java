package dev.lawson.lunarstuff.managers;

import com.lunarclient.apollo.Apollo;
import com.lunarclient.apollo.common.location.ApolloLocation;
import com.lunarclient.apollo.module.team.TeamMember;
import com.lunarclient.apollo.module.team.TeamModule;
import dev.lawson.lunarstuff.Lunarstuff;
import com.booksaw.betterTeams.Team;  // BetterTeams Team class
import com.booksaw.betterTeams.customEvents.PlayerJoinTeamEvent;
import com.booksaw.betterTeams.customEvents.PlayerLeaveTeamEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.*;

public class TeamManager implements Listener {
    private final TeamModule teamModule;
    // Map BetterTeams team name -> Apollo TeamGroup
    private final Map<String, TeamGroup> teamsByName = new HashMap<>();
    // Map player UUID -> Apollo TeamGroup (for quick lookup)
    private final Map<UUID, TeamGroup> teamByPlayer = new HashMap<>();

    public TeamManager(Lunarstuff plugin) {
        this.teamModule = Apollo.getModuleManager().getModule(TeamModule.class);
        // Start the periodic task to update team members' locations
        startTeamUpdateTask();
    }

    private void startTeamUpdateTask() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(Lunarstuff.getInstance(), () -> {
            teamsByName.values().forEach(TeamGroup::refresh);
        }, 1L, 1L);
    }

    // Called when a player (re)joins a team or logs in to add them to the appropriate Apollo team group.
    private void addPlayerToTeam(Player player, Team betterTeam) {
        if (betterTeam == null) return;
        String teamName = betterTeam.getName();
        TeamGroup group = teamsByName.get(teamName);
        if (group == null) {
            // Create new Apollo team group for this BetterTeams team
            group = new TeamGroup(betterTeam);
            teamsByName.put(teamName, group);
        }
        group.addMember(player);
    }

    // Called when a player leaves a team or quits to remove them from their Apollo team group.
    private void removePlayerFromTeam(Player player) {
        TeamGroup group = teamByPlayer.get(player.getUniqueId());
        if (group != null) {
            group.removeMember(player);
            // If that team group is now empty, remove it from the map
            if (group.getMembers().isEmpty()) {
                teamsByName.values().remove(group);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!Lunarstuff.getInstance().getConfig().getBoolean("staff-mode.enabled")) {
            // If team sync is disabled in config, do nothing
            return;
        }
        Player player = event.getPlayer();
        Team betterTeam = Team.getTeam(player);  // BetterTeams API: get the team the player is in (null if none)
        if (betterTeam != null) {
            addPlayerToTeam(player, betterTeam);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        // Remove from any Apollo team group
        removePlayerFromTeam(player);
    }

    @EventHandler
    public void onTeamJoin(PlayerJoinTeamEvent event) {
        // Fired when a player joins a team in BetterTeams
        Player player = event.getPlayer().getPlayer();
        Team newTeam = event.getTeam(); // the team the player joined
        addPlayerToTeam(player, newTeam);
        // Optionally, update nametag as well (handled in NametagManager)
    }

    @EventHandler
    public void onTeamLeave(PlayerLeaveTeamEvent event) {
        // Fired when a player leaves or is removed from a team
        Player player = event.getPlayer().getPlayer();
        removePlayerFromTeam(player);
        // Optionally, update nametag (handled in NametagManager)
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        removePlayerFromTeam(player);
    }

    /** Reset all Apollo team data for all players (clears team markers on client side). */
    public void resetAllTeams() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Apollo.getPlayerManager().getPlayer(player.getUniqueId())
                    .ifPresent(ap -> teamModule.resetTeamMembers(ap));
        }
        teamsByName.clear();
        teamByPlayer.clear();
    }

    /**
     * Inner class representing an Apollo team group (wrapping BetterTeams' team).
     * Holds a set of online players and handles Apollo updates for them.
     */
    private class TeamGroup {
        private final UUID teamId;
        private final Set<Player> members;
        private final Team betterTeam;

        TeamGroup(Team betterTeam) {
            this.teamId = UUID.randomUUID();
            this.members = new HashSet<>();
            this.betterTeam = betterTeam;
        }

        void addMember(Player player) {
            members.add(player);
            teamByPlayer.put(player.getUniqueId(), this);
        }

        void removeMember(Player player) {
            members.remove(player);
            teamByPlayer.remove(player.getUniqueId());
            // Remove all team info from this player's client
            Apollo.getPlayerManager().getPlayer(player.getUniqueId())
                    .ifPresent(teamModule::resetTeamMembers);
        }

        Set<Player> getMembers() {
            return members;
        }

        // Create a TeamMember object for a given player (with location and display name)
        private TeamMember createTeamMember(Player p) {
            Location loc = p.getLocation();
            return TeamMember.builder()
                    .playerUuid(p.getUniqueId())
                    .displayName(Component.text(p.getName()).color(NamedTextColor.WHITE))
                    .markerColor(getTeamColor(betterTeam))
                    .location(ApolloLocation.builder()
                            .world(loc.getWorld().getName())
                            .x(loc.getX()).y(loc.getY()).z(loc.getZ())
                            .build())
                    .build();
        }

        // Update all members' Apollo team view with current teammates
        void refresh() {
            // Prepare list of TeamMember info for all online members
            List<TeamMember> teammateInfo = new ArrayList<>();
            for (Player member : members) {
                if (member.isOnline()) {
                    teammateInfo.add(createTeamMember(member));
                }
            }
            // Send the updated team list to each member's Apollo client
            for (Player member : members) {
                if (!member.isOnline()) continue;
                Apollo.getPlayerManager().getPlayer(member.getUniqueId()).ifPresent(ap ->
                        teamModule.updateTeamMembers(ap, teammateInfo));
            }
        }
    }

    public void reloadAllTeams() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Team betterTeam = Team.getTeam(player);
            if (betterTeam != null) {
                addPlayerToTeam(player, betterTeam);
            } else {
                removePlayerFromTeam(player);
            }
        }
    }

    /**
     * Retrieves the team's color from BetterTeams and converts it to a java.awt.Color.
     * If the team color is not set or invalid, defaults to white.
     */
    private java.awt.Color getTeamColor(Team team) {
        if (team == null) return java.awt.Color.WHITE;
        ChatColor chatColor = team.getColor();
        if (chatColor == null) return java.awt.Color.WHITE;

        // Map ChatColor to java.awt.Color
        switch (chatColor) {
            case BLACK:         return new java.awt.Color(0, 0, 0);
            case DARK_BLUE:     return new java.awt.Color(0, 0, 170);
            case DARK_GREEN:    return new java.awt.Color(0, 170, 0);
            case DARK_AQUA:     return new java.awt.Color(0, 170, 170);
            case DARK_RED:      return new java.awt.Color(170, 0, 0);
            case DARK_PURPLE:   return new java.awt.Color(170, 0, 170);
            case GOLD:          return new java.awt.Color(255, 170, 0);
            case GRAY:          return new java.awt.Color(170, 170, 170);
            case DARK_GRAY:     return new java.awt.Color(85, 85, 85);
            case BLUE:          return new java.awt.Color(85, 85, 255);
            case GREEN:         return new java.awt.Color(85, 255, 85);
            case AQUA:          return new java.awt.Color(85, 255, 255);
            case RED:           return new java.awt.Color(255, 85, 85);
            case LIGHT_PURPLE:  return new java.awt.Color(255, 85, 255);
            case YELLOW:        return new java.awt.Color(255, 255, 85);
            case WHITE:         return new java.awt.Color(255, 255, 255);
            default:            return java.awt.Color.WHITE;
        }
    }
}
