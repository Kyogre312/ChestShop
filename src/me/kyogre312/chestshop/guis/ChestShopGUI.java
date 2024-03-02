package me.kyogre312.chestshop.guis;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.UUID;

import me.kyogre312.chestshop.MySQL;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.plotsquared.core.plot.Plot;

import net.milkbowl.vault.economy.Economy;

public class ChestShopGUI extends InventoryGUI {
	private String desc;
	private MySQL mysql;
	private ItemStack item;
	private BigDecimal price;
	private boolean ankauf;
	private Location chestLoc;
	private String owner;
	private int id;
	private Plot plot;
	private Economy econ;
	private String economy;
	private boolean usebank;
	
	public ChestShopGUI(int id, MySQL mysql, JavaPlugin plugin, String desc, ItemStack item, BigDecimal price, boolean ankauf, Location chestLoc, String owner, Plot plot, Economy econ, String economy, boolean usebank) {
		super(plugin, desc, 3);
		this.id = id;
		this.desc = desc;
		this.mysql = mysql;
		this.item = item;
		this.price = price;
		this.ankauf = ankauf;
		this.chestLoc = chestLoc;
		this.owner = owner;
		this.plot = plot;
		this.economy = economy;
		this.usebank = usebank;
		setItem(4, item);
		if(ankauf) {
			setItem(10, Material.RED_DYE, "§c" + item.getAmount() + " verkaufen", "§7" + DecimalFormat.getNumberInstance(Locale.GERMAN).format(price) + economy + "");
			setItem(11, 2, Material.RED_DYE, "§c" + item.getAmount()*2 + " verkaufen", "§7" + DecimalFormat.getNumberInstance(Locale.GERMAN).format(price.multiply(new BigDecimal(2))) + economy + "");
			setItem(12, 4, Material.RED_DYE, "§c" + item.getAmount()*4 + " verkaufen", "§7" + DecimalFormat.getNumberInstance(Locale.GERMAN).format(price.multiply(new BigDecimal(4))) + economy + "");
			setItem(13, 8, Material.RED_DYE, "§c" + item.getAmount()*8 + " verkaufen", "§7" + DecimalFormat.getNumberInstance(Locale.GERMAN).format(price.multiply(new BigDecimal(8))) + economy + "");
			setItem(14, 16, Material.RED_DYE, "§c" + item.getAmount()*16 + " verkaufen", "§7" + DecimalFormat.getNumberInstance(Locale.GERMAN).format(price.multiply(new BigDecimal(16))) + economy + "");
			setItem(15, 32, Material.RED_DYE, "§c" + item.getAmount()*32 + " verkaufen", "§7" + DecimalFormat.getNumberInstance(Locale.GERMAN).format(price.multiply(new BigDecimal(32))) + economy + "");
			setItem(16, 64, Material.RED_DYE, "§c" + item.getAmount()*64 + " verkaufen", "§7" + DecimalFormat.getNumberInstance(Locale.GERMAN).format(price.multiply(new BigDecimal(64))) + economy + "");
			setItem(22, Material.CHEST, "§cMaximale Anzahl verkaufen", "§7" + DecimalFormat.getNumberInstance(Locale.GERMAN).format(price) + economy + " je " + (item.getAmount() != 1 ? item.getAmount() + "er-Stack" : "Stück"));
		} else {
			setItem(10, Material.LIME_DYE, "§a" + item.getAmount() + " kaufen", "§7" + DecimalFormat.getNumberInstance(Locale.GERMAN).format(price) + economy + "");
			setItem(11, 2, Material.LIME_DYE, "§a" + item.getAmount()*2 + " kaufen", "§7" + DecimalFormat.getNumberInstance(Locale.GERMAN).format(price.multiply(new BigDecimal(2))) + economy + "");
			setItem(12, 4, Material.LIME_DYE, "§a" + item.getAmount()*4 + " kaufen", "§7" + DecimalFormat.getNumberInstance(Locale.GERMAN).format(price.multiply(new BigDecimal(4))) + economy + "");
			setItem(13, 8, Material.LIME_DYE, "§a" + item.getAmount()*8 + " kaufen", "§7" + DecimalFormat.getNumberInstance(Locale.GERMAN).format(price.multiply(new BigDecimal(8))) + economy + "");
			setItem(14, 16, Material.LIME_DYE, "§a" + item.getAmount()*16 + " kaufen", "§7" + DecimalFormat.getNumberInstance(Locale.GERMAN).format(price.multiply(new BigDecimal(16))) + economy + "");
			setItem(15, 32, Material.LIME_DYE, "§a" + item.getAmount()*32 + " kaufen", "§7" + DecimalFormat.getNumberInstance(Locale.GERMAN).format(price.multiply(new BigDecimal(32))) + economy + "");
			setItem(16, 64, Material.LIME_DYE, "§a" + item.getAmount()*64 + " kaufen", "§7" + DecimalFormat.getNumberInstance(Locale.GERMAN).format(price.multiply(new BigDecimal(64))) + economy + "");
			setItem(22, Material.CHEST, "§aMaximale Anzahl kaufen", "§7" + DecimalFormat.getNumberInstance(Locale.GERMAN).format(price) + economy + " je " + (item.getAmount() != 1 ? item.getAmount() + "er-Stack" : "Stück"));
		}
		this.econ = econ;
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
			if(!(chestLoc.getBlock().getState() instanceof Container)) {
				paramPlayer.closeInventory();
				return;
			}
			Inventory container = ((Container) chestLoc.getBlock().getState()).getInventory();
			int anzahl;
			if(paramInt == 10) {
				anzahl = 1;
			} else if(paramInt == 11) {
				anzahl = 2;
			} else if(paramInt == 12) {
				anzahl = 4;
			} else if(paramInt == 13) {
				anzahl = 8;
			} else if(paramInt == 14) {
				anzahl = 16;
			} else if(paramInt == 15) {
				anzahl = 32;
			} else if(paramInt == 16) {
				anzahl = 64;
			} else if(paramInt == 22) {
				BigDecimal kontostand = !ankauf ? new BigDecimal(econ.getBalance(paramPlayer)) : Bukkit.getPlayer(UUID.fromString(owner)) != null || !usebank ? new BigDecimal(econ.getBalance(Bukkit.getOfflinePlayer(UUID.fromString(owner)))) : mysql.getBank(UUID.fromString(owner));
				BigDecimal geld = kontostand.divide(price, 0, RoundingMode.DOWN);
				int platz = !ankauf ? checkInvSpace(item, paramPlayer.getInventory().getStorageContents()) : checkInvSpace(item, container.getContents());
				platz = platz/item.getAmount();
				int items = !ankauf ? checkInvItems(item, container.getContents()) : checkInvItems(item, paramPlayer.getInventory().getStorageContents());
				items = items/item.getAmount();
				anzahl = geld.compareTo(new BigDecimal(64*9*4)) >= 0 ? (64*9*4) : geld.intValue();
				if(platz < anzahl) {
					anzahl = platz;
				}
				if(items < anzahl) {
					anzahl = items;
				}
				if(anzahl == 0) {
					anzahl = 1;
				}
			} else {
				return;
			}
			if(!ankauf) {
				if(checkInvItems(item, container.getContents()) < (anzahl*item.getAmount())) {
					paramPlayer.sendMessage("§cDer Behälter ist leer!");
					return;
				}
				if(checkInvSpace(item, paramPlayer.getInventory().getStorageContents()) < (anzahl*item.getAmount())) {
					paramPlayer.sendMessage("§cDein Inventar ist voll!");
					return;
				}
				BigDecimal betrag = price.multiply(new BigDecimal(anzahl));
				if(!econ.has(paramPlayer, betrag.doubleValue())) {
					paramPlayer.sendMessage("§cDu hast nicht genug Geld!");
					return;
				}
				econ.withdrawPlayer(paramPlayer, betrag.doubleValue());
				Player powner = Bukkit.getPlayer(UUID.fromString(owner));
				if(powner != null || !usebank) {
					econ.depositPlayer(Bukkit.getOfflinePlayer(UUID.fromString(owner)), betrag.doubleValue());
				} else {
					mysql.setBank(UUID.fromString(owner), mysql.getBank(UUID.fromString(owner)).add(betrag));
				}
				int entfernen = anzahl*item.getAmount();
				for(ItemStack item : container.getContents()) {
					if(item == null) {
						continue;
					}
					if(item.isSimilar(this.item)) {
						if(item.getAmount() < entfernen) {
							paramPlayer.getInventory().addItem(new ItemStack(item));
							entfernen -= item.getAmount();
							item.setAmount(0);
						} else {
							ItemStack neu = new ItemStack(item);
							neu.setAmount(entfernen);
							paramPlayer.getInventory().addItem(neu);
							item.setAmount(item.getAmount()-entfernen);
							break;
						}
					}
				}
				if(powner != null) {
					powner.sendMessage("§9" + paramPlayer.getName() + " §7hat §e" + anzahl*item.getAmount() + " Stück §7von §6" + desc + " §7gekauft (§a+" + DecimalFormat.getNumberInstance(Locale.GERMAN).format(betrag) + economy + "§7).");
				}
			} else {
				if(checkInvItems(item, paramPlayer.getInventory().getStorageContents()) < (anzahl*item.getAmount())) {
					paramPlayer.sendMessage("§cDu hast nicht genügend Items!");
					return;
				}
				if(checkInvSpace(item, container.getContents()) < (anzahl*item.getAmount())) {
					paramPlayer.sendMessage("§cDer Behälter ist voll!");
					return;
				}
				BigDecimal betrag = price.multiply(new BigDecimal(anzahl));
				Player powner = Bukkit.getPlayer(UUID.fromString(owner));
				if(powner != null || !usebank) {
					if(!econ.has(Bukkit.getOfflinePlayer(UUID.fromString(owner)), betrag.doubleValue())) {
						paramPlayer.sendMessage("§cDer Besitzer hat nicht genug Geld!");
						return;
					}
					econ.withdrawPlayer(Bukkit.getOfflinePlayer(UUID.fromString(owner)), betrag.doubleValue());
				} else {
					BigDecimal bank = mysql.getBank(UUID.fromString(owner));
					if(bank.compareTo(betrag) < 0) {
						paramPlayer.sendMessage("§cDer Besitzer hat nicht genug Geld!");
						return;
					}
					mysql.setBank(UUID.fromString(owner), bank.subtract(betrag));
				}
				econ.depositPlayer(paramPlayer, betrag.doubleValue());
				int entfernen = anzahl*item.getAmount();
				for(ItemStack item : paramPlayer.getInventory().getStorageContents()) {
					if(item == null) {
						continue;
					}
					if(item.isSimilar(this.item)) {
						if(item.getAmount() < entfernen) {
							container.addItem(new ItemStack(item));
							entfernen -= item.getAmount();
							item.setAmount(0);
						} else {
							ItemStack neu = new ItemStack(item);
							neu.setAmount(entfernen);
							container.addItem(neu);
							item.setAmount(item.getAmount()-entfernen);
							break;
						}
					}
				}
				if(powner != null) {
					powner.sendMessage("§9" + paramPlayer.getName() + " §7hat §e" + anzahl*item.getAmount() + " Stück §7von §6" + desc + " §7verkauft (§c-" + DecimalFormat.getNumberInstance(Locale.GERMAN).format(betrag) + economy + "§7).");
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			paramPlayer.sendMessage("§4Ein Fehler ist aufgetreten!");
			return;
		}
	}
	
	private int checkInvSpace(ItemStack stack, ItemStack[] inv) {
		int space = 0;
		for(ItemStack item : inv) {
			if(item == null) {
				space += stack.getMaxStackSize();
				continue;
			}
			if(item.isSimilar(stack)) {
				space += stack.getMaxStackSize()-item.getAmount();
				continue;
			}
		}
		return space;
	}
	
	private int checkInvItems(ItemStack stack, ItemStack[] inv) {
		int space = 0;
		for(ItemStack item : inv) {
			if(item == null) {
				continue;
			}
			if(item.isSimilar(stack)) {
				space += item.getAmount();
				continue;
			}
		}
		return space;
	}

}
