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

import charlie.plugin.IUi;
import charlie.view.sprite.TurnIndicator;
import charlie.GameFrame;
import charlie.audio.Effect;
import charlie.audio.SoundFactory;
import charlie.card.Hid;
import charlie.card.Card;
import charlie.card.HoleCard;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import javax.swing.JPanel;
import charlie.util.Point;
import charlie.dealer.Seat;
import charlie.plugin.IGerty;
import charlie.plugin.ISideBetView;
import charlie.util.Constant;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is the main table panel.
 *
 * @author Ron Coleman
 */
public final class ATable extends JPanel implements Runnable, IUi, MouseListener {
    private final Logger LOG = LoggerFactory.getLogger(ATable.class);
    protected final String SIDE_BET_VIEW_PROPERTY = "charlie.sidebet.view";    
    protected Random ran = new Random();
    protected String[] b9s = {"Apollo", "Zeus", "Talos"};
    protected String[] n6s = {"Hera", "Athena", "Hecate"};
    protected AHandsManager you = new AHandsManager("You", new Point(225, 225));
    protected AHandsManager dealer = new AHandsManager("Dealer", new Point(225, 0));
    protected AHandsManager b9 = new AHandsManager(b9s[ran.nextInt(b9s.length)], new Point(450, 150));
    protected AHandsManager n6 = new AHandsManager(n6s[ran.nextInt(n6s.length)], new Point(25, 150));
    protected AHandsManager[] handsManager = {you, dealer, b9, n6};
    protected TurnIndicator turnSprite = new TurnIndicator();
    protected AHand turn = null;
    protected HashMap<Seat, AHandsManager> seats = new HashMap<Seat, AHandsManager>() {
        {
            put(Seat.YOU, you);
            put(Seat.RIGHT, b9);
            put(Seat.LEFT, n6);
            put(Seat.DEALER, dealer);
        }
    };
    private final HashMap<Seat, AMoneyManager> monies = new HashMap<Seat, AMoneyManager>() {
        {
            put(Seat.YOU, new AMoneyManager());
            put(Seat.RIGHT, new ABotMoneyManager());
            put(Seat.LEFT, new ABotMoneyManager());
        }
    };
    
    protected HashMap<Hid, AHand> manos = new HashMap<>();
    protected Thread gameLoop;
    protected static Color COLOR_FELT = new Color(0, 153, 100);
    protected final int DELAY = 50;
    protected final GameFrame frame;
    protected boolean bettable = false;
    protected boolean gameOver = true;
    protected int shoeSize;
    protected Image instrImg;
    protected Image shoeImg;
    protected Image trayImg;
    protected ABurnCard burnCard = new ABurnCard();
    protected int numHands;
    protected int looserCount;
    protected int pushCount;
    protected int winnerCount;
    protected ISideBetView sideBetView;
    protected Properties props; 
    protected IGerty gerty;
    private Card holeCard;
    private int[] holeValues;

    /**
     * Constructor
     *
     * @param frame Main game frame
     * @param parent Parent panel containing this one.
     */
    public ATable(GameFrame frame, JPanel parent) {
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

        this.instrImg = new ImageIcon(Constant.DIR_IMGS + "dealer-stands-0.png").getImage();
        this.shoeImg = new ImageIcon(Constant.DIR_IMGS + "shoe-0.png").getImage();
        this.trayImg = new ImageIcon(Constant.DIR_IMGS + "tray-0.png").getImage();
        
        this.loadConfig();
    }
    
    /**
     * Clears table of old bets, etc.
     */
    public void clear() {
        winnerCount = looserCount = pushCount = 0;

        for (Hid hid : manos.keySet()) {
            AMoneyManager money = monies.get(hid.getSeat());

            // Skip dealer since it doesn't have a money manager
            if (money == null)
                continue;

            money.undubble();
        }
        
        if(sideBetView != null)
            sideBetView.starting();     
        
        holeCard = null;
        
        holeValues = null;
    }

    /**
     * Gets the main bet amount on the table.<br>
     * This should only be requested when making a bet but before the table
     * has been cleared
     * @return Bet amount
     */
    public Integer getBetAmt() {
        AMoneyManager money = this.monies.get(Seat.YOU);
        
        Integer amt = money.getWager();
        
        return amt;
    }
    
    /**
     * Gets the side bet amount on the table.
     * @return Side bet amount
     */
    public Integer getSideAmt() {
        int amt = 0;
        
        if(this.sideBetView != null)
            amt = this.sideBetView.getAmt();
        
        LOG.info("side bet = "+amt);
        
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
        
        // Render the side bet
        if(sideBetView != null)
            sideBetView.render(g);

        // Render the burn card
        if(burnCard.isVisible())
            burnCard.render(g);
        
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
        
        // Update the side bet
        if(sideBetView != null)
            sideBetView.update(); 
        
        // If it's my turn, I didn't break, and my cards have landed,
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
        
        burnCard.update();
       
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
     * Double bet on the table.
     *
     * @param hid Hand id
     */
    public void dubble(Hid hid) {
        AMoneyManager money = this.monies.get(hid.getSeat());

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
            // Reveal dealer's hole card
            hand.get(0).flip();
            
            // Inform gety since we bypassed sending this to gerty during the deal
            // This is really only important at this stage for counting cards.
            if(gerty != null)
                gerty.deal(hid, holeCard, holeValues);
            
            // Disable the "turn" signal
            // Note: "turn" will be null on dealer blackjack in which case
            // nobody has played.
            if (turn != null)
                turn.enablePlaying(false);
            
            // Disable player input
            this.frame.enableTrucking(false);
            this.frame.enablePlay(false);
        } else {           
            // Disable old hand
            if (turn != null)
                turn.enablePlaying(false);

            // Enable new hand
            turn = hand;

            turn.enablePlaying(true);

            // If turn is NOT my hand, disable my hand
            boolean enable = true;

            if (hid.getSeat() != Seat.YOU) {
                enable = false;
            }

            if (gerty == null) {
                this.frame.enableTrucking(enable);
                this.frame.enablePlay(enable);
            }
            else
                gerty.play(hid);
            SoundFactory.play(Effect.TURN);
        }
    }

    /**
     * Receives a hit for a hand.
     *
     * @param hid Hand id
     * @param card Card hitting the hand
     * @param handValues Hand values
     */
    @Override
    public synchronized void deal(Hid hid, Card card, int[] handValues) {
        // Get the burn card off the table
        burnCard.clear();
        
        SoundFactory.play(Effect.DEAL);

        AHand hand = manos.get(hid);

        hand.setValues(handValues);

        // If card is null, this is not a "real" hit but only
        // updating the respective hand value.
        if (card == null)
            return;

        // Convert card to an animated card and hit the hand
        ACard acard = ACard.animate(card);

        hand.hit(acard);
        
        // Let the advisor, if it exists, know what's going on
        frame.deal(hid, card, handValues);
        
        // Let Gerty, if it exists, know what's going on except for the hole card
        // which we'll send to Gerty when it's the dealer's turn.
        if(card instanceof HoleCard) {
            this.holeValues = handValues;
            this.holeCard = card;
        }
        
        if(gerty != null && !(card instanceof HoleCard))
            gerty.deal(hid, card, handValues);
    }

    /**
     * Busts a hand.
     *
     * @param hid Hand id
     */
    @Override
    public void bust(Hid hid) {
        LOG.info("BUST for hid = "+hid+" amt = "+hid.getAmt());
        
        AHand hand = manos.get(hid);

        hand.setOutcome(AHand.Outcome.Bust);

        AMoneyManager money = this.monies.get(hid.getSeat());

        money.decrease(hid.getAmt());

        if (hid.getSeat() != Seat.DEALER) {
            SoundFactory.play(Effect.BUST);
            looserCount++;
        }
        
        if(sideBetView != null)
            sideBetView.ending(hid);
        
        if(gerty != null)
            gerty.bust(hid);
    }

    /**
     * Updates hand with winning outcome.
     *
     * @param hid Hand id
     */
    @Override
    public void win(Hid hid) {
        LOG.info("WIN for hid = "+hid+" amt = "+hid.getAmt());
        
        AHand hand = manos.get(hid);

        hand.setOutcome(AHand.Outcome.Win);

        AMoneyManager money = this.monies.get(hid.getSeat());

        money.increase(hid.getAmt());

        winnerCount++;
        
        if(sideBetView != null)
            sideBetView.ending(hid);
        
        if(gerty != null)
            gerty.win(hid);
    }

    /**
     * Updates hand with loosing outcome.
     *
     * @param hid Hand id
     */
    @Override
    public void loose(Hid hid) {
        LOG.info("LOOSE for hid = "+hid+" amt = "+hid.getAmt());
        
        AHand hand = manos.get(hid);

        hand.setOutcome(AHand.Outcome.Lose);

        AMoneyManager money = this.monies.get(hid.getSeat());

        money.decrease(hid.getAmt());

        looserCount++;
        
        if(sideBetView != null)
            sideBetView.ending(hid);
        
        if(gerty != null)
            gerty.lose(hid);
    }

    /**
     * Updates hand with push outcome.
     *
     * @param hid Hand id
     */
    @Override
    public void push(Hid hid) {
        LOG.info("PUSH for hid = "+hid+" amt = "+hid.getAmt());
        
        AHand hand = manos.get(hid);

        hand.setOutcome(AHand.Outcome.Push);

        AMoneyManager money = this.monies.get(hid.getSeat());
        
        money.increase(hid.getSideAmt());
        
        ++pushCount;
        
        if(sideBetView != null)
            sideBetView.ending(hid);
        
        if(gerty != null)
            gerty.push(hid);
    }

    /**
     * Updates hand with Blackjack outcome.
     *
     * @param hid Hand id
     */
    @Override
    public void blackjack(Hid hid) {
        LOG.info("BJ for hid = "+hid+" amt = "+hid.getAmt());
        
        AHand hand = manos.get(hid);

        hand.setOutcome(AHand.Outcome.Blackjack);

        AMoneyManager money = this.monies.get(hid.getSeat());

        money.increase(hid.getAmt());

        if (hid.getSeat() != Seat.DEALER) {
            SoundFactory.play(Effect.BJ);

            winnerCount++;
        }
        
        if(sideBetView != null)
            sideBetView.ending(hid);
        
        if(gerty != null)
            gerty.blackjack(hid);
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

        AMoneyManager money = this.monies.get(hid.getSeat());

        money.increase(hid.getAmt());

        SoundFactory.play(Effect.CHARLIE);

        winnerCount++;
        
        if(sideBetView != null)
            sideBetView.ending(hid);
        
        if(gerty != null)
            gerty.charlie(hid);
    }

    /**
     * Starts a game. Note: we received the initial player bankroll during login
     * which is handled by GameFrame.
     *
     * @param shoeSize Shoe size
     * @param hids Hand ids
     */
    @Override
    public void starting(List<Hid> hids, int shoeSize) {
        numHands = hids.size();
        
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
        
        if(gerty != null)
            gerty.startGame(hids, shoeSize);
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
        
        if (gerty == null) {
            // Enable betting and dealing again
            frame.enableDeal(true);
            this.bettable = true;

            // Disable play -- we must wait for player to bet and request deal
            frame.enablePlay(false);
        }
        else
            gerty.endGame(shoeSize);

        if (winnerCount == numHands - 1) {
            SoundFactory.play(Effect.NICE);
        } else if (looserCount == numHands - 1) {
            SoundFactory.play(Effect.TOUGH);
        } else if (pushCount == numHands - 1) {
            SoundFactory.play(Effect.PUSH);
        }
    }

    /**
     * Handles shuffling from dealer.
     */
    @Override
    public void shuffling() {
        burnCard.launch();
        
        SoundFactory.play(Effect.SHUFFLING);
        
        if(gerty != null)
            gerty.shuffling();
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
        if(gerty != null)
            return;
        
        if (!bettable)
            return;

        // Get the coordinates of the mouse and let bet manager
        // determine whether this is a bet and how much.
        int x = e.getX();
        int y = e.getY();

        // Place main bet on left-click
        if(SwingUtilities.isLeftMouseButton(e))
            monies.get(Seat.YOU).click(x, y);
        
        // Ditto for the side bet system on right-click
        if(sideBetView != null && SwingUtilities.isRightMouseButton(e))
            sideBetView.click(x, y);
    }

    /**
     * Toggles the button image from pressed to up.
     *
     * @param e
     */
    @Override
    public void mouseReleased(MouseEvent e) {
        if(gerty != null)
            return;
        
        monies.get(Seat.YOU).unclick();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
    
    /**
     * Loads the side bet rule.
     */
    protected void loadConfig() { 
        try {
            // Open the configuration file
            props = new Properties();
            props.load(new FileInputStream("charlie.props"));
            
            // Get the side bet view
            String className = props.getProperty(SIDE_BET_VIEW_PROPERTY);

            if (className == null)
                return;
 
            Class<?> clazz;
            clazz = Class.forName(className);

            this.sideBetView = (ISideBetView) clazz.newInstance();
            
            this.sideBetView.setMoneyManager(this.monies.get(Seat.YOU));

            LOG.info("successfully loaded side bet rule");
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IOException ex) {
            LOG.error("side bet view failed to load: " + ex);
        }
    }    
}
