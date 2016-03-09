package com.github.keyzou.sortemall.runnable;

import com.github.keyzou.sortemall.Main;
import com.github.keyzou.sortemall.api.entities.PNJ;
import com.github.keyzou.sortemall.api.Room;
import net.minecraft.server.v1_8_R3.BlockPosition;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class VerificationTask extends BukkitRunnable {

    /**
     * La liste des salles à vérifier
     */
    private List<Room> rooms = new ArrayList<Room>();
    /**
     * Un lien vers la classe principale (on va en avoir besoin)
     */
    private Main plugin;

    public VerificationTask(Main plugin, List<Room> rooms){
        this.rooms.addAll(rooms); // On injecte les salles directement
        this.plugin = plugin;
    }
    public void run() {
        // Si le plugin doit s'arrêter ou la partie est finie, on arrête la tâche et on nettoie les salles
        if(plugin.mustStop()){
            this.cancel();
            for(Room r : rooms){
                r.destroy(plugin);
            }
            return;
        }

        for(Room r : rooms) { // On va voir dans chaque salle
            for (PNJ pnj : r.getCurrentPNJ()) { // On regarde chaque PNJ
                // Dans les cas où le PNJ est encore dans la liste mais qu'il doit être retiré
                if (r.getToRemove().contains(pnj)) {
                    continue;
                }
                // On va vérifier si le PNJ ne bouge plus et s'il a vécu plus de 10 ticks car quand il spawn il est immobile de base.
                if (pnj.motX == 0 && pnj.motZ == 0 && pnj.getLife() > 10) {
                    BlockPosition pnjPos = new BlockPosition(pnj.locX, pnj.locY, pnj.locZ);
                    BlockPosition objPos = new BlockPosition(pnj.getObjective().getBlockX(), pnj.getObjective().getBlockY(), pnj.getObjective().getBlockZ());
                    compare(pnj, r, pnjPos, objPos);
                    r.getToRemove().add(pnj);
                    pnj.die();
                }
            }
            r.getCurrentPNJ().removeAll(r.getToRemove());
        }
    }


    /**
     * Permet de comparer la position du PNJ avec sa destination
     * @param pnj Le PNJ à comparer
     * @param r La salle dans laquelle le PNJ se situe
     * @param pnjPos Sa position
     * @param objPos La position de sa destination
     */
    private void compare(PNJ pnj, Room r, BlockPosition pnjPos, BlockPosition objPos) {
        if (pnjPos.equals(objPos)) { // Si il a atteint sa destination on marque un point..
            if (pnj.isGood()) // Seulement si c'est un blanc
                r.scorePoint(1);
            else r.scoreError(); // Sinon c'est une erreur
        } else { // S'il a pas atteint sa destination..
            if(pnj.isGood()) // mais que c'est un blanc on lui retire un point
                r.scoreError();
        }
    }
}
