package me.kaketuz.cloudy.abilities.steam.combos;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager;
import com.projectkorra.projectkorra.ability.util.ComboUtil;
import com.projectkorra.projectkorra.util.ActionBar;
import com.projectkorra.projectkorra.util.ColoredParticle;
import com.projectkorra.projectkorra.util.DamageHandler;
import me.kaketuz.cloudy.Cloudy;
import me.kaketuz.cloudy.abilities.steam.SteamControl;
import me.kaketuz.cloudy.abilities.steam.util.Cloud;
import me.kaketuz.cloudy.abilities.sub.SteamAbility;
import me.kaketuz.cloudy.util.Methods;
import me.kaketuz.cloudy.util.Particles;
import me.kaketuz.cloudy.util.Sounds;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;


//I think need optimization :/

public class CoupleIcicles extends SteamAbility implements AddonAbility, ComboAbility {

    private int maxClouds, shotsPerCloudsAmount, currShots;
    private double damage, collisionRadius, speed, range, sourceRadius, followSpeed;

    private long cooldown;

    private double ratio;

    private boolean gravityFalling, displayVar, allowNoDamageTicks;

    private String message;


    private double ringRadius, ringSpeed, currRad;
    private boolean flag = true;

    private final List<ItemDisplay> displays = new CopyOnWriteArrayList<>();

    private final ConcurrentHashMap<Cloud, AtomicBoolean> clouds = new ConcurrentHashMap<>();

    public CoupleIcicles(Player player) {
        super(player);
        if (!bPlayer.canBendIgnoreBinds(this) || hasAbility(player, CoupleIcicles.class)) return;

        maxClouds = Cloudy.config.getInt("Steam.Combo.CoupleIcicles.MaxClouds");
        shotsPerCloudsAmount = Cloudy.config.getInt("Steam.Combo.CoupleIcicles.ShotsPerCloudsAmount");
        damage = Cloudy.config.getDouble("Steam.Combo.CoupleIcicles.Damage");
        collisionRadius = Cloudy.config.getDouble("Steam.Combo.CoupleIcicles.CollisionRadius");
        speed = Cloudy.config.getDouble("Steam.Combo.CoupleIcicles.Speed");
        range = Cloudy.config.getDouble("Steam.Combo.CoupleIcicles.Range");
        ringRadius = Cloudy.config.getDouble("Steam.Combo.CoupleIcicles.RingRadius");
        ringSpeed = Cloudy.config.getDouble("Steam.Combo.CoupleIcicles.RingSpeed");
        sourceRadius = Cloudy.config.getDouble("Steam.Combo.CoupleIcicles.SourceRadius");
        followSpeed = Cloudy.config.getDouble("Steam.Combo.CoupleIcicles.FollowSpeed");
        cooldown = Cloudy.config.getLong("Steam.Combo.CoupleIcicles.Cooldown");
        gravityFalling = Cloudy.config.getBoolean("Steam.Combo.CoupleIcicles.GravityFalling");
        displayVar = Cloudy.config.getBoolean("Steam.Combo.CoupleIcicles.DisplayVariation");
        allowNoDamageTicks = Cloudy.config.getBoolean("Steam.Combo.CoupleIcicles.AllowNoDamageTicks");
        message = Cloudy.config.getString("Steam.Combo.CoupleIcicles.Message");
        int count = 0;

        if (hasAbility(player, SteamControl.class)) getAbility(player, SteamControl.class).remove();

        if (Cloud.getCloudsAroundPoint(player.getEyeLocation(), sourceRadius).isEmpty()) return;

        for (Cloud c : Cloud.getCloudsAroundPoint(player.getEyeLocation(), sourceRadius)) {
            if (FollowingSteams.isCloudInFollowingCouples(c)) {

                if (c.getOwner() != null && !c.getOwner().equals(player)) continue;
            }
            c.setOwner(player);
            clouds.put(c, new AtomicBoolean(false));
            count++;
            if (count >= maxClouds) break;
        }

        currShots += shotsPerCloudsAmount;


        start();
    }

    @Override
    public void progress() {
        if (player.isDead() || !player.isOnline() || !bPlayer.canBendIgnoreBinds(this)) {
            bPlayer.addCooldown(this);
            remove();
        }
        if (!player.isSneaking()) {
            Sounds.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1, 1);
            for (int i = 0; i < 30; i++) {
                Vector rv = Methods.getRandom().setY(0).normalize().multiply(ringRadius);
                Particles.spawnParticle(Particle.CLOUD, player.getLocation().add(0,1, 0).add(rv), 0, rv.getX(), 0, rv.getZ(), 0.1);
            }
            bPlayer.addCooldown(this);
            remove();
        }
        if (0 >= currShots) {
            bPlayer.addCooldown(this);
            remove();
        }

        ratio += ringSpeed;
        if (currShots != 0 && clouds.values().stream().anyMatch(AtomicBoolean::get)) {
            for (int i = 0; i < currShots; i++) {
                double angle = i * (2 * Math.PI / currShots);
                double x = currRad * Math.cos(angle + ratio);
                double z = currRad * Math.sin(angle + ratio);
                Location loc = player.getLocation().clone();
                loc.add(x, 1, z);
                if (!displayVar) {
                    new ColoredParticle(Color.fromRGB(140, 180, 198), 1).display(loc, 1, 0, 0, 0);
                }
                else {
                    if (!displays.isEmpty()) {
                        loc.setPitch(0);
                        loc.setYaw(0);
                        displays.get(i == displays.size() ? i - 1 : i).setTeleportDuration(2);
                        displays.get(i == displays.size() ? i - 1 : i).teleport(loc);
                    }
                }
            }
        }

         ActionBar.sendActionBar(ChatColor.AQUA + "" + ChatColor.UNDERLINE + message + currShots, player);

        if (ratio >= 360) ratio = 0;

        clouds.forEach((c, b) -> {

            if (!b.get()) {
                c.addLivetime(100);
                c.move(GeneralMethods.getDirection(c.getLocation(), player.getEyeLocation()).normalize().multiply(followSpeed));
                if (c.getLocation().distance(player.getEyeLocation()) < 2) {
                    displays.forEach(ItemDisplay::remove);
                    displays.clear();
                    currShots += shotsPerCloudsAmount;

                    for (int i = 0; i < currShots + 1; i++) {
                        double angle = i * (2 * Math.PI / currShots);
                        double x = currRad * Math.cos(angle + ratio);
                        double z = currRad * Math.sin(angle + ratio);
                        Location loc = player.getLocation().clone();
                        loc.add(x, 1, z);
                        Particles.spawnParticle(Particle.SNOWFLAKE, loc, 4, 0.2, 0.3, 0.2, 0.07);
                        if (displayVar) {
                            ItemDisplay db = (ItemDisplay) Objects.requireNonNull(loc.getWorld()).spawnEntity(loc, EntityType.ITEM_DISPLAY);
                            db.setItemStack(new ItemStack(Material.ICE));
                            Transformation t = db.getTransformation();
                            t.getScale().set(new Vector3f(0.1f, 0.5f, 0.1f));
                            db.setTransformation(t);
                            displays.add(db);
                        }
                    }

                    if (displayVar) recalculateDisplays();

                    for (int i = 0; i < 10; i++) {
                        Vector rv = Methods.getRandom();
                        Particles.spawnParticle(Particle.CLOUD, player.getLocation().add(0, 1, 0), 0, rv.getX(), 0, rv.getZ(), 0.2);
                    }
                    Sounds.playSound(player.getEyeLocation(), Sound.ENTITY_PLAYER_HURT_FREEZE, 0.5f, 1);
                    Sounds.playSound(player.getEyeLocation(), Sound.BLOCK_GLASS_BREAK, 0.5f, 0);
                    flag = false;
                    c.remove(true);
                    b.set(true);
                }
            }
        });

        if (!flag) {
            if (currRad < ringRadius) currRad += 0.2;
            else currRad = ringRadius;
        }


    }

    private void recalculateDisplays() {
        if (displays.isEmpty()) return;
        displays.removeLast().remove();
        //Thats all lol
    }


    public void shot() {
        Sounds.playSound(player.getEyeLocation(), Sound.ENTITY_PLAYER_HURT_FREEZE, 0.5f, 0);
        if (displayVar) recalculateDisplays();
        new Icicle();
        currShots--;
    }

    @Override
    public void remove() {
        super.remove();
        clouds.forEach((c, b) -> {
            c.remove(true);
            clouds.remove(c, b);
        });
        displays.forEach(FallingDisplay::new);
        displays.clear();
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
        return "CoupleIcicles";
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
        return new CoupleIcicles(player);
    }

    @Override
    public ArrayList<ComboManager.AbilityInformation> getCombination() {
        return ComboUtil.generateCombinationFromList(this, Cloudy.config.getStringList("Steam.Combo.CoupleIcicles.Combination"));
    }

    @Override
    public boolean isEnabled() {
        return Cloudy.config.getBoolean("Steam.Combo.CoupleIcicles.Enabled");
    }

    private class Icicle extends BukkitRunnable {

        private final Location origin;
        private Location location;
        private final Vector direction;

        private ItemDisplay icicle;

        private Location arc;
        private double c;

        public Icicle() {
            origin = player.getEyeLocation();
            location = origin.clone();
            direction = player.getLocation().getDirection().multiply(speed);

            if (displayVar) {
                icicle = (ItemDisplay) Objects.requireNonNull(origin.getWorld()).spawnEntity(origin, EntityType.ITEM_DISPLAY);
                icicle.setItemStack(new ItemStack(Material.ICE));
                Transformation t = icicle.getTransformation();
                t.getScale().set(new Vector3f(0.1f, 1, 0.1f));
                icicle.setPersistent(false);
                icicle.setTransformation(t);
                icicle.setTeleportDuration(3);
                Location add = location.clone().setDirection(direction);
                float pitch = location.getPitch();
                float yaw = location.getYaw();

                pitch += location.getPitch() >= 0 ? -90 : 90;

                add.setPitch(pitch);
                add.setYaw(yaw);

                c = 90;

                icicle.teleport(add);
            }

            runTaskTimer(Cloudy.plugin, 1L, 0);
        }

        @Override
        public synchronized void cancel() throws IllegalStateException {
            super.cancel();
            Sounds.playSound(location, Sound.BLOCK_GLASS_BREAK, 1, 0.75f);
            if (displayVar && icicle != null) icicle.remove();
        }

        @Override
        public void run() {
            location = location.add(direction);
            arc = location.clone();
            float pitch = location.getPitch();
            float yaw = location.getYaw();

            pitch += location.getPitch() >= 0 ? -c : c;

            arc.setPitch(pitch);
            arc.setYaw(yaw);


            if (!displayVar) {
                Particles.spawnParticle(Particle.SNOWFLAKE, location, 0, direction.getX(), direction.getY(), direction.getZ(), 0.2);
                Particles.spawnParticle(GeneralMethods.getMCVersion() >= 1205 ? Particle.valueOf("BLOCK") : Particle.BLOCK_CRACK, location, 1, 0, 0, 0, 0, Material.ICE.createBlockData());
                new ColoredParticle(Color.fromRGB(140, 180, 198), 1).display(location, 1, 0, 0, 0);
            }
            else {
                icicle.setTeleportDuration(3);
//                Location add = location.clone().setDirection(direction);
//                float pitch = location.getPitch();
//                float yaw = location.getYaw();
//
//                pitch += location.getPitch() >= 0 ? -90 : 90;
//
//                add.setPitch(pitch);
//                add.setYaw(yaw);

                icicle.teleport(arc);
            }
            RayTraceResult result = Objects.requireNonNull(location.getWorld()).rayTraceBlocks(location, direction, speed, FluidCollisionMode.NEVER, true);

            Optional.ofNullable(result)
                    .map(RayTraceResult::getHitBlock)
                    .filter(GeneralMethods::isSolid)
                    .ifPresent(b -> {
                        Particles.spawnParticle(GeneralMethods.getMCVersion() >= 1205 ? Particle.valueOf("BLOCK") : Particle.BLOCK_CRACK, location, 10, 0.3, 0.3, 0.3, 0, Material.ICE.createBlockData());
                        cancel();
                    });

            if (origin.distance(location) >= range) {
                if (gravityFalling) {
                    direction.subtract(new Vector(0, 0.08, 0));
                    c++; //C++ NO WAY
                }
                else cancel();
            }

            GeneralMethods.getEntitiesAroundPoint(location, collisionRadius).stream()
                    .filter(e -> e instanceof LivingEntity && !e.getUniqueId().equals(player.getUniqueId()))
                    .forEach(e -> {
                        DamageHandler.damageEntity(e, player, damage, CoupleIcicles.this);
                        e.setFreezeTicks(15);
                        Particles.spawnParticle(GeneralMethods.getMCVersion() >= 1205 ? Particle.valueOf("BLOCK") : Particle.BLOCK_CRACK, location, 10, 0.3, 0.3, 0.3, 0, Material.ICE.createBlockData());
                        if (allowNoDamageTicks) ((LivingEntity)e).setNoDamageTicks(0);
                        cancel();
                    });
        }
    }
    @Override
    public String getDescription() {
        return Cloudy.config.getString("Steam.Combo.CoupleIcicles.Description");
    }

    @Override
    public String getInstructions() {
        return Cloudy.config.getString("Steam.Combo.CoupleIcicles.Instructions");
    }


    private static class FallingDisplay extends BukkitRunnable {

        private float rotX, rotY, rotZ;
        private final ItemDisplay display;
        private double gravity;

        public FallingDisplay(ItemDisplay display) {
            this.display = display;
            this.gravity = 0.1;

            runTaskTimer(Cloudy.plugin, 1L, 0);
        }

        @Override
        public void run() {
            display.setTeleportDuration(3);
            display.teleport(display.getLocation().subtract(new Vector(0, gravity, 0)));
            gravity += 0.05;


            Transformation t = display.getTransformation();
            t.getLeftRotation().set(new Quaternionf().rotateLocalX(rotX).rotateLocalY(rotY).rotateLocalZ(rotZ));
            display.setTransformation(t);

            rotX += 0.005f;
            rotY += 0.005f;
            rotZ += 0.005f;

            RayTraceResult result = display.getWorld().rayTraceBlocks(display.getLocation(), new Vector(0, gravity, 0), gravity, FluidCollisionMode.NEVER, true);

            Optional.ofNullable(result)
                    .map(RayTraceResult::getHitBlock)
                    .ifPresent(b -> cancel());
        }

        @Override
        public synchronized void cancel() throws IllegalStateException {
            super.cancel();
            Sounds.playSound(display.getLocation(), Sound.BLOCK_GLASS_BREAK, 1, 1);
            Particles.spawnParticle(GeneralMethods.getMCVersion() >= 1205 ? Particle.valueOf("BLOCK") : Particle.BLOCK_CRACK, display.getLocation(), 15, 0.2, 0.2, 0.2, 0, Objects.requireNonNull(display.getItemStack()).getType().createBlockData());
            display.remove();
        }
    }

    public int getCurrShots() {
        return currShots;
    }

    public double getDamage() {
        return damage;
    }

    public double getSpeed() {
        return speed;
    }

    public ConcurrentHashMap<Cloud, AtomicBoolean> getClouds() {
        return clouds;
    }

    public int getShotsPerCloudsAmount() {
        return shotsPerCloudsAmount;
    }

    public double getRange() {
        return range;
    }

    public double getRingSpeed() {
        return ringSpeed;
    }

    public double getRatio() {
        return ratio;
    }

    public double getSourceRadius() {
        return sourceRadius;
    }

    public double getCurrRad() {
        return currRad;
    }

    public double getRingRadius() {
        return ringRadius;
    }

    public int getMaxClouds() {
        return maxClouds;
    }

    public double getFollowSpeed() {
        return followSpeed;
    }

    public List<ItemDisplay> getDisplays() {
        return displays;
    }

    public void setCurrShots(int currShots) {
        this.currShots = currShots;
    }

    public void setMaxClouds(int maxClouds) {
        this.maxClouds = maxClouds;
    }

    public void setShotsPerCloudsAmount(int shotsPerCloudsAmount) {
        this.shotsPerCloudsAmount = shotsPerCloudsAmount;
    }

    public void setCurrRad(double currRad) {
        this.currRad = currRad;
    }

    public void setDisplayVar(boolean displayVar) {
        this.displayVar = displayVar;
    }

    public boolean isDisplayVar() {
        return displayVar;
    }

    public boolean isAllowNoDamageTicks() {
        return allowNoDamageTicks;
    }

    public boolean isGravityFalling() {
        return gravityFalling;
    }

    public void setAllowNoDamageTicks(boolean allowNoDamageTicks) {
        this.allowNoDamageTicks = allowNoDamageTicks;
    }

    public void setFollowSpeed(double followSpeed) {
        this.followSpeed = followSpeed;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    public void setRange(double range) {
        this.range = range;
    }

    public void setRingSpeed(double ringSpeed) {
        this.ringSpeed = ringSpeed;
    }

    public void setRatio(double ratio) {
        this.ratio = ratio;
    }

    public void setGravityFalling(boolean gravityFalling) {
        this.gravityFalling = gravityFalling;
    }

    public void setRingRadius(double ringRadius) {
        this.ringRadius = ringRadius;
    }

    public void setSourceRadius(double sourceRadius) {
        this.sourceRadius = sourceRadius;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

}
