package me.Massacrer.DispenserLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;
import org.bukkit.block.Block;
import org.bukkit.block.ContainerBlock;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.inventory.ItemStack;

/**
 * Dispenser Loader block listener
 * 
 * @author Massacrer
 * 
 */

class DLBlockListener extends BlockListener {
	
	private final DispenserLoader plugin;
	static Logger log = DispenserLoader.log;
	
	DLBlockListener(final DispenserLoader plugin) {
		this.plugin = plugin;
	}
	
	/**
	 * Called by Bukkit, main code block for this class
	 */
	public void onBlockDamage(BlockDamageEvent event) {
		Block block = event.getBlock();
		Player player = event.getPlayer();
		DLPlayerConfig pConfig = plugin.dlUsers.get(player);
		
		// Checks is player is enabled, and whether the damage was caused with
		// the wand item
		if (plugin.enabled(player)
				&& player.getItemInHand().getTypeId() == pConfig.wandItem) {
			if (plugin.debug) {
				log.info("Block event recognised: player " + player.getName()
						+ ", block " + block.getX() + "," + block.getY() + ","
						+ block.getZ());
			}
			if (block.getType() == (pConfig.chestMode ? Material.CHEST
					: Material.DISPENSER)) {
				String blockTypeStringUCase = (pConfig.chestMode ? "Chest"
						: "Dispenser");
				// Material.DISPENSER if it isnt the
				// cause of the bug
				// Checks which mode the player is in: single block or cuboid
				if (pConfig.blockAreaMode == false) { // Code block for
														// single block mode
					ContainerBlock targetBlock = (ContainerBlock) event
							.getBlock().getState();
					if (pConfig.singleClearMode) {
						emptyContainer(targetBlock);
						if (pConfig.clearOnce) {
							pConfig.clearOnce = false;
							pConfig.singleClearMode = false;
						}
						player.sendMessage(ChatColor.DARK_AQUA
								+ blockTypeStringUCase + " emptied");
						if (plugin.debug)
							log.info("DLOAD: " + blockTypeStringUCase
									+ " hit by player " + player.getName()
									+ " in single clear mode.");
					}
					if (pConfig.singleFillMode) {
						fill(targetBlock, pConfig.material);
						if (pConfig.fillOnce) {
							plugin.dlUsers.get(player).fillOnce = false;
							plugin.dlUsers.get(player).singleFillMode = false;
						}
						player.sendMessage(ChatColor.DARK_AQUA
								+ blockTypeStringUCase
								+ " filled up with item type "
								+ pConfig.material);
						if (plugin.debug)
							log.info("DLOAD: " + blockTypeStringUCase
									+ " hit by player " + player.getName()
									+ " in single fill mode. Material: "
									+ pConfig.material + ".");
					}
					if (!pConfig.singleClearMode && !pConfig.singleFillMode) {
						add(targetBlock, pConfig.material, pConfig.amount);
						player.sendMessage(ChatColor.DARK_AQUA
								+ blockTypeStringUCase + " filled with "
								+ pConfig.amount + " of item type "
								+ pConfig.material);
						if (plugin.debug)
							log.info("DLOAD: " + blockTypeStringUCase
									+ " hit by player " + player.getName()
									+ " in single add mode. Material: "
									+ pConfig.material + ", amount: "
									+ pConfig.amount + ".");
					}
					
				}
			}
			if (pConfig.blockAreaMode == true) { // Code block for area
													// mode - accepts any block
													// hit.
				Block firstBlock = pConfig.areaBlock1;
				Block secondBlock = pConfig.areaBlock2;
				
				// If both blocks are defined
				if (firstBlock instanceof Block && secondBlock instanceof Block) {
					// If last block defined is block 1
					if (pConfig.lastAreaBlockSet == 1) {
						pConfig.areaBlock2 = block;
						sendBlockSetMsg(2, player, block);
						pConfig.lastAreaBlockSet = 2;
						if (plugin.debug) {
							log.info("both blocks defined for player "
									+ player.getName()
									+ ", lastblockset is now 2");
						}
					}
					// If last block defined is second block
					else {
						pConfig.areaBlock1 = block;
						sendBlockSetMsg(1, player, block);
						pConfig.lastAreaBlockSet = 1;
						if (plugin.debug) {
							log.info("both blocks already defined for player "
									+ player.getName()
									+ ", lastblockset is now 1");
						}
					}
					return;
				}
				
				// If first block is defined, but not second
				if (firstBlock instanceof Block
						&& !(secondBlock instanceof Block)) {
					pConfig.areaBlock2 = block;
					sendBlockSetMsg(2, player, block);
					pConfig.lastAreaBlockSet = 2;
					if (plugin.debug) {
						log.info("first block already defined for player "
								+ player.getName() + ", lastblockset is now 2");
					}
					return;
				}
				
				// If first block not defined (therefore neither is)
				if (!(firstBlock instanceof Block)) {
					pConfig.areaBlock1 = block;
					sendBlockSetMsg(1, player, block);
					pConfig.lastAreaBlockSet = 1;
					if (plugin.debug) {
						log.info("neither block already defined for player "
								+ player.getName() + ", lastblockset is now 1");
					}
					return;
				}
			} // End of area mode code
		} // End of run validity check code
	} // End of onBlockDamage code
	
	/**
	 * Sends the player a message that their block selection has been recognised
	 * 
	 * @param blockNo
	 *            Which block was set
	 * @param player
	 *            Player to inform
	 * @param block
	 *            Block that was selected
	 */
	void sendBlockSetMsg(int blockNo, Player player, Block block) {
		if (blockNo == 1) {
			player.sendMessage(ChatColor.DARK_AQUA + "First block set at "
					+ block.getX() + "," + block.getY() + "," + block.getZ());
		} else {
			player.sendMessage(ChatColor.DARK_AQUA + "Second block set at "
					+ block.getX() + "," + block.getY() + "," + block.getZ());
		}
	}
	
	// These numbers are important to the function of areaEffect()
	int x1 = 0;
	int x2 = 0;
	int y1 = 0;
	int y2 = 0;
	int z1 = 0;
	int z2 = 0;
	
	/**
	 * Takes 3 pairs of numbers and arranges them so that they are all in the
	 * order (smaller, larger). Used by areaEffect().
	 */
	void ascendingOrder() {
		int temp = 0;
		if (x1 > x2) {
			temp = x1;
			x1 = x2;
			x2 = temp;
		}
		if (y1 > y2) {
			temp = y1;
			y1 = y2;
			y2 = temp;
		}
		if (z1 > z2) {
			temp = z1;
			z1 = z2;
			z2 = temp;
		}
	}
	
	/**
	 * Causes an effect to multiple dispensers by determining the 3D area that
	 * is selected, and then looping to cover this area
	 * 
	 * @param first
	 *            First block in selection
	 * @param second
	 *            Second block in selection
	 * @param world
	 *            World the event was called in
	 * @param material
	 *            Material to update the dispensers with
	 * @param amount
	 *            Amount of material to update the dispensers with
	 * @param fill
	 *            Whether or not to fill the dispensers up
	 * @param clear
	 *            Whether or not to clear the dispensers
	 * @return number of dispensers modified
	 */
	int areaEffect(Block first, Block second, World world, int material,
			int amount, boolean fill, boolean clear, DLPlayerConfig pConfig) {
		int numberChanged = 0;
		x1 = first.getX();
		y1 = first.getY();
		z1 = first.getZ();
		x2 = second.getX();
		y2 = second.getY();
		z2 = second.getZ();
		ascendingOrder();
		
		for (int a = x1; a <= x2; a++) {
			for (int b = y1; b <= y2; b++) {
				for (int c = z1; c <= z2; c++) {
					// Gets the block at the coordinates at the current point in
					// the for loops
					Block block = (new Location(world, a, b, c, 0, 0))
							.getBlock();
					// Obligatory check for dispenser blocks
					if (block.getType() == (pConfig.chestMode ? Material.CHEST : Material.DISPENSER)) {
						// If command was /area empty, empty inventories
						if (clear) {
							emptyContainer((ContainerBlock) block.getState());
							numberChanged++;
							continue;
						}
						// If command was /area fill
						if (fill) {
							fill((ContainerBlock) block.getState(), material);
							numberChanged++;
							continue;
						}
						// If command was /area add
						if (!fill && !clear) {
							add((ContainerBlock) block.getState(), material, amount);
							numberChanged++;
							continue;
						}
					}
				}
			}
		}
		return numberChanged;
	}
	
	/**
	 * Empties a single dispenser
	 * 
	 * @param dispenser
	 *            Dispenser to empty
	 */
	void emptyContainer(ContainerBlock container) {
		container.getInventory().clear();
	}
	
	/**
	 * Fills a single dispenser with material
	 * 
	 * @param target
	 *            Dispenser to fill
	 * @param material
	 *            Material to fill it with
	 */
	void fill(ContainerBlock target, int material) {
		ItemStack items = new ItemStack(material, 64);
		HashMap<Integer, ItemStack> overflowItems = new HashMap<Integer, ItemStack>();
		do {
			overflowItems.putAll(target.getInventory().addItem(items));
		} while (overflowItems.isEmpty());
	}
	
	/**
	 * Adds an amount of material to a single dispenser
	 * 
	 * @param target
	 *            Dispenser to add to
	 * @param material
	 *            Material to put in
	 * @param amount
	 *            Amount of material to put in
	 */
	void add(ContainerBlock target, int material, int amount) {
		ArrayList<ItemStack> items = new ArrayList<ItemStack>();
		try {
			if (amount <= 64) {
				items.add(new ItemStack(material, amount));
			}
			while (amount > 64) {
				items.add(new ItemStack(material, amount));
				amount -= 64;
				if (amount < 64 && amount < 0) {
					items.add(new ItemStack(material, amount));
				}
			}
		} catch (ArrayIndexOutOfBoundsException ex) {
			log.warning("Unplanned exception thrown, see stack trace: ");
			ex.printStackTrace();
		}
		
		if (plugin.debug) {
			log.info("DL: aTD: items.size() = " + items.size());
		}
		for (int i = 0; i < items.size(); i++) {
			if (plugin.debug) {
				log.info("DL: addToContainerBlock: i = " + i
						+ ", i.items.getType() = " + items.get(i).getTypeId());
			}
			target.getInventory().addItem(items.get(i));
		}
	}
}
