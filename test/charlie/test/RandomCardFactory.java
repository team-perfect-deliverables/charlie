package charlie.test;

import charlie.card.Card;
import charlie.card.Card;
import charlie.card.Card.Suit;
import java.util.Random;

public class RandomCardFactory
{
    private static long seed;

    public static Card getRandomCard()
    {
        Random random = new Random(System.currentTimeMillis());
        int getSuit = random.nextInt(4);
        Suit suit;
        if(getSuit == 0)
        {
            suit = Card.Suit.SPADES;
        }
        else if(getSuit == 1)
        {
            suit = Card.Suit.HEARTS;
        }
        else if(getSuit == 1)
        {
            suit = Card.Suit.CLUBS;
        }
        else 
        {
            suit = Card.Suit.DIAMONDS;
        }
        return new Card(random.nextInt(13) + 1, suit);
    }
}
