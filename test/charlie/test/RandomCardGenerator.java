package charlie.test;

import charlie.card.Card;
import charlie.card.Card.Suit;
import java.util.Random;

public class RandomCardGenerator
{

    private static Random random = new Random(System.currentTimeMillis());

    /**
     *  Creates and returns a random Card.
     * 
     * @return A random card based on a random seed or the previously set seed (if set manually).
     */
    public static Card getRandomCard()
    {
        Suit suit = Suit.values()[random.nextInt(4)];
        return new Card(random.nextInt(13) + 1, suit);
    }

    /**
     * Creates and returns a random Card using the given seed. 
     * 
     * The seed is saved and used for all subsequent generation until this method is called again.
     * 
     * @param seed The seed to be used for this and future random Cards.
     * @return A random card based on the given seed.
     */
    public static Card getRandomCard(long seed)
    {
        random = new Random(seed);
        return getRandomCard();
    }
}
