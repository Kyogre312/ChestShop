package me.kyogre312.chestshop;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

public class MySQL
{
	private String host = null;
	private int port = 0;
	private String db = null;
	private String user = null;
	private String pass = null;
	private Connection con;
	private String prefix = null;
	private String file = null;

	public MySQL(String host, int port, String db, String user, String pass, String prefix) {
		this.host = host;
		this.port = port;
		this.db = db;
		this.user = user;
		this.pass = pass;
		this.prefix = prefix;
	}

	public MySQL(String file) {
		this.file = file;
		this.prefix = "";
	}

	public void enableBank() throws SQLException {
		connect();
		con.prepareStatement("CREATE TABLE IF NOT EXISTS `" + prefix + "chestshop_bank` (`uuid` varchar(36) PRIMARY KEY NOT NULL, `money` text NOT NULL);").execute();
		disconnect();
	}

	public BigDecimal getBank(UUID uuid) throws SQLException {
		connect();
		ResultSet set = this.con.prepareStatement("SELECT * FROM `" + prefix + "chestshop_bank` WHERE `uuid`='" + uuid.toString() + "'").executeQuery();
		if(set.next()) {
			BigDecimal result = new BigDecimal(set.getString("money"));
			disconnect();
			return result;
		} else {
			disconnect();
			return BigDecimal.ZERO;
		}
	}

	public void setBank(UUID uuid, BigDecimal money) throws SQLException {
		connect();
		ResultSet set = this.con.prepareStatement("SELECT * FROM `" + prefix + "chestshop_bank` WHERE `uuid`='" + uuid.toString() + "'").executeQuery();
		if(!set.next()) {
			con.prepareStatement("INSERT INTO `" + prefix + "chestshop_bank` (`uuid`, `money`) VALUES ('" + uuid.toString() + "', '" + money.toString() + "')").execute();
			disconnect();
			return;
		}
		this.con.prepareStatement("UPDATE `" + prefix + "chestshop_bank` SET `money` = '" + money.toString() + "' WHERE `uuid`='" + uuid.toString() + "';").execute();
		disconnect();
	}

	private void connect() throws SQLException {
		if(file == null) {
			this.con = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.db, this.user, this.pass);
		} else {
			this.con = DriverManager.getConnection("jdbc:sqlite:" + this.file);
		}
	}

    public void init() throws SQLException {
        connect();
		if(file == null) {
			con.prepareStatement("CREATE TABLE IF NOT EXISTS `" + prefix + "chestshops` (`id` int(11) PRIMARY KEY NOT NULL AUTO_INCREMENT, `owner` varchar(36) NOT NULL, `sign_world` text NOT NULL, `sign_x` int(11) NOT NULL, `sign_y` int(11) NOT NULL, `sign_z` int(11) NOT NULL, `chest_world` text DEFAULT NULL, `chest_x` int(11) DEFAULT NULL, `chest_y` int(11) DEFAULT NULL, `chest_z` int(11) DEFAULT NULL, `item` text DEFAULT NULL, `mode` int(11) NOT NULL, `price` text NOT NULL, `description` text NOT NULL);").execute();
		} else {
			con.prepareStatement("CREATE TABLE IF NOT EXISTS `" + prefix + "chestshops` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `owner` varchar(36) NOT NULL, `sign_world` text NOT NULL, `sign_x` int(11) NOT NULL, `sign_y` int(11) NOT NULL, `sign_z` int(11) NOT NULL, `chest_world` text DEFAULT NULL, `chest_x` int(11) DEFAULT NULL, `chest_y` int(11) DEFAULT NULL, `chest_z` int(11) DEFAULT NULL, `item` text DEFAULT NULL, `mode` int(11) NOT NULL, `price` text NOT NULL, `description` text NOT NULL);").execute();
		}
		disconnect();
    }
	
	public int createChestShop(UUID uuid, Location sign, Location chest, ItemStack item, int mode, BigDecimal price, String desc) throws Exception {
		connect();
		PreparedStatement stat = con.prepareStatement("INSERT INTO " + prefix + "chestshops (owner, sign_world, sign_x, sign_y, sign_z, chest_world, chest_x, chest_y, chest_z, item, mode, price, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
		stat.setString(1, uuid.toString());
		stat.setString(2, sign.getWorld().getName());
		stat.setInt(3, sign.getBlockX());
		stat.setInt(4, sign.getBlockY());
		stat.setInt(5, sign.getBlockZ());
		stat.setString(6, chest.getWorld().getName());
		stat.setInt(7, chest.getBlockX());
		stat.setInt(8, chest.getBlockY());
		stat.setInt(9, chest.getBlockZ());
		stat.setString(10, toString(item));
		stat.setInt(11, mode);
		stat.setString(12, price.toString());
		stat.setString(13, desc);
		stat.execute();
		ResultSet set = stat.getGeneratedKeys();
		set.next();
		int result = set.getInt(1);
		disconnect();
		return result;
	}

	public int createFreeShop(UUID uuid, Location sign, Location chest, ItemStack item, int mode, String rank, long time, String desc) throws Exception {
		connect();
		PreparedStatement stat = con.prepareStatement("INSERT INTO " + prefix + "chestshops (owner, sign_world, sign_x, sign_y, sign_z, chest_world, chest_x, chest_y, chest_z, item, mode, price, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
		stat.setString(1, uuid.toString());
		stat.setString(2, sign.getWorld().getName());
		stat.setInt(3, sign.getBlockX());
		stat.setInt(4, sign.getBlockY());
		stat.setInt(5, sign.getBlockZ());
		stat.setString(6, chest.getWorld().getName());
		stat.setInt(7, chest.getBlockX());
		stat.setInt(8, chest.getBlockY());
		stat.setInt(9, chest.getBlockZ());
		stat.setString(10, toString(item));
		stat.setInt(11, mode);
		stat.setString(12, rank != null ? rank + ";" + time : Long.toString(time));
		stat.setString(13, desc);
		stat.execute();
		ResultSet set = stat.getGeneratedKeys();
		set.next();
		int result = set.getInt(1);
		con.prepareStatement("CREATE TABLE IF NOT EXISTS `" + prefix + "freeshop_uses` (`id` varchar(50) PRIMARY KEY, `time` BIGINT NOT NULL);").execute();
		disconnect();
		return result;
	}
	
	public int createAdminChestShop(UUID uuid, Location sign, ItemStack item, int mode, BigDecimal price, String desc) throws Exception {
		connect();
		PreparedStatement stat = con.prepareStatement("INSERT INTO " + prefix + "chestshops (owner, sign_world, sign_x, sign_y, sign_z, item, mode, price, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
		stat.setString(1, uuid.toString());
		stat.setString(2, sign.getWorld().getName());
		stat.setInt(3, sign.getBlockX());
		stat.setInt(4, sign.getBlockY());
		stat.setInt(5, sign.getBlockZ());
		stat.setString(6, toString(item));
		stat.setInt(7, mode);
		stat.setString(8, price.toString());
		stat.setString(9, desc);
		stat.execute();
		ResultSet set = stat.getGeneratedKeys();
		set.next();
		int result = set.getInt(1);
		disconnect();
		return result;
	}

	public int createAdminFreeShop(UUID uuid, Location sign, ItemStack item, int mode, String rank, long time, String desc) throws Exception {
		connect();
		PreparedStatement stat = con.prepareStatement("INSERT INTO " + prefix + "chestshops (owner, sign_world, sign_x, sign_y, sign_z, item, mode, price, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
		stat.setString(1, uuid.toString());
		stat.setString(2, sign.getWorld().getName());
		stat.setInt(3, sign.getBlockX());
		stat.setInt(4, sign.getBlockY());
		stat.setInt(5, sign.getBlockZ());
		stat.setString(6, toString(item));
		stat.setInt(7, mode);
		stat.setString(8, rank != null ? rank + ";" + time : Long.toString(time));
		stat.setString(9, desc);
		stat.execute();
		ResultSet set = stat.getGeneratedKeys();
		set.next();
		int result = set.getInt(1);
		con.prepareStatement("CREATE TABLE IF NOT EXISTS `" + prefix + "freeshop_uses` (`id` varchar(50) PRIMARY KEY, `time` BIGINT NOT NULL);").execute();
		disconnect();
		return result;
	}

	public long getRestTime(UUID uuid, int id) throws SQLException {
		long delay = getFreeShopDelay(id);
		connect();
		PreparedStatement stat = con.prepareStatement("SELECT * FROM `" + prefix + "freeshop_uses` WHERE id = ?");
		stat.setString(1, uuid.toString() + "_" + id);
		ResultSet set = stat.executeQuery();
		if(set.next()) {
			long time = set.getLong("time");
			long now = System.currentTimeMillis();
			if(delay < 0) {
				disconnect();
				return -1;
			}
			if(now >= time + delay) {
				disconnect();
				return 0;
			}
			disconnect();
			return time + delay - now;
		}
		disconnect();
		return 0;
	}

	public long useFreeShop(UUID uuid, int id) throws SQLException {
		connect();
		PreparedStatement stat = con.prepareStatement("SELECT * FROM `" + prefix + "freeshop_uses` WHERE id = ?");
		stat.setString(1, uuid.toString() + "_" + id);
		ResultSet set = stat.executeQuery();
		if(set.next()) {
			stat = con.prepareStatement("DELETE FROM `" + prefix + "freeshop_uses` WHERE id = ?");
			stat.setString(1, uuid.toString() + "_" + id);
			stat.execute();
		}
		stat = con.prepareStatement("INSERT INTO `" + prefix + "freeshop_uses` (id, time) VALUES (?, ?)");
		stat.setString(1, uuid.toString() + "_" + id);
		stat.setLong(2, System.currentTimeMillis());
		stat.execute();
		disconnect();
		return getFreeShopDelay(id);
	}
	
	public int createPlotChestShop(UUID uuid, Location sign, int mode, BigDecimal price, String desc) throws Exception {
		connect();
		PreparedStatement stat = con.prepareStatement("INSERT INTO " + prefix + "chestshops (owner, sign_world, sign_x, sign_y, sign_z, mode, price, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
		stat.setString(1, uuid.toString());
		stat.setString(2, sign.getWorld().getName());
		stat.setInt(3, sign.getBlockX());
		stat.setInt(4, sign.getBlockY());
		stat.setInt(5, sign.getBlockZ());
		stat.setInt(6, mode);
		stat.setString(7, price.toString());
		stat.setString(8, desc);
		stat.execute();
		ResultSet set = stat.getGeneratedKeys();
		set.next();
		int result = set.getInt(1);
		disconnect();
		return result;
	}
	
	private String toString(ItemStack stack) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		BukkitObjectOutputStream bukkitOut = new BukkitObjectOutputStream(out);
		bukkitOut.writeObject(stack);
		bukkitOut.close();
		return Base64.getEncoder().encodeToString(out.toByteArray());
	}

	private ItemStack fromString(String string) throws Exception {
		ByteArrayInputStream in = new ByteArrayInputStream(Base64.getDecoder().decode(string));
		BukkitObjectInputStream bukkitIn = new BukkitObjectInputStream(in);
		ItemStack result = (ItemStack)bukkitIn.readObject();
		bukkitIn.close();
		return result;
	}
	
	public boolean idExist(int id) throws SQLException {
		connect();
		PreparedStatement stat = con.prepareStatement("SELECT * FROM " + prefix + "chestshops WHERE id = ?");
		stat.setInt(1, id);
		ResultSet set = stat.executeQuery();
		if(set.next()) {
			disconnect();
			return true;
		}
		disconnect();
		return false;
	}
	
	public Location getChestLocation(int id) throws SQLException {
		connect();
		PreparedStatement stat = con.prepareStatement("SELECT * FROM " + prefix + "chestshops WHERE id = ?");
		stat.setInt(1, id);
		ResultSet set = stat.executeQuery();
		if(set.next()) {
			String world = set.getString("chest_world");
			int x = set.getInt("chest_x");
			int y= set.getInt("chest_y");
			int z = set.getInt("chest_z");
			disconnect();
			if(world == null) {
				return null;
			} else {
				return new Location(Bukkit.getWorld(world), x, y, z);
			}
		}
		disconnect();
		return null;
	}
	
	public Location getSignLocation(int id) throws SQLException {
		connect();
		PreparedStatement stat = con.prepareStatement("SELECT * FROM " + prefix + "chestshops WHERE id = ?");
		stat.setInt(1, id);
		ResultSet set = stat.executeQuery();
		if(set.next()) {
			String world = set.getString("sign_world");
			int x = set.getInt("sign_x");
			int y= set.getInt("sign_y");
			int z = set.getInt("sign_z");
			disconnect();
			return new Location(Bukkit.getWorld(world), x, y, z);
		}
		disconnect();
		return null;
	}
	
	public UUID getOwner(int id) throws SQLException {
		connect();
		PreparedStatement stat = con.prepareStatement("SELECT * FROM " + prefix + "chestshops WHERE id = ?");
		stat.setInt(1, id);
		ResultSet set = stat.executeQuery();
		if(set.next()) {
			UUID result = UUID.fromString(set.getString("owner"));
			disconnect();
			return result;
		}
		disconnect();
		return null;
	}
	
	public ItemStack getItem(int id) throws Exception {
		connect();
		PreparedStatement stat = con.prepareStatement("SELECT * FROM " + prefix + "chestshops WHERE id = ?");
		stat.setInt(1, id);
		ResultSet set = stat.executeQuery();
		if(set.next()) {
			String string = set.getString("item");
			if(string == null) {
				disconnect();
				return null;
			}
			ItemStack result = fromString(string);
			disconnect();
			return result;
		}
		disconnect();
		return null;
	}
	
	public int getMode(int id) throws SQLException {
		connect();
		PreparedStatement stat = con.prepareStatement("SELECT * FROM " + prefix + "chestshops WHERE id = ?");
		stat.setInt(1, id);
		ResultSet set = stat.executeQuery();
		if(set.next()) {
			int result = set.getInt("mode");
			disconnect();
			return result;
		}
		disconnect();
		return 0;
	}
	
	public BigDecimal getPrice(int id) throws SQLException {
		connect();
		PreparedStatement stat = con.prepareStatement("SELECT * FROM " + prefix + "chestshops WHERE id = ?");
		stat.setInt(1, id);
		ResultSet set = stat.executeQuery();
		if(set.next()) {
			BigDecimal result = new BigDecimal(set.getString("price"));
			disconnect();
			return result;
		}
		disconnect();
		return null;
	}

	public String getFreeShopRank(int id) throws SQLException {
		connect();
		PreparedStatement stat = con.prepareStatement("SELECT * FROM " + prefix + "chestshops WHERE id = ?");
		stat.setInt(1, id);
		ResultSet set = stat.executeQuery();
		if(set.next()) {
			String price = set.getString("price");
			String result = null;
			if(price.contains(";")) {
				result = price.split(";")[0];
			}
			disconnect();
			return result;
		}
		disconnect();
		return null;
	}

	public long getFreeShopDelay(int id) throws SQLException {
		connect();
		PreparedStatement stat = con.prepareStatement("SELECT * FROM " + prefix + "chestshops WHERE id = ?");
		stat.setInt(1, id);
		ResultSet set = stat.executeQuery();
		if(set.next()) {
			String price = set.getString("price");
			long result;
			if(price.contains(";")) {
				result = Long.parseLong(price.split(";")[1]);
			} else {
				result = Long.parseLong(price);
			}
			disconnect();
			return result;
		}
		disconnect();
		return -1;
	}

	public String getDescription(int id) throws SQLException {
		connect();
		PreparedStatement stat = con.prepareStatement("SELECT * FROM " + prefix + "chestshops WHERE id = ?");
		stat.setInt(1, id);
		ResultSet set = stat.executeQuery();
		if(set.next()) {
			String result = set.getString("description");
			disconnect();
			return result;
		}
		disconnect();
		return null;
	}

	private void disconnect() throws SQLException {
		this.con.close();
	}
}
