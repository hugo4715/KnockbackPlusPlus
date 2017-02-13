package tk.hugo4715.anticheat.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class Util {
	public boolean isThereWallsAround(Player p) {
		for (int x = p.getLocation().getBlockX() - 1; x < x + 2; x++) {
			for (int z = p.getLocation().getBlockZ() - 1; z < z + 2; z++) {
				Block b = p.getWorld().getBlockAt(x, p.getLocation().getBlockY(), z);

				if (b.getType() != Material.AIR) {
					return true;
				}
			}
		}
		return false;
	}
	
	private static Method getHandleMethod;
    private static Field pingField;
	public static int getPing(Player player) {
        try {
            if (getHandleMethod == null) {
                getHandleMethod = player.getClass().getDeclaredMethod("getHandle");
                getHandleMethod.setAccessible(true);
            }
            Object entityPlayer = getHandleMethod.invoke(player);
            if (pingField == null) {
                pingField = entityPlayer.getClass().getDeclaredField("ping");
                pingField.setAccessible(true);
            }
            return pingField.getInt(entityPlayer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
}
