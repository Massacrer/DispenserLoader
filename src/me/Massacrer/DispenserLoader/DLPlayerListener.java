package me.Massacrer.DispenserLoader;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import me.Massacrer.DispenserLoader.DispenserLoader;

/**
 * Dispenser Loader Player listener class
 * 
 * @author Massacrer
 * 
 */
class DLPlayerListener implements Listener {
	private final DispenserLoader plugin;
	
	public DLPlayerListener(DispenserLoader instance) {
		plugin = instance;
	}
	
	/**
	 * Called by Bukkit when player quits, used to remove the player from config
	 * when they quit.
	 */
	@EventHandler(priority=EventPriority.NORMAL)
	public void onPlayerQuit(PlayerQuitEvent event) {
		if (plugin.enabled(event.getPlayer())) {
			plugin.dlUsers.remove(event.getPlayer());
		}
	}
	
}
