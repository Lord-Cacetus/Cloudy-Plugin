package me.kaketuz.cloudy;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ability.Ability;
import com.projectkorra.projectkorra.ability.CoreAbility;
import me.kaketuz.cloudy.abilities.steam.passives.Condensation;
import me.kaketuz.cloudy.abilities.steam.util.Cloud;
import me.kaketuz.cloudy.abilities.sub.SteamAbility;
import me.kaketuz.cloudy.commands.ReloadCommand;
import me.kaketuz.cloudy.util.GradientAPI;
import me.kaketuz.cloudy.util.Methods;
import me.kaketuz.cloudy.util.logger.ANSIValues;
import me.kaketuz.cloudy.util.logger.Logger;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;

public final class Cloudy extends JavaPlugin {

    public static Cloudy plugin;
    public static FileConfiguration config;

    private static String version;

    @Override
    public void onEnable() {
        plugin = this;

        version = plugin.getDescription().getVersion();

        new Configuration();
        config = Configuration.configu.get();
        AbilityListener.registerListener();
        Configuration.register();
        CoreAbility.registerPluginAbilities(this, "me.kaketuz.cloudy.abilities");

        ReloadCommand.register();



        Logger.sendCustom("Thanks for installing the Cloudy Plugin! The Developer is very grateful to you!!! <3", ANSIValues.PURPLE);
        Logger.sendSuccessfully("Cloudy plugin was successfully enabled!");

//                Bukkit.getScheduler().runTaskTimer(this, () -> Bukkit.getOnlinePlayers().forEach(p -> {
//            BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(p);
//            Optional.ofNullable(bPlayer)
//                    .filter(bp -> bp.canUseSubElement(SteamAbility.STEAM))
//                    .ifPresent(bp -> {
//                        if (Methods.getHumidity(p.getLocation()) >= 0.8) {
//                            new Condensation(p);
//                        }
//                    });
//        }), 0, 1L);

    }

    @Override
    public void onDisable() {
        Cloud.getClouds().forEach(c -> c.remove(true));
    }

    public static String getAuthor(Ability ability) {
        if (ability.getElement() == SteamAbility.STEAM) {
            return GradientAPI.colorize("<#919CC2>| ᴋᴀᴋᴇᴛᴜᴢ</#D2FFFA>");
        }
        else if (ability.getElement() == Element.LAVA) {
            return GradientAPI.colorize("<#FF5C00>| ᴋᴀᴋᴇᴛᴜᴢ</#CB1825>");
        }

        return "kaketuz :/";
    }

    public static String getVersion(Ability ability) {
        if (ability.getElement() == SteamAbility.STEAM) {
            return GradientAPI.colorize("<#919CC2>[sᴛᴇᴀᴍʙᴇɴᴅɪɴɢ: " + version + "]</#D2FFFA>");
        }
        else if (ability.getElement() == Element.LAVA) {
            return GradientAPI.colorize("<#FF5C00>[ʟᴀᴠᴀʙᴇɴᴅɪɴɢ: " + version + "]</#CB1825>");
        }

        return "[idk element xd]: " + version;
    }
}
