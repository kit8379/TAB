package me.neznamy.tab.platforms.bukkit;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.Getter;
import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.platform.BukkitPlatform;
import me.neznamy.tab.platforms.bukkit.scoreboard.PacketScoreboard;
import me.neznamy.tab.shared.chat.rgb.RGBUtils;
import me.neznamy.tab.shared.hook.ViaVersionHook;
import me.neznamy.tab.shared.platform.bossbar.BossBar;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.platforms.bukkit.bossbar.EntityBossBar;
import me.neznamy.tab.platforms.bukkit.bossbar.BukkitBossBar;
import me.neznamy.tab.platforms.bukkit.bossbar.ViaBossBar;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcher;
import me.neznamy.tab.platforms.bukkit.nms.storage.nms.NMSStorage;
import me.neznamy.tab.platforms.bukkit.nms.storage.packet.*;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.backend.BackendTabPlayer;
import me.neznamy.tab.shared.backend.EntityData;
import me.neznamy.tab.shared.backend.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;

/**
 * TabPlayer implementation for Bukkit platform
 */
@SuppressWarnings("deprecation")
@Getter
public class BukkitTabPlayer extends BackendTabPlayer {

    /** Player's NMS handle (EntityPlayer), preloading for speed */
    private final Object handle;

    /** Player's connection for sending packets, preloading for speed */
    private final Object playerConnection;

    private final Scoreboard<BukkitTabPlayer> scoreboard = new PacketScoreboard(this);
    private final TabList tabList = new BukkitTabList(this);
    private final BossBar bossBar = TAB.getInstance().getServerVersion().getMinorVersion() >= 9 ?
            new BukkitBossBar(this) : getVersion().getMinorVersion() >= 9 ? new ViaBossBar(this) : new EntityBossBar(this);

    /**
     * Constructs new instance with given bukkit player and protocol version
     *
     * @param   p
     *          bukkit player
     */
    @SneakyThrows
    public BukkitTabPlayer(Player p) {
        super(p, p.getUniqueId(), p.getName(), TAB.getInstance().getConfiguration().getServerName(),
                p.getWorld().getName(), ViaVersionHook.getInstance().getPlayerVersion(p.getUniqueId(), p.getName()));
        handle = NMSStorage.getInstance().getHandle.invoke(player);
        playerConnection = NMSStorage.getInstance().PLAYER_CONNECTION.get(handle);
    }

    @Override
    public boolean hasPermission(@NotNull String permission) {
        return getPlayer().hasPermission(permission);
    }

    @Override
    @SneakyThrows
    public int getPing() {
        if (TAB.getInstance().getServerVersion().getMinorVersion() >= 17) {
            return getPlayer().getPing();
        }
        return NMSStorage.getInstance().PING.getInt(handle);
    }

    @SneakyThrows
    public void sendPacket(@Nullable Object nmsPacket) {
        if (nmsPacket == null || !getPlayer().isOnline()) return;
        NMSStorage.getInstance().sendPacket.invoke(playerConnection, nmsPacket);
    }

    @Override
    public void sendMessage(@NotNull IChatBaseComponent message) {
        getPlayer().sendMessage(RGBUtils.getInstance().convertToBukkitFormat(message.toFlatText(),
                getVersion().getMinorVersion() >= 16 && TAB.getInstance().getServerVersion().getMinorVersion() >= 16));
    }

    @Override
    public boolean hasInvisibilityPotion() {
        return getPlayer().hasPotionEffect(PotionEffectType.INVISIBILITY);
    }

    @Override
    public boolean isDisguised() {
        try {
            if (!((BukkitPlatform)TAB.getInstance().getPlatform()).isLibsDisguisesEnabled()) return false;
            return (boolean) Class.forName("me.libraryaddict.disguise.DisguiseAPI").getMethod("isDisguised", Entity.class).invoke(null, getPlayer());
        } catch (LinkageError | ReflectiveOperationException e) {
            //java.lang.NoClassDefFoundError: Could not initialize class me.libraryaddict.disguise.DisguiseAPI
            TAB.getInstance().getErrorManager().printError("Failed to check disguise status using LibsDisguises", e);
            ((BukkitPlatform)TAB.getInstance().getPlatform()).setLibsDisguisesEnabled(false);
            return false;
        }
    }

    @Override
    @SneakyThrows
    public TabList.Skin getSkin() {
        Collection<Property> col = ((GameProfile)NMSStorage.getInstance().getProfile.invoke(handle)).getProperties().get(TabList.TEXTURES_PROPERTY);
        if (col.isEmpty()) return null; //offline mode
        Property property = col.iterator().next();
        return new TabList.Skin(property.getValue(), property.getSignature());
    }

    @Override
    public @NotNull Player getPlayer() {
        return (Player) player;
    }

    @Override
    public boolean isOnline() {
        return getPlayer().isOnline();
    }

    @Override
    public boolean isVanished() {
        return getPlayer().getMetadata("vanished").stream().anyMatch(MetadataValue::asBoolean);
    }

    @Override
    public int getGamemode() {
        return getPlayer().getGameMode().getValue();
    }

    @Override
    public void spawnEntity(int entityId, @NotNull UUID id, @NotNull Object entityType, @NotNull Location location, @NotNull EntityData data) {
        sendPacket(PacketPlayOutSpawnEntityLivingStorage.build(entityId, id, entityType, location, data));
        if (TAB.getInstance().getServerVersion().getMinorVersion() >= 15) {
            updateEntityMetadata(entityId, data);
        }
    }

    @Override
    @SneakyThrows
    public void updateEntityMetadata(int entityId, @NotNull EntityData data) {
        if (PacketPlayOutEntityMetadataStorage.CONSTRUCTOR.getParameterCount() == 2) {
            //1.19.3+
            sendPacket(PacketPlayOutEntityMetadataStorage.CONSTRUCTOR.newInstance(entityId, DataWatcher.packDirty.invoke(data.build())));
        } else {
            sendPacket(PacketPlayOutEntityMetadataStorage.CONSTRUCTOR.newInstance(entityId, data.build(), true));
        }
    }

    @Override
    public void teleportEntity(int entityId, @NotNull Location location) {
        sendPacket(PacketPlayOutEntityTeleportStorage.build(entityId, location));
    }

    @Override
    @SneakyThrows
    public void destroyEntities(int... entities) {
        if (PacketPlayOutEntityDestroyStorage.CONSTRUCTOR.getParameterTypes()[0] != int.class) {
            sendPacket(PacketPlayOutEntityDestroyStorage.CONSTRUCTOR.newInstance(new Object[]{entities}));
        } else {
            //1.17.0 Mojank
            for (int entity : entities) {
                sendPacket(PacketPlayOutEntityDestroyStorage.CONSTRUCTOR.newInstance(entity));
            }
        }
    }
}
