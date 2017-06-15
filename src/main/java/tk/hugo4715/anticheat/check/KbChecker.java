package tk.hugo4715.anticheat.check;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.google.common.eventbus.Subscribe;

import tk.hugo4715.anticheat.KbPlus;
import tk.hugo4715.anticheat.player.ACPlayer;
import tk.hugo4715.tinyprotocol.event.PacketOutEvent;
import tk.hugo4715.tinyprotocol.packet.PacketAccessor;

public class KbChecker {
	protected static final int REPORT_TIME = 12000;

	public KbChecker() {
		KbPlus.get().getPacketHook().getEventBus().register(this);
		
		if(KbPlus.get().getConfig().getBoolean("enable-random-checks",true)){
			KbPlus.get().getLogger().info("Enabled random checks.");
			new BukkitRunnable() {
				
				@Override
				public void run() {
					for(ACPlayer gp : KbPlus.get().getPlayers()){
						double chance = 0.1;
						if(gp.violations > 0){
							chance = 1;
						}
						if(gp.getPlayer().isOnGround() && !gp.isInWater() && !gp.isOnLadder() && !gp.isInWeb() && Math.random() < chance){
							gp.getPlayer().setVelocity(new Vector(0,0.2,0));
						}
					}
				}
			}.runTaskTimer(KbPlus.get(), 20,20*5);
		}
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
					
					//sync process in order to fix 
					new BukkitRunnable() {
						
						@Override
						public void run() {
							//don't check if there is a ceiling or anything that could block from taking kb
							if(acp.hasCeiling() || !p.isOnGround() || acp.isOnLadder() || p.isInsideVehicle() || p.getFireTicks() > 0 || p.isFlying() || acp.isInWeb() || acp.isInWater() || p.isDead() || p.getGameMode().equals(GameMode.CREATIVE))return;

							
							final int ticksToReact = (int) (KbPlus.get().getConfig().getDouble("check-time",1.5)*20);//ticks for the client to get up

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
						}
					}.runTask(KbPlus.get());
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
			if(predictedY < realY || gp.getPlayer().hasPermission("knockbackplusplus.bypass")){
				//legit
				gp.onLegit();
			}else{
				//hax
				double percentage = Math.abs(((realY-predictedY)/predictedY) );
				gp.onViolation(percentage);
			}
	}



}
