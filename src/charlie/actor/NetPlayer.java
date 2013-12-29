/*
 Copyright (c) 2014 Ron Coleman

 Permission is hereby granted, free of charge, to any person obtaining
 a copy of this software and associated documentation files (the
 "Software"), to deal in the Software without restriction, including
 without limitation the rights to use, copy, modify, merge, publish,
 distribute, sublicense, and/or sell copies of the Software, and to
 permit persons to whom the Software is furnished to do so, subject to
 the following conditions:

 The above copyright notice and this permission notice shall be
 included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
import charlie.message.view.to.Observe;
import charlie.message.view.to.Ready;
import charlie.message.view.to.Play;
import charlie.message.view.to.Push;
import charlie.message.view.to.Starting;
import charlie.message.view.to.Win;
import com.googlecode.actorom.Actor;
import com.googlecode.actorom.Address;
import com.googlecode.actorom.annotation.OnMessage;
import com.googlecode.actorom.remote.ClientTopology;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements the game interface to a player in the cloud.
 * @author Ron Coleman
 */
public class NetPlayer implements IPlayer {
    private final Logger LOG = LoggerFactory.getLogger(NetPlayer.class);
    ClientTopology topology;
    protected Address myAddress;
    protected Actor courier;   
    protected Dealer dealer;
    protected Hand playing;

    /**
     * Constructor
     * @param dealer Dealer the player is using.
     * @param courierAddress Actor address of our courier to the remote host
     */
    public NetPlayer(Dealer dealer, Address courierAddress) {
        this.dealer = dealer;
        
        String host = courierAddress.getHost();
        Integer port = courierAddress.getPort();
        LOG.info("courier addr = "+courierAddress);
        
        this.topology = new ClientTopology(host, port, 5, TimeUnit.SECONDS, 3, TimeUnit.SECONDS);

        // Tell courier surrogate's ready
        this.courier = topology.getActor(courierAddress);
    }
    
    /**
     * Sends ready to the courier to let remote host know we're connected.
     */
    public void ready() {
        courier.send(new Ready(myAddress));
    }

    /**
     * Receives a bet from the courier.
     * @param bet Bet
     */
    @OnMessage(type = Bet.class)
    public void onReceive(Bet bet) {     
        LOG.info("player actor received bet = "+bet.getAmt());
        
        dealer.bet(this, bet.getHid(), bet.getAmt());
    }
    
    /**
     * Receives a request from the courier.
     * @param request Request
     */
    @OnMessage(type = Request.class)
    public void onReceive(Request request) {
        LOG.info("received request = "+request);
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
    
//    @OnMessage(type = Stay.class)
//    public void onReceive(Stay request) {      
//        dealer.stay(this, request.getHid());
//    }
//    
//    @OnMessage(type = DoubleDown.class)
//    public void onReceive(DoubleDown request) {       
//        dealer.doubleDown(this, request.getHid());
//    }

    /**
     * Sets my address since courier doesn't know where it is.
     * @param myAddress My address
     */
    public void setMyAddress(Address mine) {
        this.myAddress = mine;
    }

    /**
     * Observes a card for a hand.
     * @param hid Hand id
     * @param card Card
     */
    @Override
    public void observe(Hid hid, Card card) {
        courier.send(new Observe(hid,card));
    }

    /**
     * Sends request to courier for insurance
     */
    @Override
    public void insure() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Sends break notice to courier.
     * @param hid Hand id
     * @param bet Bet amount lost
     */
    @Override
    public void bust(Hid hid,Double bet) {
        courier.send(new Bust(hid,bet));
    }
    
    /**
     * Sends win notice to courier.
     * @param hid Hand id
     * @param bet Bet amount won
     */
    @Override
    public void win(Hid hid,Double bet) {
        courier.send(new Win(hid,bet));
    }
    
    /**
     * Sends lost notice to courier.
     * @param hid Hand id
     * @param bet Bet amount lost
     */
    @Override
    public void loose(Hid hid,Double bet) {
        courier.send(new Loose(hid,bet));
    }
    
    /**
     * Sends push notice to courier.
     * @param hid Hand id
     */
    @Override
    public void push(Hid hid) {
        courier.send(new Push(hid));
    }

    /**
     * Sends starting notice to courier.
     * @param hids Hands starting
     */
    @Override
    public void startGame(List<Hid> hids,int shoeSize) {
        courier.send(new Starting(hids,shoeSize));
    }

    /**
     * Sends ending notice to courier.
     * @param bankroll New bankroll
     */
    @Override
    public void endGame(Double bankroll) {
        courier.send(new Ending(bankroll));
    }

    /**
     * Sends shuffling notice to courier.
     */
    @Override
    public void shuffling() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Sends play turn notice to courier for a hand.
     * @param hid Hand id
     */
    @Override
    public void play(Hid hid) {
        courier.send(new Play(hid));
    }

    /**
     * Deals a card with the hand values.
     * @param hid Hand id
     * @param card Card
     * @param values Hand values
     */
    @Override
    public void deal(Hid hid, Card card, int[] values) {
        Deal deal = new Deal(hid,values,card);
        
        courier.send(deal);
    }
    
    /**
     * Sends blackjack notice to courier.
     * @param hid Hand id
     * @param bet Bet amount won
     */
    @Override
    public void blackjack(Hid hid,Double bet) {
        courier.send(new Blackjack(hid,bet) );
    }
    
    /**
     * Sends Charlie notice to courier.
     * @param hid Hand id
     * @param bet Bet amount won
     */
    @Override
    public void charlie(Hid hid,Double bet) {
        courier.send(new Charlie(hid,bet) );
    }
    
    /**
     * Converts instance to a string.
     * @return String representation
     */
    @Override
    public String toString() {
        return this.myAddress + " -> " + courier.getAddress();
    }
}
