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
    //For convenience in the hard coded array
    private final Play H = Play.HIT;
    private final Play D = Play.DOUBLE_DOWN;
    private final Play S = Play.STAY;
 
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
     * The value of the hand minus the ace(s) is the index for the first array.
     * The value of the dealers up card will be the index for the second array.
     *
     * The first value in each sub-array is null since the dealer can not have zero for an up card 
     */
    private final Play[][] byAces =  
                        {{},                                        //Null array to fix indexing
                        {null, H, H, H, H, H, H, H, H, H, H}, //1
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
    @Override
    public Play advise(Hand myHand, Card upCard)
    {
        //Set default return to SPLIT for testing
        //If it says SPLIT, it did not have any advice
        Play toReturn = Play.SPLIT;

        //Get the value of the hand with hard and soft aces
        int softValue = myHand.getValues()[Constant.HAND_SOFT_VALUE];
        int literalValue = myHand.getValues()[Constant.HAND_LITERAL_VALUE];
        int valueWithoutAces = literalValue - 1;        

        //Check if the hand has any aces
        boolean hasAce = softValue != literalValue;
        
        //If our hand has at least one ace, we have to handle it differently.
        if(hasAce)
        {
            //If we've already hit once, handle the aces by the highest value of the hand
            if(myHand.size() > 2)
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
            //If we haven't hit yet, handle it by the value of the non-ace card
            //If you have two aces, one gets treated as a 1
            else
            {
                toReturn = byAces[valueWithoutAces][upCard.value()]; 
            }   
        }
        //There are no aces
        else 
        {
            toReturn = byValue[literalValue][upCard.value()]; 
        } 

        return toReturn;
    }
    

}
