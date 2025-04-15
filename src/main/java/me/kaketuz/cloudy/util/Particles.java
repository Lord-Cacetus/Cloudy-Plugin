package me.kaketuz.cloudy.util;

import org.bukkit.Location;
import org.bukkit.Particle;

import java.util.Objects;

// TODO: сделать поддержку версий от 1.12 до 1.21.5

//IM
public class Particles {
    public static void spawnParticle(Particle particle, Location location, int amount, double xOffset, double yOffset, double zOffset, double speed) {
        Objects.requireNonNull(location.getWorld()).spawnParticle(particle, location, amount, xOffset, yOffset, zOffset, speed);
    }
    public static void spawnParticle(Particle particle, Location location, int amount, double xOffset, double yOffset, double zOffset, double speed, Object obj) {
        Objects.requireNonNull(location.getWorld()).spawnParticle(particle, location, amount, xOffset, yOffset, zOffset, speed, obj);
    }
    public static void spawnParticle(Particle particle, Location location, int amount, double xOffset, double yOffset, double zOffset) {
        Objects.requireNonNull(location.getWorld()).spawnParticle(particle, location, amount, xOffset, yOffset, zOffset);
    }
    public static void spawnParticle(Particle particle, Location location, int amount) {
        Objects.requireNonNull(location.getWorld()).spawnParticle(particle, location, amount);
    }

    //A STEEVE
}
