package me.kaketuz.cloudy.abilities.steam;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.waterbending.passive.FastSwim;
import me.kaketuz.cloudy.Cloudy;
import me.kaketuz.cloudy.Configuration;
import me.kaketuz.cloudy.abilities.steam.combos.FollowingSteams;
import me.kaketuz.cloudy.abilities.steam.util.Cloud;
import me.kaketuz.cloudy.abilities.steam.util.SteamFlow;
import me.kaketuz.cloudy.abilities.sub.SteamAbility;
import me.kaketuz.cloudy.util.Methods;
import me.kaketuz.cloudy.util.Particles;
import me.kaketuz.cloudy.util.Sounds;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

//AWAWAWAWAWWWAWAWAW I LOVE U ALL, I LOVE PROJECTKORRA I LOVE HIRO, STRANGEONEEE, SIMPLICITEEEEE, COZMYC, DREIGMICHIHIHIHIHI, KOTEWWUWUWUW  dPOWIHRVUFEPOIHROIBWP!OBER"HTIB{INSR
//02.04.25 0am: there was an attraction of mature and love
public class FumeAbsorption extends SteamAbility implements AddonAbility {

    private int maxClouds, streamsMultiplier;

    private double minRange, speed, knockback, damage, sourceRange, followSpeed;

    private long cooldown;

    private boolean randomDirections;

    private final List<Cloud> clouds = new CopyOnWriteArrayList<>();
    private final List<Cloud> ready = new CopyOnWriteArrayList<>();

    private boolean isInstant;

    private boolean coldBiomesBuff, nightBuff;
    private double buffFactor;

    public FumeAbsorption(Player player, boolean instant) {
        super(player);
        if (!bPlayer.canBendIgnoreBinds(this) || hasAbility(player, FumeAbsorption.class)) return;




        maxClouds = Cloudy.config.getInt("Steam.FumeAbsorption.MaxClouds");
        streamsMultiplier = Cloudy.config.getInt("Steam.FumeAbsorption.StreamsMultiplier");
        minRange = Cloudy.config.getDouble("Steam.FumeAbsorption.MinRange");
        speed = Cloudy.config.getDouble("Steam.FumeAbsorption.Speed");
        knockback = Cloudy.config.getDouble("Steam.FumeAbsorption.Knockback");
        damage = Cloudy.config.getDouble("Steam.FumeAbsorption.Damage");
        followSpeed = Cloudy.config.getDouble("Steam.FumeAbsorption.FollowSpeed");
        sourceRange = Cloudy.config.getDouble("Steam.FumeAbsorption.SourceRadius");
        cooldown = Cloudy.config.getLong("Steam.FumeAbsorption.Cooldown");
        randomDirections = Cloudy.config.getBoolean("Steam.FumeAbsorption.RandomDirections");
        coldBiomesBuff = Cloudy.config.getBoolean("Steam.FumeAbsorption.ColdBiomesBuff");
        nightBuff = Cloudy.config.getBoolean("Steam.FumeAbsorption.NightBuff");
        buffFactor = Cloudy.config.getDouble("Steam.FumeAbsorption.BuffFactor");

        this.isInstant = instant;

        if (coldBiomesBuff && Methods.getTemperature(player.getLocation()) <= 0) {
            maxClouds *= (int) buffFactor;
            streamsMultiplier *= (int) buffFactor;
            minRange *= buffFactor;
            speed *= buffFactor;
            sourceRange *= buffFactor;
            damage *= (int) buffFactor;
            followSpeed *= (long) buffFactor;
        }
        if (nightBuff && isNight(player.getWorld())) {
            maxClouds *= (int) buffFactor;
            streamsMultiplier *= (int) buffFactor;
            minRange *= buffFactor;
            speed *= buffFactor;
            sourceRange *= buffFactor;
            damage *= (int) buffFactor;
            followSpeed *= (long) buffFactor;
        }

        if (!instant) {

            int count = 0;

            if (Cloud.getCloudsAroundPoint(player.getEyeLocation(), sourceRange).isEmpty()) return;

            for (Cloud c : Cloud.getCloudsAroundPoint(player.getEyeLocation(), sourceRange)) {
                if (c.isUsing() || c.isHidden()) continue;
                clouds.add(c);
                c.setOwner(player);
                count++;
                if (count >= maxClouds) break;
            }


            if (hasAbility(player, FastSwim.class)) getAbility(player, FastSwim.class).remove();

            clouds.forEach(c -> {
                c.setOwner(player);
                c.setUse(true);
            });
        }
        else {
            if (!hasAbility(player, FollowingSteams.class)) return;

            ready.addAll(getAbility(player, FollowingSteams.class).getUsableClouds());
            getAbility(player, FollowingSteams.class).removeAllClouds();
        }

        start();
    }

    @Override
    public void progress() {
        if (player.isDead() || !player.isOnline() || !bPlayer.canBendIgnoreBinds(this)) {
            bPlayer.addCooldown(this);
            remove();
        }

        if (isInstant) {
            bPlayer.addCooldown(this);
            remove();
        }


        clouds.forEach(c -> {
            c.move(GeneralMethods.getDirection(c.getLocation(), player.getEyeLocation()).normalize().multiply(followSpeed));
            if (c.getLocation().distance(player.getEyeLocation()) < 1.5 && !ready.contains(c)) ready.add(c);

        });

        clouds.removeIf(Cloud::isCancelled);
        ready.removeIf(Cloud::isCancelled);



        if (!player.isSneaking() || !bPlayer.getBoundAbilityName().equals(getName())) {
            bPlayer.addCooldown(this);
            remove();
        }
    }

    @Override
    public void remove() {
        if (GeneralMethods.getMCVersion() >= 1204) {
            Sounds.playSound(player.getLocation(), Sound.ENTITY_BREEZE_IDLE_AIR, 0.6f, 0.5f);
            if (GeneralMethods.getMCVersion() >= 1205) {
                Sounds.playSound(player.getLocation(), Sound.ENTITY_BREEZE_WHIRL, 2, 0.5f);
                Sounds.playSound(player.getLocation(), Sound.ENTITY_BREEZE_WIND_BURST, 0.5f, 0);
            }
        }
        if (randomDirections) {
            for (int i = 0; i < ready.size() * streamsMultiplier; i++) {
                new SteamFlow(player.getEyeLocation(), null, 2, 2000, false, 0);
                new Stream(player.getLocation().add(0, 1, 0), minRange + ready.size() * streamsMultiplier, speed, damage, knockback, null);
            }
        }
        else {
            for (int i = 0; i < ready.size() * streamsMultiplier; i++) {
                double angle = i * (2 * Math.PI / (ready.size() * streamsMultiplier));
                double x = 0.1 * Math.cos(angle);
                double z = 0.1 * Math.sin(angle);
                Location loc = player.getLocation().clone();
                loc.add(x, 1, z);
                new SteamFlow(loc, null, 2, 2000, false, 0);
                new Stream(loc, minRange + ready.size() * streamsMultiplier, speed, damage, knockback, new Vector(x, 0, z).multiply(speed));
            }
        }
        clouds.forEach(c -> {
            c.remove(true);
            c.setUse(false);
        });
        super.remove();
    }

    @Override
    public boolean isSneakAbility() {
        return true;
    }

    @Override
    public boolean isHarmlessAbility() {
        return false;
    }

    @Override
    public long getCooldown() {
        return cooldown;
    }

    @Override
    public String getName() {
        return "FumeAbsorption";
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

    private class Stream extends BukkitRunnable {

        private final Location origin;
        private Location location;

        private final Vector direction;

        private final double range, speed, damage, knockback;

        private final Set<Entity> damaged = new HashSet<>();

        public Stream(Location location, double range, double speed, double damage, double knockback, Vector direction) {
            this.origin = location.clone();
            this.location = origin.clone();
            this.range = range;
            this.speed = speed;
            this.damage = damage;
            this.knockback = knockback;
            this.direction = direction == null ? Methods.getRandom().multiply(speed).setY(0).normalize() : direction;
            this.direction.normalize().multiply(speed);
            this.runTaskTimer(Cloudy.plugin, 1L, 0);
        }

        @Override
        public void run() {
            Sounds.playSound(location, Sound.ENTITY_PHANTOM_FLAP, 0.1f, 0.75f);
            location = location.add(direction);
            if (origin.distance(location) >= range) cancel();
            GeneralMethods.getEntitiesAroundPoint(location, 1).stream()
                    .filter(e -> e instanceof LivingEntity && !e.getUniqueId().equals(player.getUniqueId()))
                    .forEach(e -> {
                        if (!damaged.contains(e)) DamageHandler.damageEntity(e, player, damage, getAbility(FumeAbsorption.class));
                        e.setVelocity(direction.clone().multiply(knockback));
                        damaged.add(e);
                    });

            for (int i = 0; i < 3; i++) {
                Vector rv = Methods.getRandom().setY(0).normalize();
                Particles.spawnParticle(Particle.CLOUD, location.clone().add(rv), 0, rv.getX(), 0.1, rv.getZ(), 0.05);
            }


            RayTraceResult result = Objects.requireNonNull(location.getWorld()).rayTraceBlocks(location, direction, speed, FluidCollisionMode.NEVER, true);

            Optional.ofNullable(result)
                    .map(RayTraceResult::getHitBlock)
                    .ifPresent(b -> {
                        if (GeneralMethods.isSolid(b)) cancel();
                        if (isWater(b)) {
                            Particles.spawnParticle(GeneralMethods.getMCVersion() >= 1205 ? Particle.valueOf("WATER_BUBBLE") : Particle.BUBBLE, location, 20, 0.2, 0.2, 0.2, 0.08);
                        }
                    });
        }
    }

    @Override
    public boolean isEnabled() {
        return Cloudy.config.getBoolean("Steam.FumeAbsorption.Enabled");
    }

    @Override
    public String getDescription() {
        return Cloudy.config.getString("Steam.FumeAbsorption.Description");
    }
    @Override
    public String getInstructions() {
        return Cloudy.config.getString("Steam.FumeAbsorption.Instructions");
    }

    public double getSourceRange() {
        return sourceRange;
    }

    public double getSpeed() {
        return speed;
    }

    public int getMaxClouds() {
        return maxClouds;
    }

    public double getMinRange() {
        return minRange;
    }

    public int getStreamsMultiplier() {
        return streamsMultiplier;
    }

    public double getKnockback() {
        return knockback;
    }

    public double getFollowSpeed() {
        return followSpeed;
    }

    public double getDamage() {
        return damage;
    }

    public List<Cloud> getClouds() {
        return clouds;
    }

    public List<Cloud> getReady() {
        return ready;
    }

    public boolean isInstant() {
        return isInstant;
    }

    public void setSourceRange(double sourceRange) {
        this.sourceRange = sourceRange;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public void setMaxClouds(int maxClouds) {
        this.maxClouds = maxClouds;
    }

    public void setFollowSpeed(double followSpeed) {
        this.followSpeed = followSpeed;
    }

    public void setMinRange(double minRange) {
        this.minRange = minRange;
    }

    public void setStreamsMultiplier(int streamsMultiplier) {
        this.streamsMultiplier = streamsMultiplier;
    }

    public void setKnockback(double knockback) {
        this.knockback = knockback;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    public void setInstant(boolean instant) {
        isInstant = instant;
    }

    public boolean isRandomDirections() {
        return randomDirections;
    }

    public void setRandomDirections(boolean randomDirections) {
        this.randomDirections = randomDirections;
    }

    public double getBuffFactor() {
        return buffFactor;
    }

    public void setNightBuff(boolean nightBuff) {
        this.nightBuff = nightBuff;
    }

    public void setColdBiomesBuff(boolean coldBiomesBuff) {
        this.coldBiomesBuff = coldBiomesBuff;
    }

    public void setBuffFactor(double buffFactor) {
        this.buffFactor = buffFactor;
    }

    public boolean isNightBuff() {
        return nightBuff;
    }
}
