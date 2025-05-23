package me.kaketuz.cloudy.abilities.steam.avatarstate;

import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import me.kaketuz.cloudy.Cloudy;
import me.kaketuz.cloudy.abilities.steam.Evaporate;
import me.kaketuz.cloudy.abilities.steam.util.Cloud;
import me.kaketuz.cloudy.util.Methods;
import me.kaketuz.cloudy.util.Particles;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;


public class ASEvaporate extends ASVariation {

    private double radius, sourceRange;
    private int maxClouds;
    private long cooldown;
    private boolean enabled;

    public ASEvaporate(Player player) {
        super(player);
         enabled = Cloudy.config.getBoolean("Steam.Evaporate.AvatarState.Enabled");

        if (!enabled) return;



        radius = Cloudy.config.getDouble("Steam.Evaporate.AvatarState.Radius");
        sourceRange = Cloudy.config.getDouble("Steam.Evaporate.AvatarState.SourceRange");
        maxClouds = Cloudy.config.getInt("Steam.Evaporate.AvatarState.MaxClouds");
        cooldown = Cloudy.config.getInt("Steam.Evaporate.AvatarState.Cooldown");

        Block source = Methods.getLookingAt(player, sourceRange, false);

        if (!ElementalAbility.isWater(source) && !ElementalAbility.isIce(source)) return;

        for (int i = 0; i < 50; i++) {
            Vector rv = Methods.getRandom().setY(0).normalize();
            Particles.spawnParticle(Particle.CLOUD, source.getLocation().add(0, 1, 0), 0, rv.getX(), 0, rv.getZ(), 0.3);
        }

        for (int i = 0; i < maxClouds; i++) {
            Vector vec = Methods.getRandom().setY(0).normalize().multiply(ThreadLocalRandom.current().nextDouble(0, radius));
            Location loc = source.getLocation();
            if (!ElementalAbility.isWater(loc.add(vec).getBlock()) && !ElementalAbility.isIce(loc.add(vec).getBlock())) continue;
            Cloud.createCloud(loc.add(vec), player);
        }
        bPlayer.addCooldown("Evaporate", cooldown);
        runTaskTimer(Cloudy.plugin, 1L, 0);
    }


    @Override
    public void run() {
        cancel();
    }

    public double getRadius() {
        return radius;
    }

    public double getSourceRange() {
        return sourceRange;
    }

    public int getMaxClouds() {
        return maxClouds;
    }

    public long getCooldown() {
        return cooldown;
    }

    public void setSourceRange(double sourceRange) {
        this.sourceRange = sourceRange;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public void setCooldown(long cooldown) {
        this.cooldown = cooldown;
    }

    public void setMaxClouds(int maxClouds) {
        this.maxClouds = maxClouds;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
