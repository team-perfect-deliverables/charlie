/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charlie.view;

import charlie.card.Hid;
import static charlie.view.AHand.HOME_OFFSET_X;
import charlie.util.Constant;
import charlie.util.Point;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

/**
 * Dealer hand only uses the soft value.
 * @author Ron Coleman, Ph.D.
 */
public class ADealerHand extends AHand {
    public ADealerHand(Hid hid) {
        super(hid);
    }
    
    @Override
    public boolean hit(ACard card) {
        // Move over other cards
        int cardWidth = AHandsManager.getCardWidth();

        int sz = cards.size();

        for (int i = 0; i < sz; i++) {
            ACard acard = cards.get(i);

            Point ahome = acard.getHome();

            int x = ahome.getX() - (cardWidth / 2 + HOME_OFFSET_X / 2);
            int y = ahome.getY();

            Point newHome = new Point(x, y);

            acard.setHome(newHome);
        }

        int x;

        if (sz == 0) {
            x = home.getX();
        } else {
            int xLastCard = cards.get(sz - 1).getHome().getX();

            x = xLastCard + cardWidth + HOME_OFFSET_X / 2;
        }

        Point sweetHome = new Point(x, home.getY());

        return cards.add(new ACard(card, sweetHome));
    }
    
    @Override
    protected void renderState(Graphics2D g,String text) {
        if(cards.isEmpty())
            return;

        FontMetrics fm = g.getFontMetrics(stateFont);
        
        int x = cards.get(0).getX() + getPileWidth() / 2 - fm.charsWidth(text.toCharArray(), 0, text.length()) / 2;
        int y = AHandsManager.getCardHeight() + fm.getHeight();
        
        g.setColor(stateColor);
        g.setFont(stateFont);

        g.drawString(text, x, y); 
    }
    
    @Override
    public Integer getPileWidth() {
        if (cards.isEmpty()) {
            return 0;
        }

        int sz = cards.size();
        
        int cardWidth = AHandsManager.getCardWidth();
        
        int pw = cardWidth * sz + (sz - 1) * HOME_OFFSET_X / 2;
        
        return pw;
    }
    
    @Override
    public Integer getPileHeight() {
        if (cards.isEmpty()) {
            return 0;
        }

        return AHandsManager.getCardHeight();
    }
    
    @Override
    protected String getText() {
        int value = values[Constant.HAND_SOFT_VALUE] <= 21 ?
                values[Constant.HAND_SOFT_VALUE] :
                values[Constant.HAND_LITERAL_VALUE];
        
        String text = name;

        if(value != 0) {
            if(cards.size() == 2 && value == 21)
                text += ": Blackjack !";
            else
                text += ": "+value;
        }
        
        return text;
    }
}
