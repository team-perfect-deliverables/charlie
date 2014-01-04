package charlie.test;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import charlie.audio.Sound;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author roncoleman125
 */
public class Sound00 {
    
    public Sound00() {
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
    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
     @Test
     public void test() {
         Sound sound = new Sound("audio/013012_Casino-Cards_27_A1.wav");
         
         sound.play();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Sound00.class.getName()).log(Level.SEVERE, null, ex);
        }
         
         assert(true);
     }
}