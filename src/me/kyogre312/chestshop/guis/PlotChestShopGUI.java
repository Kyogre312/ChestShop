package me.kyogre312.chestshop.guis;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.UUID;

import me.kyogre312.chestshop.MySQL;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.core.plot.Plot;

import net.milkbowl.vault.economy.Economy;

public class PlotChestShopGUI extends InventoryGUI {
	private String desc;
	private MySQL mysql;
	private BigDecimal price;
	private String owner;
	private int id;
	private Plot plot;
	private Economy econ;
	private String economy;
	private boolean usebank;
	
	public PlotChestShopGUI(int id, MySQL mysql, JavaPlugin plugin, String desc, BigDecimal price, String owner, Plot plot, Economy econ, String economy, boolean usebank) {
		super(plugin, desc, 3);
		this.id = id;
		this.desc = desc;
		this.mysql = mysql;
		this.price = price;
		this.owner = owner;
		this.plot = plot;
		this.usebank = usebank;
		setItem(13, Material.OAK_SIGN, "§aGrundstück kaufen", "§7" + DecimalFormat.getNumberInstance(Locale.GERMAN).format(price) + economy);
		this.econ = econ;
		this.economy = economy;
	}

	@Override
	public void click(Player paramPlayer, int paramInt, boolean paramBoolean) {
		try {
			if(plot.getOwner() == null) {
				paramPlayer.closeInventory();
				return;
			}
			if(!plot.getOwner().toString().equals(owner)) {
				paramPlayer.closeInventory();
				return;
			}
			if(paramInt != 13) {
				return;
			}
			if(!paramPlayer.hasPermission("plots.plot.*")) {
				int rest = BukkitUtil.adapt(paramPlayer).getAllowedPlots() - BukkitUtil.adapt(paramPlayer).getPlotCount();
				if(rest < plot.getConnectedPlots().size()) {
					paramPlayer.sendMessage("§cDu kannst keine weiteren Grundstücke besitzen!");
					return;
				}
			}
			if(!econ.has(paramPlayer, price.doubleValue())) {
				paramPlayer.sendMessage("§cDu hast nicht genug Geld!");
				return;
			}
			econ.withdrawPlayer(paramPlayer, price.doubleValue());
			Player powner = Bukkit.getPlayer(UUID.fromString(owner));
			if(powner != null || !usebank) {
				econ.depositPlayer(Bukkit.getOfflinePlayer(UUID.fromString(owner)), price.doubleValue());
			} else {
				mysql.setBank(UUID.fromString(owner), mysql.getBank(UUID.fromString(owner)).add(price));
			}
			plot.setOwner(paramPlayer.getUniqueId());
			paramPlayer.sendMessage("§aDas Grundstück gehört nun dir!");
			paramPlayer.closeInventory();
			if(powner != null) {
				powner.sendMessage("§9" + paramPlayer.getName() + " §7hat §6" + desc + " §7gekauft (§a+" + DecimalFormat.getNumberInstance(Locale.GERMAN).format(price) + economy + "§7).");
			}
		} catch(Exception e) {
			e.printStackTrace();
			paramPlayer.sendMessage("§4Ein Fehler ist aufgetreten!");
			return;
		}
	}

}
