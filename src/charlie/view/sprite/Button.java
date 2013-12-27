/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charlie.view.sprite;

import java.awt.Graphics2D;
import java.awt.Image;

/**
 *
 * @author roncoleman125
 */
public class Button {
    protected final Image up;
    protected final Image down;
    protected boolean pressed = false;
    private final int x;
    private final int y;
    private int width;
    private int height;
    protected boolean ready = true;
    
    public Button(Image up, Image down,int x, int y) { 
        this.up = up;
        this.down = down;
        this.x = x;
        this.y = y;
        this.width = this.height = up.getWidth(null);
    }
    
    public void render(Graphics2D g) {
        if(!pressed)
            g.drawImage(up, x, y, null);
        else
            g.drawImage(down, x, y, null);
    }
    
    public boolean isPressed(int x, int y) {
        if( x > this.x && x < this.x+width && y > this.y && y < this.y+height) {
            pressed = true;
            ready = false;
        }
        
        return pressed;
    }
    
    public void release() {
        pressed = false;
        ready = true;
    }
    
    public boolean isReady() {
        return ready;
    }
    
    public Image getImage() {
        return up;
    }
}
