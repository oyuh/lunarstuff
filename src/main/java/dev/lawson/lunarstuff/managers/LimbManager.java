package dev.lawson.lunarstuff.managers;

import com.lunarclient.apollo.Apollo;
import com.lunarclient.apollo.module.limb.BodyPart;
import com.lunarclient.apollo.module.limb.LimbModule;
import com.lunarclient.apollo.recipients.Recipients;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class LimbManager {

    private final LimbModule limbModule;
    private final Map<UUID, Set<BodyPart>> playerLimbs = new HashMap<>();

    public LimbManager() {
        this.limbModule = Apollo.getModuleManager().getModule(LimbModule.class);
    }

    public void hideLimb(Player player, BodyPart part) {
        UUID uuid = player.getUniqueId();
        playerLimbs.computeIfAbsent(uuid, k -> EnumSet.noneOf(BodyPart.class)).add(part);
        limbModule.hideBodyParts(Recipients.ofEveryone(), uuid, EnumSet.of(part));
    }

    public void showLimb(Player player, BodyPart part) {
        UUID uuid = player.getUniqueId();
        Set<BodyPart> parts = playerLimbs.get(uuid);
        if (parts != null) {
            parts.remove(part);
            if (parts.isEmpty()) {
                playerLimbs.remove(uuid);
            }
        }
        limbModule.resetBodyParts(Recipients.ofEveryone(), uuid, EnumSet.of(part));
        //showBodyParts(Recipients.ofEveryone(), uuid, EnumSet.of(part));

    }

    public Set<BodyPart> getHiddenLimbs(Player player) {
        return playerLimbs.getOrDefault(player.getUniqueId(), Collections.emptySet());
    }

    public void resetLimbs(Player player) {
        UUID uuid = player.getUniqueId();
        limbModule.resetBodyParts(Recipients.ofEveryone(), uuid, EnumSet.allOf(BodyPart.class));
        playerLimbs.remove(uuid);
    }
}
