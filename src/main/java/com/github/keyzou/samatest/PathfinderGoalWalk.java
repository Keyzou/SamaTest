package com.github.keyzou.samatest;

import net.minecraft.server.v1_8_R3.PathfinderGoal;
import org.bukkit.Location;

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
        return false;
    }

    @Override
    public void e(){
        pnj.life++;
    }


}
