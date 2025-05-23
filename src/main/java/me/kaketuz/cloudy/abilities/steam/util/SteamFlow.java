package me.kaketuz.cloudy.abilities.steam.util;


import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.waterbending.SurgeWave;
import com.projectkorra.projectkorra.waterbending.Torrent;
import com.projectkorra.projectkorra.waterbending.combo.IceWave;
import com.projectkorra.projectkorra.waterbending.ice.PhaseChange;
import com.projectkorra.projectkorra.waterbending.multiabilities.WaterArmsSpear;
import me.kaketuz.cloudy.Cloudy;
import me.kaketuz.cloudy.util.Methods;
import me.kaketuz.cloudy.util.Particles;
import me.kaketuz.cloudy.util.Sounds;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.SplittableRandom;


public class SteamFlow extends BukkitRunnable {

        private Location center;
        private final long start, duration, fadeDuration;
        private Vector direction;
        private final Vector fadeVec;
        private final double speed;
        private Location location;
        private boolean isReturning;

        private final boolean free;

        private boolean hitGround;

        private final double power;

        public SteamFlow(Location center, Vector direction, double speed, long duration, boolean free, double power) {
            SplittableRandom random = new SplittableRandom();
            this.center = center;
            this.duration = duration;
            this.power = power;
            this.free = free;
            Optional.ofNullable(direction)
                    .ifPresentOrElse(
                            d -> this.direction = d.clone().rotateAroundAxis(center.getDirection(), Math.toRadians(random.nextDouble(0, 10))),
                            () -> this.direction = Methods.getRandom());
            this.start = System.currentTimeMillis();
            this.fadeDuration = random.nextLong(250, 750);
            this.fadeVec = this.direction.clone().add(Methods.getRandom().multiply(0.3));

            this.speed = speed;
            this.direction.normalize().multiply(speed);
            this.location = this.center.clone();
            this.runTaskTimer(Cloudy.plugin, 1L, 0);
        }

        public void setCenter(Location center) {
            if (free) return;
            this.center = center;
        }


        private void reflect(Block hitBlock) {
            Vector newDir = direction.clone();

            ArrayList<Block> blocks = new ArrayList<>(0);
            blocks.add(hitBlock.getRelative(BlockFace.DOWN));
            blocks.add(hitBlock.getRelative(BlockFace.UP));
            blocks.add(hitBlock.getRelative(BlockFace.NORTH));
            blocks.add(hitBlock.getRelative(BlockFace.SOUTH));
            blocks.add(hitBlock.getRelative(BlockFace.EAST));
            blocks.add(hitBlock.getRelative(BlockFace.WEST));
            Vector tmpDirection = this.location.getDirection().clone().multiply(0.1D);
            Location tmpLocation = this.location.clone().subtract(tmpDirection);
            while (tmpLocation.getBlock().getLocation().equals(hitBlock.getLocation()))
                tmpLocation.subtract(tmpDirection);
            int i = 0;
            for (Block b : blocks) {
                if (tmpLocation.getBlock().getLocation().equals(b.getLocation()))
                    break;
                i++;
            }
            if ((((i == 0) ? 1 : 0) | ((i == 1) ? 1 : 0)) != 0) {
                newDir.setY(newDir.getY() * -1.0D);
            } else if ((((i == 2) ? 1 : 0) | ((i == 3) ? 1 : 0)) != 0) {
                newDir.setZ(newDir.getZ() * -1.0D);
            } else {
                newDir.setX(newDir.getX() * -1.0D);
            }

            direction = Vector.fromJOML(direction.toVector3d().reflect(newDir.toVector3d()));
        }

        @Override
        public void run() {
            location = location.add(direction);
            direction.add(fadeVec.clone().multiply(0.01));
            direction.normalize().multiply(speed);
            Sounds.playSound(location, Sound.ENTITY_PHANTOM_FLAP, 0.06f, 0.75f);
            for (int i = 0; i < 2; i++) {
                Vector rv = Methods.getRandom().multiply(0.3);
                ParticleEffect.CLOUD.display(location.clone().add(rv), 0, direction.getX(), direction.getY(), direction.getZ(), System.currentTimeMillis() <= start + duration ? 0.3 : 0.1);
            }
            if (hitGround) isReturning = false;

            if (System.currentTimeMillis() <= start + duration) {
                if (location.distance(center) > 10 && !hitGround && !isReturning && !free) {
                    isReturning = true;
                }
                if (isReturning) {

                    direction.add(GeneralMethods.getDirection(location, center).normalize().multiply(location.distance(center) / 50));
                    if (location.distance(center) < 0.2) {
                        isReturning = false;
                    }
                }
            }
            else if (System.currentTimeMillis() <= start + duration + fadeDuration) {
                direction.add(fadeVec.normalize().multiply(0.2));
            }
            else cancel();

            if (GeneralMethods.isSolid(location.getBlock())) cancel();

            GeneralMethods.getEntitiesAroundPoint(location, 0.4).forEach(e -> {
                if (power != 0) e.setVelocity(direction.clone().multiply(power));
            });

            RayTraceResult result = Objects.requireNonNull(location.getWorld()).rayTraceBlocks(location, direction, speed * 2, FluidCollisionMode.ALWAYS, true);

            Optional.ofNullable(result)
                    .map(RayTraceResult::getHitBlock)
                    .ifPresent(b -> {
                        if (ElementalAbility.isWater(b)) {
                            hitGround = true;
                            isReturning = false;
                            Particles.spawnParticle(GeneralMethods.getMCVersion() >= 1205 ? Particle.valueOf("WATER_BUBBLE") : Particle.BUBBLE, location, 10, 0.5, 0.5, 0.5, 0.1);
                            reflect(b);
                        }
                        if (!ElementalAbility.isAir(b.getType())) {

                            hitGround = true;
                            isReturning = false;
                            reflect(b);

                            if (ElementalAbility.isIce(b)) {
                                if (Torrent.canThaw(b)) Torrent.thaw(b);
                                else if (IceWave.canThaw(b)) IceWave.thaw(b);
                                else if (SurgeWave.canThaw(b)) SurgeWave.thaw(b);
                                else if (WaterArmsSpear.canThaw(b)) WaterArmsSpear.thaw(b);
                                else if (!PhaseChange.thaw(b)) b.setType(Material.WATER);
                            }
                            if (ElementalAbility.isSnow(b)) {
                                new TempBlock(b, Material.AIR);
                            }
                        }
                    });
        }
    }


