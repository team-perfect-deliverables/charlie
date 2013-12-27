/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charlie.message.view.from;

import charlie.card.Hid;
import charlie.message.Message;

/**
 *
 * @author roncoleman125
 */
public class Bet extends Message {
    private final Integer amt;
    private final Hid hid;
    
    public Bet(Hid hid) {
        this(hid,5);
    }
    
    public Bet(Hid hid, Integer amt) {
        this.hid = hid;
        this.amt = amt;
    }

    public Hid getHid() {
        return hid;
    }

    public Integer getAmt() {
        return amt;
    }
    
    @Override
    public String toString() {
        return "hid = "+this.hid+" amt = "+amt;
    }
}
