package me.kaketuz.cloudy.abilities.steam.avatarstate;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.CoreAbility;
import me.kaketuz.cloudy.Cloudy;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public abstract class ASVariation extends BukkitRunnable {

    protected Player player;
    protected BendingPlayer bPlayer;
    protected final ThreadLocalRandom random = ThreadLocalRandom.current();

    public ASVariation(Player player) {
        this.player = player;
        this.bPlayer = BendingPlayer.getBendingPlayer(player);

        if (bPlayer == null || !bPlayer.isAvatarState()) return;


        avatarInstances.add(this);
    }

    public BendingPlayer getBendingPlayer() {
        return bPlayer;
    }

    public Player getPlayer() {
        return player;
    }

    private static final List<ASVariation> avatarInstances = new ArrayList<>();

    public static boolean hasASAbility(Player player, Class<? extends ASVariation> clazz) {
        return avatarInstances.stream().anyMatch(i -> i.getPlayer().equals(player) && i.getClass().equals(clazz) && avatarInstances.contains(i));
    }

    public static boolean playerUsingASAbility(Player player) {
        return avatarInstances.stream().anyMatch(i -> i.getPlayer().equals(player)  && avatarInstances.contains(i));
    }

    public static ASVariation getASAbility(Player player) {
        if (!playerUsingASAbility(player)) return null;
        return avatarInstances.stream()
                .takeWhile(i -> i.getPlayer().equals(player) && avatarInstances.contains(i))
                .toList()
                .getFirst();
    }

    @Override
    public synchronized void cancel() throws IllegalStateException {
        super.cancel();
        avatarInstances.remove(this);
    }
}
