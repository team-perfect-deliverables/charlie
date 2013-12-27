/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charlie.message;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author roncoleman125
 */
public class Handshake implements Serializable {
    public enum Status { OK, ERROR };
    protected Long serialno;
    protected Long stamp = System.currentTimeMillis();
    protected Status status = Status.OK;
    protected String message = "NONE";
    protected Object data = null;

    public Handshake(Status status, Long serialno) {
        this.status = status;
        this.serialno = serialno;
    }
    
    public Handshake(Long serialno) {
        this.serialno = serialno;
    }

    public Long getSerialno() {
        return serialno;
    }
    
    @Override
    public String toString() {
        return new Date(stamp) + " >> " + serialno;
    }

    public Long getStamp() {
        return stamp;
    }

    public void setStamp(Long stamp) {
        this.stamp = stamp;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    
}
