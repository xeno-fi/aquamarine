package fi.xeno.aquamarine.command;

import fi.xeno.aquamarine.AquamarinePermission;
import fi.xeno.aquamarine.XTicketManager;
import fi.xeno.aquamarine.XTicketsPlugin;
import fi.xeno.aquamarine.util.XTicket;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.StringJoiner;

public class CommandTickets implements TabExecutor {
    
    private final XTicketsPlugin plugin;
    private final XTicketManager ticketManager;

    public CommandTickets(XTicketsPlugin plugin, XTicketManager ticketManager) {
        this.plugin = plugin;
        this.ticketManager = ticketManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        
        if (!sender.hasPermission(AquamarinePermission.CREATE_TICKET)) {
            sender.sendMessage(plugin.lang("generic-no-permission"));
            return true;
        }
        
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be executed by a player.");
            return true;
        }
        
        Player player = (Player)sender;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            
            List<XTicket> tickets = ticketManager.getStorage().getTicketsBySender(player.getUniqueId());

            if (tickets.size() == 0) {
                plugin.sendPrefixed(plugin.lang("generic-no-tickets"), player);
                return;
            }
            
            tickets.stream()
                    .filter(xt -> System.currentTimeMillis() - xt.getTimestamp() <= 1000L*60*60*24*14)
                    .forEach(xt -> player.spigot().sendMessage(xt.renderChatPreview(false, true)));
            
        });

        return true;

    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return null;
    }


}
