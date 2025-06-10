package me.kaketuz.cloudy;

import com.projectkorra.projectkorra.ability.Ability;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.util.ActionBar;
import me.kaketuz.cloudy.abilities.steam.util.Cloud;
import me.kaketuz.cloudy.abilities.sub.SteamAbility;
import me.kaketuz.cloudy.commands.ReloadCommand;
import me.kaketuz.cloudy.util.GradientAPI;
import me.kaketuz.cloudy.util.logger.ANSIValues;
import me.kaketuz.cloudy.util.logger.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.*;

public final class Cloudy extends JavaPlugin {

    public static Cloudy plugin;
    public static FileConfiguration config;

    private static String version;

    public static int maxCloudsPerPlayer;
    public static String fullCloudsMsg;

    public static Map<Player, Cloud[]> playersClouds;


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

        maxCloudsPerPlayer = config.getInt("Cloud.MaxCloudsPerPlayer");
        fullCloudsMsg = config.getString("Cloud.FullCloudsMessage");
        playersClouds = new HashMap<>();
        Logger.sendCustom("Thanks for installing the Cloudy Plugin! The Developer is very grateful to you!!! <3", ANSIValues.PURPLE);
        Logger.sendSuccessfully("Cloudy plugin was successfully enabled!");

    }

    public static boolean addCloudArray(Cloud cloud, Player owner) {

        if (owner == null) return false;
        if (maxCloudsPerPlayer == -1) return true;

        if (!playersClouds.containsKey(owner)) playersClouds.put(owner, new Cloud[maxCloudsPerPlayer]);
        if (!Objects.equals(cloud.getOwner(), owner)) return false;


        if (Arrays.stream(playersClouds.get(owner)).filter(Objects::nonNull).toList().size() == maxCloudsPerPlayer) {
            Bukkit.getScheduler().runTaskLater(Cloudy.plugin, () -> ActionBar.sendActionBar(ChatColor.RED + fullCloudsMsg, owner), 5);
            return false;
        }

        for (int i = 0; i < playersClouds.get(owner).length; i++) {
            if (playersClouds.get(owner)[i] == null) {
                playersClouds.get(owner)[i] = cloud;
                int finalI = i;
                Bukkit.getScheduler().runTaskLater(Cloudy.plugin, () -> ActionBar.sendActionBar(ChatColor.GREEN + "" + finalI + "/" + maxCloudsPerPlayer), 5);

                return true;
            }
        }
        return false;
    }

    public static void removeCloudArray(Player owner, Cloud cloud) {
        if (owner == null) return;
        if (maxCloudsPerPlayer == -1) return;
        if (!playersClouds.containsKey(owner)) return;
        if (!Objects.equals(cloud.getOwner(), owner)) return;

        for (int i = 0; i < playersClouds.get(owner).length; i++) {
            if (Objects.equals(playersClouds.get(owner)[i], cloud)) {
                playersClouds.get(owner)[i] = null;
                break;
            }
        }

    }

    @Override
    public void onDisable() {
        Cloud.getClouds().forEach(c -> c.remove(true));
    }

    public static String getAuthor(Ability ability) {
        if (ability.getElement() == SteamAbility.STEAM) {
            return GradientAPI.colorize("<#919CC2>| ʟᴏʀᴅ ᴄᴀᴄᴇᴛᴜs</#D2FFFA>");
        }
        return "kaketuz :/";
    }

    public static String getVersion(Ability ability) {
        if (ability.getElement() == SteamAbility.STEAM) {
            return GradientAPI.colorize("<#919CC2>[sᴛᴇᴀᴍʙᴇɴᴅɪɴɢ: " + version + "]</#D2FFFA>");
        }
        return "[idk element xd]: " + version;
    }
}
