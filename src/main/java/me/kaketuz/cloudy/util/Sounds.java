package me.kaketuz.cloudy.util;

import org.bukkit.Location;
import org.bukkit.Sound;

import java.util.Objects;

public class Sounds {

    public static void playSound(Location location, Sound sound, float volume, float pitch) {
        Objects.requireNonNull(location.getWorld()).playSound(location, sound, volume, pitch);
    }
}
