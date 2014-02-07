package charlie.test;

import charlie.plugins.Advisor;
import charlie.card.Hand;
import charlie.card.Card;
import charlie.card.Hid;
import charlie.dealer.Seat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

public class AdvisorTest
{

    private final int RUN_NUMBER = 5000000;
    private Advisor advisor;

    @Before
    public void setup()
    {
        advisor = new Advisor();
    }

    @Test
    public void fuzzTest()
    {
        Hand hand = null;
        Card upCard = null;

        try
        {
            for (int i = 0; i < RUN_NUMBER; i++)
            {
                hand = new Hand(new Hid(Seat.YOU));
                hand.hit(RandomCardFactory.getRandomCard());
                hand.hit(RandomCardFactory.getRandomCard());
                upCard = RandomCardFactory.getRandomCard();
                advisor.advise(hand, upCard);
                
                hand.hit(RandomCardFactory.getRandomCard());
                while(hand.getValue() < 21)
                {
                    advisor.advise(hand, upCard);
                    hand.hit(upCard);
                }
            }

        } catch (Exception e)
        {
            System.out.println("Hand: " + hand);
            System.out.println("Up Card: " + upCard);
            e.printStackTrace();
            fail("The method threw an exception: \n");
            
        }

        assertTrue(true);
    }
}
