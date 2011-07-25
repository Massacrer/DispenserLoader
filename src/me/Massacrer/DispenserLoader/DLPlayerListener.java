package me.Massacrer.DispenserLoader;

import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;
import me.Massacrer.DispenserLoader.DispenserLoader;

/**
 * Dispenser Loader Player listener class
 * 
 * @author Massacrer
 * 
 */
class DLPlayerListener extends PlayerListener {
	private final DispenserLoader plugin;
	
	public DLPlayerListener(DispenserLoader instance) {
		plugin = instance;
	}
	
	/**
	 * Called by Bukkit when player quits, used to remove the player from config
	 * when they quit.
	 */
	public void onPlayerQuit(PlayerQuitEvent event) {
		if (plugin.enabled(event.getPlayer())) {
			plugin.dlUsers.remove(event.getPlayer());
		}
		
	}
	
}
