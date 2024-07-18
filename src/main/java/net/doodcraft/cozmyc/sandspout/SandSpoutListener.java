package net.doodcraft.cozmyc.sandspout;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

public class SandSpoutListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onClick(PlayerAnimationEvent event) {
        Player player = event.getPlayer();
        BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

        if (bPlayer == null) return;

        if (bPlayer.getBoundAbilityName().equalsIgnoreCase("SandSpout")) {
            new SandSpout(player);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerMove(final PlayerMoveEvent event) {
        if (BendingPlayer.isWorldDisabled(event.getPlayer().getWorld())) return;
        if (event.getTo() == null) return;
        if (compareLocations(event.getFrom(), event.getTo())) return;

        final Player player = event.getPlayer();

        if (CoreAbility.hasAbility(player, SandSpout.class)) {
            Vector vel = new Vector();
            vel.setX(event.getTo().getX() - event.getFrom().getX());
            vel.setZ(event.getTo().getZ() - event.getFrom().getZ());

            double currentSpeed = vel.length();
            double maxSpeed = ConfigManager.defaultConfig.get().getDouble("ExtraAbilities.Cozmyc.SandSpout.FlySpeed") + 0.01;
            if (currentSpeed > maxSpeed) {
                vel = vel.normalize().multiply(maxSpeed);
                event.getPlayer().setVelocity(vel);
            }
        }
    }

    private boolean compareLocations(Location to, Location from) {
        return (int) to.getX() == (int) from.getX()
                && (int) to.getY() == (int) from.getY()
                && (int) to.getZ() == (int) from.getZ();
    }
}
