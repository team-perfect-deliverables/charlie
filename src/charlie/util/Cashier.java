/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charlie.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author roncoleman125
 */
public class Cashier {
    public enum Chip { DOLLAR, FIVE, TWENTY_FIVE, HUNDRED };
    
    private static HashMap<Chip,Integer> exchange = new HashMap<Chip,Integer>( )
    {{
        put(Chip.DOLLAR,1);
        put(Chip.FIVE,5);
        put(Chip.TWENTY_FIVE,25);
        put(Chip.HUNDRED,100);
    }};
    
    public static List<Chip> dubble(List<Chip> chips) {
        chips.addAll(chips);
        return chips;
    }
    
    public static Double cashin(List<Chip> chips) {
        Double sum = 0.0;
        
        for(Chip chip: chips) {
            sum += exchange.get(chip);
        }
        
        return sum;
    }
    
    public static List<Chip> toChips(Integer amt) {       
        List<Chip> myChips = new ArrayList<>();

        int hundreds = amt / 100;
        
        for(int k=0; k < hundreds; k++)
            myChips.add(Chip.HUNDRED);
        
        int leftover = amt - hundreds * 100;
        
        int twentyFives = leftover / 25;
        
        for(int k=0; k < twentyFives; k++)
            myChips.add(Chip.TWENTY_FIVE);
        
        leftover = amt - hundreds * 100 - twentyFives * 25;
        
        int fives = leftover / 5;
        
        for(int k=0; k < fives; k++)
            myChips.add(Chip.FIVE);        

        return myChips;
    }
}
