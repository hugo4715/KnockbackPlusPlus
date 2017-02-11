package tk.hugo4715.anticheat.player;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import tk.hugo4715.anticheat.KbPlus;

public class ACPlayer {
	private Player player;
	
	private int violations = 0;
	
	public ACPlayer(Player p) {
		this.player = p;
	}
	
	public boolean hasCeiling(){
		Location loc = player.getLocation().clone().add(0, 2, 0);
		if(loc.getBlock().getType().isSolid())return true;
		else if(loc.getX() > 0.66 && loc.getBlock().getRelative(BlockFace.EAST).getType().isSolid())return true;
		else if(loc.getX() < -0.66 && loc.getBlock().getRelative(BlockFace.WEST).getType().isSolid())return true;
		else if(loc.getZ() > 0.66 && loc.getBlock().getRelative(BlockFace.SOUTH).getType().isSolid())return true;
		else if(loc.getZ() < -0.66 && loc.getBlock().getRelative(BlockFace.NORTH).getType().isSolid())return true;
		
		return false;
	}
	
	
	public Player getPlayer() {
		return player;
	}

	public void onViolation() {
		violations++;
		if(violations > 2){
			List<String> cmds = KbPlus.get().getConfig().getStringList("cmd-on-ban");
			cmds.forEach( cmd -> {
				cmd = cmd.replace("%player%", player.getName()).replace("%prefix%", KbPlus.PREFIX);
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
			});
			player.setBanned(true);
		}
	}
	
	public void onLegit(){
		violations --;
		if(violations < 0)violations = 0;
	}
}
