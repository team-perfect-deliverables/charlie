/*
 Copyright (c) 2014 Ron Coleman

 Permission is hereby granted, free of charge, to any person obtaining
 a copy of this software and associated documentation files (the
 "Software"), to deal in the Software without restriction, including
 without limitation the rights to use, copy, modify, merge, publish,
 distribute, sublicense, and/or sell copies of the Software, and to
 permit persons to whom the Software is furnished to do so, subject to
 the following conditions:

 The above copyright notice and this permission notice shall be
 included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
 * This class implements an animated card that moves through the game world.
 * This card seeks home, if it is not already there, by traveling on a
 * straight line.
 * This class is the animated analog of the Card.
 * @see charlie.card.Card
 * @author Ron Coleman
 */
public class ACard extends Sprite {
    protected final static int SPEED = 15;
    protected Point home = new Point(ShoeView.SHOE_X, ShoeView.SHOE_Y);
    protected Image back;
    protected Image front;
    protected boolean up = true;

    /**
     * Constructor<p>
     * It is defined from a non-animated card.
     * @param card Card
     * @param pos Position in world
     */
    public ACard(Card card, Point pos) {
        front = AHandsManager.getImage(card);

        if (card instanceof HoleCard) {
            this.back = AHandsManager.getBackImage();
            up = false;
        }

        this.x = pos.getX();
        this.y = pos.getY();
    }

    /**
     * Copy constructor
     * @param card Card to copy
     * @param home 
     */
    public ACard(ACard card, Point home) {
        this.front = card.front;
        this.back = card.back;
        this.up = card.up;
        this.x = card.getX();
        this.y = card.getY();
        this.home = home;
    }

    /**
     * Renders the card.
     * @param g Graphics context
     */
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

    /**
     * Updates the card position, if needed.
     */
    @Override
    public void update() {        
        // Set which image is showing, front or back...thus
        // it is possible for a card to "flip" in its current place
        if (up == true || back == null)
            img = front;
        else
            img = back;

        // If card is home, there's nothing more to do.
        if(this.isLanded())
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

    /**
     * Tests if a card has reached its home.
     * @return True if the card (x,y) == home
     */
    public boolean isLanded() {
       return home.getX() == x && home.getY() == y;
    }
    
    /**
     * Turns over the card to its up face
     */
    public void flip() {
        up = true;
    }
    
    /**
     * Sets the home position in the world.
     * @param home Home point
     */
    public void setHome(Point home) {
        this.home = home;
    }

    /**
     * Puts a card in its home position.
     */
    public void settle() {
        this.x = home.getX();
        this.y = home.getY();
    }

    /**
     * Gets the card home position.
     * @return Home point
     */
    public Point getHome() {
        return home;
    }
    
    
}
