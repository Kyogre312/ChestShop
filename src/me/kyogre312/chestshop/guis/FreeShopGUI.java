package me.kyogre312.chestshop.guis;

import com.plotsquared.core.plot.Plot;
import me.kyogre312.chestshop.MySQL;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class FreeShopGUI extends InventoryGUI {
	private String desc;
	private MySQL mysql;
	private ItemStack item;
	private Location chestLoc;
	private String owner;
	private int id;
	private Plot plot;
	private boolean haspermission;
	private String permission;

	public FreeShopGUI(int id, MySQL mysql, JavaPlugin plugin, String desc, ItemStack item, Location chestLoc, String owner, Plot plot, boolean haspermission, long rest, String rank, long delay, String permission) {
		super(plugin, desc, 3);
		this.id = id;
		this.desc = desc;
		this.mysql = mysql;
		this.item = item;
		this.chestLoc = chestLoc;
		this.owner = owner;
		this.plot = plot;
		this.haspermission = haspermission;
		this.permission = permission;
		setItem(4, item);
		if(!haspermission) {
			setItem(4+9, Material.RED_STAINED_GLASS_PANE, "§cKeine Berechtigung!");
		} else {
			if(rest == 0) {
				setItem(4+9, Material.LIME_STAINED_GLASS_PANE, "§aJetzt abholen!");
			} else if(rest < 0) {
				setItem(4+9, Material.ORANGE_STAINED_GLASS_PANE, "§6Bereits abgeholt!");
			} else {
				setItem(4+9, Material.YELLOW_STAINED_GLASS_PANE, "§eAbholbar in:", "§7" + dauer(rest));
			}
		}
		setItem(4+9+9, Material.OAK_SIGN, "§5FreeShop", "§8Mindestrang: §7" + rank, "§8Zeitabstand: §7" + dauer(delay));
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
			Inventory container = null;
			if(chestLoc != null) {
				if(!(chestLoc.getBlock().getState() instanceof Container)) {
					paramPlayer.closeInventory();
					return;
				}
				container = ((Container) chestLoc.getBlock().getState()).getInventory();
			}
			if(paramInt == 4+9) {
				if(!paramPlayer.hasPermission(permission)) {
					setItem(4+9, Material.RED_STAINED_GLASS_PANE, "§cKeine Berechtigung!");
					return;
				}
				long rest = mysql.getRestTime(paramPlayer.getUniqueId(), id);
				if(rest != 0) {
					if(rest > 0) {
						setItem(4+9, Material.YELLOW_STAINED_GLASS_PANE, "§eAbholbar in:", "§7" + dauer(rest));
					}
					return;
				}
				if(container != null) {
					if(checkInvItems(item, container.getContents()) < (item.getAmount())) {
						paramPlayer.sendMessage("§cDer Behälter ist leer!");
						setItem(4+9, Material.LIME_STAINED_GLASS_PANE, "§aJetzt abholen!");
						return;
					}
				}
				if(checkInvSpace(item, paramPlayer.getInventory().getStorageContents()) < (item.getAmount())) {
					paramPlayer.sendMessage("§cDein Inventar ist voll!");
					setItem(4+9, Material.LIME_STAINED_GLASS_PANE, "§aJetzt abholen!");
					return;
				}
				rest = mysql.useFreeShop(paramPlayer.getUniqueId(), id);
				if(rest == 0) {
					setItem(4+9, Material.LIME_STAINED_GLASS_PANE, "§aJetzt abholen!");
				} else if(rest < 0) {
					setItem(4+9, Material.ORANGE_STAINED_GLASS_PANE, "§6Bereits abgeholt!");
				} else {
					setItem(4+9, Material.YELLOW_STAINED_GLASS_PANE, "§eAbholbar in:", "§7" + dauer(rest));
				}
				if(container != null) {
					int entfernen = item.getAmount();
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
				} else {
					paramPlayer.getInventory().addItem(new ItemStack(item));
				}
				if(container != null) {
					Player powner = Bukkit.getPlayer(UUID.fromString(owner));
					if(powner != null) {
						powner.sendMessage("§9" + paramPlayer.getName() + " §7hat §e" + item.getAmount() + " Stück §7von §6" + desc + " §7abgeholt.");
					}
				} else {
					Bukkit.broadcast("§9" + paramPlayer.getName() + " §7hat §e" + item.getAmount() + " Stück §7von §6" + desc + " §7abgeholt.", "chestshop.admin");
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

	private String dauer(long time) {
		if(time < 0) {
			return "Einmalig";
		}
		if(time == 0) {
			return "Unendlich";
		}
		if (time < 1000L) {
			return "Jetzt";
		}
		StringBuilder builder = new StringBuilder();
		if (time >= 31536000000L) {
			long h = time / 31536000000L;
			time -= h * 31536000000L;
			builder.append(String.valueOf(h) + ((h == 1L) ? " Jahr " : " Jahre "));
		}
		if (time >= 2592000000L) {
			long h = time / 2592000000L;
			time -= h * 2592000000L;
			builder.append(String.valueOf(h) + ((h == 1L) ? " Monat " : " Monate "));
		}
		if (time >= 604800000L) {
			long h = time / 604800000L;
			time -= h * 604800000L;
			builder.append(String.valueOf(h) + ((h == 1L) ? " Woche " : " Wochen "));
		}
		if (time >= 86400000L) {
			long h = time / 86400000L;
			time -= h * 86400000L;
			builder.append(String.valueOf(h) + ((h == 1L) ? " Tag " : " Tage "));
		}
		if (time >= 3600000L) {
			long h = time / 3600000L;
			time -= h * 3600000L;
			builder.append(String.valueOf(h) + ((h == 1L) ? " Stunde " : " Stunden "));
		}
		if (time >= 60000L) {
			long h = time / 60000L;
			time -= h * 60000L;
			builder.append(String.valueOf(h) + ((h == 1L) ? " Minute " : " Minuten "));
		}
		if (time >= 1000L) {
			long h = time / 1000L;
			time -= h * 1000L;
			builder.append(String.valueOf(h) + ((h == 1L) ? " Sekunde " : " Sekunden "));
		}
		String string = builder.toString();
		return string.substring(0, string.length() - 1);
	}

}
