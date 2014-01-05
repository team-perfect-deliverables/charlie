/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charlie.view;

import charlie.audio.Effect;
import charlie.audio.SoundFactory;
import charlie.view.sprite.Chip;
import charlie.view.sprite.BetAmtSprite;
import charlie.view.sprite.Button;
import charlie.util.Constant;
import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.ImageIcon;

/**
 *
 * @author roncoleman125
 */
public class AMoneyManager {
    public final static int HOME_X = 210;
    public final static int HOME_Y = 355;
    public final static int BET_HOME_X = 255;
    public final static int BET_HOME_Y = 130;    
    public final static int PLACE_HOME_X = BET_HOME_X + BetAmtSprite.DIAMETER + 10;
    public final static int PLACE_HOME_Y = BET_HOME_Y + BetAmtSprite.DIAMETER / 4;
    
    protected final int INDEX_5 = 2;
    protected final int INDEX_25 = 1;
    protected final int INDEX_100 = 0;
    protected Font font = new Font("Arial", Font.BOLD, 18);
    protected BasicStroke stroke = new BasicStroke(3);    
    protected Integer[] amounts = { 100, 25, 5 };
    protected Random ran = new Random();
    
    protected final static String[] UP_FILES =
        {"chip-100-1.png","chip-25-1.png","chip-5-1.png"};
    
    protected final static String[] DOWN_FILES =
        {"chip-100-2.png","chip-25-2.png","chip-5-2.png"};
    
    protected List<Button> buttons = new ArrayList<>();
    
    protected BetAmtSprite betAmt = new BetAmtSprite(BET_HOME_X,BET_HOME_Y,0);
    protected List<Chip> chips = new ArrayList<>();
    private final int width;
    protected ABankroll bankroll;
    protected Integer xDeposit = 0;
    protected boolean dubble = false;
    
    public AMoneyManager() {
        ImageIcon icon = new ImageIcon(Constant.DIR_IMGS+UP_FILES[0]);

        Image img = icon.getImage();
        this.width = img.getWidth(null);
//        int height = img.getHeight(null);
        
        int xoff =0;
        for(int i=0; i < amounts.length; i++) {
            Image up = new ImageIcon(Constant.DIR_IMGS+UP_FILES[i]).getImage();
            Image down = new ImageIcon(Constant.DIR_IMGS+DOWN_FILES[i]).getImage();
            buttons.add(new Button(up,down,HOME_X+xoff,HOME_Y));
            xoff += (width + 7);
        }
        
        xDeposit = HOME_X + xoff + 5;
        bankroll = new ABankroll(xDeposit,HOME_Y+5,0.0);
    }
    
    public Integer getAmount() {
        return this.betAmt.getAmt();
    }
    
    public void dubble() {
        // Can double only once
        if(dubble)
            return;
        
        // Copy in the new chips
        int sz = chips.size();
        int x = chips.get(sz-1).getX();
        int y = chips.get(sz-1).getY() + ran.nextInt(5)-5;
        
        for(int n=0; n < sz; n++) {
                int placeX = x + (n+1) * width/3 + ran.nextInt(10)-10;
                int placeY = y + ran.nextInt(5)-5;
                
                Chip chip = new Chip(chips.get(n));
                
                chip.setX(placeX);
                chip.setY(placeY);
                
                chips.add(chip);                        
        }
        
        this.betAmt.dubble();
        
        dubble = true;
    }
    
    public void undubble() {
        if(!this.dubble)
            return;

        // Get a new chip set
        List<Chip> newChips = new ArrayList<>();
       
        this.betAmt.zero();
        
        int sz = chips.size();
        
        // Transfer half of old chips
        for(int i=0; i < sz / 2; i++) {
            newChips.add(chips.get(i));
            
            betAmt.increase(chips.get(i).getAmt());
        }
        
        // Make new the current chip set
        chips = newChips;
        
        // Enable double down
        dubble = false;
    }
    
    public void increase(Double amt) {
        bankroll.increase(amt);
    }
    
    public void increase(Chip chip) {
        betAmt.increase(chip.getAmt());
    }
    
    public void decrease(Double amt) {
        bankroll.decrease(amt);
    }    

    public void setBankroll(Double amt) {
        bankroll.setAmount(amt);
    }    
    
    public void render(Graphics2D g) {
        for(int i=0; i < buttons.size(); i++) {
            Button button = buttons.get(i);
            button.render(g);
        }
        
        for(int i=0; i < chips.size(); i++) {
            Chip chip = chips.get(i);
            chip.render(g);
        }
        
        this.betAmt.render(g);
        this.bankroll.render(g);
    }
    
    public void click(int x, int y) {
        for(int i=0; i < buttons.size(); i++) {
            Button button = buttons.get(i);
            if(button.isReady() && button.isPressed(x,y)) {
                int n = chips.size();
                
                int placeX = PLACE_HOME_X + n * width/3 + ran.nextInt(10)-10;
                
                int placeY = PLACE_HOME_Y + ran.nextInt(5)-5;
                
                Chip chip = new Chip(button.getImage(),placeX,placeY,amounts[i]);
                
                chips.add(chip);
                
                betAmt.increase(amounts[i]);
                
                SoundFactory.play(Effect.CHIPS_IN);
            }
        }
        
        // Check for bet reset
        if(this.betAmt.isPressed(x, y)) {
            this.betAmt.zero();
            chips.clear();
            SoundFactory.play(Effect.CHIPS_OUT);
        }
    }
    
    public void unclick() {
        for(int i=0; i < buttons.size(); i++) {
            Button button = buttons.get(i);
            button.release();
        }        
    }
}
