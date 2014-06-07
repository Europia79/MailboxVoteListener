
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VoteListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Solves the problem of players who vote while not logged in. <br/><br/>
 * 
 * <pre>
 * Normally, players who vote while offline will not receive their diamonds. 
 * MailboxVoteListener will send rewards to the player's mailbox. 
 * 
 * Dependencies:
 * http://dl.bukkit.org/downloads/bukkit/
 * http://dev.bukkit.org/bukkit-plugins/votifier/
 * http://dev.bukkit.org/bukkit-plugins/webauctionplus/
 * http://dev.mysql.com/downloads/connector/j/
 * 
 * Tested on Jan 16th, 2014 using https://minestatus.net/votifier/test
 * </pre>
 * 
 * @author Nikolai
 * 
 */
public class MailboxVoteListener implements VoteListener {

    private JavaPlugin wap;
    private JavaPlugin votifier;

    private String query;
    // INSERT INTO `WA_Items` (`playerName`, `itemId`, `qty`) VALUES (?, ?, ?)
    private String prefix;
    private String voter;
    private int rewardId;
    private int rewardqty;
    // "jdbc:mysql://localhost:3306/WebAuctionPlus?user=root&password="
    private String host;
    private String port;
    private String database;
    private String mysqluser;
    private String password;
    private Connection connect = null;
    private PreparedStatement pstat = null;
    private ResultSet resultSet = null;
    

    public MailboxVoteListener() {
        this.wap = (JavaPlugin) Bukkit.getPluginManager().getPlugin("WebAuctionPlus");
        this.votifier = (JavaPlugin) Bukkit.getPluginManager().getPlugin("Votifier");
        this.host = wap.getConfig().getString("MySQL.Host", "localhost");
        this.port = wap.getConfig().getString("MySQL.Port", "3306");
        this.database = wap.getConfig().getString("MySQL.Database", "WebAuctionPlus");
        this.mysqluser = wap.getConfig().getString("MySQL.Username", "root");
        this.password = wap.getConfig().getString("MySQL.Password", "");
        
        this.rewardId = this.votifier.getConfig().getInt("WebAuctionPlus.reward.id", 264);
        this.rewardqty = this.votifier.getConfig().getInt("WebAuctionPlus.reward.quantity", 2);
        
        this.prefix = wap.getConfig().getString("MySQL.TablePrefix", "WA_");
        // "INSERT INTO `WA_Items` (`playerName`, `itemId`, `qty`) VALUES (?, ?, ?)");
        this.query = "INSERT INTO `" + prefix + "Items` (`playerName`, `itemId`, `qty`) VALUES (?, ?, ?)";
    }

    @Override
    public void voteMade(Vote vote) {

        voter = vote.getUsername();

        try {

            Class.forName("com.mysql.jdbc.Driver");

            connect = DriverManager
                    .getConnection("jdbc:mysql://"
                    + host + ":"
                    + port + "/"
                    + database + "?"
                    + "user=" + mysqluser + "&"
                    + "password=" + password);
            
            // "INSERT INTO `WA_Items` (`playerName`, `itemId`, `qty`) VALUES (?, ?, ?)"
            pstat = connect.prepareStatement(this.query);

            pstat.setString(1, voter);
            pstat.setInt(2, rewardId);
            pstat.setInt(3, rewardqty);
            pstat.executeUpdate();
            
        } catch (Exception ex) {

            Logger.getLogger(MailboxVoteListener.class.getName()).log(Level.SEVERE, null, ex);

        } finally {
            close();
        }

    }

    private void close() {
        try {
            if (resultSet != null) {
                resultSet.close();
            }

            if (pstat != null) {
                pstat.close();
            }

            if (connect != null) {
                connect.close();
            }
        } catch (Exception ex) {

            Logger.getLogger(MailboxVoteListener.class.getName()).log(Level.SEVERE, null, ex);

        }
    }
}
