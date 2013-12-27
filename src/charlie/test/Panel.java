/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charlie.test;

import charlie.card.Card;
import charlie.view.AHandsManager;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

/**
 *
 * @author roncoleman125
 */
public class Panel extends JPanel implements Runnable {
    Image card;// = AHandsManager.getImage(new Card(2,Card.Suite.SPADES));
    
    public Panel() {
        setBackground(Color.GREEN);
        
        setDoubleBuffered(true);

        setSize(280, 240);
        
        ImageIcon icon = new ImageIcon("./images/_blackjack_table_layout.png");
        
        card = icon.getImage();
      
        this.addNotify();
    }
    
    @Override
    public void run() {
        long beforeTime, timeDiff, sleep;

        beforeTime = System.currentTimeMillis();

        while (true) {           
            repaint();

            timeDiff = System.currentTimeMillis() - beforeTime;
            sleep = 50 - timeDiff;

            if (sleep < 0)
                sleep = 2;
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                System.out.println("interrupted");
            }

            beforeTime = System.currentTimeMillis();
        }
    }
    
        @Override
    public synchronized void paint(Graphics g) {
        super.paint(g);

        Graphics2D g2d = (Graphics2D)g;
        
        g2d.drawImage(card, 0, 0, null);
        
        Toolkit.getDefaultToolkit().sync();
        
        g.dispose();
    }
    @Override
    public void addNotify() {
        super.addNotify();
        
        Thread gameLoop = new Thread(this);
        gameLoop.start();
    }
    
    
    
    
}
