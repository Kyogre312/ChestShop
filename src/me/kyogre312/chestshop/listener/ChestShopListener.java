package me.kyogre312.chestshop.listener;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import me.kyogre312.chestshop.MySQL;
import me.kyogre312.chestshop.guis.AdminChestshopGUI;
import me.kyogre312.chestshop.guis.ChestShopGUI;
import me.kyogre312.chestshop.guis.FreeShopGUI;
import me.kyogre312.chestshop.guis.PlotChestShopGUI;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.core.plot.Plot;

import net.milkbowl.vault.economy.Economy;

public class ChestShopListener implements Listener {
	private JavaPlugin plugin;
	private MySQL mysql;
	private Economy econ;
	private String economy;
    private boolean usebank;
	private Map<String, String> permissions;
	private Map<String, String> displaynames;
	
	public ChestShopListener(JavaPlugin plugin, MySQL mysql, Economy econ, String economy, boolean usebank, Map<String, String> permissions, Map<String, String> displaynames) {
		this.plugin = plugin;
		this.mysql = mysql;
		this.econ = econ;
		this.economy = economy;
        this.usebank = usebank;
		this.permissions = permissions;
		this.displaynames = displaynames;
	}
	
	@EventHandler
	public void onClick(PlayerInteractEvent event) {
		if(!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			return;
		}
		if(!event.getHand().equals(EquipmentSlot.HAND)) {
			return;
		}
		Block block = event.getClickedBlock();
		if(!(block.getState() instanceof Sign)) {
			return;
		}
		Sign sign = (Sign) block.getState();
		if(!sign.getSide(Side.BACK).getLine(0).startsWith("§cCSH")) {
			return;
		}
		int id;
		try {
			id = Integer.parseInt(sign.getSide(Side.BACK).getLine(0).substring(5));
			event.setCancelled(true);
		} catch(NumberFormatException ex) {
			return;
		}
		try {
			if(!mysql.idExist(id)) {
				return;
			}
			UUID owner = mysql.getOwner(id);
			Location loc = mysql.getSignLocation(id);
			if(!loc.equals(block.getLocation())) {
				return;
			}
			Plot plot = Plot.getPlot(BukkitUtil.adapt(loc));
			if(plot == null) {
				return;
			}
			if(plot.getOwner() == null) {
				return;
			}
			if(!plot.getOwner().equals(owner)) {
				event.getPlayer().sendMessage("§cChestShops müssen nach einem Besitzerwechsel erneut erstellt werden.");
				return;
			}
			String desc = mysql.getDescription(id);
			int mode = mysql.getMode(id);
			if(mode == 6) {
				BigDecimal price = mysql.getPrice(id);
				new PlotChestShopGUI(id, mysql, plugin, desc, price, owner.toString(), plot, econ, economy, usebank).open(event.getPlayer());
				return;
			}
			ItemStack item = mysql.getItem(id);
			if(mode == 4 || mode == 5) {
				BigDecimal price = mysql.getPrice(id);
				new AdminChestshopGUI(id, mysql, plugin, desc, item, price, mode == 5, owner.toString(), plot, econ, economy).open(event.getPlayer());
				return;
			}
			if(mode == 7) {
				String rank = mysql.getFreeShopRank(id);
				if(!permissions.containsKey(rank)) {
					event.getPlayer().sendMessage("§cDer Rang existiert nicht mehr!");
					return;
				}
				new FreeShopGUI(id, mysql, plugin, desc, item, null, owner.toString(), plot, event.getPlayer().hasPermission(permissions.get(rank)), mysql.getRestTime(event.getPlayer().getUniqueId(), id), displaynames.get(rank), mysql.getFreeShopDelay(id), permissions.get(rank)).open(event.getPlayer());
				return;
			}
			Location chestLoc = mysql.getChestLocation(id);
			Plot chestPlot = Plot.getPlot(BukkitUtil.adapt(chestLoc));
			if(chestPlot == null) {
				return;
			}
			if(!chestPlot.equals(plot)) {
				return;
			}
			if(!(chestLoc.getBlock().getState() instanceof Container)) {
				return;
			}
			if(mode == 3) {
				String rank = mysql.getFreeShopRank(id);
				if(!permissions.containsKey(rank)) {
					event.getPlayer().sendMessage("§cDer Rang existiert nicht mehr!");
					return;
				}
				new FreeShopGUI(id, mysql, plugin, desc, item, chestLoc, owner.toString(), plot, event.getPlayer().hasPermission(permissions.get(rank)), mysql.getRestTime(event.getPlayer().getUniqueId(), id), displaynames.get(rank), mysql.getFreeShopDelay(id), permissions.get(rank)).open(event.getPlayer());
				return;
			}
			BigDecimal price = mysql.getPrice(id);
			new ChestShopGUI(id, mysql, plugin, desc, item, price, mode == 2, chestLoc, plot.getOwner().toString(), plot, econ, economy, usebank).open(event.getPlayer());
		} catch(Exception e) {
			e.printStackTrace();
			event.getPlayer().sendMessage("§4Ein Fehler ist aufgetreten!");
			return;
		}
	}

}
