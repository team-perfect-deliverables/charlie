/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charlie.plugin;

import charlie.card.Hid;
import charlie.view.sprite.Chip;
import java.awt.Graphics2D;

/**
 *
 * @author roncoleman125
 */
public interface ISideView {
    /**
     * Handles clicks in this region of the UI.
     * @param x X coordinate
     * @param y Y coordinate
     */
    public void click(int x, int y);
    
    /**
     * Sets the hand id for the side bet.
     * @param hid Hand id
     */
    public void setHid(Hid hid);
    
    /**
     * Resets the side bet on the UI. Side bets don't "stick" like
     * regular bets but are cleared after each game. Thus, the player
     * must continually place a side bet if they want to make one.
     */
    public void reset();
    
    /**
     * Sets side bet amount unit amount. Players can make
     * side bets in units of this chip only.
     * @param chip Chip
     */
    public void setUnit(Chip chip);
    
    /**
     * Updates the side bet
     */
    public void update();
    
    /**
     * Renders the side bet.
     * @param g Graphics context
     */
    public void render(Graphics2D g);
}
