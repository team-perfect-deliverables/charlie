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
package charlie.audio;

import static charlie.audio.Effect.CHIPS_IN;
import static charlie.audio.Effect.PUSH;
import java.util.Random;

/**
 * This class implements the factory pattern for managing and playing sounds.
 * @author Ron Coleman
 */
public class SoundFactory {   
    private final static Sound dealSound0 = new Sound("audio/PlayingCardsPo_eOnFelt01_87.wav");
    private final static Sound dealSound1 = new Sound("audio/tap.wav");
    private final static Sound dealSound2 = new Sound("audio/Telemet_33G_HD2-32076.wav");
    private final static Sound[] charlieSounds = { 
        new Sound("audio/shazam2.wav")
    };
    private final static Sound[] bjSounds = { 
        new Sound("audio/you-can-do-it.wav"), 
        new Sound("audio/you-got-it-1.wav"),
        new Sound("audio/wahoo.wav")
    };
    private final static Sound[] niceSounds = { 
        new Sound("audio/wow.wav"), 
        new Sound("audio/austin_yeahbaby_converted.wav"),
        new Sound("audio/woow.wav")
    };
    private final static Sound[] toughSounds = {
        new Sound("audio/evil_laf.wav"), 
        new Sound("audio/aaaah.wav"), 
        new Sound("audio/bone_converted.wav"), 
        new Sound("audio/glass.wav"),
//        new Sound("audio/vplaugh.wav")
    };
    private final static Sound[] pushSounds ={ 
        new Sound("audio/trap.wav") 
    };
    private final static Sound[] bustSounds = { 
        new Sound("audio/ouch.wav")
    };
    private final static Sound chipsIn = new Sound("audio/Games_Poker_Chip_08950004.wav");
    private final static Sound chipsOut = new Sound("audio/Games_Poker_Chip_08950003.wav");
    private final static Sound shuffle = new Sound("audio/013012_Casino-Cards_28_A1.wav");
    private final static Sound turn = new Sound("audio/Telemet_33G_HD2-32076.wav");
    private static long lastTime = System.currentTimeMillis();   
    protected static Random toss = new Random();
    private static boolean enabled = true;

    /**
     * Primes the sound line
     */
    public static void prime() {
        // Get any sound
        Sound sound = niceSounds[0];
        
        // Set volume to allowed minimum
        sound.setVolume(-80.0f);
        
        // Play the sound
        sound.play();
        
        // Restore volume to allowed maximum
        sound.setVolume(6.0f);
    }
    
    public static void enable(boolean state) {
        enabled = state;
    }
    
    /**
     * Plays a sound
     * @param e Effect
     */
    public static void play(Effect e) {
        if(!enabled)
            return;
        
        switch(e) {
            case TURN:
                turn.play();
                break;
            case SHUFFLING:
                backgroundPlay(shuffle,2);
                break;
            case DEAL:
                backgroundPlay(dealSound1,1);
                break;
            case CHARLIE:
                charlieSounds[toss.nextInt(charlieSounds.length)].play();
                break;
            case BJ:
                bjSounds[toss.nextInt(bjSounds.length)].play();
                break;
            case NICE:
                niceSounds[toss.nextInt(niceSounds.length)].play();
                break;
            case TOUGH:
                toughSounds[toss.nextInt(toughSounds.length)].play();  
                break;
            case PUSH:
                pushSounds[toss.nextInt(pushSounds.length)].play();
                break;                
            case BUST:
                bustSounds[toss.nextInt(bustSounds.length)].play();
                break; 
            case CHIPS_IN:
                backgroundPlay(chipsIn,1);
                break;
            case CHIPS_OUT:
                backgroundPlay(chipsOut,1);
                break;                
        }        
    }
    
    protected static void backgroundPlay(final Sound sound,final int loop) {
        long now = System.currentTimeMillis();
        
        if(now - lastTime < 500)
            return;
        
        lastTime = now;
        
        new Thread(new Runnable() { 
            @Override
            public void run() {
                for(int i=0; i < loop; i++)
                    sound.play();
            }
        }).start();
    } 
}
