package uk.co.ElmHoe;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import uk.co.ElmHoe.Utilities.ItemUtility;
import uk.co.ElmHoe.Utilities.PlayerUtility;
import uk.co.ElmHoe.Utilities.StringUtility;

public class PickAxeLevels extends JavaPlugin{
	
	public class EventListener implements Listener {
		public EventListener(PickAxeLevels plugin){
			plugin.getServer().getPluginManager().registerEvents(this,  plugin);
		}
		
		@EventHandler(priority = EventPriority.HIGHEST)
		public void onKill(PlayerDeathEvent event){
			Player killed = event.getEntity();
			if(killed.getKiller() instanceof Player){
				if(killed.getKiller() != null){
					Player killer = killed.getKiller();
					if(!(playerKills.containsKey(killer.getUniqueId()) && playerKills.get(killer.getUniqueId()).equals(killed.getUniqueId()))){
						if(killer.getInventory().getItemInMainHand() != null){
							if(killer.getInventory().getItemInMainHand().getType().equals(Material.DIAMOND_SWORD)){
								if(killer.getInventory().getItemInMainHand().hasItemMeta()){
									ItemMeta meta = killer.getInventory().getItemInMainHand().getItemMeta();
									if(meta.getDisplayName() != null){
										if(meta.getDisplayName().contains(depictChar)){
											int kills = getKills(meta) + 1;
											Tier currentTier = null;
											boolean rankedUp = false;
											for(Tier tier : swordTiers){
												if(tier.getThreshold() < kills){
													currentTier = tier;
													rankedUp = false;
													if(kills - 1 == tier.getThreshold()){
														rankedUp = true;
													}
												}else{
													break;
												}
											}
											int level = swordTiers.indexOf(currentTier) + 1;
											int killsLeft = 0;
											if(tiers.indexOf(currentTier) != tiers.size() - 1){
												killsLeft = tiers.get(tiers.indexOf(currentTier) + 1).getThreshold() - kills;
											}
											if(rankedUp){
												killer.sendMessage(swordRankUpMessage.replace("{KILLS}", "" + kills).replace("{LEVEL}", "" + level).replace("{MAXLEVEL}", "" + swordMaxLevels).replace("{MONEY}", StringUtility.formatMoney(currentTier.getMoney())));
												meta = ItemUtility.applyEnchants(meta, currentTier.getEnchants());
												PlayerUtility.deposite(killer.getUniqueId(), currentTier.getMoney());
												if(currentTier.isRepaired()){
													killer.getInventory().getItemInMainHand().setDurability((short)0);
												}
											}
											meta = setKills(meta, kills, level, killsLeft);
										}else if(meta.getDisplayName().equals(swordCreateName)){
											meta = initSword(meta);
										}
										playerKills.put(killer.getUniqueId(), killed.getUniqueId());
										killer.getInventory().getItemInMainHand().setItemMeta(meta);
									}
								}
							}
						}
					}
				}
			}
		}
		
		@EventHandler(priority = EventPriority.HIGHEST)
		public void onBreak(BlockBreakEvent event){
			if(!event.isCancelled()){
				Player player = event.getPlayer();
				if(player.getInventory().getItemInMainHand() != null){
					if(player.getInventory().getItemInMainHand().getType().equals(Material.DIAMOND_PICKAXE)){
						if(player.getInventory().getItemInMainHand().hasItemMeta()){
							ItemMeta meta = player.getInventory().getItemInMainHand().getItemMeta();
							if(meta.getDisplayName() != null){
								if(meta.getDisplayName().contains(depictChar)){
									int blocks = getBlocks(meta) + 1;
									Tier currentTier = null;
									boolean rankedUp = false;
									for(Tier tier : tiers){
										if(tier.getThreshold() < blocks){
											currentTier = tier;
											rankedUp = false;
											if(blocks - 1 == tier.getThreshold()){
												rankedUp = true;
											}
										}else{
											break;
										}
									}
									int level = tiers.indexOf(currentTier) + 1;
									int blocksLeft = 0;
									if(tiers.indexOf(currentTier) != tiers.size() - 1){
										blocksLeft = tiers.get(tiers.indexOf(currentTier) + 1).getThreshold() - blocks;
									}
									if(rankedUp){
										player.sendMessage(rankUpMessage.replace("{BLOCKS}", "" + blocks).replace("{LEVEL}", "" + level).replace("{MAXLEVEL}", "" + maxLevels).replace("{MONEY}", StringUtility.formatMoney(currentTier.getMoney())));
										meta = ItemUtility.applyEnchants(meta, currentTier.getEnchants());
										PlayerUtility.deposite(player.getUniqueId(), currentTier.getMoney());
										if(currentTier.isRepaired()){
											player.getInventory().getItemInMainHand().setDurability((short)0);
										}
									}
									meta = setBlocks(meta, blocks, level, blocksLeft);
								}else if(meta.getDisplayName().equals(createName)){
									meta = initPick(meta);
								}
								player.getInventory().getItemInMainHand().setItemMeta(meta);
							}
						}
					}
				}
			}
		}
	}
	
	private int getBlocks(ItemMeta meta){
		String line = meta.getLore().get(loreLine);
		String number = "";
		for(char chr : line.toCharArray()){
			if(StringUtility.isNumeric("" + chr)){
				number += chr;
			}
		}
		return Integer.parseInt(number);
	}
	
	private int getKills(ItemMeta meta){
		String line = meta.getLore().get(swordLoreLine);
		String number = "";
		for(char chr : line.toCharArray()){
			if(StringUtility.isNumeric("" + chr)){
				number += chr;
			}
		}
		return Integer.parseInt(number);
	}
	
	private ItemMeta setBlocks(ItemMeta meta, int blocks, int level, int blocksLeft){
		ItemMeta clone = meta.clone();
		clone.setDisplayName(depictChar + name.replace("{BLOCKS}", "" + blocks).replace("{LEVEL}", "" + level).replace("{MAXLEVEL}", "" + maxLevels).replace("{BLOCKSLEFT}", "" + blocksLeft));
		List<String> lore = new ArrayList<String>();
		for(String line : loreLines){
			lore.add(line.replace("{BLOCKS}", "" + blocks).replace("{LEVEL}", "" + level).replace("{MAXLEVEL}", "" + maxLevels).replace("{BLOCKSLEFT}", "" + blocksLeft));
		}
		String lastLine = "";
		for(char chr : Integer.toString(blocks).toCharArray()){
			lastLine += "§" + chr;
		}
		lore.add(lastLine);
		clone = ItemUtility.applyEnchants(clone, tiers.get(level - 1).getEnchants());
		clone.setLore(lore);
		return clone;
	}
	
	private ItemMeta setKills(ItemMeta meta, int kills, int level, int killsLeft){
		ItemMeta clone = meta.clone();
		clone.setDisplayName(depictChar + name.replace("{KILLS}", "" + kills).replace("{LEVEL}", "" + level).replace("{MAXLEVEL}", "" + maxLevels).replace("{KILLSLEFT}", "" + killsLeft));
		List<String> lore = new ArrayList<String>();
		for(String line : swordLoreLines){
			lore.add(line.replace("{KILLS}", "" + kills).replace("{LEVEL}", "" + level).replace("{MAXLEVEL}", "" + maxLevels).replace("{KILLSLEFT}", "" + killsLeft));
		}
		clone = ItemUtility.applyEnchants(clone, tiers.get(level - 1).getEnchants());
		clone.setLore(lore);
		clone = ItemUtility.setData(clone, kills);
		return clone;
	}
	
	private ItemMeta initSword(ItemMeta meta){
		ItemMeta clone = meta.clone();
		clone.setDisplayName(depictChar + name.replace("{BLOCKS}", "" + 0).replace("{LEVEL}", "" + 0).replace("{MAXLEVEL}", "" + maxLevels));
		List<String> lore = new ArrayList<String>();
		for(String line : swordLoreLines){
			lore.add(line);
		}
		lore.add("§0");
		clone.setLore(lore);
		return clone;
	}
	
	private ItemMeta initPick(ItemMeta meta){
		ItemMeta clone = meta.clone();
		clone.setDisplayName(depictChar + name.replace("{BLOCKS}", "" + 0).replace("{LEVEL}", "" + 0).replace("{MAXLEVEL}", "" + maxLevels));
		List<String> lore = new ArrayList<String>();
		for(String line : loreLines){
			lore.add(line);
		}
		lore.add("§0");
		clone.setLore(lore);
		return clone;
	}
	
	File configFile;
	FileConfiguration config;
	String header = ChatColor.GOLD + "[" + ChatColor.AQUA + "PickAxeLeveling" + ChatColor.GOLD + "] ";
	List<Tier> tiers = new ArrayList<Tier>();
	List<Tier> swordTiers = new ArrayList<Tier>();
	List<String> loreLines = new ArrayList<String>();
	List<String> swordLoreLines = new ArrayList<String>();
	String name;
	String swordName;
	String createName;
	String swordCreateName;
	String rankUpMessage;
	String swordRankUpMessage;
	int loreLine;
	int swordLoreLine;
	String depictChar = "§k§r§f§r";
	int maxLevels;
	int swordMaxLevels;
	Map<UUID, UUID> playerKills = new HashMap<UUID, UUID>();
	
	public void onEnable(){
		Bukkit.getConsoleSender().sendMessage(header + "Loading Please Wait....");
		configFile = new File(getDataFolder(), "config.yml");
		try{
			firstRun();
		} catch (Exception e){
			e.printStackTrace();
		}
		config = new YamlConfiguration();
	    loadYamls();
	    new EventListener(this);
		init();
		Bukkit.getConsoleSender().sendMessage(header + "Version 1.0 by ElmHoe");
	}
	
	private void init(){
		loreLines = StringUtility.format(config.getStringList("config.messages.lore"));
		loreLine = loreLines.size();
		name = StringUtility.format(config.getString("config.messages.name"));
		createName = StringUtility.format(config.getString("config.messages.createName"));
		rankUpMessage = StringUtility.format(config.getString("config.messages.levelUp"));
		if(config.getConfigurationSection("config.levelData") != null){
			for(String index : config.getConfigurationSection("config.levelData").getKeys(false)){
				int threshold = config.getInt("config.levelData." + index + ".blockThreshold");
				boolean repair = config.getBoolean("config.levelData." + index + ".repair");
				double money = config.getDouble("config.levelData." + index + ".money");
				List<String> enchants = config.getStringList("config.levelData." + index + ".enchants");
				tiers.add(new Tier(threshold, enchants, money, repair));
			}
		}
		maxLevels = tiers.size();
		
		swordLoreLines = StringUtility.format(config.getStringList("config.messages.swordLore"));
		swordLoreLine = swordLoreLines.size();
		swordName = StringUtility.format(config.getString("config.messages.swordName"));
		swordCreateName = StringUtility.format(config.getString("config.messages.swordCreateName"));
		swordRankUpMessage = StringUtility.format(config.getString("config.messages.swordLevelUp"));
		if(config.getConfigurationSection("config.swordData") != null){
			for(String index : config.getConfigurationSection("config.levelData").getKeys(false)){
				int threshold = config.getInt("config.swordData." + index + ".killThreshold");
				boolean repair = config.getBoolean("config.swordData." + index + ".repair");
				double money = config.getDouble("config.swordData." + index + ".money");
				List<String> enchants = config.getStringList("config.swordData." + index + ".enchants");
				swordTiers.add(new Tier(threshold, enchants, money, repair));
			}
		}
		swordMaxLevels = swordTiers.size();
	}
	
	private void firstRun() throws Exception {
	    if(!configFile.exists()){
	        configFile.getParentFile().mkdirs();
	        copy(getResource("config.yml"), configFile);
	    }
	}
	
	private void copy(InputStream in, File file) {
	    try {
	        OutputStream out = new FileOutputStream(file);
	        byte[] buf = new byte[1024];
	        int len;
	        while((len=in.read(buf))>0){
	            out.write(buf,0,len);
	        }
	        out.close();
	        in.close();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	private void loadYamls() {
	    try {
	        config.load(configFile);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
}