package dev.lawson.lunarstuff.commands;

import dev.lawson.lunarstuff.managers.TeamManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;

public class RefreshTeamCommand implements CommandExecutor {
    private final TeamManager teamManager;

    public RefreshTeamCommand(TeamManager teamManager) {
        this.teamManager = teamManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("lunarstuff.refreshteam")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }
        teamManager.reloadAllTeams();
        sender.sendMessage(ChatColor.GREEN + "Team markers refreshed for all players.");
        return true;
    }
}