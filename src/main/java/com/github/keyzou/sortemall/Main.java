package com.github.keyzou.sortemall;
import com.github.keyzou.sortemall.api.entities.PNJ;
import com.github.keyzou.sortemall.api.Room;
import com.github.keyzou.sortemall.runnable.GameTask;
import com.github.keyzou.sortemall.runnable.SpawnTask;
import com.github.keyzou.sortemall.runnable.VerificationTask;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.EntityInsentient;
import net.minecraft.server.v1_8_R3.EntityTypes;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class Main extends JavaPlugin implements Listener {

    /**
     * Liste des salles (toutes les salles, à ne pas confondre avec roomsPlaying: les salles où un joueur joue)
     */
    private List<Room> rooms = new ArrayList<Room>();
    /**
     * Liste des joueurs en attente
     */
    private List<Player> waitingList = new ArrayList<Player>();
    /**
     * Emplacement de la salle d'attente
     */
    private Location waitingRoom;

    /**
     * Si le jeu doit être arrêté ou pas
     */
    private boolean stop;
    /**
     * Compte à rebours utilisé pour le début et la fin du jeu
     */
    private int countdown = 5;
    /**
     * ID du Runnable qui effectue le compte à rebours
     */
    private int countdownID;

    /**
     * Fréquence d'apparition des villageois (en ticks, 20 ticks = 1 seconde)
     */
    private long spawnFrequency = 40L;
    /**
     * Liste des salles où un joueur est entrain de jouer
     */
    private List<Room> roomsPlaying = new ArrayList<Room>();
    /**
     * Matrice permettant d'associer un score à un joueur
     */
    private Map<String, Integer> scores = new HashMap<String, Integer>();
    /**
     * Temps écoulé depuis le début de la partie (en secondes). Utilisé lorsqu'un joueur perd la partie.
     */
    private int time = 0;

    /**
     * Pour pas que SolarLint nous fasse un caca nerveux
     */
    private String worldName = "world";

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this); // On a qu'un seul event à gérer donc on le fait dans cette classe
        registerEntity("CustomVillager", 120, PNJ.class); // On enregistre notre villageois spécial
        this.waitingRoom = new Location(getServer().getWorld(worldName), 0, getServer().getWorld(worldName).getHighestBlockYAt(0,0), 0);
    }

    /**
     * Méthode qui va initialiser les salles, affecter les joueurs aux salles puis enfin lancer la partie au bout de 5 secondes
     */
    private void startGame(){
        for(int i = 0;i <waitingList.size();i++){
            rooms.add(new Room(new Location(getServer().getWorld(worldName), 20*i, getServer().getWorld(worldName).getHighestBlockYAt(10*rooms.size(), 20), 20)));
        }
        List<Player> toRemove = new ArrayList<Player>(); // on stocke les joueurs qui vont jouer
        for (int i = 0; i < rooms.size(); i++) { // la boucle s'arrête s'il y a plus de salle à traiter ou alors plus de joueurs
            if(i >= waitingList.size())
                continue;
            Room r = rooms.get(i);
            r.attachPlayer(waitingList.get(i));
            r.start();
            toRemove.add(waitingList.get(i));
        }
        waitingList.removeAll(toRemove); // on retire de la file tous les joueurs qui joue
        countdown = 5;
        // Création d'un tâche pour faire le compte à rebours
        countdownID = this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {

            public void run() {
                if (countdown == 0) {
                    getServer().getScheduler().cancelTask(countdownID);
                    getServer().broadcastMessage(ChatColor.GREEN + "La partie a commencé !");
                    stop = false;
                    runGame();
                } else {
                    getServer().broadcastMessage(ChatColor.BLUE + "La partie commence dans " + countdown + " secondes !");
                    countdown--;
                }
            }
        }, 0L, 20L);// 20 ticks == 1 sec
    }

    /**
     * Petite fonction permettant de tuer les PNJ d'une liste puis de la vider
     * @param list Liste de PNJ
     */
    public void clearPNJ(List<PNJ> list){
        for (PNJ pnj : list)
            pnj.die();
        list.clear();
    }

    /**
     * Fonction qui lance les différentes tâches qui vont faire fonctionner le jeu
     */
    private void runGame(){
        roomsPlaying.addAll(rooms);
        time = 0;
        GameTask gameTask = new GameTask(this);
        gameTask.runTaskTimer(this, 0L, 20L);
        // Task vérif
        SpawnTask spawnTask = new SpawnTask(this, roomsPlaying);
        spawnTask.runTaskLater(this, spawnFrequency);
        // Task spawn variable
        VerificationTask verifTask = new VerificationTask(this, roomsPlaying);
        verifTask.runTaskTimer(this, 0L, 3L); // ~ 2 secondes pour laisser le temps au pnj retardataires de rentrer dans la case

    }

    /**
     * Permet d'arrêter le jeu, puis d'éteindre le serveur après 5 secondes
     */
    public void endGame(){
        Map.Entry<String,Integer> maxEntry = null;

        for(Map.Entry<String,Integer> entry : scores.entrySet()) {
            if (maxEntry == null || entry.getValue() > maxEntry.getValue()) {
                maxEntry = entry;
            }
        }

       getServer().broadcastMessage(ChatColor.GOLD+"La partie est terminée !");
        if (maxEntry != null) {
            getServer().broadcastMessage(ChatColor.GOLD+"Le gagnant est "+maxEntry.getKey()+" avec "+maxEntry.getValue()+" points !");
        }
        countdown = 5;
        countdownID = this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {

            public void run() {
                if(countdown == 0) {
                    getServer().getScheduler().cancelTask(countdownID);
                    getServer().shutdown();
                }
                else{
                    getServer().broadcastMessage(ChatColor.BLUE+"Vous allez être téléporté dans "+countdown+" secondes");
                    countdown--;
                }
            }
        }, 0L, 20L);// 60 L == 3 sec, 20 ticks == 1 sec
    }

    /**
     * Fonction permettant d'enregistrer une entité customisée dans le coeur du serveur par le biais de la reflection
     * Cette fonction a été prise d'un tutoriel sur NMS et les entités:
     * https://www.spigotmc.org/threads/tutorial-register-and-use-nms-entities-1-8.77607/
     * Concernant l'ID de l'entité, on a juste un villageois avec une AI simple, donc on lui attribue l'ID d'un villageois classique.
     * @param name Nom de l'entité
     * @param id ID de l'entité
     * @param customClass Classe de l'entité custom
     */
    public void registerEntity(String name, int id, Class<? extends EntityInsentient> customClass){

        try {
            List<Map<?, ?>> dataMap = new ArrayList<Map<?, ?>>();
            for (Field f : EntityTypes.class.getDeclaredFields()){
                if (Map.class.isAssignableFrom(f.getType())){
                    f.setAccessible(true);
                    dataMap.add((Map<?, ?>) f.get(null));
                }
            }

            if (dataMap.get(2).containsKey(id)){
                dataMap.get(0).remove(name);
                dataMap.get(2).remove(id);
            }

        Method method = EntityTypes.class.getDeclaredMethod("a", Class.class, String.class, int.class);
        method.setAccessible(true);
         method.invoke(null, customClass, name, id);
        } catch (Exception e){
            Bukkit.getLogger().log(Level.SEVERE, "Erreur !", e);
        }


    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        e.getPlayer().setGameMode(GameMode.ADVENTURE); // ça serait bête qu'il casse des blocs #TravailEnMoins
        e.getPlayer().sendMessage(ChatColor.BLUE+"Vous avez été placé dans la file d'attente. Dès qu'il y aura suffisament de joueurs, le jeu se lancera automatiquement");
        waitingList.add(e.getPlayer()); // On met le joueur dans la file d'attente
        e.getPlayer().teleport(waitingRoom);
        if(waitingList.size() > 1)
            startGame();
    }
    /**
     * Pour pouvoir accéder aux salles de l'extérieur
     * @return Les salles
     */
    public List<Room> getRooms() {
        return rooms;
    }

    /**
     * Pour pouvoir accéder aux salles de l'extérieur
     * @return Les salles
     */
    public List<Player> getWaitingList() {
        return waitingList;
    }

    /**
     * Pour pouvoir accéder aux salles de l'extérieur
     * @return Les salles
     */
    public boolean mustStop() {
        return stop;
    }

    /**
     * Renvoie l'emplacement de la salle d'attente
     * @return emplacement de la salle d'attente
     */
    public Location getWaitingRoom() {
        return waitingRoom;
    }

    /**
     * Permet d'arrêter le jeu au prochain tick ou pas
     * @param stop true si le jeu doit s'arrêter false sinon
     */
    public void setStop(boolean stop) {
        this.stop = stop;
    }

    /**
     * Pour pouvoir accéder aux salles de l'extérieur
     * @return Les salles
     */
    public List<Room> getRoomsPlaying() {
        return roomsPlaying;
    }

    /**
     * Pour pouvoir accéder aux salles de l'extérieur
     * @return Les salles
     */
    public Map<String, Integer> getScores() {
        return scores;
    }

    /**
     * Pour pouvoir accéder aux salles de l'extérieur
     * @return Les salles
     */
    public int getTime() {
        return time;
    }

    /**
     * Permet d'incrémenter le temps (en général de 1 seconde)
     * @param nb augmenter de combien ?
     */
    public void addTime(int nb){
        time += nb;
    }

    /**
     * Pour pouvoir accéder aux salles de l'extérieur
     * @return Les salles
     */
    public long getSpawnFrequency() {
        return spawnFrequency;
    }

    /**
     * Réduit le taux de spawn par le nombre entré en paramètre
     * @param amount réduire de combien ?
     */
    public void reduceSpawnFrequency(int amount){
        this.spawnFrequency -= amount;
        if(spawnFrequency < 20)
            spawnFrequency = 20;
    }
}
