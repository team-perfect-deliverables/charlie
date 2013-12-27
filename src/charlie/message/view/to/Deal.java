/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charlie.message.view.to;

import charlie.card.Card;
import charlie.card.Hid;
import charlie.message.Message;

/**
 *
 * @author roncoleman125
 */
public class Deal extends Message {  
    private final Hid hid;
    private final Card card;
    private final int[] values;
    
    public Deal(Hid hid, int[] values, Card card) {
        this.hid = hid;
        this.values = values;
        this.card = card;
    }

    public Hid getHid() {
        return hid;
    }

    public Card getCard() {
        return card;
    }
    
    public int[] getHandValues() {
        return values;
    }
}
