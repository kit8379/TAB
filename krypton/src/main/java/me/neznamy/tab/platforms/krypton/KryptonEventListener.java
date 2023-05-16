package me.neznamy.tab.platforms.krypton;

import me.neznamy.tab.shared.platform.EventListener;
import org.kryptonmc.api.entity.player.Player;
import org.kryptonmc.api.event.Listener;
import org.kryptonmc.api.event.command.CommandExecuteEvent;
import org.kryptonmc.api.event.player.PlayerJoinEvent;
import org.kryptonmc.api.event.player.PlayerQuitEvent;

public class KryptonEventListener extends EventListener {

    @Listener
    public void onJoin(PlayerJoinEvent event) {
        join(new KryptonTabPlayer(event.getPlayer()));
    }

    @Listener
    public void onQuit(PlayerQuitEvent event) {
        quit(event.getPlayer().getUuid());
    }

    //TODO world switch

    @Listener
    public void onCommand(CommandExecuteEvent event) {
        if (event.getSender() instanceof Player && command(((Player) event.getSender()).getUuid(), event.getCommand())) {
            event.deny();
        }
    }
}
