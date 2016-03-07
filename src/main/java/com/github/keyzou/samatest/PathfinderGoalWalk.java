package com.github.keyzou.samatest;

import net.minecraft.server.v1_8_R3.PathfinderGoal;
import org.bukkit.Location;

/**
 * Created by Dean on 06/03/2016.
 */
public class PathfinderGoalWalk extends PathfinderGoal {

    private Location objective;
    private PNJ pnj;

    public PathfinderGoalWalk(PNJ pnj, Location objective){
        this.pnj = pnj;
        this.objective = objective;
    }

    @Override
    public boolean a() {
        return true;
    }

    @Override
    public void c(){
        this.pnj.getNavigation().a(objective.getBlockX(), objective.getBlockY(), objective.getBlockZ(), 0.5f );
    }

    @Override
    public boolean b() {
        return !this.pnj.getNavigation().m();
    }

}
