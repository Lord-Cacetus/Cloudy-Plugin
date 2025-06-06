package me.kaketuz.cloudy.util;

import com.projectkorra.projectkorra.GeneralMethods;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Methods {


    public static void removeWaterFromCauldron(Block block) {
        if (!block.getType().equals(Material.WATER_CAULDRON)) return;
        BlockData dat = block.getBlockData();
        if (((Levelled) dat).getLevel() - 1 == 0) {
            block.setType(Material.CAULDRON);
        }
        else {
            ((Levelled) dat).setLevel(((Levelled) dat).getLevel() - 1);
            block.setBlockData(dat);
        }
    }

    //Thx Dreig <3
    public static Vector getRandom() {
        double pitch = ThreadLocalRandom.current().nextDouble(-90.0, 90.0);
        double yaw = ThreadLocalRandom.current().nextDouble(-180.0, 180.0);
        return new Vector(-Math.cos(pitch) * Math.sin(yaw), -Math.sin(pitch), Math.cos(pitch) * Math.cos(yaw));
    }

    public static Vector getEllipseIgnoreY(double height) {
        double angle = ThreadLocalRandom.current().nextDouble(0, 2 * Math.PI);

        double radius = 1.0;

        double x = Math.cos(angle) * radius;
        double z = Math.sin(angle) * radius;

        return new Vector(x, ThreadLocalRandom.current().nextDouble(-height, height), z).normalize();
    }

    public static boolean isWarmBiome(Location loc) {
        Biome b = loc.getBlock().getBiome();
        return b.equals(Biome.BADLANDS) ||
                b.equals(Biome.DEEP_LUKEWARM_OCEAN) ||
                b.equals(Biome.LUKEWARM_OCEAN) ||
                b.equals(Biome.WARM_OCEAN) ||
                b.equals(Biome.DESERT) ||
                b.equals(Biome.ERODED_BADLANDS) ||
                b.equals(Biome.MANGROVE_SWAMP) ||
                b.equals(Biome.SAVANNA) ||
                b.equals(Biome.SAVANNA_PLATEAU) ||
                b.equals(Biome.WOODED_BADLANDS) ||
                b.equals(Biome.WINDSWEPT_SAVANNA) ||
                b.equals(Biome.CRIMSON_FOREST) ||
                b.equals(Biome.WARPED_FOREST) ||
                b.equals(Biome.NETHER_WASTES) ||
                b.equals(Biome.SOUL_SAND_VALLEY) ||
                b.equals(Biome.BASALT_DELTAS);
    }

    public static boolean isNetherBiome(Location loc) {
        Biome b = loc.getBlock().getBiome();
        return b.equals(Biome.CRIMSON_FOREST) ||
                b.equals(Biome.WARPED_FOREST) ||
                b.equals(Biome.NETHER_WASTES) ||
                b.equals(Biome.SOUL_SAND_VALLEY) ||
                b.equals(Biome.BASALT_DELTAS);
    }

    public static double getTemperature(Location location) {
        return Objects.requireNonNull(location.getWorld()).getTemperature(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }
    public static double getHumidity(Location location) {
        return Objects.requireNonNull(location.getWorld()).getHumidity(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public static boolean isFireMob(Entity e) {
        EntityType t = e.getType();

        return t == EntityType.BLAZE
                || t == EntityType.MAGMA_CUBE
                || t == EntityType.STRIDER
                || t == EntityType.WARDEN
                || t == EntityType.ZOMBIFIED_PIGLIN;
    }

    @Nullable
    public static Block getLookingAt(Player player, double distance, boolean ignoreLiquids) {

        RayTraceResult result = player.getWorld().rayTraceBlocks(
                player.getEyeLocation(), player.getLocation().getDirection(),
                distance, ignoreLiquids ? FluidCollisionMode.NEVER : FluidCollisionMode.ALWAYS, true);
        if (result == null) return null;
        return result.getHitBlock();
    }

    public static void drySponge(Block block) {
        if (!block.getType().equals(Material.WET_SPONGE)) return;

        block.setType(Material.SPONGE);
    }

    @Nullable
    public static Block getGround(Location loc, int height) {
        Block standingblock = loc.getBlock();
        for (int i = 0; i <= height + 5; i++) {
            Block block = standingblock.getRelative(BlockFace.DOWN, i);
            if (GeneralMethods.isSolid(block) || block.isLiquid()) {
                return block;
            }
        }
        return null;
    }






    public static void addSnowLayer(Block block) {
        if (block.getType() != Material.SNOW) return;
        BlockData dat = block.getBlockData();
        if (((Levelled) dat).getLevel() - 1 != 0) {
            ((Levelled) dat).setLevel(((Levelled) dat).getLevel() - 1);
            block.setBlockData(dat);
        }

    }
    private static final SplittableRandom rand = new SplittableRandom();
    public static boolean chance(double percent) {
        return rand.nextDouble(0, 100) <= percent;
    }

    public static boolean chanceConcurrent(double percent) {
        return ThreadLocalRandom.current().nextDouble(0, 100) <= percent;
    }

    public static Biome getBiome(Location location) {
        return location.getBlock().getBiome();
    }

    public static void knockback(Entity entity, Location from, double power) {
        entity.setVelocity(GeneralMethods.getDirection(from, entity.getLocation()).normalize().multiply(power));
    }

    public static void getEntities(Player player, Location centre, double rad, Consumer<Entity> todo) {
        GeneralMethods.getEntitiesAroundPoint(centre, rad).stream()
                .takeWhile(e -> e instanceof LivingEntity && !player.getUniqueId().equals(e.getUniqueId()))
                .forEach(todo);
    }

    public static Predicate<? super Entity> getEntityPredicator(Player player) {
        return e -> e instanceof LivingEntity && !player.getUniqueId().equals(e.getUniqueId());
    }
    public static <K, V> void removeIf(Map<K, V> map, BiPredicate<K, V> filter) {
        map.entrySet().removeIf(entry -> filter.test(entry.getKey(), entry.getValue()));
    }
    public static <K, V> void removeIf_concurrent(ConcurrentHashMap<K, V> map, BiPredicate<K, V> filter) {
        map.entrySet().removeIf(entry -> filter.test(entry.getKey(), entry.getValue()));
    }

    public static Location getCenter(Collection<Location> locations) {
        if (locations == null || locations.isEmpty()) {
            throw new IllegalArgumentException("Collection is empty!");
        }

        double sumX = 0, sumY = 0, sumZ = 0;
        int count = 0;
        String worldName = null;

        for (Location loc : locations) {
            if (worldName == null) {
                worldName = loc.getWorld().getName();
            } else if (!loc.getWorld().getName().equals(worldName)) {
                throw new IllegalArgumentException("All locations should be in the same world");
            }

            sumX += loc.getX();
            sumY += loc.getY();
            sumZ += loc.getZ();
            count++;
        }

        return new Location(
                locations.iterator().next().getWorld(),
                sumX / count,
                sumY / count,
                sumZ / count
        );
    }

}
