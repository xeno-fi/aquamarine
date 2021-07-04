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

public class CommandTicket implements TabExecutor {
    
    private final XTicketsPlugin plugin;
    private final XTicketManager ticketManager;

    public CommandTicket(XTicketsPlugin plugin, XTicketManager ticketManager) {
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
        
        if (args.length == 0) {
            sender.sendMessage(plugin.lang("command-ticket-usage").replace("%label%", label));
            return true;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {

            StringJoiner msgJoiner = new StringJoiner(" ");
            for (String s:args) msgJoiner.add(s);

            Player player = (Player)sender;

            switch (ticketManager.canCreateTicket(player)) {

                case DENY_NEARBY:
                    plugin.sendPrefixed(plugin.lang("ticket-deny-nearby"), player);
                    return;

                case DENY_PLAYER:
                    plugin.sendPrefixed(plugin.lang("ticket-deny-player"), player);
                    return;

            }
            
            XTicket ticket = ticketManager.createTicket(player, msgJoiner.toString().trim());
            
            plugin.sendPrefixed(plugin.lang("ticket-created").replace("%ticketId%", ""+ticket.getId()));
            
        });

        return true;

    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return null;
    }


}
