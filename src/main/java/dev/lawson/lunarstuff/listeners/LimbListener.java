package dev.lawson.lunarstuff.listeners;

import dev.lawson.lunarstuff.managers.LimbManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public class LimbListener implements Listener {

    private final LimbManager limbManager;

    public LimbListener(LimbManager limbManager) {
        this.limbManager = limbManager;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        limbManager.resetLimbs(event.getPlayer());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        limbManager.resetLimbs(event.getEntity());
    }
}
