package me.kyogre312.chestshop.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class CommandBase implements CommandExecutor {
	private boolean player;
	private String perm;
	private int args;
	private String syntax;

	public CommandBase() {
		this(false, null, 0, null);
	}

	public CommandBase(String perm) {
		this(false, perm, 0, null);
	}

	public CommandBase(int args, String syntax) {
		this(false, null, args, syntax);
	}

	public CommandBase(String perm, int args, String syntax) {
		this(false, perm, args, syntax);
	}

	public CommandBase(boolean player) {
		this(player, null, 0, null);
	}

	public CommandBase(boolean player, String perm) {
		this(player, perm, 0, null);
	}

	public CommandBase(boolean player, int args, String syntax) {
		this(player, null, args, syntax);
	}

	public CommandBase(boolean player, String perm, int args, String syntax) {
		this.player = player;
		this.perm = perm;
		this.args = args;
		this.syntax = syntax;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (this.player && 
				!(sender instanceof Player)) {
			sender.sendMessage("§cDieser Befehl kann nur von Spielern ausgeführt werden!");
			return false;
		} 

		if (this.perm != null && 
				!sender.hasPermission(this.perm)) {
			sender.sendMessage("§4Dafür hast du keine Rechte!");
			return false;
		} 

		if (args.length < this.args) {
			sender.sendMessage("§cSyntax: §6" + this.syntax);
			return false;
		} 
		if (sender instanceof Player) {
			return run(sender, (Player)sender, args);
		}
		return run(sender, null, args);
	}

	public abstract boolean run(CommandSender paramCommandSender, Player paramPlayer, String[] paramArrayOfString);
}
