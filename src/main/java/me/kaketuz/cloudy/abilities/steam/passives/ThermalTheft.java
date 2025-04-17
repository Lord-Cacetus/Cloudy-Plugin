package me.kaketuz.cloudy.abilities.steam.passives;

import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.PassiveAbility;
import me.kaketuz.cloudy.Cloudy;
import me.kaketuz.cloudy.abilities.steam.util.Cloud;
import me.kaketuz.cloudy.abilities.sub.SteamAbility;
import me.kaketuz.cloudy.util.Methods;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ThermalTheft extends SteamAbility implements AddonAbility, PassiveAbility {


    public ThermalTheft(Player player, Location location) {
        super(player);
        Cloud.createCloud(location, player);
    }

    @Override
    public void progress() {

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
        return 0;
    }

    @Override
    public String getName() {
        return "ThermalTheft";
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
    public boolean isInstantiable() {
        return false;
    }

    @Override
    public boolean isProgressable() {
        return true;
    }

    @Override
    public String getDescription() {
        return Cloudy.config.getString("Steam.Passives.ThermalTheft.Description");
    }

    @Override
    public String getInstructions() {
        return Cloudy.config.getString("Steam.Passives.ThermalTheft.Instructions");
    }

    @Override
    public boolean isEnabled() {
        return Cloudy.config.getBoolean("Steam.Passives.ThermalTheft.Enabled");
    }
}
