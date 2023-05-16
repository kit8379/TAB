package me.neznamy.tab.platforms.bungeecord;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.platform.bossbar.BossBar;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * BossBar handler for BungeeCord. It uses packets, since
 * BungeeCord does not have a BossBar API. Only supports
 * 1.9+ players, as dealing with entities would be simply impossible.
 */
@RequiredArgsConstructor
public class BungeeBossBar implements BossBar {
    
    private final @NotNull BungeeTabPlayer player;

    /** Both are included in the same action, need to remember and write them both */
    private final Map<UUID, Integer> colors = new HashMap<>();
    private final Map<UUID, Integer> styles = new HashMap<>();

    @Override
    public void create(@NonNull UUID id, @NonNull String title, float progress, @NonNull BarColor color, @NonNull BarStyle style) {
        if (player.getVersion().getMinorVersion() < 9) return;

        net.md_5.bungee.protocol.packet.BossBar bossbar = new net.md_5.bungee.protocol.packet.BossBar(id, 0);
        bossbar.setHealth(progress);
        bossbar.setTitle(IChatBaseComponent.optimizedComponent(title).toString(player.getVersion()));
        bossbar.setColor(color.ordinal());
        bossbar.setDivision(style.ordinal());
        player.getPlayer().unsafe().sendPacket(bossbar);

        colors.put(id, color.ordinal());
        styles.put(id, style.ordinal());
    }

    @Override
    public void update(@NonNull UUID id, @NonNull String title) {
        if (player.getVersion().getMinorVersion() < 9) return;

        net.md_5.bungee.protocol.packet.BossBar bossbar = new net.md_5.bungee.protocol.packet.BossBar(id, 3);
        bossbar.setTitle(IChatBaseComponent.optimizedComponent(title).toString(player.getVersion()));
        player.getPlayer().unsafe().sendPacket(bossbar);
    }

    @Override
    public void update(@NonNull UUID id, float progress) {
        if (player.getVersion().getMinorVersion() < 9) return;

        net.md_5.bungee.protocol.packet.BossBar bossbar = new net.md_5.bungee.protocol.packet.BossBar(id, 2);
        bossbar.setHealth(progress);
        player.getPlayer().unsafe().sendPacket(bossbar);
    }

    @Override
    public void update(@NonNull UUID id, @NonNull BarStyle style) {
        if (player.getVersion().getMinorVersion() < 9) return;

        net.md_5.bungee.protocol.packet.BossBar bossbar = new net.md_5.bungee.protocol.packet.BossBar(id, 4);
        bossbar.setDivision(style.ordinal());
        bossbar.setColor(colors.get(id));
        player.getPlayer().unsafe().sendPacket(bossbar);

        styles.put(id, style.ordinal());
    }

    @Override
    public void update(@NonNull UUID id, @NonNull BarColor color) {
        if (player.getVersion().getMinorVersion() < 9) return;

        net.md_5.bungee.protocol.packet.BossBar bossbar = new net.md_5.bungee.protocol.packet.BossBar(id, 4);
        bossbar.setDivision(styles.get(id));
        bossbar.setColor(color.ordinal());
        player.getPlayer().unsafe().sendPacket(bossbar);

        colors.put(id, color.ordinal());
    }

    @Override
    public void remove(@NonNull UUID id) {
        if (player.getVersion().getMinorVersion() < 9) return;

        player.getPlayer().unsafe().sendPacket(new net.md_5.bungee.protocol.packet.BossBar(id, 1));

        colors.remove(id);
        styles.remove(id);
    }
}
