package fi.xeno.aquamarine.storage;

import fi.xeno.aquamarine.XTicketsPlugin;
import fi.xeno.aquamarine.sql.XHikariDatabase;
import fi.xeno.aquamarine.util.XStoredLocation;
import fi.xeno.aquamarine.util.XTicket;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class XSQLTicketDataStorage extends XTicketDataStorage {
    
    private final XTicketsPlugin plugin;
    private final XHikariDatabase db;
    
    private String tableName;

    public XSQLTicketDataStorage(XTicketsPlugin plugin, XHikariDatabase db, String tableName) {
        
        this.plugin = plugin;
        this.db = db;
        
        if (tableName.matches("[^A-Za-z0-9\\_\\-]")) {
            throw new RuntimeException("Table name '" + tableName + "' contains illegal characters.");
        }
        
        Connection c = null;
        PreparedStatement st = null;

        try {
            
            c = db.c();
            
            st = c.prepareStatement("CREATE TABLE IF NOT EXISTS `" + tableName + "` (" +
                    "  `id` int(11) NOT NULL AUTO_INCREMENT," +
                    "  `ticketId` int(11) DEFAULT NULL," +
                    "  `timestamp` bigint(22) DEFAULT NULL," +
                    "  `sentByUuid` varchar(64) DEFAULT NULL," +
                    "  `sentByName` varchar(64) DEFAULT NULL," +
                    "  `location` varchar(128) DEFAULT NULL," +
                    "  `message` text DEFAULT NULL," +
                    "  `isSolved` tinyint(1) DEFAULT NULL," +
                    "  `solvedByUuid` varchar(64) DEFAULT NULL," +
                    "  `solvedByName` varchar(64) DEFAULT NULL," +
                    "  `solveComment` text DEFAULT NULL," +
                    "  `timeSolved` bigint(22) DEFAULT NULL," +
                    "  PRIMARY KEY (`id`)" +
                    ")"
            );
            
            st.executeUpdate();
            
        } catch (SQLException throwables) {
            plugin.getLogger().severe("Unable to create database tables:");
            throwables.printStackTrace();
        } finally {
            try {
                db.close(null, st, c);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }

    }

    @Override
    public XTicket createTicket(Player player, String message) {
        
        XTicket ticket = XTicket.asPlayer(getNextTicketId(), player, message);
        
        Connection c = null;
        PreparedStatement st = null;

        try {
            
            c = db.c();
            st = c.prepareStatement("INSERT INTO " + tableName + " " +
                    "(id, ticketId, timestamp, sentByUuid, sentByName, location, message, isSolved, solvedByUuid, solvedByName, solveComment, timeSolved) VALUES " +
                    "(NULL, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            
            st.setInt(1, ticket.getId());
            st.setLong(2, ticket.getTimestamp());
            
            st.setString(3, ticket.getSentByUuid().toString());
            st.setString(4, ticket.getSentByName());
            
            st.setString(5, ticket.getLocation().toString());
            st.setString(6, ticket.getMessage());
            
            st.setInt(7, ticket.isSolved() ? 1 : 0);
            st.setString(8, "");
            st.setString(9, "");
            st.setString(10, "");
            st.setLong(11, 0L);

            int count = st.executeUpdate();
            if (count <= 0) ticket = null;

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            ticket = null;
        } finally {
            try {
                db.close(null, st, c);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
                ticket = null;
            }
        }

        return ticket;
        
    }

    @Override
    public void solveTicket(XTicket ticket, CommandSender solver, String comment) {
        
        // these SHOULD be unneeded, but just in case...
        ticket.setSolved(true);
        ticket.setSolvedByUuid(solver instanceof Player ? ((Player)solver).getUniqueId() : new UUID(0,0));
        ticket.setSolvedByName(solver.getName());
        ticket.setSolveComment(comment);
        ticket.setTimeSolved(System.currentTimeMillis());
        
        Connection c = null;
        PreparedStatement st = null;

        try {
            
            c = db.c();
            st = c.prepareStatement("UPDATE " + tableName + " " +
                                    "SET isSolved = 1, solvedByUuid = ?, solvedByName = ?, solveComment = ?, timeSolved = ? " +
                                    "WHERE ticketId = ?");
            
            st.setString(1, (solver instanceof Player ? ((Player)solver).getUniqueId() : new UUID(0,0)).toString());
            st.setString(2, solver.getName());
            
            st.setString(3, comment);
            st.setLong(4, System.currentTimeMillis());
            
            st.setInt(5, ticket.getId());
            
            st.executeUpdate();
            announceSolveTicket(ticket, solver, comment);
            
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            try {
                db.close(null, st, c);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }

    }

    @Override
    public Optional<XTicket> getTicketByNumber(int id) {
        return Optional.empty();
    }

    @Override
    public List<XTicket> getTickets() {
        return queryTickets("SELECT * FROM " + tableName + " ORDER BY id DESC", o());
    }

    @Override
    public List<XTicket> getWaitingTickets() {
        return queryTickets("SELECT * FROM " + tableName + " WHERE isSolved = 0 ORDER BY id DESC", o());
    }

    @Override
    public List<XTicket> getSolvedTickets() {
        return queryTickets("SELECT * FROM " + tableName + " WHERE isSolved = 1 ORDER BY id DESC", o());
    }

    @Override
    public List<XTicket> getWaitingNearbyTickets(XStoredLocation location, double radius) {
        return getWaitingTickets()
                .stream()
                .filter(t -> t.getLocation().isInRadius(location, radius))
                .collect(Collectors.toList());
    }

    @Override
    public List<XTicket> getWaitingTicketsBySender(UUID uuid) {
        return queryTickets("SELECT * FROM " + tableName + " WHERE isSolved = 0 AND sentByUuid = ? ORDER BY id DESC", o(uuid.toString()));
    }

    @Override
    public List<XTicket> getTicketsBySender(UUID uuid) {
        return queryTickets("SELECT * FROM " + tableName + " WHERE sentByUuid = ?", o(uuid.toString()));
    }

    @Override
    public List<XTicket> getTicketsBySolver(UUID uuid) {
        return queryTickets("SELECT * FROM " + tableName + " WHERE isSolved = 1 AND solvedByUuid = ?", o(uuid.toString()));
    }
    
    private List<XTicket> queryTickets(String query, Object[] params) {

        Connection c = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        
        List<XTicket> out = new ArrayList<>();

        try {

            c = db.c();
            st = c.prepareStatement(query);
            
            int n = 1;
            for (Object _o:params) {
                if (_o instanceof String) {
                    st.setString(n, (String)_o);
                } else if (_o instanceof Integer) {
                    st.setInt(n, (Integer)_o);
                } else if (_o instanceof Long) {
                    st.setLong(n, (Long)_o);
                }
                n++;
            }
            
            rs = st.executeQuery();
            
            while (rs.next()) {
                out.add(XTicket.fromSQLResult(rs));
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            try {
                db.close(rs, st, c);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        
        return out;
        
    }

    @Override
    public int getNextTicketId() {
        
        Connection c = null;
        PreparedStatement st = null;
        ResultSet rs = null;

        int lastTicketId = 0;
        
        try {
            
            c = db.c();
            st = c.prepareStatement("SELECT * FROM " + tableName + " ORDER BY id DESC LIMIT 1");
            rs = st.executeQuery();
            
            if (rs.next())
                lastTicketId = rs.getInt("ticketId");
            
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            try {
                db.close(rs, st, c);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        
        return lastTicketId + 1;

    }

    @Override
    public void close() {
        plugin.getLogger().info("Closing database connection...");
        this.db.close();
    }

    private Object[] o(Object... objects){
        return objects;
    }
    
}
