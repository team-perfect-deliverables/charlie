/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charlie.card;

/**
 *
 * @author roncoleman125
 */
public class DealerHand extends Hand {
    public DealerHand(Hid hid) {
        super(hid);
    }
    
    public Card upCard() {
        if(cards.isEmpty())
            return null;
        
        return cards.get(0);
    }
}
