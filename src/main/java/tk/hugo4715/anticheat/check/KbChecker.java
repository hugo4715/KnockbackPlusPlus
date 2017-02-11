package tk.hugo4715.anticheat.check;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;

import tk.hugo4715.anticheat.KbPlus;
import tk.hugo4715.anticheat.player.ACPlayer;

public class KbChecker {

	public KbChecker() {
		KbPlus.get().getProtocolManager().addPacketListener(new PacketAdapter(KbPlus.get(), ListenerPriority.MONITOR, PacketType.Play.Server.ENTITY_VELOCITY) {
			@Override
			public void onPacketSending(PacketEvent event) {
				PacketContainer c = event.getPacket();
				int entId = c.getIntegers().getValues().get(0);
//				Integer velX = c.getIntegers().getValues().get(1); not used
				Integer velY = c.getIntegers().getValues().get(2); 
//				Integer velZ = c.getIntegers().getValues().get(3); not used

				//search for player
				for(Player p : Bukkit.getOnlinePlayers()){
					
					//found player
					if(p.getEntityId() == entId){
						
						
						ACPlayer acp = KbPlus.get().getACPlayer(p);
						
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
			}
		});
	}

	
}
