/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package charlie.bs.section2;

import charlie.bs.section1.*;
import charlie.card.Card;
import charlie.card.Hand;
import charlie.card.Hid;
import charlie.dealer.Seat;
import charlie.advisor.Advisor;
import charlie.util.Play;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author travis
 */
public class Test01_5_7 {
    
    private Advisor advisor;
    
    @Before
    public void setUp() {
        advisor = new Advisor();
    }
    
    @Test
    public void test() {
        Hand hand = new Hand(new Hid(Seat.YOU));
        Card upCard = new Card(Card.ACE, Card.Suit.CLUBS);
        hand.hit(new Card(9, Card.Suit.CLUBS));
        hand.hit(new Card(3, Card.Suit.CLUBS));
        
        assertEquals(advisor.advise(hand, upCard), Play.HIT);
    }
}
