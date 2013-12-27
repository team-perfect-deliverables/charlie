/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charlie.card;

import java.io.Serializable;

/**
 *
 * @author roncoleman125
 */
public class Card implements Serializable {
    public enum Suit { SPADES, CLUBS, HEARTS, DIAMONDS };
    public final static int ACE = 1;
    public final static int JACK = 11;
    public final static int QUEEN = 12;
    public final static int KING = 13;
    
    protected final int rank;
    private final Suit suit;
    
    public Card(Card card) {
        this.suit = card.suit;
        this.rank = card.rank;
    }
    
    public Card(Integer rank,Suit suite) {
        this.suit = suite;
        this.rank = rank;
    }
    
    public Integer value() {
        if(isFace())
            return 10;
        
        return rank;
    }
    
    public boolean isAce() {
        return rank == 1;
    }
    
    public boolean isFace() {
        return rank >=11 && rank <= 13;
    }
    
    public String getFace() {
        if(!isFace() && !isAce())
            return rank + "";
        
        if(rank == ACE)
            return "A";
        
        else if(rank == JACK)
            return "J";
        
        else if(rank == QUEEN)
            return "Q";
        
        else if(rank == KING)
            return "K";
        
        else
            return "?";
    }
    
    @Override
    public String toString() {
        return suit.toString().charAt(0) + "" + getFace();
    }
}
