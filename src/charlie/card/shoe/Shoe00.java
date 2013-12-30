/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charlie.card.shoe;

import java.util.Random;

/**
 *
 * @author roncoleman125
 */
public class Shoe00 extends Shoe {
    @Override
    public void init() {
        ran = new Random(0);
        load();
        shuffle();
    }
}
