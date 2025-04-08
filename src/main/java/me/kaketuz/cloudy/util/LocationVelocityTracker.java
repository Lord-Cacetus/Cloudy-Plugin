package me.kaketuz.cloudy.util;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import me.kaketuz.cloudy.Cloudy;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;


//GEOMETRY DASH TUTUTUTUTUTUTUTUTUUTUTUT


/**
 * Hi HorizontalVelocityTracker!
 * @see com.projectkorra.projectkorra.object.HorizontalVelocityTracker
 * */
public class LocationVelocityTracker extends BukkitRunnable {

    private static final ConcurrentHashMap<Trackable, LocationVelocityTracker> trackers = new ConcurrentHashMap<>();

    private final Trackable trackerable;
    private double power;
    private final Location location;
    private final Vector velocity;
    private final long start;

    public LocationVelocityTracker(Location location, Vector velocity, Trackable tracker) {
        this.location = location;
        this.velocity = velocity;
        this.power = this.velocity.length();
        trackers.put(tracker, this);
        this.trackerable = tracker;
        this.start = System.currentTimeMillis();
        this.runTaskTimer(Cloudy.plugin, 1L, 0);
    }
    @Override
    public void run() {
        location.add(velocity);

        RayTraceResult result = Objects.requireNonNull(location.getWorld()).rayTraceBlocks(location, velocity, power, FluidCollisionMode.ALWAYS, true);

        if (System.currentTimeMillis() > start + 200) {
            Optional.ofNullable(result)
                    .map(RayTraceResult::getHitBlock)
                    .ifPresent(b -> {
                        if (GeneralMethods.isSolid(b)) cancel();
                        if (ElementalAbility.isWater(b)) power -= 0.1;
                    });
        }

        velocity.normalize().multiply(power);
        power -= 0.05;
        if (power <= 0) cancel();
    }

    @Override
    public synchronized void cancel() throws IllegalStateException {
        super.cancel();
        trackers.remove(trackerable);
    }

    public Location getLocation() {
        return location;
    }

    public static ConcurrentHashMap<Trackable, LocationVelocityTracker> getTrackers() {
        return trackers;
    }

    public Vector getVelocity() {
        return velocity;
    }
}
