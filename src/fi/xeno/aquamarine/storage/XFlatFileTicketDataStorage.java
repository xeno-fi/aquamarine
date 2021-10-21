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
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class XFlatFileTicketDataStorage extends XTicketDataStorage {

    private final XTicketsPlugin plugin;
    private final File file;
    private final Gson gson = new GsonBuilder()
                                .setPrettyPrinting()
                                .create();
                    
    private final Map<Integer, XTicket> tickets = new ConcurrentHashMap<>();
    
    public XFlatFileTicketDataStorage(XTicketsPlugin plugin, File file) {
        
        this.plugin = plugin;
        this.file = file;

        try {
            load();
        } catch (IOException e) {
            plugin.getLogger().severe("Unable to load data from flat file storage:");
            e.printStackTrace();
        }

    }
    
    private synchronized void load() throws IOException {
        
        tickets.clear();
        
        if (!file.exists()) {
            return;
        }
        
        JsonReader reader = new JsonReader(new FileReader(file));
        JsonParser parser = new JsonParser();
        JsonElement root = parser.parse(reader);
        
        if (!root.isJsonArray()) {
            throw new RuntimeException("Unable to parse ticket data: JSON is not formatted as an array.");
        }
        
        JsonArray rawArr = root.getAsJsonArray();
        
        for (JsonElement el:rawArr) {
            
            if (!el.isJsonObject()) {
                throw new RuntimeException("Unable to parse ticket data: ticket is not formatted as a JSON object");
            }
            
            XTicket ticket = XTicket.fromJson(el.getAsJsonObject());
            tickets.put(ticket.getId(), ticket);
            
        }
        
        reader.close();
        
    }
    
    private synchronized void save() {
        
        JsonArray out = new JsonArray();
        tickets.values()
                .stream()
                .sorted(Comparator.comparingInt(XTicket::getId))
                .forEach(t -> out.add(t.toJson()));

        try {
            Writer writer = new FileWriter(file);
            gson.toJson(out, writer);
            writer.close();
        } catch (IOException e) {
            plugin.getLogger().severe("Unable to save ticket JSON data:");
            e.printStackTrace();
        }
        
    }
    
    private void saveAsync() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, this::save);
    }


    @Override
    public synchronized XTicket createTicket(Player player, String message) {
        
        XTicket ticket = XTicket.asPlayer(getNextTicketId(), player, message);
        tickets.put(ticket.getId(), ticket);
        
        saveAsync();
        
        return ticket;
        
    }

    @Override
    public synchronized void solveTicket(XTicket ticket, CommandSender solver, String comment) {
        
        ticket.setSolved(true);
        
        ticket.setSolvedByUuid(solver instanceof Player ? ((Player)solver).getUniqueId() : new UUID(0,0));
        ticket.setSolvedByName(solver.getName());
        
        ticket.setSolveComment(comment);
        ticket.setTimeSolved(System.currentTimeMillis());
        
        // this SHOULD be unnecessary, but just to be sure...
        tickets.put(ticket.getId(), ticket);
        
        announceSolveTicket(ticket, solver, comment);
        saveAsync();
        
    }

    @Override
    public synchronized Optional<XTicket> getTicketByNumber(int id) {
        return Optional.ofNullable(tickets.getOrDefault(id, null));
    }

    @Override
    public synchronized List<XTicket> getTickets() {
        return tickets.values()
                .stream()
                .sorted(Comparator.comparingInt(XTicket::getId))
                .collect(Collectors.toList());
    }

    @Override
    public synchronized List<XTicket> getWaitingTickets() {
        return tickets.values()
                .stream()
                .filter(t -> !t.isSolved())
                .sorted(Comparator.comparingInt(XTicket::getId))
                .collect(Collectors.toList());
    }

    @Override
    public synchronized List<XTicket> getSolvedTickets() {
        return tickets.values()
                .stream()
                .filter(XTicket::isSolved)
                .sorted(Comparator.comparingInt(XTicket::getId))
                .collect(Collectors.toList());
    }

    @Override
    public synchronized List<XTicket> getWaitingNearbyTickets(XStoredLocation location, double radius) {
        return tickets.values()
                .stream()
                .filter(t -> !t.isSolved() && t.getLocation().isInRadius(location, radius))
                .sorted(Comparator.comparingInt(XTicket::getId))
                .collect(Collectors.toList());
    }

    @Override
    public synchronized List<XTicket> getWaitingTicketsBySender(UUID uuid) {
        return tickets.values()
                .stream()
                .filter(t -> !t.isSolved() && t.getSentByUuid().equals(uuid))
                .sorted(Comparator.comparingInt(XTicket::getId))
                .collect(Collectors.toList());
    }

    @Override
    public synchronized List<XTicket> getTicketsBySender(UUID uuid) {
        return tickets.values()
                .stream()
                .filter(t -> t.getSentByUuid().equals(uuid))
                .sorted(Comparator.comparingInt(XTicket::getId))
                .collect(Collectors.toList());
    }

    @Override
    public synchronized List<XTicket> getTicketsBySolver(UUID uuid) {
        return tickets.values()
                .stream()
                .filter(t -> t.isSolved() && t.getSolvedByUuid().equals(uuid))
                .sorted(Comparator.comparingInt(XTicket::getId))
                .collect(Collectors.toList());
    }

    @Override
    public synchronized int getNextTicketId() {
        return tickets.keySet().stream().max(Comparator.comparingInt(Integer::intValue)).orElse(0) + 1;
    }

    @Override
    public synchronized void close() {
        plugin.getLogger().info("Saving ticket storage...");
        save();
    }

}
