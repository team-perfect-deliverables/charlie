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
abstract public class Request extends Message {
    protected final Hid hid;
    
    public Request(Hid hid) {
        this.hid = hid;
    }
    
    public Hid getHid() {
        return hid;
    }
}
