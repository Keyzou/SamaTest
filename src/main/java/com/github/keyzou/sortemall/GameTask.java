package com.github.keyzou.sortemall;

import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class GameTask extends BukkitRunnable {

    /**
     * Un lien vers la classe principale (on va en avoir besoin)
     */
    private Main plugin;

    /**
     * La liste des salles à retirer dès que possible (si un joueur a perdu par exemple)
     */
    private List<Room> roomsRemove = new ArrayList<Room>();

    public GameTask(Main plugin) {
        this.plugin = plugin;
    }

    public void run() {
        // Si le plugin doit s'arrêter ou la partie est finie, on arrête la tâche et on nettoie les salles
        if (plugin.stop) {
            this.cancel();
            return;
        }
        plugin.time++; // on incrémente le temps de jeu (cette tâche se répète toutes les secondes)
        if (plugin.time % 30 == 0) { // s'il s'est écoulé 30 secondes
            plugin.getServer().broadcastMessage(ChatColor.GOLD + "On accélère la cadence !");
            /*
            Pour les trois lignes d'en dessous, le délai de départ est 40 ticks. Au bout de 30 secondes, on réduit ce délai de 10 ticks
            puis par la suite on réduit ce délai de 5 ticks par 5 ticks, et on empêche la fréquence de descendre en dessous d'un villageois par seconde.
             */
            plugin.spawnFrequency -= plugin.spawnFrequency < 30 ? 5 : 10;
            if (plugin.spawnFrequency <= 20)
                plugin.spawnFrequency = 20;
        }
        for (Room r : plugin.roomsPlaying) { // Pour chaque salle où quelqu'un joue
            if (roomsRemove.contains(r)) // si la salle doit être retirée on fait pas les traitements
                continue;
            if (r.errors >= 3) { // Si un joueur perd on enregistre son score, on se moque de lui puis on le sort de la salle
                plugin.scores.put(r.playerAttached.getDisplayName(), r.score);
                plugin.getServer().broadcastMessage(r.playerAttached.getDisplayName() + ChatColor.RED + " a perdu !");
                plugin.getServer().broadcastMessage(r.playerAttached.getDisplayName() + ChatColor.RED + " aura tenu " + (plugin.time / 60) + " minutes et " + plugin.time % 60 + " secondes !");
                roomsRemove.add(r);
                r.lose(plugin);
            }
        }
        plugin.roomsPlaying.removeAll(roomsRemove);
        if (plugin.roomsPlaying.size() <= 1) { // Si il reste qu'une seule personne, on arrête la partie.
            plugin.scores.put(plugin.roomsPlaying.get(0).playerAttached.getDisplayName(), plugin.roomsPlaying.get(0).score);
            plugin.stop = true;
            plugin.endGame();
        }
    }
}
