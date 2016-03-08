package com.github.keyzou.samatest;

import com.github.keyzou.samatest.Main;
import com.github.keyzou.samatest.PNJ;
import com.github.keyzou.samatest.Room;
import net.minecraft.server.v1_8_R3.BlockPosition;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class VerificationTask extends BukkitRunnable {
    private List<Room> rooms = new ArrayList<Room>();
    private Main plugin;

    public VerificationTask(Main plugin, List<Room> rooms){
        this.rooms.addAll(rooms);
        this.plugin = plugin;
    }
    public void run() {
        if(plugin.stop){
            this.cancel();
            for(Room r : rooms){
                r.destroy(plugin);
            }
            return;
        }
        for(Room r : rooms) {
            for (PNJ pnj : r.currentPNJ) {
                if (r.toRemove.contains(pnj)) {
                    continue;
                }
                if (pnj.motX == 0 && pnj.motZ == 0 && pnj.life > 10) { // le pnj ne bouge plus
                    BlockPosition pnjPos = new BlockPosition(pnj.locX, pnj.locY, pnj.locZ);
                    BlockPosition objPos = new BlockPosition(pnj.objective.getBlockX(), pnj.objective.getBlockY(), pnj.objective.getBlockZ());
                    compare(pnj, r, pnjPos, objPos);
                    r.toRemove.add(pnj);
                    pnj.die();
                }
            }
            r.currentPNJ.removeAll(r.toRemove);
        }
    }

    private void compare(PNJ pnj, Room r, BlockPosition pnjPos, BlockPosition objPos) {
        if (pnjPos.equals(objPos)) { // on marque un point
            if (pnj.good)
                r.scorePoint(1);
            else r.scoreError();
        } else {
            if(pnj.good)
                r.scoreError();
        }
    }
}
