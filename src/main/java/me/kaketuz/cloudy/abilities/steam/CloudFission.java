package me.kaketuz.cloudy.abilities.steam;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.util.DamageHandler;
import me.kaketuz.cloudy.Cloudy;
import me.kaketuz.cloudy.abilities.steam.util.Cloud;
import me.kaketuz.cloudy.abilities.sub.SteamAbility;
import me.kaketuz.cloudy.util.Methods;
import me.kaketuz.cloudy.util.Particles;
import me.kaketuz.cloudy.util.Sounds;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class CloudFission extends SteamAbility implements AddonAbility {

    private double sourceRange, damage, buffFactor;
    private long cooldown;
    private int freezeTicks, slowLevel, slowDuration, maxClouds;

    private boolean coldBiomesBuff, nightBuff;

    private double radius;

    public CloudFission(Player player, List<Cloud>clouds) {
        super(player);
        if (!bPlayer.canBendIgnoreBinds(this) || hasAbility(player, CloudFission.class)) return;

        sourceRange = Cloudy.config.getDouble("Steam.CloudFission.SourceRange");
        damage = Cloudy.config.getDouble("Steam.CloudFission.Damage");
        cooldown = Cloudy.config.getLong("Steam.CloudFission.Cooldown");
        freezeTicks = Cloudy.config.getInt("Steam.CloudFission.FreezeTicks");
        slowLevel = Cloudy.config.getInt("Steam.CloudFission.SlowLevel");
        slowDuration = Cloudy.config.getInt("Steam.CloudFission.SlowDuration");
        maxClouds = Cloudy.config.getInt("Steam.CloudFission.MaxClouds");
        coldBiomesBuff = Cloudy.config.getBoolean("Steam.CloudFission.ColdBiomesBuff");
        nightBuff = Cloudy.config.getBoolean("Steam.CloudFission.NightBuff");
        buffFactor = Cloudy.config.getDouble("Steam.CloudFission.BuffFactor");
        radius = Cloudy.config.getDouble("Steam.CloudFission.Radius");

        if (coldBiomesBuff && Methods.getTemperature(player.getLocation()) <= 0) {
            sourceRange *= buffFactor;
            damage *= buffFactor;
            freezeTicks *= (int) buffFactor;
            slowDuration *= (int) buffFactor;
            slowLevel *= (int) buffFactor;
            maxClouds *= (int) buffFactor;
        }
        if (nightBuff && isNight(player.getWorld())) {
            sourceRange *= buffFactor;
            damage *= buffFactor;
            freezeTicks *= (int) buffFactor;
            slowDuration *= (int) buffFactor;
            slowLevel *= (int) buffFactor;
            maxClouds *= (int) buffFactor;
        }

        if (clouds.isEmpty()) return;

        clouds.forEach(c -> {

            Sounds.playSound(c.getLocation(), Sound.BLOCK_GLASS_BREAK, 1, 0);
            Sounds.playSound(c.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 0.2f, 1);
            Sounds.playSound(c.getLocation(), Sound.ENTITY_PLAYER_HURT_FREEZE, 1, 0);
            Sounds.playSound(c.getLocation(), Sound.BLOCK_WOOL_BREAK, 1, 0);

            Particles.spawnParticle(Particle.SNOWFLAKE, c.getLocation(), 50, 0.2, 0.2, 0.2, 0.3);
            Particles.spawnParticle(Particle.FALLING_DUST, c.getLocation(), 10, 0.2, 0.2, 0.2, 0, Material.ICE.createBlockData());
            Particles.spawnParticle(GeneralMethods.getMCVersion() <= 1204 ? Particle.valueOf("BLOCK_CRACK") : Particle.BLOCK, c.getLocation(), 10, 0.2, 0.2, 0.2, 0, Material.ICE.createBlockData());

            GeneralMethods.getEntitiesAroundPoint(c.getLocation(), radius).stream()
                    .filter(e -> e instanceof LivingEntity && !e.getUniqueId().equals(player.getUniqueId()))
                    .forEach(e -> {
                        DamageHandler.damageEntity(e, player, damage, this);
                        e.setFreezeTicks(freezeTicks);
                        ((LivingEntity)e).addPotionEffect(new PotionEffect(GeneralMethods.getMCVersion() <= 1204 ? PotionEffectType.getByName("SLOW") : PotionEffectType.SLOWNESS, slowDuration, slowLevel, true, false, false));
                    });
            c.remove(true);
        });


        start();
    }

    public CloudFission(Player player) {
        super(player);
        if (!bPlayer.canBendIgnoreBinds(this) || hasAbility(player, CloudFission.class)) return;

        sourceRange = Cloudy.config.getDouble("Steam.CloudFission.SourceRange");
        damage = Cloudy.config.getDouble("Steam.CloudFission.Damage");
        cooldown = Cloudy.config.getLong("Steam.CloudFission.Cooldown");
        freezeTicks = Cloudy.config.getInt("Steam.CloudFission.FreezeTicks");
        slowLevel = Cloudy.config.getInt("Steam.CloudFission.SlowLevel");
        slowDuration = Cloudy.config.getInt("Steam.CloudFission.SlowDuration");
        maxClouds = Cloudy.config.getInt("Steam.CloudFission.MaxClouds");
        coldBiomesBuff = Cloudy.config.getBoolean("Steam.CloudFission.ColdBiomesBuff");
        nightBuff = Cloudy.config.getBoolean("Steam.CloudFission.NightBuff");
        buffFactor = Cloudy.config.getDouble("Steam.CloudFission.BuffFactor");
        radius = Cloudy.config.getDouble("Steam.CloudFission.Radius");

        if (coldBiomesBuff && Methods.getTemperature(player.getLocation()) <= 0) {
            sourceRange *= buffFactor;
            damage *= buffFactor;
            freezeTicks *= (int) buffFactor;
            slowDuration *= (int) buffFactor;
            slowLevel *= (int) buffFactor;
            maxClouds *= (int) buffFactor;
        }
        if (nightBuff && isNight(player.getWorld())) {
            sourceRange *= buffFactor;
            damage *= buffFactor;
            freezeTicks *= (int) buffFactor;
            slowDuration *= (int) buffFactor;
            slowLevel *= (int) buffFactor;
            maxClouds *= (int) buffFactor;
        }

        List<Cloud> clouds = Cloud.getCloudsAroundPoint(player.getEyeLocation(), sourceRange).stream()
                .filter(c -> !c.isUsing() && !c.isHidden())
                .limit(maxClouds)
                .toList();

        if (clouds.isEmpty()) return;

        clouds.forEach(c -> {

            Sounds.playSound(c.getLocation(), Sound.BLOCK_GLASS_BREAK, 1, 0);
            Sounds.playSound(c.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 0.2f, 1);
            Sounds.playSound(c.getLocation(), Sound.ENTITY_PLAYER_HURT_FREEZE, 1, 0);
            Sounds.playSound(c.getLocation(), Sound.BLOCK_WOOL_BREAK, 1, 0);

            Particles.spawnParticle(Particle.SNOWFLAKE, c.getLocation(), 50, 0.2, 0.2, 0.2, 0.3);
            Particles.spawnParticle(Particle.FALLING_DUST, c.getLocation(), 10, 0.2, 0.2, 0.2, 0, Material.ICE.createBlockData());
            Particles.spawnParticle(GeneralMethods.getMCVersion() <= 1204 ? Particle.valueOf("BLOCK_CRACK") : Particle.BLOCK, c.getLocation(), 10, 0.2, 0.2, 0.2, 0, Material.ICE.createBlockData());

            GeneralMethods.getEntitiesAroundPoint(c.getLocation(), radius).stream()
                    .filter(e -> e instanceof LivingEntity && !e.getUniqueId().equals(player.getUniqueId()))
                    .forEach(e -> {
                        DamageHandler.damageEntity(e, player, damage, this);
                        e.setFreezeTicks(freezeTicks);
                        ((LivingEntity)e).addPotionEffect(new PotionEffect(GeneralMethods.getMCVersion() <= 1204 ? PotionEffectType.getByName("SLOW") : PotionEffectType.SLOWNESS, slowDuration, slowLevel, true, false, false));
                    });
            c.remove(true);
        });


        start();
    }

    @Override
    public void progress() {
        bPlayer.addCooldown(this);
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

    @Override
    public long getCooldown() {
        return cooldown;
    }

    @Override
    public String getName() {
        return "CloudFission";
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
        return Cloudy.config.getBoolean("Steam.CloudFission.Enabled");
    }

    @Override
    public String getDescription() {
        return Cloudy.config.getString("Steam.CloudFission.Description");
    }

    @Override
    public String getInstructions() {
        return Cloudy.config.getString("Steam.CloudFission.Instructions");
    }

    public double getDamage() {
        return damage;
    }

    public double getSourceRange() {
        return sourceRange;
    }

    public int getMaxClouds() {
        return maxClouds;
    }

    public int getFreezeTicks() {
        return freezeTicks;
    }

    public int getSlowDuration() {
        return slowDuration;
    }

    public int getSlowLevel() {
        return slowLevel;
    }

    public void setSourceRange(double sourceRange) {
        this.sourceRange = sourceRange;
    }

    public void setMaxClouds(int maxClouds) {
        this.maxClouds = maxClouds;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    public void setCooldown(long cooldown) {
        this.cooldown = cooldown;
    }

    public void setFreezeTicks(int freezeTicks) {
        this.freezeTicks = freezeTicks;
    }

    public void setSlowDuration(int slowDuration) {
        this.slowDuration = slowDuration;
    }

    public void setSlowLevel(int slowLevel) {
        this.slowLevel = slowLevel;
    }

    public void setBuffFactor(double buffFactor) {
        this.buffFactor = buffFactor;
    }

    public void setColdBiomesBuff(boolean coldBiomesBuff) {
        this.coldBiomesBuff = coldBiomesBuff;
    }

    public void setNightBuff(boolean nightBuff) {
        this.nightBuff = nightBuff;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public double getBuffFactor() {
        return buffFactor;
    }

    public double getRadius() {
        return radius;
    }

    public boolean isNightBuff() {
        return nightBuff;
    }
}
