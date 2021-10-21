package fi.xeno.aquamarine.command;

import fi.xeno.aquamarine.AquamarinePermission;
import fi.xeno.aquamarine.XText;
import fi.xeno.aquamarine.XTicketManager;
import fi.xeno.aquamarine.XTicketsPlugin;
import fi.xeno.aquamarine.util.TimestampedValue;
import fi.xeno.aquamarine.util.XTicket;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
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
            plugin.sendPrefixed(plugin.lang("generic-no-permission"), sender);
            return true;
        }
        
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            
            if (args.length == 0) {
                
                List<XTicket> tickets = ticketManager.getStorage().getWaitingTickets();

                if (tickets.size() == 0) {
                    plugin.sendPrefixed(plugin.lang("generic-no-tickets"), sender);
                    return;
                }
                
                tickets.forEach(xt -> sender.spigot().sendMessage(xt.renderChatPreview(true)));
                
                return;
                
            }
            
            switch (args[0].toLowerCase(Locale.ROOT)) {
                
                case "help": {
                    plugin.sendPrefixed("§b/" + label + " §7help, view, goto, solve, player, all");
                    break;
                }
                
                case "all": {

                    int page = 0;
                    if (args.length > 1) {
                        try {
                            page = Integer.parseInt(args[1])-1;
                        } catch (NumberFormatException e) {
                            plugin.sendPrefixed(plugin.lang("generic-invalid-number").replaceAll("\\$n\\$", args[1]), sender);
                            return;
                        }
                    }

                    List<XTicket> tickets = ticketManager.getStorage().getTickets();

                    if (tickets.size() == 0) {
                        plugin.sendPrefixed(plugin.lang("generic-no-tickets"), sender);
                        return;
                    }

                    int perPage = 8;
                    int totalPages = (int)Math.ceil((double)tickets.size() / (double)perPage);

                    tickets.stream()
                            .skip((long)page*perPage)
                            .limit(perPage)
                            .forEach(xt -> sender.spigot().sendMessage(xt.renderChatPreview(true)));

                    ComponentBuilder paginationMenu = new ComponentBuilder();

                    if (page > 0)
                        paginationMenu.append(XText.commandButton("§8[§b‹§8] ", "§7" + plugin.lang("generic-previous"), "/" + label + " all " + (page-1)));

                    if (page+1 < totalPages)
                        paginationMenu.append(XText.commandButton("§8[§b›§8] ", "§7" + plugin.lang("generic-next"), "/" + label + " all " + (page+1)));

                    paginationMenu.append(XText.hoverText(plugin.lang("generic-page") + "§b" + (page+1) + "/" + totalPages, "§r"));
                    sender.spigot().sendMessage(paginationMenu.create());

                    break;
                    
                }
                
                case "player":
                case "from": {
                    
                    if (args.length < 2) {
                        plugin.sendPrefixed(plugin.lang("command-ticket-player-usage").replaceAll("\\$label\\$", label), sender);
                        return;
                    }
                    
                    String playerName = args[1];
                    OfflinePlayer ofp = Bukkit.getOfflinePlayer(playerName);
                    
                    if (!ofp.hasPlayedBefore() && !ofp.isOnline()) {
                        plugin.sendPrefixed(plugin.lang("generic-invalid-player").replaceAll("\\$player\\$", playerName), sender);
                        return;
                    }
                    
                    int page = 0;
                    if (args.length > 2) {
                        try {
                            page = Integer.parseInt(args[2]);
                        } catch (NumberFormatException e) {
                            plugin.sendPrefixed(plugin.lang("generic-invalid-number").replaceAll("\\$n\\$", args[1]), sender);
                            return;
                        }
                    }
                    
                    List<XTicket> tickets = ticketManager.getStorage().getTicketsBySender(ofp.getUniqueId());
                    
                    if (tickets.size() == 0) {
                        plugin.sendPrefixed(plugin.lang("generic-no-tickets"), sender);
                        return;
                    }
                    
                    int perPage = 8;
                    int totalPages = (int)Math.ceil((double)tickets.size() / (double)perPage);
                    
                    tickets.stream()
                            .skip((long)page*perPage)
                            .limit(perPage)
                            .forEach(xt -> sender.spigot().sendMessage(xt.renderChatPreview(true)));

                    ComponentBuilder paginationMenu = new ComponentBuilder();
                    
                    if (page > 0)
                        paginationMenu.append(XText.commandButton("§8[§b‹§8] ", "§7" + plugin.lang("generic-previous"), "/" + label + " player " + ofp.getName() + " " + (page-1)));
                    
                    if (page+1 < totalPages)
                        paginationMenu.append(XText.commandButton("§8[§b›§8] ", "§7" + plugin.lang("generic-next"), "/" + label + " player " + ofp.getName() + " " + (page+1)));
                    
                    paginationMenu.append(XText.hoverText(plugin.lang("generic-page") + "§b" + (page+1) + "/" + totalPages, "§r"));
                    sender.spigot().sendMessage(paginationMenu.create());
                    
                    break;
                    
                }
                
                case "view":
                case "info": 
                case "show": {

                    if (args.length < 2) {
                        plugin.sendPrefixed(plugin.lang("command-ticket-view-usage").replaceAll("\\$label\\$", label), sender);
                        return;
                    }

                    int ticketId;
                    try {
                        ticketId = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        plugin.sendPrefixed(plugin.lang("generic-invalid-number").replaceAll("\\$n\\$", args[1]), sender);
                        return;
                    }

                    Optional<XTicket> ticketOptional = ticketManager.getStorage().getTicketByNumber(ticketId);
                    if (!ticketOptional.isPresent()) {
                        plugin.sendPrefixed(plugin.lang("generic-ticket-not-found"), sender);
                        return;
                    }

                    XTicket ticket = ticketOptional.get();
                    sender.spigot().sendMessage(ticket.renderChatPreview(true, true));
                    
                    break;
                    
                }
                
                case "goto": {
                    
                    if (!(sender instanceof Player)) {
                        plugin.sendPrefixed(plugin.lang("generic-not-player"), sender);
                        return;
                    }

                    if (args.length < 2) {
                        plugin.sendPrefixed(plugin.lang("command-ticket-goto-usage").replaceAll("\\$label\\$", label), sender);
                        return;
                    }

                    int ticketId;
                    try {
                        ticketId = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        plugin.sendPrefixed(plugin.lang("generic-invalid-number").replaceAll("\\$n\\$", args[1]), sender);
                        return;
                    }

                    Optional<XTicket> ticketOptional = ticketManager.getStorage().getTicketByNumber(ticketId);
                    if (!ticketOptional.isPresent()) {
                        plugin.sendPrefixed(plugin.lang("generic-ticket-not-found"), sender);
                        return;
                    }

                    XTicket ticket = ticketOptional.get();
                    Player player = (Player)sender;
                    Location location = ticket.getLocation().toLocation();

                    if (location == null || !location.isWorldLoaded()) {
                        plugin.sendPrefixed(plugin.lang("generic-invalid-ticket"), sender);
                        return;
                    }

                    if (!location.getChunk().isLoaded())
                        location.getChunk().load();

                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        player.teleport(location);
                        plugin.sendPrefixed(plugin.lang("command-ticket-goto-teleport"), sender);
                        player.playSound(player.getLocation(), "entity.enderman.teleport", 1f, 1f);
                        player.playSound(player.getLocation(), "entity.endermen.teleport", 1f, 1f);
                    }, 2L);
                    
                    break;
                    
                }
                    
                case "solve": {
                    
                    if (args.length < 2) {
                        plugin.sendPrefixed(plugin.lang("command-ticket-solve-usage").replaceAll("\\$label\\$", label), sender);
                        return;
                    }
                    
                    StringJoiner commentJoiner = new StringJoiner(" ");
                    Arrays.stream(args).skip(2).forEach(commentJoiner::add);
                    String comment = commentJoiner.toString();

                    int ticketId;
                    try {
                        ticketId = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        plugin.sendPrefixed(plugin.lang("generic-invalid-number").replaceAll("\\$n\\$", args[1]), sender);
                        return;
                    }

                    Optional<XTicket> ticketOptional = ticketManager.getStorage().getTicketByNumber(ticketId);
                    if (!ticketOptional.isPresent()) {
                        plugin.sendPrefixed(plugin.lang("generic-ticket-not-found"), sender);
                        return;
                    }

                    XTicket ticket = ticketOptional.get();
                    ticketManager.getStorage().solveTicket(ticket, sender, comment);

                    break;
                    
                }
                
            }
            
        });

        return true;

    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return null;
    }
    
}
