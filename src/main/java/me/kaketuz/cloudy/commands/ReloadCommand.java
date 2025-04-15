package me.kaketuz.cloudy.commands;

import me.kaketuz.cloudy.Cloudy;
import me.kaketuz.cloudy.Configuration;
import me.kaketuz.cloudy.abilities.steam.util.Cloud;
import me.kaketuz.cloudy.util.GradientAPI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class ReloadCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length != 1) return true;

        if (args[0].equalsIgnoreCase("reload")) {
            Cloud.getClouds().forEach(c -> c.remove(true));


            Configuration.configu.reload();

            sender.sendMessage(GradientAPI.colorize("<#919CC2>ᴄʟᴏᴜᴅʏ | ᴄᴏɴꜰɪɢ ʀᴇʟᴏᴀᴅᴇᴅ!</#D2FFFA>"));
            return true;
        }

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) return List.of("reload");

        return Collections.emptyList();
    }

    public static void register() {
        Cloudy.plugin.getServer().getPluginCommand("cloudy").setExecutor(new ReloadCommand());
    }
}
