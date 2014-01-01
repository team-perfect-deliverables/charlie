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
package charlie.card.shoe;

import charlie.card.Card;
import charlie.card.Card.Suit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * This class implements a six deck shoe, the standard in many houses.
 * This class is also the base class for all other shoes.
 * @author Ron Coleman
 */
public class Shoe  {
    protected Integer numDecks = 6;
    protected List<Card> cards = new ArrayList<>();
    protected Integer index = 0;
    protected int burnIndex = Integer.MAX_VALUE;
    protected Random ran;
    
    /**
     * Constructor
     */
    public Shoe() {

    }
    
    /**
     * Constructor
     * @param numDecks Number of decks
     */
    public Shoe(int numDecks) {
        this.numDecks = numDecks;
    }
    
    /**
     * Initializes the shoe.
     */
    public void init() {
        ran = new Random(System.currentTimeMillis());
        
        load();
        
        shuffle();        
    }
       
    /**
     * Loads the shoe with cards.
     */
    protected final void load() {
        cards.clear();
        for(int deckno=0; deckno < this.numDecks; deckno++) {
            for(int rank=1; rank <= 13; rank++)
                for(Suit suit: Suit.values())
                    cards.add(new Card(rank,suit));
        }        
    }
    
    /**
     * Shuffles cards in the shoe.
     */
    public final void shuffle() {
        Collections.shuffle(cards,ran);
        
        index = 0;
                
        burnIndex = cards.size() - ran.nextInt(52);
    }
    
    /**
     * Gets the next card.
     * @return A card, if there is one
     */
    public Card next() {
        if(index >= cards.size())
            return null;
        
        return cards.get(index++);
    }
    
    /**
     * Tests if shoe has another card.
     * @return True if shoe has another card, false otherwise.
     */
    public boolean hasNext() {
        return index < cards.size();
    }
    
    /**
     * Tests if shoe needs shuffling.
     * @return True if we've reach the burn card, false otherwise.
     */
    public boolean shuffleNeeded() {
        return burnIndex < index;
    }
    
    /**
     * Gets number of cards still in the shoe.
     * @return Number cards in shoe
     */
    public int size() {
        return cards.size() - index;
    }
    
    /**
     * Converts shoe to string.
     * @return String
     */
    @Override
    public String toString() {
        String s = "";
        for(int i=index, count=0; i < cards.size() && count < 15; i++, count++)
            s += cards.get(i) + " ";
        return this.getClass().getName()+": "+s;
    }
}
