/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charlie.message.view.from;

import charlie.message.Message;
import com.googlecode.actorom.Address;

/**
 *
 * @author roncoleman125
 */
public class Logout extends Message {
    public Logout(Address mine) {
        super(mine);
    }
}
