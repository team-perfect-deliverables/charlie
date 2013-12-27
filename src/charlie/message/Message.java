/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charlie.message;

import com.googlecode.actorom.Address;
import java.io.Serializable;

/**
 *
 * @author roncoleman125
 */
abstract public class Message implements Serializable {
    protected Long serialno = 0L;
    protected static Long counter = 0L;
    protected final static String monitor = "YUMMY";
    private Address source;
    private final Long stamp = System.currentTimeMillis();;

    public Message() {
        this.source = null;
    }
    
    public Message(Address source) {
        this.source = source;
        
        synchronized(monitor) {
            serialno = counter;
            counter += 1;
        }
    }

    public Long getSerialno() {
        return serialno;
    }

    public void setSerialno(Long serialno) {
        this.serialno = serialno;
    }

    public Address getSource() {
        return source;
    }
    
    public void setSource(Address source) {
        this.source = source;
    }

    public Long getStamp() {
        return stamp;
    }
    
}
