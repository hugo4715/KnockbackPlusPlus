package tk.hugo4715.anticheat;

import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.google.common.collect.Sets;

import net.md_5.bungee.api.ChatColor;
import tk.hugo4715.anticheat.bstats.Metrics;
import tk.hugo4715.anticheat.check.KbChecker;
import tk.hugo4715.anticheat.player.ACPlayer;

/**
 * TODO:
 *   - Auto updater
 *   - custom command on violations
 * @author hugo4715
 *
 */
public class KbPlus extends JavaPlugin {
	public static final String PREFIX = ChatColor.GOLD + "[" + ChatColor.GREEN + "AntiCheat" + ChatColor.GOLD + "]" + ChatColor.GREEN;


	private Set<ACPlayer> players = Sets.newHashSet();

	private KbChecker kbChecker;


	private ProtocolManager protocolManager;


	private Metrics metrics;

	@Override
	public void onEnable() {
		this.metrics = new Metrics(this);
	    protocolManager = ProtocolLibrary.getProtocolManager();
		kbChecker = new KbChecker();
	}

	
	public KbChecker getChecker() {
		return kbChecker;
	}

	public ACPlayer getACPlayer(Player p){
		return getACPlayer(p.getUniqueId());
	}
	
	public ProtocolManager getProtocolManager() {
		return protocolManager;
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
