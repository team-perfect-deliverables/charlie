/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charlie.audio;


import java.io.ByteArrayInputStream;

/**
 *
 * @author roncoleman125
 */
public class Bais extends ByteArrayInputStream {
    public Bais(byte[] buf,int offset,int len) {
        super(buf,offset,len);
    }
    
    public void rewind() {
        super.pos = 0;
    }
    
}
