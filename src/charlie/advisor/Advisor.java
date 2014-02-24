package charlie.advisor;

import charlie.util.Constant;
import charlie.card.Card;
import charlie.card.Hand;
import charlie.plugin.IAdvisor;
import charlie.util.Play;

/**
 * An advisor plugin for "Charlie" that advises the correct way to play. 
 *
 * @author Jacob Leach
 */
public class Advisor implements IAdvisor
{   
    //For convenience in the hard coded array
    private final Play H = Play.HIT;
    private final Play D = Play.DOUBLE_DOWN;
    private final Play S = Play.STAY;
    private final Play P = Play.SPLIT;
 
    /*
     * This will be accessed by using the value of the hand as the index for the first array.
     * The value of the dealers up card will be the index for the second array.
     *
     * The first value in each sub-array is null since the dealer can not have zero for an up card 
     */
    private final Play[][] byValue =  
                        {{},                                  //Null array to fix indexing
                        {null, H, H, H, H, H, H, H, H, H, H}, //1
                        {null, H, H, H, H, H, H, H, H, H, H}, //2
                        {null, H, H, H, H, H, H, H, H, H, H}, //3
                        {null, H, H, H, H, H, H, H, H, H, H}, //4
                        {null, H, H, H, H, H, H, H, H, H, H}, //5
                        {null, H, H, H, H, H, H, H, H, H, H}, //6
                        {null, H, H, H, H, H, H, H, H, H, H}, //7
                        {null, H, H, H, H, H, H, H, H, H, H}, //8
                        {null, H, H, D, D, D, D, H, H, H, H}, //9
                        {null, H, D, D, D, D, D, D, D, D, H}, //10
                        {null, H, D, D, D, D, D, D, D, D, D}, //11
                        {null, H, H, H, S, S, S, H, H, H, H}, //12
                        {null, H, S, S, S, S, S, H, H, H, H}, //13
                        {null, H, S, S, S, S, S, H, H, H, H}, //14
                        {null, H, S, S, S, S, S, H, H, H, H}, //15
                        {null, H, S, S, S, S, S, H, H, H, H}, //16
                        {null, S, S, S, S, S, S, S, S, S, S}, //17
                        {null, S, S, S, S, S, S, S, S, S, S}, //18
                        {null, S, S, S, S, S, S, S, S, S, S}, //19
                        {null, S, S, S, S, S, S, S, S, S, S}  //20
                        };

    /*
     * The value of the hand minus the ace (or minus as value one with a pair) is the index for the first array.
     * The value of the dealers up card will be the index for the second array.
     *
     * The first value in each sub-array is null since the dealer can not have zero for an up card 
     */
    private final Play[][] byAces =  
                        {{},                                  //Null array to fix indexing
                        {null, P, P, P, P, P, P, P, P, P, P}, //1
                        {null, H, H, H, H, D, D, H, H, H, H}, //2
                        {null, H, H, H, H, D, D, H, H, H, H}, //3
                        {null, H, H, H, D, D, D, H, H, H, H}, //4
                        {null, H, H, H, D, D, D, H, H, H, H}, //5
                        {null, H, H, D, D, D, D, H, H, H, H}, //6
                        {null, H, S, D, D, D, D, S, S, H, H}, //7
                        {null, S, S, S, S, S, S, S, S, S, S}, //8
                        {null, S, S, S, S, S, S, S, S, S, S}, //9
                        {null, S, S, S, S, S, S, S, S, S, S}, //10
                        };
    
    private final Play[][] byPair = 
                        {{},                                   //Null array to fix indexing 
                        {},                                  //If we have a one, that's an ace
                        {null, P, P, P, P, P, P, H, H, H, H}, //2
                        {null, P, P, P, P, P, P, H, H, H, H}, //3
                        {null, H, H, H, P, P, H, H, H, H, H}, //4
                        {null, D, D, D, D, D, D, D, D, H, H}, //5
                        {null, P, P, P, P, P, H, H, H, H, H}, //6
                        {null, P, P, P, P, P, P, H, H, H, H}, //7
                        {null, P, P, P, P, P, P, P, P, P, P}, //8
                        {null, P, P, P, P, P, S, P, P, S, S}, //9
                        {null, S, S, S, S, S, S, S, S, S, S}  //10
                        };
    @Override
    public Play advise(Hand myHand, Card upCard)
    {
        //Set default return to NONE for testing
        Play toReturn = Play.NONE;

        //Get the value of the hand with hard and soft aces
        int softValue = myHand.getValues()[Constant.HAND_SOFT_VALUE];
        int literalValue = myHand.getValues()[Constant.HAND_LITERAL_VALUE];
        int valueWithoutAces = literalValue - 1;        

        //Check if the hand has any aces
        boolean hasAce = softValue != literalValue;

        //Check if the hand has a pair
        boolean isPair = myHand.getCard(0).toString().equals(myHand.getCard(1).toString()); 
        
        //If this is our initial two cards
        if(myHand.size() == 2)
        {
            if(hasAce)
            {
                toReturn = byAces[valueWithoutAces][upCard.value()]; 
            }
            else if(isPair)
            {
                toReturn = byPair[myHand.getCard(0).value()][upCard.value()];
            }
            else
            {
                toReturn = byValue[literalValue][upCard.value()]; 
            }
        }
        //If we have already hit
        else
        {
            if(softValue <= 21) 
            {
                toReturn = byValue[softValue][upCard.value()];
            }
            else
            {
                toReturn = byValue[literalValue][upCard.value()];
            }
        }

        return toReturn;
    }
}
