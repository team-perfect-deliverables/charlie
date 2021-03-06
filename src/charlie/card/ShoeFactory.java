/*
 Copyright (c) 2014 Ron Coleman

 Permission is hereby granted, free of charge, to any person obtaining
 a copy of this software and associated documentation files (the
 "Software"), to deal in the Software without restriction, including
 without limitation the rights to use, copy, modify, merge, publish,
 distribute, sublicense, and/or sell copies of the Software, and to
 permit persons to whom the Software is furnished to do so, subject to
 the following conditions:

 The above copyright notice and this permission notice shall be
 included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package charlie.card;

import charlie.plugin.IShoe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements the shoe factory.
 * @author Ron Coleman
 */
public class ShoeFactory {     
    private static final Logger LOG = LoggerFactory.getLogger(ShoeFactory.class);
    /**
     * Gets an instance of a shoe based on a scenario.
     * @param scenario Scenario
     * @return Shoe
     */
    public static IShoe getInstance(String scenario) {
        Class<?> clazz;
        try {
            clazz = Class.forName(scenario);
            
            IShoe shoe = (IShoe) clazz.newInstance();
            
            return shoe;
        }
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            LOG.error("failed to instantiate shoe '"+scenario+"': " + ex);
        }
        
        return null;
    }
}
