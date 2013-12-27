/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charlie.view.sprite;

import charlie.util.Point;
import java.awt.Graphics2D;
import java.awt.Image;

/**
 *
 * @author roncoleman125
 */
abstract public class Sprite {
    protected Image img;
    protected int x;
    protected int y;
    
    public Sprite() {
        
    }
    
    public Sprite(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public void render(Graphics2D g) {
        // Majic: possible synchronizaton problem here
        if(img == null)
            return;
        
        g.drawImage(this.img,x,y,null);
    }
    
    public void update() {
        
    }

     /**
     * Get the value of x
     *
     * @return the value of x
     */
    public int getX() {
        return x;
    }

    /**
     * Set the value of x
     *
     * @param x new value of x
     */
    public void setX(int x) {
        this.x = x;
    }


    /**
     * Get the value of y
     *
     * @return the value of y
     */
    public int getY() {
        return y;
    }

    /**
     * Set the value of y
     *
     * @param y new value of y
     */
    public void setY(int y) {
        this.y = y;
    }
    
    public void setXY(Point p) {
        this.x = p.getX();
        this.y = p.getY();
    }

    public int getWidth() {
        return img.getWidth(null);
    }
    
    public int getHeight() {
        return img.getHeight(null);
    }
    
    public Image getImage() {
        return img;
    }
}
