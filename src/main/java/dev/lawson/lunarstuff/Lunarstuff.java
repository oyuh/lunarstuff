package dev.lawson.lunarstuff;

import dev.lawson.lunarstuff.commands.LimbCommand;
import dev.lawson.lunarstuff.commands.StaffModeCommand;
import dev.lawson.lunarstuff.listeners.LimbListener;
import dev.lawson.lunarstuff.managers.*;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class Lunarstuff extends JavaPlugin {

    private static Lunarstuff instance;

    private StaffManager staffManager;
    private NametagManager nametagManager;
    private TeamManager teamManager;
    private RichPresenceManager richPresenceManager;
    private LimbManager limbManager;

    @Override
    public void onEnable() {
        instance = this;

        // Load config.yml from resources
        saveDefaultConfig();

        // Initialize managers
        staffManager = new StaffManager(this);
        nametagManager = new NametagManager(this);
        teamManager = new TeamManager(this);
        richPresenceManager = new RichPresenceManager(this);
        limbManager = new LimbManager();

        // Register commands
        PluginCommand staffModeCmd = getCommand("staffmode");
        if (staffModeCmd != null) {
            staffModeCmd.setExecutor(new StaffModeCommand(staffManager));
        } else {
            getLogger().severe("Failed to register /staffmode command. Check plugin.yml.");
        }

        PluginCommand limbCmd = getCommand("limb");
        if (limbCmd != null) {
            LimbCommand limbCommand = new LimbCommand(limbManager);
            limbCmd.setExecutor(limbCommand);
            limbCmd.setTabCompleter(limbCommand);
        } else {
            getLogger().severe("Failed to register /limb command. Check plugin.yml.");
        }

        // Register event listeners
        Bukkit.getPluginManager().registerEvents(staffManager, this);
        Bukkit.getPluginManager().registerEvents(nametagManager, this);
        Bukkit.getPluginManager().registerEvents(teamManager, this);
        Bukkit.getPluginManager().registerEvents(new LimbListener(limbManager), this);
        Bukkit.getPluginManager().registerEvents(new LunarClientCheckManager(), this);

        // Start tasks
        nametagManager.startNametagUpdateTask();
        richPresenceManager.start();
        teamManager.reloadAllTeams();
    }

    @Override
    public void onDisable() {
        // Clean up
        staffManager.disableAllStaffMods();
        nametagManager.resetAllNametags();
        richPresenceManager.resetAllPresence();
        teamManager.resetAllTeams();
        nametagManager.stopNametagUpdateTask();

        instance = null;
    }

    public static Lunarstuff getInstance() {
        return instance;
    }

    // Getters for managers
    public StaffManager getStaffManager() {
        return staffManager;
    }

    public NametagManager getNametagManager() {
        return nametagManager;
    }

    public TeamManager getTeamManager() {
        return teamManager;
    }

    public RichPresenceManager getRichPresenceManager() {
        return richPresenceManager;
    }

    public LimbManager getLimbManager() {
        return limbManager;
    }
}
