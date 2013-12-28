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
public class Observe extends Message {
    private final Hid hid;
    private final Card card;
    public Observe(Hid hid, Card card) {
        this.hid = hid;
        this.card = card;
    }

    public Hid getHid() {
        return hid;
    }

    public Card getCard() {
        return card;
    }
    
    @Override
    public String toString() {
        return "card:" + card + " hid:" + hid;
    }
}
