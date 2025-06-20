package me.kaketuz.cloudy.abilities.steam.util;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.airbending.AirBlast;
import com.projectkorra.projectkorra.airbending.AirSwipe;
import com.projectkorra.projectkorra.firebending.FireBlast;
import com.projectkorra.projectkorra.region.RegionProtection;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.TempBlock;
import me.kaketuz.cloudy.Cloudy;
import me.kaketuz.cloudy.abilities.steam.CloudCushion;
import me.kaketuz.cloudy.abilities.steam.Evaporate;
import me.kaketuz.cloudy.abilities.steam.combos.FollowingSteams;
import me.kaketuz.cloudy.util.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class Cloud extends BukkitRunnable implements Trackable {

    private static final Set<Cloud> clouds = ConcurrentHashMap.newKeySet();

    private final Set<LocationVelocityTracker> trackers = new HashSet<>();

    private static final Set<TempBlock> lakes = ConcurrentHashMap.newKeySet();


    static {lakes.removeIf(TempBlock::isReverted);}

    private boolean isForming, isAfterFire;
    private long duration;
    private final long start;
    private final long fireBuffDuration;
    private final double damage;
    private final double damageBuffed;
    private Location location;

    private final SplittableRandom random;

    private final Vector ambientDir;

    private final double ambientMovementSpeed;

    private final int particleAmount;

    private final LocationVelocityTracker formTracker;

    private boolean canMoveByAmbient, canFireBuffs, canAirAbilitiesMove, canCreateLakes;

    private long startAfterFireTiming;

    private final double collisionRadius;

    private final boolean snowVariable;

    private boolean hide;

    private double velocity;

    private int snowVarHeight;

    private boolean used;


    private Player owner;

    private Cloud(Location location, Player owner) {



        this.location = location;
        this.isForming = true;
        this.owner = owner;
        this.duration = Cloudy.config.getLong("Cloud.Duration");
        this.start = System.currentTimeMillis();
        this.fireBuffDuration = Cloudy.config.getLong("Cloud.FireBuffDuration");
        this.damage = Cloudy.config.getDouble("Cloud.Damage");
        this.damageBuffed = Cloudy.config.getDouble("Cloud.DamageBuffed");
        double formSpeed = Cloudy.config.getDouble("Cloud.FormSpeed");
        this.canFireBuffs = Cloudy.config.getBoolean("Cloud.CanFireAbilitiesBuffs");
        this.canMoveByAmbient = Cloudy.config.getBoolean("Cloud.CanMoveByAmbient");
        this.canAirAbilitiesMove = Cloudy.config.getBoolean("Cloud.CanAirAbilitiesMove");
        this.canCreateLakes = Cloudy.config.getBoolean("Cloud.CanCreateLakes");
        snowVariable = Cloudy.config.getBoolean("Cloud.SnowVariable");
        this.particleAmount = Cloudy.config.getInt("Cloud.ParticleAmount");
        this.ambientMovementSpeed = Cloudy.config.getDouble("Cloud.AmbientMovementSpeed");
        this.collisionRadius = Cloudy.config.getDouble("Cloud.CollisionRadius");
        this.velocity = Cloudy.config.getDouble("Cloud.Velocity");
        this.snowVarHeight = Cloudy.config.getInt("Cloud.SnowVarHeight");
        this.formTracker = new LocationVelocityTracker(location, new Vector(0, formSpeed, 0), this);
        this.random = new SplittableRandom();
        this.ambientDir = Methods.getRandom().setY(0).normalize().multiply(ambientMovementSpeed);
        //if (Methods.isNetherBiome(location)) return; causes error (ill fix it later :3)
        if (!Cloudy.addCloudArray(this, owner)) return;


        clouds.add(this);
        this.runTaskTimer(Cloudy.plugin, 1L, 0);
    }

    @Override
    public void run() {
        if (owner != null && (!owner.isOnline() || owner.isDead())) return;

        if (!hide && getCloudsAroundPoint(location, 10).size() < 5) {
            Sounds.playSound(location, Sound.ENTITY_PHANTOM_FLAP, 0.2f, 0.75f);
        }

        spawnSnowEffects();
        moveOrDrift();
        handleDamage();
        spawnWaterParticles();
        applyFireBuffFromLava();
        spawnCloudParticles();

        if (isForming) {
            isForming = !formTracker.isCancelled();
        } else {
            reactToAirAbilities();
            if (canFireBuffs) checkFireBlasts();
            if (System.currentTimeMillis() > start + duration) remove(false);
        }
    }

    private void spawnSnowEffects() {
        if ((location.getBlockY() >= snowVarHeight || Methods.getTemperature(location) <= 0) && snowVariable && !hide) {
            Particles.spawnParticle(Particle.SNOWFLAKE, location, particleAmount / 2, 0.5, 0.5, 0.5, 0);
            Optional.ofNullable(Methods.getGround(location, 10)).ifPresent(b -> {
                if (ElementalAbility.isAir(b.getRelative(BlockFace.UP).getType()) && random.nextInt(100) < 20 && !ElementalAbility.isWater(b)) {
                    Sounds.playSound(location, Sound.BLOCK_POWDER_SNOW_HIT, 0.3f, 0f);
                    b.getRelative(BlockFace.UP).setType(Material.SNOW);
                }
                if (b.getRelative(BlockFace.UP).getType() == Material.SNOW && random.nextInt(100) < 10) {
                    Sounds.playSound(location, Sound.BLOCK_POWDER_SNOW_HIT, 0.3f, 0f);
                    Methods.addSnowLayer(b);
                }
            });
        }
    }

    private void moveOrDrift() {
        if (LocationVelocityTracker.getTrackers().containsKey(this)) {
            location.add(LocationVelocityTracker.getTrackers().get(this).getVelocity());
        } else if (canMoveByAmbient && !isForming && random.nextInt(100) < 5) {
            ambientDir.add(Methods.getRandom().setY(0).normalize().multiply(ambientMovementSpeed));
            move(ambientDir);
        }
    }

    private void handleDamage() {
        if (damage <= 0) return;
        GeneralMethods.getEntitiesAroundPoint(location, 1).stream()
                .filter(e -> e instanceof LivingEntity && (owner == null || !owner.getUniqueId().equals(e.getUniqueId())))
                .forEach(e -> {
                    if (owner != null) {
                        DamageHandler.damageEntity(e, owner, isAfterFire ? damage + damageBuffed : damage, CoreAbility.getAbility(Evaporate.class));
                    } else {
                        ((LivingEntity) e).damage(isAfterFire ? damage + damageBuffed : damage);
                    }
                });
    }

    private void spawnWaterParticles() {
        if (!hide && ElementalAbility.isWater(location.getBlock())) {
            Particle particle = GeneralMethods.getMCVersion() < 1205 ? Particle.valueOf("WATER_BUBBLE") : Particle.BUBBLE;
            Particles.spawnParticle(particle, location, particleAmount, 0.5, 0.5, 0.5, 0.04);
        }
    }

    private void applyFireBuffFromLava() {
        if (!canFireBuffs) return;
        GeneralMethods.getBlocksAroundPoint(location, 5).stream()
                .filter(ElementalAbility::isLava)
                .findAny()
                .ifPresent(b -> setAfterFire());

        if (isAfterFire && !isForming && System.currentTimeMillis() > startAfterFireTiming + fireBuffDuration) {
            isAfterFire = false;
        }
    }

    private void spawnCloudParticles() {
        if (!hide) {
            Particles.spawnParticle(Particle.CLOUD, location, particleAmount, 0.5, 0.5, 0.5, isAfterFire ? 0.08 : 0.01);
        }
    }

    private void reactToAirAbilities() {
        if (!canAirAbilitiesMove) return;
        CoreAbility.getAbilities(AirBlast.class).stream()
                .filter(ab -> ab.getLocation().getWorld().equals(location.getWorld()) && ab.getLocation().distance(location) <= collisionRadius)
                .findAny()
                .ifPresent(ab -> move(ab.getDirection().normalize().multiply(velocity)));
    }

    private void checkFireBlasts() {
        CoreAbility.getAbilities(FireBlast.class).stream()
                .filter(fb -> fb.getLocation().getWorld().equals(location.getWorld()) && fb.getLocation().distance(location) <= collisionRadius)
                .findAny()
                .ifPresent(fb -> setAfterFire());
    }

    public static boolean isLake(Block block) {
        if (!TempBlock.isTempBlock(block)) return false;
        return lakes.contains(TempBlock.get(block));
    }
    @Nullable
    public static TempBlock getLake(Block block) {
        if (!TempBlock.isTempBlock(block)) return null;
        return TempBlock.get(block);
    }


    public static Cloud createCloud(Location location, Player owner) {
        return new Cloud(location, owner);
    }


    public void hide() {
        hide =! hide;
    }

    public void moveTo(Location location, double len) {
        move(GeneralMethods.getDirection(this.location, location).normalize().multiply(len));
    }

    public void move(Vector direction) {
        if (isForming) return;

        double len = direction.length();
        final Vector[] d = {direction};
        RayTraceResult result = Objects.requireNonNull(location.getWorld()).rayTraceBlocks(location, direction, direction.length(), FluidCollisionMode.ALWAYS, true);

        Optional.ofNullable(result)
                .map(RayTraceResult::getHitBlock)
                .ifPresent(b -> {
                    if (GeneralMethods.isSolid(b) || ElementalAbility.isWater(b)) {

                      //This code has been taken from LaserFission ability (ProjectCosmos plugin) and slightly changed by me

                        Vector newDir = d[0].clone();

                        Block block = this.getLocation().getBlock();
                        ArrayList<Block> blocks = new ArrayList<>(0);
                        blocks.add(block.getRelative(BlockFace.DOWN));
                        blocks.add(block.getRelative(BlockFace.UP));
                        blocks.add(block.getRelative(BlockFace.NORTH));
                        blocks.add(block.getRelative(BlockFace.SOUTH));
                        blocks.add(block.getRelative(BlockFace.EAST));
                        blocks.add(block.getRelative(BlockFace.WEST));
                        Vector tmpDirection = this.location.getDirection().clone().multiply(0.1D);
                        Location tmpLocation = this.location.clone().subtract(tmpDirection);
                        while (tmpLocation.getBlock().getLocation().equals(block.getLocation()))
                            tmpLocation.subtract(tmpDirection);
                        int i = 0;
                        for (Block b1 : blocks) {
                            if (tmpLocation.getBlock().getLocation().equals(b1.getLocation()))
                                break;
                            i++;
                        }
                        if ((((i == 0) ? 1 : 0) | ((i == 1) ? 1 : 0)) != 0) {
                            newDir.setY(newDir.getY() * -1.0D);
                        } else if ((((i == 2) ? 1 : 0) | ((i == 3) ? 1 : 0)) != 0) {
                            newDir.setZ(newDir.getZ() * -1.0D);
                        } else {
                            newDir.setX(newDir.getX() * -1.0D);
                        }
                        d[0] = newDir;
                    }
                });
        this.location.add(d[0]);
    }

    public void setVelocity(Vector direction) {
        if (isForming) return;
        new LocationVelocityTracker(this.location, direction, this);
    }

    public Location getLocation() {
        return location;
    }

    public boolean isAfterFire() {
        return isAfterFire;
    }

    public boolean isForming() {
        return isForming;
    }

    public boolean isCanFireBuffs() {
        return canFireBuffs;
    }

    public boolean isCanAirAbilitiesMove() {
        return canAirAbilitiesMove;
    }

    public boolean isCanMoveByAmbient() {
        return canMoveByAmbient;
    }

    public void setAfterFire() {
        if (!canFireBuffs) return;
        if (isForming) return;
        if (isAfterFire) return;
        isAfterFire = true;
        startAfterFireTiming = System.currentTimeMillis();
    }

    @Nullable
    public Player getOwner() {
        return owner;
    }

    public void setOwner(Player owner) {
        if (owner.equals(this.owner)) return;
        this.owner = owner;
    }

    public void setCanMoveByAmbient(boolean canMoveByAmbient) {
        this.canMoveByAmbient = canMoveByAmbient;
    }

    public void setCanAirAbilitiesMove(boolean canAirAbilitiesMove) {
        this.canAirAbilitiesMove = canAirAbilitiesMove;
    }

    public void setCanCreateLakes(boolean canCreateLakes) {
        this.canCreateLakes = canCreateLakes;
    }

    public void setCanFireBuffs(boolean canFireBuffs) {
        this.canFireBuffs = canFireBuffs;
    }

    public void setForming(boolean forming) {
        isForming = forming;
    }

    public boolean isCanCreateLakes() {
        return canCreateLakes;
    }


    public void remove(boolean forced) {
        if (!forced) {
            if (!hide) {
                Particles.spawnParticle(Particle.FALLING_WATER, location, 10, 0.5, 0, 0.5);
                for (int i = 0; i < 10; i++) {
                    Vector rv = Methods.getRandom();
                    Particles.spawnParticle(Particle.CLOUD, location, 0, rv.getX(), 0, rv.getZ(), 0.1);
                }
            }
           if (canCreateLakes) {
               Optional.ofNullable(Methods.getGround(location, 10))
                       .ifPresent(b -> {
                           if (!ElementalAbility.isWater(b)) {
                               if (!hide) {
                                   lakes.add(new TempBlock(b.getRelative(BlockFace.UP, 1),
                                           Material.WATER.createBlockData(dat -> ((Levelled)dat).setLevel(6)), 10000));
                               }
                           }
                       });
           }
        }

        cancel();
    }

    @Override
    public synchronized void cancel() throws IllegalStateException {
        super.cancel();
        clouds.remove(this);
        Cloudy.removeCloudArray(owner, this);
    }

    public void addLivetime(long milliseconds) {
        this.duration += milliseconds;
    }

    public static Set<Cloud> getClouds() {
        return clouds;
    }

    public boolean isOnVelocityTracker() {
        return LocationVelocityTracker.getTrackers().containsKey(this);
    }

    public static List<Cloud> getCloudsAroundPoint(Location center, double radius) {
        return getClouds().stream()
                .filter(c -> Objects.equals(c.getLocation().getWorld(), center.getWorld()) && c.getLocation().distance(center) <= radius)
                .collect(Collectors.toList());
    }

    public void setVelocity(double velocity) {
        this.velocity = velocity;
    }

    public double getVelocity() {
        return velocity;
    }

    public boolean isHidden() {
        return hide;
    }

    public double getCollisionRadius() {
        return collisionRadius;
    }

    public double getDamage() {
        return damage;
    }

    public long getDuration() {
        return duration;
    }

    public double getAmbientMovementSpeed() {
        return ambientMovementSpeed;
    }

    public double getDamageBuffed() {
        return damageBuffed;
    }

    public int getParticleAmount() {
        return particleAmount;
    }

    public long getFireBuffDuration() {
        return fireBuffDuration;
    }

    public Set<LocationVelocityTracker> getTrackers() {
        return trackers;
    }

    public static Set<TempBlock> getLakes() {
        return lakes;
    }

    public int getSnowVarHeight() {
        return snowVarHeight;
    }

    public Vector getAmbientDir() {
        return ambientDir;
    }

    public LocationVelocityTracker getFormTracker() {
        return formTracker;
    }

    public boolean isSnowVariable() {
        return snowVariable;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setAfterFire(boolean afterFire) {
        isAfterFire = afterFire;
    }

    public void setHide(boolean hide) {
        this.hide = hide;
    }

    public void setSnowVarHeight(int snowVarHeight) {
        this.snowVarHeight = snowVarHeight;
    }

    public boolean isUsing() {
        return used;
    }

    public void setUse(boolean used) {
        this.used = used;
    }

    public long getStartAfterFireTiming() {
        return startAfterFireTiming;
    }

    public void setStartAfterFireTiming(long startAfterFireTiming) {
        this.startAfterFireTiming = startAfterFireTiming;
    }

    public void teleportTo(Location location) {
        this.location = location;
    }
}
