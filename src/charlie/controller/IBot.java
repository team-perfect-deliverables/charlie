/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charlie.controller;

import charlie.card.Hand;

/**
 *
 * @author roncoleman125
 */
public interface IBot extends IPlayer {
    public Hand getHand();
    public void setDealer(Dealer dealer);
    public void sit(Seat seat);
}
