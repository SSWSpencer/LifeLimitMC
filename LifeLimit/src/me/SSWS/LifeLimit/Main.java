package me.SSWS.LifeLimit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener{
	
	@Override
	public void onEnable() {
		this.saveDefaultConfig();
		this.getServer().getPluginManager().registerEvents(this, this);
		
		// Timer to Reset Daily Lives to Cap
		String resetLivesTime = this.getConfig().getString("resetLivesTime");
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				SimpleDateFormat formatter= new SimpleDateFormat("HH:mm:ss");
				Date date = new Date(System.currentTimeMillis());
				if(formatter.format(date).equals(resetLivesTime)) {
					dailyReset();
				}
			}
		},20,20);
		try {			
			File readMe = new File("./plugins/LifeLimit/README.yml");
			if(readMe.createNewFile()) {
				System.out.println("File Created!" + readMe.getName());
			} else {
				System.out.println("LifeLimit ReadMe Already Exists!");
			}
		}
		catch(IOException e) {
			System.out.println("An Error occurred.");
			e.printStackTrace();
		}
		try {
			FileWriter writeReadMe = new FileWriter("./plugins/LifeLimit/README.yml");
			writeReadMe.write("# ============== Configuration ==============\n");
			writeReadMe.write("# Config.yml will definitely get long as time progresses, so here's an outline of what there is to configure\n#\n");
			writeReadMe.write("# Quick refresh on data types (if you don't know them). Syntax is important in this config file.\n");
			writeReadMe.write("# [String]: A word or phase surrounded by quotes-- \"foo\", \"bar\", \"baz\", \"00:00:00\"\n");
			writeReadMe.write("# [Integer]: A whole number between -2147483647 and 2147483647, not surrounded by quotes-- 1, 2, 18, 52, 1597823\n");
			writeReadMe.write("# [Boolean]: A true or false value, not surrounded by quotes-- true, false\n#\n#\n");
			writeReadMe.write("# - [field]: [Value data type] [Description of what the specific field does]\n");
			writeReadMe.write("# - reloadMessage: [String] sets the success message returned when running /lifelimit reload\n#\n");
			writeReadMe.write("# - resetLivesTime: [String] sets the time that all players' current lives will reset to their daily life cap\n#\n");
			writeReadMe.write("# - globalDefaultLives: [Integer] sets the default number of daily lives a player will join the server with\n#\n");
			writeReadMe.write("# - firstTimeLoginCurrentLives: [Integer] sets the number of daily lives a player will have upon joining the server for the first time\n#\n");
			writeReadMe.write("# - autoRespawn: [Boolean] sets whether or not players go into spectator mode every time they die, or only when they run out of lives. \n");
			writeReadMe.write("#      true: players respawn in survival mode until they run out of lives\n");
			writeReadMe.write("#      false: players respawn in spectator mode after every death and have to perform /respawn\n#\n");
			writeReadMe.write("# - spectatorsMuted: [Boolean] sets whether or not players in spectator mode can chat with players in survival mode\n#\n");
			writeReadMe.write("#  - opsSeeSpectators: [Boolean] sets whether or not spectator chat will be visible to server operators\n#\n#\n");
			writeReadMe.write("# =============== Important Notes ===============\n");
			writeReadMe.write("# Plugin Data (playerDailyLives and playerCurrentLives)\n");
			writeReadMe.write("# It is recommended that you not edit these values in here, as it can cause the plugin to either break or lose data. \n");
			writeReadMe.write("# However, if you must, both playerDailyLives and playerCurrentLives take strings containing a 32-character UUID (including 4 hypens), followed by a colon and a space, then the player username, followed by a colon and space, and then an integer value\n");
			writeReadMe.write("# Formatted, it will look like - \"xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx: Player_Name: 1\" (the colons and spaces are important parts of the formatting and removing them will cause issues)\n");
			writeReadMe.write("# Changing the UUID will 100% cause a loss of data and possibly break the plugin entirely\n");
			writeReadMe.write("# Changing a player name will likely not affect anything. There is a small possibility that it will break the plugin, but there should be no reason to change a player's name in here, because if they change their name via minecraft.net, their UUID will stay the same and it will auto-update their name in here\n");
			writeReadMe.write("# Changing a player's daily/current life count is the only thing that you should be touching below here, and it won't even update until you /reload the server, whereas changing the values via commands will take immediate effect.\n#\n");
			writeReadMe.write("# Do not remove the placeholder line (- \"UUID, Username, Life Count\") until data has been generated and saved. You don't have to ever remove it, but if you like clean files with no dummy data, wait until after. Doing so prior to having any data will cause an index out of bounds error when trying to insert the first player");
			
			writeReadMe.close();
		}
		catch(IOException e) {
			System.out.println("An error occurred");
			e.printStackTrace();
		}
	}

	@Override
	public void onDisable() {

	}
	
	
	
	// ==============================  Command Listener ============================== 
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		// lifelimit administrative
		if (label.equalsIgnoreCase("lifelimit")) {
			if(!sender.hasPermission("lifelimit.admin")){
				sender.sendMessage(ChatColor.RED + "You cannot run this command");
				return true;
			}
			if(args.length > 0) {
				if(args[0].equalsIgnoreCase("help")) {
					sender.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "===== LifeLimit Help =====");
					sender.sendMessage(ChatColor.YELLOW + "/lifelimit help: " + ChatColor.WHITE + "shows a list of all available commands");
					sender.sendMessage(ChatColor.YELLOW + "/lifelimit reload: " + ChatColor.WHITE + "reloads the plugin (will not affect any of the life counts)");
					sender.sendMessage(ChatColor.YELLOW + "/lifelimit dailyReset: " + ChatColor.WHITE + "resets players' current lives to their daily value if current count is lower than daily");
					sender.sendMessage(ChatColor.YELLOW + "/lifelimit dailyResetHard: " + ChatColor.WHITE + "resets all players' current lives to their daily value");
					sender.sendMessage(ChatColor.YELLOW + "/lifelimit set daily [player] [number]: " + ChatColor.WHITE + "sets player's daily life count");
					sender.sendMessage(ChatColor.YELLOW + "/lifelimit set current [player] [number]: " + ChatColor.WHITE + "sets players's current life count");
				}
				
				// reload
				if(args[0].equalsIgnoreCase("reload")) {
					this.reloadConfig();
					sender.sendMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + this.getConfig().getString("reloadMessage"));
				}
				
				// force midnight (reset all players' current lives to their daily life count)
				if(args[0].equalsIgnoreCase("dailyReset")) {
					dailyReset();
					Bukkit.broadcastMessage("[LifeLimit]: Daily lives have been reset to their cap!");
				}
				
				if(args[0].equalsIgnoreCase("dailyResetHard")) {
					if(args.length > 1) {
						if(args[1].equalsIgnoreCase("confirm")) {
							dailyHardReset();
							Bukkit.broadcastMessage(ChatColor.RED + "[LifeLimit]: All lives have been reset to their maximum value.");
						}
						else {
							sender.sendMessage(ChatColor.RED + "[LifeLimit]: Doing this action will reset all lives to their daily value, including players who are currently above their daily cap.");
							sender.sendMessage(ChatColor.RED + "[LifeLimit]: If you would like to only reset the life count of players whose current life count is lower than their daily cap, use the command" + ChatColor.YELLOW + " /lifelimit dailyReset" + ChatColor.RED + " instead!");
							sender.sendMessage(ChatColor.RED + "[LifeLimit]: If you would like to proceed, run " + ChatColor.YELLOW + "/lifelimit dailyResetHard confirm");
						}
					}
					else {
						sender.sendMessage(ChatColor.RED + "[LifeLimit]: Doing this action will reset all lives to their daily value, including players who are currently above their daily cap.");
						sender.sendMessage(ChatColor.RED + "[LifeLimit]: If you would like to only reset the life count of players whose current life count is lower than their daily cap, use the command" + ChatColor.YELLOW + " /lifelimit dailyReset" + ChatColor.RED + " instead!");
						sender.sendMessage(ChatColor.RED + "[LifeLimit]: If you would like to proceed, run " + ChatColor.YELLOW + "/lifelimit dailyResetHard confirm");
					}
				}
				
				// set daily/current lives
				if(args[0].equalsIgnoreCase("set")) {
					if(args.length > 3) {			
						
						// set daily lives
						if(args[1].equalsIgnoreCase("daily")) {						
							if(NumberUtils.isParsable(args[3])) {
								try {
									int lifeCount = Integer.parseInt(args[3]);
									setDailyLives(sender, args[2], lifeCount);
								}
								catch(Exception e){
									sender.sendMessage(ChatColor.RED + "[LifeLimit]: The maximum integer limit in java is 2147483647, so that value has been applied.");
									setDailyLives(sender, args[2], 2147483647);
								}
							}
							else {
								sender.sendMessage(ChatColor.RED + "[LifeLimit]: That's not a number!");
								sender.sendMessage(ChatColor.RED + "[LifeLimit]: /lifelimit set daily [username] [number of lives]");
							}
						}
						
						//set current lives
						else if(args[1].equalsIgnoreCase("current")) {
							if(NumberUtils.isParsable(args[3])) {
								try {
									int lifeCount = Integer.parseInt(args[3]);
									setCurrentLives(sender, args[2], lifeCount);
								}
								catch(Exception e){
									sender.sendMessage(ChatColor.RED + "[LifeLimit]: The maximum integer limit in java is 2147483647, so that value has been applied.");
									setCurrentLives(sender, args[2], 2147483647);
								}
							}
							else {
								sender.sendMessage(ChatColor.RED + "[LifeLimit]: That's not a number!");
								sender.sendMessage(ChatColor.RED + "[LifeLimit]: /lifelimit set daily [username] [number of lives]");
							}
						}
						else {
							sender.sendMessage(ChatColor.RED + "[LifeLimit]: /lifelimit set [daily/current] [username] [number of lives]");
						}
						
					}
					else {
						sender.sendMessage(ChatColor.RED + "[LifeLimit]: /lifelimit set [daily|current] [username] [number of lives]");
					}
				}
			}
			else {
				sender.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "===== LifeLimit Help =====");
				sender.sendMessage(ChatColor.YELLOW + "/lifelimit help: " + ChatColor.WHITE + "shows a list of all available commands");
				sender.sendMessage(ChatColor.YELLOW + "/lifelimit reload: " + ChatColor.WHITE + "reloads the plugin (will not affect any of the life counts)");
				sender.sendMessage(ChatColor.YELLOW + "/lifelimit dailyReset: " + ChatColor.WHITE + "resets players' current lives to their daily value if current count is lower than daily");
				sender.sendMessage(ChatColor.YELLOW + "/lifelimit dailyResetHard: " + ChatColor.WHITE + "resets all players' current lives to their daily value");
				sender.sendMessage(ChatColor.YELLOW + "/lifelimit set daily [player] [number]: " + ChatColor.WHITE + "sets player's daily life count");
				sender.sendMessage(ChatColor.YELLOW + "/lifelimit set current [player] [number]: " + ChatColor.WHITE + "sets players's current life count");
			}
			
		}
		
		// respawn
		if(label.equalsIgnoreCase("respawn")) {
			Player player = this.getServer().getPlayer(sender.getName());
			int livesLeft = getLivesLeft(player.getUniqueId().toString(), player.getName());
			if(livesLeft > 0 && player.getGameMode().toString().equalsIgnoreCase("spectator") || player.isOp() || player.hasPermission("lifelimit.noLimit")) {
				player.setHealth(0);
				player.setGameMode(GameMode.SURVIVAL);
			}
			else if(!player.getGameMode().toString().equalsIgnoreCase("spectator")) {
				player.sendMessage(ChatColor.GREEN + "[LifeLimit]: You're already alive!");
			}
			else {
				player.sendMessage(ChatColor.RED + "[LifeLimit]: You're out of lives for today!");
			}
		}
		
		// life count
		if(label.equalsIgnoreCase("lifecount")) {
			Player player = this.getServer().getPlayer(sender.getName());
			int livesLeft = getLivesLeft(player.getUniqueId().toString(), player.getName());
			sender.sendMessage(ChatColor.LIGHT_PURPLE + "[LifeLimit]: You have " + livesLeft + " lives remaining today!");
		}
		
		// life timer
		if(label.equalsIgnoreCase("lifetime")) {
			sender.sendMessage(ChatColor.LIGHT_PURPLE + "[LifeLimit]: " + getTimeToReset());
		}
		
		return false;
	}
	
	// ==============================  Functions ============================== 
	
	public String getTimeToReset() {
		SimpleDateFormat formatter= new SimpleDateFormat("HH:mm:ss");
		Date date = new Date(System.currentTimeMillis());
		String currentTime = formatter.format(date);
		String resetTime = this.getConfig().getString("resetLivesTime");
		if(Integer.parseInt(resetTime.substring(0,2)) == -1) {
			return "[LifeLimit]: Daily resets have been disabled!";
		}
		int currentSeconds = Integer.parseInt(currentTime.substring(6));
		int resetSeconds = Integer.parseInt(resetTime.substring(6));
		int remainingSeconds = 0;
		int extraMinute = 0;
		if (currentSeconds > resetSeconds) {
			remainingSeconds = 60 - currentSeconds + resetSeconds;
			extraMinute = 1;
		}
		else {
			remainingSeconds = resetSeconds - currentSeconds;
		}
		
		int currentMinutes = Integer.parseInt(currentTime.substring(3,5)) + extraMinute;
		int resetMinutes = Integer.parseInt(resetTime.substring(3,5));
		int remainingMinutes = 0;
		int extraHour = 0;
		if(currentMinutes > resetMinutes) {
			remainingMinutes = 60 - currentMinutes + resetMinutes;
			extraHour = 1;
		}
		else {
			remainingMinutes = resetMinutes - currentMinutes;
		}
		
		int currentHours = Integer.parseInt(currentTime.substring(0,2)) + extraHour;
		int resetHours = Integer.parseInt(resetTime.substring(0,2));
		int remainingHours = 0;
		if(currentHours > resetHours) {
			remainingHours = 24 - currentHours + resetHours;
		}
		else {
			remainingHours = resetHours - currentHours;
		}
		return "Daily life count reset: " + remainingHours + " hours, " + remainingMinutes + " minutes, and " + remainingSeconds + " seconds!";
	}
	
	// increase player daily lives
	public void setDailyLives(CommandSender sender, String playerName, int amount) {
		String uuid = "";
		try {
			uuid = Bukkit.getPlayer(playerName).getUniqueId().toString();
			List<String> playersInConfig = this.getConfig().getStringList("playerDailyLives");
			boolean playerFound = false;
			for(int i = 0; i < playersInConfig.size(); i++) {
				if(playersInConfig.get(i).length() > 38) {				
					if(playersInConfig.get(i).contains(uuid)) {
						playersInConfig.set(i, uuid + ": " + Bukkit.getPlayer(playerName).getName() + ": " + amount);
						playerFound = true;
					}
				}
			}
			if(!playerFound) {
				sender.sendMessage(ChatColor.RED + "[LifeLimit]: Player \"" + playerName +"\" Not found!");
			}
			else {			
				this.getConfig().set("playerDailyLives", playersInConfig);
				this.saveConfig();
				sender.sendMessage(ChatColor.LIGHT_PURPLE + "[LifeLimit]: Successfully set player \"" + Bukkit.getPlayer(playerName).getName() + "\" to " + amount + " daily lives!");
			}
		}
		catch(Exception e) {
			sender.sendMessage(ChatColor.RED + "[LifeLimit]: Player \"" + playerName +"\" Not found!");
		}
		
	}
	
	// increase player current lives
	public void setCurrentLives(CommandSender sender, String playerName, int amount) {
		String uuid = "";
		try{
			uuid = Bukkit.getPlayer(playerName).getUniqueId().toString();
			List<String> playersInConfig = this.getConfig().getStringList("playerCurrentLives");
			boolean playerFound = false;
			for(int i = 0; i < playersInConfig.size(); i++) {
				if(playersInConfig.get(i).length() > 38) {				
					if(playersInConfig.get(i).contains(uuid)) {
						playersInConfig.set(i, uuid + ": " + Bukkit.getPlayer(playerName).getName() + ": " + amount);
						playerFound = true;
					}
				}
			}
			if(!playerFound) {
				sender.sendMessage(ChatColor.RED + "[LifeLimit]: Player \"" + playerName +"\" Not found!");
			}
			else {				
				this.getConfig().set("playerCurrentLives", playersInConfig);
				this.saveConfig();
				sender.sendMessage(ChatColor.LIGHT_PURPLE + "[LifeLimit]: Successfully set player \"" + Bukkit.getPlayer(playerName).getName() + "\" to " + amount + " current lives!");
			}
		}
		catch(Exception e){
			sender.sendMessage(ChatColor.RED + "[LifeLimit]: Player \"" + playerName +"\" Not found!");
		}
		
	}
	// increase player current lives (Overload)
		public void setCurrentLives(String playerName, int amount) {
			String uuid = Bukkit.getPlayer(playerName).getUniqueId().toString();
			List<String> playersInConfig = this.getConfig().getStringList("playerCurrentLives");
			for(int i = 0; i < playersInConfig.size(); i++) {
				if(playersInConfig.get(i).length() > 38) {				
					if(playersInConfig.get(i).contains(uuid)) {
						playersInConfig.set(i, uuid + ": " + playerName + ": " + amount);
					}
				}
			}
			this.getConfig().set("playerCurrentLives", playersInConfig);
			this.saveConfig();
		}
	
	// Reset Lives to Daily Limit (Soft)
	public void dailyReset() {
		for(Player p : Bukkit.getOnlinePlayers()) {
			if(p.getGameMode().toString().equalsIgnoreCase("Spectator")) {
				p.setHealth(0);
			}
			p.sendMessage(ChatColor.LIGHT_PURPLE + "===== LifeLimit Daily Reset =====");
			p.sendMessage(ChatColor.LIGHT_PURPLE + "[LifeLimit]: Resetting Lives of Fallen Players");
			p.setGameMode(GameMode.SURVIVAL);
		}
		List<String> playersDaily = this.getConfig().getStringList("playerDailyLives");
		for(int i = 0; i < playersDaily.size(); i++) {
			if(playersDaily.get(i).length() > 38) {				
				String uuid = playersDaily.get(i).substring(0,36);
				String name = "";
				String lifeString = "";
				int colonCount = 0;
				for(int j = 38; j < playersDaily.get(i).length(); j++) {
					if(playersDaily.get(i).charAt(j) == ':') {
						colonCount++;
					}
					if(colonCount == 0) {						
						name += playersDaily.get(i).charAt(j);
					}
					else if (colonCount == 1) {
						lifeString += playersDaily.get(i).charAt(j);
					}
					
				}
				int dailyLives = Integer.parseInt(lifeString.substring(2));
				int currentLives = getLivesLeft(uuid, name);
				if(currentLives > dailyLives) {
					String newString = uuid + ": " + name + ": " + currentLives;
					playersDaily.set(i, newString);
				}	
			}			
		}
		this.getConfig().set("playerCurrentLives", playersDaily);
		this.saveConfig();
	}
	
	// Reset Lives to Daily Limit (Hard)
	
	public void dailyHardReset() {
		for(Player p : Bukkit.getOnlinePlayers()) {
			if(p.getGameMode().toString().equalsIgnoreCase("Spectator")) {
				setCurrentLives(p.getName(), 2);
				p.setHealth(0);
			}
			p.sendMessage(ChatColor.DARK_RED + "===== LifeLimit Daily Reset =====");
			p.sendMessage(ChatColor.DARK_RED + "[LifeLimit]: Resetting Lives to Daily Limit");
			p.setGameMode(GameMode.SURVIVAL);
		}
		List<String> playersInConfig = this.getConfig().getStringList("playerDailyLives");
		this.getConfig().set("playerCurrentLives", playersInConfig);
		this.saveConfig();
	}
	
	// get remaining lives
	public int getLivesLeft(String uuid, String name) {
		List<String> playersInConfig = this.getConfig().getStringList("playerCurrentLives");
		int livesLeft = -1;
		for(int i = 0; i < playersInConfig.size(); i++) {
			if(playersInConfig.get(i).contains(uuid)) {
				int firstChar = uuid.length() + 1 + name.length() + 3;
				livesLeft = Integer.parseInt(playersInConfig.get(i).substring(firstChar));	
			}
		}
		
		return livesLeft;
	}
	
	// handle player join
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		String user = player.getUniqueId().toString();
		List<String> playersInConfig = this.getConfig().getStringList("playerDailyLives");
		boolean joinedBefore = false;
		for(int i = 0; i < playersInConfig.size(); i++) {
			if(playersInConfig.get(i).toString().contains(user)) {
				joinedBefore = true;
			}
		}
		if(joinedBefore) {
			for(int i = 0; i < playersInConfig.size(); i++) {
				if(playersInConfig.get(i).contains(player.getUniqueId().toString())) {
					
					// verify name is correctly linked to uuid
					if(playersInConfig.get(i).contains(player.getName())) {
						System.out.println("Name checks out!");
					}
					
					// fix name in event of name change
					else {
						// fix playerDailyLives
						String oldString = playersInConfig.get(i);
						String newString = "";
						int colonCount = 0;
						int nameCount = 0;
						for(int j = 0; j < oldString.length(); j++) {
							if(oldString.charAt(j) == ':') {
								colonCount++;
							}
							if(colonCount != 1) {
								newString += oldString.charAt(j);
							}
							if(colonCount == 1 && nameCount == 0) {
								newString += ": " + player.getName();
								nameCount++;
							}
						}
						playersInConfig.set(i, newString);
						this.getConfig().set("playerDailyLives", playersInConfig);
						this.saveConfig();
						
						// fix playerCurrentLives
						List<String> playersCurrent = this.getConfig().getStringList("playerCurrentLives");
						int pcIndex = -1;
						for(int j = 0; j < playersCurrent.size(); j++) {
							if(playersCurrent.get(j).contains(player.getUniqueId().toString())) {
								pcIndex = j;
							}
						}
						oldString = playersCurrent.get(pcIndex);
						newString = "";
						colonCount = 0;
						nameCount = 0;
						for(int j = 0; j < oldString.length(); j++) {
							if(oldString.charAt(j) == ':') {
								colonCount++;
							}
							if(colonCount != 1) {
								newString += oldString.charAt(j);
							}
							if(colonCount == 1 && nameCount == 0) {
								newString += ": " + player.getName();
								nameCount++;
							}
						}
						playersCurrent.set(pcIndex, newString);
						this.getConfig().set("playerCurrentLives", playersCurrent);
						this.saveConfig();
						
					}
					if(getLivesLeft(player.getUniqueId().toString(), player.getName()) > 0 && player.getGameMode().toString().equalsIgnoreCase("spectator")) {
						player.setHealth(0);
						player.setGameMode(GameMode.SURVIVAL);
					}
				}
			}
		}
		
		// add new player to the config
		else {
			playersInConfig.add(player.getUniqueId() + ": " + player.getName() + ": " + this.getConfig().getInt("globalDefaultLives"));
			this.getConfig().set("playerDailyLives", playersInConfig);
			this.saveConfig();
			
			playersInConfig = this.getConfig().getStringList("playerCurrentLives");
			playersInConfig.add(player.getUniqueId() + ": " + player.getName() + ": " + this.getConfig().getInt("firstTimeLoginCurrentLives"));
			this.getConfig().set("playerCurrentLives", playersInConfig);
			this.saveConfig();
		}
		
	}
	
	// Handle Player Death
//	@EventHandler
//	public void onPlayerDeath(PlayerDeathEvent event) {
//		Player player = event.getEntity();
//		if(player.getGameMode().toString().equalsIgnoreCase("spectator")) {
//			event.setDeathMessage("");
//		}
//		if(player.getGameMode().toString().equalsIgnoreCase("survival")) {
//			String uuid = player.getUniqueId().toString();
//			List<String> playersInConfig = this.getConfig().getStringList("playerCurrentLives");
//			for(int i = 0; i < playersInConfig.size(); i++) {
//				if(playersInConfig.get(i).contains(uuid)) {
//					int firstChar = uuid.length() + 1 + player.getName().length() + 3;
//					int livesLeft = Integer.parseInt(playersInConfig.get(i).substring(firstChar)) - 1;
//					playersInConfig.set(i, (player.getUniqueId() + ": " + player.getName() + ": " + livesLeft));
//					
//					
//					if(livesLeft <= 0) {
//						player.setGameMode(GameMode.SPECTATOR);
//						player.sendMessage(ChatColor.BOLD + "" + ChatColor.LIGHT_PURPLE + "You have used up all of your lives for today!");
//					}
//					
//				}
//			}
//			this.getConfig().set("playerCurrentLives", playersInConfig);
//			this.saveConfig();
//		}
//		
//		
//	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		if(player.getGameMode().toString().equalsIgnoreCase("spectator")) {
			event.setDeathMessage("");
		}
		else {
			String uuid = player.getUniqueId().toString();
			int livesLeft = getLivesLeft(uuid, player.getName());
			setCurrentLives(player.getName(), livesLeft - 1);
			if(!this.getConfig().getBoolean("autoRespawn")) {				
				player.setGameMode(GameMode.SPECTATOR);
			}
			if(livesLeft - 1 <= 0) {
				player.setGameMode(GameMode.SPECTATOR);
				player.sendMessage(ChatColor.RED + "[LifeLimit]: You have run out of lives!");
				player.sendMessage(ChatColor.RED + getTimeToReset());
			}
			if(livesLeft - 1 > 0) {
				player.sendMessage(ChatColor.GREEN + "[LifeLimit]: You have died, but it's okay! You've still got " + (livesLeft - 1) +" lives remaining!.");
				if(!this.getConfig().getBoolean("autoRespawn")) {					
					player.sendMessage(ChatColor.GREEN + "[LifeLimit]: You may use /respawn to live again today!");
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		if(this.getConfig().getBoolean("spectatorsMuted")) {			
			Player player = event.getPlayer();
			if(player.getGameMode().toString().equalsIgnoreCase("spectator")) {
				event.setCancelled(true);
				for(Player p : Bukkit.getOnlinePlayers()) {
					if(this.getConfig().getBoolean("opsSeeSpectators")) {						
						if(p.getGameMode().toString().equalsIgnoreCase("spectator") || p.isOp()) {
							Bukkit.getPlayer(p.getName()).sendMessage("[RIP] <" + player.getName() + "> " + event.getMessage());
						}
					}
					else {
						if(p.getGameMode().toString().equalsIgnoreCase("spectator")) {
							Bukkit.getPlayer(p.getName()).sendMessage("[RIP] <" + player.getName() + "> " + event.getMessage());
						}
					}
				}
			}
		}
	}
	

}