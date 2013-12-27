/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charlie.server;

import com.googlecode.actorom.Address;
import java.io.Serializable;

/**
 *
 * @author roncoleman125
 */
public class Ticket implements Serializable {
    protected final Address house;
    protected final long number;
    protected double bankroll;

    public Ticket(Address house, long number,double bankroll) {
        this.house = house;
        this.number = number;
        this.bankroll = bankroll;
    }

    public Address getHouse() {
        return house;
    }

    public Long getNumber() {
        return number;
    }

    public Double getBankroll() {
        return bankroll;
    }

    public void setBankroll(Double bankroll) {
        this.bankroll = bankroll;
    }
    
    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Ticket))
            return false;
        
        Ticket ticket = (Ticket)obj;
        return ticket.number == number;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + (int) (this.number ^ (this.number >>> 32));
        return hash;
    }
    
    @Override
    public String toString() {
        return Long.toHexString(number).toUpperCase();
    }
}
