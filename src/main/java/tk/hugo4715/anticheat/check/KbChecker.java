package tk.hugo4715.anticheat.check;

import org.apache.commons.lang.exception.NestableDelegate;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.eventbus.Subscribe;

import tk.hugo4715.anticheat.KbPlus;
import tk.hugo4715.anticheat.player.ACPlayer;
import tk.hugo4715.anticheat.util.Util;
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
					if(acp.hasCeiling() || !p.isOnGround() || p.isInsideVehicle() || p.getFireTicks() > 0 || p.isFlying() || p.isDead() || p.getGameMode().equals(GameMode.CREATIVE))return;
					//calculate y move 
//					double yMove = velY / 8000.0;
					double yMove = velY / 3100.0;
					
					int ping = Util.getPing(p);
					int timeToReact =  ping*ping;
					if(timeToReact < 500)timeToReact = 500;
					final int ticksToReact = timeToReact/ 20;

					//if the player should move a bit
					if(yMove > 0.2){


						//give client half a second to react
						new BukkitRunnable() {
							private int iterations = 0;
							double reachedY = 0;//dif reached
							double baseY = p.getLocation().getY();

							@Override
							public void run() {
								iterations++;

								if(p.getLocation().getY()-baseY > reachedY)reachedY = p.getLocation().getY()-baseY;

								if(iterations > ticksToReact){
									//check
									double diff = yMove-reachedY;
									double percent =  diff/yMove*100;
									System.out.println("reached: " + reachedY + " needed: " + yMove  + "  %:" + percent + " bool: " + (diff > 0));

									if(percent > 0)acp.onViolation(percent);
									else acp.onLegit();
									cancel();
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
