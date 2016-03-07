package com.github.keyzou.samatest;

import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.DamageSource;
import net.minecraft.server.v1_8_R3.GenericAttributes;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Dean on 06/03/2016.
 */
public class GameTask extends BukkitRunnable {

    private List<Room> rooms;
    private List<Room> roomsRemove = new ArrayList<Room>();
    private Main plugin;
    private Random rand;

    public GameTask(Main plugin, List<Room> rooms){
        this.plugin = plugin;
        this.rooms = rooms;
        rand = new Random();
    }

    public void run() {
        int nbGoodSpawn = rand.nextInt(5);
        for(Room r : rooms){
            if(r.errors >= 3) {
                r.lose();
                roomsRemove.add(r);
            }
            new SpawnTask(r, nbGoodSpawn).runTask(plugin);
            for(PNJ pnj : r.currentPNJ){
                if(pnj.getNavigation().m()){
                    BlockPosition pnjPos = new BlockPosition(pnj.locX, pnj.locY, pnj.locZ);
                    BlockPosition objPos = new BlockPosition(pnj.objective.getBlockX(), pnj.objective.getBlockY(), pnj.objective.getBlockZ());
                    if(!(pnjPos.getX() == objPos.getX() && pnjPos.getY() == objPos.getY() && pnjPos.getZ() == objPos.getZ())) {
                        pnj.setHealth(0);
                        if(pnj.good){
                            r.scoreError();
                        }
                        r.toRemove.add(pnj);
                        continue;
                    }
                    if(pnj.good){
                        r.scorePoint(1);
                    }
                    else r.scoreError();
                    r.toRemove.add(pnj);
                    pnj.die();
                }
            }
            r.currentPNJ.removeAll(r.toRemove);
        }
        rooms.removeAll(roomsRemove);
        if(plugin.stop)
            this.cancel();
    }
    
    private class SpawnTask extends BukkitRunnable{

        private Room room;
        private int nbGood;

        protected SpawnTask(Room rooms, int nbGood){
            this.room = rooms;
            this.nbGood = nbGood;
            rand = new Random();
        }

        public void run() {
            int spawnNb = rand.nextInt(room.villagerSpawns.size());
            Location l = room.villagerSpawns.get(spawnNb);
            World mcWorld = ((CraftWorld) l.getWorld()).getHandle();
            PNJ pnj = new PNJ(mcWorld, room.fencesLocations.get(spawnNb), rand.nextInt(room.villagerSpawns.size()) > nbGood);
            pnj.setLocation(l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch());
            mcWorld.addEntity(pnj, CreatureSpawnEvent.SpawnReason.CUSTOM);
            room.addPNJ(pnj);
        }
    }
}
