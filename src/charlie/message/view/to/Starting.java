/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charlie.message.view.to;

import charlie.card.Hid;
import charlie.message.Message;
import java.util.List;

/**
 *
 * @author roncoleman125
 */
public class Starting extends Message {
    private final List<Hid> hids;
    
    public Starting(List<Hid> hids) {
        this.hids = hids;
    }

    public List<Hid> getHids() {
        return hids;
    }
}
