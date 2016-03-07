package com.github.keyzou.samatest;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.EntityInsentient;
import net.minecraft.server.v1_8_R3.EntityTypes;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;

public class Main extends JavaPlugin implements Listener {

    private static Main instance;

    private List<Room> rooms = new ArrayList<Room>();
    private List<Player> waitingList = new ArrayList<Player>();
    protected boolean stop = true;
    protected Scoreboard scoreboard;

    @Override
    public void onEnable(){
        setInstance(this);
        getServer().getPluginManager().registerEvents(this, this);
        registerEntity("CustomVillager", 120, PNJ.class);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if(!(sender instanceof Player)) {
            getLogger().warning("Seulement des joueurs peuvent utiliser les commandes !");
            return false;
        }
        if("st".equalsIgnoreCase(cmd.getName())){
        Player p = (Player) sender;
            if("addRoom".equalsIgnoreCase(args[0])){
                rooms.add(new Room(p.getLocation()));
                p.sendMessage(ChatColor.GREEN+"La salle a été créée avec succès !");
                return true;
            }
            if("start".equalsIgnoreCase(args[0])){
                p.sendMessage(ChatColor.RED+"Ca commence !!");
                for(int i = 0; i < rooms.size(); i++){
                    Room r = rooms.get(i);
                    r.attachPlayer(waitingList.get(i));
                    r.start();
                    waitingList.remove(i);
                    r.playerAttached.sendMessage(ChatColor.BLUE + "La partie démarrera dans 5 secondes !");
                }
                this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {

                    public void run() {
                        getServer().broadcastMessage("La partie a commencé !");
                        stop = false;
                        runGame();
                    }
                }, 100L);// 60 L == 3 sec, 20 ticks == 1 sec
                return true;
            }
            if("stop".equalsIgnoreCase(args[0])){
                p.sendMessage(ChatColor.RED+"Stop !!");
                for(int i = 0; i < rooms.size(); i++){
                    waitingList.add(rooms.get(i).playerAttached);
                    rooms.get(i).playerAttached = null;
                    for(PNJ pnj : rooms.get(i).currentPNJ)
                        pnj.die();
                    rooms.get(i).currentPNJ.clear();
                    for(PNJ pnj : rooms.get(i).toRemove)
                        pnj.die();
                    rooms.get(i).toRemove.clear();
                    stop = true;
                }
                return true;
            }
        }
        return false;
    }

    private void runGame(){
        GameTask game = new GameTask(this, rooms);
        game.runTaskTimer(this, 20, 20);
    }

    public void registerEntity(String name, int id, Class<? extends EntityInsentient> customClass){

        try {
            List<Map<?, ?>> dataMap = new ArrayList<Map<?, ?>>();
            for (Field f : EntityTypes.class.getDeclaredFields()){
                if (f.getType().getSimpleName().equals(Map.class.getSimpleName())){
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

    public static Main getInstance(){
        return instance;
    }

    public void setInstance(Main main){
        instance = main;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        e.getPlayer().setGameMode(GameMode.CREATIVE);
        waitingList.add(e.getPlayer());
    }
}
