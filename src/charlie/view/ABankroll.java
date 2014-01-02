/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charlie.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

/**
 *
 * @author Ron Coleman
 */
public class ABankroll {
    protected Double amount;
    protected Font font = new Font("Arial", Font.BOLD, 18);
    private final int x;
    private final int y;
    
    public ABankroll(int x, int y, Double amt) {
        this.x = x;
        this.y = y;
        this.amount = amt;
    }
    
    public void increase(Double amt) {
        this.amount += amt;
    }
    
    public void decrease(Double amt) {
        this.amount -= amt;
    }    

    public void setAmount(Double amount) {
        this.amount = amount;
    }
    
    public void render(Graphics2D g) {
        g.setFont(font);
        String remark = "";
        if(amount >= 100)
            g.setColor(Color.WHITE);
        else if(amount > 50 && amount < 100) {
            g.setColor(Color.YELLOW);
            remark = " !";
        }
        else {
            g.setColor(Color.CYAN);
            remark = " !!!!!";
        }
            
        g.drawString("Bankroll: "+amount+remark, x, y);        
    }
}
