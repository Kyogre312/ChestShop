package me.kyogre312.chestshop.commands;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Locale;

import me.kyogre312.chestshop.MySQL;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CBankCommand implements CommandExecutor {
	private MySQL mysql;
	private boolean usebank;
	private Economy econ;
	
	public CBankCommand(MySQL mysql, Economy econ, boolean usebank) {
		this.mysql = mysql;
		this.econ = econ;
		this.usebank = usebank;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args) {
		if(!(sender instanceof Player)) {
			sender.sendMessage("§cDieser Befehl kann nur von einem Spieler ausgeführt werden!");
			return false;
		}
		Player p = (Player) sender;
		if(!usebank) {
			p.sendMessage("§cDieses Feature ist deaktiviert!");
			return false;
		}
		if(args.length == 0) {
			p.sendMessage("§c/cbank guthaben");
			p.sendMessage("§c/cbank einzahlen <Betrag>");
			p.sendMessage("§c/cbank abheben <Betrag>");
			return false;
		}
		if(args[0].toLowerCase().equals("guthaben")) {
			try {
				p.sendMessage("§aDein Bankguthaben: §c" + DecimalFormat.getNumberInstance(Locale.GERMAN).format(mysql.getBank(p.getUniqueId())) + "$");
			} catch (SQLException e) {
				p.sendMessage("§cEin Fehler ist aufgetreten: §4" + e.getMessage());
				return false;
			}
			return true;
		}
		if(args[0].toLowerCase().equals("einzahlen")) {
			if(args.length == 1) {
				p.sendMessage("§cSyntax: §6/cbank einzahlen <Betrag>");
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
			try {
				if(!econ.has(p, betrag.doubleValue())) {
					p.sendMessage("§cDu hast nicht genug Geld!");
					return false;
				}
				econ.withdrawPlayer(p, betrag.doubleValue());
				mysql.setBank(p.getUniqueId(), mysql.getBank(p.getUniqueId()).add(betrag));
				p.sendMessage("§c" + DecimalFormat.getNumberInstance(Locale.GERMAN).format(betrag) + "$ §awurden eingezahlt.");
				return true;
			} catch(SQLException e) {
				p.sendMessage("§cEin Fehler ist aufgetreten: §4" + e.getMessage());
				return false;
			}
		}
		if(args[0].toLowerCase().equals("abheben")) {
			if(args.length == 1) {
				p.sendMessage("§cSyntax: §6/cbank abheben <Betrag>");
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
			try {
				if(mysql.getBank(p.getUniqueId()).compareTo(betrag) < 0) {
					p.sendMessage("§cDu hast nicht genug Geld!");
					return false;
				}
				mysql.setBank(p.getUniqueId(), mysql.getBank(p.getUniqueId()).subtract(betrag));
				econ.depositPlayer(p, betrag.doubleValue());
				p.sendMessage("§c" + DecimalFormat.getNumberInstance(Locale.GERMAN).format(betrag) + "$ §awurden ausgezahlt.");
				return true;
			} catch (SQLException e) {
				p.sendMessage("§cEin Fehler ist aufgetreten: §4" + e.getMessage());
				return false;
			}
		}
		p.sendMessage("§cUngültiger Befehl! §6/cbank guthaben§c, §6/cbank einzahlen§c, §6/cbank abheben§c.");
		return false;
	}

}
