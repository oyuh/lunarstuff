package dev.lawson.lunarstuff.managers;

import com.lunarclient.apollo.Apollo;
import com.lunarclient.apollo.player.ApolloPlayer;
import dev.lawson.lunarstuff.Lunarstuff;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Optional;

public class LunarClientCheckManager implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Delay to allow Apollo to detect the client
        Bukkit.getScheduler().runTaskLater(Lunarstuff.getInstance(), () -> {
            Optional<ApolloPlayer> apolloPlayer = Apollo.getPlayerManager().getPlayer(player.getUniqueId());
            if (apolloPlayer.isPresent()) {
                // Player is using Lunar Client. Do nothing.
                return;
            }
            // Build a fancy clickable message using Adventure
// ... in your handler ...
            String url = "https://www.lunarclient.com/";
            Component message = Component.text()
                    .append(Component.text("We strongly suggest you to use ", NamedTextColor.GRAY))
                    .append(Component.text("Lunar Client", NamedTextColor.AQUA))
                    .append(Component.text(" (", NamedTextColor.GRAY))
                    .append(Component.text(url, NamedTextColor.AQUA))
                    .append(Component.text(").", NamedTextColor.GRAY))
                    .build();

// Serialize to legacy section-color string and send as plain text
            player.sendMessage(LegacyComponentSerializer.legacySection().serialize(message));
        }, 40L); // 2 second delay
    }
}
