package me.kaketuz.cloudy.abilities.steam;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import me.kaketuz.cloudy.Cloudy;
import me.kaketuz.cloudy.abilities.steam.combos.FollowingSteams;
import me.kaketuz.cloudy.abilities.steam.util.Cloud;
import me.kaketuz.cloudy.abilities.sub.SteamAbility;
import me.kaketuz.cloudy.util.Methods;
import me.kaketuz.cloudy.util.Particles;
import me.kaketuz.cloudy.util.Sounds;
import org.bukkit.*;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.type.Farmland;
import org.bukkit.block.data.type.Sapling;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;


import java.util.ArrayList;
import java.util.List;
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

    private int speedBoost, jumpBoost;
    private boolean affectOtherSteambenders;

    private boolean canGrowPlants;
    private int moistureFactor;
    private double growChance;

    private boolean coldBiomesBuff, nightBuff;
    private double buffFactor;

    private long startLaunchingTiming;



    public CloudCushion(Player player, boolean instant) {
        super(player);
        if (!bPlayer.canBendIgnoreBinds(this)) return;

        for (CloudCushion c : getAbilities(CloudCushion.class)) {
            if (!c.isRemoved() && c.cloud.getLocation().distance(player.getEyeLocation()) <= 5) {
                c.remove();
                this.cloud = c.cloud;
                break;
            }
        }

        range = Cloudy.config.getDouble("Steam.CloudCushion.Range");
        speed = Cloudy.config.getDouble("Steam.CloudCushion.Speed");
        radius = Cloudy.config.getDouble("Steam.CloudCushion.Radius");
        sourceRadius = Cloudy.config.getDouble("Steam.CloudCushion.SourceRadius");
        knockback = Cloudy.config.getDouble("Steam.CloudCushion.Knockback");
        cooldown = Cloudy.config.getLong("Steam.CloudCushion.Cooldown");
        duration = Cloudy.config.getLong("Steam.CloudCushion.Duration");
        followSpeed = Cloudy.config.getDouble("Steam.CloudCushion.FollowSpeed");
        speedBoost = Cloudy.config.getInt("Steam.CloudCushion.SpeedBoost");
        jumpBoost = Cloudy.config.getInt("Steam.CloudCushion.JumpBoost");
        affectOtherSteambenders = Cloudy.config.getBoolean("Steam.CloudCushion.AffectOtherSteambenders");
        canGrowPlants = Cloudy.config.getBoolean("Steam.CloudCushion.CanGrowPlants");
        moistureFactor = Cloudy.config.getInt("Steam.CloudCushion.MoistureFactor");
        growChance = Cloudy.config.getDouble("Steam.CloudCushion.GrowChance");
        coldBiomesBuff = Cloudy.config.getBoolean("Steam.CloudCushion.ColdBiomesBuff");
        nightBuff = Cloudy.config.getBoolean("Steam.CloudCushion.NightBuff");
        buffFactor = Cloudy.config.getDouble("Steam.CloudCushion.BuffFactor");

        if (coldBiomesBuff && Methods.getTemperature(player.getLocation()) <= 0) {
            sourceRadius *= buffFactor;
            knockback *= buffFactor;
            range *= buffFactor;
            speed *= buffFactor;
            duration *= (long) buffFactor;
            speedBoost *= (int) buffFactor;
            jumpBoost *= (int) buffFactor;
            followSpeed *= buffFactor;
            radius *= buffFactor;
        }
        if (nightBuff && isNight(player.getWorld())) {
            sourceRadius *= buffFactor;
            knockback *= buffFactor;
            range *= buffFactor;
            speed *= buffFactor;
            duration *= (long) buffFactor;
            speedBoost *= (int) buffFactor;
            jumpBoost *= (int) buffFactor;
            followSpeed *= buffFactor;
            radius *= buffFactor;
        }

        if (!instant) {

            if (cloud == null) {

                if (Cloud.getCloudsAroundPoint(player.getEyeLocation(), sourceRadius).isEmpty()) return;

                if (Cloud.getCloudsAroundPoint(player.getEyeLocation(), sourceRadius).stream()
                        .allMatch(c -> c.isUsing() || c.isHidden())) return;

                //Why not :/
                try {
                    cloud = Cloud.getCloudsAroundPoint(player.getEyeLocation(), sourceRadius).getLast();
                    cloud.setOwner(player);
                    cloud.setUse(true);
                } catch (Exception e) {
                    return;
                }

                if (cloud == null)
                    return;
            }

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
             if (cloud != null) cloud.teleportTo(location);
             Sounds.playSound(location, Sound.ENTITY_PHANTOM_FLAP, 0.1f, 0.75f);
            location = location.add(direction);
            for (int i = 0; i < 6; i++) {
                Vector rv = Methods.getRandom();
                Particles.spawnParticle(Particle.CLOUD, location.clone().add(rv), 0, direction.getX(), direction.getY(), direction.getZ(), 0.2);
            }
            if (origin.distance(location) >= range) {
                Particles.spawnParticle(Particle.CLOUD, location, 50, 0, 0, 0, 0.5);
                GeneralMethods.getEntitiesAroundPoint(location, radius)
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

            if (canGrowPlants) {
                GeneralMethods.getBlocksAroundPoint(center, radius).stream()
                        .filter(b -> b.getBlockData() instanceof Ageable || b.getType() == Material.FARMLAND)
                        .forEach(b -> {
                            if (b.getType() == Material.FARMLAND) {
                                Farmland f = (Farmland) b.getBlockData();
                                f.setMoisture(f.getMaximumMoisture());
                            }
                            else {
                                if (Methods.chance(growChance)) {
                                    Ageable a = (Ageable) b.getBlockData();
                                    a.setAge(Math.min(a.getAge() + moistureFactor, a.getMaximumAge()));
                                    b.setBlockData(a);
                                }
                            }
                        });
            }

            GeneralMethods.getEntitiesAroundPoint(center, radius).forEach(e -> {
                if (affectOtherSteambenders && e instanceof Player p &&
                        BendingPlayer.getBendingPlayer(p) != null &&
                        BendingPlayer.getBendingPlayer(p).canUseSubElement(STEAM)) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 10, jumpBoost - 1, true, false, false));
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10, speedBoost - 1, true, false, false));
                    e.setFallDistance(.0f);
                }
                else if (e.equals(player)) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 10, jumpBoost - 1, true, false, false));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10, speedBoost - 1, true, false, false));
                    player.setFallDistance(.0f);
                }

            });
        }
    }

    @Override
    public void remove() {
        super.remove();
        if (cloud != null)  cloud.setUse(false);
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
        if (isLaunched) return;
        startLaunchingTiming = System.currentTimeMillis();
        bPlayer.addCooldown(this);
        origin = player.getEyeLocation();
        location = origin.clone();
        direction = player.getLocation().getDirection().multiply(speed);
        isLaunched = true;
        isReadyToLaunch = false;
    }

    public void explode(boolean fission) {
        if (!fission) {
            Particles.spawnParticle(Particle.CLOUD, location, 50, 0, 0, 0, 0.5);
            GeneralMethods.getEntitiesAroundPoint(location, radius)
                    .forEach(e -> e.setVelocity(GeneralMethods.getDirection(location, e.getLocation()).normalize().multiply(knockback)));
        }
        else if (cloud != null) new CloudFission(player, List.of(cloud));

        remove();
    }

    @Override
    public boolean isSneakAbility() {
        return true;
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

    public int getSpeedBoost() {
        return speedBoost;
    }

    public int getJumpBoost() {
        return jumpBoost;
    }

    public void setSpeedBoost(int speedBoost) {
        this.speedBoost = speedBoost;
    }

    public void setJumpBoost(int jumpBoost) {
        this.jumpBoost = jumpBoost;
    }

    public int getMoistureFactor() {
        return moistureFactor;
    }

    public void setCanGrowPlants(boolean canGrowPlants) {
        this.canGrowPlants = canGrowPlants;
    }

    public boolean isCanGrowPlants() {
        return canGrowPlants;
    }

    public void setMoistureFactor(int moistureFactor) {
        this.moistureFactor = moistureFactor;
    }

    public long getStartLaunchingTiming() {
        return startLaunchingTiming;
    }

    public void setStartLaunchingTiming(long startLaunchingTiming) {
        this.startLaunchingTiming = startLaunchingTiming;
    }

    public void setOrigin(Location origin) {
        this.origin = origin;
    }

    public void setAffectOtherSteambenders(boolean affectOtherSteambenders) {
        this.affectOtherSteambenders = affectOtherSteambenders;
    }

    public void setStartCushionTiming(long startCushionTiming) {
        this.startCushionTiming = startCushionTiming;
    }

    public void setBuffFactor(double buffFactor) {
        this.buffFactor = buffFactor;
    }

    public void setColdBiomesBuff(boolean coldBiomesBuff) {
        this.coldBiomesBuff = coldBiomesBuff;
    }
    @Deprecated
    public void setCloud(Cloud cloud) {
        this.cloud = cloud;
    }

    public void setGrowChance(double growChance) {
        this.growChance = growChance;
    }

    public void setNightBuff(boolean nightBuff) {
        this.nightBuff = nightBuff;
    }

    public double getBuffFactor() {
        return buffFactor;
    }

    public double getGrowChance() {
        return growChance;
    }

    public boolean isAffectOtherSteambenders() {
        return affectOtherSteambenders;
    }

    public boolean isNightBuff() {
        return nightBuff;
    }

    public boolean isColdBiomesBuff() {
        return coldBiomesBuff;
    }

}
