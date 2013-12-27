/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charlie.message.view.from;

import charlie.message.Message;
import charlie.server.Ticket;
import com.googlecode.actorom.Address;

/**
 *
 * @author roncoleman125
 */
public class Arrival extends Message {
    protected final Address player;
    private final Ticket ticket;
    
    public Arrival(Ticket ticket,Address source) {
        super(source);
        this.ticket = ticket;
        this.player = source;
    }

    public Address getChannelAddress() {
        return player;
    }

    public Ticket getTicket() {
        return ticket;
    }
}
