package me.kaketuz.cloudy.abilities.sub;

import com.projectkorra.projectkorra.Element.ElementType;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.Element.SubElement;
import com.projectkorra.projectkorra.ability.Ability;
import com.projectkorra.projectkorra.ability.SubAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import me.kaketuz.cloudy.Cloudy;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;



public abstract class SteamAbility extends WaterAbility implements SubAbility {

    public static final SubElement STEAM;

    static {
        STEAM = new SubElement("Steam", Element.WATER, ElementType.BENDING, Cloudy.plugin) {
            @Override
            public ChatColor getColor() {
                String hex = Cloudy.config.getString("Steam.SubElementColor");
                assert hex != null;
                return ChatColor.of(hex);
            }
        };
    }

    @Override
    public Element getElement() {
        return STEAM;
    }

    @Override
    public Class<? extends Ability> getParentAbility() {
        return WaterAbility.class;
    }

    public SteamAbility(Player player) {
        super(player);
    }
}
