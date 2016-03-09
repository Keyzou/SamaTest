package com.github.keyzou.sortemall.api.entities;

import com.github.keyzou.sortemall.api.ai.PathfinderGoalWalk;
import net.minecraft.server.v1_8_R3.EntityVillager;
import net.minecraft.server.v1_8_R3.PathfinderGoalSelector;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.util.UnsafeList;

import java.lang.reflect.Field;
import java.util.logging.Level;


public class PNJ extends EntityVillager {

    /**
     * La destination du PNJ
     */
    private Location objective;
    /**
     * Pour savoir si c'est un bon ou un mauvais PNJ
     */
    private boolean good;
    /**
     * Nombre de ticks pendant lequel le PNJ a vécu
     */
    private int life;

    public PNJ(World world, Location obj, boolean good) {
        super(world);
        this.objective = obj;
        this.good = good;
        /*
        Par la suite on utilise la reflection pour récupérer l'AI du PNJ et la redéfinir.
         */
        try {
            Field bField = PathfinderGoalSelector.class.getDeclaredField("b");
            bField.setAccessible(true);
            Field cField = PathfinderGoalSelector.class.getDeclaredField("c");
            cField.setAccessible(true);
            bField.set(goalSelector, new UnsafeList<PathfinderGoalSelector>());
            bField.set(targetSelector, new UnsafeList<PathfinderGoalSelector>());
            cField.set(goalSelector, new UnsafeList<PathfinderGoalSelector>());
            cField.set(targetSelector, new UnsafeList<PathfinderGoalSelector>());
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "Erreur mdr", e);
        }
        this.setProfession(good ? 1 : 2); // 1 = vêtement blanc / 2 = vêtement violet
        this.goalSelector.a(0, new PathfinderGoalWalk(this, objective)); // On rend notre PNJ intelligent
    }

    /**
     * Permet de récupérer la destination du PNJ
     * @return la destination du pnj
     */
    public Location getObjective() {
        return objective;
    }

    /**
     * Permet de récupérer le nombre de ticks durant lequel le PNJ a vécu
     * @return la "vie" du pnj
     */
    public int getLife() {
        return life;
    }

    /**
     * Permet de savoir si un pnj est bon ou mauvais
     * @return true si bon sinon false
     */
    public boolean isGood() {
        return good;
    }

    /**
     * Ajoute de la "vie" au PNJ (s'incrémente de 1 à chaque tick)
     * @param life la vie à ajouter
     */
    public void addLife(int life) {
        this.life += life;
    }
}
