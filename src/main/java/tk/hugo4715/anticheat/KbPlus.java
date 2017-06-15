package tk.hugo4715.anticheat;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.inventivetalent.update.spiget.SpigetUpdate;
import org.inventivetalent.update.spiget.UpdateCallback;
import org.inventivetalent.update.spiget.comparator.VersionComparator;

import com.google.common.collect.Sets;

import net.md_5.bungee.api.ChatColor;
import tk.hugo4715.anticheat.bstats.Metrics;
import tk.hugo4715.anticheat.check.KbChecker;
import tk.hugo4715.anticheat.player.ACPlayer;
import tk.hugo4715.tinyprotocol.PacketHook;

/**
 * @author hugo4715
 */
public class KbPlus extends JavaPlugin {
	public static final String PREFIX = ChatColor.GOLD + "[" + ChatColor.GREEN + "AntiCheat" + ChatColor.GOLD + "]" + ChatColor.GREEN;

	private Set<ACPlayer> players = Sets.newHashSet();
	private KbChecker kbChecker;
	private PacketHook packetHook;
	
	private File datafile;
	private YamlConfiguration data;
	
	
	@Override
	public void onEnable() {
		
		update();
		saveDefaultConfig();
		loadData();
		this.packetHook = new PacketHook(get());
		this.kbChecker = new KbChecker();
		loadMetrics();
	}

	private void loadMetrics() {
		Metrics metrics = new Metrics(this);
		metrics.addCustomChart(new Metrics.SingleLineChart("violations") {
			
			@Override
			public int getValue() {
				return data.getInt("violations");
			}
		});
	}


	private void loadData() {
		datafile = new File(getDataFolder(),"data.yml");
		
		if(datafile.exists()){
			data = YamlConfiguration.loadConfiguration(datafile);
		}else{
			//create it
			data = new YamlConfiguration();
			data.set("violations", 0);
			saveData();
		}
	}
	
	public YamlConfiguration getData() {
		return data;
	}
	
	public Set<ACPlayer> getPlayers() {
		return players;
	}
	
	public void saveData(){
		Validate.notNull(datafile,"Tried to save data before loading it!");
		try {
			data.save(datafile);
		} catch (IOException e) {
			e.printStackTrace();
			getLogger().severe("Could not save data.yml!");
		}
	}


	private void update() {
		final SpigetUpdate updater = new SpigetUpdate(this, 36140);
		updater.setVersionComparator(VersionComparator.SEM_VER);
		updater.checkForUpdate(new UpdateCallback() {
		    @Override
		    public void updateAvailable(String newVersion, String downloadUrl, boolean hasDirectDownload) {
		        // First check if there is a direct download available
		        // (Either the resources is hosted on spigotmc.org, or Spiget has a cached version to download)
		        // external downloads won't work if they are disabled (by default) in spiget.properties
		        if (hasDirectDownload) {
		            if (updater.downloadUpdate()) {
		                // Update downloaded, will be loaded when the server restarts
		            } else {
		                // Update failed
		                getLogger().warning("Update download failed, reason is " + updater.getFailReason());
		            }
		        }
		    }

		    @Override
		    public void upToDate() {
		    	getLogger().info("Plugin is up to date");
		    }
		});
	}


	public KbChecker getChecker() {
		return kbChecker;
	}

	public ACPlayer getACPlayer(Player p){
		return getACPlayer(p.getUniqueId());
	}
	
	public PacketHook getPacketHook() {
		return packetHook;
	}

	public ACPlayer getACPlayer(UUID id){
		
		for(ACPlayer p : players){
			if(p.getPlayer().getUniqueId().equals(id))return p;
		}
		
		ACPlayer p = new ACPlayer(Bukkit.getPlayer(id));
		players.add(p);
		return p;
	}
	
	public static KbPlus get(){
		return getPlugin(KbPlus.class);
	}
}
