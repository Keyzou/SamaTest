package com.github.keyzou.samatest;

import net.minecraft.server.v1_8_R3.EntityVillager;
import net.minecraft.server.v1_8_R3.PathEntity;
import net.minecraft.server.v1_8_R3.PathfinderGoalSelector;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.util.UnsafeList;

import java.lang.reflect.Field;
import java.util.logging.Level;


public class PNJ extends EntityVillager {

    protected Location objective;
    protected boolean good;
    protected double speed;
    protected int life;

    protected PathEntity path;

    public PNJ(World world, Location obj, boolean good) {
        super(world);
        this.objective = obj;
        this.good = good;
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
            Main.instance.getLogger().log(Level.SEVERE, "Erreur !", e);
        }
        this.setProfession(good ? 1 : 2);
        this.goalSelector.a(0, new PathfinderGoalWalk(this, objective));
        this.path = getNavigation().a(objective.getBlockX(), objective.getBlockY(), objective.getBlockZ());
    }

}
