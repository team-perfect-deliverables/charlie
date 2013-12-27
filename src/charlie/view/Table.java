/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charlie.view;

import charlie.view.sprite.TurnSprite;
import charlie.GameFrame;
import charlie.card.Hid;
import charlie.card.Card;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import javax.swing.JPanel;
import charlie.util.Point;
import charlie.controller.Seat;
import charlie.util.Helper;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 *
 * @author roncoleman125
 */
public final class Table extends JPanel implements Runnable, IUi, MouseListener {
    protected Random ran = new Random();
    
    protected String[] hombres = { "Fred", "Bob", "Dave" };
    protected String[] mujeres = { "Betty", "Sue", "Natalie" };
    
    protected AHandsManager you = new AHandsManager("You", new Point(225,225));
    protected AHandsManager dealer = new AHandsManager("Dealer", new Point(225,0));
    protected AHandsManager hombre = new AHandsManager(hombres[ran.nextInt(hombres.length)], new Point(0,50));
    protected AHandsManager mujer = new AHandsManager(mujeres[ran.nextInt(mujeres.length)], new Point(100,50));
    
    protected AHandsManager[] handsManager = { you, dealer, hombre, mujer };
    
    protected TurnSprite turnSprite = new TurnSprite();
    
    protected AHand turn = null;
    protected List<Feedback> feedbacks = new ArrayList<>();
    
    protected HashMap<Seat,AHandsManager> seats = new HashMap<Seat,AHandsManager>()
    {{
        put(Seat.YOU,you);
        put(Seat.LEFT,hombre);
        put(Seat.RIGHT,mujer);
        put(Seat.DEALER,dealer);
    }};
    
    protected HashMap<Hid,AHand> manos = new HashMap<>();

    private Thread gameLoop;
    
    private static Color COLOR_FELT = new Color(0,153,0);

    private final int DELAY = 50;
    private Image cover = null;
    private final GameFrame frame;  
    private final BetManager betManager = new BetManager();
    private boolean bettable = false;
    private boolean gameOver = true;

    public Table(GameFrame frame,JPanel parent) {
        this.frame = frame;
        
        setSize(parent.getWidth(),parent.getHeight());
        
        init();
    }
    
    public void init() {
        setBackground(COLOR_FELT);
        
        setDoubleBuffered(true);

        this.addMouseListener(this);
        
        this.addNotify();        
    }
    
    public Integer getBetAmt() {
        Integer amt = this.betManager.getAmount();
        return amt;
    }
    
    @Override
    public void addNotify() {
        super.addNotify();
        
        gameLoop = new Thread(this);
        
        gameLoop.start();
    }

    @Override
    public synchronized void paint(Graphics _g) {
        super.paint(_g);

        Graphics2D g = (Graphics2D)_g;
        
//        g.drawImage(cover, 0, 0, null);
        
        for(int i=0; i < handsManager.length; i++)
            handsManager[i].render(g);
        
//        turnSprite.render(g);
        this.betManager.render(g);
        
        Toolkit.getDefaultToolkit().sync();
        
        _g.dispose();
    }

    public synchronized void update() {
        for(int i=0; i < handsManager.length; i++)
            handsManager[i].update();
        
        
        // If it's my turn, I didn't break, and my cards have landed, then enable playing
        if(turn != null &&
                turn.hid.getSeat() == Seat.YOU &&
                !turn.isBroke() &&
                you.isReady() &&
                dealer.isReady() &&
                !gameOver)
        {
            frame.enableTrucking(true);
            frame.enablePlaying(true);
        }

//        boolean isReady = true;
//        for(int i=0; i < handsManager.length; i++)
//            if(!handsManager[i].isReady()) {
//                isReady = false;
//                break;
//            }
//        frame.enablePlaying(isReady);
    }
    
    public void render() {
        repaint();
    }

    @Override
    public void run() {
        long beforeTime, timeDiff, sleep;

        beforeTime = System.currentTimeMillis();

        while (true) {
            update();
            
            render();

            timeDiff = System.currentTimeMillis() - beforeTime;
            sleep = DELAY - timeDiff;

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

    public void setBankroll(Double bankroll) {
        this.betManager.setBankroll(bankroll);
    }
    
    public void enableBetting(boolean betting) {
        bettable = betting;
    }
    
    @Override
    public void turn(Hid hid) {
        AHand hand = manos.get(hid);
        
        if(hid.getSeat() == Seat.DEALER) {
            hand.get(0).reveal();
            this.frame.enableTrucking(false);
            this.frame.enablePlaying(false);
        }
        else {
            // Disable current hand
            if(turn != null)
                turn.enablePlaying(false);
            
            turn = hand;

            boolean enableMe = true;
            
            if (hid.getSeat() != Seat.YOU)
                enableMe = false;
            
            turn.enablePlaying(enableMe);
            this.frame.enableTrucking(enableMe);
            this.frame.enablePlaying(enableMe);
        }
    }
    
    /**
     * Hits the animated hand.
     * @param hid Hand id
     * @param card Card hitting the animated hand
     */
    @Override
    public synchronized void deal(Hid hid, Card card, int[] handValues) {
        AHand ahand = manos.get(hid);
        
        ahand.setValues(handValues);
        
        // If the card is null, we're just updating the hand value
        // most likely turning over a hole card.
        if(card != null)      
            ahand.add(AHandsManager.animate(card));
        
    }

    @Override
    public void bust(Hid hid) {
        AHand hand = manos.get(hid);
        
        hand.setOutcome(AHand.Outcome.Bust);
    }

    @Override
    public void win(Hid hid) {
        AHand hand = manos.get(hid);
        
        hand.setOutcome(AHand.Outcome.Win);
    }

    @Override
    public void loose(Hid hid) {
        AHand hand = manos.get(hid);
        
        hand.setOutcome(AHand.Outcome.Loose);
    }

    @Override
    public void push(Hid hid) {
        AHand hand = manos.get(hid);
        
        hand.setOutcome(AHand.Outcome.Push);
    }

    @Override
    public void blackjack(Hid hid) {
        AHand hand = manos.get(hid);
        
        hand.setOutcome(AHand.Outcome.Blackjack);
    }

    @Override
    public void charlie(Hid hid) {
        AHand hand = manos.get(hid);
        
        hand.setOutcome(AHand.Outcome.Charlie);
    }
    
    @Override
    public void starting(List<Hid> handIds) {
        for(AHandsManager animator: seats.values())
            animator.clear();
        
        turn = null;
        gameOver = false;
        
        for(Hid hid: handIds) {
            AHand hand =
                hid.getSeat() == Seat.DEALER ? new ADealerHand(hid) : new AHand(hid);
            
            AHandsManager animator = seats.get(hid.getSeat());
            
            manos.put(hid, hand);
            
            animator.add(hand);
        }
    }
    
    @Override
    public void ending(Double bankroll) {
        betManager.setBankroll(bankroll);
        frame.enableBetting(true);
        frame.enablePlaying(false);
        gameOver = true;
    }

    @Override
    public void shuffling() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mouseClicked(MouseEvent e) {
//        if(e.getClickCount() == 1) {
//            int x = e.getX();
//            int y = e.getY();
//            System.out.println("mouse clicked x = "+x+" y = "+y);
//        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if(!bettable)
            return;
        
        int x = e.getX();
        int y = e.getY();
        this.betManager.click(x, y);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        this.betManager.unclick();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}
