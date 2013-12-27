/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charlie.message.view.to;

import charlie.card.Hid;
import charlie.message.Message;

/**
 *
 * @author roncoleman125
 */
abstract public class Outcome extends Message {
    protected final Double pl;
    private final Hid hid;
    
    public Outcome(Hid hid, Double pl) {
        this.hid = hid;
        this.pl = pl;
    }

    public Double getPl() {
        return pl;
    }

    public Hid getHid() {
        return hid;
    }
}
