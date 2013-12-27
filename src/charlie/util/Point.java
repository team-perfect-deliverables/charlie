/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charlie.util;

/**
 *
 * @author roncoleman125
 */
public class Point {
    
    protected int x;
    protected int y;
    
    public Point() {
        this.x = this.y = 0;
    }
    
    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public Point(Point pt) {
        this.x = pt.x;
        this.y = pt.y;
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
}
