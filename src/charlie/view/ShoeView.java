/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charlie.view;

import charlie.card.Card;
import charlie.util.Point;

/**
 *
 * @author roncoleman125
 */
public class ShoeView {
    public final static int SHOE_X = 500;
    public final static int SHOE_Y = 0;
    
    public static ACard from(Card card) {
        return new ACard(card,new Point(SHOE_X, SHOE_Y));
    }
}
