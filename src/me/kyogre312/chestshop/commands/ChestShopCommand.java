package me.kyogre312.chestshop.commands;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import me.kyogre312.chestshop.MySQL;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.core.plot.Plot;

public class ChestShopCommand implements CommandExecutor {
	private MySQL mysql;
	private List<String> lines;
	private String economy;
    private boolean usebank;
	private Map<String, String> groups;
	
	public ChestShopCommand(MySQL mysql, List<String> lines, String economy, boolean usebank, Map<String, String> groups) {
		this.mysql = mysql;
		this.lines = lines;
		this.economy = economy;
        this.usebank = usebank;
		this.groups = groups;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args) {
		if(!(sender instanceof Player)) {
			sender.sendMessage("§cDieser Befehl kann nur von einem Spieler ausgeführt werden!");
			return false;
		}
		Player p = (Player) sender;
		if(args.length == 0) {
			p.sendMessage("§6§lChestShop System by §c§lKyogre312");
			p.sendMessage("§aVerkauf erstellen:");
			p.sendMessage("§91. §dPlatziere ein Schild an einer Truhe und schaue es an.");
			p.sendMessage("§92. §dHalte das Item das du verkaufen möchtest in der entsprechenden Anzahl in der Hand.");
			p.sendMessage("§93. §dGebe ein: §c/csh verkauf <Verkaufspreis> <Beschreibung>");
			p.sendMessage("§aAnkauf erstellen:");
			p.sendMessage("§91. §dPlatziere ein Schild an einer Truhe und schaue es an.");
			p.sendMessage("§92. §dHalte das Item das du ankaufen möchtest in der entsprechenden Anzahl in der Hand.");
			p.sendMessage("§93. §dGebe ein: §c/csh ankauf <Ankaufspreis> <Beschreibung>");
			p.sendMessage("§aNächste Seite: §9/csh help 2");
			return false;
		}
		if(args[0].toLowerCase().equals("help")) {
			int seite = 1;
			if(args.length > 1) {
				try {
					seite = Integer.parseInt(args[1]);
				} catch(NumberFormatException e) {
					p.sendMessage("§cKeine gültige Nummer!");
					return false;
				}
			}
			if(seite < 1 || seite > 2) {
				p.sendMessage("§cKeine gültige Seite!");
				return false;
			}
			if(seite == 1) {
				p.sendMessage("§6§lChestShop System by §c§lKyogre312");
				p.sendMessage("§aVerkauf erstellen:");
				p.sendMessage("§91. §dPlatziere ein Schild an einer Truhe und schaue es an.");
				p.sendMessage("§92. §dHalte das Item das du verkaufen möchtest in der entsprechenden Anzahl in der Hand.");
				p.sendMessage("§93. §dGebe ein: §c/csh verkauf <Verkaufspreis> <Beschreibung>");
				p.sendMessage("§aAnkauf erstellen:");
				p.sendMessage("§91. §dPlatziere ein Schild an einer Truhe und schaue es an.");
				p.sendMessage("§92. §dHalte das Item das du ankaufen möchtest in der entsprechenden Anzahl in der Hand.");
				p.sendMessage("§93. §dGebe ein: §c/csh ankauf <Ankaufspreis> <Beschreibung>");
				p.sendMessage("§aNächste Seite: §9/csh help 2");
				return true;
			}
			if(seite == 2) {
				p.sendMessage("§6§lChestShop System by §c§lKyogre312");
				p.sendMessage("§aGrundstück verkaufen:");
				p.sendMessage("§91. §dPlatziere ein Schild auf dem Grundstück und schaue es an.");
				p.sendMessage("§92. §dGebe ein: §c/csh sellplot <Preis>");
				p.sendMessage("§aItems verschenken:");
				p.sendMessage("§91. §dPlatziere ein Schild an einer Truhe und schaue es an.");
				p.sendMessage("§92. §dHalte das Item das du verschenken möchtest in der entsprechenden Anzahl in der Hand.");
				p.sendMessage("§93. §dGebe ein: §c/csh free [Zeitabstand]<Einheit> <Mindestrang> <Beschreibung>");
				p.sendMessage("§dfür eine Liste von gültigen Einheiten gebe §c/csh free §dein.");
			}
			return true;
		}
		if(args[0].toLowerCase().equals("verkauf")) {
			if(args.length < 3) {
				p.sendMessage("§cSyntax: §6/csh verkauf <Verkaufspreis> <Beschreibung>");
				return false;
			}
			BigDecimal betrag;
			try {
				betrag = new BigDecimal(args[1]);
				betrag = betrag.setScale(2, RoundingMode.DOWN);
			} catch(NumberFormatException ex) {
				p.sendMessage("§cKein gültiger Betrag eingegeben!");
				return false;
			}
			if(betrag.compareTo(BigDecimal.ZERO) <= 0) {
				p.sendMessage("§cBitte gebe einen positiven Betrag ein!");
				return false;
			}
			StringBuilder desc = new StringBuilder();
			for(int i = 0; i < args.length; i++) {
				if(i < 2) {
					continue;
				}
				if(i == 2) {
					desc.append(args[i]);
				} else {
					desc.append(" " + args[i]);
				}
			}
			Block block = p.getTargetBlockExact(5);
			if(block == null) {
				p.sendMessage("§cDu schaust kein Schild an!");
				return false;
			}
			if(!(block.getState() instanceof Sign)) {
				p.sendMessage("§cDu schaust kein Schild an!");
				return false;
			}
			Sign sign = (Sign) block.getState();
			Location loc = block.getLocation();
			Plot plot = Plot.getPlot(BukkitUtil.adapt(loc));
			if(plot == null) {
				p.sendMessage("§cShops dürfen nur auf Plots erstellt werden!");
				return false;
			}
			if(plot.getOwner() == null) {
				p.sendMessage("§cDas Grundstück hat keinen Besitzer!");
				return false;
			}
			if(!plot.getOwner().toString().equals(p.getUniqueId().toString())) {
				if(!p.hasPermission("chestshop.admin")) {
					p.sendMessage("§cDas ist nicht dein Grundstück!");
					return false;
				}
			}
			ItemStack hand = p.getInventory().getItemInMainHand();
			if(hand == null) {
				p.sendMessage("§cDu hälst kein Item in der Hand!");
				return false;
			}
			if(hand.getType().equals(Material.AIR)) {
				p.sendMessage("§cDu hälst kein Item in der Hand!");
				return false;
			}
			hand = new ItemStack(hand);
			List<Location> locs = new ArrayList<>();
			locs.add(new Location(loc.getWorld(), loc.getX()+1, loc.getY(), loc.getZ()));
			locs.add(new Location(loc.getWorld(), loc.getX()-1, loc.getY(), loc.getZ()));
			locs.add(new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ()+1));
			locs.add(new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ()-1));
			Container container = null;
			for(Location l : locs) {
				Block b = l.getBlock();
				if(b == null) {
					continue;
				}
				if(b.getState() instanceof Container) {
					container = (Container) b.getState();
					break;
				}
			}
			if(container == null) {
				p.sendMessage("§cEs ist keine Truhe an dem Schild!");
				return false;
			}
			try {
				int id = mysql.createChestShop(plot.getOwner(), loc, container.getLocation(), hand, 1, betrag, desc.toString());
				sign.getSide(Side.FRONT).setLine(0, getSignText(lines.get(0), id, desc.toString(), "BUY", betrag));
				sign.getSide(Side.FRONT).setLine(1, getSignText(lines.get(1), id, desc.toString(), "BUY", betrag));
				sign.getSide(Side.FRONT).setLine(2, getSignText(lines.get(2), id, desc.toString(), "BUY", betrag));
				sign.getSide(Side.FRONT).setLine(3, getSignText(lines.get(3), id, desc.toString(), "BUY", betrag));
				sign.getSide(Side.BACK).setLine(0, "§cCSH" + id);
				sign.update();
				p.sendMessage("§aDein Shop wurde erstellt!");
                if(usebank) {
                    p.sendMessage("§cHinweis: §6Während du offline bist, werden Einnahmen in der ChestShop-Bank gespeichert (§e/cbank§6).");
                }
				return true;
			} catch(Exception e) {
				e.printStackTrace();
				p.sendMessage("§4Ein Fehler ist aufgetreten!");
				return false;
			}
		}
		if(args[0].toLowerCase().equals("ankauf")) {
			if(args.length < 3) {
				p.sendMessage("§cSyntax: §6/csh ankauf <Ankaufspreis> <Beschreibung>");
				return false;
			}
			BigDecimal betrag;
			try {
				betrag = new BigDecimal(args[1]);
				betrag = betrag.setScale(2, RoundingMode.DOWN);
			} catch(NumberFormatException ex) {
				p.sendMessage("§cKein gültiger Betrag eingegeben!");
				return false;
			}
			if(betrag.compareTo(BigDecimal.ZERO) <= 0) {
				p.sendMessage("§cBitte gebe einen positiven Betrag ein!");
				return false;
			}
			StringBuilder desc = new StringBuilder();
			for(int i = 0; i < args.length; i++) {
				if(i < 2) {
					continue;
				}
				if(i == 2) {
					desc.append(args[i]);
				} else {
					desc.append(" " + args[i]);
				}
			}
			Block block = p.getTargetBlockExact(5);
			if(block == null) {
				p.sendMessage("§cDu schaust kein Schild an!");
				return false;
			}
			if(!(block.getState() instanceof Sign)) {
				p.sendMessage("§cDu schaust kein Schild an!");
				return false;
			}
			Sign sign = (Sign) block.getState();
			Location loc = block.getLocation();
			Plot plot = Plot.getPlot(BukkitUtil.adapt(loc));
			if(plot == null) {
				p.sendMessage("§cShops dürfen nur auf Plots erstellt werden!");
				return false;
			}
			if(plot.getOwner() == null) {
				p.sendMessage("§cDas Grundstück hat keinen Besitzer!");
				return false;
			}
			if(!plot.getOwner().toString().equals(p.getUniqueId().toString())) {
				if(!p.hasPermission("chestshop.admin")) {
					p.sendMessage("§cDas ist nicht dein Grundstück!");
					return false;
				}
			}
			ItemStack hand = p.getInventory().getItemInMainHand();
			if(hand == null) {
				p.sendMessage("§cDu hälst kein Item in der Hand!");
				return false;
			}
			if(hand.getType().equals(Material.AIR)) {
				p.sendMessage("§cDu hälst kein Item in der Hand!");
				return false;
			}
			hand = new ItemStack(hand);
			List<Location> locs = new ArrayList<>();
			locs.add(new Location(loc.getWorld(), loc.getX()+1, loc.getY(), loc.getZ()));
			locs.add(new Location(loc.getWorld(), loc.getX()-1, loc.getY(), loc.getZ()));
			locs.add(new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ()+1));
			locs.add(new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ()-1));
			Container container = null;
			for(Location l : locs) {
				Block b = l.getBlock();
				if(b == null) {
					continue;
				}
				if(b.getState() instanceof Container) {
					container = (Container) b.getState();
					break;
				}
			}
			if(container == null) {
				p.sendMessage("§cEs ist keine Truhe an dem Schild!");
				return false;
			}
			try {
				int id = mysql.createChestShop(plot.getOwner(), loc, container.getLocation(), hand, 2, betrag, desc.toString());
				sign.getSide(Side.FRONT).setLine(0, getSignText(lines.get(0), id, desc.toString(), "SELL", betrag));
				sign.getSide(Side.FRONT).setLine(1, getSignText(lines.get(1), id, desc.toString(), "SELL", betrag));
				sign.getSide(Side.FRONT).setLine(2, getSignText(lines.get(2), id, desc.toString(), "SELL", betrag));
				sign.getSide(Side.FRONT).setLine(3, getSignText(lines.get(3), id, desc.toString(), "SELL", betrag));
				sign.getSide(Side.BACK).setLine(0, "§cCSH" + id);
				sign.update();
				p.sendMessage("§aDein Shop wurde erstellt!");
                if(usebank) {
                    p.sendMessage("§cHinweis: §6Während du offline bist, werden Ausgaben von der ChestShop-Bank abgezogen (§e/cbank§6).");
                    p.sendMessage("§6Achte darauf, dass genügend Geld dort vorhanden ist, während du offline bist!");
                }
				return true;
			} catch(Exception e) {
				e.printStackTrace();
				p.sendMessage("§4Ein Fehler ist aufgetreten!");
				return false;
			}
		}
		if(args[0].toLowerCase().equals("free")) {
			if(args.length < 4) {
				p.sendMessage("§cSyntax: §6/csh free [Zeitabstand]<Einheit> <Mindestrang> <Beschreibung>");
				sender.sendMessage("§6Gültige Einheiten:");
				sender.sendMessage("§es: Sekunden");
				sender.sendMessage("§em: Minuten");
				sender.sendMessage("§eh: Stunden");
				sender.sendMessage("§ed: Tage");
				sender.sendMessage("§eW: Wochen");
				sender.sendMessage("§eM: Monate");
				sender.sendMessage("§eY: Jahre");
				sender.sendMessage("§eP: §lEINMALIG");
				return false;
			}
			long delay;
			if (args[1].equals("P")) {
				delay = -1L;
			} else {
				long i;
				if (args[1].length() == 1) {
					sender.sendMessage("§cKein gültiger Zeitabstand!");
					return false;
				}
				try {
					i = Long.parseLong(args[1].substring(0, args[1].length() - 1));
				} catch (NumberFormatException ex) {
					sender.sendMessage("§cKein gültiger Zeitabstand!");
					return false;
				}
				if (i < 0L) {
					sender.sendMessage("§cKein gültiger Zeitabstand!");
					return false;
				}
				char c = args[1].charAt(args[1].length() - 1);
				if (c == 's') {
					delay = i * 1000L;
				} else if (c == 'm') {
					delay = i * 1000L * 60L;
				} else if (c == 'h') {
					delay = i * 1000L * 60L * 60L;
				} else if (c == 'd') {
					delay = i * 1000L * 60L * 60L * 24L;
				} else if (c == 'W') {
					delay = i * 1000L * 60L * 60L * 24L * 7L;
				} else if (c == 'M') {
					delay = i * 1000L * 60L * 60L * 24L * 30L;
				} else if (c == 'Y') {
					delay = i * 1000L * 60L * 60L * 24L * 365L;
				} else if (c == 'P') {
					delay = -1L;
				} else {
					sender.sendMessage("§cKein gültiger Zeitabstand!");
					return false;
				}
			}
			String rank = groups.get(args[2]);
			if(rank == null) {
				p.sendMessage("§cDieser Rang existiert nicht!");
				return false;
			}
			StringBuilder desc = new StringBuilder();
			for(int i = 0; i < args.length; i++) {
				if(i < 3) {
					continue;
				}
				if(i == 3) {
					desc.append(args[i]);
				} else {
					desc.append(" " + args[i]);
				}
			}
			Block block = p.getTargetBlockExact(5);
			if(block == null) {
				p.sendMessage("§cDu schaust kein Schild an!");
				return false;
			}
			if(!(block.getState() instanceof Sign)) {
				p.sendMessage("§cDu schaust kein Schild an!");
				return false;
			}
			Sign sign = (Sign) block.getState();
			Location loc = block.getLocation();
			Plot plot = Plot.getPlot(BukkitUtil.adapt(loc));
			if(plot == null) {
				p.sendMessage("§cShops dürfen nur auf Plots erstellt werden!");
				return false;
			}
			if(plot.getOwner() == null) {
				p.sendMessage("§cDas Grundstück hat keinen Besitzer!");
				return false;
			}
			if(!plot.getOwner().toString().equals(p.getUniqueId().toString())) {
				if(!p.hasPermission("chestshop.admin")) {
					p.sendMessage("§cDas ist nicht dein Grundstück!");
					return false;
				}
			}
			ItemStack hand = p.getInventory().getItemInMainHand();
			if(hand == null) {
				p.sendMessage("§cDu hälst kein Item in der Hand!");
				return false;
			}
			if(hand.getType().equals(Material.AIR)) {
				p.sendMessage("§cDu hälst kein Item in der Hand!");
				return false;
			}
			hand = new ItemStack(hand);
			List<Location> locs = new ArrayList<>();
			locs.add(new Location(loc.getWorld(), loc.getX()+1, loc.getY(), loc.getZ()));
			locs.add(new Location(loc.getWorld(), loc.getX()-1, loc.getY(), loc.getZ()));
			locs.add(new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ()+1));
			locs.add(new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ()-1));
			Container container = null;
			for(Location l : locs) {
				Block b = l.getBlock();
				if(b == null) {
					continue;
				}
				if(b.getState() instanceof Container) {
					container = (Container) b.getState();
					break;
				}
			}
			if(container == null) {
				p.sendMessage("§cEs ist keine Truhe an dem Schild!");
				return false;
			}
			try {
				int id = mysql.createFreeShop(plot.getOwner(), loc, container.getLocation(), hand, 3, rank, delay, desc.toString());
				sign.getSide(Side.FRONT).setLine(0, getFreeShopSignText(lines.get(0), id, desc.toString(), rank));
				sign.getSide(Side.FRONT).setLine(1, getFreeShopSignText(lines.get(1), id, desc.toString(), rank));
				sign.getSide(Side.FRONT).setLine(2, getFreeShopSignText(lines.get(2), id, desc.toString(), rank));
				sign.getSide(Side.FRONT).setLine(3, getFreeShopSignText(lines.get(3), id, desc.toString(), rank));
				sign.getSide(Side.BACK).setLine(0, "§cCSH" + id);
				sign.update();
				p.sendMessage("§aDein Shop wurde erstellt!");
				return true;
			} catch(Exception e) {
				e.printStackTrace();
				p.sendMessage("§4Ein Fehler ist aufgetreten!");
				return false;
			}
		}
		if(args[0].toLowerCase().equals("adminverkauf") && p.hasPermission("chestshop.admin")) {
			if(args.length < 3) {
				p.sendMessage("§cSyntax: §6/csh adminverkauf <Verkaufspreis> <Beschreibung>");
				return false;
			}
			BigDecimal betrag;
			try {
				betrag = new BigDecimal(args[1]);
				betrag = betrag.setScale(2, RoundingMode.DOWN);
			} catch(NumberFormatException ex) {
				p.sendMessage("§cKein gültiger Betrag eingegeben!");
				return false;
			}
			if(betrag.compareTo(BigDecimal.ZERO) <= 0) {
				p.sendMessage("§cBitte gebe einen positiven Betrag ein!");
				return false;
			}
			StringBuilder desc = new StringBuilder();
			for(int i = 0; i < args.length; i++) {
				if(i < 2) {
					continue;
				}
				if(i == 2) {
					desc.append(args[i]);
				} else {
					desc.append(" " + args[i]);
				}
			}
			Block block = p.getTargetBlockExact(5);
			if(block == null) {
				p.sendMessage("§cDu schaust kein Schild an!");
				return false;
			}
			if(!(block.getState() instanceof Sign)) {
				p.sendMessage("§cDu schaust kein Schild an!");
				return false;
			}
			Sign sign = (Sign) block.getState();
			Location loc = block.getLocation();
			Plot plot = Plot.getPlot(BukkitUtil.adapt(loc));
			if(plot == null) {
				p.sendMessage("§cShops dürfen nur auf Plots erstellt werden!");
				return false;
			}
			if(plot.getOwner() == null) {
				p.sendMessage("§cDas Grundstück hat keinen Besitzer!");
				return false;
			}
			ItemStack hand = p.getInventory().getItemInMainHand();
			if(hand == null) {
				p.sendMessage("§cDu hälst kein Item in der Hand!");
				return false;
			}
			if(hand.getType().equals(Material.AIR)) {
				p.sendMessage("§cDu hälst kein Item in der Hand!");
				return false;
			}
			hand = new ItemStack(hand);
			try {
				int id = mysql.createAdminChestShop(plot.getOwner(), loc, hand, 4, betrag, desc.toString());
				sign.getSide(Side.FRONT).setLine(0, getSignText(lines.get(0), id, desc.toString(), "BUY", betrag));
				sign.getSide(Side.FRONT).setLine(1, getSignText(lines.get(1), id, desc.toString(), "BUY", betrag));
				sign.getSide(Side.FRONT).setLine(2, getSignText(lines.get(2), id, desc.toString(), "BUY", betrag));
				sign.getSide(Side.FRONT).setLine(3, getSignText(lines.get(3), id, desc.toString(), "BUY", betrag));
				sign.getSide(Side.BACK).setLine(0, "§cCSH" + id);
				sign.update();
				p.sendMessage("§aDein Shop wurde erstellt!");
				return true;
			} catch(Exception e) {
				e.printStackTrace();
				p.sendMessage("§4Ein Fehler ist aufgetreten!");
				return false;
			}
		}
		if(args[0].toLowerCase().equals("adminankauf") && p.hasPermission("chestshop.admin")) {
			if(args.length < 3) {
				p.sendMessage("§cSyntax: §6/csh adminankauf <Ankaufspreis> <Beschreibung>");
				return false;
			}
			BigDecimal betrag;
			try {
				betrag = new BigDecimal(args[1]);
				betrag = betrag.setScale(2, RoundingMode.DOWN);
			} catch(NumberFormatException ex) {
				p.sendMessage("§cKein gültiger Betrag eingegeben!");
				return false;
			}
			if(betrag.compareTo(BigDecimal.ZERO) <= 0) {
				p.sendMessage("§cBitte gebe einen positiven Betrag ein!");
				return false;
			}
			StringBuilder desc = new StringBuilder();
			for(int i = 0; i < args.length; i++) {
				if(i < 2) {
					continue;
				}
				if(i == 2) {
					desc.append(args[i]);
				} else {
					desc.append(" " + args[i]);
				}
			}
			Block block = p.getTargetBlockExact(5);
			if(block == null) {
				p.sendMessage("§cDu schaust kein Schild an!");
				return false;
			}
			if(!(block.getState() instanceof Sign)) {
				p.sendMessage("§cDu schaust kein Schild an!");
				return false;
			}
			Sign sign = (Sign) block.getState();
			Location loc = block.getLocation();
			Plot plot = Plot.getPlot(BukkitUtil.adapt(loc));
			if(plot == null) {
				p.sendMessage("§cShops dürfen nur auf Plots erstellt werden!");
				return false;
			}
			if(plot.getOwner() == null) {
				p.sendMessage("§cDas Grundstück hat keinen Besitzer!");
				return false;
			}
			ItemStack hand = p.getInventory().getItemInMainHand();
			if(hand == null) {
				p.sendMessage("§cDu hälst kein Item in der Hand!");
				return false;
			}
			if(hand.getType().equals(Material.AIR)) {
				p.sendMessage("§cDu hälst kein Item in der Hand!");
				return false;
			}
			hand = new ItemStack(hand);
			try {
				int id = mysql.createAdminChestShop(plot.getOwner(), loc, hand, 5, betrag, desc.toString());
				sign.getSide(Side.FRONT).setLine(0, getSignText(lines.get(0), id, desc.toString(), "SELL", betrag));
				sign.getSide(Side.FRONT).setLine(1, getSignText(lines.get(1), id, desc.toString(), "SELL", betrag));
				sign.getSide(Side.FRONT).setLine(2, getSignText(lines.get(2), id, desc.toString(), "SELL", betrag));
				sign.getSide(Side.FRONT).setLine(3, getSignText(lines.get(3), id, desc.toString(), "SELL", betrag));
				sign.getSide(Side.BACK).setLine(0, "§cCSH" + id);
				sign.update();
				p.sendMessage("§aDein Shop wurde erstellt!");
				return true;
			} catch(Exception e) {
				e.printStackTrace();
				p.sendMessage("§4Ein Fehler ist aufgetreten!");
				return false;
			}
		}
		if(args[0].toLowerCase().equals("sellplot")) {
			if(args.length < 2) {
				p.sendMessage("§cSyntax: §6/csh sellplot <Preis>");
				return false;
			}
			BigDecimal betrag;
			try {
				betrag = new BigDecimal(args[1]);
				betrag = betrag.setScale(2, RoundingMode.DOWN);
			} catch(NumberFormatException ex) {
				p.sendMessage("§cKein gültiger Betrag eingegeben!");
				return false;
			}
			if(betrag.compareTo(BigDecimal.ZERO) <= 0) {
				p.sendMessage("§cBitte gebe einen positiven Betrag ein!");
				return false;
			}
			Block block = p.getTargetBlockExact(5);
			if(block == null) {
				p.sendMessage("§cDu schaust kein Schild an!");
				return false;
			}
			if(!(block.getState() instanceof Sign)) {
				p.sendMessage("§cDu schaust kein Schild an!");
				return false;
			}
			Sign sign = (Sign) block.getState();
			Location loc = block.getLocation();
			Plot plot = Plot.getPlot(BukkitUtil.adapt(loc));
			if(plot == null) {
				p.sendMessage("§cShops dürfen nur auf Plots erstellt werden!");
				return false;
			}
			if(plot.getOwner() == null) {
				p.sendMessage("§cDas Grundstück hat keinen Besitzer!");
				return false;
			}
			if(!plot.getOwner().toString().equals(p.getUniqueId().toString())) {
				if(!p.hasPermission("chestshop.admin")) {
					p.sendMessage("§cDas ist nicht dein Grundstück!");
					return false;
				}
			}
			try {
				int id = mysql.createPlotChestShop(plot.getOwner(), loc, 6, betrag, "Plot " + plot.getId().getX() + ";" + plot.getId().getY());
				sign.getSide(Side.FRONT).setLine(0, getSignText(lines.get(0), id, "Plot " + plot.getId().getX() + ";" + plot.getId().getY(), "BUYPLOT", betrag));
				sign.getSide(Side.FRONT).setLine(1, getSignText(lines.get(1), id, "Plot " + plot.getId().getX() + ";" + plot.getId().getY(), "BUYPLOT", betrag));
				sign.getSide(Side.FRONT).setLine(2, getSignText(lines.get(2), id, "Plot " + plot.getId().getX() + ";" + plot.getId().getY(), "BUYPLOT", betrag));
				sign.getSide(Side.FRONT).setLine(3, getSignText(lines.get(3), id, "Plot " + plot.getId().getX() + ";" + plot.getId().getY(), "BUYPLOT", betrag));
				sign.getSide(Side.BACK).setLine(0, "§cCSH" + id);
				sign.update();
				p.sendMessage("§aDein Shop wurde erstellt!");
                if(usebank) {
                    p.sendMessage("§cHinweis: §6Während du offline bist, werden Einnahmen in der ChestShop-Bank gespeichert (§e/cbank§6).");
                }
				return true;
			} catch(Exception e) {
				e.printStackTrace();
				p.sendMessage("§4Ein Fehler ist aufgetreten!");
				return false;
			}
		}
		if(args[0].toLowerCase().equals("adminfree") && p.hasPermission("chestshop.admin")) {
			if(args.length < 4) {
				p.sendMessage("§cSyntax: §6/csh adminfree [Zeitabstand]<Einheit> <Mindestrang> <Beschreibung>");
				sender.sendMessage("§6Gültige Einheiten:");
				sender.sendMessage("§es: Sekunden");
				sender.sendMessage("§em: Minuten");
				sender.sendMessage("§eh: Stunden");
				sender.sendMessage("§ed: Tage");
				sender.sendMessage("§eW: Wochen");
				sender.sendMessage("§eM: Monate");
				sender.sendMessage("§eY: Jahre");
				sender.sendMessage("§eP: §lEINMALIG");
				return false;
			}
			long delay;
			if (args[1].equals("P")) {
				delay = -1L;
			} else {
				long i;
				if (args[1].length() == 1) {
					sender.sendMessage("§cKein gültiger Zeitabstand!");
					return false;
				}
				try {
					i = Long.parseLong(args[1].substring(0, args[1].length() - 1));
				} catch (NumberFormatException ex) {
					sender.sendMessage("§cKein gültiger Zeitabstand!");
					return false;
				}
				if (i < 0L) {
					sender.sendMessage("§cKein gültiger Zeitabstand!");
					return false;
				}
				char c = args[1].charAt(args[1].length() - 1);
				if (c == 's') {
					delay = i * 1000L;
				} else if (c == 'm') {
					delay = i * 1000L * 60L;
				} else if (c == 'h') {
					delay = i * 1000L * 60L * 60L;
				} else if (c == 'd') {
					delay = i * 1000L * 60L * 60L * 24L;
				} else if (c == 'W') {
					delay = i * 1000L * 60L * 60L * 24L * 7L;
				} else if (c == 'M') {
					delay = i * 1000L * 60L * 60L * 24L * 30L;
				} else if (c == 'Y') {
					delay = i * 1000L * 60L * 60L * 24L * 365L;
				} else if (c == 'P') {
					delay = -1L;
				} else {
					sender.sendMessage("§cKein gültiger Zeitabstand!");
					return false;
				}
			}
			String rank = groups.get(args[2]);
			if(rank == null) {
				p.sendMessage("§cDieser Rang existiert nicht!");
				return false;
			}
			StringBuilder desc = new StringBuilder();
			for(int i = 0; i < args.length; i++) {
				if(i < 3) {
					continue;
				}
				if(i == 3) {
					desc.append(args[i]);
				} else {
					desc.append(" " + args[i]);
				}
			}
			Block block = p.getTargetBlockExact(5);
			if(block == null) {
				p.sendMessage("§cDu schaust kein Schild an!");
				return false;
			}
			if(!(block.getState() instanceof Sign)) {
				p.sendMessage("§cDu schaust kein Schild an!");
				return false;
			}
			Sign sign = (Sign) block.getState();
			Location loc = block.getLocation();
			Plot plot = Plot.getPlot(BukkitUtil.adapt(loc));
			if(plot == null) {
				p.sendMessage("§cShops dürfen nur auf Plots erstellt werden!");
				return false;
			}
			if(plot.getOwner() == null) {
				p.sendMessage("§cDas Grundstück hat keinen Besitzer!");
				return false;
			}
			ItemStack hand = p.getInventory().getItemInMainHand();
			if(hand == null) {
				p.sendMessage("§cDu hälst kein Item in der Hand!");
				return false;
			}
			if(hand.getType().equals(Material.AIR)) {
				p.sendMessage("§cDu hälst kein Item in der Hand!");
				return false;
			}
			hand = new ItemStack(hand);
			try {
				int id = mysql.createAdminFreeShop(plot.getOwner(), loc, hand, 7, rank, delay, desc.toString());
				sign.getSide(Side.FRONT).setLine(0, getFreeShopSignText(lines.get(0), id, desc.toString(), rank));
				sign.getSide(Side.FRONT).setLine(1, getFreeShopSignText(lines.get(1), id, desc.toString(), rank));
				sign.getSide(Side.FRONT).setLine(2, getFreeShopSignText(lines.get(2), id, desc.toString(), rank));
				sign.getSide(Side.FRONT).setLine(3, getFreeShopSignText(lines.get(3), id, desc.toString(), rank));
				sign.getSide(Side.BACK).setLine(0, "§cCSH" + id);
				sign.update();
				p.sendMessage("§aDein Shop wurde erstellt!");
				return true;
			} catch(Exception e) {
				e.printStackTrace();
				p.sendMessage("§4Ein Fehler ist aufgetreten!");
				return false;
			}
		}
		p.sendMessage("§cUngültiger Befehl! §6/chestshop §cfür Hilfe!");
		return false;
	}

    private String getSignText(String line, int id, String desc, String type, BigDecimal price) {
		line = line.replace("{ID}", Integer.toString(id));
		line = line.replace("{Description}", desc);
		line = line.replace("{Type}", type);
		line = line.replace("{Price}", DecimalFormat.getNumberInstance(Locale.GERMAN).format(price) + economy);
		line = ChatColor.translateAlternateColorCodes('&', line);
		return line;
	}

	private String getFreeShopSignText(String line, int id, String desc, String rank) {
		line = line.replace("{ID}", Integer.toString(id));
		line = line.replace("{Description}", desc);
		line = line.replace("{Type}", "FREE");
		line = line.replace("{Price}", rank);
		line = ChatColor.translateAlternateColorCodes('&', line);
		return line;
	}

}
