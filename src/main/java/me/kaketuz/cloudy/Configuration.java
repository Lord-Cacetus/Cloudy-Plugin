package me.kaketuz.cloudy;

import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import me.kaketuz.cloudy.util.Config;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.concurrent.ConcurrentHashMap;

public class Configuration {

    public static Config configu;

    public Configuration() {
        configu = new Config(new File("configuration.yml"));
    }

    public enum AvatarStateVars {
        EVAPORATE(false);
        private boolean val;

        AvatarStateVars(boolean val) {
            this.val = val;
        }

        public boolean isVal() {
            return val;
        }
    }




    public static void register() {
        FileConfiguration config = configu.get();



        config.addDefault("Steam.SubElementColor", "#CCEFFF");

        //Steam

        //Cloud
        config.addDefault("Cloud.Duration", 20000);
        config.addDefault("Cloud.FireBuffDuration", 4000);
        config.addDefault("Cloud.Damage", 1);
        config.addDefault("Cloud.DamageBuffed", 0.5);
        config.addDefault("Cloud.FormSpeed", 0.5);
        config.addDefault("Cloud.CanFireAbilitiesBuffs", true);
        config.addDefault("Cloud.CanMoveByAmbient", true);
        config.addDefault("Cloud.CanAirAbilitiesMove", true);
        config.addDefault("Cloud.CanCreateLakes", true);
        config.addDefault("Cloud.ParticleAmount", 5);
        config.addDefault("Cloud.AmbientMovementSpeed", 0.01);
        config.addDefault("Cloud.CollisionRadius", 1.5);
        config.addDefault("Cloud.SnowVariable", true);
        config.addDefault("Cloud.Velocity", 0.5);
        config.addDefault("Cloud.SnowVarHeight", 100);
        config.addDefault("Cloud.MaxCloudsPerPlayer", -1);
        config.addDefault("Cloud.FullCloudsMessage", "You have created the maximum number of clouds for yourself!");

        //Evaporate
        config.addDefault("Steam.Evaporate.Enabled", true);
        config.addDefault("Steam.Evaporate.Cooldown", 1000);
        config.addDefault("Steam.Evaporate.SourceRange", 8);
        config.addDefault("Steam.Evaporate.ColdBiomesBuff", true);
        config.addDefault("Steam.Evaporate.NightBuff", true);
        config.addDefault("Steam.Evaporate.BuffFactor", 1.4);
        config.addDefault("Steam.Evaporate.Description", "With this ability, you can create clouds of steam out of water, which you can later control. The steam will fly aimlessly and eventually turn back into water if left unused for a long time. The steam itself is very hot and will cause damage if someone decides to enter it.");
        config.addDefault("Steam.Evaporate.Instructions", "Hold the shift while looking at water or ice. After that, the cloud will rise and begin to float aimlessly in the air.");
        //Avatarstate 1
        config.addDefault("Steam.Evaporate.AvatarState.Enabled", true);
        config.addDefault("Steam.Evaporate.AvatarState.Radius", 3);
        config.addDefault("Steam.Evaporate.AvatarState.SourceRange", 15);
        config.addDefault("Steam.Evaporate.AvatarState.MaxClouds", 5);
        config.addDefault("Steam.Evaporate.AvatarState.Cooldown", 3000);

        //SteamControl
        config.addDefault("Steam.SteamControl.Enabled", true);
        config.addDefault("Steam.SteamControl.MaxClouds", 5);
        config.addDefault("Steam.SteamControl.ThrowSpeed", 1);
        config.addDefault("Steam.SteamControl.FollowSpeed", 0.5);
        config.addDefault("Steam.SteamControl.EndBurstPower", 0.5);
        config.addDefault("Steam.SteamControl.ColdBiomesBuff", true);
        config.addDefault("Steam.SteamControl.NightBuff", true);
        config.addDefault("Steam.SteamControl.BuffFactor", 1.4);
        config.addDefault("Steam.SteamControl.SourceRadius", 10);
        config.addDefault("Steam.SteamControl.AdditionalLiveTime", 2000);
        config.addDefault("Steam.SteamControl.RemoveAfterThrow", false);
        config.addDefault("Steam.SteamControl.Cooldown", 10000);
        config.addDefault("Steam.SteamControl.Description", "With this ability, you can control clouds of steam by collecting them in one pile or throwing them in different directions. Note: You can also capture other players clouds and after that it will stop doing you any harm.");
        config.addDefault("Steam.SteamControl.Instructions", "Hold down the shift, after that all the clouds that will be nearby will gather into one point. You can control them and throw them by clicking the left mouse button if the cloud has reached a common point. If you throw at least one cloud, then when you release the shift, all the other clouds will fly off in different directions.");

        //CloudStream
        config.addDefault("Steam.CloudStream.Enabled", true);
        config.addDefault("Steam.CloudStream.Radius", 2);
        config.addDefault("Steam.CloudStream.PushPower", 1.5);
        config.addDefault("Steam.CloudStream.Range", 10);
        config.addDefault("Steam.CloudStream.Speed", 1);
        config.addDefault("Steam.CloudStream.SourceRange", 12);
        config.addDefault("Steam.CloudStream.Cooldown", 12000);
        config.addDefault("Steam.CloudStream.FlowAmount", 6);
        config.addDefault("Steam.CloudStream.FlowDuration", 1500);
        config.addDefault("Steam.CloudStream.ColdBiomesBuff", true);
        config.addDefault("Steam.CloudStream.NightBuff", true);
        config.addDefault("Steam.CloudStream.BuffFactor", 1.4);
        config.addDefault("Steam.CloudStream.Description", "This is a very powerful steam ability. With it, you create a massive stream of steam that will blow away everyone in its path, including yourself.");
        config.addDefault("Steam.CloudStream.Instructions", "Hold the shift while looking at water or ice. After that, a small funnel is formed. It's a sign that you're doing everything right. After that, click the left mouse button, looking in the direction you want the steam stream to fly.");
        config.addDefault("Steam.CloudStream.Geyser.Range", 5);
        config.addDefault("Steam.CloudStream.Geyser.Speed", 1.5);
        config.addDefault("Steam.CloudStream.Geyser.Damage", 2);
        config.addDefault("Steam.CloudStream.Geyser.UppercutPower", 1);
        config.addDefault("Steam.CloudStream.Geyser.CollisionRadius", 0.5);
        config.addDefault("Steam.CloudStream.Geyser.SourceRange", 8);
        config.addDefault("Steam.CloudStream.Geyser.Cooldown", 3000);
        config.addDefault("Steam.CloudStream.Geyser.Enabled", true);
        config.addDefault("Steam.CloudStream.Geyser.Instructions", "click the left mouse button while looking at the water or ice");
        config.addDefault("Steam.CloudStream.Geyser.Description", "This combination is a variation of CloudStream's ability. You create a geyser of steam that will cause damage and lift those who fall under the geyser.");

        //FumeAbsorption
        config.addDefault("Steam.FumeAbsorption.Enabled", true);
        config.addDefault("Steam.FumeAbsorption.MaxClouds", 5);
        config.addDefault("Steam.FumeAbsorption.MinRange", 5);
        config.addDefault("Steam.FumeAbsorption.Speed", 2);
        config.addDefault("Steam.FumeAbsorption.Knockback", 3);
        config.addDefault("Steam.FumeAbsorption.Damage", 2);
        config.addDefault("Steam.FumeAbsorption.StreamsMultiplier", 5);
        config.addDefault("Steam.FumeAbsorption.FollowSpeed", 0.5);
        config.addDefault("Steam.FumeAbsorption.SourceRadius", 10);
        config.addDefault("Steam.FumeAbsorption.RandomDirections", false);
        config.addDefault("Steam.FumeAbsorption.Cooldown", 14000);
        config.addDefault("Steam.FumeAbsorption.ColdBiomesBuff", true);
        config.addDefault("Steam.FumeAbsorption.NightBuff", true);
        config.addDefault("Steam.FumeAbsorption.BuffFactor", 1.4);
        config.addDefault("Steam.FumeAbsorption.Description", "Another very powerful steam ability. You collect all the clouds in the vicinity and increase the pressure around you. After that, any action can trigger an explosion. If an explosion occurs, a powerful wave of steam will form that will cause great damage and strongly repel any entities from you. (The power of the explosion depends on the number of clouds collected)");
        config.addDefault("Steam.FumeAbsorption.Instructions", "Hold down the shift. After that, the nearest clouds will start flying towards you. After they reach you, you can release the shift to cause an explosion.");

        //VaporBomb
        config.addDefault("Steam.VaporBomb.Enabled", false);
        config.addDefault("Steam.VaporBomb.Speed", 2);
        config.addDefault("Steam.VaporBomb.GravityFactor", 0.08);
        config.addDefault("Steam.VaporBomb.GravityMultiplier", 0.02);
        config.addDefault("Steam.VaporBomb.Damage", 2);
        config.addDefault("Steam.VaporBomb.Knockback", 2);
        config.addDefault("Steam.VaporBomb.SourceRange", 10);
        config.addDefault("Steam.VaporBomb.Cooldown", 12000);
        config.addDefault("Steam.VaporBomb.MistDuration", 8000);
        config.addDefault("Steam.VaporBomb.BlindnessDuration", 100);
        config.addDefault("Steam.VaporBomb.MistSegmentsAmount", 10);
        config.addDefault("Steam.VaporBomb.CanPlayerHide", true);
        config.addDefault("Steam.VaporBomb.ColdBiomesBuff", true);
        config.addDefault("Steam.VaporBomb.NightBuff", true);
        config.addDefault("Steam.VaporBomb.BuffFactor", 1.4);
        config.addDefault("Steam.VaporBomb.Description", "With this ability, you can throw a compressed stream of steam and when you reach an entity or block, it will explode, causing damage and creating an area of fog where you will not be visible.");
        config.addDefault("Steam.VaporBomb.Instructions", "Hold the shift while looking at water or ice. If there is a splash, then you can press the left mouse button to eject the compressed steam charge. You can click the left mouse button again to blow up steam or let it fly to blocks or entities.");

        //CloudCushion
        config.addDefault("Steam.CloudCushion.Enabled", true);
        config.addDefault("Steam.CloudCushion.Range", 40);
        config.addDefault("Steam.CloudCushion.Speed", 2);
        config.addDefault("Steam.CloudCushion.Radius", 4);
        config.addDefault("Steam.CloudCushion.SourceRadius", 10);
        config.addDefault("Steam.CloudCushion.Knockback", 2);
        config.addDefault("Steam.CloudCushion.Cooldown", 12000);
        config.addDefault("Steam.CloudCushion.Duration", 7000);
        config.addDefault("Steam.CloudCushion.FollowSpeed", 0.7);
        config.addDefault("Steam.CloudCushion.SpeedBoost", 3);
        config.addDefault("Steam.CloudCushion.JumpBoost", 3);
        config.addDefault("Steam.CloudCushion.CanGrowPlants", true);
        config.addDefault("Steam.CloudCushion.MoistureFactor", 1);
        config.addDefault("Steam.CloudCushion.GrowChance", 15);
        config.addDefault("Steam.CloudCushion.AffectOtherSteambenders", true);
        config.addDefault("Steam.CloudCushion.ColdBiomesBuff", true);
        config.addDefault("Steam.CloudCushion.NightBuff", true);
        config.addDefault("Steam.CloudCushion.BuffFactor", 1.4);
        config.addDefault("Steam.CloudCushion.Description", "This auxiliary ability will help you and other Steambenders escape from fall damage. You take one cloud and throw it wherever you want. Upon reaching the block, it explodes, covering the surface with a layer of steam that will protect you and other entities from falling. And if the steam flow does not touch the blocks, it creates a small explosion that will push away everyone who will be nearby.");
        config.addDefault("Steam.CloudCushion.Instructions", "Hold down the shift. After that, the nearest cloud will fly up to you. When it reaches you, click the left mouse button to shoot a stream of steam.");

        //CloudFission
        config.addDefault("Steam.CloudFission.Enabled", true);
        config.addDefault("Steam.CloudFission.SourceRange", 10);
        config.addDefault("Steam.CloudFission.Damage", 3);
        config.addDefault("Steam.CloudFission.Cooldown", 14000);
        config.addDefault("Steam.CloudFission.FreezeTicks", 500);
        config.addDefault("Steam.CloudFission.SlowLevel", 2);
        config.addDefault("Steam.CloudFission.Radius", 5);
        config.addDefault("Steam.CloudFission.SlowDuration", 40);
        config.addDefault("Steam.CloudFission.MaxClouds", 9);
        config.addDefault("Steam.CloudFission.ColdBiomesBuff", true);
        config.addDefault("Steam.CloudFission.NightBuff", true);
        config.addDefault("Steam.CloudFission.BuffFactor", 1.4);
        config.addDefault("Steam.CloudFission.Description", "This is a very strong vapor ability. You use it to tear apart the nearest clouds. The explosion will freeze and slow down the entities that will be nearby");
        config.addDefault("Steam.CloudFission.Instructions", "press shift");

        //Combos
        //FollowingSteams
        config.addDefault("Steam.Combo.FollowingSteams.Enabled", true);
        config.addDefault("Steam.Combo.FollowingSteams.Duration", 10000);
        config.addDefault("Steam.Combo.FollowingSteams.Cooldown", 20000);
        config.addDefault("Steam.Combo.FollowingSteams.MaxClouds", 7);
        config.addDefault("Steam.Combo.FollowingSteams.SpeedBoost", 2);
        config.addDefault("Steam.Combo.FollowingSteams.Knockback", 2);
        config.addDefault("Steam.Combo.FollowingSteams.FollowSpeed", 1);
        config.addDefault("Steam.Combo.FollowingSteams.SourceRadius", 13);
        config.addDefault("Steam.Combo.FollowingSteams.ColdBiomesBuff", true);
        config.addDefault("Steam.Combo.FollowingSteams.NightBuff", true);
        config.addDefault("Steam.Combo.FollowingSteams.BuffFactor", 1.4);
        config.addDefault("Steam.Combo.FollowingSteams.Combination", Arrays.asList("Evaporate:LEFT_CLICK", "Evaporate:LEFT_CLICK", "CloudStream:SHIFT_DOWN"));
        config.addDefault("Steam.Combo.FollowingSteams.Description", "This is a very useful combination. With its help, you collect the nearest clouds into yourself and create a small steam storage around yourself. This storage will give you movement speed and will help you use the steam abilities by taking the steam that is next to you. This will help you avoid waiting for the steam to reach you to use the ability. Keep in mind that using large-scale abilities, steam will quickly run out. But using low-cost abilities, you will have more steam for other movements.");
        config.addDefault("Steam.Combo.FollowingSteams.Instructions", "Evaporate: left click twice -> CloudStream: Shift Down");

        //FogRun
        config.addDefault("Steam.Combo.FogRun.Enabled", true);
        config.addDefault("Steam.Combo.FogRun.Duration", 9000);
        config.addDefault("Steam.Combo.FogRun.FlowDuration", 2000);
        config.addDefault("Steam.Combo.FogRun.Cooldown", 14000);
        config.addDefault("Steam.Combo.FogRun.Angle", 0.22);
        config.addDefault("Steam.Combo.FogRun.Speed", 1);
        config.addDefault("Steam.Combo.FogRun.RemoveIfHitWall", true);
        config.addDefault("Steam.Combo.FogRun.EndFade", true);
        config.addDefault("Steam.Combo.FogRun.OldEffects", false);
        config.addDefault("Steam.Combo.FogRun.ColdBiomesBuff", true);
        config.addDefault("Steam.Combo.FogRun.NightBuff", true);
        config.addDefault("Steam.Combo.FogRun.BuffFactor", 1.4);
        config.addDefault("Steam.Combo.FogRun.Combination", Arrays.asList("CloudStream:SHIFT_DOWN", "CloudCushion:LEFT_CLICK"));
        config.addDefault("Steam.Combo.FogRun.Description", "Another combination that will help you move quickly through the water. With it, you can run quickly through the water, leaving a trail of mist.");
        config.addDefault("Steam.Combo.FogRun.Instructions", "CloudStream: Shift down -> CloudCushion: Left click");

        //GloomyHails
        config.addDefault("Steam.Combo.GloomyHails.Enabled", true);
        config.addDefault("Steam.Combo.GloomyHails.MaxClouds", 10);
        config.addDefault("Steam.Combo.GloomyHails.Speed", 0.25);
        config.addDefault("Steam.Combo.GloomyHails.HailSpeed", 1);
        config.addDefault("Steam.Combo.GloomyHails.Radius", 2);
        config.addDefault("Steam.Combo.GloomyHails.Damage", 0.5);
        config.addDefault("Steam.Combo.GloomyHails.SourceRadius", 15);
        config.addDefault("Steam.Combo.GloomyHails.CollisionRadius", 0.25);
        config.addDefault("Steam.Combo.GloomyHails.SpawnHailChance", 1);
        config.addDefault("Steam.Combo.GloomyHails.Cooldown", 20000);
        config.addDefault("Steam.Combo.GloomyHails.Duration", 7000);
        config.addDefault("Steam.Combo.GloomyHails.DisplayVariation", true);
        config.addDefault("Steam.Combo.GloomyHails.HailHeight", 0.5);
        config.addDefault("Steam.Combo.GloomyHails.CanBreakPlants", true);
        config.addDefault("Steam.Combo.GloomyHails.ForcedBreak", false);
        config.addDefault("Steam.Combo.GloomyHails.ColdBiomesBuff", true);
        config.addDefault("Steam.Combo.GloomyHails.NightBuff", true);
        config.addDefault("Steam.Combo.GloomyHails.BuffFactor", 1.4);
        config.addDefault("Steam.Combo.GloomyHails.CanGrowPlants", true);
        config.addDefault("Steam.Combo.GloomyHails.CanRegen", true);
        config.addDefault("Steam.Combo.GloomyHails.PlantsGrowChance", 30);
        config.addDefault("Steam.Combo.GloomyHails.RegenDuration", 20);
        config.addDefault("Steam.Combo.GloomyHails.RegenLevel", 2);
        config.addDefault("Steam.Combo.GloomyHails.Combination", Arrays.asList("Evaporate:SHIFT_DOWN", "Evaporate:SHIFT_UP", "Evaporate:SHIFT_DOWN", "Evaporate:SHIFT_UP", "Evaporate:LEFT_CLICK", "Evaporate:SHIFT_DOWN"));
        config.addDefault("Steam.Combo.GloomyHails.Description", "This combination will help you turn all the nearby clouds into gloomy clouds that will form a powerful hail under them.");
        config.addDefault("Steam.Combo.GloomyHails.Instructions", "Evaporate: sneak twice -> Evaporate: left click -> Evaporate: sneak");
        config.addDefault("Steam.Combo.GloomyHails.Rain.Enabled", true);
        config.addDefault("Steam.Combo.GloomyHails.Rain.Combination", Arrays.asList("CloudStream:SHIFT_DOWN", "CloudStream:SHIFT_UP", "CloudStream:SHIFT_DOWN", "CloudStream:SHIFT_UP", "CloudStream:SHIFT_DOWN", "CloudStream:SHIFT_UP"));
        config.addDefault("Steam.Combo.GloomyHails.Rain.Description", "This is a variation of the GloomyHails combination. Instead of creating powerful hail, you create healing rain, which heals all entities and helps plants grow.");
        config.addDefault("Steam.Combo.GloomyHails.Rain.Instructions", "CloudStream: sneak 3 times");


        //CoupleIcicles
        config.addDefault("Steam.Combo.CoupleIcicles.Enabled", true);
        config.addDefault("Steam.Combo.CoupleIcicles.MaxClouds", 5);
        config.addDefault("Steam.Combo.CoupleIcicles.ShotsPerCloudsAmount", 10);
        config.addDefault("Steam.Combo.CoupleIcicles.Damage", 1);
        config.addDefault("Steam.Combo.CoupleIcicles.CollisionRadius", 0.5);
        config.addDefault("Steam.Combo.CoupleIcicles.Speed", 1.2);
        config.addDefault("Steam.Combo.CoupleIcicles.Range", 20);
        config.addDefault("Steam.Combo.CoupleIcicles.RingRadius", 2);
        config.addDefault("Steam.Combo.CoupleIcicles.RingSpeed", 0.025);
        config.addDefault("Steam.Combo.CoupleIcicles.SourceRadius", 8);
        config.addDefault("Steam.Combo.CoupleIcicles.FollowSpeed", 0.5);
        config.addDefault("Steam.Combo.CoupleIcicles.Cooldown", 13000);
        config.addDefault("Steam.Combo.CoupleIcicles.GravityFalling", true);
        config.addDefault("Steam.Combo.CoupleIcicles.DisplayVariation", true);
        config.addDefault("Steam.Combo.CoupleIcicles.AllowNoDamageTicks", true);
        config.addDefault("Steam.Combo.CoupleIcicles.ColdBiomesBuff", true);
        config.addDefault("Steam.Combo.CoupleIcicles.NightBuff", true);
        config.addDefault("Steam.Combo.CoupleIcicles.BuffFactor", 1.4);
        config.addDefault("Steam.Combo.CoupleIcicles.Message", "Shots remaining: ");
        config.addDefault("Steam.Combo.CoupleIcicles.Combination", Arrays.asList("Evaporate:SHIFT_DOWN", "Evaporate:SHIFT_UP", "SteamControl:LEFT_CLICK", "SteamControl:SHIFT_DOWN"));
        config.addDefault("Steam.Combo.CoupleIcicles.Description", "One of the strongest steam combinations is CoupleIcicles. With it, you collect clouds that are near you and transform them into icicles. The more clouds you collect, the more icicles there will be");
        config.addDefault("Steam.Combo.CoupleIcicles.Instructions", "Evaporate: shift down, shift up -> SteamControl: left click -> SteamControl: shift down");

        //ThickHaze
        config.addDefault("Steam.Combo.ThickHaze.Radius", 10);
        config.addDefault("Steam.Combo.ThickHaze.Speed", 0.3);
        config.addDefault("Steam.Combo.ThickHaze.Height", 0.5);
        config.addDefault("Steam.Combo.ThickHaze.SourceRadius", 10);
        config.addDefault("Steam.Combo.ThickHaze.SettlementSpeed", 0.5);
        config.addDefault("Steam.Combo.ThickHaze.FreezeSpeed", 10);
        config.addDefault("Steam.Combo.ThickHaze.MaxClouds", 9);
        config.addDefault("Steam.Combo.ThickHaze.Duration", 10000);
        config.addDefault("Steam.Combo.ThickHaze.Cooldown", 20000);
        config.addDefault("Steam.Combo.ThickHaze.Combination", Arrays.asList("CloudCushion:LEFT_CLICK", "Evaporate:LEFT_CLICK", "CloudCushion:LEFT_CLICK", "Evaporate:SHIFT_DOWN"));
        config.addDefault("Steam.Combo.ThickHaze.Description", "With this ability, you allow the nearest clouds to settle on the ground, which creates a thick fog. In it, players will not be able to see anything and will gradually freeze.");
        config.addDefault("Steam.Combo.ThickHaze.Instructions", "CloudCushion: left click -> (Evaporate) left click -> CloudCushion: left click -> Evaporate: shift down");

        //Passives
        //ThermalTheft
        config.addDefault("Steam.Passives.ThermalTheft.Enabled", true);
        config.addDefault("Steam.Passives.ThermalTheft.Radius", 20);
        config.addDefault("Steam.Passives.ThermalTheft.Description", "If Firebender next to you throws a fire charge into the water, a cloud will form at the point of impact.");
        config.addDefault("Steam.Passives.ThermalTheft.Instructions", "You must be close to the position where the fire hits the water.");


        Arrays.stream(AvatarStateVars.values()).forEach(as -> {
            StringBuilder builder = new StringBuilder();
            boolean toUpper = true;

            for (char c : as.name().toLowerCase().toCharArray()) {
                if (c == '_') {
                    toUpper = true;
                } else {
                    if (toUpper) {
                        builder.append(Character.toUpperCase(c));
                        toUpper = false;
                    } else {
                        builder.append(c);
                    }
                }
            }

            String absolute = builder.toString();

            CoreAbility ability = CoreAbility.getAbility(absolute);
            boolean enabled;
            if (ability instanceof ComboAbility) {
                enabled = config.getBoolean("Steam.Combo." + absolute + ".AvatarState.Enabled");
            }
            else {
                enabled = config.getBoolean("Steam." + absolute + ".AvatarState.Enabled");
            }
            as.val = enabled;
        });

        configu.save();
    }

}
