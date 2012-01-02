package me.Massacrer.DispenserLoader;

import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * Dispenser Loader Player config class
 * 
 * @author Massacrer
 * 
 */
class DLPlayerConfig {
	Player player = null;
	int material = 262;
	int amount = 64;
	int wandItem = 269;
	short damageValue = 0;
	boolean singleClearMode = false;
	boolean blockAreaMode = false;
	boolean singleFillMode = false;
	Block areaBlock1 = null;
	Block areaBlock2 = null;
	int lastAreaBlockSet = 0;
	boolean clearOnce = false;
	boolean fillOnce = false;
	static Logger log = null;
	static DispenserLoader plugin = null;
	boolean chestMode = false;
	
	@SuppressWarnings("static-access")
	DLPlayerConfig(int m, int a, Player p, DispenserLoader plugin) {
		this.player = p;
		this.material = m;
		this.amount = a;
		this.log = plugin.log;
		this.plugin = plugin;
	}
	
	/**
	 * Sets player's wand item
	 * 
	 * @param item
	 *            Item to set wandItem to
	 */
	void setWandItem(int item) {
		this.wandItem = item;
		player.sendMessage(ChatColor.DARK_AQUA + "Wanditem is now " + wandItem);
		if (plugin.debug)
			log.info("DL: setWandItem called for player " + player.getName()
					+ " (item: " + item + ").");
	}
	
	/**
	 * Sets player's material
	 * 
	 * @param material
	 *            Material to set player's Material to
	 */
	void setMaterial(int material) {
		this.material = material;
		player.sendMessage(ChatColor.DARK_AQUA + "Material is now " + material);
		if (plugin.debug)
			log.info("DL: setMaterial called for player " + player.getName()
					+ " (material: " + material + ").");
	}
	
	void materialAndDamage(String str) {
		String[] strs = str.split(":");
		this.material = Integer.parseInt(strs[0]);
		this.damageValue = Short.parseShort(strs[1]);
	}
	
	/**
	 * Sets player's amount of material
	 * 
	 * @param amount
	 *            Amount of material to set player's amount to
	 */
	void setAmount(int amount) {
		this.amount = amount;
		player.sendMessage(ChatColor.DARK_AQUA + "Amount is now " + amount);
		if (plugin.debug)
			log.info("DL: setAmount called for player " + player.getName()
					+ " (amount: " + amount + ").");
	}
	
	/**
	 * Toggles the user's area mode
	 */
	void toggleAreaMode() {
		if (this.blockAreaMode == true) {
			this.disableAreaMode();
			this.fillOnce = false;
			this.clearOnce = false;
			this.singleClearMode = false;
			this.singleFillMode = false;
			this.singleClearMode = false;
			this.singleFillMode = false;
			this.clearOnce = false;
			this.fillOnce = false;
			player.sendMessage("Area mode disabled, now in single-block adding mode");
		} else {
			this.blockAreaMode = true;
			player.sendMessage(ChatColor.DARK_AQUA
					+ "Now in cuboid mode: left-click to select opposite points, then type /dload area [material amount / fill / empty]");
		}
		if (plugin.debug)
			log.info("DL: toggleAreaMode called for player " + player.getName()
					+ " (areamode now " + this.blockAreaMode + ").");
	}
	
	/**
	 * Toggles the user's single block fill flag
	 * 
	 * @param once
	 *            Whether or not to only do the operation once
	 */
	void toggleSingleFillFlag(boolean once) {
		if (this.singleFillMode == true) {
			this.singleFillMode = false;
			player.sendMessage(ChatColor.DARK_AQUA
					+ "Now in normal adding mode");
		} else {
			this.singleFillMode = true;
			if (once) {
				this.fillOnce = true;
			}
			player.sendMessage(ChatColor.DARK_AQUA
					+ "Now in single-block filling mode");
		}
		if (plugin.debug)
			log.info("DL: toggleSingleFillFlag called for player "
					+ player.getName() + "(fillflag now " + this.singleFillMode
					+ ").");
	}
	
	/**
	 * Toggles player's single-block clear flag
	 * 
	 * @param once
	 *            Whether or not to only do the operation once
	 */
	void toggleSingleClearFlag(boolean once) {
		if (this.singleClearMode == true) {
			this.singleClearMode = false;
			player.sendMessage(ChatColor.DARK_AQUA + "Mode set to add");
		} else {
			this.singleClearMode = true;
			if (once) {
				this.clearOnce = true;
			}
			player.sendMessage(ChatColor.DARK_AQUA + "Mode set to empty");
		}
		if (plugin.debug)
			log.info("DL: toggleSingleClearFlag called for player "
					+ player.getName() + " (clearflag now "
					+ this.singleClearMode + ").");
	}
	
	/**
	 * disables area mode, and clears blocks
	 */
	void disableAreaMode() {
		// player.sendMessage(ChatColor.DARK_AQUA +
		// "Now in single-block adding mode");
		this.blockAreaMode = false;
		this.areaBlock1 = null;
		this.areaBlock2 = null;
		this.lastAreaBlockSet = 2;
		if (plugin.debug)
			log.info("DL: disableAreaMode called for player "
					+ player.getName() + ".");
	}
	
	/**
	 * Prints player internal data to console
	 */
	void dbgPrintInfo() {
		log.info("Begin DLOAD debug dump");
		log.info("player = " + player.getName());
		log.info("material = " + material);
		log.info("amount = " + amount);
		log.info("wanditem = " + wandItem);
		log.info("singleClear = " + singleClearMode);
		log.info("areamode = " + blockAreaMode);
		log.info("singlefill = " + singleFillMode);
		log.info("areaBlock1=null = " + (areaBlock1 == null));
		log.info("areaBlock2=null = " + (areaBlock2 == null));
		log.info("lastAreaBlockSet = " + lastAreaBlockSet);
		log.info("clearonce = " + clearOnce);
		log.info("fillonce = " + fillOnce);
		log.info("chestMode = " + chestMode);
		log.info("End DLOAD debug dump");
	}
	
	/**
	 * Toggles mode between working with Dispensers and Chests
	 */
	void setChestMode(boolean mode) {
		this.chestMode = mode;
		player.sendMessage(ChatColor.DARK_AQUA + "Now working with "
				+ (chestMode ? "chests" : "dispensers"));
	}
}