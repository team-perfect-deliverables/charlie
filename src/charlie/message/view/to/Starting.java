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
    private final int shoeSize;
    
    public Starting(List<Hid> hids,int shoeSize) {
        this.hids = hids;
        this.shoeSize = shoeSize;
    }

    public List<Hid> getHids() {
        return hids;
    }

    public int getShoeSize() {
        return shoeSize;
    }
}
