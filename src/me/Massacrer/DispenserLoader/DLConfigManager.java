package me.Massacrer.DispenserLoader;

import java.io.File;
import org.bukkit.util.config.Configuration;

class DLConfigManager {
	@SuppressWarnings("unused")
	private final DispenserLoader plugin;
	private final String configFolder = "plugins/DispenserLoader/";
	private final File mainConfigFile = new File(configFolder + "config.txt");
	private final Configuration config = new Configuration(mainConfigFile);
	
	/**
	 * @param plugin
	 *            the main plugin
	 */
	DLConfigManager(DispenserLoader plugin) {
		this.plugin = plugin;
		config.load();
	}
	
	void setupConfigFile() {
		
	}
}
/*
 * config and permissions-related stuff
 * required stuff:
 * 
 * infinite arrows available
 * able to use plugin
 * max number of dispensers to modify
 * 
 */