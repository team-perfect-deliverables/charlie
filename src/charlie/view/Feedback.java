/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charlie.view;

/**
 *
 * @author roncoleman125
 */
public class Feedback {
    private final String text;
    private final int x;
    private final int y;
    public Feedback(String text,int x, int y) {
        this.text = text;
        this.x = x;
        this.y = y;
    }

    public String getText() {
        return text;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
    
}
