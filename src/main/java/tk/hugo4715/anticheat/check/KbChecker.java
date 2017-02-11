package tk.hugo4715.anticheat.check;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.eventbus.Subscribe;

import tk.hugo4715.anticheat.KbPlus;
import tk.hugo4715.anticheat.player.ACPlayer;
import tk.hugo4715.tinyprotocol.event.PacketOutEvent;
import tk.hugo4715.tinyprotocol.packet.PacketAccessor;

public class KbChecker {

	public KbChecker() {
		KbPlus.get().getPacketHook().getEventBus().register(this);
	}
	
	@Subscribe
	public void onPacketEvent(PacketOutEvent e) {
		if(!e.getPacket().getPacketClassSimpleName().equals("PacketPlayOutEntityVelocity"))return;
		
		try {

			PacketAccessor c = e.getPacket();
			int entId = c.getField(0).getInt(c.getHandle());
			Integer velY = c.getField(2).getInt(c.getHandle());
			
			//search for player
			for(Player p : Bukkit.getOnlinePlayers()){
				
				//found player
				if(p.getEntityId() == entId){
					
					
					ACPlayer acp = KbPlus.get().getACPlayer(p);
					
					if(p.hasPermission("knockbackplusplus.bypass")){
						acp.onLegit();
						return;
					}
					//don't check if there is a ceiling
					if(acp.hasCeiling())return;
					//calculate y move 
					double yMove = velY / 8000.0;
					double yMin = (yMove*100.0)/120.0;//(20% allowed)
					
					//if the player should move a bit
					if(yMove > 0.2){
						double baseY = p.getLocation().getY();

						//give client half a second to react
						new BukkitRunnable() {
							private int lvl = 0;

							@Override
							public void run() {
								//player is too low
								if(baseY+yMin > p.getLocation().getY()){
									//violations
									lvl++;
									if(lvl > 20){
										acp.onViolation();
										cancel();
										Bukkit.getScheduler().cancelTask(getTaskId());
									}
								}else{
									acp.onLegit();
									cancel();
									Bukkit.getScheduler().cancelTask(getTaskId());
								}
							}
						}.runTaskTimer(KbPlus.get(), 1, 1);
					}
					return;
				}
			}
		} catch (Exception e2) {
			e2.printStackTrace();
		}
		
		
	}

	
}
