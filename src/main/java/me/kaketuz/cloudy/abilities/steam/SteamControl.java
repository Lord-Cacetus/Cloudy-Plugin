package me.kaketuz.cloudy.abilities.steam;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.waterbending.passive.FastSwim;
import me.kaketuz.cloudy.Cloudy;
import me.kaketuz.cloudy.abilities.steam.combos.FollowingSteams;
import me.kaketuz.cloudy.abilities.steam.util.Cloud;
import me.kaketuz.cloudy.abilities.sub.SteamAbility;
import me.kaketuz.cloudy.util.Methods;
import me.kaketuz.cloudy.util.Particles;
import me.kaketuz.cloudy.util.Sounds;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class SteamControl extends SteamAbility implements AddonAbility {

    private int maxClouds;
    private double throwSpeed, followSpeed, endBurstPower, sourceRadius;
    private long additionalLiveTime;

    private boolean removeAfterThrow;

    private long cooldown;

    private final List<Cloud> clouds = new CopyOnWriteArrayList<>();

    private ConcurrentHashMap<Cloud, AtomicBoolean> readyClouds; //<---- omagad atomic boolean

    //private final List<Pair<Cloud, Boolean>> readyClouds = new CopyOnWriteArrayList<>();

    private Location target;

    private boolean threwOnce;

    public SteamControl(Player player, boolean followSteams) {
        super(player);
        if (!bPlayer.canBendIgnoreBinds(this) || hasAbility(player, SteamControl.class)) return;

        maxClouds = Cloudy.config.getInt("Steam.SteamControl.MaxClouds");
        throwSpeed = Cloudy.config.getDouble("Steam.SteamControl.ThrowSpeed");
        followSpeed = Cloudy.config.getDouble("Steam.SteamControl.FollowSpeed");
        endBurstPower = Cloudy.config.getDouble("Steam.SteamControl.EndBurstPower");
        sourceRadius = Cloudy.config.getDouble("Steam.SteamControl.SourceRadius");
        additionalLiveTime = Cloudy.config.getLong("Steam.SteamControl.AdditionalLiveTime");
        removeAfterThrow = Cloudy.config.getBoolean("Steam.SteamControl.RemoveAfterThrow");
        cooldown = Cloudy.config.getLong("Steam.SteamControl.Cooldown");

        if (!followSteams) {

            int count = 0;
            Location targ = GeneralMethods.getTargetedLocation(player, 4);

            if (Cloud.getCloudsAroundPoint(targ, sourceRadius).isEmpty()) return;

            for (Cloud c : Cloud.getCloudsAroundPoint(targ, sourceRadius)) {
                if (!FollowingSteams.isCloudInFollowingCouples(c) && !Objects.equals(c.getOwner(), player)) continue;

                clouds.add(c);
                count++;
                if (count >= maxClouds) break;
            }
            clouds.forEach(c -> c.addLivetime(additionalLiveTime));
            clouds.forEach(c -> c.setOwner(player));

            readyClouds = new ConcurrentHashMap<>(clouds.size());

            for (Cloud cloud : clouds) {
                readyClouds.put(cloud, new AtomicBoolean(false));
            }
        }
        else {
            if (!hasAbility(player, FollowingSteams.class)) return;
            clouds.addAll(getAbility(player, FollowingSteams.class).getUsableClouds());
            getAbility(player, FollowingSteams.class).getUsableClouds().forEach(c -> readyClouds.put(c, new AtomicBoolean(true)));
            getAbility(player, FollowingSteams.class).removeAllClouds();
        }

        if (hasAbility(player, FastSwim.class)) getAbility(player, FastSwim.class).remove();



        start();
    }





    @Override
    public void progress() {
        if (player.isDead() || !player.isOnline() || !bPlayer.canBendIgnoreBinds(this)) {
            bPlayer.addCooldown(this);
            remove();
        }

        target = GeneralMethods.getTargetedLocation(player, 4);

        clouds.forEach(c -> {
            if (!c.isOnVelocityTracker()) {
                readyClouds.get(c).set(c.getLocation().distance(target) < 1);
                c.move(GeneralMethods.getDirection(c.getLocation(), target).normalize().multiply(followSpeed));
            }
        });

        clouds.removeIf(Cloud::isCancelled);
        readyClouds.forEach((c, b) -> {
            if (c.isCancelled()) readyClouds.remove(c, b);
        });


        if (clouds.isEmpty()) {
            bPlayer.addCooldown(this);
            remove();
        }
        if (!player.isSneaking()) {
            bPlayer.addCooldown(this);
            remove();
        }

        if (!bPlayer.getBoundAbilityName().equals(getName())) {
            bPlayer.addCooldown(this);
            remove();
        }
    }

    public void throwCloud() {
        if (readyClouds.values().stream().noneMatch(AtomicBoolean::get)) return;


        AtomicReference<Cloud> cloud = new AtomicReference<>(null);

        readyClouds.forEach((c, b) -> {
            if (b.get() && cloud.get() == null) cloud.set(c);
        });

        cloud.get().setVelocity(player.getLocation().getDirection().multiply(throwSpeed));
        threwOnce = true;
        Sounds.playSound(cloud.get().getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.3f, 0.75f);
        if (removeAfterThrow) {
            clouds.remove(cloud.get());
            readyClouds.remove(cloud.get());
        }
        else readyClouds.get(cloud.get()).set(false);

    }

    @Override
    public void remove() {
        super.remove();
        if (!readyClouds.isEmpty() && threwOnce) {
            Particles.spawnParticle(Particle.CLOUD, target, 30, 0, 0, 0, 0.8);
            clouds.forEach(c -> c.setVelocity(Methods.getRandom().normalize().multiply(endBurstPower)));
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
        return "SteamControl";
    }

    @Override
    public Location getLocation() {
        return target == null ? null : target;
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
        return Cloudy.config.getBoolean("Steam.SteamControl.Enabled");
    }

    @Override
    public String getDescription() {
        return Cloudy.config.getString("Steam.SteamControl.Description");
    }
    @Override
    public String getInstructions() {
        return Cloudy.config.getString("Steam.SteamControl.Instructions");
    }

    public ConcurrentHashMap<Cloud, AtomicBoolean> getReadyClouds() {
        return readyClouds;
    }

    public int getMaxClouds() {
        return maxClouds;
    }

    public double getFollowSpeed() {
        return followSpeed;
    }

    public double getThrowSpeed() {
        return throwSpeed;
    }

    public double getSourceRadius() {
        return sourceRadius;
    }

    public double getEndBurstPower() {
        return endBurstPower;
    }

    public long getAdditionalLiveTime() {
        return additionalLiveTime;
    }

    public List<Cloud> getClouds() {
        return clouds;
    }

    public Location getTarget() {
        return target;
    }

    public boolean isRemoveAfterThrow() {
        return removeAfterThrow;
    }

    public boolean isThrewOnce() {
        return threwOnce;
    }

    public void setFollowSpeed(double followSpeed) {
        this.followSpeed = followSpeed;
    }

    public void setMaxClouds(int maxClouds) {
        this.maxClouds = maxClouds;
    }

    public void setThrowSpeed(double throwSpeed) {
        this.throwSpeed = throwSpeed;
    }

    public void setSourceRadius(double sourceRadius) {
        this.sourceRadius = sourceRadius;
    }

    public void setAdditionalLiveTime(long additionalLiveTime) {
        this.additionalLiveTime = additionalLiveTime;
    }

    public void setEndBurstPower(double endBurstPower) {
        this.endBurstPower = endBurstPower;
    }


    public void setRemoveAfterThrow(boolean removeAfterThrow) {
        this.removeAfterThrow = removeAfterThrow;
    }

    public void setTarget(Location target) {
        this.target = target;
    }

    public void setThrewOnce(boolean threwOnce) {
        this.threwOnce = threwOnce;
    }

}
