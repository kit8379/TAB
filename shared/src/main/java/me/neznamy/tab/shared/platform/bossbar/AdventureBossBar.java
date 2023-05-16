package me.neznamy.tab.shared.platform.bossbar;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import net.kyori.adventure.audience.Audience;

@AllArgsConstructor
public class AdventureBossBar implements BossBar {

    @Setter private Audience audience;

    private final Map<UUID, net.kyori.adventure.bossbar.BossBar> bossBars = new LinkedHashMap<>();

    @Override
    public void create(@NonNull UUID id, @NonNull String title, float progress, @NonNull BarColor color, @NonNull BarStyle style) {
        if (bossBars.containsKey(id)) return;
        net.kyori.adventure.bossbar.BossBar bar = net.kyori.adventure.bossbar.BossBar.bossBar(IChatBaseComponent.optimizedComponent(title).toAdventureComponent(),
                progress,
                net.kyori.adventure.bossbar.BossBar.Color.valueOf(color.toString()),
                net.kyori.adventure.bossbar.BossBar.Overlay.valueOf(style.toString()));
        bossBars.put(id, bar);
        audience.showBossBar(bar);
    }

    @Override
    public void update(@NonNull UUID id, @NonNull String title) {
        bossBars.get(id).name(IChatBaseComponent.optimizedComponent(title).toAdventureComponent());
    }

    @Override
    public void update(@NonNull UUID id, float progress) {
        bossBars.get(id).progress(progress);
    }

    @Override
    public void update(@NonNull UUID id, @NonNull BarStyle style) {
        bossBars.get(id).overlay(net.kyori.adventure.bossbar.BossBar.Overlay.valueOf(style.toString()));
    }

    @Override
    public void update(@NonNull UUID id, @NonNull BarColor color) {
        bossBars.get(id).color(net.kyori.adventure.bossbar.BossBar.Color.valueOf(color.toString()));
    }

    @Override
    public void remove(@NonNull UUID id) {
        audience.hideBossBar(bossBars.remove(id));
    }
}
