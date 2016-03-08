package com.github.keyzou.samatest;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.EntityInsentient;
import net.minecraft.server.v1_8_R3.EntityTypes;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class Main extends JavaPlugin implements Listener {

    public static final Main instance = new Main();

    private List<Room> rooms = new ArrayList<Room>();
    protected List<Player> waitingList = new ArrayList<Player>();
    protected Location waitingRoom;
    protected boolean stop = true;
    protected Scoreboard scoreboard;
    private int countdown = 5;
    private int countdownID;


    protected long spawnFrequency = 40L;
    protected List<Room> roomsPlaying = new ArrayList<Room>();
    protected Map<String, Integer> scores = new HashMap<String, Integer>();
    protected int time = 0;


    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        registerEntity("CustomVillager", 120, PNJ.class);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            getLogger().warning("Seulement des joueurs peuvent utiliser les commandes !");
            return false;
        }
        if ("st".equalsIgnoreCase(cmd.getName())) {
            Player p = (Player) sender;
            return processCommand(p, args[0]);
        }
        return false;
    }

    public boolean processCommand(Player p, String arg) {
        if ("addRoom".equalsIgnoreCase(arg)) {
            rooms.add(new Room(p.getLocation()));
            p.sendMessage(ChatColor.GREEN + "La salle a été créée avec succès !");
            return true;
        }
        if ("start".equalsIgnoreCase(arg)) {
            return processStartCommand();
        }
        if ("addWaiting".equalsIgnoreCase(arg)) {
            waitingRoom = p.getLocation();
            p.sendMessage(ChatColor.GREEN + "La salle d'attente a été créée avec succès !");
            return true;
        }
        if ("stop".equalsIgnoreCase(arg)) {
            p.sendMessage(ChatColor.RED + "Stop !!");
            for (Room r : rooms) {
                waitingList.add(r.playerAttached);
                r.playerAttached = null;
                clearPNJ(r.currentPNJ);
                clearPNJ(r.toRemove);
                stop = true;
            }
            return true;
        }
        return false;
    }

    private boolean processStartCommand(){
        for (int i = 0; i < rooms.size() && i < waitingList.size(); i++) {
            Room r = rooms.get(i);
            r.attachPlayer(waitingList.get(i));
            r.start();
            waitingList.remove(i);
        }
        countdown = 5;
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
        }, 0L, 20L);// 60 L == 3 sec, 20 ticks == 1 sec
        return true;
    }

    private void clearPNJ(List<PNJ> list){
        for (PNJ pnj : list)
            pnj.die();
        list.clear();
    }

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
            instance.getLogger().log(Level.SEVERE, "Erreur !", e);
        }


    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        e.getPlayer().setGameMode(GameMode.ADVENTURE);
        waitingList.add(e.getPlayer());
    }
}
