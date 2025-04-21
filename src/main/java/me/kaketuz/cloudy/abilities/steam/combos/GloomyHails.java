package me.kaketuz.cloudy.abilities.steam.combos;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager;
import com.projectkorra.projectkorra.ability.util.ComboUtil;
import com.projectkorra.projectkorra.util.ColoredParticle;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.TempBlock;
import me.kaketuz.cloudy.Cloudy;
import me.kaketuz.cloudy.abilities.steam.util.Cloud;
import me.kaketuz.cloudy.abilities.sub.SteamAbility;
import me.kaketuz.cloudy.util.Methods;
import me.kaketuz.cloudy.util.Particles;
import me.kaketuz.cloudy.util.Sounds;
import org.bukkit.*;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.type.Farmland;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Vector3f;

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

    private boolean displayVar;
    private float hailHeight;

    private boolean canBreakPlants, forcedBreak;

    private boolean coldBiomesBuff, nightBuff;
    private double buffFactor;

    private boolean rain;
    private int plantsGrowChance, regenLevel, regenDuration;
    private boolean canRegen, canGrow;




    public GloomyHails(Player player, boolean rain) {
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
        displayVar = Cloudy.config.getBoolean("Steam.Combo.GloomyHails.DisplayVariation");
        hailHeight = (float) Cloudy.config.getDouble("Steam.Combo.GloomyHails.HailHeight");
        canBreakPlants = Cloudy.config.getBoolean("Steam.Combo.GloomyHails.CanBreakPlants");
        forcedBreak = Cloudy.config.getBoolean("Steam.Combo.GloomyHails.ForcedBreak");
        coldBiomesBuff = Cloudy.config.getBoolean("Steam.Combo.GloomyHails.ColdBiomesBuff");
        nightBuff = Cloudy.config.getBoolean("Steam.Combo.GloomyHails.NightBuff");
        buffFactor = Cloudy.config.getDouble("Steam.Combo.GloomyHails.BuffFactor");
        canGrow = Cloudy.config.getBoolean("Steam.Combo.GloomyHails.CanGrowPlants");
        canRegen = Cloudy.config.getBoolean("Steam.Combo.GloomyHails.CanRegen");
        plantsGrowChance = Cloudy.config.getInt("Steam.Combo.GloomyHails.PlantsGrowChance");
        regenDuration = Cloudy.config.getInt("Steam.Combo.GloomyHails.RegenDuration");
        regenLevel = Cloudy.config.getInt("Steam.Combo.GloomyHails.RegenLevel");

        int count = 0;

        this.rain = rain;

        if (coldBiomesBuff && Methods.getTemperature(player.getLocation()) <= 0) {
            duration *= (long) buffFactor;
            maxClouds *= (int) buffFactor;
            radius *=  buffFactor;
            hailSpeed *= buffFactor;
            sourceRadius *= buffFactor;
            spawnHailChance *= buffFactor;
            speed *= buffFactor;
        }
        if (nightBuff && isNight(player.getWorld())) {
            duration *= (long) buffFactor;
            maxClouds *= (int) buffFactor;
            radius *=  buffFactor;
            hailSpeed *= buffFactor;
            sourceRadius *= buffFactor;
            spawnHailChance *= buffFactor;
            speed *= buffFactor;
        }

        if (Cloud.getCloudsAroundPoint(player.getEyeLocation(), sourceRadius).isEmpty()) return;

        for (Cloud c : Cloud.getCloudsAroundPoint(player.getEyeLocation(), sourceRadius)) {
            if (c.isHidden() || c.isUsing()) continue;
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

            if (ThreadLocalRandom.current().nextDouble() < spawnHailChance) new Precipitation();
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
        return new GloomyHails(player, false);
    }

    @Override
    public ArrayList<ComboManager.AbilityInformation> getCombination() {
        return ComboUtil.generateCombinationFromList(this, Cloudy.config.getStringList("Steam.Combo.GloomyHails.Combination"));
    }

    @Override
    public boolean isEnabled() {
        return Cloudy.config.getBoolean("Steam.Combo.GloomyHails.Enabled");
    }


    private class Precipitation extends BukkitRunnable {

        private Location location;
        private Location origin;
        private Vector direction;
        private ItemDisplay icicle;

        public Precipitation() {
            if (locations.isEmpty()) return;
            origin = locations.get(ThreadLocalRandom.current().nextInt(0, locations.size()))
                    .clone()
                    .add(Methods.getRandom()
                            .multiply(ThreadLocalRandom.current().nextDouble(0, radius)));
            location = origin.clone();
            direction = new Vector(0, -1, 0).multiply(hailSpeed);

            if (displayVar && !rain) {
                icicle = (ItemDisplay) origin.getWorld().spawnEntity(origin.clone(), EntityType.ITEM_DISPLAY);
                icicle.setItemStack(new ItemStack(Material.ICE));
                Transformation t = icicle.getTransformation();
                t.getScale().set(new Vector3f(0.1f, hailHeight, 0.1f));
                icicle.setTransformation(t);
            }

            this.runTaskTimer(Cloudy.plugin, 1L, 0);
        }


        @Override
        public void run() {
            if (origin.distance(location) >= 100) cancel();

            location = location.add(direction);

            if (canBreakPlants && !rain) {
                GeneralMethods.getBlocksAroundPoint(location, 1).stream()
                        .filter(b -> b.getBlockData() instanceof Ageable)
                        .forEach(b -> {
                            if (forcedBreak) new TempBlock(b, Material.AIR);
                            else b.breakNaturally();
                        });
            }
            if (canGrow && rain) {
                GeneralMethods.getBlocksAroundPoint(location, 1).stream()
                        .filter(b -> b.getBlockData() instanceof Ageable)
                        .forEach(b -> {
                            if (Methods.chance(plantsGrowChance)) {
                                Ageable a = (Ageable) b.getBlockData();
                                a.setAge(Math.min(a.getAge() + 1, a.getMaximumAge()));
                                b.setBlockData(a);
                            }
                        });
            }

            if (rain) Sounds.playSound(location, Sound.WEATHER_RAIN_ABOVE, 0.05f, 1);


            RayTraceResult result = Objects.requireNonNull(location.getWorld()).rayTraceBlocks(location, direction, hailSpeed / 2, FluidCollisionMode.ALWAYS, true);

            Optional.ofNullable(result)
                    .map(RayTraceResult::getHitBlock)
                    .filter(GeneralMethods::isSolid)
                    .ifPresent(b -> cancel());

            if (rain) {
                Particles.spawnParticle(Particle.FALLING_WATER, location, 1, 0, 0, 0, 0);
            }

            if (!displayVar) {
                if (!rain) {
                    Particles.spawnParticle(GeneralMethods.getMCVersion() >= 1205 ? Particle.valueOf("BLOCK") : Particle.BLOCK_CRACK, location, 1, 0, 0, 0, 0, Material.ICE.createBlockData());
                    new ColoredParticle(Color.fromRGB(140, 180, 198), 1).display(location, 1, 0, 0, 0);
                }
            }
            else if (!rain){
                icicle.setTeleportDuration(1);
                icicle.teleport(location);
            }
            GeneralMethods.getEntitiesAroundPoint(location, collisionRadius).stream()
                    .filter(e -> e instanceof LivingEntity)
                    .forEach(e -> {
                        if (!rain) {
                            if (!e.getUniqueId().equals(player.getUniqueId())) {
                                DamageHandler.damageEntity(e, player, damage, GloomyHails.this);
                                e.setFreezeTicks(10);
                                cancel();
                            }
                        }
                        else if (canRegen) {
                            ((LivingEntity) e).addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, regenDuration, regenLevel - 1, true, false, false));
                        }
                    });

        }

        @Override
        public synchronized void cancel() throws IllegalStateException {
            super.cancel();
            if (!rain) Sounds.playSound(location, Sound.BLOCK_GLASS_BREAK, 0.5f, 1.4f);
            if (displayVar && !rain) {
                Particles.spawnParticle(GeneralMethods.getMCVersion() >= 1205 ? Particle.valueOf("BLOCK") : Particle.BLOCK_CRACK, location, 15, 0.2, 0.2, 0.2, 0, Objects.requireNonNull(icicle.getItemStack()).getType().createBlockData());
                icicle.remove();

            }
        }
    }
    @Override
    public String getDescription() {
        return Cloudy.config.getString("Steam.Combo.GloomyHails.Description") + "\n\n GloomyRains: " + Cloudy.config.getString("Steam.Combo.GloomyHails.Rain.Description");
    }

    @Override
    public String getInstructions() {
        return Cloudy.config.getString("Steam.Combo.GloomyHails.Instructions") + "\n\n GloomyRains: " + Cloudy.config.getString("Steam.Combo.GloomyHails.Rain.Instructions");
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

    public boolean isDisplayVar() {
        return displayVar;
    }

    public void setDisplayVar(boolean displayVar) {
        this.displayVar = displayVar;
    }

    public float getHailHeight() {
        return hailHeight;
    }

    public void setHailHeight(float hailHeight) {
        this.hailHeight = hailHeight;
    }

    public boolean isCanBreakPlants() {
        return canBreakPlants;
    }

    public void setCanBreakPlants(boolean canBreakPlants) {
        this.canBreakPlants = canBreakPlants;
    }

    public boolean isForcedBreak() {
        return forcedBreak;
    }

    public void setForcedBreak(boolean forcedBreak) {
        this.forcedBreak = forcedBreak;
    }

    public boolean isRain() {
        return rain;
    }

    public void setRain(boolean rain) {
        this.rain = rain;
    }

    public double getBuffFactor() {
        return buffFactor;
    }

    public void setNightBuff(boolean nightBuff) {
        this.nightBuff = nightBuff;
    }

    public int getPlantsGrowChance() {
        return plantsGrowChance;
    }

    public boolean isNightBuff() {
        return nightBuff;
    }

    public int getRegenDuration() {
        return regenDuration;
    }

    public int getRegenLevel() {
        return regenLevel;
    }

    public void setColdBiomesBuff(boolean coldBiomesBuff) {
        this.coldBiomesBuff = coldBiomesBuff;
    }

    public void setBuffFactor(double buffFactor) {
        this.buffFactor = buffFactor;
    }

    public void setCanGrow(boolean canGrow) {
        this.canGrow = canGrow;
    }

    public void setCanRegen(boolean canRegen) {
        this.canRegen = canRegen;
    }

    public void setPlantsGrowChance(int plantsGrowChance) {
        this.plantsGrowChance = plantsGrowChance;
    }

    public void setRegenDuration(int regenDuration) {
        this.regenDuration = regenDuration;
    }

    public void setRegenLevel(int regenLevel) {
        this.regenLevel = regenLevel;
    }

    public boolean isColdBiomesBuff() {
        return coldBiomesBuff;
    }

    public boolean isCanGrow() {
        return canGrow;
    }

    public boolean isCanRegen() {
        return canRegen;
    }
}
