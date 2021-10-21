package fi.xeno.aquamarine.storage;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import fi.xeno.aquamarine.XTicketsPlugin;
import fi.xeno.aquamarine.util.XStoredLocation;
import fi.xeno.aquamarine.util.XTicket;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class XMemoryTicketDataStorage extends XTicketDataStorage {

    private final XTicketsPlugin plugin;
    private final Map<Integer, XTicket> tickets = new ConcurrentHashMap<>();
    
    public XMemoryTicketDataStorage(XTicketsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public XTicket createTicket(Player player, String message) {
        
        XTicket ticket = XTicket.asPlayer(getNextTicketId(), player, message);
        tickets.put(ticket.getId(), ticket);
        
        return ticket;
        
    }

    @Override
    public void solveTicket(XTicket ticket, CommandSender solver, String comment) {
        
        ticket.setSolved(true);

        ticket.setSolvedByUuid(solver instanceof Player ? ((Player)solver).getUniqueId() : new UUID(0,0));
        ticket.setSolvedByName(solver.getName());
        
        ticket.setSolveComment(comment);
        ticket.setTimeSolved(System.currentTimeMillis());
        
        // this SHOULD be unnecessary, but just to be sure...
        tickets.put(ticket.getId(), ticket);
        
        announceSolveTicket(ticket, solver, comment);
        
    }

    @Override
    public Optional<XTicket> getTicketByNumber(int id) {
        return Optional.ofNullable(tickets.getOrDefault(id, null));
    }

    @Override
    public List<XTicket> getTickets() {
        return tickets.values()
                .stream()
                .sorted(Comparator.comparingInt(XTicket::getId))
                .collect(Collectors.toList());
    }

    @Override
    public List<XTicket> getWaitingTickets() {
        return tickets.values()
                .stream()
                .filter(t -> !t.isSolved())
                .sorted(Comparator.comparingInt(XTicket::getId))
                .collect(Collectors.toList());
    }

    @Override
    public List<XTicket> getSolvedTickets() {
        return tickets.values()
                .stream()
                .filter(XTicket::isSolved)
                .sorted(Comparator.comparingInt(XTicket::getId))
                .collect(Collectors.toList());
    }

    @Override
    public List<XTicket> getWaitingNearbyTickets(XStoredLocation location, double radius) {
        return tickets.values()
                .stream()
                .filter(t -> !t.isSolved() && t.getLocation().isInRadius(location, radius))
                .sorted(Comparator.comparingInt(XTicket::getId))
                .collect(Collectors.toList());
    }

    @Override
    public List<XTicket> getWaitingTicketsBySender(UUID uuid) {
        return tickets.values()
                .stream()
                .filter(t -> !t.isSolved() && t.getSentByUuid().equals(uuid))
                .sorted(Comparator.comparingInt(XTicket::getId))
                .collect(Collectors.toList());
    }

    @Override
    public List<XTicket> getTicketsBySender(UUID uuid) {
        return tickets.values()
                .stream()
                .filter(t -> t.getSentByUuid().equals(uuid))
                .sorted(Comparator.comparingInt(XTicket::getId))
                .collect(Collectors.toList());
    }

    @Override
    public List<XTicket> getTicketsBySolver(UUID uuid) {
        return tickets.values()
                .stream()
                .filter(t -> t.isSolved() && t.getSolvedByUuid().equals(uuid))
                .sorted(Comparator.comparingInt(XTicket::getId))
                .collect(Collectors.toList());
    }

    @Override
    public int getNextTicketId() {
        return tickets.keySet().stream().max(Comparator.comparingInt(Integer::intValue)).orElse(0) + 1;
    }

    @Override
    public void close() {}

}
