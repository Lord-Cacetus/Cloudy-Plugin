package me.kaketuz.cloudy.abilities.steam;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.util.DamageHandler;
import me.kaketuz.cloudy.Cloudy;
import me.kaketuz.cloudy.abilities.sub.SteamAbility;
import me.kaketuz.cloudy.util.Methods;
import me.kaketuz.cloudy.util.Particles;
import me.kaketuz.cloudy.util.Sounds;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
//🚬BOOOOOOOM
public class VaporBomb extends SteamAbility implements AddonAbility {

    private double speed, gravityFactor, gravityMultiplier, damage, knockback, sourceRange;
    private long cooldown, mistDuration, blindnessDuration;
    private int mistSegmentsAmount;

    private boolean isLaunched, isExploded, canHidePlayer;

    private Location origin, location;
    private Vector direction;


    public VaporBomb(Player player, boolean instant) {
        super(player);
        if (!bPlayer.canBendIgnoreBinds(this)) return;

        if (hasAbility(player, VaporBomb.class)) {
            if (!getAbility(player, VaporBomb.class).isLaunched) {
                getAbility(player, VaporBomb.class).remove();
            }
        }

        speed = Cloudy.config.getDouble("Steam.VaporBomb.Speed");
        gravityFactor = Cloudy.config.getDouble("Steam.VaporBomb.GravityFactor");
        gravityMultiplier = Cloudy.config.getDouble("Steam.VaporBomb.GravityMultiplier");
        damage = Cloudy.config.getDouble("Steam.VaporBomb.Damage");
        knockback = Cloudy.config.getDouble("Steam.VaporBomb.Knockback");
        sourceRange = Cloudy.config.getDouble("Steam.VaporBomb.SourceRange");
        cooldown = Cloudy.config.getLong("Steam.VaporBomb.Cooldown");
        mistDuration = Cloudy.config.getLong("Steam.VaporBomb.MistDuration");
        blindnessDuration = Cloudy.config.getLong("Steam.VaporBomb.BlindnessDuration");
        mistSegmentsAmount = Cloudy.config.getInt("Steam.VaporBomb.MistSegmentsAmount");
        canHidePlayer = Cloudy.config.getBoolean("Steam.VaporBomb.CanPlayerHide");


        if (!instant) {

            Block b = Methods.getLookingAt(player, sourceRange, false);

            if (b == null) return;

            if (!isWater(b) && !isIce(b)) return;

            origin = b.getLocation();
            location = origin.clone();
            Sounds.playSound(origin, Sound.ENTITY_SQUID_AMBIENT, 1, 1.4f);
            Sounds.playSound(origin, Sound.BLOCK_BUBBLE_COLUMN_UPWARDS_AMBIENT, 0.25f, 1.5f);
        }
        else {
            origin = player.getEyeLocation();
            location = origin.clone();
            launch();
        }




        start();
    }

    @Override
    public void progress() {
        if (!isLaunched) {
            if (!bPlayer.getBoundAbilityName().equals(getName()) || !player.isOnline() || player.isDead() || !bPlayer.canBendIgnoreBinds(this)) {
                bPlayer.addCooldown(this);
                remove();
            }
            Particles.spawnParticle(GeneralMethods.getMCVersion() >= 1205 ? Particle.valueOf("SPLASH") : Particle.WATER_SPLASH, origin.clone().add(0, 1, 0), 3, 0, 0.2, 0);
            Particles.spawnParticle(Particle.BUBBLE_POP, origin.clone().add(0, 1, 0), 3, 0.5, 0, 0.5, 0);
            Particles.spawnParticle(GeneralMethods.getMCVersion() >= 1205 ? Particle.valueOf("BUBBLE") : Particle.WATER_BUBBLE, origin.clone().add(0, 1, 0), 3, 0.5, 0, 0.5, 0.1);
        }
        else {
            Sounds.playSound(location, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.15f, 0.75f);
            location = location.add(direction);
            direction.subtract(new Vector(0, gravityFactor, 0));
            gravityFactor += gravityMultiplier;

            Particles.spawnParticle(Particle.CLOUD, location, 0, direction.getX(), direction.getY(), direction.getZ(), 0.5);

            RayTraceResult result = Objects.requireNonNull(location.getWorld()).rayTrace(location,
                    direction, speed,
                    FluidCollisionMode.ALWAYS,
                    true, 1,
                    e -> e instanceof LivingEntity && !e.getUniqueId().equals(player.getUniqueId()));

            Optional.ofNullable(result)
                    .map(RayTraceResult::getHitBlock)
                    .ifPresent(b -> explode());
            Optional.ofNullable(result)
                    .map(RayTraceResult::getHitEntity)
                    .ifPresent(e -> explode());
        }
    }

    public void explode() {
        isExploded = true;
        if (GeneralMethods.getMCVersion() >= 1206) {
            Sounds.playSound(location, Sound.valueOf("ENTITY_WIND_CHARGE_WIND_BURST"), 1f, 0f);
        }
        GeneralMethods.getEntitiesAroundPoint(location, 1,
                e -> e instanceof LivingEntity && !e.getUniqueId().equals(player.getUniqueId()))
                .forEach(e -> {
                    DamageHandler.damageEntity(e, player, damage, this);
                    e.setVelocity(GeneralMethods.getDirection(e.getLocation(), location).normalize().multiply(knockback));
        });
        for (int i = 0; i < mistSegmentsAmount; i++) {
            new Misty(location);
        }
        bPlayer.addCooldown(this);
        remove();
    }

    public boolean isLaunched() {
        return isLaunched;
    }

    public void launch() {
        direction = player.getLocation().getDirection().multiply(speed);
        isLaunched = true;
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
        return "VaporBomb";
    }

    @Override
    public Location getLocation() {
        return location;
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
        return Cloudy.config.getBoolean("Steam.VaporBomb.Enabled");
    }


    private class Misty extends BukkitRunnable {

        private Location location;
        private final Vector direction;
        private double speed;
        private final long start;

        private final List<Location> locations = new ArrayList<>();

        public Misty(Location location) {
            this.location = location.clone();
            this.speed = VaporBomb.this.speed / 2;
            this.direction = Methods.getRandom().multiply(speed);
            this.start = System.currentTimeMillis();
            runTaskTimer(Cloudy.plugin, 1L, 0);
        }
        @Override
        public void run() {
            Sounds.playSound(location, Sound.ENTITY_PHANTOM_FLAP, 0.1f, 0.75f);
            location = location.add(direction);
            direction.subtract(new Vector(0, VaporBomb.this.gravityFactor / 2, 0));
            direction.normalize().multiply(speed);

            if (System.currentTimeMillis() > start + mistDuration) cancel();

            for (Location location1 : locations) {
                   if (ThreadLocalRandom.current().nextDouble() < 0.3) {
                       Vector rv = Methods.getRandom().multiply(ThreadLocalRandom.current().nextDouble(0, 5));
                       Particles.spawnParticle(Particle.CLOUD, location1.clone().add(rv), 0, direction.getX(), direction.getY(), direction.getZ(), 0.1);
                   }
                GeneralMethods.getEntitiesAroundPoint(location1, 1, e -> e instanceof LivingEntity && !e.getUniqueId().equals(player.getUniqueId()))
                        .forEach(e -> {
                            ((LivingEntity)e).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, (int) blindnessDuration, 0, true, false, false));
                        });
                if (canHidePlayer) {
                    if (player.getLocation().distance(location1) < 2) {
                        player.hidePlayer(Cloudy.plugin, player);
                    }
                    else player.showPlayer(Cloudy.plugin, player);
                }
            }



            if (speed > 0) {
                locations.add(location);
                speed -= 0.1;
            }
            else speed = 0;
        }

        @Override
        public synchronized void cancel() throws IllegalStateException {
            super.cancel();

        }
    }

    @Override
    public String getDescription() {
        return Cloudy.config.getString("Steam.VaporBomb.Description");
    }

    @Override
    public String getInstructions() {
        return Cloudy.config.getString("Steam.VaporBomb.Instructions");
    }

    public double getSpeed() {
        return speed;
    }

    public double getKnockback() {
        return knockback;
    }

    public double getDamage() {
        return damage;
    }

    public double getSourceRange() {
        return sourceRange;
    }

    public double getGravityFactor() {
        return gravityFactor;
    }

    public double getGravityMultiplier() {
        return gravityMultiplier;
    }

    public Location getOrigin() {
        return origin;
    }

    public long getBlindnessDuration() {
        return blindnessDuration;
    }

    public int getMistSegmentsAmount() {
        return mistSegmentsAmount;
    }

    public long getMistDuration() {
        return mistDuration;
    }

    public Vector getDirection() {
        return direction;
    }

    public boolean isExploded() {
        return isExploded;
    }

    public void setKnockback(double knockback) {
        this.knockback = knockback;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    public void setSourceRange(double sourceRange) {
        this.sourceRange = sourceRange;
    }

    public void setGravityFactor(double gravityFactor) {
        this.gravityFactor = gravityFactor;
    }

    public void setOrigin(Location origin) {
        this.origin = origin;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setLaunched(boolean launched) {
        isLaunched = launched;
    }

    public void setDirection(Vector direction) {
        this.direction = direction;
    }

    public void setBlindnessDuration(long blindnessDuration) {
        this.blindnessDuration = blindnessDuration;
    }

    public void setCanHidePlayer(boolean canHidePlayer) {
        this.canHidePlayer = canHidePlayer;
    }

    public void setGravityMultiplier(double gravityMultiplier) {
        this.gravityMultiplier = gravityMultiplier;
    }

    public void setMistDuration(long mistDuration) {
        this.mistDuration = mistDuration;
    }

    public void setExploded(boolean exploded) {
        isExploded = exploded;
    }

    public void setMistSegmentsAmount(int mistSegmentsAmount) {
        this.mistSegmentsAmount = mistSegmentsAmount;
    }

}
