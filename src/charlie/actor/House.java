/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charlie.actor;

import charlie.card.Hid;
import charlie.controller.Dealer;
import charlie.controller.IPlayer;
import charlie.message.view.from.Arrival;
import charlie.server.GameServer;
import charlie.server.Ticket;
import com.googlecode.actorom.Address;
import com.googlecode.actorom.Topology;
import com.googlecode.actorom.annotation.OnMessage;
import com.googlecode.actorom.annotation.TopologyInstance;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author roncoleman125
 */
public class House implements Serializable {
    private final Logger LOG = LoggerFactory.getLogger(House.class);
    
    @TopologyInstance private Topology topology;
    
    private final String PLAYER_ACTOR = "PLAYER-";
    
    public final static Double[] MIN_BETS = {5.0, 25.0, 100.0};

    protected List<NetPlayer> players = new ArrayList<>();
    
    private Integer nextPlayerId = 0;
    private final Properties props;
    private Address myAddress;
    private final GameServer server;
    protected HashMap<IPlayer,Ticket> accounts = new HashMap<>();

    
    public House(GameServer server,Properties props) {    
        this.server = server;
        this.props = props;
    }
    
    @OnMessage(type = Arrival.class)
    public void onReceive(Arrival arrival) {
        Address channelAddress = arrival.getChannelAddress();
        LOG.info("arrival from "+channelAddress);
        
        Ticket ticket = arrival.getTicket();
        
        if(!valid(ticket)) {
            LOG.error("invalid ticket = "+ticket);
            return;
        }

        LOG.info("validate ticket = "+ticket);
        
        // Get a dealer for this player
        // Note: if we were allocating dealers from a pool, this is the place
        // to implement that logic. For now we'll just spawn dealers without
        // restriction.
        Dealer dealer = new Dealer(this);
        
        // Spawn a player actor in server
        NetPlayer player = new NetPlayer(dealer, channelAddress);
        accounts.put(player,ticket);
        
        synchronized(this) {
            nextPlayerId++;
            
            players.add(player);
        }
        
        String id = PLAYER_ACTOR + nextPlayerId;
        Address playerAddress = topology.spawnActor(id, player); 
        
        player.setMyAddress(playerAddress);
        
        // Inform channel we're ready
        player.ready();        
    }

    public Properties getProps() {
        return props;
    }
    
    protected boolean valid(Ticket ticket) {
        List<Ticket> logins = server.getLogins();
        
        for(int i=0; i < logins.size(); i++) {
            if(logins.get(i).equals(ticket))
                return true;
        }
        
        return false;
    }
    /**
     * Updates the bankroll
     * @param hid Hand
     * @param gain P&L
     */
    public void updateBankroll(IPlayer player,Double amt,Double gain) {      
        if(player == null || !accounts.containsKey(player))
            return;
        
        Ticket ticket = accounts.get(player);
        
        Double bankroll = ticket.getBankroll() + gain * amt;
        
        ticket.setBankroll(bankroll);
    }
    
    public Double getBankroll(IPlayer player) {
        if(player == null || !accounts.containsKey(player))
            return 0.0;
        
        return accounts.get(player).getBankroll();
    }
}
