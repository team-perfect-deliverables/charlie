/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package charlie.test;

import charlie.card.Card;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author jacob
 */
public class CardTest
{
    @Test
    public void equalsTest()
    {
        Card a = new Card(1, Card.Suit.HEARTS);
        Card b = new Card(2, Card.Suit.HEARTS);
        Card c = new Card(1, Card.Suit.DIAMONDS);
        Card d = null;
        Card e = new Card(1, Card.Suit.HEARTS);
        
        assertFalse("", a.equals(b));   //Same suit, different rank   
        assertFalse("", b.equals(a));   //Different calling object
        assertFalse("", a.equals(c));   //Same rank, different rank
        assertFalse("", c.equals(a));   //Different calling object
        assertFalse("", d.equals(a));   //Null object
        
        assertTrue("", a.equals(a));    //Same object
        assertTrue("", a.equals(e));    //Same rank, suit
    }
}
