package fi.xeno.aquamarine.command;

import fi.xeno.aquamarine.AquamarinePermission;
import fi.xeno.aquamarine.XText;
import fi.xeno.aquamarine.XTicketManager;
import fi.xeno.aquamarine.XTicketsPlugin;
import fi.xeno.aquamarine.util.TimestampedValue;
import fi.xeno.aquamarine.util.XTicket;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CommandXti implements TabExecutor, Listener {
    
    private final XTicketsPlugin plugin;
    private final XTicketManager ticketManager;
    
    private final XTicketGui ticketGui;

    public CommandXti(XTicketsPlugin plugin, XTicketManager ticketManager) {
        
        this.plugin = plugin;
        this.ticketManager = ticketManager;
        
        this.ticketGui = new XTicketGui();
        
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        
        if (!sender.hasPermission(AquamarinePermission.STAFF)) {
            plugin.sendPrefixed(plugin.lang("generic-no-permission"), sender);
            return true;
        }
        
        if (!(sender instanceof Player)) {
            plugin.sendPrefixed(plugin.lang("generic-not-player"), sender);
            return true;
        }
        
        Player player = (Player)sender;
        
        ticketManager.getStorage().getWaitingTicketsAsync(tickets -> {
            
            if (tickets.size() == 0) {
                plugin.sendPrefixed(plugin.lang("generic-no-tickets"), player);
                return;
            }
            
            sync(() -> {
                
                Inventory inv = ticketGui.getInventory();
                ticketGui.refresh(tickets);
                
                player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1f, 1f);
                player.openInventory(inv);
                
            });
            
        });

        return true;

    }

    @EventHandler
    private void onInventoryClick(InventoryClickEvent e) {

        if (e.getInventory().getHolder() == null || !(e.getInventory().getHolder() instanceof XTicketGui)) {
            return;
        }

        if (e.getCurrentItem() == null) {
            return;
        }
        
        Player player = (Player)e.getWhoClicked();
        ItemStack item = e.getCurrentItem();
        
        PersistentDataContainer pd = item.getItemMeta().getPersistentDataContainer();
        String ticketId = pd.getOrDefault(XTicketsPlugin.key("xTicketId"), PersistentDataType.STRING, "?");
        
        if (e.getClick().equals(ClickType.LEFT)) {
            player.performCommand("xt goto " + ticketId);
        } else if (e.getClick().equals(ClickType.RIGHT)) {
            player.performCommand("xt solve " + ticketId + " -");
        }
        
        player.closeInventory();
        e.setCancelled(true);

    }

    @EventHandler
    private void onInventoryClick(InventoryDragEvent e) {
        if (e.getInventory().getHolder() != null && e.getInventory().getHolder() instanceof XTicketGui) {
            e.setCancelled(true);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return null;
    }
    
    private void sync(Runnable r) {
        Bukkit.getScheduler().runTask(plugin, r);
    }
    
    
    
    private static class XTicketGui implements InventoryHolder {

        private final Inventory inv;
        
        public XTicketGui() {
            this.inv = Bukkit.createInventory(this, 9*6, "* ✎ *");
        }
        
        public synchronized void refresh(List<XTicket> tickets) {
            inv.clear();
            tickets.forEach(t -> inv.addItem(t.renderMenuItem()));
        }

        @Override
        public Inventory getInventory() {
            return inv;
        }
        
    }
    
}
