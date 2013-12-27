/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
 *
 * @author roncoleman125
 */
public class Hid implements Serializable {
    private final org.slf4j.Logger LOG = LoggerFactory.getLogger(Hid.class);
    
    private static Random ran = new Random(0);
    
    private Long handno;
    private String host = "UNKNOWN";
    private Seat seat;

    public Hid() {
        try {            
            handno = Math.abs(ran.nextLong());
            
            this.seat = Seat.YOU;
            
            InetAddress addr = InetAddress.getLocalHost();
            
            this.host = addr.getHostName();
            
        } catch (UnknownHostException ex) {
            LOG.error(ex.toString());
        }
    }
    
    public Hid(Seat seat) {
        this();
        this.seat = seat;
    }
    
    public Hid(Long handno) {
        this();
        this.handno = handno;
    }
    
    public Long getHandno() {
        return handno;
    }

    public void setHandno(Long handno) {
        this.handno = handno;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Seat getSeat() {
        return seat;
    }

    public void setSeat(Seat seat) {
        this.seat = seat;
    }
    
    @Override
    public String toString() {
        return host + ":" + seat + ":" +Long.toHexString(this.handno).toUpperCase();
    }
    
    /**
     * Hashes this object to insure hash code based on hand id.
     * @return Hash code
     */
    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Hid other = (Hid) obj;
        if (!Objects.equals(this.handno, other.handno)) {
            return false;
        }
        return true;
    }
}
