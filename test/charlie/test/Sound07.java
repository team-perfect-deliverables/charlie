/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charlie.test;

import charlie.audio.Effect;
import charlie.audio.Sound;
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
public class Sound07 {
    
    public Sound07() {
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
         Sound sound = new Sound("audio/austin_yeahbaby_converted.wav");
         
         sound.play();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Sound00.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    //austin_yeahbaby.wav
}