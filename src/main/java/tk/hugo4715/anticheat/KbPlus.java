package tk.hugo4715.anticheat;

import java.util.Set;
import java.util.UUID;

import com.comphenix.protocol.ProtocolLibrary;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.UnknownDependencyException;
import org.bukkit.plugin.java.JavaPlugin;
import org.inventivetalent.update.spiget.SpigetUpdate;
import org.inventivetalent.update.spiget.UpdateCallback;
import org.inventivetalent.update.spiget.comparator.VersionComparator;

import com.google.common.collect.Sets;

import net.md_5.bungee.api.ChatColor;
import tk.hugo4715.anticheat.bstats.Metrics;
import tk.hugo4715.anticheat.check.KbChecker;
import tk.hugo4715.anticheat.player.ACPlayer;

/**
 * @author hugo4715
 */
public class KbPlus extends JavaPlugin {
	public static final String PREFIX = ChatColor.GOLD + "[" + ChatColor.GREEN + "AntiCheat" + ChatColor.GOLD + "]" + ChatColor.GREEN;

	private Set<ACPlayer> players = Sets.newHashSet();
	private KbChecker kbChecker;

	@Override
	public void onEnable() {
		update();
		saveDefaultConfig();
		loadMetrics();

		if(Bukkit.getPluginManager().isPluginEnabled("ProtocolLib")){
			this.kbChecker = new KbChecker();
		}else{

			getLogger().severe("");
			getLogger().severe("");
			getLogger().severe("");
			getLogger().severe("");
			getLogger().severe("ProtocolLib is required for this plugin!");
			getLogger().severe("");
			getLogger().severe("");
			getLogger().severe("");
			getLogger().severe("");
			Bukkit.getPluginManager().disablePlugin(this);
			throw new UnknownDependencyException("ProtocolLib");
		}
	}

	@Override
	public void onDisable() {
		if(kbChecker != null) {
			ProtocolLibrary.getProtocolManager().removePacketListener(kbChecker);
		}
	}

	private void loadMetrics() {
		Metrics metrics = new Metrics(this);
		metrics.addCustomChart(new Metrics.SimplePie("protocollib_version") {
			@Override
			public String getValue() {
				return Bukkit.getPluginManager().isPluginEnabled("ProtocolLib") ?
						Bukkit.getPluginManager().getPlugin("ProtocolLib").getDescription().getVersion()
						: "false";
			}
		});
	}

	public Set<ACPlayer> getPlayers() {
		return players;
	}

	private void update() {
		final SpigetUpdate updater = new SpigetUpdate(this, 36140);
		updater.setVersionComparator(VersionComparator.SEM_VER);
		updater.checkForUpdate(new UpdateCallback() {
			@Override
			public void updateAvailable(String newVersion, String downloadUrl, boolean hasDirectDownload) {
				if (hasDirectDownload) {
					getLogger().info("Found version version " + newVersion);
					getLogger().info("Downloading updated jar");
					if (updater.downloadUpdate()) {
						// Update downloaded, will be loaded when the server restarts
						getLogger().info("Downloaded plugin successfuly, it wil be loaded on next server restart");
					} else {
						// Update failed
						getLogger().warning("Update download failed, reason is " + updater.getFailReason());
					}
				}else{
					getLogger().info("A new version is available!");
					getLogger().info("Version: " + getDescription().getVersion());
					getLogger().info("Newest version: " + newVersion);
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
