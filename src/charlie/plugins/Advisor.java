/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package charlie.plugins;

import charlie.util.Constant;
import charlie.card.Card;
import charlie.card.Hand;
import charlie.plugin.IAdvisor;
import charlie.util.Play;

/**
 *
 * @author jacob
 */
public class Advisor implements IAdvisor
{
    private final Play H = Play.HIT;
    private final Play D = Play.DOUBLE_DOWN;
    private final Play S = Play.STAY;
 
    /*
     * This will be accessed by using the value of the hand as the index for the first array.
     * The value of the dealers up card will be the index for the second array.
     *
     * The first 9 values are blank because:
     * A) To make the indexes match the values (there is no zero value)
     * B) The first 8 values are the same so they are handled in an if statement for convenience
     *
     * The first value in each sub-array is null since the dealer can not have zero for an up card 
     */
    private final Play[][] byValue =  {{}, {}, {}, {}, {}, {}, {}, {}, {},  //9 blank values (see above comment)
                        {null, H, D, D, D, D, H, H, H, H, H}, //9
                        {null, D, D, D, D, D, D, D, D, H, H}, //10
                        {null, D, D, D, D, D, D, D, D, D, H}, //11
                        {null, H, H, S, S, S, H, H, H, H, H}, //12
                        {null, S, S, S, S, S, H, H, H, H, H}, //13
                        {null, S, S, S, S, S, H, H, H, H, H}, //14
                        {null, S, S, S, S, S, H, H, H, H, H}, //15
                        {null, S, S, S, S, S, H, H, H, H, H}  //16
                        };

    @Override
    public Play advise(Hand myHand, Card upCard)
    {
        //Set default return
        Play toReturn = Play.STAY;

        //Get the value of the hand with hard and soft aces
        int softValue = myHand.getValues()[Constant.HAND_SOFT_VALUE];
        int literalValue = myHand.getValues()[Constant.HAND_LITERAL_VALUE];
        
        //Check if the hand has any aces
        boolean hasAce = softValue != literalValue;
        
        //If our hand has at least one ace, we have to handle it differently.
        if(hasAce)
        {

        }
        //There are no aces
        else 
        {
            //If value is less than 8, we always hit
            if(literalValue <= 8)
            {
                toReturn = Play.HIT;
            }
            else
            {
                
            }
        }

        return toReturn;
    }
    

}
