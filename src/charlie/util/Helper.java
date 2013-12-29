/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charlie.util;

/**
 *
 * @author roncoleman125
 */
public class Helper {
    /**
     * Gets the value of a hand given its values.
     * @param values Size two integer array
     * @return Hand value
     */
    public static int getValue(int[] values) {
        return values[Constant.HAND_SOFT_VALUE] <= 21 ?
                values[Constant.HAND_SOFT_VALUE] :
                values[Constant.HAND_LITERAL_VALUE];
    }
}
