package dev.lawson.lunarstuff.commands;

import dev.lawson.lunarstuff.managers.LimbManager;
import com.lunarclient.apollo.module.limb.BodyPart;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class LimbCommand implements CommandExecutor, TabCompleter {

    private final LimbManager limbManager;

    public LimbCommand(LimbManager limbManager) {
        this.limbManager = limbManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("lunarstuff.limb")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /limb <take|put|list> <player> [limb]");
            return true;
        }

        String action = args[0].toLowerCase();
        Player target = Bukkit.getPlayer(args[1]);

        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        switch (action) {
            case "take":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Please specify a limb to take.");
                    return true;
                }
                handleLimbAction(sender, target, args[2], true);
                break;
            case "put":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Please specify a limb to put.");
                    return true;
                }
                handleLimbAction(sender, target, args[2], false);
                break;
            case "list":
                Set<BodyPart> hiddenLimbs = limbManager.getHiddenLimbs(target);
                if (hiddenLimbs.isEmpty()) {
                    sender.sendMessage(ChatColor.YELLOW + target.getName() + " has no hidden limbs.");
                } else {
                    String limbs = hiddenLimbs.stream()
                            .map(part -> part.name().toLowerCase())
                            .collect(Collectors.joining(", "));
                    sender.sendMessage(ChatColor.YELLOW + target.getName() + "'s hidden limbs: " + limbs);
                }
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Unknown action. Use take, put, or list.");
                break;
        }

        return true;
    }

    private void handleLimbAction(CommandSender sender, Player target, String limbArg, boolean isTake) {
        try {
            BodyPart part = BodyPart.valueOf(limbArg.toUpperCase());
            if (isTake) {
                limbManager.hideLimb(target, part);
                sender.sendMessage(ChatColor.GREEN + "Hid " + part.name().toLowerCase() + " for " + target.getName() + ".");
            } else {
                limbManager.showLimb(target, part);
                sender.sendMessage(ChatColor.GREEN + "Shown " + part.name().toLowerCase() + " for " + target.getName() + ".");
            }
        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + "Invalid limb specified. Available limbs: " +
                    Arrays.stream(BodyPart.values())
                            .map(part -> part.name().toLowerCase())
                            .collect(Collectors.joining(", ")));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("lunarstuff.limb")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return Arrays.asList("take", "put", "list").stream()
                    .filter(sub -> sub.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 3 && (args[0].equalsIgnoreCase("take") || args[0].equalsIgnoreCase("put"))) {
            return Arrays.stream(BodyPart.values())
                    .map(part -> part.name().toLowerCase())
                    .filter(name -> name.startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
