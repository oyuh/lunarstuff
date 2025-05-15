package dev.lawson.lunarstuff.managers;

import com.lunarclient.apollo.Apollo;
import com.lunarclient.apollo.module.staffmod.StaffModModule;
import com.lunarclient.apollo.module.staffmod.StaffMod;
import com.lunarclient.apollo.player.ApolloPlayer;
import dev.lawson.lunarstuff.Lunarstuff;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class StaffManager implements Listener {
    private final StaffModModule staffModModule;
    private final Set<UUID> xrayEnabled;  // track players with XRAY enabled

    public StaffManager(Lunarstuff plugin) {
        // Get Apollo's StaffMod module
        this.staffModModule = Apollo.getModuleManager().getModule(StaffModModule.class);
        this.xrayEnabled = new HashSet<>();
    }

    /**
     * Toggle XRAY staff mod for a player. Enables if currently disabled, or disables if enabled.
     * @param player The player toggling XRAY.
     * @return true if XRAY is now enabled, false if it was disabled.
     */
    public boolean toggleXray(Player player) {
        UUID uuid = player.getUniqueId();
        Optional<ApolloPlayer> apolloPlayerOpt = Apollo.getPlayerManager().getPlayer(uuid);
        if (!apolloPlayerOpt.isPresent()) {
            // Player is not using Lunar Client or Apollo not available for them
            return false;
        }
        ApolloPlayer apolloPlayer = apolloPlayerOpt.get();
        if (xrayEnabled.contains(uuid)) {
            // Disable XRAY staff mod
            staffModModule.disableStaffMods(apolloPlayer, java.util.Collections.singletonList(StaffMod.XRAY));
            xrayEnabled.remove(uuid);
            return false;
        } else {
            // Enable XRAY staff mod
            staffModModule.enableStaffMods(apolloPlayer, java.util.Collections.singletonList(StaffMod.XRAY));
            xrayEnabled.add(uuid);
            return true;
        }
    }

    /** Disable XRAY for all players in staff mode (used on plugin shutdown). */
    public void disableAllStaffMods() {
        // Iterate over a copy of enabled set to avoid modification issues
        for (UUID uuid : new HashSet<>(xrayEnabled)) {
            Apollo.getPlayerManager().getPlayer(uuid).ifPresent(ap ->
                    staffModModule.disableStaffMods(ap, java.util.Collections.singletonList(StaffMod.XRAY))
            );
        }
        xrayEnabled.clear();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (xrayEnabled.contains(uuid)) {
            // If a staff member with XRAY on leaves, disable it to be safe
            Apollo.getPlayerManager().getPlayer(uuid).ifPresent(ap ->
                    staffModModule.disableStaffMods(ap, java.util.Collections.singletonList(StaffMod.XRAY))
            );
            xrayEnabled.remove(uuid);
        }
    }
}
