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
package charlie.controller;

import charlie.card.Hand;
import charlie.card.shoe.Shoe;
//import isCharlie.card.DealerHand;
import charlie.actor.House;
import charlie.actor.NetPlayer;
import charlie.card.Card;
import charlie.card.HoleCard;
import charlie.card.Hid;
import charlie.card.shoe.ShoeFactory;
import charlie.util.Constant;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements the Blackjack dealer.
 * It uses the following rules:<br>
 * <ol>
 * <li>Dealer stands on 17, hard or soft.
 * <li>Blackjack pays 3:2. For discussion of house advantage of different
 * pay outs see http://answers.yahoo.com/question/index?qid=20080617174652AAOBfaj
 * <li>Five card Charlie pays 2:1.
 * </ol>
 * @author Ron Coleman
 */
public class Dealer implements Serializable { 
    public final static Double BLACKJACK_PAYS = 3/2.;
    public final static Double CHARLIE_PAYS = 2/1.;
    protected final static Double PROFIT = 1.0;
    protected final static Double LOSS = -1.0;    
    private final Logger LOG = LoggerFactory.getLogger(Dealer.class);
    protected Shoe shoe;
    protected HashMap<Hid,Hand> hands = new HashMap<>();
//    protected HashMap<Hid,Integer> bets = new HashMap<>();
    protected HashMap<Hid,IPlayer> players = new HashMap<>();
    protected List<Hid> handSequence = new ArrayList<>();
    protected List<IPlayer> playerSequence = new ArrayList<>();
    protected final House house;
    protected Integer handSeqIndex = 0;
    protected IPlayer active = null;
    protected Hand dealerHand;
    
    /**
     * Constructor
     * @param house House actor which launched us.
     */
    public Dealer(House house) {
        this.house = house;

        // Instantiate the shoe
        Properties props = house.getProps();
        
        String scenario = props.getProperty("charlie.shoe", "6deck");
        
        shoe = ShoeFactory.getInstance(scenario);
        
        shoe.init();
    }
    
    /**
     * Receives a bet request from a "real" you. Don't invoke this method
     * for a bot. Bots are instantiated directly by this class.
     * @param you Player
     * @param yours Hand
     * @param bet Bet amount
     */
    public void bet(NetPlayer you,Hid yours) {
        LOG.info("got new bet = "+yours.getAmt()+" from "+you+" for hid = "+yours);
        
        // Clear out the old stuff
        handSequence.clear();
        playerSequence.clear();
        hands.clear();

        HashMap<Seat,IBot> bots = spawnBots();
        
        // Add yours in sequence of hands to play
        if(bots != null) {
            handSequence.add(bots.get(Seat.ROSIE).getHid());
            playerSequence.add(bots.get(Seat.ROSIE));
            players.put(bots.get(Seat.ROSIE).getHid(),bots.get(Seat.ROSIE));
        }
        
        handSequence.add(yours);
        playerSequence.add(you); 
        players.put(yours, you);
        hands.put(yours, new Hand(yours));
        
        if(bots != null) {
            handSequence.add(bots.get(Seat.ROBBY).getHid());
            playerSequence.add(bots.get(Seat.ROBBY));
            players.put(bots.get(Seat.ROBBY).getHid(),bots.get(Seat.ROBBY));
        }
        
        handSeqIndex = 0;        

        // Create the dealer hand
        dealerHand = new Hand(new Hid(Seat.DEALER));
        
        // Shuffle cards, if needed
        if(shoe.shuffleNeeded()) {
            shoe.shuffle();
            
            for(IPlayer _player: playerSequence)
                _player.shuffling();
        }
        
        // Let the game begin!
        startGame();
    }
        
    protected HashMap<Seat,IBot> spawnBots() {
        return null;
    }
    
    /** Starts a game */
    protected void startGame() {
        LOG.info("starting a game");
        try {
            // Gather up all the initial hands
            List<Hid> hids = new ArrayList<>();
            
            for(Hid hid: hands.keySet()) {
                hids.add(hid);
            }
            
            // Include the dealer's hand
            hids.add(dealerHand.getHid());
          
            LOG.info("hands at table + dealer = "+hids.size());
            
            // Tell each player we're starting a game
            for(IPlayer player: playerSequence)              
                player.startGame(hids,shoe.size());
            
            Thread.sleep(250);
            
            // First round hole card sent to everyone
            HoleCard holeCard = new HoleCard(shoe.next());
            dealerHand.hit(holeCard);
            round(hids,holeCard);
            Thread.sleep(Constant.DEAL_DELAY);
            
            // Second round up-card sent to everyone
            Card upCard = shoe.next();
            dealerHand.hit(upCard);
            round(hids,upCard);

            // Revalue the dealer's hand since the normal hit doesn't count
            // the hole card
            dealerHand.revalue();
            
            // CHeck if players want to buy insurance
            if(upCard.isAce())
                insure();
            
            if(dealerHand.isBlackjack()) {
                closeGame();
            }
            else
                goNextHand();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Insures a dealer isBlackjack
     */
    protected void insure() {
        // TODO
    }
    
    /**
     * Deals a round of cards to everyone.
     * @param hids Hand ids
     * @param dealerCard The dealer card, up or hole.
     */
    protected void round(List<Hid> hids,Card dealerCard) {
        try {
            for(Hid hid: hids) {
                IPlayer player = players.get(hid);
                
                // If there's no correspondsing player, must be dealer's hid
                if(player == null)
                    continue;
                
                // Get a card from the shoe
                Card card = shoe.next();
                
                // Deal this card
                LOG.info("dealing to "+player+" card 1 = "+card); 
                
                Hand hand = this.hands.get(hid);
                
                hand.hit(card);
                
                player.deal(hid, card, hand.getValues());

                Thread.sleep(Constant.DEAL_DELAY);

                // Deal the corresponding dealer card
                LOG.info("sending dealer card = "+dealerCard);

                player.deal(dealerHand.getHid(), dealerCard, dealerHand.getValues());
            }            
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Hits player hand upon request only AFTER the initial rounds. 
     * @param player Player requesting a hit.
     * @param hid Player's hand id
     */
    public void hit(IPlayer player,Hid hid) {
        // Validate the request
        Hand hand = validate(hid);
        if(hand == null) {
            LOG.error("got invalide HIT player = "+player);
            return;
        }
        
        // Deal a card
        Card card = shoe.next();
        
        hand.hit(card);
        
        player.deal(hid, card, hand.getValues());
        
        // If the hand isBroke, we're done with this hand
        if(hand.isBroke()) {
            house.updateBankroll(player,hid.getAmt(),LOSS);
            
            // Tell everyone what happened
            for (IPlayer _player : playerSequence)
                _player.bust(hid);
            
            goNextHand();
        }
        // If hand got a isCharlie, we're done with this hand
        else if(hand.isCharlie()) {
            hid.multiplyAmt(CHARLIE_PAYS);
            house.updateBankroll(player,hid.getAmt(),PROFIT);
            
            // Tell everyone what happened
            for (IPlayer _player : playerSequence)
                _player.charlie(hid);
            
            goNextHand();
        }
    }    
    
    /**
     * Stands down player hand upon request only AFTER the initial rounds. 
     * @param player Player requesting a hit.
     * @param hid Player's hand id
     */
    public void stay(IPlayer player, Hid hid) {
        // Validate the request
        Hand hand = validate(hid);
        if(hand == null) {
            LOG.error("got invalide STAY player = "+player);
            return;
        }
        
        // Since player stayed, we're done with hand
        goNextHand();
    }
    
    /**
     * Double down player hand upon request only AFTER the initial rounds. 
     * @param player Player requesting a hit.
     * @param hid Player's hand id
     */    
    public void doubleDown(IPlayer player, Hid hid) {
        // Validate the request
        Hand hand = validate(hid);
        
        if(hand == null) {
            LOG.error("got invalide DOUBLE DOWN player = "+player);
            return;
        }
        
        Card card = shoe.next();

        hand.hit(card);
        
        // Doubble the bet and hit the hand once

        
        hand.hit(card);
        
        player.deal(hid, card, hand.getValues());
        
        // If hand isBroke, tell everyone
        if(hand.isBroke()) {
            house.updateBankroll(player,hid.getAmt(),LOSS);
            
            for (IPlayer _player : playerSequence)
                _player.bust(hid);
        }
        
        // Go to next hand regardless
        goNextHand();
    }
    
    /**
     * Moves to the next hand at the table
     */
    protected void goNextHand() {
        // Get next hand and inform player
        if (handSeqIndex < handSequence.size()) {
            Hid hid = handSequence.get(handSeqIndex++);

            active = players.get(hid);
            LOG.info("active player = " + active);

            // Check for isBlackjack before moving on
            Hand hand = this.hands.get(hid);

            // If hand has isBlackjack, it's not automatic hand wins
            // since the dealer may also have isBlackjack
            if (hand.isBlackjack()) {               
                hid.multiplyAmt(BLACKJACK_PAYS);

                IPlayer player = this.players.get(hid);

                house.updateBankroll(player, hid.getAmt(), PROFIT);
                for (IPlayer _player : playerSequence)
                    _player.blackjack(hid);

                goNextHand();

                return;
            }

            // Unless the player got a isBlackjack, tell the player they're
            // to start playing this hand
            active.play(hid);

            return;
        }

        // If there are no more hands, close out game with dealer
        // making last play.
        closeGame();
    }
    
    protected void closeGame() { 
        // Tell everyone it's dealer's turn
        signal();
        
        // "null" card means update the value of the hand
        for (IPlayer player : playerSequence)
            player.deal(dealerHand.getHid(), null, dealerHand.getValues());
     
        // Dealer only plays if there is someone standing and dealer doesn't
        // isBlackjack
        if (handsStanding() && !dealerHand.isBlackjack()) {
            // Draw until we reach (any) 17 or we break
            while (dealerHand.getValue() < 17) {
                Card card = shoe.next();

                dealerHand.hit(card);

                // Tell everybody what dealer drew
                for (IPlayer player : playerSequence) {
                    player.deal(dealerHand.getHid(), card, dealerHand.getValues());
                }
                
                try {
                    if(dealerHand.getValue() < 17)
                        Thread.sleep(Constant.DEAL_DELAY);
                }
                catch (InterruptedException ex) {
                    java.util.logging.Logger.getLogger(Dealer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        // Compute outcomes and inform everyone
        for(Hid hid: handSequence) {
            Hand hand = hands.get(hid);
            
            // These handled during hit cycle
            if(hand.isBroke() || hand.isCharlie())
                continue;

            // If hand less than dealer and dealer not isBroke, hand lost
            if(hand.getValue() < dealerHand.getValue() && !dealerHand.isBroke()) {
                house.updateBankroll(players.get(hid),hid.getAmt(),LOSS);
                
                for (IPlayer player: playerSequence)
                    player.loose(hid);
            }
            // If hand less than dealer and dealer isBroke OR...
            //    hand greater than dealer and dealer NOT isBroke => hand won
            else if(hand.getValue() < dealerHand.getValue() && dealerHand.isBroke() ||
                    hand.getValue() > dealerHand.getValue() && !dealerHand.isBroke()) {
                
                house.updateBankroll(players.get(hid),hid.getAmt(),PROFIT);
                
                for (IPlayer player: playerSequence)
                    player.win(hid);   
            }
            // If player and dealer hands same, hand pushed
            else if(hand.getValue() == dealerHand.getValue())
                for (IPlayer player: playerSequence)
                    player.push(hid);
//            else
//                LOG.error("bad outcome");

        }
        
        // Wrap up the game
        wrapUp();
    }
    

    
    /**
     * Tells everyone game over.
     */
    protected void wrapUp() {
        for (IPlayer player: playerSequence)           
            player.endGame(shoe.size());      
    }
    
    /**
     * Tells everyone it's dealer's turn.
     */
    protected void signal() {
        for (IPlayer player: playerSequence) {
            player.play(this.dealerHand.getHid());
        }    
    }
    
    /**
     * Returns true if there are any hands that haven't isBroke
     * @return True if at least one hand hasn't broken, false otherwise
     */
    protected boolean handsStanding() {
        for(Hid hid: handSequence) {
            Hand hand = hands.get(hid);
            
            if(!hand.isBroke())
                return true;
        }
        
        return false;
    }
    
    /**
     * Validates a hand.
     * @param hid Hand
     * @return True if had is valid, false otherwise
     */
    protected Hand validate(Hid hid) {
        if(hid == null)
            return null;
        
        Hand hand = hands.get(hid);
        
        if(hand.isBroke())
            return null;
        
        if(players.get(hid) != active)
            return null;
        
        return hand;
    }
}
