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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * This class is the main table panel.
 * @author Ron Coleman
 */
public final class Table extends JPanel implements Runnable, IUi, MouseListener {

    protected Random ran = new Random();
    protected String[] hombres = {"Fred", "Barney", "George"};
    protected String[] damas = {"Wilma", "Betty", "Jane"};
    protected AHandsManager you = new AHandsManager("You", new Point(225, 225));
    protected AHandsManager dealer = new AHandsManager("Dealer", new Point(225, 0));
    protected AHandsManager b9 = new AHandsManager(hombres[ran.nextInt(hombres.length)], new Point(0, 50));
    protected AHandsManager prot = new AHandsManager(damas[ran.nextInt(damas.length)], new Point(100, 50));
    protected AHandsManager[] handsManager = {you, dealer, b9, prot};
    protected TurnSprite turnSprite = new TurnSprite();
    protected AHand turn = null;
    protected List<Feedback> feedbacks = new ArrayList<>();
    protected HashMap<Seat, AHandsManager> seats = new HashMap<Seat, AHandsManager>() {
        {
            put(Seat.YOU, you);
            put(Seat.RIGHT, b9);
            put(Seat.LEFT, prot);
            put(Seat.DEALER, dealer);
        }
    };
    
    private final HashMap<Seat,AMoneyManager> monies = new HashMap<Seat,AMoneyManager>() {
        {
            put(Seat.YOU,new AMoneyManager());
            put(Seat.RIGHT,new ABotMoneyManager());
            put(Seat.LEFT,new ABotMoneyManager());
        }
    };
    
    protected HashMap<Hid, AHand> manos = new HashMap<>();
    private Thread gameLoop;
    private static Color COLOR_FELT = new Color(0, 127, 14);
    private final int DELAY = 50;
    private final GameFrame frame;
    private boolean bettable = false;
    private boolean gameOver = true;
    private int shoeSize;

    /**
     * Constructor
     * @param frame Main game frame
     * @param parent Parent panel containing this one.
     */
    public Table(GameFrame frame, JPanel parent) {
        this.frame = frame;

        setSize(parent.getWidth(), parent.getHeight());

        init();
    }

    /**
     * Initializes custom table components.
     */
    public void init() {
        setBackground(COLOR_FELT);

        setDoubleBuffered(true);

        this.addMouseListener(this);

        this.addNotify();
    }

    /**
     * Gets the bet amount on the table.
     * @return 
     */
    public Integer getBetAmt() {
        Integer amt = this.monies.get(Seat.YOU).getAmount();
        return amt;
    }

    /**
     * Makes the paint method get invoked.
     */
    @Override
    public void addNotify() {
        super.addNotify();

        gameLoop = new Thread(this);

        gameLoop.start();
    }

    /**
     * Paints the display some time after repainted invoked.
     * @param _g 
     */
    @Override
    public synchronized void paint(Graphics _g) {
        super.paint(_g);

        Graphics2D g = (Graphics2D) _g;

        // Render the bet on the table
        this.monies.get(Seat.YOU).render(g);
        
        // Render the hands
        for (int i = 0; i < handsManager.length; i++) {
            handsManager[i].render(g);
        }
        // Java tool related stuff
        Toolkit.getDefaultToolkit().sync();

        _g.dispose();
    }

    /**
     * Updates the table.
     */
    public synchronized void update() {
        // Update every hand at the table
        for (int i = 0; i < handsManager.length; i++)
            handsManager[i].update();

        // If it's my turn, I didn't break, and my cards have landed,
        // then enable me to play
        if (turn != null
                && turn.hid.getSeat() == Seat.YOU
                && !turn.isBroke()
                && you.isReady()
                && dealer.isReady()
                && !gameOver) {
            // "trucking" => I'm running
            frame.enableTrucking(true);
            
            // Enable the buttons
            frame.enablePlay(true);
        }
    }

    /**
     * Repaints the diplay
     */
    public void render() {
        repaint();
    }

    /**
     * Runs the game loop
     */
    @Override
    public void run() {
        long beforeTime, timeDiff, sleep;

        beforeTime = System.currentTimeMillis();

        while (true) {
            update();

            render();

            timeDiff = System.currentTimeMillis() - beforeTime;
            sleep = DELAY - timeDiff;

            if (sleep < 0) {
                sleep = 2;
            }
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                System.out.println("interrupted");
            }

            beforeTime = System.currentTimeMillis();
        }
    }

    /**
     * Sets the amt.
     * @param amt Bankroll
     */
    public void setBankroll(Double amt) {
        this.monies.get(Seat.YOU).setBankroll(amt);
    }

    /**
     * Enables betting (i.e., the keys work)
     * @param betting True or false
     */
    public void enableBetting(boolean betting) {
        bettable = betting;
    }

    /**
     * Sets the turn for a hand.
     *
     * @param hid Hand id
     */
    @Override
    public void turn(Hid hid) {
        AHand hand = manos.get(hid);

        if (hid.getSeat() == Seat.DEALER) {
            // Turn off the turn signal
            // Note: "turn" will be null on dealer blackjack in which case
            // nobody has played.
            if (turn != null) {
                turn.enablePlaying(false);
            }

            // Reveal the hole card
            hand.get(0).reveal();

            // Disable player input
            this.frame.enableTrucking(false);
            this.frame.enablePlay(false);
        } else {
            // Disable old hand
            if (turn != null)
            {
                turn.enablePlaying(false);
            }

            // Enable new hand
            turn = hand;

            // If turn is NOT my hand, disable my hand
            boolean me = true;

            if (hid.getSeat() != Seat.YOU) {
                me = false;
            }

            turn.enablePlaying(me);
            this.frame.enableTrucking(me);
            this.frame.enablePlay(me);
        }
    }

    /**
     * Receives a hit for a hand.
     *
     * @param hid Hand id
     * @param card Card hitting the hand
     */
    @Override
    public synchronized void deal(Hid hid, Card card, int[] handValues) {
        AHand hand = manos.get(hid);

        hand.setValues(handValues);

        // If card is null, this is not a "real" hit but only
        // updating the respective hand value.
        if (card == null) {
            return;
        }

        // Convert card to an animated card and hit the hand
        ACard acard = AHandsManager.animate(card);

        hand.hit(acard);
    }

    /**
     * Busts a hand.
     *
     * @param hid Hand id
     */
    @Override
    public void bust(Hid hid) {
        AHand hand = manos.get(hid);

        hand.setOutcome(AHand.Outcome.Bust);
        
        AMoneyManager money = this.monies.get(hid.getSeat());
        
        money.decrease(hid.getAmt());        
    }

    /**
     * Updates hand with winning outcome.
     *
     * @param hid Hand id
     */
    @Override
    public void win(Hid hid) {
        AHand hand = manos.get(hid);

        hand.setOutcome(AHand.Outcome.Win);
        
        AMoneyManager money = this.monies.get(hid.getSeat());
        
        money.increase(hid.getAmt());
    }

    /**
     * Updates hand with loosing outcome.
     *
     * @param hid Hand id
     */
    @Override
    public void loose(Hid hid) {
        AHand hand = manos.get(hid);

        hand.setOutcome(AHand.Outcome.Loose);
        
        AMoneyManager money = this.monies.get(hid.getSeat());
        
        money.decrease(hid.getAmt());        
    }

    /**
     * Updates hand with push outcome.
     *
     * @param hid Hand id
     */
    @Override
    public void push(Hid hid) {
        AHand hand = manos.get(hid);

        hand.setOutcome(AHand.Outcome.Push);
    }

    /**
     * Updates hand with Blackjack outcome.
     *
     * @param hid Hand id
     */
    @Override
    public void blackjack(Hid hid) {
        AHand hand = manos.get(hid);

        hand.setOutcome(AHand.Outcome.Blackjack);
        
        AMoneyManager money = this.monies.get(hid.getSeat());
        
        money.increase(hid.getAmt());        
    }

    /**
     * Updates hand with Charlie outcome.
     *
     * @param hid Hand id
     */
    @Override
    public void charlie(Hid hid) {
        AHand hand = manos.get(hid);

        hand.setOutcome(AHand.Outcome.Charlie);
        
        AMoneyManager money = this.monies.get(hid.getSeat());
        
        money.increase(hid.getAmt());        
    }

    /**
     * Starts a game.
     * Note: we received the initial player bankroll during login
     * which is handled by GameFrame.
     *
     * @param hids Hand ids
     */
    @Override
    public void starting(List<Hid> hids,int shoeSize) {
        // Clear out everything from last game
        for (AHandsManager animator : seats.values())
            animator.clear();

        this.shoeSize = shoeSize;
        
        // It's nobdy's turn...yet
        turn = null;

        // Game definitely not over
        gameOver = false;

        // Create corresponding (animated) hands
        for (Hid hid : hids) {
            AHand hand =
                    hid.getSeat() == Seat.DEALER ? new ADealerHand(hid) : new AHand(hid);

            // Assign hand to it's manager
            AHandsManager animator = seats.get(hid.getSeat());

            animator.add(hand);

            // Put the hand in its mano cache for quick look up later
            manos.put(hid, hand);
        }
    }

    /**
     * Signals end of a game.
     * @param shoeSize Shoe size
     */
    @Override
    public void ending(int shoeSize) {
        // Game now over
        gameOver = true;
        
        // Update the shoe size
        this.shoeSize = shoeSize;

        // Enable betting and dealing again
        frame.enableDeal(true);
        this.bettable = true;

        // Disable play -- we must wait for player to bet and request deal
        frame.enablePlay(false);
    }

    @Override
    public void shuffling() {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    /**
     * Register bets.
     *
     * @param e Event
     */
    @Override
    public void mousePressed(MouseEvent e) {
        if (!bettable) {
            return;
        }

        // Get the coordinates of the mouse and let bet manager
        // determine whether this is a bet and how much.
        int x = e.getX();
        int y = e.getY();

        monies.get(Seat.YOU).click(x, y);
    }

    /**
     * Toggles the button image from pressed to up.
     *
     * @param e
     */
    @Override
    public void mouseReleased(MouseEvent e) {
        monies.get(Seat.YOU).unclick();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}
