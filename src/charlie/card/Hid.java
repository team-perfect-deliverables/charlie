/*
 Copyright (c) 2014 Ron Coleman

 Permission is hereby granted, free of charge, to any person obtaining
 a copy of this software and associated documentation files (the
 "Software"), to deal in the Software without restriction, including
 without limitation the rights to use, copy, modify, merge, publish,
 distribute, sublicense, and/or sell copies of the Software, and to
 permit persons to whom the Software is furnished to do so, subject to
 the following conditions:

 The above copyright notice and this permission notice shall be
 included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package charlie.card;

import charlie.controller.Seat;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.Random;
import org.slf4j.LoggerFactory;

/**
 * This class implements the hand id.
 * The hand id is ubiquitous and serves as the key to managing hands throughout
 * a game. A hand id does not have meaning apart from a hand. Thus, if there is
 * a hand id, there is a corresponding hand somewhere in the game.
 * @author Ron Coleman
 */
public class Hid implements Serializable {
    private final org.slf4j.Logger LOG = LoggerFactory.getLogger(Hid.class);
    private static Random ran = new Random(0);
    private Long key;
    private String host = "UNKNOWN";
    private Seat seat;
    protected Double amt;
    
    /**
     * Constructor
     * @param seat Hand id for this seat
     */
    public Hid(Seat seat,Double amt) {
        try {       
            this.amt = amt;
            this.key = Math.abs(ran.nextLong());
            this.seat = seat;
            
            InetAddress addr = InetAddress.getLocalHost();
            
            this.host = addr.getHostName();
            
        } catch (UnknownHostException ex) {
            LOG.error(ex.toString());
        }
    }
    
    public Hid(Seat seat) {
        this(seat,0.0);
    }

    /**
     * Gets the amount for this hand.
     * @return Amount
     */
    public Double getAmt() {
        return amt;
    }

    /**
     * Sets the amount for this hand.
     * @param amt Amount
     */
    public void setAmt(Double amt) {
        this.amt = amt;
    }
    
    /**
     * Doubles the amount.
     */
    public void multiplyAmt(Double factor) {
        this.amt *= factor;
    }

    /**
     * Gets the seat.
     * @return A seat id
     */
    public Seat getSeat() {
        return seat;
    }

    /**
     * Sets the seat.
     * @param seat Seat
     */
    public void setSeat(Seat seat) {
        this.seat = seat;
    }
    
    /**
     * Gets the string representation of the hand id.
     * @return String representation
     */
    @Override
    public String toString() {
        return host + ":" + seat + ":" +Long.toHexString(this.key).toUpperCase();
    }
    
    /**
     * Hashes this object to insure hash code based on hand id.
     * @return Hash code
     */
    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    /**
     * Tests if object is an hand id.
     * @param obj Other object
     * @return True if hands are equal.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Hid other = (Hid) obj;
        if (!Objects.equals(this.key, other.key)) {
            return false;
        }
        return true;
    }
}
