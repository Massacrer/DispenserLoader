package me.Massacrer.DispenserLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class DLConfigManager  extends Properties {
	private static final long serialVersionUID = 1L;
	private final DispenserLoader plugin;
	private final String configFolder = "plugins/DispenserLoader/";
	private final File mainConfigFile = new File(configFolder + "config.txt");
	
	public DLConfigManager(DispenserLoader plugin) {
		this.plugin = plugin;
	}
	
	void onEnable() {
		try {
			new File(configFolder).mkdir();
			mainConfigFile.createNewFile();
			load(new FileInputStream(mainConfigFile));
		} catch (IOException e) {
			//TODO: handle IOException
		}
	}
	
}