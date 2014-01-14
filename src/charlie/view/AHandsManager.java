/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charlie.view;

import charlie.card.Card;
import charlie.util.Constant;
import charlie.util.Point;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.ImageIcon;

/**
 *
 * @author roncoleman125
 */
public class AHandsManager { 
    private List<AHand> hands = new ArrayList<>();
    private int handInset = ACard.cardWidth;
    
    private final Point center;
    
    private final String name;
    
    public void clear() {
        hands.clear();
    }
    
    public AHandsManager(String name, Point center) {
        this.name = name;
        this.center = center;
    }
    
    public boolean isReady() {
        for(int i=0; i < hands.size(); i++) {
            AHand ahand = hands.get(i);
            
            if(!ahand.isReady())
                return false;
        }
        
        return true; 
    }
    
    public void update() {
        for(int i=0; i < hands.size(); i++) {
            AHand ahand = hands.get(i);
            ahand.update();
        }
    }
    
    public void render(Graphics2D g) {
        for(int i=0; i < hands.size(); i++) {
            AHand ahand = hands.get(i);
            ahand.render(g);
        }
//        for(AHand hand: this)
//            hand.render(g);
    }
    
    public boolean add(AHand hand) {
        // Add this new hand
        boolean tf = hands.add(hand);
        
        hand.setName(name);
        
        // If this is the only hand, then its home is the player center
        // otherwise we have to spread the hands.
        if(hands.size() == 1)
            hand.setHome(center);
        else
            spread();
        
        return tf;
    }
    
//    public ACard split(int k) {
//        if(k < 0 || k >= hands.size())
//            return null;
//        
//        // Split the hand
//        AHand hand1 = hands.get(k);
//        
//        ACard card = hand1.split();
//        
//        // Create new hand with the card and hit the hand to the animator.
//        AHand hand2 = new AHand(card);       
//        this.hit(hand2);
//        
//        // Spread out the hands
//        spread();
//        
//        return card;
//    }
    
    protected void spread() { 
        // Select the offset relative to the current unsplit hand.
        // Note: to keep the cards even laid out along the player's center
        // the offset size depends on the rank of hands.
        int offset = hands.size() % 2 == 0 ? ACard.cardWidth : ACard.cardWidth / 2;
        
        // Move all the hands further to the left
        int x = hands.get(0).getHome().getX() - offset;
        int y = hands.get(0).getHome().getY();
        
        for(AHand hand: hands) {
            Point newhome = new Point(x,y);
            
            hand.setHome(newhome);
            
            x += (ACard.cardWidth + handInset);
        }
    }
    
    public void settle() {
        for(AHand hand: hands)
            hand.settle();
    }
}
