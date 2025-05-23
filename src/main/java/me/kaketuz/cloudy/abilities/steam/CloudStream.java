package me.kaketuz.cloudy.abilities.steam;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import me.kaketuz.cloudy.Cloudy;
import me.kaketuz.cloudy.abilities.steam.util.SteamFlow;
import me.kaketuz.cloudy.abilities.sub.SteamAbility;
import me.kaketuz.cloudy.util.Methods;
import me.kaketuz.cloudy.util.Particles;
import me.kaketuz.cloudy.util.Sounds;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

public class CloudStream extends SteamAbility implements AddonAbility {

    private double radius, pushPower, range, speed, sourceRange;
    private long cooldown, flowDuration;

    private int flowAmount;

    private boolean isLaunched;

    private Location origin, location;
    private Vector direction;

    private boolean coldBiomesBuff, nightBuff;
    private double buffFactor;

    public CloudStream(Player player, boolean instant) {
        super(player);
        if (!bPlayer.canBendIgnoreBinds(this) || hasAbility(player, CloudStream.class)) return;

        radius = Cloudy.config.getDouble("Steam.CloudStream.Radius");
        pushPower = Cloudy.config.getDouble("Steam.CloudStream.PushPower");
        range = Cloudy.config.getDouble("Steam.CloudStream.Range");
        speed = Cloudy.config.getDouble("Steam.CloudStream.Speed");
        sourceRange = Cloudy.config.getDouble("Steam.CloudStream.SourceRange");
        cooldown = Cloudy.config.getLong("Steam.CloudStream.Cooldown");
        flowAmount = Cloudy.config.getInt("Steam.CloudStream.FlowAmount");
        flowDuration = Cloudy.config.getLong("Steam.CloudStream.FlowDuration");
        coldBiomesBuff = Cloudy.config.getBoolean("Steam.CloudFission.ColdBiomesBuff");
        nightBuff = Cloudy.config.getBoolean("Steam.CloudFission.NightBuff");
        buffFactor = Cloudy.config.getDouble("Steam.CloudFission.BuffFactor");

        if (coldBiomesBuff && Methods.getTemperature(player.getLocation()) <= 0) {
            radius *= buffFactor;
            pushPower *= buffFactor;
            range *= buffFactor;
            speed *= buffFactor;
            sourceRange *= buffFactor;
            flowAmount *= (int) buffFactor;
            flowDuration *= (long) buffFactor;
        }
        if (nightBuff && isNight(player.getWorld())) {
            radius *= buffFactor;
            pushPower *= buffFactor;
            range *= buffFactor;
            speed *= buffFactor;
            sourceRange *= buffFactor;
            flowAmount *= (int) buffFactor;
            flowDuration *= (long) buffFactor;
        }

        if (!instant) {
            Block b = Methods.getLookingAt(player, sourceRange, false);

            if (b == null) return;

            if (!isWater(b) && !isIce(b)) return;

            origin = b.getLocation().add(0, 1, 0);
            location = origin.clone();
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
        if (player.isDead() || !player.isOnline() || !bPlayer.canBendIgnoreBinds(this)) {
            bPlayer.addCooldown(this);
            remove();
        }

        if (!isLaunched) {
            if (!player.isSneaking() || !bPlayer.getBoundAbilityName().equals(getName())) {
                bPlayer.addCooldown(this);
                remove();
            }
            for (int i = 0; i < 10; i++) {
                Vector rv = Methods.getRandom();
                Vector rv2 = Methods.getRandom().setY(0).normalize().multiply(2);
                Particles.spawnParticle(GeneralMethods.getMCVersion() >= 1205 ? Particle.valueOf("WATER_WAKE") : Particle.FISHING, origin.clone().add(rv2), 0, -rv.getX(), 0, -rv.getZ(), 0.2);
                Sounds.playSound(origin, Sound.ENTITY_BOAT_PADDLE_WATER, 0.15f, 0);
                Particles.spawnParticle(Particle.CLOUD, origin.clone().subtract(0, 1, 0).add(rv), 0, rv.getX(), 0.1, rv.getZ(), 0.3);
                Particles.spawnParticle(GeneralMethods.getMCVersion() >= 1205 ? Particle.valueOf("WATER_BUBBLE") : Particle.BUBBLE, origin.clone().subtract(0, 1, 0).add(rv), 0, rv.getX(), 0, rv.getZ(), 0.3);
            }
        }
        else {
            location = location.add(direction);
            for (int i = 0; i < flowAmount; i++) {
                Vector rv = Methods.getRandom().multiply(ThreadLocalRandom.current().nextDouble(0, radius));
                new SteamFlow(location.clone().add(rv), direction, speed, flowDuration, true, pushPower);
            }

            GeneralMethods.getEntitiesAroundPoint(location, radius)
                    .forEach(e -> e.setVelocity(direction.clone().multiply(pushPower)));

            if (origin.distance(location) >= range) {
                bPlayer.addCooldown(this);
                remove();
            }
        }
    }

    public void launch() {
        direction = player.getLocation().getDirection();
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
        return "CloudStream";
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
        return Cloudy.config.getBoolean("Steam.CloudStream.Enabled");
    }

    @Override
    public String getDescription() {
        return Cloudy.config.getString("Steam.CloudStream.Description") + "\n\n Geyser: " + Cloudy.config.getString("Steam.CloudStream.Geyser.Description");
    }
    @Override
    public String getInstructions() {
        return Cloudy.config.getString("Steam.CloudStream.Instructions") + "\n\n Geyser: " + Cloudy.config.getString("Steam.CloudStream.Geyser.Instructions");
    }

    public void setRange(double range) {
        this.range = range;
    }

    public double getRadius() {
        return radius;
    }

    public double getRange() {
        return range;
    }

    public double getSpeed() {
        return speed;
    }

    public Vector getDirection() {
        return direction;
    }

    public double getPushPower() {
        return pushPower;
    }

    public double getSourceRange() {
        return sourceRange;
    }

    public Location getOrigin() {
        return origin;
    }

    public long getFlowDuration() {
        return flowDuration;
    }

    public int getFlowAmount() {
        return flowAmount;
    }

    public boolean isLaunched() {
        return isLaunched;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public void setLaunched(boolean launched) {
        isLaunched = launched;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public void setPushPower(double pushPower) {
        this.pushPower = pushPower;
    }

    public void setDirection(Vector direction) {
        this.direction = direction;
    }

    public void setFlowDuration(long flowDuration) {
        this.flowDuration = flowDuration;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setFlowAmount(int flowAmount) {
        this.flowAmount = flowAmount;
    }

    public void setOrigin(Location origin) {
        this.origin = origin;
    }

    public void setSourceRange(double sourceRange) {
        this.sourceRange = sourceRange;
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

    public boolean isColdBiomesBuff() {
        return coldBiomesBuff;
    }
}
