package me.neznamy.tab.api;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * A helper class for easy management of armor stands of a player
 */
public class ArmorStandManager {

	//map of registered armor stands
	private Map<String, ArmorStand> armorStands = Collections.synchronizedMap(new LinkedHashMap<>());
	
	//players in entity tracking range
	private Set<TabPlayer> nearbyPlayers = Collections.synchronizedSet(new HashSet<>());

	/**
	 * Adds armor stand into list
	 * @param name - key name of the armor stand
	 * @param as - armor stand to add
	 */
	public void addArmorStand(String name, ArmorStand as) {
		armorStands.put(name, as);
		nearbyPlayers.forEach(as::spawn);
	}
	
	/**
	 * Returns armor stand with given name or null if not present
	 * @param name - name of armor stand registered with
	 * @return armor stand with given name
	 */
	public ArmorStand getArmorStand(String name) {
		return armorStands.get(name);
	}

	/**
	 * Removes armor stand from list. It is not automatically destroyed for players
	 * @param name - name of armor stand to remove
	 */
	public void removeArmorStand(String name) {
		armorStands.get(name).destroy();
		armorStands.remove(name);
	}

	/**
	 * Spawns all armor stands for specified viewer and adds them into nearby players
	 * @param viewer - player to spawn armor stands for
	 */
	public void spawn(TabPlayer viewer) {
		nearbyPlayers.add(viewer);
		if (viewer.getVersion().getMinorVersion() < 8) return;
		synchronized (armorStands) {
			armorStands.values().forEach(a -> a.spawn(viewer));
		}
	}

	/**
	 * Sets sneak value of armor stands to specified value
	 * @param sneaking - new sneaking status
	 */
	public void sneak(boolean sneaking) {
		synchronized (armorStands) {
			armorStands.values().forEach(a -> a.sneak(sneaking));
		}
	}

	/**
	 * Teleports armor stands to player's current location for all nearby players
	 */
	public void teleport() {
		synchronized (armorStands) {
			armorStands.values().forEach(ArmorStand::teleport);
		}
	}

	/**
	 * Teleports armor stands to player's current location for specified viewer
	 * @param viewer - player to teleport armor stands for
	 */
	public void teleport(TabPlayer viewer) {
		synchronized (armorStands) {
			armorStands.values().forEach(a -> a.teleport(viewer));
		}
	}

	/**
	 * Refreshes name of all armor stands for all nearby players
	 */
	public void refresh() {
		synchronized (armorStands) {
			armorStands.values().forEach(ArmorStand::refresh);
		}
	}

	/**
	 * Updates armor stand visibility for all armor stands
	 * @param force - true if packets should be sent despite seemingly not needed
	 */
	public void updateVisibility(boolean force) {
		synchronized (armorStands) {
			armorStands.values().forEach(a -> a.updateVisibility(force));
		}
	}

	/**
	 * Removes specified player from list of nearby players
	 * @param viewer - player to remove
	 */
	public void unregisterPlayer(TabPlayer viewer) {
		nearbyPlayers.remove(viewer);
	}

	/**
	 * Sends destroy packet of all armor stands to everyone and clears nearby players list
	 */
	public void destroy() {
		synchronized (armorStands) {
			armorStands.values().forEach(ArmorStand::destroy);
		}
		nearbyPlayers.clear();
	}

	/**
	 * Sends destroy packet of all armor stands to specified player and removes them from nearby players list
	 * @param viewer - player to destroy armor stands for
	 */
	public void destroy(TabPlayer viewer) {
		synchronized (armorStands) {
			armorStands.values().forEach(a -> a.destroy(viewer));
		}
		nearbyPlayers.remove(viewer);
	}

	/**
	 * Returns true if manager contains armor stand with specified entity id, false if not
	 * @param entityId - entity id
	 * @return true if armor stand with specified entity id exists, false if not
	 */
	public boolean hasArmorStandWithID(int entityId) {
		synchronized (armorStands) {
			for (ArmorStand as : armorStands.values()) {
				if (as.getEntityId() == entityId) {
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * Returns a copy of list of all registered armor stands
	 * @return copy of armor stands
	 */
	public Collection<ArmorStand> getArmorStands(){
		return armorStands.values();
	}

	/**
	 * Returns list of nearby players
	 * @return list of nearby players
	 */
	public Set<TabPlayer> getNearbyPlayers(){
		synchronized (nearbyPlayers) {
			return new HashSet<>(nearbyPlayers);
		}
	}
}