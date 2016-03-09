package com.github.keyzou.sortemall.api.ai;

import com.github.keyzou.sortemall.api.entities.PNJ;
import net.minecraft.server.v1_8_R3.PathfinderGoal;
import org.bukkit.Location;

public class PathfinderGoalWalk extends PathfinderGoal {

    /**
     * L'endroit où le PNJ doit aller (portillon)
     */
    private Location objective;
    /**
     * Le famous PNJ
     */
    private PNJ pnj;

    public PathfinderGoalWalk(PNJ pnj, Location objective){
        this.pnj = pnj;
        this.objective = objective;
    }

    /**
     * fonction shouldExecute()
     * @return toujours true parce qu'on veut toujours que le pnj aille droit au but
     */
    @Override
    public boolean a() {
        return true;
    }

    /**
     * Fonction startExecuting(), on défini la destination du pnj sur le portillon
     */
    @Override
    public void c(){
        this.pnj.getNavigation().a(objective.getBlockX(), objective.getBlockY(), objective.getBlockZ(), 0.5f );
    }

    /**
     * Rien ne doit arrêter ce PNJ
     * @return false
     */
    @Override
    public boolean b() {
        return false;
    }

    /**
     * Quand on met à jour son trajet, on le fait vieillir.
     */
    @Override
    public void e(){
        pnj.addLife(1);
    }


}
