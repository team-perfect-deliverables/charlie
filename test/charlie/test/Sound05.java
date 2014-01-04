/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charlie.test;

import charlie.audio.Effect;
import charlie.audio.SoundFactory;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author roncoleman125
 */
public class Sound05 {
    
    public Sound05() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }
    @Test
    public void test() {
        SoundFactory.make(Effect.NICE);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Sound01.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}