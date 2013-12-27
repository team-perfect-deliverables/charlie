/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charlie.message.view.to;

import charlie.message.Message;
import com.googlecode.actorom.Address;

/**
 *
 * @author roncoleman125
 */
public class Ready extends Message {
    protected Address address;
    
    public Ready() {
    }
    
    public Ready(Address address) {
        this.address = address;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
    
}
