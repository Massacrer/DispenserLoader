package me.Massacrer.DispenserLoader;

import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.ContainerBlock;
import org.bukkit.inventory.ItemStack;

public class DLBlockInterface {
	private DispenserLoader plugin = null;
	
	DLBlockInterface(DispenserLoader plugin) {
		this.plugin = plugin;
	}
	
	/**
	 * Causes an effect to multiple dispensers by determining the 3D area that
	 * is selected, and then looping to cover this area
	 * 
	 * @param first
	 *            First block in selection
	 * @param second
	 *            Second block in selection
	 * @param material
	 *            Material to update the dispensers with
	 * @param amount
	 *            Amount of material to update the dispensers with
	 * @param fill
	 *            Whether or not to fill the dispensers up
	 * @param clear
	 *            Whether or not to clear the dispensers
	 * @param pConfig
	 *            TODO
	 * @return number of dispensers modified
	 */
	int areaEffect(Block first, Block second, int material, int amount,
			short damage, boolean fill, boolean clear, DLPlayerConfig pConfig) {
		int numberChanged = 0;
		int x1 = first.getX();
		int y1 = first.getY();
		int z1 = first.getZ();
		int x2 = second.getX();
		int y2 = second.getY();
		int z2 = second.getZ();
		
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
		
		for (int a = x1; a <= x2; a++) {
			for (int b = y1; b <= y2; b++) {
				for (int c = z1; c <= z2; c++) {
					// Gets the block at the coordinates at the current point in
					// the for loops
					Block block = (new Location(first.getWorld(), a, b, c, 0, 0)).getBlock();
					// Obligatory check for dispenser blocks
					if (block.getType() == (pConfig.chestMode ? Material.CHEST
							: Material.DISPENSER)) {
						// If command was /area empty, empty inventories
						if (clear) {
							emptyContainer((ContainerBlock) block.getState());
							numberChanged++;
							continue;
						}
						// If command was /area fill
						if (fill) {
							fill((ContainerBlock) block.getState(), material,
									damage);
							numberChanged++;
							continue;
						}
						// If command was /area add
						if (!fill && !clear) {
							add((ContainerBlock) block.getState(), material,
									amount, damage);
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
	 * Fills a single dispenser with material
	 * 
	 * @param target
	 *            Dispenser to fill
	 * @param material
	 *            Material to fill it with
	 */
	void fill(ContainerBlock target, int material, short damage) {
		ItemStack items = new ItemStack(material, 64, damage);
		HashMap<Integer, ItemStack> overflowItems = new HashMap<Integer, ItemStack>();
		do {
			overflowItems.putAll(target.getInventory().addItem(items));
		} while (overflowItems.isEmpty());
	}
	
	/**
	 * Empties a single dispenser
	 * 
	 * @param dispenser
	 *            Dispenser to empty
	 * @param container
	 */
	void emptyContainer(ContainerBlock container) {
		container.getInventory().clear();
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
	void add(ContainerBlock target, int material, int amount, short damage) {
		ArrayList<ItemStack> items = new ArrayList<ItemStack>();
		
		if (amount <= 64) {
			items.add(new ItemStack(material, amount, damage));
		}
		while (amount > 64) {
			items.add(new ItemStack(material, amount, damage));
			amount -= 64;
			if (amount < 64 && amount < 0) {
				items.add(new ItemStack(material, amount, damage));
			}
		}
		
		if (plugin.debug) {
			DLBlockListener.log.info("DL: aTD: items.size() = " + items.size());
		}
		for (int i = 0; i < items.size(); i++) {
			if (plugin.debug) {
				DLBlockListener.log.info("DL: addToContainerBlock: i = " + i
						+ ", i.items.getType() = " + items.get(i).getTypeId());
			}
			target.getInventory().addItem(items.get(i));
		}
	}
}
