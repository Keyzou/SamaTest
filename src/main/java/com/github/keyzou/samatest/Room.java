package com.github.keyzou.samatest;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.List;

public class Room {

    protected int score;
    protected int errors;

    private Scoreboard scoreboard;
    private Objective objective;

    private Location spawnPoint;
    protected Player playerAttached;
    protected List<Location> villagerSpawns = new ArrayList<Location>();
    protected List<Location> fencesLocations = new ArrayList<Location>();

    protected List<PNJ> currentPNJ = new ArrayList<PNJ>();
    protected List<PNJ> toRemove = new ArrayList<PNJ>();

    public Room(Location spawnPoint){
        this.spawnPoint = spawnPoint;
    }

    public void attachPlayer(Player p){
        playerAttached = p;
    }

    public void start(){
        generateRoom();
        setupScoreboard();
        playerAttached.teleport(spawnPoint);
    }

    private void setupScoreboard(){
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        objective = scoreboard.registerNewObjective("minigame", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(ChatColor.AQUA+"SamaTest");
        Score score = objective.getScore(ChatColor.GOLD +"Score");
        score.setScore(0);
        Score errors = objective.getScore(ChatColor.RED +"Erreurs");
        errors.setScore(0);
        playerAttached.setScoreboard(scoreboard);
    }

    private void generateRoom(){
        new Location(spawnPoint.getWorld(), spawnPoint.getX(), spawnPoint.getY()-1, spawnPoint.getZ()).getBlock().setType(Material.DIAMOND_BLOCK);
        Location firstSpawn = new Location(spawnPoint.getWorld(), spawnPoint.getX()+8, spawnPoint.getY(), spawnPoint.getZ()-6);
        for(int i = 1; i <= 5; i++){
            Location spawn = firstSpawn.add(0, 0, 2);
            villagerSpawns.add(new Location(spawn.getWorld(), spawn.getX(), spawn.getY(), spawn.getZ()));
            new Location(spawn.getWorld(), spawn.getX(), spawn.getY()-1, spawn.getZ()).getBlock().setType(Material.WOOL);
            for(int j = 1; j <= 4; j++){
                new Location(spawn.getWorld(), spawn.getX()-j, spawn.getY()-1, spawn.getZ()).getBlock().setType(Material.WOOD);
            }
            Location location = new Location(spawn.getWorld(), spawn.getX()-5, spawn.getY(), spawn.getZ());
            Block b = location.getBlock();
            b.setTypeIdAndData(107, (byte) 1, false);
            fencesLocations.add(location);
            b = new Location(spawn.getWorld(), spawn.getX()-5, spawn.getY()-1, spawn.getZ()).getBlock();
            b.setTypeIdAndData(35, (byte) 14, false);
            new Location(spawn.getWorld(), spawn.getX()-5, spawn.getY(), spawn.getZ()-1).getBlock().setType(Material.FENCE);
        }
    }

    public void addPNJ(PNJ pnj){
        currentPNJ.add(pnj);
    }

    public void removePNJ(PNJ pnj){
        currentPNJ.remove(pnj);
    }

    public void scorePoint(int points){
        score += points;
        Score score = objective.getScore(ChatColor.GOLD +"Score");
        score.setScore(this.score);
    }

    public void scoreError(){
        errors++;
        Score errors = objective.getScore(ChatColor.RED +"Erreurs");
        errors.setScore(this.errors);

    }

    public void lose(){
        playerAttached.sendMessage(ChatColor.RED+"Vous avez perdu !");
        // TODO: Waiting Room
    }
}
