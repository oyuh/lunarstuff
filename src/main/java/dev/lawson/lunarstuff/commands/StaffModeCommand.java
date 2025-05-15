package dev.lawson.lunarstuff.commands;

import dev.lawson.lunarstuff.managers.StaffManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;

public class StaffModeCommand implements CommandExecutor {

    private final StaffManager staffManager;

    public StaffModeCommand(StaffManager staffManager) {
        this.staffManager = staffManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used in-game by players.");
            return true;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("apollo.staff")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use staff mode.");
            return true;
        }
        // Toggle XRAY staff mode for this player
        boolean nowEnabled = staffManager.toggleXray(player);
        if (nowEnabled) {
            player.sendMessage(ChatColor.GREEN + "Staff XRAY mode enabled.");
        } else {
            player.sendMessage(ChatColor.YELLOW + "Staff XRAY mode disabled.");
        }
        return true;
    }
}
