package me.Massacrer.DispenserLoader;

import java.util.logging.Logger;
import org.bukkit.block.Block;
import org.bukkit.block.ContainerBlock;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockListener;

/**
 * Dispenser Loader block listener
 * 
 * @author Massacrer
 * 
 */

class DLBlockListener extends BlockListener {
	
	final DispenserLoader plugin;
	private DLBlockInterface blockInterface = null;
	static Logger log = DispenserLoader.log;
	
	DLBlockListener(final DispenserLoader plugin) {
		this.plugin = plugin;
		blockInterface = plugin.blockInterface;
	}
	
	/**
	 * Called by Bukkit, main code block for this class
	 */
	public void onBlockDamage(BlockDamageEvent event) {
		event.setCancelled(true);
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
					ContainerBlock targetBlock = (ContainerBlock) event.getBlock().getState();
					if (pConfig.singleClearMode) {
						blockInterface.emptyContainer(targetBlock);
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
						blockInterface.fill(targetBlock, pConfig.material,
								pConfig.damageValue);
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
						blockInterface.add(targetBlock, pConfig.material,
								pConfig.amount, pConfig.damageValue);
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
	
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
			event.setCancelled(true);
			BlockDamageEvent bde = new BlockDamageEvent(event.getPlayer(),
					event.getBlock(), event.getPlayer().getItemInHand(), false);
			bde.setCancelled(true);
			this.onBlockDamage(bde);
		}
	}
	
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
}
