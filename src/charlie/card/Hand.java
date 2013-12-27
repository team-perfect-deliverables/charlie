/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charlie.card;

import charlie.card.HoleCard;
import charlie.card.Card;
import charlie.card.Hid;
import charlie.util.Constant;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author roncoleman125
 */
public class Hand implements Serializable {  
    protected Hid hid;
    
    protected List<Card> cards = new ArrayList<>();

    protected int[] values;

    public Hand() {
        this.hid = new Hid();
        values = new int[2];
    }
    
    public Hand(Hid hid) {
        this.hid = hid;
        values = new int[2];
    }
    
    public boolean broke() {
        if(values[Constant.HAND_VALUE] > 21 && values[Constant.HAND_SOFT_VALUE] > 21)
            return true;
        
        return false;
    }
    
    public boolean blackjack() {
        if(cards.size() == 2 && values[Constant.HAND_SOFT_VALUE] == 21)
            if(cards.get(0).isAce() || cards.get(1).isAce())
                return true;
        
        return false;
    }
    
    public boolean charlie() {
        if(cards.size() == 5 && values[Constant.HAND_SOFT_VALUE] <= 21)
            return true;
        
        return false;
    }
    
    public Hid getHid() {
        return hid;
    }
    
    public void hit(Card card) {
        this.cards.add(card);

        if(card instanceof HoleCard)
            return;
        
        Integer value = card.value();
        
        values[Constant.HAND_VALUE] += value;
        values[Constant.HAND_SOFT_VALUE] += value;
        
        if(card.isAce() && values[Constant.HAND_SOFT_VALUE]+10 <= 21)
            values[Constant.HAND_SOFT_VALUE] += 10;
    }
    
    public void revalue() {
        values[Constant.HAND_VALUE] = values[Constant.HAND_SOFT_VALUE] = 0;
        
        for(Card card: cards) {
            int value = card.value();
            
            values[Constant.HAND_VALUE] += value;
            values[Constant.HAND_SOFT_VALUE] += value;
            
            if(card.isAce() && values[Constant.HAND_SOFT_VALUE]+10 <= 21)
                values[Constant.HAND_SOFT_VALUE] += 10;
        }
    }
    
    public int[] getValues() {
        return values;
    }
    
    public int getValue() {
        return values[Constant.HAND_SOFT_VALUE] <= 21 ?
                values[Constant.HAND_SOFT_VALUE] :
                values[Constant.HAND_VALUE];
    }

}
