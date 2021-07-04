package fi.xeno.aquamarine;

import fi.xeno.aquamarine.storage.XTicketDataStorage;
import fi.xeno.aquamarine.util.XStoredLocation;
import fi.xeno.aquamarine.util.XTicket;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;

public class XTicketManager implements Listener {
    
    private final XTicketsPlugin plugin;
    private final XTicketDataStorage storage;

    public XTicketManager(XTicketsPlugin plugin, XTicketDataStorage storage) {
        this.plugin = plugin;
        this.storage = storage;
    }
    
    public XTicketDataStorage getStorage() {
        return storage;
    }
    
    public TicketCreationStatus canCreateTicket(Player player) {
        
        if (plugin.getConfig().getBoolean("enable-max-per-radius", false)) {
            
            int nearbyTickets = storage.getWaitingNearbyTickets(new XStoredLocation(player.getLocation()),
                                                                plugin.getConfig().getDouble("check-radius", 3d)).size();
            
            if (nearbyTickets >= plugin.getConfig().getInt("max-per-radius", 3)) {
                return TicketCreationStatus.DENY_NEARBY;
            }
            
        }
        
        if (plugin.getConfig().getBoolean("enable-max-per-player", false)) {
            
            int playerTickets = storage.getWaitingTicketsBySender(player.getUniqueId()).size();
            
            if (playerTickets >= plugin.getConfig().getInt("max-per-player", 3)) {
                return TicketCreationStatus.DENY_PLAYER;
            }
            
        }
        
        return TicketCreationStatus.ALLOW;
        
    }
    
    public XTicket createTicket(Player player, String message) {
        
        XTicket ticket = storage.createTicket(player, message);
        
        if (ticket == null)
            throw new RuntimeException("Unable to create new ticket. Check your storage method.");

        String staffAnnounce = plugin.lang("ticket-created-announcement")
                .replace("player", player.getName())
                .replace("ticketId", ""+ticket.getId());
        
        Bukkit.getOnlinePlayers()
                .stream()
                .filter(p -> p.hasPermission(AquamarinePermission.STAFF))
                .forEach(p -> plugin.sendPrefixed(staffAnnounce, p));
        
        return ticket;
        
    }
    
    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent e) {
        
        if (!plugin.getConfig().getBoolean("enable-join-announce", false))
            return;
        
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            
            Player player = e.getPlayer();
            
            if (player.hasPermission(AquamarinePermission.STAFF)) {
                storage.getWaitingTicketsAsync((List<XTicket> tickets) -> {
                    if (tickets.size() > 0) {
                        plugin.sendPrefixed(plugin.lang("ticket-join-announcement").replace("%ticketCount%", ""+tickets.size()));
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.5f);
                    }
                });
            }
            
        }, 20L*plugin.getConfig().getInt("join-announce-delay-seconds", 5));
        
    }
    
    
    public static enum TicketCreationStatus {
        
        ALLOW(true),
        DENY_NEARBY(false),
        DENY_PLAYER(false);

        private boolean wasAllowed;
        
        TicketCreationStatus(boolean b) {
            this.wasAllowed = b;
        }

        public boolean wasAllowed() {
            return wasAllowed;
        }
        
    }
    
}
