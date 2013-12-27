/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charlie.view;

import charlie.view.sprite.Sprite;
import charlie.card.Card;
import charlie.card.HoleCard;
import charlie.util.Point;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;

/**
 *
 * @author roncoleman125
 */
public class ACard extends Sprite {

    protected final static int SPEED = 15;
    protected Point home = new Point(ShoeView.SHOE_X, ShoeView.SHOE_Y);
    protected Image back;
    protected Image front;
    protected boolean up = true;

    public ACard(Card card, Point pos) {
        front = AHandsManager.getImage(card);

        if (card instanceof HoleCard) {
            this.back = AHandsManager.getBackImage();
            up = false;
        }

        this.x = pos.getX();
        this.y = pos.getY();
    }

    public ACard(ACard card, Point home) {
        this.front = card.front;
        this.back = card.back;
        this.up = card.up;
        this.x = card.getX();
        this.y = card.getY();
        this.home = home;
    }

    @Override
    public void render(Graphics2D g) {
        // Majic: synchronizaton problem here !!!
        if(img == null)
            return;
        
        super.render(g);
        
        // Draw a border -- it looks nicer
        int w = img.getWidth(null);
        int h = img.getHeight(null);

        g.setColor(Color.GRAY);

        g.drawRect(x, y, w, h);
    }

    @Override
    public void update() {
        if(back != null)
            System.out.print("");
        
        // Set which image is showing
        if (up == true || back == null)
            img = front;
        else
            img = back;

        if(this.landed())
            return;

        // Otherwise get the angle of attack
        double sx = home.getX() - x;
        double sy = home.getY() - y;

        double s = Math.sqrt(sx * sx + sy * sy);

        double theta = Math.asin(sx / s);

        // Move along attack angle at our speed
        double dx = SPEED * Math.sin(theta) + 0.5;
        double dy = SPEED * Math.cos(theta) + 0.5;

        this.x += dx;
        this.y += dy;

        // Correct overshoot, if necessary
        if (Math.abs(home.getX() - x) <= SPEED) {
            x = home.getX();
        }

        if (Math.abs(home.getY() - y) <= SPEED) {
            y = home.getY();
        }
    }

    public boolean landed() {
        // Stay put if we're already home
        if (home.getX() == x && home.getY() == y)
            return true;
        
       return false;
    }
    
    public void reveal() {
        up = true;
    }
    
    public void setHome(Point home) {
        this.home = home;
    }

    public void settle() {
        this.x = home.getX();
        this.y = home.getY();
    }

    public Point getPoint() {
        return new Point(x, y);
    }

    public Point getHome() {
        return home;
    }
    
    
}
