/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charlie.message.view.to;

import charlie.message.Message;

/**
 *
 * @author roncoleman125
 */
public class Ending extends Message {
    private final Double bankroll;
    
    public Ending(Double bankroll) {
        this.bankroll = bankroll;
    }

    public Double getBankroll() {
        return bankroll;
    }
}
