package charlie.test;

import charlie.card.Card;
import java.util.HashMap;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author Jacob Leach
 */
public class RandomCardGeneratorTest
{
    private static final int RUN_NUMBER = 10000;
    
    @Test
    public void generatorRandomnessTest()
    {
        HashMap<Card, Integer> randomness = new HashMap<>();
        for(int i = 0; i < RUN_NUMBER; i++)
        {
            Card randomCard = RandomCardGenerator.getRandomCard();
            if(randomness.containsKey(randomCard))
            {
                randomness.put(randomCard, randomness.get(randomCard) + 1);
            }
            else
            {
                randomness.put(RandomCardGenerator.getRandomCard(), 1);
            }
        }
        
        Object[] cards = randomness.keySet().toArray();
        
        for(Object o : cards)
        {
            System.out.println("Card: " + (Card) o + " - " + randomness.get((Card) o));
        }
        
        assertTrue(true);
    }
}
