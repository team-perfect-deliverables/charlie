/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charlie.view.sprite;

import java.awt.Image;

/**
 *
 * @author roncoleman125
 */
public class Chip extends Sprite {
    private final int amt;
    
    public Chip(Chip chip) {
        this(chip.img,chip.x,chip.y,chip.amt);
    }
    
    public Chip(Image img, int x, int y, int amt) {
        super(x,y);
        super.img = img;
        this.amt = amt;
    }

    public int getAmt() {
        return amt;
    }   
}
