/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charlie.view;

import charlie.card.Hid;
import charlie.card.Card;
import java.util.List;

/**
 * Messages to the UI.
 * @author roncoleman125
 */
public interface IUi {
    abstract public void deal(Hid hid, Card card, int[] handValues);
    abstract public void turn(Hid hid);
    abstract public void bust(Hid hid);
    abstract public void win(Hid hid);
    abstract public void loose(Hid hid);
    abstract public void push(Hid hid);
    abstract public void blackjack(Hid hid);
    abstract public void charlie(Hid hid);
    abstract public void starting(List<Hid>hids,int shoeSize);
    abstract public void ending(int shoeSize);
    abstract public void shuffling();
}
