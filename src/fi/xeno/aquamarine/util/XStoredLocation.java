package fi.xeno.aquamarine.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;

public class XStoredLocation {
    
    private String world;
    
    private double x;
    private double y;
    private double z;
    
    private float pitch;
    private float yaw;

    public XStoredLocation(String world, double x, double y, double z, float pitch, float yaw) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
    }

    public XStoredLocation(Location loc) {
        this.world = loc.getWorld().getName();
        this.x = loc.getX();
        this.y = loc.getY();
        this.z = loc.getZ();
        this.pitch = loc.getPitch();
        this.yaw = loc.getYaw();
    }
    
    public Location toLocation() {
        return new Location(Bukkit.getWorld(world), x, y, z);
    }
    
    public Block toBlock() {
        return toLocation().getBlock();
    }
    
    public void teleport(LivingEntity ent) {
        ent.teleport(this.toLocation());
    }
    
    public boolean isInRadius(XStoredLocation otherLocation, double radius) {
        
        double dx = otherLocation.getX() - this.getX();
        double dy = otherLocation.getY() - this.getY();
        double dz = otherLocation.getZ() - this.getZ();
        
        return otherLocation.getWorld().equals(this.getWorld())
                && Math.sqrt(dx*dx + dy*dy + dz*dz) <= radius;
        
    }

    public String getWorld() {
        return world;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getPitch() {
        return pitch;
    }

    public float getYaw() {
        return yaw;
    }

    @Override
    public String toString() {
        return world + "/" + x + "/" + y + "/" + z + "/" + pitch + "/" + yaw;
    }
    
    public String toReadable() {
        return world + "/" + ((int)x) + "/" + ((int)y) + "/" + ((int)z);
    }
    
    public static XStoredLocation fromString(String s) {
        
        String[] pts = s.split("\\/");
        
        if (pts.length < 4) {
            throw new RuntimeException("Invalid location string: '" + s + "'");
        }
        
        String worldName = pts[0];
        
        double x = Double.parseDouble(pts[1]);
        double y = Double.parseDouble(pts[2]);
        double z = Double.parseDouble(pts[3]);
        
        float pitch = Float.parseFloat(pts[4]);
        float yaw = Float.parseFloat(pts[5]);
        
        return new XStoredLocation(worldName, x, y, z, pitch, yaw);
        
    }
    
}
