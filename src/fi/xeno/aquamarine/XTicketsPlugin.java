package fi.xeno.aquamarine;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class XTicketsPlugin extends JavaPlugin {
    
    private static XTicketsPlugin instance;
    public static XTicketsPlugin getInstance(){
        return instance;
    }
    
    public void onEnable() {
        instance = this;
    }
    
    public void onDisable() {}
    
}
