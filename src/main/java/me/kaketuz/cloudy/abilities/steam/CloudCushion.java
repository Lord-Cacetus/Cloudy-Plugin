package me.kaketuz.cloudy.abilities.steam;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import me.kaketuz.cloudy.Cloudy;
import me.kaketuz.cloudy.abilities.steam.combos.FollowingSteams;
import me.kaketuz.cloudy.abilities.steam.util.Cloud;
import me.kaketuz.cloudy.abilities.sub.SteamAbility;
import me.kaketuz.cloudy.util.Methods;
import me.kaketuz.cloudy.util.Particles;
import me.kaketuz.cloudy.util.Sounds;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;


import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

//AirCushion?????????????????
public class CloudCushion extends SteamAbility implements AddonAbility {

    private double range, speed, radius, sourceRadius, followSpeed, knockback;
    private long cooldown, duration, startCushionTiming;
    private Cloud cloud;

    private Location origin, location;
    private Vector direction;

    private boolean isReadyToLaunch, isLaunched, isCushion;

    private Location center;

    public CloudCushion(Player player, boolean instant) {
        super(player);
        if (!bPlayer.canBendIgnoreBinds(this)) return;

        range = Cloudy.config.getDouble("Steam.CloudCushion.Range");
        speed = Cloudy.config.getDouble("Steam.CloudCushion.Speed");
        radius = Cloudy.config.getDouble("Steam.CloudCushion.Radius");
        sourceRadius = Cloudy.config.getDouble("Steam.CloudCushion.SourceRadius");
        knockback = Cloudy.config.getDouble("Steam.CloudCushion.Knockback");
        cooldown = Cloudy.config.getLong("Steam.CloudCushion.Cooldown");
        duration = Cloudy.config.getLong("Steam.CloudCushion.Duration");
        followSpeed = Cloudy.config.getDouble("Steam.CloudCushion.FollowSpeed");

        if (!instant) {
            if (Cloud.getCloudsAroundPoint(player.getEyeLocation(), sourceRadius).isEmpty()) return;

            if (Cloud.getCloudsAroundPoint(player.getEyeLocation(), sourceRadius).stream()
                    .allMatch(c -> !FollowingSteams.isCloudInFollowingCouples(c) && !Objects.equals(c.getOwner(), player)))
                return;

            //Why not :/
            try {
                cloud = Cloud.getCloudsAroundPoint(player.getEyeLocation(), sourceRadius).getLast();
            } catch (Exception e) {
                return;
            }

            if (cloud == null)
                return;

            cloud.setOwner(this.player);
        }
        else launch();

        start();
    }

    @Override
    public void progress() {
        if (!isLaunched) {
            cloud.move(GeneralMethods.getDirection(cloud.getLocation(), player.getEyeLocation()).normalize().multiply(followSpeed));
            if (player.getEyeLocation().distance(cloud.getLocation()) < 1.5) isReadyToLaunch = true;

            if (!player.isSneaking() || !bPlayer.getBoundAbilityName().equals(getName()) || cloud.isCancelled()) remove();
        }
         if (!isCushion && isLaunched) {
             Sounds.playSound(location, Sound.ENTITY_PHANTOM_FLAP, 0.1f, 0.75f);
            location = location.add(direction);
            for (int i = 0; i < 6; i++) {
                Vector rv = Methods.getRandom();
                Particles.spawnParticle(Particle.CLOUD, location.clone().add(rv), 0, direction.getX(), direction.getY(), direction.getZ(), 0.2);
            }
            if (origin.distance(location) >= range) {
                Particles.spawnParticle(Particle.CLOUD, location, 50, 0, 0, 0, 0.5);
                GeneralMethods.getEntitiesAroundPoint(location, 3)
                        .forEach(e -> e.setVelocity(GeneralMethods.getDirection(location, e.getLocation()).normalize().multiply(knockback)));
                remove();
            }
            //Yea I love RayTraceResult, Optional and Lambdas :3
            RayTraceResult result = Objects.requireNonNull(location.getWorld()).rayTraceBlocks(location, direction, speed, FluidCollisionMode.NEVER, true);
            //idk why, but I love :: operator
            Optional.ofNullable(result)
                    .map(RayTraceResult::getHitBlock)
                    .ifPresent(b -> cushion(b.getLocation().add(0, 1, 0)));
        }
        if (isCushion) {
            Sounds.playSound(center, Sound.ENTITY_PHANTOM_FLAP, 0.1f, 0.75f);
            if (System.currentTimeMillis() > startCushionTiming + duration) remove();
            for (int i = 0; i < 10; i++) {
                Vector rv = Methods.getRandom().setY(0).normalize().multiply(ThreadLocalRandom.current().nextDouble(0, radius));
                Particles.spawnParticle(Particle.CLOUD, center.clone().add(rv), 0, rv.getX(), 0.3, rv.getZ(), 0.1);
            }

            GeneralMethods.getEntitiesAroundPoint(center, radius).forEach(e -> e.setFallDistance(.0f));
        }
    }

    public void cushion(Location location) {
        isCushion = true;

        if (GeneralMethods.getMCVersion() >= 1206) {
            Sounds.playSound(location, Sound.valueOf("ENTITY_WIND_CHARGE_WIND_BURST"), 1f, 0f);
        }
        center = location;
        Particles.spawnParticle(Particle.CLOUD, center, 30, 0, 0, 0, 0.5);
        startCushionTiming = System.currentTimeMillis();
    }

    public void launch() {
        bPlayer.addCooldown(this);
        origin = player.getEyeLocation();
        location = origin.clone();
        direction = player.getLocation().getDirection().multiply(speed);
        if (cloud != null) cloud.remove(true);
        isLaunched = true;
        isReadyToLaunch = false;
    }

    @Override
    public boolean isSneakAbility() {
        return false;
    }

    @Override
    public boolean isHarmlessAbility() {
        return false;
    }

    public boolean isLaunched() {
        return isLaunched;
    }

    public boolean isCushion() {
        return isCushion;
    }

    public boolean isReadyToLaunch() {
        return isReadyToLaunch;
    }

    @Override
    public long getCooldown() {
        return cooldown;
    }

    @Override
    public String getName() {
        return "CloudCushion";
    }

    @Override
    public Location getLocation() {
        return null;
    }


    @Override
    public void load() {

    }

    @Override
    public void stop() {

    }

    @Override
    public String getAuthor() {
        return Cloudy.getAuthor(this);
    }

    @Override
    public String getVersion() {
        return Cloudy.getVersion(this);
    }

    @Override
    public boolean isEnabled() {
        return Cloudy.config.getBoolean("Steam.CloudCushion.Enabled");
    }

    @Override
    public String getDescription() {
        return Cloudy.config.getString("Steam.CloudCushion.Description");
    }

    @Override
    public String getInstructions() {
        return Cloudy.config.getString("Steam.CloudCushion.Instructions");
    }

    public double getSpeed() {
        return speed;
    }

    public void setSourceRadius(double sourceRadius) {
        this.sourceRadius = sourceRadius;
    }

    public double getRadius() {
        return radius;
    }

    public double getSourceRadius() {
        return sourceRadius;
    }

    public double getRange() {
        return range;
    }

    public long getDuration() {
        return duration;
    }

    public Vector getDirection() {
        return direction;
    }

    public double getKnockback() {
        return knockback;
    }

    public double getFollowSpeed() {
        return followSpeed;
    }

    public long getStartCushionTiming() {
        return startCushionTiming;
    }

    public Cloud getCloud() {
        return cloud;
    }

    public Location getCenter() {
        return center;
    }

    public Location getOrigin() {
        return origin;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public void setRange(double range) {
        this.range = range;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setKnockback(double knockback) {
        this.knockback = knockback;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public void setFollowSpeed(double followSpeed) {
        this.followSpeed = followSpeed;
    }

    public void setDirection(Vector direction) {
        this.direction = direction;
    }

    public void setCenter(Location center) {
        this.center = center;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setCushion(boolean cushion) {
        isCushion = cushion;
    }

    public void setLaunched(boolean launched) {
        isLaunched = launched;
    }

    public void setReadyToLaunch(boolean readyToLaunch) {
        isReadyToLaunch = readyToLaunch;
    }
}
