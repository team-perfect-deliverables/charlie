/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charlie.view.sprite;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

/**
 *
 * @author roncoleman125
 */
public class BetAmtSprite {
    public final static int HOME_X = 225;
    public final static int HOME_Y = 130;
    public final static int DIAMETER = 50;
    
    protected Integer amt;
    protected Font font = new Font("Arial", Font.BOLD, 18);
    protected Color fontColor = Color.YELLOW;
    protected BasicStroke stroke = new BasicStroke(3);
    
    public BetAmtSprite(Integer amt) {
        this.amt = amt;
    }
    
    public void render(Graphics2D g) {
        g.setColor(Color.BLACK);
                
        g.setStroke(stroke);
        g.drawOval(HOME_X, HOME_Y, DIAMETER, DIAMETER);
        
        g.setColor(fontColor);
        g.setFont(font);

        String text = amt + "";
        FontMetrics fm = g.getFontMetrics(font);

        int x = HOME_X + DIAMETER/2 - fm.charsWidth(text.toCharArray(), 0, text.length()) / 2;
        int y = HOME_Y + DIAMETER/2 + fm.getHeight() / 4;
        
        g.drawString(amt+"", x, y);
    }

    public Integer getAmt() {
        return amt;
    }

    public void setAmt(Integer amt) {
        this.amt = amt;
    }
    
    public void increase(Integer amt) {
        this.amt += amt;
    }
    
    public void clear() {
        amt = 0;
    }
}
