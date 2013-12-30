/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charlie.message.view.to;

import charlie.message.Message;

/**
 *
 * @author roncoleman125
 */
public class Ending extends Message { 
    private final int shoeSize;
    public Ending(int shoeSize) {
        this.shoeSize = shoeSize;
    }

    public int getShoeSize() {
        return shoeSize;
    }
    
}
