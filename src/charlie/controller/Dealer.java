/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charlie.controller;

import charlie.card.Hand;
import charlie.card.Shoe;
import charlie.card.DealerHand;
import charlie.actor.House;
import charlie.actor.RealPlayer;
import charlie.card.Card;
import charlie.card.HoleCard;
import charlie.card.Hid;
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
 *
 * @author roncoleman125
 */
public class Dealer implements Serializable { 
    private final Logger LOG = LoggerFactory.getLogger(Dealer.class);
    private final static Double PROFIT = 1.0;
    private final static Double LOSS = -1.0;
    protected Shoe shoe;
    protected HashMap<Hid,Hand> hands = new HashMap<>();
    protected HashMap<Hid,Integer> bets = new HashMap<>();
    protected HashMap<Hid,IPlayer> players = new HashMap<>();
    protected HashMap<IPlayer,Double> accounts = new HashMap<>();
    protected List<Hid> handSequence = new ArrayList<>();
    protected List<IPlayer> playerSequence = new ArrayList<>();
    protected final House house;
    protected Integer handSeqIndex = 0;
    protected IPlayer active = null;
    protected DealerHand dealerHand;
    private final Double deposit;
    
    public Dealer(House house,Double deposit) {
        this.house = house;
        this.deposit = deposit;

        Properties props = house.getProps();
        
        int test = Integer.parseInt(props.getProperty("charlie.test", "-1"));
        
        if(test == -1)
            shoe = new Shoe();
        else
            shoe = new Shoe(test);
    }
    
    public void bet(RealPlayer player,Hid hid,Integer bet) {
        LOG.info("got new bet = "+bet+" from "+player+" for hid = "+hid);
        
        handSequence.clear();
        playerSequence.clear();
        hands.clear();
        bets.clear();


        handSequence.add(hid);
        playerSequence.add(player);  
        bets.put(hid, bet);        
        players.put(hid, player);
        
        this.handSeqIndex = 0;

        if(!accounts.containsKey(player))
            accounts.put(player, deposit);
        
        // Add new hand for this player
        hands.put(hid, new Hand(hid));
        
        // Create the bots
        spawnBots();

        // Create the dealer hand
        dealerHand = new DealerHand(new Hid(Seat.DEALER));
        
        // Shuffle cards, if needed
        if(shoe.shuffleNeeded()) {
            shoe.shuffle();
            
            for(IPlayer _player: playerSequence)
                _player.shuffling();
        }
        
        // Let the game begin!
        startGame();
    }
        
    protected void spawnBots() {
        // TODO:
    }
    
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
            
            for(IPlayer player: playerSequence)              
                player.startGame(hids);
            
            Thread.sleep(250);
            
            // First round hole card to everyone
            HoleCard holeCard = new HoleCard(shoe.next());
            
            dealerHand.hit(holeCard);
            
            round(hids,holeCard);
            
            Thread.sleep(Constant.DEAL_DELAY);
            
            // Second round up-card to everyone
            Card upCard = shoe.next();
            
            dealerHand.hit(upCard);
            
            round(hids,upCard);

            // Check for dealer having blackjack
            dealerHand.revalue();
            
            if(dealerHand.blackjack()) {
                closeGame();
            }
            else
                goNextHand();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    protected void round(List<Hid> hids,Card dealerCard) {
        try {
            for(Hid hid: hids) {
                IPlayer player = players.get(hid);
                
                // If there's no correspondsing player, must be dealer's hid
                if(player == null)
                    continue;
                
                Card card = shoe.next();
                
                LOG.info("dealing to "+player+" card 1 = "+card);  
                Hand hand = this.hands.get(hid);
                hand.hit(card);
                player.deal(hid, card, hand.getValues());

                Thread.sleep(Constant.DEAL_DELAY);

                LOG.info("sending dealer card = "+dealerCard);

                player.deal(dealerHand.getHid(), dealerCard, dealerHand.getValues());
            }            
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public void hit(IPlayer player,Hid hid) {
        Hand hand = validate(hid);
        if(hand == null) {
            LOG.error("got invalide HIT player = "+player);
            return;
        }
        
        Card card = shoe.next();
        
        hand.hit(card);
        
        player.deal(hid, card, hand.getValues());
        
        if(hand.broke()) {
            updateAccount(hid,LOSS);
            
            for (IPlayer _player : playerSequence)
                _player.bust(hid);
            
            goNextHand();
        }
        else if(hand.charlie()) {
            updateAccount(hid,PROFIT);
            
            for (IPlayer _player : playerSequence)
                _player.charlie(hid);
            
            goNextHand();
        }
    }    
    
    public void stay(IPlayer player, Hid hid) {
        Hand hand = validate(hid);
        if(hand == null) {
            LOG.error("got invalide STAY player = "+player);
            return;
        }
        
        goNextHand();
    }
    
    public void doubleDown(IPlayer player, Hid hid) {
        Hand hand = validate(hid);
        
        if(hand == null) {
            LOG.error("got invalide DOUBLE DOWN player = "+player);
            return;
        }
        
        Card card = shoe.next();

        hand.hit(card);
        
        // Dubble the bet
        Integer bet = bets.get(hid) * 2;

        bets.put(hid, bet);
        
        hand.hit(card);
        
        player.deal(hid, card, hand.getValues());
        
        if(hand.broke())
            player.bust(hid);
        
        goNextHand();
    }
    
    public void goNextHand() {
        // Get next hand and inform player
        if(handSeqIndex < handSequence.size()) {
            Hid hid = handSequence.get(handSeqIndex++);
            
            active = players.get(hid);
            LOG.info("active player = "+active);

            // Check for blackjack before moving on
            Hand hand = this.hands.get(hid);
            if(hand.blackjack()) {
                for (IPlayer player : playerSequence)
                    player.blackjack(hid);
             
                goNextHand();
                
                return;
            }
            
            active.play(hid);
            
            return;
        }

        // Close out with the dealer making last play
        closeGame();
    }
    
    protected void closeGame() { 
        // Tell everyone it's dealer's turn
        signal();
        
        // "null" card means just update the value of the hand
        for (IPlayer player : playerSequence)
            player.deal(dealerHand.getHid(), null, dealerHand.getValues());
     
        // Dealer only plays if there is someone standing
        if (handsStanding() && !dealerHand.blackjack()) {
            // Draw until we reach 17 or we break
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
                } catch (InterruptedException ex) {
                    java.util.logging.Logger.getLogger(Dealer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        // Compute outcomes and inform everyone
        // Case "bust" & "charlie" =>  handled during hit cycle
        // Case dealer blackjack => handled during initial deal
        for(Hid hid: handSequence) {
            Hand hand = hands.get(hid);
            
            if(hand.broke() || hand.charlie())
                continue;

            if(hand.getValue() < dealerHand.getValue() && !dealerHand.broke()) {
                updateAccount(hid,-1.0);
                
                for (IPlayer player: playerSequence)
                    player.loose(hid);
            }
            else if(hand.getValue() < dealerHand.getValue() && dealerHand.broke() ||
                    hand.getValue() > dealerHand.getValue() && !dealerHand.broke()) {
                updateAccount(hid,1.0);
                for (IPlayer player: playerSequence)
                    player.win(hid);   
            }
            else if(hand.getValue() == dealerHand.getValue())
                for (IPlayer player: playerSequence)
                    player.push(hid);
//            else
//                LOG.error("bad outcome");

        }
        
        // Wrap up the accounting
        wrapUp();
    }
    
    protected void updateAccount(Hid hid,Double gain) {
        IPlayer player = this.players.get(hid);
        
        if(!accounts.containsKey(player))
            return;
        
        Double bankroll = accounts.get(player);
        
        Integer bet = this.bets.get(hid);
        
        bankroll += (gain * bet);
        
        accounts.put(player, bankroll);
    }
    
    protected void wrapUp() {
        for (IPlayer player: playerSequence) {
            if(accounts.containsKey(player)) {
                Double bankroll = accounts.get(player);
                player.endGame(bankroll);
            }
        }         
    }
    
    protected void signal() {
        for (IPlayer player: playerSequence) {
            player.play(this.dealerHand.getHid());
        }    
    }
    
    protected boolean handsStanding() {
        for(Hid hid: handSequence) {
            Hand hand = hands.get(hid);
            
            if(!hand.broke())
                return true;
        }
        
        return false;
    }
    
    protected Hand validate(Hid hid) {
        if(hid == null)
            return null;
        
        Hand hand = hands.get(hid);
        
        if(hand.broke())
            return null;
        
        if(players.get(hid) != active)
            return null;
        
        return hand;
    }

    public HashMap<Hid, Hand> getHands() {
        return hands;
    }
    
    
}
