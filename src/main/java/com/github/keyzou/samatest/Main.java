package com.github.keyzou.samatest;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class Main extends JavaPlugin implements Listener {

    private List<Room> rooms = new ArrayList<Room>();
    private List<Player> waitingList = new ArrayList<Player>();

    @Override
    public void onEnable(){
        getServer().getPluginManager().registerEvents(this, this);
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
                for(int i = 0; i < rooms.size() && i < waitingList.size(); i++){
                    rooms.get(i).attachPlayer(waitingList.get(i));
                    rooms.get(i).start();
                }
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        e.getPlayer().setGameMode(GameMode.ADVENTURE);
        waitingList.add(e.getPlayer());
    }
}
