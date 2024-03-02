package me.kyogre312.chestshop;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.kyogre312.chestshop.commands.CBankCommand;
import me.kyogre312.chestshop.commands.ChestShopCommand;
import me.kyogre312.chestshop.listener.ChestShopListener;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;

public class ChestShop extends JavaPlugin {
	
	@Override
	public void onEnable() {
		FileConfiguration config = getConfig();
		if(!config.contains("mysql")) {
			config.set("mysql.use", false);
			config.set("mysql.host", "localhost");
			config.set("mysql.port", 3306);
			config.set("mysql.database", "minecraft");
			config.set("mysql.username", "minecraft");
			config.set("mysql.password", "aA1234Aa");
			config.set("mysql.prefix", "");
		}
		if(!config.contains("format")) {
			config.set("format.line1", "&cChestShop #{ID}");
			config.set("format.line2", "&6{Description}");
			config.set("format.line3", "&9{Type}");
			config.set("format.line4", "&a{Price}");
			config.set("format.economy", "$");
		}
		if(!config.contains("enable-offline-bank")) {
			config.set("enable-offline-bank", false);
		}
		if(!config.contains("freeshop-ranks")) {
			config.set("freeshop-ranks.default.permission", "group.default");
			config.set("freeshop-ranks.default.displayname", "Spieler");
		}
		saveConfig();
		boolean usemysql = config.getBoolean("mysql.use");
		boolean usebank = config.getBoolean("enable-offline-bank");
		MySQL mysql;
		if(usemysql) {
			mysql = new MySQL(config.getString("mysql.host"), config.getInt("mysql.port"), config.getString("mysql.database"), config.getString("mysql.username"), config.getString("mysql.password"), config.getString("mysql.prefix"));
		} else {
			File dbfile = new File(getDataFolder(), "storage.db");
			mysql = new MySQL(dbfile.getAbsolutePath());
		}
		try {
			mysql.init();
			if(usebank) {
				mysql.enableBank();
			}
		} catch(SQLException e) {
			e.printStackTrace();
			return;
		}
		List<String> lines = new ArrayList<>();
		lines.add(config.getString("format.line1"));
		lines.add(config.getString("format.line2"));
		lines.add(config.getString("format.line3"));
		lines.add(config.getString("format.line4"));
		String economy = config.getString("format.economy");
		Map<String, String> groups = new HashMap<>();
		Map<String, String> permissions = new HashMap<>();
		Map<String, String> displaynames = new HashMap<>();
		for(String s : config.getConfigurationSection("freeshop-ranks").getKeys(false)) {
			groups.put(s, s);
			String displayname = config.getString("freeshop-ranks." + s + ".displayname");
			groups.put(displayname, s);
			displaynames.put(s, displayname);
			String permission = config.getString("freeshop-ranks." + s + ".permission");
			permissions.put(s, permission);
		}
		getCommand("chestshop").setExecutor(new ChestShopCommand(mysql, lines, economy, usebank, groups));
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		Economy econ = rsp.getProvider();
		Bukkit.getPluginManager().registerEvents(new ChestShopListener(this, mysql, econ, economy, usebank, permissions, displaynames), this);
		getCommand("cbank").setExecutor(new CBankCommand(mysql, econ, usebank));
	}

}
