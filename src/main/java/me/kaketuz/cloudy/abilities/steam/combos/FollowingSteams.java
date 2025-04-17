package me.kaketuz.cloudy.abilities.steam.combos;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager;
import com.projectkorra.projectkorra.ability.util.ComboUtil;
import me.kaketuz.cloudy.Cloudy;
import me.kaketuz.cloudy.abilities.steam.util.Cloud;
import me.kaketuz.cloudy.abilities.sub.SteamAbility;
import me.kaketuz.cloudy.util.Methods;
import me.kaketuz.cloudy.util.Particles;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class FollowingSteams extends SteamAbility implements AddonAbility, ComboAbility {

    //good morning code!

    private long duration, cooldown;
    private int maxClouds, speedBoost;
    private double knockback, followSpeed, sourceRadius;

    //I love ConcurrentModificationException
    private final List<Cloud> selectedClouds = new CopyOnWriteArrayList<>();
    private final List<Cloud> usableClouds = new CopyOnWriteArrayList<>();


    private boolean coldBiomesBuff, nightBuff;
    private double buffFactor;

    private boolean nonEmpty;

    public FollowingSteams(Player player) {
        super(player);
        if (!bPlayer.canBendIgnoreBinds(this) || hasAbility(player, FollowingSteams.class)) return;

        duration = Cloudy.config.getLong("Steam.Combo.FollowingSteams.Duration");
        cooldown = Cloudy.config.getLong("Steam.Combo.FollowingSteams.Cooldown");
        maxClouds = Cloudy.config.getInt("Steam.Combo.FollowingSteams.MaxClouds");
        speedBoost = Cloudy.config.getInt("Steam.Combo.FollowingSteams.SpeedBoost");
        knockback = Cloudy.config.getDouble("Steam.Combo.FollowingSteams.Knockback");
        followSpeed = Cloudy.config.getDouble("Steam.Combo.FollowingSteams.FollowSpeed");
        sourceRadius = Cloudy.config.getDouble("Steam.Combo.FollowingSteams.SourceRadius");
        coldBiomesBuff = Cloudy.config.getBoolean("Steam.Combo.FollowingSteams.ColdBiomesBuff");
        nightBuff = Cloudy.config.getBoolean("Steam.Combo.FollowingSteams.NightBuff");
        buffFactor = Cloudy.config.getDouble("Steam.Combo.FollowingSteams.BuffFactor");

        if (coldBiomesBuff && Methods.getTemperature(player.getLocation()) <= 0) {
            duration *= (long) buffFactor;
            maxClouds *= (int) buffFactor;
            speedBoost *= (int) buffFactor;
            followSpeed *= buffFactor;
            sourceRadius *= buffFactor;
        }
        if (nightBuff && isNight(player.getWorld())) {
            duration *= (long) buffFactor;
            maxClouds *= (int) buffFactor;
            speedBoost *= (int) buffFactor;
            followSpeed *= buffFactor;
            sourceRadius *= buffFactor;
        }

        int count = 0;

        if (Cloud.getCloudsAroundPoint(player.getEyeLocation(), sourceRadius).isEmpty()) return;

        for (Cloud c : Cloud.getCloudsAroundPoint(player.getEyeLocation(), sourceRadius)) {
            if (c.isUsing() || c.isHidden()) continue;
            c.setUse(true);
            c.setOwner(player);
            selectedClouds.add(c);
            count++;
            if (count >= maxClouds) break;
        }


        start();
    }

    @Override
    public void progress() {
        if (player.isDead() || !player.isOnline() || !bPlayer.canBendIgnoreBinds(this)) {
            bPlayer.addCooldown(this);
            remove();
        }
        if (!usableClouds.isEmpty()) {
            nonEmpty = true;
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, speedBoost - 1, true, false, false));
        }
        else if (nonEmpty) {
            bPlayer.addCooldown(this);
            remove();
        }
        selectedClouds.forEach(c -> {
            c.addLivetime(100);
            c.move(GeneralMethods.getDirection(c.getLocation(), player.getEyeLocation()).normalize().multiply(followSpeed));
            if (!usableClouds.contains(c)) {
                if (c.getLocation().distance(player.getEyeLocation()) < 1.5) {
                    usableClouds.add(c);
                }
            }
        });

        if (System.currentTimeMillis() > getStartTime() + duration) {
            bPlayer.addCooldown(this);
            remove();
        }


    }

    public boolean canPlayerUseFastVars() {
        return usableClouds.isEmpty();
    }

    public void removeCloud() {
        selectedClouds.getLast().remove(true);
        selectedClouds.remove(selectedClouds.getLast());
        usableClouds.getLast().remove(true);
        usableClouds.remove(usableClouds.getLast());
    }
    public static boolean isCloudInFollowingCouples(Cloud cloud) {
        return getAbilities(FollowingSteams.class).stream().anyMatch(f -> f.usableClouds.contains(cloud));
    }
    public void removeAllClouds() {
        usableClouds.forEach(c -> {
            c.remove(true);

            usableClouds.remove(c);
            selectedClouds.remove(c);
        });
    }


    public List<Cloud> getUsableClouds() {
        return usableClouds;
    }

    @Override
    public void remove() {
        super.remove();
        if (!usableClouds.isEmpty()) {
            Particles.spawnParticle(Particle.CLOUD, player.getEyeLocation(), 30, 0, 0, 0, 0.8);
            usableClouds.forEach(c -> {
                c.setUse(false);
                c.setVelocity(Methods.getRandom());
            });
            GeneralMethods.getEntitiesAroundPoint(player.getEyeLocation(), knockback)
                    .forEach(e -> e.setVelocity(GeneralMethods.getDirection(player.getEyeLocation(), e.getLocation()).normalize().multiply(knockback)));
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
        return "FollowingSteams";
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
        return new FollowingSteams(player);
    }

    @Override
    public ArrayList<ComboManager.AbilityInformation> getCombination() {
        return ComboUtil.generateCombinationFromList(this, Cloudy.config.getStringList("Steam.Combo.FollowingSteams.Combination"));
    }

    @Override
    public boolean isEnabled() {
        return Cloudy.config.getBoolean("Steam.Combo.FollowingSteams.Enabled");
    }

    @Override
    public String getDescription() {
        return Cloudy.config.getString("Steam.Combo.FollowingSteams.Description");
    }

    @Override
    public String getInstructions() {
        return Cloudy.config.getString("Steam.Combo.FollowingSteams.Instructions");
    }

    public long getDuration() {
        return duration;
    }

    public double getFollowSpeed() {
        return followSpeed;
    }

    public int getMaxClouds() {
        return maxClouds;
    }

    public double getKnockback() {
        return knockback;
    }

    public int getSpeedBoost() {
        return speedBoost;
    }

    public double getSourceRadius() {
        return sourceRadius;
    }

    public List<Cloud> getSelectedClouds() {
        return selectedClouds;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setMaxClouds(int maxClouds) {
        this.maxClouds = maxClouds;
    }

    public void setFollowSpeed(double followSpeed) {
        this.followSpeed = followSpeed;
    }

    public void setSpeedBoost(int speedBoost) {
        this.speedBoost = speedBoost;
    }

    public void setKnockback(double knockback) {
        this.knockback = knockback;
    }

    public void setSourceRadius(double sourceRadius) {
        this.sourceRadius = sourceRadius;
    }

    public double getBuffFactor() {
        return buffFactor;
    }

    public boolean isNightBuff() {
        return nightBuff;
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

    public void setNonEmpty(boolean nonEmpty) {
        this.nonEmpty = nonEmpty;
    }

    public boolean isColdBiomesBuff() {
        return coldBiomesBuff;
    }

    public boolean isNonEmpty() {
        return nonEmpty;
    }
}
