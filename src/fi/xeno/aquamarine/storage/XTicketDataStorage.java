package fi.xeno.aquamarine.storage;

import fi.xeno.aquamarine.AquamarinePermission;
import fi.xeno.aquamarine.XTicketsPlugin;
import fi.xeno.aquamarine.util.XStoredLocation;
import fi.xeno.aquamarine.util.XTicket;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public abstract class XTicketDataStorage {

    public abstract XTicket createTicket(Player player, String message);
    public abstract void solveTicket(XTicket ticket, CommandSender solver, String comment);
    
    public abstract Optional<XTicket> getTicketByNumber(int id);
    
    public abstract List<XTicket> getTickets();
    public abstract List<XTicket> getWaitingTickets();
    public abstract List<XTicket> getSolvedTickets();
    public abstract List<XTicket> getWaitingNearbyTickets(XStoredLocation location, double radius);
    public abstract List<XTicket> getWaitingTicketsBySender(UUID uuid);
    
    public abstract List<XTicket> getTicketsBySender(UUID uuid);
    public abstract List<XTicket> getTicketsBySolver(UUID uuid);
    
    public abstract int getNextTicketId();
    
    public abstract void close();
    
    public void createTicketAsync(Player player, String message, Consumer<XTicket> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(XTicketsPlugin.getInstance(), () -> callback.accept(createTicket(player, message)));
    }
    
    public void solveTicketAsync(XTicket ticket, Player solver, String comment, Runnable callback) {
        Bukkit.getScheduler().runTaskAsynchronously(XTicketsPlugin.getInstance(), () -> {
            solveTicket(ticket, solver, comment);
            callback.run();
        });
    }
    
    public Optional<XTicket> solveTicket(int ticketId, CommandSender solver, String comment) {
        
        XTicket ticket = getTicketByNumber(ticketId).orElse(null);
        
        if (ticket == null)
            return Optional.empty();
        
        solveTicket(ticket, solver, comment);
        
        return Optional.of(ticket);
        
    }
    
    public void announceSolveTicket(XTicket ticket, CommandSender sender, String comment) {
        
        String announceText = XTicketsPlugin.getInstance().lang("command-ticket-solved")
                .replaceAll("\\$solver\\$", sender.getName())
                .replaceAll("\\$n\\$", ""+ticket.getId())
                .replaceAll("\\$comment\\$", comment);
        
        Bukkit.getOnlinePlayers()
                .stream()
                .filter(p -> p.hasPermission(AquamarinePermission.STAFF)
                                || p.getUniqueId().equals(ticket.getSentByUuid()))
                .forEach(p -> XTicketsPlugin.getInstance().sendPrefixed(announceText, p));
        
    }
    
    public void solveTicketAsync(int ticketId, Player solver, String comment, Consumer<Optional<XTicket>> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(XTicketsPlugin.getInstance(), () -> callback.accept(solveTicket(ticketId, solver, comment)));
    }
    
    public void getTicketByNumberAsync(int id, Consumer<Optional<XTicket>> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(XTicketsPlugin.getInstance(), () -> callback.accept(getTicketByNumber(id)));
    }
    
    public void getTicketsAsync(Consumer<List<XTicket>> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(XTicketsPlugin.getInstance(), () -> callback.accept(getTickets()));
    }
    
    public void getWaitingTicketsAsync(Consumer<List<XTicket>> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(XTicketsPlugin.getInstance(), () -> callback.accept(getWaitingTickets()));
    }
    
    public void getSolvedTicketsAsync(Consumer<List<XTicket>> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(XTicketsPlugin.getInstance(), () -> callback.accept(getSolvedTickets()));
    }

    public void getWaitingNearbyTicketsAsync(XStoredLocation location, double radius, Consumer<List<XTicket>> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(XTicketsPlugin.getInstance(), () -> callback.accept(getWaitingNearbyTickets(location, radius)));
    }

    public void getWaitingTicketsBySenderAsync(UUID uuid, Consumer<List<XTicket>> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(XTicketsPlugin.getInstance(), () -> callback.accept(getWaitingTicketsBySender(uuid)));
    }

    public void getTicketsBySenderAsync(UUID uuid, Consumer<List<XTicket>> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(XTicketsPlugin.getInstance(), () -> callback.accept(getTicketsBySender(uuid)));
    }

    public void getTicketsBySolverAsync(UUID uuid, Consumer<List<XTicket>> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(XTicketsPlugin.getInstance(), () -> callback.accept(getTicketsBySolver(uuid)));
    }

    public void getNextTicketIdAsync(Consumer<Integer> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(XTicketsPlugin.getInstance(), () -> callback.accept(getNextTicketId()));
    }
    
}
