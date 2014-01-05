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
import charlie.audio.Effect;
import charlie.audio.SoundFactory;
import charlie.card.Hid;
import charlie.card.Card;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import javax.swing.JPanel;
import charlie.util.Point;
import charlie.dealer.Seat;
import charlie.util.Config;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import javax.swing.ImageIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is the main table panel.
 *
 * @author Ron Coleman
 */
public final class Table extends JPanel implements Runnable, IUi, MouseListener {
    private final Logger LOG = LoggerFactory.getLogger(Table.class);
    
    protected Random ran = new Random();
    protected String[] b9s = {"Apollo", "Zeus", "Talos"};
    protected String[] aaf709s = {"Hera", "Athena", "Hecate"};
    protected AHandsManager you = new AHandsManager("You", new Point(225, 225));
    protected AHandsManager dealer = new AHandsManager("Dealer", new Point(225, 0));
    protected AHandsManager b9 = new AHandsManager(b9s[ran.nextInt(b9s.length)], new Point(450, 150));
    protected AHandsManager aaf709 = new AHandsManager(aaf709s[ran.nextInt(aaf709s.length)], new Point(25, 150));
    protected AHandsManager[] handsManager = {you, dealer, b9, aaf709};
    protected TurnSprite turnSprite = new TurnSprite();
    protected AHand turn = null;
    protected HashMap<Seat, AHandsManager> seats = new HashMap<Seat, AHandsManager>() {
        {
            put(Seat.YOU, you);
            put(Seat.RIGHT, b9);
            put(Seat.LEFT, aaf709);
            put(Seat.DEALER, dealer);
        }
    };
    private final HashMap<Seat, AMoneyIndicator> monies = new HashMap<Seat, AMoneyIndicator>() {
        {
            put(Seat.YOU, new AMoneyIndicator());
            put(Seat.RIGHT, new ABotMoneyManager());
            put(Seat.LEFT, new ABotMoneyManager());
        }
    };
    protected HashMap<Hid, AHand> manos = new HashMap<>();
    private Thread gameLoop;
    private static Color COLOR_FELT = new Color(0, 153, 100);
    private final int DELAY = 50;
    private final GameFrame frame;
    private boolean bettable = false;
    private boolean gameOver = true;
    private int shoeSize;
    private Image instrImg;
    private Image shoeImg;
    private Image trayImg;
    private int numHands;
    private int looserCount;
    private int pushCount;
    private int winnerCount;

    /**
     * Constructor
     *
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

        this.instrImg = new ImageIcon(Config.DIR_IMGS + "dealer-stands-0.png").getImage();
        this.shoeImg = new ImageIcon(Config.DIR_IMGS + "shoe-0.png").getImage();
        this.trayImg = new ImageIcon(Config.DIR_IMGS + "tray-0.png").getImage();
    }

    /**
     * Gets the bet amount on the table.
     *
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
     *
     * @param _g
     */
    @Override
    public synchronized void paint(Graphics _g) {
        super.paint(_g);

        Graphics2D g = (Graphics2D) _g;

        // Render the paraphenelia
        g.drawImage(this.instrImg, 140, 208, this);
        g.drawImage(this.shoeImg, 540, 5, this);
        g.drawImage(this.trayImg, 430, 5, this);

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
        for (int i = 0; i < handsManager.length; i++) {
            handsManager[i].update();
        }

        // If it's my turn, I didn't break, and my cards have isLanded,
        // then enable enable to play
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
     * Repaints the display
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
     *
     * @param amt Bankroll
     */
    public void setBankroll(Double amt) {
        this.monies.get(Seat.YOU).setBankroll(amt);
    }

    /**
     * Enables betting (i.e., the keys work)
     *
     * @param betting True or false
     */
    public void enableBetting(boolean betting) {
        bettable = betting;
    }

    /**
     * Double the bet on the panel.
     *
     * @param hid Hand id
     */
    public void dubble(Hid hid) {
        AMoneyIndicator money = this.monies.get(hid.getSeat());

        money.dubble();
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
            hand.get(0).flip();

            // Disable player input
            this.frame.enableTrucking(false);
            this.frame.enablePlay(false);
        } else {
            // Disable old hand
            if (turn != null) {
                turn.enablePlaying(false);
            }

            // Enable new hand
            turn = hand;

            turn.enablePlaying(true);

            // If turn is NOT my hand, disable my hand
            boolean enable = true;

            if (hid.getSeat() != Seat.YOU) {
                enable = false;
            }

            this.frame.enableTrucking(enable);
            this.frame.enablePlay(enable);
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
        SoundFactory.play(Effect.DEAL);

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
        LOG.info("BUST for hid = "+hid);
        
        AHand hand = manos.get(hid);

        hand.setOutcome(AHand.Outcome.Bust);

        AMoneyIndicator money = this.monies.get(hid.getSeat());

        money.decrease(hid.getAmt());

        if (hid.getSeat() != Seat.DEALER) {
            SoundFactory.play(Effect.BUST);
            looserCount++;
        }
    }

    /**
     * Updates hand with winning outcome.
     *
     * @param hid Hand id
     */
    @Override
    public void win(Hid hid) {
        LOG.info("WIN for hid = "+hid);
        
        AHand hand = manos.get(hid);

        hand.setOutcome(AHand.Outcome.Win);

        AMoneyIndicator money = this.monies.get(hid.getSeat());

        money.increase(hid.getAmt());

        winnerCount++;
    }

    /**
     * Updates hand with loosing outcome.
     *
     * @param hid Hand id
     */
    @Override
    public void loose(Hid hid) {
        LOG.info("LOOSE for hid = "+hid);
        
        AHand hand = manos.get(hid);

        hand.setOutcome(AHand.Outcome.Loose);

        AMoneyIndicator money = this.monies.get(hid.getSeat());

        money.decrease(hid.getAmt());

        looserCount++;
    }

    /**
     * Updates hand with push outcome.
     *
     * @param hid Hand id
     */
    @Override
    public void push(Hid hid) {
        LOG.info("PUSH for hid = "+hid);
        
        AHand hand = manos.get(hid);

        hand.setOutcome(AHand.Outcome.Push);

        ++pushCount;
    }

    /**
     * Updates hand with Blackjack outcome.
     *
     * @param hid Hand id
     */
    @Override
    public void blackjack(Hid hid) {
        LOG.info("BJ for hid = "+hid);
        
        AHand hand = manos.get(hid);

        hand.setOutcome(AHand.Outcome.Blackjack);

        AMoneyIndicator money = this.monies.get(hid.getSeat());

        money.increase(hid.getAmt());

        if (hid.getSeat() != Seat.DEALER) {
            SoundFactory.play(Effect.BJ);

            winnerCount++;
        }
    }

    /**
     * Updates hand with Charlie outcome.
     *
     * @param hid Hand id
     */
    @Override
    public void charlie(Hid hid) {
        LOG.info("CHARLIE for hid = "+hid);
        
        AHand hand = manos.get(hid);

        hand.setOutcome(AHand.Outcome.Charlie);

        AMoneyIndicator money = this.monies.get(hid.getSeat());

        money.increase(hid.getAmt());

        SoundFactory.play(Effect.CHARLIE);

        winnerCount++;
    }

    /**
     * Starts a game. Note: we received the initial player bankroll during login
     * which is handled by GameFrame.
     *
     * @param hids Hand ids
     */
    @Override
    public void starting(List<Hid> hids, int shoeSize) {
        // Clear out everything from last game 
        numHands = hids.size();

        winnerCount = looserCount = pushCount = 0;

        for (Hid hid : manos.keySet()) {
            AMoneyIndicator money = monies.get(hid.getSeat());

            // Skip the dealer since it doesn't have a money manager
            if (money == null) {
                continue;
            }

            money.undubble();
        }

        for (AHandsManager animator : seats.values()) {
            animator.clear();
        }

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
     *
     * @param shoeSize Shoe size
     */
    @Override
    public void ending(int shoeSize) {
        LOG.info("num hands = "+numHands);
        LOG.info("winner count = "+winnerCount);
        LOG.info("looser count = "+looserCount);
        LOG.info("push count = "+pushCount);
        
        // Game now over
        gameOver = true;

        // Update the shoe size
        this.shoeSize = shoeSize;

        // Enable betting and dealing again
        frame.enableDeal(true);
        this.bettable = true;

        // Disable play -- we must wait for player to bet and request deal
        frame.enablePlay(false);

        if (winnerCount == numHands - 1) {
            SoundFactory.play(Effect.NICE);
        } else if (looserCount == numHands - 1) {
            SoundFactory.play(Effect.TOUGH);
        } else if (pushCount == numHands - 1) {
            SoundFactory.play(Effect.PUSH);
        }
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
