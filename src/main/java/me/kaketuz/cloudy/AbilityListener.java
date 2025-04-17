package me.kaketuz.cloudy;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.event.AbilityEndEvent;
import com.projectkorra.projectkorra.event.BendingReloadEvent;
import me.kaketuz.cloudy.abilities.steam.*;
import me.kaketuz.cloudy.abilities.steam.combos.CoupleIcicles;
import me.kaketuz.cloudy.abilities.steam.combos.FollowingSteams;
import me.kaketuz.cloudy.abilities.steam.passives.ThermalTheft;
import me.kaketuz.cloudy.abilities.steam.util.Cloud;
import me.kaketuz.cloudy.abilities.steam.util.CloudStreamGeyser;
import me.kaketuz.cloudy.abilities.sub.SteamAbility;
import me.kaketuz.cloudy.util.GradientAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.server.ServerLoadEvent;

public class AbilityListener implements Listener {

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

        if (bPlayer == null || !event.isSneaking()) return;

        if (CoreAbility.hasAbility(player, FollowingSteams.class)) {
            if (bPlayer.getBoundAbilityName().equals("FumeAbsorption")) {
                new FumeAbsorption(player, true);
            } else if (bPlayer.getBoundAbilityName().equals("SteamControl")) {
                new SteamControl(player, true);
            }
        }
        else {
            switch (bPlayer.getBoundAbilityName().toUpperCase()) {
                case "EVAPORATE" -> new Evaporate(player);
                case "STEAMCONTROL" -> new SteamControl(player, false);
                case "CLOUDSTREAM" -> new CloudStream(player, false);
                case "FUMEABSORPTION" -> new FumeAbsorption(player, false);
                case "VAPORBOMB" -> new VaporBomb(player, false);
                case "CLOUDCUSHION" -> new CloudCushion(player, false);
                case "CLOUDFISSION" -> new CloudFission(player);
            }
        }
    }

    @EventHandler
    public void onClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

        if (bPlayer == null) return;

        if (!CoreAbility.hasAbility(player, CloudStream.class) && bPlayer.getBoundAbilityName().equalsIgnoreCase("CloudStream")) {
            if (Cloudy.config.getBoolean("Steam.CloudStream.Geyser.Enabled")) {
                new CloudStreamGeyser(player);
                return;
            }

        }

        if (CoreAbility.hasAbility(player, SteamControl.class)) CoreAbility.getAbility(player, SteamControl.class).throwCloud();
        if (CoreAbility.hasAbility(player, CloudStream.class)) CoreAbility.getAbility(player, CloudStream.class).launch();
        if (CoreAbility.hasAbility(player, VaporBomb.class)) {
            if (!CoreAbility.getAbility(player, VaporBomb.class).isLaunched()) CoreAbility.getAbility(player, VaporBomb.class).launch();
            else CoreAbility.getAbility(player, VaporBomb.class).explode();
        }
        if (CoreAbility.hasAbility(player, CloudCushion.class)) {
            if (CoreAbility.getAbility(player, CloudCushion.class).isReadyToLaunch() && !CoreAbility.getAbility(player, CloudCushion.class).isLaunched()) {
                CoreAbility.getAbility(player, CloudCushion.class).launch();
            }
        }



        if (CoreAbility.hasAbility(player, FollowingSteams.class)) {
            switch (bPlayer.getBoundAbilityName().toUpperCase()) {
                case "CLOUDCUSHION" -> {
                    new CloudCushion(player, true);
                    CoreAbility.getAbility(player, FollowingSteams.class).removeCloud();
                }
                case "VAPORBOMB" -> {
                    new VaporBomb(player, true);
                    CoreAbility.getAbility(player, FollowingSteams.class).removeCloud();
                }
                case "CLOUDSTREAM" -> {
                    new CloudStream(player, true);
                    CoreAbility.getAbility(player, FollowingSteams.class).removeAllClouds();
                }
            }
        }

        if (CoreAbility.hasAbility(player, CoupleIcicles.class)) {
            CoreAbility.getAbility(player, CoupleIcicles.class).shot();
        }
        if (CoreAbility.hasAbility(player, CloudCushion.class)) {
            CloudCushion c = CoreAbility.getAbility(player, CloudCushion.class);
            if (c.isLaunched() && System.currentTimeMillis() > c.getStartLaunchingTiming() + 100) {
                c.explode(bPlayer.getBoundAbilityName().equalsIgnoreCase("CloudFission"));
            }
        }
    }
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBendingReload(BendingReloadEvent event) {
        Cloud.getClouds().forEach(c -> c.remove(true));

        Configuration.configu.reload();
        event.getSender().sendMessage(GradientAPI.colorize("<#919CC2>ᴄʟᴏᴜᴅʏ | ᴄᴏɴꜰɪɢ ʀᴇʟᴏᴀᴅᴇᴅ!</#D2FFFA>"));
    }

    @EventHandler
    public void onReload(ServerLoadEvent event) {

        if (event.getType().equals(ServerLoadEvent.LoadType.RELOAD)) Cloud.getClouds().forEach(c -> c.remove(true));
    }

    public static void registerListener() {
        Cloudy.plugin.getServer().getPluginManager().registerEvents(new AbilityListener(), Cloudy.plugin);
    }

    @EventHandler
    public void onAbilityRemove(AbilityEndEvent event) {
        if (event.getAbility().getElement() != Element.FIRE) return;

        if (!CoreAbility.getAbility(ThermalTheft.class).isEnabled()) return;

        if (event.getAbility().getLocation() != null) {

            if (GeneralMethods.getBlocksAroundPoint(event.getAbility().getLocation(), 3).stream().anyMatch(ElementalAbility::isWater)) {

                Bukkit.getOnlinePlayers().forEach(p -> {
                    BendingPlayer bP = BendingPlayer.getBendingPlayer(p);

                    double rad = Cloudy.config.getDouble("Steam.Passives.ThermalTheft.Radius");

                    if (bP != null && p.getLocation().distance(event.getAbility().getLocation()) <= rad) {
                        if (bP.canUseSubElement(SteamAbility.STEAM)) new ThermalTheft(p, event.getAbility().getLocation());
                    }
                });
            }
        }
    }
}
