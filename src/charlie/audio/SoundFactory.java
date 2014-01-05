/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charlie.audio;

import static charlie.audio.Effect.CHIPS_IN;
import static charlie.audio.Effect.PUSH;
import java.util.Random;

/**
 *
 * @author roncoleman125
 */
public class SoundFactory {   
    private static Sound dealSound0 = new Sound("audio/PlayingCardsPo_eOnFelt01_87.wav");
    private static Sound dealSound1 = new Sound("audio/tap.wav");
    private static Sound[] charlieSounds = { new Sound("audio/shazam2.wav") };
    private static Sound[] bjSounds = { new Sound("audio/you-can-do-it.wav"), new Sound("audio/you-got-it-1.wav")};
    private static Sound[] niceSounds = { new Sound("audio/wow.wav")/*, new Sound("audio/cheer2.wav")*/ };
    private static Sound[] toughSounds = { new Sound("audio/evil_laf.wav"), new Sound("audio/aaaah.wav") };
    private static Sound[] pushSounds = { new Sound("audio/trap.wav") };
    private static Sound[] bustSounds = { new Sound("audio/ouch.wav") };
    private static Sound chipsIn = new Sound("audio/Games_Poker_Chip_08950004.wav");
    private static Sound chipsOut = new Sound("audio/Games_Poker_Chip_08950003.wav");
    
    private static long lastTime = System.currentTimeMillis();   
    protected static Random toss = new Random();

    public static void play(Effect e) {
        switch(e) {
            case DEAL:
                backgroundPlay(dealSound1);
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
                backgroundPlay(chipsIn);
                break;
            case CHIPS_OUT:
                backgroundPlay(chipsOut);
                break;                
        }        
    }
    
    protected static void backgroundPlay(final Sound sound) {
        long now = System.currentTimeMillis();
        
        if(now - lastTime < 500)
            return;
        
        lastTime = now;
        
        new Thread(new Runnable() { 
            @Override
            public void run() {
                sound.play();
            }
        }).start();
    } 
}
