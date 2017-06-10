package tk.hugo4715.anticheat.check;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
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

					//don't check if there is a ceiling or anything that could block from taking kb
					if(acp.hasCeiling() || !p.isOnGround() || p.isInsideVehicle() || p.getFireTicks() > 0 || p.isFlying() || p.isDead() || p.getGameMode().equals(GameMode.CREATIVE))return;

					
					final int ticksToReact = (int) (1.5*20);//ticks for the client to get up

					if(velY < 5000){
						//give client some time to react
						new BukkitRunnable() {
							private int iterations = 0;
							double reachedY = 0;//dif reached
							double baseY = p.getLocation().getY();

							@Override
							public void run() {
								iterations++;
								if(p.getLocation().getY()-baseY > reachedY)reachedY = p.getLocation().getY()-baseY;
								if(iterations > ticksToReact){
									checkKnockback(acp,velY,reachedY);
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
	
	private void checkKnockback(ACPlayer gp , int packetY, double realY){
			//old equation is y = 0,0006x - 0,8253 (thx excel)
			//new equation is y = 8E-08x2 + 1E-04x - 0,0219 
//			double predictedY = 0.0006 * packetY - 0.8253;
			double predictedY = (0.00000008 * packetY * packetY) + (0.0001 * packetY)- 0.0219;
			packetY -= 0.05;
			if(predictedY < realY){
				//legit
				gp.onLegit();
			}else{
				//hax
				double percentage = Math.abs(((realY-predictedY)/predictedY) );
				gp.onViolation(percentage);
			}
	}



}
