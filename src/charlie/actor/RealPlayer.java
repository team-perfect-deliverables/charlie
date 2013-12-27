/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charlie.actor;

import charlie.card.Card;
import charlie.message.view.from.Bet;
import charlie.message.view.from.Hit;
import charlie.card.Hid;
import charlie.controller.Dealer;
import charlie.card.Hand;
import charlie.controller.IPlayer;
import charlie.message.view.from.DoubleDown;
import charlie.message.view.from.Request;
import charlie.message.view.from.Stay;
import charlie.message.view.to.Blackjack;
import charlie.message.view.to.Bust;
import charlie.message.view.to.Charlie;
import charlie.message.view.to.Deal;
import charlie.message.view.to.Ending;
import charlie.message.view.to.Loose;
import charlie.message.view.to.Ready;
import charlie.message.view.to.Play;
import charlie.message.view.to.Push;
import charlie.message.view.to.Starting;
import charlie.message.view.to.Win;
import com.googlecode.actorom.Actor;
import com.googlecode.actorom.Address;
import com.googlecode.actorom.annotation.OnMessage;
import com.googlecode.actorom.remote.ClientTopology;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author roncoleman125
 */
public class RealPlayer implements IPlayer {
    private final Logger LOG = LoggerFactory.getLogger(RealPlayer.class);
    
    ClientTopology topology;
    
    protected List<Hand> hands = new ArrayList<>();
    
    protected Address myAddress;
    protected Actor channel;   
    
    protected Dealer dealer;
    
    protected Hand playing;
    
    protected Integer index = 0;
    
    protected Long serialno = -1L;
    
    protected Integer bet;

    public RealPlayer(Dealer dealer, Address playerAddress) {
        this.dealer = dealer;
        
        String host = playerAddress.getHost();
        Integer port = playerAddress.getPort();
        LOG.info("channel addr = "+playerAddress);
        
        this.topology = new ClientTopology(host, port, 5, TimeUnit.SECONDS, 3, TimeUnit.SECONDS);

        // Tell channel surrogate's ready
        this.channel = topology.getActor(playerAddress);
        
//        topology.shutdown();
    }
    
    public void ready() {
        channel.send(new Ready(myAddress));
    }

    @OnMessage(type = Bet.class)
    public void onReceive(Bet bet) {
        this.bet = bet.getAmt();
        
        LOG.info("ghost received bet = "+this.bet);
        
        dealer.bet(this, bet.getHid(), (int)this.bet);
    }
    
    @OnMessage(type = Hit.class)
    public void onReceive(Request request) {
        Hid hand = request.getHid();
        
        if(request instanceof Hit)
            dealer.hit(this, hand);
        
        else if(request instanceof Stay)
            dealer.stay(this, hand);
        
        else if(request instanceof DoubleDown)
            dealer.doubleDown(this, hand);
        
        else
            LOG.error("received unknown request: "+request+" for hand = "+hand);
    }
    
    @OnMessage(type = Stay.class)
    public void onReceive(Stay request) {
        serialno = request.getSerialno();
        
        dealer.stay(this, request.getHid());
    }
    
    @OnMessage(type = DoubleDown.class)
    public void onReceive(DoubleDown request) {
        // Doubles the bet
        bet *= 2;
        
        dealer.doubleDown(this, request.getHid());
    }

    /**
     * Sets my address since channel doesn't know where it is.
     * @param myAddress My address
     */
    public void setMyAddress(Address mine) {
        this.myAddress = mine;
    }

    @Override
    public void observe(Hid hid, Card card) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void insure() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void bust(Hid hid) {
        channel.send(new Bust(hid,(double)bet));
    }

    @Override
    public void win(Hid hid) {
        channel.send(new Win(hid,(double)bet));
    }

    @Override
    public void loose(Hid hid) {
        channel.send(new Loose(hid,(double)bet));
    }

    @Override
    public void push(Hid hid) {
        channel.send(new Push(hid));
    }

    @Override
    public void startGame(List<Hid> hids) {
        channel.send(new Starting(hids));
    }

    @Override
    public void endGame(Double bankroll) {
        channel.send(new Ending(bankroll));
    }

    @Override
    public void shuffling() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void play(Hid hid) {
        channel.send(new Play(hid));
    }

    @Override
    public void deal(Hid hid, Card card, int[] values) {
        Deal deal = new Deal(hid,values,card);
        
        channel.send(deal);
    }

    @Override
    public void blackjack(Hid hid) {
        channel.send(new Blackjack(hid,bet * 1.5) );
    }

    @Override
    public void charlie(Hid hid) {
        channel.send(new Charlie(hid,(double)bet) );
    }
    
    @Override
    public String toString() {
        return this.myAddress + " -> " + channel.getAddress();
    }
}
