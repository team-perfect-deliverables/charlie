/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charlie.plugin;

import charlie.card.Card;

/**
 *
 * @author roncoleman125
 */
public interface IShoe {
    public void init();
    public boolean shuffleNeeded();
    public void shuffle();
    public Card next();
    public int size();
}
