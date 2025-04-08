package me.kaketuz.cloudy.abilities.steam.combos;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager;
import com.projectkorra.projectkorra.ability.util.ComboUtil;
import com.projectkorra.projectkorra.util.ColoredParticle;
import com.projectkorra.projectkorra.util.DamageHandler;
import me.kaketuz.cloudy.Cloudy;
import me.kaketuz.cloudy.abilities.steam.util.Cloud;
import me.kaketuz.cloudy.abilities.sub.SteamAbility;
import me.kaketuz.cloudy.util.Methods;
import me.kaketuz.cloudy.util.Particles;
import me.kaketuz.cloudy.util.Sounds;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class GloomyHails extends SteamAbility implements AddonAbility, ComboAbility {

    private int maxClouds;
    private double speed, radius, damage, sourceRadius, collisionRadius, hailSpeed, spawnHailChance;
    private long cooldown, duration;

    private final List<Location> locations = new CopyOnWriteArrayList<>();

    private Vector direction;


    public GloomyHails(Player player) {
        super(player);
        if (!bPlayer.canBendIgnoreBinds(this) || hasAbility(player, GloomyHails.class)) return;

        maxClouds = Cloudy.config.getInt("Steam.Combo.GloomyHails.MaxClouds");
        speed = Cloudy.config.getDouble("Steam.Combo.GloomyHails.Speed");
        hailSpeed = Cloudy.config.getDouble("Steam.Combo.GloomyHails.HailSpeed");
        radius = Cloudy.config.getDouble("Steam.Combo.GloomyHails.Radius");
        damage = Cloudy.config.getDouble("Steam.Combo.GloomyHails.Damage");
        sourceRadius = Cloudy.config.getDouble("Steam.Combo.GloomyHails.SourceRadius");
        collisionRadius = Cloudy.config.getDouble("Steam.Combo.GloomyHails.CollisionRadius");
        spawnHailChance = Cloudy.config.getDouble("Steam.Combo.GloomyHails.SpawnHailChance");
        cooldown = Cloudy.config.getLong("Steam.Combo.GloomyHails.Cooldown");
        duration = Cloudy.config.getLong("Steam.Combo.GloomyHails.Duration");

        int count = 0;

        if (Cloud.getCloudsAroundPoint(player.getEyeLocation(), sourceRadius).isEmpty()) return;

        for (Cloud c : Cloud.getCloudsAroundPoint(player.getEyeLocation(), sourceRadius)) {
            if (!FollowingSteams.isCloudInFollowingCouples(c) && !Objects.equals(c.getOwner(), player)) continue;
            locations.add(c.getLocation());
            c.remove(true);
            count++;
            if (count >= maxClouds) break;
        }

        locations.forEach(l -> Particles.spawnParticle(Particle.FLASH, l, 1));

        direction = player.getLocation().getDirection().setY(0).normalize().multiply(speed);

        Sounds.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1, 0);
        Sounds.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1, 2);
        start();
    }

    @Override
    public void progress() {

        locations.forEach(l -> {
            l.add(direction);
                Vector rv = Methods.getRandom().multiply(radius).setY(0).normalize();
                Particles.spawnParticle(GeneralMethods.getMCVersion() >= 1205 ? Particle.valueOf("DUST") : Particle.REDSTONE, l.clone().add(rv), 10, radius / 2, 0.3, radius / 2, 0, new Particle.DustOptions(Color.fromRGB(175, 175, 175), 2.5f));
            Sounds.playSound(l, Sound.ENTITY_PHANTOM_FLAP, 0.1f, 0.75f);

            RayTraceResult result = Objects.requireNonNull(l.getWorld()).rayTraceBlocks(l, direction, speed, FluidCollisionMode.NEVER, true);
            Optional.ofNullable(result)
                    .map(RayTraceResult::getHitBlock)
                    .filter(GeneralMethods::isSolid)
                    .ifPresent(b -> locations.remove(l));

            if (ThreadLocalRandom.current().nextDouble() < spawnHailChance) new Hail();
        });

        if (System.currentTimeMillis() > getStartTime() + duration) {
            bPlayer.addCooldown(this);
            remove();
        }

    }

    @Override
    public boolean isSneakAbility() {
        return false;
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
        return "GloomyHails";
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
    public Object createNewComboInstance(Player player) {
        return new GloomyHails(player);
    }

    @Override
    public ArrayList<ComboManager.AbilityInformation> getCombination() {
        return ComboUtil.generateCombinationFromList(this, Cloudy.config.getStringList("Steam.Combo.GloomyHails.Combination"));
    }

    @Override
    public boolean isEnabled() {
        return Cloudy.config.getBoolean("Steam.Combo.GloomyHails.Enabled");
    }


    private class Hail extends BukkitRunnable {

        private Location location;
        private final Location origin;
        private final Vector direction;

        public Hail() {
            origin = locations.get(ThreadLocalRandom.current().nextInt(0, locations.size()))
                    .clone()
                    .add(Methods.getRandom()
                            .multiply(ThreadLocalRandom.current().nextDouble(0, radius)));
            location = origin.clone();
            direction = new Vector(0, -1, 0).multiply(hailSpeed);
            this.runTaskTimer(Cloudy.plugin, 1L, 0);
        }


        @Override
        public void run() {
            if (origin.distance(location) >= 100) cancel();

            location = location.add(direction);

            RayTraceResult result = Objects.requireNonNull(location.getWorld()).rayTraceBlocks(location, direction, hailSpeed, FluidCollisionMode.ALWAYS, true);

            Optional.ofNullable(result)
                    .map(RayTraceResult::getHitBlock)
                    .filter(GeneralMethods::isSolid)
                    .ifPresent(b -> cancel());

            Particles.spawnParticle(GeneralMethods.getMCVersion() >= 1205 ? Particle.valueOf("BLOCK") : Particle.BLOCK_CRACK, location, 1, 0, 0, 0, 0, Material.ICE.createBlockData());
            new ColoredParticle(Color.fromRGB(140, 180, 198), 1).display(location, 1, 0, 0, 0);

            GeneralMethods.getEntitiesAroundPoint(location, collisionRadius).stream()
                    .filter(e -> e instanceof LivingEntity && !e.getUniqueId().equals(player.getUniqueId()))
                    .forEach(e -> {
                        DamageHandler.damageEntity(e, player, damage, GloomyHails.this);
                        e.setFreezeTicks(10);
                        cancel();
                    });

        }

        @Override
        public synchronized void cancel() throws IllegalStateException {
            super.cancel();
            Sounds.playSound(location, Sound.BLOCK_GLASS_BREAK, 0.5f, 1.4f);
        }
    }
    @Override
    public String getDescription() {
        return Cloudy.config.getString("Steam.Combo.GloomyHails.Description");
    }

    @Override
    public String getInstructions() {
        return Cloudy.config.getString("Steam.Combo.GloomyHails.Instructions");
    }

    public int getMaxClouds() {
        return maxClouds;
    }

    public double getSourceRadius() {
        return sourceRadius;
    }

    public double getSpeed() {
        return speed;
    }

    public long getDuration() {
        return duration;
    }

    public double getRadius() {
        return radius;
    }

    public Vector getDirection() {
        return direction;
    }

    public double getSpawnHailChance() {
        return spawnHailChance;
    }

    public double getDamage() {
        return damage;
    }

    @Override
    public double getCollisionRadius() {
        return collisionRadius;
    }

    public double getHailSpeed() {
        return hailSpeed;
    }

    public void setMaxClouds(int maxClouds) {
        this.maxClouds = maxClouds;
    }

    public void setSourceRadius(double sourceRadius) {
        this.sourceRadius = sourceRadius;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    public void setCollisionRadius(double collisionRadius) {
        this.collisionRadius = collisionRadius;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setHailSpeed(double hailSpeed) {
        this.hailSpeed = hailSpeed;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public void setDirection(Vector direction) {
        this.direction = direction;
    }

    public void setSpawnHailChance(double spawnHailChance) {
        this.spawnHailChance = spawnHailChance;
    }

}
