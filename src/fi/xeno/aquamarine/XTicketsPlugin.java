package fi.xeno.aquamarine;

import fi.xeno.aquamarine.command.CommandTicket;
import fi.xeno.aquamarine.command.CommandTickets;
import fi.xeno.aquamarine.command.CommandXt;
import fi.xeno.aquamarine.command.CommandXti;
import fi.xeno.aquamarine.sql.XHikariDatabase;
import fi.xeno.aquamarine.storage.XFlatFileTicketDataStorage;
import fi.xeno.aquamarine.storage.XMemoryTicketDataStorage;
import fi.xeno.aquamarine.storage.XSQLTicketDataStorage;
import fi.xeno.aquamarine.storage.XTicketDataStorage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class XTicketsPlugin extends JavaPlugin {
    
    private static XTicketsPlugin instance;
    public static XTicketsPlugin getInstance(){
        return instance;
    }
    
    private static String INFO_PREFIX = "§e";
    
    private Logger logger;
    private XTicketManager ticketManager;
    
    private File fileLang;
    private YamlConfiguration lang;
    
    private SimpleDateFormat dateFormat;
    
    public void onEnable() {
        
        instance = this;
        logger = this.getLogger();
        
        if (!this.getDataFolder().exists()) {
            this.getDataFolder().mkdir();
        }
        
        this.saveDefaultConfig();
        
        fileLang = new File(this.getDataFolder(), "lang.yml");
        if (!fileLang.exists()) this.saveResource("lang.yml", false);
        lang = YamlConfiguration.loadConfiguration(fileLang);
        
        INFO_PREFIX = ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("message-prefix", INFO_PREFIX));
        dateFormat = new SimpleDateFormat(this.getConfig().getString("date-format", "dd.MM.yyyy kk:mm:ss"));

        XTicketDataStorage dataStorage;
        
        switch (this.getConfig().getString("storage-method", "file").toLowerCase()) {
            
            case "mysql":
                
                XHikariDatabase db = new XHikariDatabase(this,
                        this.getConfig().getString("mysql-host"),
                        this.getConfig().getString("mysql-port"),
                        this.getConfig().getString("mysql-db"),
                        this.getConfig().getString("mysql-user"),
                        this.getConfig().getString("mysql-pass"));
                
                dataStorage = new XSQLTicketDataStorage(this, db, this.getConfig().getString("mysql-table", "aquamarine_tickets"));
                
                break;
                
            case "file":
                dataStorage = new XFlatFileTicketDataStorage(this, new File(this.getDataFolder(), "tickets.json"));
                break;
                
            case "memory":
                dataStorage = new XMemoryTicketDataStorage(this);
                break;
                
            default:
                throw new RuntimeException("Storage method '" + this.getConfig().getString("storage-method") + "' is not supported");
                
        }
        
        ticketManager = new XTicketManager(this, dataStorage);
        Bukkit.getPluginManager().registerEvents(ticketManager, this);
        
        registerCommand("ticket", new CommandTicket(this, ticketManager));
        registerCommand("tickets", new CommandTickets(this, ticketManager));
        registerCommand("xt", new CommandXt(this, ticketManager));
        
        CommandXti ticketGuiCommand = new CommandXti(this, ticketManager);
        Bukkit.getPluginManager().registerEvents(ticketGuiCommand, this);
        registerCommand("xti", ticketGuiCommand);
        
        logger.info("Aquamarine has been enabled!");
        
    }
    
    public void onDisable() {
        ticketManager.getStorage().close();
        logger.info("Aquamarine has been disabled.");
    }
    
    
    
    private void registerCommand(String label, TabExecutor command) {
        this.getCommand(label).setExecutor(command);
        this.getCommand(label).setTabCompleter(command);
    }
    
    public void sendPrefixed(String message, CommandSender... recipients) {
        sendPrefixed(message, Arrays.asList(recipients));
    }
    
    public void sendPrefixed(String message, Iterable<CommandSender> recipients) {
        String out = INFO_PREFIX + message;
        recipients.forEach(p -> p.sendMessage(out));
    }
    
    public String lang(String key){
        return ChatColor.translateAlternateColorCodes('&', lang.getString(key, "lang["+key+"]"));
    }
    
    public String formatTimestamp(long timestamp) {
        return dateFormat.format(Date.from(Instant.ofEpochMilli(timestamp)));
    }
    
    private static Map<String, NamespacedKey> keyCache = new ConcurrentHashMap<>();
    public static NamespacedKey key(String n) {
        if (!keyCache.containsKey(n)) keyCache.put(n, new NamespacedKey(getInstance(), n));
        return keyCache.get(n);
    }

    public XTicketManager getTicketManager() {
        return ticketManager;
    }
    
}
