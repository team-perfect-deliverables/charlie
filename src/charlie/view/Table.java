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
import java.awt.Image;
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
    protected String[] hombres = {"Fred", "Bob", "Dave"};
    protected String[] mujeres = {"Betty", "Sue", "Natalie"};
    protected AHandsManager you = new AHandsManager("You", new Point(225, 225));
    protected AHandsManager dealer = new AHandsManager("Dealer", new Point(225, 0));
    protected AHandsManager hombre = new AHandsManager(hombres[ran.nextInt(hombres.length)], new Point(0, 50));
    protected AHandsManager mujer = new AHandsManager(mujeres[ran.nextInt(mujeres.length)], new Point(100, 50));
    protected AHandsManager[] handsManager = {you, dealer, hombre, mujer};
    protected TurnSprite turnSprite = new TurnSprite();
    protected AHand turn = null;
    protected List<Feedback> feedbacks = new ArrayList<>();
    protected HashMap<Seat, AHandsManager> seats = new HashMap<Seat, AHandsManager>() {
        {
            put(Seat.YOU, you);
            put(Seat.LEFT, hombre);
            put(Seat.RIGHT, mujer);
            put(Seat.DEALER, dealer);
        }
    };
    protected HashMap<Hid, AHand> manos = new HashMap<>();
    private Thread gameLoop;
    private static Color COLOR_FELT = new Color(0, 127, 14);
    private final int DELAY = 50;
    private Image cover = null;
    private final GameFrame frame;
    private final BetManager betManager = new BetManager();
    private boolean bettable = false;
    private boolean gameOver = true;

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
        Integer amt = this.betManager.getAmount();
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

//        g.drawImage(cover, 0, 0, null);

        // Render the hands
        for (int i = 0; i < handsManager.length; i++) {
            handsManager[i].render(g);
        }
        
        // Render the bet on the table
        this.betManager.render(g);

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
     * Sets the bankroll.
     * @param bankroll Bankroll
     */
    public void setBankroll(Double bankroll) {
        this.betManager.setBankroll(bankroll);
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
            if (turn != null) // This turns off the turn indicator
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
    }

    /**
     * Starts a game. Note: we received the initial player bankroll after slogin
     * which is handled by GameFrame.
     *
     * @param hids Hand ids
     */
    @Override
    public void starting(List<Hid> hids) {
        // Clear out everything from last game
        for (AHandsManager animator : seats.values()) {
            animator.clear();
        }

        // It's nobdy's turn...yet
        turn = null;

        // Game definitely not over
        gameOver = false;

        // Create corresponding (animated) hands -- dealer has non-animated ones
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
     * Ends a game with a players bankroll
     *
     * @param bankroll Bankroll
     */
    @Override
    public void ending(Double bankroll) {
        betManager.setBankroll(bankroll);

        // Enable betting/dealing again
        frame.enableDeal(true);
        this.bettable = true;

        // Diable play -- waiting for a bet/deal
        frame.enablePlay(false);

        gameOver = true;
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

        this.betManager.click(x, y);
    }

    /**
     * Toggles the button image from pressed to up.
     *
     * @param e
     */
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
