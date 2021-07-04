package fi.xeno.aquamarine.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class XHikariDatabase {

    private HikariConfig config;
    private HikariDataSource dataSource;

    private Plugin plugin;

    public XHikariDatabase(Plugin plugin, String host, String port, String db, String user, String pass) {

        this.plugin = plugin;

        config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + db);
        config.setUsername(user);
        config.setPassword(pass);

        config.setDriverClassName("com.mysql.jdbc.Driver");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        dataSource = new HikariDataSource(config);

    }


    /**
     * Get the raw data source
     * @return
     */
    public HikariDataSource getDataSource() {
        return dataSource;
    }

    /**
     * Get a connection
     * @return
     * @throws SQLException
     */
    public Connection c() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Check if connection is closed
     * @return
     */
    public boolean isClosed() {
        return dataSource.isClosed();
    }


    /**
     * Close the connection
     */
    public void close() {
        if (!dataSource.isClosed()) {
            dataSource.close();
        }
    }


    /**
     * Close used things automatically
     * @param rs
     * @param st
     * @param c
     * @throws SQLException
     */
    public void close(ResultSet rs, PreparedStatement st, Connection c) throws SQLException {

        if (rs != null && !rs.isClosed()) {
            rs.close();
        }

        if (st != null && !st.isClosed()) {
            st.close();
        }

        if (c != null && !c.isClosed()) {
            c.close();
        }

    }

}
