package charlie.test;

import charlie.card.Card;
import java.util.ArrayList;
import java.util.HashMap;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author Jacob Leach
 */
public class RandomCardGeneratorTest
{

    private static final int RUN_NUMBER = 100000;

    //This should return true, most of the time. It's random so it could not sometimes.
    @Test
    public void generatorRandomnessTest()
    {
        HashMap<Card, Integer> randomness = new HashMap<>();
        ArrayList<Card> cards = new ArrayList<>();

        for (int i = 0; i < RUN_NUMBER; i++)
        {
            Card randomCard = RandomCardGenerator.getRandomCard();

            if (!(randomness.containsKey(randomCard)))
            {
                randomness.put(randomCard, 1);
                cards.add(randomCard);
            } else
            {
                randomness.put(randomCard, randomness.get(randomCard) + 1);
            }
        }

        Card first = RandomCardGenerator.getRandomCard();
        System.out.println(randomness.get(first));
        for (Card i : cards)
        {
            System.out.println((randomness.get(first) / 1.2) + " < " + randomness.get(i) + " < " + (randomness.get(first) * 1.2));
            assertTrue((randomness.get(first) / 1.2) < randomness.get(i) && randomness.get(i) < (randomness.get(first) * 1.2));
        }
    }
}
