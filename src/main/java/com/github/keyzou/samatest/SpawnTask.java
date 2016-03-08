package com.github.keyzou.samatest;

import net.minecraft.server.v1_8_R3.World;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Random;
public class SpawnTask extends BukkitRunnable {

    private List<Room> rooms;
    private Main plugin;
    private Random rand;

    protected SpawnTask(Main plugin, List<Room> rooms){
        this.rooms = rooms;
        this.plugin = plugin;
        rand = new Random();
    }

    public void run() {
        if(plugin.stop){
            this.cancel();
            return;
        }
        int nbGood = rand.nextInt(5);
        for(Room room : rooms) {
            int spawnNb = rand.nextInt(room.villagerSpawns.size());
            Location l = room.villagerSpawns.get(spawnNb);
            World mcWorld = ((CraftWorld) l.getWorld()).getHandle();
            PNJ pnj = new PNJ(mcWorld, room.fencesLocations.get(spawnNb),rand.nextInt(room.villagerSpawns.size()) > nbGood);
            pnj.setLocation(l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch());
            mcWorld.addEntity(pnj, CreatureSpawnEvent.SpawnReason.CUSTOM);
            room.addPNJ(pnj);
        }
        SpawnTask spawnTask = new SpawnTask(plugin, rooms);
        spawnTask.runTaskLater(plugin, plugin.spawnFrequency);
    }
}
