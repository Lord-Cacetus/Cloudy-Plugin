package me.kaketuz.cloudy.abilities.steam.util;

import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.waterbending.SurgeWave;
import com.projectkorra.projectkorra.waterbending.Torrent;
import com.projectkorra.projectkorra.waterbending.combo.IceWave;
import com.projectkorra.projectkorra.waterbending.ice.PhaseChange;
import com.projectkorra.projectkorra.waterbending.multiabilities.WaterArmsSpear;
import me.kaketuz.cloudy.Cloudy;
import me.kaketuz.cloudy.abilities.steam.CloudStream;
import me.kaketuz.cloudy.abilities.sub.SteamAbility;
import me.kaketuz.cloudy.util.Methods;
import me.kaketuz.cloudy.util.Particles;
import me.kaketuz.cloudy.util.Sounds;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class CloudStreamGeyser extends SteamAbility implements AddonAbility, ComboAbility {

    private double range, speed, damage, uppercutPower, collisionRadius, sourceRange;
    private long cooldown;

    private Location origin, location;
    private Vector direction;

    private final Set<Entity> affected = new HashSet<>();

    public CloudStreamGeyser(Player player) {
        super(player);
        if (!bPlayer.canBendIgnoreBinds(this) || hasAbility(player, CloudStream.class) || hasAbility(player, CloudStreamGeyser.class)) return;

        if (bPlayer.isOnCooldown("CloudStreamGeyser")) return;
        if (player.isSneaking()) return;

        range = Cloudy.config.getDouble("Steam.CloudStream.Geyser.Range");
        speed = Cloudy.config.getDouble("Steam.CloudStream.Geyser.Speed");
        damage = Cloudy.config.getDouble("Steam.CloudStream.Geyser.Damage");
        uppercutPower = Cloudy.config.getDouble("Steam.CloudStream.Geyser.UppercutPower");
        collisionRadius = Cloudy.config.getDouble("Steam.CloudStream.Geyser.CollisionRadius");
        sourceRange = Cloudy.config.getDouble("Steam.CloudStream.Geyser.SourceRange");
        cooldown = Cloudy.config.getLong("Steam.CloudStream.Geyser.Cooldown");

        Block source = Methods.getLookingAt(player, sourceRange, false);

        if (!isWater(source) && !isIce(source)) return;

        if (isIce(source)) {
            if (Torrent.canThaw(source)) Torrent.thaw(source);
            else if (IceWave.canThaw(source)) IceWave.thaw(source);
            else if (SurgeWave.canThaw(source)) SurgeWave.thaw(source);
            else if (WaterArmsSpear.canThaw(source)) WaterArmsSpear.thaw(source);
            else if (!PhaseChange.thaw(source)) source.setType(Material.WATER);
        }

        origin = source.getLocation().clone();
        location = origin.clone();
        direction = new Vector(0, 1, 0).normalize().multiply(speed);

        start();
    }

    @Override
    public void progress() {

        Sounds.playSound(location, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.2f, 1);

        location = location.add(direction);

        RayTraceResult result = player.getWorld().rayTrace(location, direction, speed, FluidCollisionMode.NEVER, true, collisionRadius, e -> e instanceof LivingEntity && !e.getUniqueId().equals(player.getUniqueId()));

        if (result != null) {
            if (result.getHitBlock() != null) {
                bPlayer.addCooldown(this, cooldown);
                remove();
            }
            if (result.getHitEntity() != null) {
                Entity e = result.getHitEntity();
                e.setVelocity(new Vector(0, 1, 0).normalize().multiply(uppercutPower));
                if (!affected.contains(e)) {
                    DamageHandler.damageEntity(e, player, damage, CoreAbility.getAbility(CloudStreamGeyser.class));
                    affected.add(e);
                }
            }
        }

        for (int i = 0; i < 7; i++) {
            Vector rv = Methods.getRandom().multiply(ThreadLocalRandom.current().nextDouble(0, collisionRadius));
            Particles.spawnParticle(Particle.CLOUD, location.clone().add(rv), 0, rv.getX(), rv.getY() + 1, rv.getZ(), 0.3);
        }

        if (origin.distance(location) >= range) {
            bPlayer.addCooldown(this, cooldown);
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
        return "CloudStreamGeyser";
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
        return null;
    }

    @Override
    public ArrayList<ComboManager.AbilityInformation> getCombination() {
        return null;
    }

    @Override
    public String getInstructions() {
        return Cloudy.config.getString("Steam.CloudStream.Geyser.Instructions");
    }

    @Override
    public String getDescription() {
        return Cloudy.config.getString("Steam.CloudStream.Geyser.Description");
    }

    @Override
    public boolean isEnabled() {
        return Cloudy.config.getBoolean("Steam.CloudStream.Geyser.Enabled");
    }

    public double getSourceRange() {
        return sourceRange;
    }

    public double getDamage() {
        return damage;
    }

    public double getSpeed() {
        return speed;
    }

    public double getRange() {
        return range;
    }

    public double getUppercutPower() {
        return uppercutPower;
    }

    public Vector getDirection() {
        return direction;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    public void setSourceRange(double sourceRange) {
        this.sourceRange = sourceRange;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public void setRange(double range) {
        this.range = range;
    }

    public void setUppercutPower(double uppercutPower) {
        this.uppercutPower = uppercutPower;
    }
}
