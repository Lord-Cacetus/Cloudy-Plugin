package me.kaketuz.cloudy.abilities.steam.combos;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager;
import com.projectkorra.projectkorra.ability.util.ComboUtil;
import me.kaketuz.cloudy.Cloudy;
import me.kaketuz.cloudy.abilities.steam.util.SteamFlow;
import me.kaketuz.cloudy.abilities.sub.SteamAbility;
import me.kaketuz.cloudy.util.Methods;
import me.kaketuz.cloudy.util.Particles;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class FogRun extends SteamAbility implements AddonAbility, ComboAbility {

    //Katara is Jesus bruh

    private final Set<SteamFlow> flows = new HashSet<>();

    private long duration, cooldown, flowDuration;
    private double speed, angle;

    private boolean removeIfHitWall, endFade;

    private Vector direction;

    public FogRun(Player player) {
        super(player);
        if (!bPlayer.canBendIgnoreBinds(this)) return;

        duration = Cloudy.config.getLong("Steam.Combo.FogRun.Duration");
        flowDuration = Cloudy.config.getLong("Steam.Combo.FogRun.FlowDuration");
        cooldown = Cloudy.config.getLong("Steam.Combo.FogRun.Cooldown");
        angle = Cloudy.config.getDouble("Steam.Combo.FogRun.Angle");
        speed = Cloudy.config.getDouble("Steam.Combo.FogRun.Speed");
        removeIfHitWall = Cloudy.config.getBoolean("Steam.Combo.FogRun.RemoveIfHitWall");
        endFade = Cloudy.config.getBoolean("Steam.Combo.FogRun.EndFade");

        Block b = player.getLocation().getBlock().getRelative(BlockFace.DOWN, 1);

        if (!isWater(b) && !isIce(b)) return;

        direction = player.getLocation().getDirection().setY(0).normalize().multiply(speed);

        start();
    }

    @Override
    public void progress() {
        if (player.isDead() || !player.isOnline() || !bPlayer.canBendIgnoreBinds(this)) {
            bPlayer.addCooldown(this);
            remove();
        }

        Block b1 = player.getLocation().getBlock().getRelative(BlockFace.DOWN, 1);

        if (!isWater(b1) && !isIce(b1)) {
            bPlayer.addCooldown(this);
            remove();
        }

            Location loc = ThreadLocalRandom.current().nextBoolean()?
                    GeneralMethods.getLeftSide(player.getLocation().add(0, 0.5, 0), 1):
                    GeneralMethods.getRightSide(player.getLocation().add(0, 0.5, 0), 1);
            loc.add(Methods.getRandom().multiply(ThreadLocalRandom.current().nextDouble(0, 2)));
            flows.add(new SteamFlow(loc, direction.clone().multiply(-1), speed, flowDuration, true, 0));

        flows.removeIf(SteamFlow::isCancelled);

        Particles.spawnParticle(GeneralMethods.getMCVersion() >= 1205 ? Particle.valueOf("FISHING") : Particle.WATER_WAKE, player.getLocation(), 10, 0.5, 0, 0.5, 0.1);
        Block topBlock = GeneralMethods.getTopBlock(this.player.getLocation(), 3);
        direction.add(player.getEyeLocation().getDirection().normalize().multiply(angle)).normalize().multiply(speed);
        double waterHeight = (double)topBlock.getY() + 1.2;
        double playerHeight = this.player.getLocation().getY();
        double displacement = waterHeight - playerHeight;
        direction.setY(displacement * 0.5);
        player.setVelocity(direction);

        if (System.currentTimeMillis() > getStartTime() + duration) {
            if (!endFade) {
                bPlayer.addCooldown(this);
                remove();
            }
            else {
                speed -= 0.05;
                if (speed <= 0) {
                    bPlayer.addCooldown(this);
                    remove();
                }
            }
        }
        if (removeIfHitWall) {
            RayTraceResult result2 = player.getWorld().rayTraceBlocks(player.getEyeLocation(), direction, speed / 2, FluidCollisionMode.NEVER, true);

            Optional.ofNullable(result2)
                    .map(RayTraceResult::getHitBlock)
                    .ifPresent(b -> {
                        if (!b.isPassable()) {
                            bPlayer.addCooldown(this);
                            remove();
                        }
                    });
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

    //FrogRun
    @Override
    public String getName() {
        return "FogRun";
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
        return new FogRun(player);
    }

    @Override
    public ArrayList<ComboManager.AbilityInformation> getCombination() {
        return ComboUtil.generateCombinationFromList(this, Cloudy.config.getStringList("Steam.Combo.FogRun.Combination"));
    }

    @Override
    public boolean isEnabled() {
        return Cloudy.config.getBoolean("Steam.Combo.FogRun.Enabled");
    }

    @Override
    public String getDescription() {
        return Cloudy.config.getString("Steam.Combo.FogRun.Description");
    }

    @Override
    public String getInstructions() {
        return Cloudy.config.getString("Steam.Combo.FogRun.Instructions");
    }

    public long getDuration() {
        return duration;
    }

    public Set<SteamFlow> getFlows() {
        return flows;
    }

    public double getSpeed() {
        return speed;
    }

    public double getAngle() {
        return angle;
    }

    public Vector getDirection() {
        return direction;
    }

    public long getFlowDuration() {
        return flowDuration;
    }

    public boolean isEndFade() {
        return endFade;
    }

    public boolean isRemoveIfHitWall() {
        return removeIfHitWall;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    public void setDirection(Vector direction) {
        this.direction = direction;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setEndFade(boolean endFade) {
        this.endFade = endFade;
    }

    public void setFlowDuration(long flowDuration) {
        this.flowDuration = flowDuration;
    }

    public void setRemoveIfHitWall(boolean removeIfHitWall) {
        this.removeIfHitWall = removeIfHitWall;
    }

}
