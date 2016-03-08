package com.github.keyzou.samatest;

import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class GameTask extends BukkitRunnable {

    private Main plugin;

    private List<Room> roomsRemove = new ArrayList<Room>();

    public GameTask(Main plugin) {
        this.plugin = plugin;
    }

    public void run() {
        if (plugin.stop) {
            this.cancel();
            return;
        }
        plugin.time++;
        if (plugin.time % 30 == 0) {
            plugin.getServer().broadcastMessage(net.md_5.bungee.api.ChatColor.GOLD + "On accélère la cadence !");
            plugin.spawnFrequency -= plugin.spawnFrequency < 30 ? 5 : 10;
            if (plugin.spawnFrequency <= 20)
                plugin.spawnFrequency = 20;
        }
        for (Room r : plugin.roomsPlaying) {
            if (roomsRemove.contains(r))
                continue;
            if (r.errors >= 3) {
                plugin.scores.put(r.playerAttached.getName(), r.score);
                plugin.getServer().broadcastMessage(r.playerAttached.getDisplayName() + org.bukkit.ChatColor.RED + " a perdu !");
                plugin.getServer().broadcastMessage(r.playerAttached.getDisplayName() + org.bukkit.ChatColor.RED + " aura tenu " + (plugin.time / 60) + " minutes et " + plugin.time % 60 + " secondes !");
                roomsRemove.add(r);
                r.lose(plugin);
            }
        }
        plugin.roomsPlaying.removeAll(roomsRemove);
        if (plugin.roomsPlaying.isEmpty()) {
            plugin.stop = true;
            plugin.endGame();
        }
    }
}
