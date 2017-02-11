package tk.hugo4715.anticheat;

import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.inventivetalent.update.spiget.SpigetUpdate;
import org.inventivetalent.update.spiget.UpdateCallback;

import com.google.common.collect.Sets;

import net.md_5.bungee.api.ChatColor;
import tk.hugo4715.anticheat.bstats.Metrics;
import tk.hugo4715.anticheat.check.KbChecker;
import tk.hugo4715.anticheat.player.ACPlayer;
import tk.hugo4715.tinyprotocol.PacketHook;

/**
 * DONE:
 *   - Auto updater
 *   - permission to bypass -> knockbackplusplus.bypass
 *   - custom command on violations -> see config.yml
 * @author hugo4715
 *
 */
public class KbPlus extends JavaPlugin {
	public static final String PREFIX = ChatColor.GOLD + "[" + ChatColor.GREEN + "AntiCheat" + ChatColor.GOLD + "]" + ChatColor.GREEN;

	private Set<ACPlayer> players = Sets.newHashSet();
	private KbChecker kbChecker;
	private PacketHook packetHook;

	@Override
	public void onEnable() {
		Metrics metrics = new Metrics(this);
		update();
		saveDefaultConfig();
		this.packetHook = new PacketHook(get());
		this.kbChecker = new KbChecker();
	}

	
	private void update() {
		final SpigetUpdate updater = new SpigetUpdate(this, 36140);
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
