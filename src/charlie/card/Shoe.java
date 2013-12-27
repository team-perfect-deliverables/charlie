/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charlie.card;

import charlie.card.Card.Suit;
import charlie.util.Constant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 *
 * @author roncoleman125
 */
public final class Shoe  {
    protected List<Card> cards = new ArrayList<>();
    protected Integer index = 0;
    protected int burnIndex = Integer.MAX_VALUE;
    protected Random ran;
    
    public Shoe() {
        ran = new Random(System.currentTimeMillis());
        
        build();
        
        shuffle();
    }
    
    public Shoe(int testno) {
        switch(testno) {
            case 0:
                ran = new Random(0);
                build();
                shuffle();
                break;
                
            case 1:
                cards.add(new Card(Card.QUEEN, Card.Suit.HEARTS));
                cards.add(new Card(6, Card.Suit.CLUBS));
                cards.add(new Card(Card.ACE, Card.Suit.SPADES));
                cards.add(new Card(3, Card.Suit.SPADES));
                cards.add(new Card(2, Card.Suit.SPADES));
                cards.add(new Card(4, Card.Suit.DIAMONDS));
                cards.add(new Card(6, Card.Suit.HEARTS));
                cards.add(new Card(5, Card.Suit.CLUBS));
                break;
            case 2:
                cards.add(new Card(3, Card.Suit.SPADES));
                cards.add(new Card(2, Card.Suit.SPADES));
                cards.add(new Card(4, Card.Suit.DIAMONDS));
                cards.add(new Card(6, Card.Suit.HEARTS));
                cards.add(new Card(5, Card.Suit.CLUBS));  
                cards.add(new Card(Card.QUEEN, Card.Suit.HEARTS));
                cards.add(new Card(6, Card.Suit.CLUBS));
                cards.add(new Card(Card.ACE, Card.Suit.SPADES));                
                break;
        }
    }
    
    protected void build() {
        for(int deckno=0; deckno < Constant.NUM_DECKS; deckno++) {
            for(int rank=1; rank <= 13; rank++)
                for(Suit suit: Suit.values())
                    cards.add(new Card(rank,suit));
        }        
    }
    
    public void shuffle() {
        Collections.shuffle(cards,ran);
        
        index = 0;
                
        burnIndex = cards.size() - ran.nextInt(52);
    }
    
    public Card next() {
        if(index >= cards.size())
            return null;
        
        return cards.get(index++);
    }
    
    public boolean hasNext() {
        return index < cards.size();
    }
    
    public boolean shuffleNeeded() {
        return burnIndex < index;
    }
}
