package com.excrele.ecore.managers;

import org.bukkit.World;
import org.bukkit.entity.Player;

import com.excrele.ecore.Ecore;

public class TimeWeatherManager {
    public TimeWeatherManager(Ecore plugin) {
        // Plugin reference kept for potential future use
    }

    public void setTime(World world, long time) {
        world.setTime(time);
    }

    public void setTime(Player player, long time) {
        setTime(player.getWorld(), time);
    }

    public void addTime(World world, long amount) {
        world.setTime(world.getTime() + amount);
    }

    public void setDay(World world) {
        world.setTime(1000);
    }

    public void setNight(World world) {
        world.setTime(13000);
    }

    public void setWeather(World world, boolean storm) {
        world.setStorm(storm);
        if (storm) {
            world.setThundering(true);
        }
    }

    public void clearWeather(World world) {
        world.setStorm(false);
        world.setThundering(false);
    }

    public void setSun(World world) {
        clearWeather(world);
    }

    public void setRain(World world) {
        setWeather(world, false);
        world.setStorm(true);
    }

    public void setStorm(World world) {
        setWeather(world, true);
    }
}

