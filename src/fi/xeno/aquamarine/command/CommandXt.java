package fi.xeno.aquamarine.command;

import fi.xeno.aquamarine.AquamarinePermission;
import fi.xeno.aquamarine.XTicketManager;
import fi.xeno.aquamarine.XTicketsPlugin;
import fi.xeno.aquamarine.util.TimestampedValue;
import fi.xeno.aquamarine.util.XTicket;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CommandXt implements TabExecutor {
    
    private final XTicketsPlugin plugin;
    private final XTicketManager ticketManager;
    
    private Map<UUID, TimestampedValue<Integer>> lastTicket = new ConcurrentHashMap<>();

    public CommandXt(XTicketsPlugin plugin, XTicketManager ticketManager) {
        
        this.plugin = plugin;
        this.ticketManager = ticketManager;
        
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            
            List<UUID> toRemove = new ArrayList<>();
            
            lastTicket.forEach((k, v) -> {
                if (v.isOlderThan(1000L*60*60)) {
                    toRemove.add(k);
                }
            });
            
            toRemove.forEach(lastTicket::remove);
            
        }, 20L*60*30, 20L*60*30);
        
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        
        if (!sender.hasPermission(AquamarinePermission.STAFF)) {
            sender.sendMessage(plugin.lang("generic-no-permission"));
            return true;
        }
        
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            
            
            
            if (args.length == 0) {
                
                List<XTicket> tickets = ticketManager.getStorage().getWaitingTickets();
                
                return;
                
            }
            
        });

        return true;

    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return null;
    }

}
