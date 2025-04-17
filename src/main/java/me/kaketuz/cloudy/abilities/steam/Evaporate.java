package me.kaketuz.cloudy.abilities.steam;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.waterbending.SurgeWave;
import com.projectkorra.projectkorra.waterbending.Torrent;
import com.projectkorra.projectkorra.waterbending.combo.IceWave;
import com.projectkorra.projectkorra.waterbending.ice.PhaseChange;
import com.projectkorra.projectkorra.waterbending.multiabilities.WaterArmsSpear;
import com.projectkorra.projectkorra.waterbending.passive.FastSwim;
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
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.Objects;

public class Evaporate extends SteamAbility implements AddonAbility {


    private long cooldown;
    private double sourceRange;
    private boolean coldBiomesBuff, nightBuff;
    private double buffFactor;

    public Evaporate(Player player) {
        super(player);
        if (!bPlayer.canBendIgnoreBinds(this)) return;


        cooldown = Cloudy.config.getLong("Steam.Evaporate.Cooldown");
        sourceRange = Cloudy.config.getDouble("Steam.Evaporate.SourceRange");
        coldBiomesBuff = Cloudy.config.getBoolean("Steam.Evaporate.ColdBiomesBuff");
        nightBuff = Cloudy.config.getBoolean("Steam.Evaporate.NightBuff");
        buffFactor = Cloudy.config.getDouble("Steam.Evaporate.BuffFactor");

        if (coldBiomesBuff && Methods.getTemperature(player.getLocation()) <= 0) {
            sourceRange *= buffFactor;
        }
        if (nightBuff && isNight(player.getWorld())) {
            sourceRange *= buffFactor;
        }

        Block pLoc = player.getLocation().getBlock().getRelative(BlockFace.DOWN, 1);
        boolean onClouds = false;

        for (int i = -1; i < 5; i++) {
            if (pLoc.getLocation().getBlockY() == 190 + i) {
                onClouds = true;
                break;
            }
        }

        if (!onClouds) {
            Block source = Methods.getLookingAt(player, sourceRange, false);

            if (source == null) return;

            if (!isWater(source) && !source.getType().equals(Material.WATER_CAULDRON) && !isIce(source) && !source.getType().equals(Material.WET_SPONGE))
                return;

            if (isIce(source)) {

                if (Torrent.canThaw(source)) Torrent.thaw(source);
                else if (IceWave.canThaw(source)) IceWave.thaw(source);
                else if (SurgeWave.canThaw(source)) SurgeWave.thaw(source);
                else if (WaterArmsSpear.canThaw(source)) WaterArmsSpear.thaw(source);
                else if (!PhaseChange.thaw(source)) source.setType(Material.WATER);
                Sounds.playSound(source.getLocation(), Sound.BLOCK_GLASS_BREAK, 0.3f, 1.5f);

                Particles.spawnParticle(GeneralMethods.getMCVersion() >= 1205 ? Particle.valueOf("BLOCK") : Particle.BLOCK_CRACK, source.getLocation(), 20, 0.5, 0.5, 0.5, 0, Material.ICE.createBlockData());
            }
            Particles.spawnParticle(GeneralMethods.getMCVersion() >= 1205 ? Particle.valueOf("SPLASH") : Particle.WATER_SPLASH, source.getLocation(), 20, 0.5, 0.5, 0.5, 0.1);
            Particles.spawnParticle(Particle.BUBBLE_POP, source.getLocation(), 10, 0.5, 0.5, 0.5, 0.1);
            Particles.spawnParticle(GeneralMethods.getMCVersion() >= 1205 ? Particle.valueOf("BUBBLE") : Particle.WATER_BUBBLE, source.getLocation(), 20, 0.5, 0.5, 0.5, 0.1);
            Sounds.playSound(source.getLocation(), Sound.BLOCK_BUBBLE_COLUMN_UPWARDS_AMBIENT, 0.3f, 2f);
            Sounds.playSound(source.getLocation(), Sound.ENTITY_BOAT_PADDLE_WATER, 0.5f, 0f);
            Sounds.playSound(source.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.25f, 1);

            Methods.removeWaterFromCauldron(source); //Nothing happens if source isn't water cauldron
            Methods.drySponge(source); //Same :3

            if (Cloud.isLake(source)) Objects.requireNonNull(Cloud.getLake(source)).revertBlock();


            Cloud.createCloud(source.getLocation(), player);
        }
        else Cloud.createCloud(GeneralMethods.getTargetedLocation(player, 2), player);

        if (hasAbility(player, FastSwim.class)) getAbility(player, FastSwim.class).remove();
        bPlayer.addCooldown(this);
        start();
    }

    @Override
    public void progress() {
        remove();
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
        return "Evaporate";
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
        return Cloudy.config.getBoolean("Steam.Evaporate.Enabled");
    }

    @Override
    public String getDescription() {
        return Cloudy.config.getString("Steam.Evaporate.Description");
    }

    @Override
    public String getInstructions() {
        return Cloudy.config.getString("Steam.Evaporate.Instructions");
    }

    public double getSourceRange() {
        return sourceRange;
    }

    public void setSourceRange(double sourceRange) {
        this.sourceRange = sourceRange;
    }
}

