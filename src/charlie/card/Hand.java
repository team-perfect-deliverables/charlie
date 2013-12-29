/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charlie.card;

import charlie.util.Constant;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This class implements a hand which has a unique id, a collection of cards
 * and values.
 * @author Ron Coleman
 */
public class Hand implements Serializable {  
    protected Hid hid;
    protected List<Card> cards = new ArrayList<>();
    protected int[] values;

    /**
     * Constructor
     */
    public Hand() {
        this.hid = new Hid();
        values = new int[2];
    }
    
    /**
     * Constructor
     * @param hid Use this hand id.
     */
    public Hand(Hid hid) {
        this.hid = hid;
        values = new int[2];
    }
    
    /**
     * Tests if the hand is broke.
     * @return True if hand > 21
     */
    public boolean isBroke() {
        if(values[Constant.HAND_LITERAL_VALUE] > 21 && values[Constant.HAND_SOFT_VALUE] > 21)
            return true;
        
        return false;
    }
    
    /**
     * Tests if hand has a blackjack.
     * @return True if hand has A+10
     */
    public boolean isBlackjack() {
        if(cards.size() == 2 && values[Constant.HAND_SOFT_VALUE] == 21)
            if(cards.get(0).isAce() || cards.get(1).isAce())
                return true;
        
        return false;
    }
    
    /**
     * Tests if hand has a blackjack.
     * @return True if hand has A+10
     */    
    public boolean isCharlie() {
        if(cards.size() == 5 && values[Constant.HAND_SOFT_VALUE] <= 21)
            return true;
        
        return false;
    }
    
    /**
     * Gets the hand id.
     * @return Hand id
     */
    public Hid getHid() {
        return hid;
    }
    
    /**
     * Hits the hand with a card.
     * @param card 
     */
    public void hit(Card card) {
        this.cards.add(card);

        // If the card is a hole card, don't count it
        if(card instanceof HoleCard)
            return;
        
        Integer value = card.value();
        
        values[Constant.HAND_LITERAL_VALUE] += value;
        values[Constant.HAND_SOFT_VALUE] += value;
        
        if(card.isAce() && values[Constant.HAND_SOFT_VALUE]+10 <= 21)
            values[Constant.HAND_SOFT_VALUE] += 10;
    }
    
    /**
     * Revalues the hand.
     * This method uses all cards, including the hole hard. The method is
     * typically invoked when the dealer plays, that is, upon showing the
     * hole card.
     */
    public void revalue() {
        values[Constant.HAND_LITERAL_VALUE] = values[Constant.HAND_SOFT_VALUE] = 0;
        
        for(Card card: cards) {
            int value = card.value();
            
            values[Constant.HAND_LITERAL_VALUE] += value;
            values[Constant.HAND_SOFT_VALUE] += value;
            
            if(card.isAce() && values[Constant.HAND_SOFT_VALUE]+10 <= 21)
                values[Constant.HAND_SOFT_VALUE] += 10;
        }
    }
    
    /**
     * Gets the hands literal and soft values.
     * @return Hand values
     */
    public int[] getValues() {
        return values;
    }
    
    /**
     * Gets the hand unified value. 
     * @return Hand value
     */
    public int getValue() {
        return Hand.getValue(values);        
    }
    
    /**
     * Get hand unified value.
     * Typically this is the soft value with the literal value as the backup.
     * @param values
     * @return 
     */
    public static int getValue(int[] values) {
        return values[Constant.HAND_SOFT_VALUE] <= 21 ?
                values[Constant.HAND_SOFT_VALUE] :
                values[Constant.HAND_LITERAL_VALUE];        
    }

}
