package fi.xeno.aquamarine.util;

import com.google.gson.JsonObject;
import fi.xeno.aquamarine.XText;
import fi.xeno.aquamarine.XTicketsPlugin;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class XTicket {
    
    private int id;
    
    private long timestamp;
    private UUID sentByUuid;
    private String sentByName;
    
    private XStoredLocation location;
    private String message;
    
    private UUID solvedByUuid;
    private String solvedByName;
    private String solveComment;
    private boolean isSolved;
    private long timeSolved;

    public XTicket(int id, long timestamp, UUID sentByUuid, String sentByName, XStoredLocation location, String message) {
        
        this.id = id;
        
        this.timestamp = timestamp;
        this.sentByUuid = sentByUuid;
        this.sentByName = sentByName;
        this.location = location;
        this.message = message;
        
        this.solvedByUuid = null;
        this.solvedByName = null;
        
        this.isSolved = false;
        this.timeSolved = -1;
        
    }

    public XTicket(int id, long timestamp, UUID sentByUuid, String sentByName, XStoredLocation location, String message, UUID solvedByUuid, String solvedByName, String solveComment, boolean isSolved, long timeSolved) {
        this.id = id;
        this.timestamp = timestamp;
        this.sentByUuid = sentByUuid;
        this.sentByName = sentByName;
        this.location = location;
        this.message = message;
        this.solvedByUuid = solvedByUuid;
        this.solvedByName = solvedByName;
        this.solveComment = solveComment;
        this.isSolved = isSolved;
        this.timeSolved = timeSolved;
    }

    public static XTicket asPlayer(int id, Player player, String message) {
        return new XTicket(id, System.currentTimeMillis(), player.getUniqueId(), player.getName(), new XStoredLocation(player.getLocation()), message);
    }

    public int getId() {
        return id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public UUID getSentByUuid() {
        return sentByUuid;
    }

    public String getSentByName() {
        return sentByName;
    }

    public XStoredLocation getLocation() {
        return location;
    }

    public String getMessage() {
        return message;
    }

    public UUID getSolvedByUuid() {
        return solvedByUuid;
    }

    public String getSolvedByName() {
        return solvedByName;
    }

    public boolean isSolved() {
        return isSolved;
    }

    public long getTimeSolved() {
        return timeSolved;
    }

    public void setSolvedByUuid(UUID solvedByUuid) {
        this.solvedByUuid = solvedByUuid;
    }

    public void setSolvedByName(String solvedByName) {
        this.solvedByName = solvedByName;
    }

    public void setSolveComment(String solveComment) {
        this.solveComment = solveComment;
    }

    public void setSolved(boolean solved) {
        isSolved = solved;
    }

    public void setTimeSolved(long timeSolved) {
        this.timeSolved = timeSolved;
    }

    public BaseComponent[] renderChatPreview(boolean withButtons) {

        ComponentBuilder out = new ComponentBuilder();
        XTicketsPlugin plugin = XTicketsPlugin.getInstance();
        
        if (withButtons) {
            out.append(XText.commandButton("§7[§e»§7] ", "§e" + XTicketsPlugin.getInstance().lang("ticket-preview-teleport"), "/xt goto " + this.getId()));
            out.append(XText.commandButton("§7[§a✔§7] ", "§a" + XTicketsPlugin.getInstance().lang("ticket-preview-solve"), "/xt solve " + this.getId()));
        }
        
        out.append(TextComponent.fromLegacyText("§b#" + this.getId() + " §f" + this.getSentByName() + " §7"));
        
        String previewString = this.message.length() > 40 ? this.message.substring(0, 40).trim() + "..." : this.message;
        
        String solvedString = "";
        if (this.isSolved) {
            
            solvedString = String.format(plugin.lang("ticket-hover-solver"), this.solvedByName) + "\n" +
                            String.format(plugin.lang("ticket-hover-solved-at"), plugin.formatTimestamp(this.timeSolved)) + "\n" +
                            plugin.lang("ticket-hover-comment") + "§f§o" + XText.wordWrap(this.solveComment, 40);
            
        }
        
        String hoverString = plugin.lang("ticket-hover-title").replace("%ticketId%", ""+this.id) + "\n" +
                                String.format(plugin.lang("ticket-hover-sender"), this.sentByName) + "\n" +
                                String.format(plugin.lang("ticket-hover-timestamp"), plugin.formatTimestamp(this.timestamp)) + "\n" +
                                String.format(plugin.lang("ticket-hover-location"), this.location.toReadable() + "\n" +
                                solvedString + "\n\n" +
                                "§f§o" + XText.wordWrap(this.message, 40));
        
        out.append(XText.hoverText("§7" + previewString, hoverString));
        
        return out.create();
        
    }
    
    public JsonObject toJson() {
        
        JsonObject out = new JsonObject();
        
        out.addProperty("id", this.id);
        out.addProperty("timestamp", this.timestamp);
        
        out.addProperty("sentByUuid", this.sentByUuid.toString());
        out.addProperty("sentByName", this.sentByName);
        
        out.addProperty("location", this.location.toString());
        out.addProperty("message", this.message);
        
        out.addProperty("isSolved", this.isSolved);
        
        if (this.isSolved) {
            out.addProperty("solvedByUuid", this.solvedByUuid.toString());
            out.addProperty("solvedByName", this.solvedByName);
            out.addProperty("solveComment", this.solveComment);
            out.addProperty("timeSolved", this.timeSolved);
        }
        
        return out;
        
    }
    
    public static XTicket fromSQLResult(ResultSet rs) throws SQLException {
        
        boolean isSolved = rs.getInt("isSolved") == 1;
        
        return new XTicket(
                rs.getInt("ticketId"),
                rs.getLong("timestamp"),
                UUID.fromString(rs.getString("sentByUuid")),
                rs.getString("sentByName"),
                XStoredLocation.fromString(rs.getString("location")),
                rs.getString("message"),
                isSolved ? UUID.fromString(rs.getString("solvedByUuid")) : null,
                isSolved ? rs.getString("solvedByName") : null,
                isSolved ? rs.getString("solveComment") : null,
                isSolved,
                isSolved ? rs.getLong("timeSolved") : -1L
        );
        
    }

    public static XTicket fromJson(JsonObject o) {
        
        boolean isSolved = o.has("isSolved") && o.get("isSolved").getAsBoolean();
        
        return new XTicket(
                o.get("id").getAsInt(),
                o.get("timestamp").getAsLong(),
                UUID.fromString(o.get("sentByUuid").getAsString()),
                o.get("sentByName").getAsString(),
                XStoredLocation.fromString(o.get("location").getAsString()),
                o.get("message").getAsString(),
                isSolved ? UUID.fromString(o.get("solvedByUuid").getAsString()) : null,
                isSolved ? o.get("solvedByName").getAsString() : null,
                isSolved ? o.get("solveComment").getAsString() : null,
                isSolved,
                isSolved ? o.get("timeSolved").getAsLong() : -1L
        );
        
    }
    
}
