package me.Massacrer.DispenserLoader;

import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.ChatColor;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Logger;
import java.util.HashMap;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

/**
 * Dispenser Loader Main class Copious amounts of comments for showing the code
 * to my java-inexperienced friend
 * 
 * @author Massacrer
 * 
 */
public class DispenserLoader extends JavaPlugin {
	// Define the logger (used to report to server console)
	static final Logger log = Logger.getLogger("Minecraft");
	// Define other parts of this plugin, and pass them references to this
	// class so they can call back
	private final DLPlayerListener playerListener = new DLPlayerListener(this);
	private final DLBlockListener blockListener = new DLBlockListener(this);
	final DLBlockInterface blockInterface = new DLBlockInterface(this);
	// Create the main HashMap that contains Players mapped to their settings
	HashMap<Player, DLPlayerConfig> dlUsers = new HashMap<Player, DLPlayerConfig>();
	// Create a PermissionHandler (used for interacting with Permissions)
	static PermissionHandler permissionHandler;
	// Initialise debug variable (used to check whether to output extra info)
	boolean debug = false;
	// Create a reference to the server's PluginManager
	PluginManager pluginManager = null;
	
	/**
	 * This is called by the server when the plugin is loaded
	 */
	public void onEnable() {
		// Define the plugin manager
		pluginManager = getServer().getPluginManager();
		// Register to be informed by the server when events occur
		pluginManager.registerEvent(Event.Type.BLOCK_DAMAGE, blockListener,
				Priority.Normal, this);
		pluginManager.registerEvent(Event.Type.BLOCK_BREAK, blockListener,
				Priority.Normal, this);
		pluginManager.registerEvent(Event.Type.PLAYER_QUIT, playerListener,
				Priority.Normal, this);
		// Output to log that the plugin is enabled
		log.info("DispenserLoader enabled");
		// Self-explanatory
		setupPermissions();
	}
	
	/**
	 * Called when the plugin is disabled
	 */
	public void onDisable() {
		// Simple, just report that it is disabled
		log.info("DispenserLoader disabled");
	}
	
	/*
	 * HashMap stuff
	 * 
	 * This is working with the HashMap that stores player settings
	 */
	/**
	 * Adds player to the HashMap, and maps them to a DLPlayerConfig containing
	 * material and amount values per player
	 * 
	 * @param player
	 *            Player to enabled
	 * @param material
	 *            Material to enable the player with
	 * @param amount
	 *            Amount to enable the player with
	 * @return false if amount is over 576, otherwise true
	 */
	private boolean hmEnable(Player player, int material, int amount) {
		// Intelligence check - dont use more than 576 material
		if (amount > 576) {
			return false;
		}
		// Create a new DLPlayerConfig object, with some starting values
		DLPlayerConfig entry = new DLPlayerConfig(material, amount, player,
				this);
		// If player is disabled, enable them and store their config in the map.
		if (!dlUsers.containsKey(player)) {
			this.dlUsers.put(player, entry);
			player.sendMessage(ChatColor.DARK_AQUA + "DispenserLoader Enabled");
		} else {
			dlUsers.get(player).material = material;
			dlUsers.get(player).amount = amount;
		}
		return true;
	}
	
	/**
	 * Removes player from the HashMap
	 * 
	 * @param player
	 *            Player to remove
	 */
	private void hmDisable(Player player) {
		this.dlUsers.remove(player);
		player.sendMessage(ChatColor.DARK_AQUA + "DispenserLoader Disabled");
	}
	
	/**
	 * Checks if player is in the HashMap
	 * 
	 * @param player
	 *            Player to check
	 * @return true if player is in map, otherwise false
	 */
	boolean enabled(Player player) {
		return dlUsers.containsKey(player);
	}
	
	/*
	 * End of HashMap stuff
	 * 
	 * Start of Command Listener stuff
	 */

	/**
	 * Overrides onCommand Main command interpretation block
	 * 
	 * Called by the server in response to a player issuing a command
	 * 
	 * @return True if command was handled successfully, otherwise false (value
	 *         used by CB - value false causes plugin Usage info to be sent to
	 *         player)
	 */
	public boolean onCommand(CommandSender sender, Command cmd,
			String commandLabel, String[] args) {
		// arg[0] = material, arg[1] = number
		
		// Overall check to ensure the plugin only handles its own commands
		// Probably irrelevant with CB only passing commands to plugins that
		// registered them, but intelligince is never a bad thing
		// Mostly
		if (commandLabel.equalsIgnoreCase("DLOAD")) {
			int argLength = args.length;
			
			// Checks if the sender is the console, and handles appropriately
			if (!(sender instanceof Player)) {
				if (debug)
					log.info("DLOAD: sender not instance of player");
				handleConsoleInput(sender, args);
				return true;
			}
			
			Player player = (Player) sender;
			String pName = player.getName();
			
			// if player is NOT allowed to use command,
			// inform them so, and return the function call with true
			if (!(playerAllowed(player))) {
				if (debug)
					log.info("DLOAD: player " + pName + " not allowed");
				player.sendMessage("You do not have permission to use this command");
				return true;
			}
			
			// If no arguments and player already enabled, disable player
			if (argLength == 0 && enabled(player)) {
				hmDisable(player);
				if (debug)
					log.info("DLOAD: player " + pName + " disabled");
				return true;
			}
			
			// If player not enabled, enable player with default values then
			// fall through to process arguments properly
			if (!enabled(player)) {
				if (!hmEnable(player, 262, 64)) {
					player.sendMessage(ChatColor.RED
							+ "Cannot use more than 576 material (576 = 64 * 9)");
					return true;
				}
				giveWand(player);
				if (debug)
					log.info("DLOAD: player " + pName + " enabled");
				if (argLength == 0)
					return true;
			}
			
			// This is below the first check because that check can cause the
			// player to become enabled, thus would be possible for this to fail
			DLPlayerConfig pConfig = dlUsers.get(player);
			
			// Word-argument handling block
			if (argLength > 0) {
				if (debug)
					log.info("DLOAD: " + pName + " entered word handling block");
				// If argument is "help", use functionality of onCommand to
				// report plugin's Usage section to player by returning false
				if (args[0].equalsIgnoreCase("help")) {
					if (debug)
						log.info("DLOAD: player " + pName
								+ " used argument: help");
					return false;
				}
				
				// If first argument is "Wand", give wand item to the player
				if (args[0].equalsIgnoreCase("wand")) {
					giveWand(player);
					player.sendMessage(ChatColor.DARK_AQUA
							+ "You have recieved an item (id "
							+ pConfig.wandItem
							+ ") to use as your DispenserLoader wand.");
					if (debug)
						log.info("DLOAD: Player " + pName
								+ " given wand item (item " + pConfig.wandItem
								+ ").");
					return true;
				}
				
				// change whether the plugin is working with dispensers or
				// chests
				if (args[0].equalsIgnoreCase("mode")) {
					if (argLength > 1) {
						if (args[1].equalsIgnoreCase("chests")) {
							pConfig.setChestMode(true);
						} else if (args[1].equalsIgnoreCase("dispensers")) {
							pConfig.setChestMode(false);
						} else {
							player.sendMessage(ChatColor.RED
									+ "Specify a valid option (chests or dispensers) for this command");
						}
					}
					return true;
				}
				
				// If first argument is "SetWand"...
				if (args[0].equalsIgnoreCase("setwand") && argLength > 1) {
					// Try to set wandItem to item id specified as next argument
					try {
						pConfig.setWandItem(Integer.parseInt(args[1]));
						player.sendMessage(ChatColor.DARK_AQUA
								+ "Wand item set to item id: "
								+ pConfig.wandItem);
						// Catch the exception to prevent crashes and report to
						// the user that they entered an invalid argument
					} catch (NumberFormatException e) {
						reportInvalidInput(player);
					}
					if (debug)
						log.info("DLOAD: Player " + pName + " called setwand");
					return true;
				}
				
				// Dumps player info to server console
				if (args[0].equalsIgnoreCase("dbgdump")) {
					if (debug) {
						this.dlUsers.get(player).dbgPrintInfo();
						player.sendMessage("Debug info dumped to server console by player "
								+ pName);
					}
					return true;
				}
				
				// Sets area and clear to false, and sets default item and
				// amount
				if (args[0].equalsIgnoreCase("reset")) {
					pConfig.blockAreaMode = false;
					pConfig.singleClearMode = false;
					pConfig.singleFillMode = false;
					pConfig.fillOnce = false;
					pConfig.clearOnce = false;
					pConfig.material = 262;
					pConfig.amount = 64;
					pConfig.chestMode = false;
					player.sendMessage(ChatColor.DARK_AQUA
							+ "Mode reset to single-block adding with 64 arrows");
					if (debug)
						log.info("DLOAD: Player " + pName + " called reset.");
					return true;
				}
				
				// Sets default item and amount
				if (args[0].equalsIgnoreCase("arrows")) {
					pConfig.material = 262;
					pConfig.amount = 64;
					player.sendMessage(ChatColor.DARK_AQUA
							+ "DispenserLoader set to load 64 arrows");
					if (debug)
						log.info("DLOAD: player " + pName + " called arrows");
					return true;
				}
				
				// Messages the player with info about their current settings
				if (args[0].equalsIgnoreCase("info")) {
					player.sendMessage(ChatColor.DARK_AQUA + "Area mode: "
							+ pConfig.blockAreaMode);
					player.sendMessage(ChatColor.DARK_AQUA + "Clear mode: "
							+ pConfig.singleClearMode);
					player.sendMessage(ChatColor.DARK_AQUA + "Fill mode: "
							+ pConfig.singleFillMode);
					player.sendMessage(ChatColor.DARK_AQUA + "Item: "
							+ pConfig.material);
					player.sendMessage(ChatColor.DARK_AQUA + "Amount: "
							+ pConfig.amount);
					if (debug)
						log.info("DLOAD: player " + pName + " called info");
					return true;
				}
				
				// Area mode toggle with argument length check
				if (args[0].equalsIgnoreCase("area")) {
					if (argLength > 1) {
						player.sendMessage(ChatColor.RED
								+ "Too many arguments: use \"dload area\" to toggle area mode");
						return true;
					}
					pConfig.toggleAreaMode();
					if (debug)
						log.info("DLOAD: " + pName
								+ "'s area mode toggled (area mode flag now "
								+ pConfig.blockAreaMode + ").");
					return true;
				}
				
				// Start of area-related code, setting up some relevant
				// variables
				boolean areaEmpty = false;
				boolean areaFill = false;
				boolean areaOperation = false;
				String areaReportString = "";
				int areaMaterial = pConfig.material;
				short areaDamage = pConfig.damageValue;
				int areaAmount = pConfig.amount;
				
				// Toggles individual dispender empty mode
				if (args[0].equalsIgnoreCase("empty")) {
					if (!pConfig.blockAreaMode) {
						boolean once = false;
						if (argLength > 1) {
							if (args[1] == "once") {
								once = true;
							}
						}
						pConfig.toggleSingleClearFlag(once);
						if (debug)
							log.info("DLOAD: " + pName
									+ "'s single empty mode toggled (flag now "
									+ pConfig.singleClearMode + ").");
						return true;
					} else {
						areaEmpty = true;
						areaOperation = true;
						areaReportString = "emptied";
					}
				}
				
				// Toggles individual dispenser fill up mode
				if (args[0].equalsIgnoreCase("fill")) {
					if (!pConfig.blockAreaMode) {
						boolean once = false;
						if (argLength > 1 && args[1] == "once") {
							once = true;
						}
						pConfig.toggleSingleFillFlag(once);
						if (debug)
							log.info("DLOAD: " + pName
									+ "'s single fill mode toggled (flag now "
									+ pConfig.singleFillMode + ").");
						return true;
					} else {
						if (argLength == 2) {
							try {
								ItemStack stack = getMaterialAndDamage(args[1]);
								areaMaterial = stack.getTypeId();
								areaDamage = stack.getDurability();
							} catch (NumberFormatException e) {
								player.sendMessage(ChatColor.RED
										+ "Invalid material number and/or damage value");
								return true;
							}
						}
						areaFill = true;
						areaOperation = true;
						areaReportString = "filled up";
					}
				}
				
				// Single block mode: sets single-block filling mode, keeps item
				// settings
				// Area mode: performs area add operation
				if (args[0].equalsIgnoreCase("add")) {
					if (!pConfig.blockAreaMode) {
						pConfig.singleClearMode = false;
						pConfig.singleFillMode = false;
						player.sendMessage(ChatColor.DARK_AQUA
								+ "Mode reset to default (single-block adding mode)");
						if (debug)
							log.info("DLOAD: player " + pName
									+ " reset to single add mode.");
						return true;
					} else {
						if (argLength >= 2) {
							try {
								ItemStack stack = getMaterialAndDamage(args[1]);
								areaMaterial = stack.getTypeId();
								areaDamage = stack.getDurability();
								if (argLength == 3)
									areaAmount = Integer.parseInt(args[2]);
							} catch (NumberFormatException e) {
								player.sendMessage(ChatColor.RED
										+ "Invalid material number and/or damage value");
								return true;
							}
						}
						areaOperation = true;
						areaReportString = "filled";
					}
				}
				
				// Performs area operation if one of the area commands has been
				// used in area mode
				if (areaOperation) {
					if (pConfig.areaBlock1 instanceof Block
							&& pConfig.areaBlock2 instanceof Block) {
						int i_blocksChanged = blockInterface.areaEffect(
								pConfig.areaBlock1, pConfig.areaBlock2,
								areaMaterial, areaAmount, areaDamage, areaFill,
								areaEmpty, dlUsers.get(player));
						player.sendMessage(ChatColor.DARK_AQUA
								+ ""
								+ i_blocksChanged
								+ (pConfig.chestMode ? " chests "
										: " dispensers ") + areaReportString
								+ ".");
						if (debug)
							log.info("DLOAD: areaEffect called by player "
									+ pName);
					} else {
						player.sendMessage(ChatColor.RED
								+ "Select the opposite corners of the selection area first");
					}
					return true;
				}
				
				// If player entered 2 ints, set these to material and amount
				if (argLength == 2) {
					try {
						ItemStack stack = getMaterialAndDamage(args[0]);
						pConfig.material = stack.getTypeId();
						pConfig.damageValue = stack.getDurability();
						pConfig.amount = Integer.parseInt(args[1]);
					} catch (NumberFormatException e) {
						player.sendMessage(ChatColor.RED
								+ "Invalid material number and/or damage value");
						return true;
					}
					
					player.sendMessage(ChatColor.DARK_AQUA + "Material set to "
							+ pConfig.material + ":" + pConfig.damageValue
							+ ", amount set to " + pConfig.amount + ".");
					if (debug)
						log.info("DLOAD: player " + pName
								+ "assigned material " + pConfig.material + ":"
								+ pConfig.damageValue + " and amount "
								+ pConfig.amount + ".");
					return true;
				}
				
				// If player entered 1 int, set this to material
				if (argLength == 1) {
					try {
						ItemStack stack = getMaterialAndDamage(args[0]);
						pConfig.material = stack.getTypeId();
						pConfig.damageValue = stack.getDurability();
					} catch (NumberFormatException e) {
						player.sendMessage(ChatColor.RED
								+ "Invalid material number and/or damage value");
						return true;
					}
					player.sendMessage(ChatColor.DARK_AQUA + "Material set to "
							+ pConfig.material + ".");
					if (debug)
						log.info("DLOAD: player " + pName
								+ "assigned material " + pConfig.material + ".");
					return true;
				}
				
				// Return if we ever reach this point (all valid commands
				// already handled)
				if (argLength > 2) {
					reportInvalidInput(sender);
					return true;
				}
			} // End of word argument handling block
		} // End of dload check
		return false;
	} // End of onCommand code
	
	/**
	 * 
	 * @param player
	 *            The player to check
	 * @return True if player is allowed, otherwise false
	 */
	private boolean playerAllowed(Player player) {
		if (this.getServer().getPluginManager().getPlugin("Permissions") != null
				&& DispenserLoader.permissionHandler != null) {
			if (DispenserLoader.permissionHandler.has(player, "dload.use")) {
				return true;
			}
		}
		if (player.isOp())
			return true;
		
		return false;
	}
	
	/**
	 * 
	 * @param s
	 *            commandSender to report to
	 */
	private void reportInvalidInput(CommandSender s) {
		s.sendMessage(ChatColor.RED
				+ "Invalid command. For help, type /dload help or see the command reference");
	}
	
	/**
	 * 
	 * @param player
	 *            The player to give the wand to
	 */
	private void giveWand(Player player) {
		if (!player.getInventory().contains(dlUsers.get(player).wandItem)) {
			ItemStack wand = new ItemStack(dlUsers.get(player).wandItem, 1);
			player.getInventory().addItem(wand);
		}
	}
	
	/**
	 * Sets up the permissions handler for the plugin
	 */
	private void setupPermissions() {
		Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin(
				"Permissions");
		if (DispenserLoader.permissionHandler == null) {
			if (permissionsPlugin != null) {
				DispenserLoader.permissionHandler = ((Permissions) permissionsPlugin).getHandler();
				log.info("DispenserLoader: Using Permissions system");
			} else {
				log.info("DispenserLoader: Permission system not detected, using default permissions");
			}
		}
	}
	
	/**
	 * 
	 * @param sender
	 *            Console that sent the command
	 * @param args
	 *            Arguments passed to the function
	 */
	private void handleConsoleInput(CommandSender sender, String[] args) {
		if (args[0].equalsIgnoreCase("userinfo")) {
			String playerName = args[1];
			if (debug)
				log.info("playerName = " + playerName);
			Player player = sender.getServer().getPlayer(playerName);
			if (player != null) {
				this.dlUsers.get(player).dbgPrintInfo();
			} else {
				sender.sendMessage("Player not found, ensure the player is online and that you have entered a valid name");
			}
		}
		if (args[0].equalsIgnoreCase("debug")) {
			if (debug) {
				this.debug = false;
				sender.sendMessage("Debug mode disabled");
			} else {
				this.debug = true;
				sender.sendMessage("Debug mode enabled, prepare to be spammed with operation and debug messages");
			}
		}
	}
	
	private ItemStack getMaterialAndDamage(String str)
			throws NumberFormatException {
		int mat = 0;
		short dmg = 0;
		
		if (str.contains(":")) {
			String[] subs = str.split(":");
			Short.parseShort(subs[1]);
			mat = Integer.parseInt(subs[0]);
			dmg = Short.parseShort(subs[1]);
		} else {
			mat = Integer.parseInt(str);
			dmg = 0;
		}
		return new ItemStack(mat, 64, dmg);
	}
}