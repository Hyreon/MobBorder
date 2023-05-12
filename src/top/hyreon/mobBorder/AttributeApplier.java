package top.hyreon.mobBorder;

import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

import java.util.Comparator;
import java.util.Random;

public class AttributeApplier implements Listener {

    static MobBorderPlugin plugin;

    AttributeApplier(MobBorderPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public static void entitySpawnEvent(EntitySpawnEvent e) {
        if (!e.getEntityType().isAlive()
                || !(e.getEntity() instanceof Attributable)
                || e.getEntityType() == EntityType.PLAYER
                || !plugin.usingAttributes()
                || plugin.ignoresMob(e.getEntityType())) return;



        Entity spawnee = e.getEntity();
        Player player = getNearest(spawnee);

        if (player == null) return;

        int mLevel = plugin.getLevelByLocation(spawnee.getLocation());
        int pLevel = player.getLevel();

        AttributeInstance speed = ((Attributable) e.getEntity()).getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        speed.addModifier(new AttributeModifier("mobborderspeed", plugin.getSpeedBuff(mLevel, pLevel) - 1, AttributeModifier.Operation.ADD_SCALAR));

        AttributeInstance keen = ((Attributable) e.getEntity()).getAttribute(Attribute.GENERIC_FOLLOW_RANGE);
        keen.addModifier(new AttributeModifier("mobborderkeen", plugin.getKeenBuff(mLevel, pLevel) - 1, AttributeModifier.Operation.ADD_SCALAR));


    }

    private static Player getNearest(Entity spawnee) {

        return spawnee.getWorld().getPlayers().stream()
                .min(Comparator.comparingDouble((p) -> p.getLocation().distanceSquared(spawnee.getLocation())))
                .orElse(null);

    }

}
