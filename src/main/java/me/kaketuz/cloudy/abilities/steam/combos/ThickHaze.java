package me.kaketuz.cloudy.abilities.steam.combos;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager;
import com.projectkorra.projectkorra.ability.util.ComboUtil;
import com.projectkorra.projectkorra.util.ParticleEffect;
import me.kaketuz.cloudy.Cloudy;
import me.kaketuz.cloudy.Configuration;
import me.kaketuz.cloudy.abilities.steam.util.Cloud;
import me.kaketuz.cloudy.abilities.sub.SteamAbility;
import me.kaketuz.cloudy.util.Methods;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Panda;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class ThickHaze extends SteamAbility implements AddonAbility, ComboAbility {

    private double radius, speed, yFactor, sourceRadius, settlementSpeed, currRad, currYF;
    private long duration, cooldown, startMistTiming;
    private int maxClouds, freezeSpeed;
    private List<Location> locations = new ArrayList<>();
    private List<Cloud> clouds = new ArrayList<>();

    private boolean grounded;

    public ThickHaze(Player player) {
        super(player);

        if (!bPlayer.canBendIgnoreBinds(this) || hasAbility(player, ThickHaze.class)) return;

        radius = Cloudy.config.getDouble("Steam.Combo.ThickHaze.Radius");
        speed = Cloudy.config.getDouble("Steam.Combo.ThickHaze.Speed");
        yFactor = Cloudy.config.getDouble("Steam.Combo.ThickHaze.Height");
        sourceRadius = Cloudy.config.getDouble("Steam.Combo.ThickHaze.SourceRadius");
        settlementSpeed = Cloudy.config.getDouble("Steam.Combo.ThickHaze.SettlementSpeed");
        maxClouds = Cloudy.config.getInt("Steam.Combo.ThickHaze.MaxClouds");
        freezeSpeed = Cloudy.config.getInt("Steam.Combo.ThickHaze.FreezeSpeed");
        duration = Cloudy.config.getLong("Steam.Combo.ThickHaze.Duration");
        cooldown = Cloudy.config.getLong("Steam.Combo.ThickHaze.Cooldown");


        clouds.addAll(Cloud.getCloudsAroundPoint(player.getEyeLocation(), sourceRadius).stream()
                .takeWhile(c -> !c.isUsing() && !c.isHidden())
                .limit(maxClouds)
                .toList());

        if (clouds.isEmpty()) return;


        start();
    }

    @Override
    public void progress() {
        if (player.isDead() || !player.isOnline() || !bPlayer.canBendIgnoreBinds(this)) {

            bPlayer.addCooldown(this);
            remove();
        }


        
        if (!grounded) {
            final Boolean[] ready = new Boolean[clouds.size()];
            Arrays.fill(ready, false);

            for (int i = clouds.size() - 1; i >= 0; i--) {
                clouds.get(i).move(new Vector(0, -settlementSpeed, 0));
                if (GeneralMethods.isSolid(clouds.get(i).getLocation().getBlock().getRelative(BlockFace.DOWN, 1))) ready[i] = true;

                if (clouds.get(i).getLocation().distance(player.getLocation()) > 100) {
                    clouds.remove(i);
                    ready[i] = true;
                }

            }

            startMistTiming = System.currentTimeMillis();
            grounded = Arrays.stream(ready).allMatch(Boolean::booleanValue);
        }
        else {
            if (currRad < radius) currRad += speed;
            else currRad = radius;

            if (currYF < yFactor) currYF += speed / 2;
            else currYF = yFactor;
            if (System.currentTimeMillis() > startMistTiming + duration) {

                bPlayer.addCooldown(this);
                remove();
            }
            if (!clouds.isEmpty()) {
                locations.addAll(clouds.stream()
                        .map(Cloud::getLocation)
                        .toList());
                clouds.forEach(c -> c.remove(true));
                clouds.clear();
            }

            locations.forEach(l -> {
                for (int i = 0; i < 5 * currRad; i++) {

                    Vector dest = Methods.getEllipseIgnoreY(currYF).multiply(ThreadLocalRandom.current().nextDouble(0, currRad));
                    Vector rot = new Vector(dest.getZ(), 1, -dest.getX());
                    Location target = l.clone().add(dest);
                    ParticleEffect.CLOUD.display(target, 0, dest.getX() + rot.getX(), dest.getY(), dest.getZ() + rot.getZ(), 0.01);
                }

                GeneralMethods.getEntitiesAroundPoint(l, currRad, e -> e instanceof LivingEntity).stream()
                        .map(e -> ((LivingEntity)e))
                        .forEach(e -> {
                            if (!e.getUniqueId().equals(player.getUniqueId())) {
                                e.setFreezeTicks(e.getFreezeTicks() + freezeSpeed);
                                e.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0, true, false, false));
                            }
                            e.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20, 0, true, false, false));
                        });
            });




            if (locations.isEmpty()) {
                bPlayer.addCooldown(this);
                remove();
            }
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
        return "ThickHaze";
    }

    @Override
    public Location getLocation() {
        return null;
    }

    @Override
    public List<Location> getLocations() {
        return locations;
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
        return new ThickHaze(player);
    }

    @Override
    public ArrayList<ComboManager.AbilityInformation> getCombination() {
        return ComboUtil.generateCombinationFromList(this, Cloudy.config.getStringList("Steam.Combo.ThickHaze.Combination"));
    }

    @Override
    public String getDescription() {
        return Cloudy.config.getString("Steam.Combo.ThickHaze.Description");
    }

    @Override
    public String getInstructions() {
        return Cloudy.config.getString("Steam.Combo.ThickHaze.Instructions");
    }

    public double getRadius() {
        return radius;
    }

    public double getCurrRad() {
        return currRad;
    }

    public long getDuration() {
        return duration;
    }

    public double getCurrYF() {
        return currYF;
    }

    public double getSettlementSpeed() {
        return settlementSpeed;
    }

    public double getSourceRadius() {
        return sourceRadius;
    }

    public double getSpeed() {
        return speed;
    }

    public double getyFactor() {
        return yFactor;
    }

    public int getMaxClouds() {
        return maxClouds;
    }

    public List<Cloud> getClouds() {
        return clouds;
    }

    public long getStartMistTiming() {
        return startMistTiming;
    }

    public boolean isGrounded() {
        return grounded;
    }

    public void setMaxClouds(int maxClouds) {
        this.maxClouds = maxClouds;
    }

    public void setCooldown(long cooldown) {
        this.cooldown = cooldown;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public void setCurrRad(double currRad) {
        this.currRad = currRad;
    }

    public void setCurrYF(double currYF) {
        this.currYF = currYF;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setSettlementSpeed(double settlementSpeed) {
        this.settlementSpeed = settlementSpeed;
    }

    public void setSourceRadius(double sourceRadius) {
        this.sourceRadius = sourceRadius;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public void setStartMistTiming(long startMistTiming) {
        this.startMistTiming = startMistTiming;
    }

    public void setyFactor(double yFactor) {
        this.yFactor = yFactor;
    }

    public void setClouds(List<Cloud> clouds) {
        this.clouds = clouds;
    }

    public void setGrounded(boolean grounded) {
        this.grounded = grounded;
    }

    public void setLocations(List<Location> locations) {
        this.locations = locations;
    }
}
