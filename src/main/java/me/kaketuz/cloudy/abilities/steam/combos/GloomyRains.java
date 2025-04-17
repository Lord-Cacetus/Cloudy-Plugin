package me.kaketuz.cloudy.abilities.steam.combos;

import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager;
import com.projectkorra.projectkorra.ability.util.ComboUtil;
import me.kaketuz.cloudy.Cloudy;
import me.kaketuz.cloudy.abilities.sub.SteamAbility;
import me.kaketuz.cloudy.util.Methods;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class GloomyRains extends SteamAbility implements AddonAbility, ComboAbility {
    public GloomyRains(Player player) {
        super(player);
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
        return Cloudy.config.getLong("Steam.Combo.GloomyHails.Rain.Cooldown");
    }

    @Override
    public String getName() {
        return "GloomyRains";
    }

    @Override
    public boolean isHiddenAbility() {
        return true;
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
        return new GloomyHails(player, true);
    }

    @Override
    public ArrayList<ComboManager.AbilityInformation> getCombination() {
        return ComboUtil.generateCombinationFromList(this, Cloudy.config.getStringList("Steam.Combo.GloomyHails.Rain.Combination"));
    }

    @Override
    public boolean isEnabled() {
        return Cloudy.config.getBoolean("Steam.Combo.GloomyRains.Enabled");
    }
}
