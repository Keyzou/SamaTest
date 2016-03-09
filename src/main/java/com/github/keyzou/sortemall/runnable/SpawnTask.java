package com.github.keyzou.sortemall.runnable;

import com.github.keyzou.sortemall.Main;
import com.github.keyzou.sortemall.api.entities.PNJ;
import com.github.keyzou.sortemall.api.Room;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Random;
public class SpawnTask extends BukkitRunnable {

    /**
     * Liste des salles sur lesquelles agir
     */
    private List<Room> rooms;
    /**
     * Lien vers le plugin (on va en avoir besoin)
     */
    private Main plugin;
    /**
     * Objet random pour obtenir des nombre aléatoires
     */
    private Random rand;

    public SpawnTask(Main plugin, List<Room> rooms){
        this.rooms = rooms;
        this.plugin = plugin;
        rand = new Random();
    }

    public void run() {
        // Si la partie est finie ou le plugin doit s'arrêter
        if(plugin.mustStop()){
            this.cancel();
            return;
        }
        int nbGood = rand.nextInt(5); // Nombre de PNJ bons entre 0 et 5
        // Ce nombre est là pour qu'il y ai autant de bon PNJ qui spawn par salle pour pas qu'il y ai de soucis d'équité
        for(Room room : rooms) {
            int spawnNb = rand.nextInt(room.getVillagerSpawns().size()); // le numéro de spawn sur lequel le PNJ va pop
            Location l = room.getVillagerSpawns().get(spawnNb);
            World mcWorld = ((CraftWorld) l.getWorld()).getHandle(); // Comme c'est une entité NMS, on doit la faire spawn avec le monde du NMS
            PNJ pnj = new PNJ(mcWorld, room.getFencesLocations().get(spawnNb),rand.nextInt(room.getVillagerSpawns().size()) > nbGood);
            pnj.setLocation(l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch());
            mcWorld.addEntity(pnj, CreatureSpawnEvent.SpawnReason.CUSTOM);
            room.addPNJ(pnj); // on l'ajoute à la liste des PNJ
        }
        // Cette tâche est récursive, elle va s'appeller de plus en plus rapidement car en parallèle plugin.spawnFrequency est modifié
        SpawnTask spawnTask = new SpawnTask(plugin, rooms);
        spawnTask.runTaskLater(plugin, plugin.getSpawnFrequency());
    }
}
