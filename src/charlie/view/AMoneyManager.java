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

import charlie.audio.Effect;
import charlie.audio.SoundFactory;
import charlie.view.sprite.Chip;
import charlie.view.sprite.AtStakeSprite;
import charlie.view.sprite.ChipButton;
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
 * This class implements money indicator for a player including chip buttons and
 * chips and money at stake on the table.
 * @author Ron Coleman
 */
public class AMoneyManager {
    // Home base for the money indicators
    public final static int HOME_X = 210;
    public final static int HOME_Y = 355;
    
    // Stake home (ie, the bet amount actually at stake)
    public final static int STAKE_HOME_X = 255;
    public final static int STAKE_HOME_Y = 130; 
    
    // Corresponding chips equal to the stake
    public final static int PLACE_HOME_X = STAKE_HOME_X + AtStakeSprite.DIAMETER + 10;
    public final static int PLACE_HOME_Y = STAKE_HOME_Y + AtStakeSprite.DIAMETER / 4;
    
    // Amount chip-to-dollar amount indices
    protected final int INDEX_5 = 2;
    protected final int INDEX_25 = 1;
    protected final int INDEX_100 = 0;
    protected Integer[] amounts = { 100, 25, 5 };
    protected Font font = new Font("Arial", Font.BOLD, 18);
    protected BasicStroke stroke = new BasicStroke(3);    
    protected Random ran = new Random();
    
    protected final static String[] UP_FILES =
        {"chip-100-1.png","chip-25-1.png","chip-5-1.png"};
    
    protected final static String[] DOWN_FILES =
        {"chip-100-2.png","chip-25-2.png","chip-5-2.png"};
    
    protected List<ChipButton> buttons = new ArrayList<>();
    
    protected AtStakeSprite wager = new AtStakeSprite(STAKE_HOME_X,STAKE_HOME_Y,0);
    protected List<Chip> chips = new ArrayList<>();
    private final int width;
    protected ABankroll bankroll;
    protected Integer xDeposit = 0;
    protected boolean dubble = false;
    
    /**
     * Constructor
     */
    public AMoneyManager() {
        ImageIcon icon = new ImageIcon(Constant.DIR_IMGS+UP_FILES[0]);

        Image img = icon.getImage();
        this.width = img.getWidth(null);
//        int height = img.getHeight(null);
        
        int xoff =0;
        for(int i=0; i < amounts.length; i++) {
            Image up = new ImageIcon(Constant.DIR_IMGS+UP_FILES[i]).getImage();
            Image down = new ImageIcon(Constant.DIR_IMGS+DOWN_FILES[i]).getImage();
            buttons.add(new ChipButton(amounts[i],up,down,HOME_X+xoff,HOME_Y));
            xoff += (width + 7);
        }
        
        xDeposit = HOME_X + xoff + 5;
        bankroll = new ABankroll(xDeposit,HOME_Y+25,0.0);
    }
    
    /**
     * Gets the wager.
     * @return 
     */
    public Integer getWager() {
        return this.wager.getAmt();
    }
    
    /**
     * Doubles the wager.
     */
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
        
        this.wager.dubble();
        
        dubble = true;
    }
    
    /**
     * Undoubles the wager in the case of double down.
     */
    public void undubble() {
        if(!this.dubble)
            return;

        // Get a new chip set
        List<Chip> newChips = new ArrayList<>();
       
        this.wager.zero();
        
        int sz = chips.size();
        
        // Transfer half of old chips
        for(int i=0; i < sz / 2; i++) {
            newChips.add(chips.get(i));
            
            wager.increase(chips.get(i).getAmt());
        }
        
        // Make new the current chip set
        chips = newChips;
        
        // Enable double down
        dubble = false;
    }
    
    /**
     * Increases bankroll from a win.
     * @param amt Amount
     */
    public void increase(Double amt) {
        bankroll.increase(amt);
    }
    
    /**
     * Increases bankroll with a chip earning.
     * @param chip Chip
     */
    public void increase(Chip chip) {
        wager.increase(chip.getAmt());
    }
    
    /**
     * Decreases bankroll from a loss.
     * @param amt Amount
     */
    public void decrease(Double amt) {
        bankroll.decrease(amt);
    }    

    /**
     * Gets the bankroll.
     * @return Bankroll
     */
    public double getBankroll() {
        return bankroll.getAmount();
    }

    /**
     * Sets the bankroll
     * @param amt Amount
     */
    public void setBankroll(Double amt) {
        bankroll.setAmount(amt);
    }    
    
    /**
     * Renders the money on table.
     * @param g 
     */
    public void render(Graphics2D g) {
        for(int i=0; i < buttons.size(); i++) {
            ChipButton button = buttons.get(i);
            button.render(g);
        }
        
        for(int i=0; i < chips.size(); i++) {
            Chip chip = chips.get(i);
            chip.render(g);
        }
        
        this.wager.render(g);
        this.bankroll.render(g);
    }
    
    /**
     * Handles if the manager is clicked.
     * @param x Mouse x
     * @param y Mouse y
     */
    public void click(int x, int y) {
        for(int i=0; i < buttons.size(); i++) {
            ChipButton button = buttons.get(i);
            if(button.isReady() && button.isPressed(x,y)) {
                int n = chips.size();
                
                int placeX = PLACE_HOME_X + n * width/3 + ran.nextInt(10)-10;
                
                int placeY = PLACE_HOME_Y + ran.nextInt(5)-5;
                
                Chip chip = new Chip(button.getImage(),placeX,placeY,amounts[i]);
                
                chips.add(chip);
                
                wager.increase(amounts[i]);
                
                SoundFactory.play(Effect.CHIPS_IN);
            }
        }
        
        // Check for wager reset
        if(this.wager.isPressed(x, y)) {
            this.wager.zero();
            
            chips.clear();
            
            SoundFactory.play(Effect.CHIPS_OUT);
        }
    }
    
    /**
     * Handles unclicking mouse.
     */
    public void unclick() {
        for(int i=0; i < buttons.size(); i++) {
            ChipButton button = buttons.get(i);
            button.release();
        }        
    }
    
    /**
     * Gets the chip buttons.
     * @return Buttons
     */
    public List<ChipButton> getButtons() {
        return this.buttons;
    }
}
