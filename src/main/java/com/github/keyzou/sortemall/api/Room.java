package com.github.keyzou.sortemall.api;

import com.github.keyzou.sortemall.Main;
import com.github.keyzou.sortemall.api.entities.PNJ;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.List;

public class Room {

    /**
     * Le score du joueur
     */
    private int score;
    /**
     * Le nombre d'erreurs du joueur
     */
    private int errors;

    /**
     * Le tableau de score que l'on peut voir dans le jeu
     */
    private Scoreboard scoreboard;
    /**
     * L'objectif qui nous permet de définir les scores / erreurs
     */
    private Objective objective;

    /**
     * Emplacement du spawn du joueur
     */
    private Location spawnPoint;
    /**
     * Joueur associé à la salle
     */
    private Player playerAttached;
    /**
     * Liste contenant les emplacements de spawn des PNJ
     */
    private List<Location> villagerSpawns = new ArrayList<Location>();
    /**
     * Liste contenant l'emplacement des portillons
     */
    private List<Location> fencesLocations = new ArrayList<Location>();

    /**
     * Liste des PNJ villageois qui sont dans le jeu
     */
    private List<PNJ> currentPNJ = new ArrayList<PNJ>();
    /**
     * Liste des PNJ à retirer dès que possible
     */
    private List<PNJ> toRemove = new ArrayList<PNJ>();

    public Room(Location spawnPoint){
        this.spawnPoint = spawnPoint;
    }

    /**
     * Associe un joueur à la salle
     * @param p Le joueur à associer
     */
    public void attachPlayer(Player p){
        playerAttached = p;
    }

    /**
     * Méthode appelée lors du démarrage du jeu, on nettoie la salle pour être sûr, on la génère et on amène le joueur
     */
    public void start(){
        currentPNJ.clear();
        toRemove.clear();
        generateRoom();
        setupScoreboard();
        playerAttached.teleport(spawnPoint);
    }

    /**
     * Mise en place du tableau de score visible pour le joueur
     */
    private void setupScoreboard(){
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        objective = scoreboard.registerNewObjective("minigame", "dummy"); // on appelle notre jeu minigame et on annonce que les scores sont augmentés par le serveur
        objective.setDisplaySlot(DisplaySlot.SIDEBAR); // On met le tableau sur la droite
        objective.setDisplayName(ChatColor.AQUA+"Sort'em All"); // que de beauté
        Score scoreObj = objective.getScore(ChatColor.GOLD +"Score");
        scoreObj.setScore(0);
        Score errorsObj = objective.getScore(ChatColor.RED +"Erreurs");
        errorsObj.setScore(0);
        playerAttached.setScoreboard(scoreboard); // on l'affiche au joueur
    }

    /**
     * On génère la salle avec des emplacement de spawn imposés
     * **Cette méthode changera certainement par la suite**
     */
    private void generateRoom(){
        Location firstSpawn = new Location(spawnPoint.getWorld(), spawnPoint.getX()+8, spawnPoint.getY(), spawnPoint.getZ()-6);
        for(int i = 1; i <= 5; i++){ // on crée 5 spawn
            Location spawn = firstSpawn.add(0, 0, 2);
            villagerSpawns.add(new Location(spawn.getWorld(), spawn.getX(), spawn.getY(), spawn.getZ()));
            Location location = new Location(spawn.getWorld(), spawn.getX()-5, spawn.getY(), spawn.getZ());
            Block b = location.getBlock();
            b.setTypeIdAndData(107, (byte) 1, false); // on met un portillon
            fencesLocations.add(location);
        }
    }

    /**
     * On ajoute un PNJ dans la liste des PNJ vivants
     * @param pnj Le PNJ à ajouter
     */
    public void addPNJ(PNJ pnj){
        currentPNJ.add(pnj);
    }

    /**
     * On fait marquer un point au joueur
     * @param points Nombre de points à marquer
     */
    public void scorePoint(int points){
        score += points;
        Score scoreObj = objective.getScore(ChatColor.GOLD +"Score");
        scoreObj.setScore(this.score);
    }

    /**
     * On incrémente le nombre d'erreurs du joueur
     */
    public void scoreError(){
        errors+=1;
        Score errorsObj = objective.getScore(ChatColor.RED +"Erreurs");
        errorsObj.setScore(this.errors);

    }

    /**
     * On annonce au joueur qu'il a perdu et on appelle la fonction {@link #destroy(Main)}
     * @param plugin Le plugin
     */
    public void lose(Main plugin){
        playerAttached.sendMessage(ChatColor.RED+"Vous avez perdu !");
        destroy(plugin);
    }

    /**
     * Cette fonction va remettre la salle à 0, dissocier le joueur et la salle et renvoyer le joueur dans la file d'attente
     * @param plugin Le plugin
     */
    public void destroy(Main plugin){
        plugin.getWaitingList().add(playerAttached);
        plugin.clearPNJ(currentPNJ);
        plugin.clearPNJ(toRemove);
        if(playerAttached != null) // dans les cas où on détruit la salle après qu'il ai déjà perdu
            playerAttached.teleport(plugin.getWaitingRoom());
        playerAttached = null;
        errors = 0;
        score = 0;
    }

    public int getScore() {
        return score;
    }

    public int getErrors() {
        return errors;
    }

    public Location getSpawnPoint() {
        return spawnPoint;
    }

    public Player getPlayerAttached() {
        return playerAttached;
    }

    public List<Location> getVillagerSpawns() {
        return villagerSpawns;
    }

    public List<Location> getFencesLocations() {
        return fencesLocations;
    }

    public List<PNJ> getCurrentPNJ() {
        return currentPNJ;
    }

    public List<PNJ> getToRemove() {
        return toRemove;
    }
}
