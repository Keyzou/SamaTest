package com.github.keyzou.samatest;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Room {

    private Location spawnPoint;
    private Player playerAttached;
    private List<Location> villagerSpawns = new ArrayList<Location>();

    public Room(Location spawnPoint){
        this.spawnPoint = spawnPoint;
    }

    public void attachPlayer(Player p){
        playerAttached = p;
    }

    public void start(){
        generateRoom();
        playerAttached.teleport(spawnPoint);
    }

    private void generateRoom(){
        new Location(spawnPoint.getWorld(), spawnPoint.getBlockX(), spawnPoint.getBlockY()-1, spawnPoint.getBlockZ()).getBlock().setType(Material.DIAMOND_BLOCK);
        Location firstSpawn = new Location(spawnPoint.getWorld(), spawnPoint.getBlockX()+8, spawnPoint.getBlockY(), spawnPoint.getBlockZ()-6);
        for(int i = 1; i <= 5; i++){
            Location spawn = firstSpawn.add(0, 0, 2);
            villagerSpawns.add(spawn);
            new Location(spawn.getWorld(), spawn.getBlockX(), spawn.getBlockY()-1, spawn.getBlockZ()).getBlock().setType(Material.WOOL);
            for(int j = 1; j <= 4; j++){
                new Location(spawn.getWorld(), spawn.getBlockX()-j, spawn.getBlockY()-1, spawn.getBlockZ()).getBlock().setType(Material.WOOD);
            }
            Block b = new Location(spawn.getWorld(), spawn.getBlockX()-5, spawn.getBlockY(), spawn.getBlockZ()).getBlock();
            b.setTypeIdAndData(107, (byte) 1, false);
            b = new Location(spawn.getWorld(), spawn.getBlockX()-5, spawn.getBlockY()-1, spawn.getBlockZ()).getBlock();
            b.setTypeIdAndData(35, (byte) 14, false);
            new Location(spawn.getWorld(), spawn.getBlockX()-5, spawn.getBlockY(), spawn.getBlockZ()-1).getBlock().setType(Material.FENCE);
        }
    }
}
