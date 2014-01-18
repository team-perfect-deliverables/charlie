/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charlie.plugin;

import charlie.card.Hand;

/**
 *
 * @author roncoleman125
 */
public interface IAdvisor {
    public String advise(Hand myHand,Hand dealerHand);
}
