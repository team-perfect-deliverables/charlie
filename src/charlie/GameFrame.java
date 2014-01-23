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
package charlie;

import charlie.card.Hid;
import charlie.actor.Courier;
import charlie.audio.SoundFactory;
import charlie.card.Card;
import charlie.card.Hand;
import charlie.dealer.Seat;
import charlie.message.view.from.Arrival;
import charlie.plugin.Play;
import charlie.plugin.IAdvisor;
import charlie.server.Login;
import charlie.server.Ticket;
import charlie.view.ATable;
import com.googlecode.actorom.Actor;
import com.googlecode.actorom.Address;
import com.googlecode.actorom.Topology;
import com.googlecode.actorom.remote.ClientTopology;
import com.googlecode.actorom.remote.ServerTopology;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements the main game frame.
 * @author Ron Coleman
 */
public class GameFrame extends javax.swing.JFrame {
    static {
        Properties props = System.getProperties();
        props.setProperty("org.slf4j.simpleLogger.logFile", "System.out");
        props.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "info");
    }
    protected final Logger LOG = LoggerFactory.getLogger(GameFrame.class);
    protected final String MY_HOST = "127.0.0.1";
    protected final Integer MY_PORT = 2345;
    protected final String GAME_SERVER = "127.0.0.1";
    protected final Integer GAME_SERVER_PORT = 9000;
    protected final Integer HOUSE_PORT = 1234;
    protected Actor house;
    protected Courier courier;
    protected ATable panel;
    protected boolean connected = false;
    protected final String COURIER_ACTOR = "CHANNEL";
    protected final String ADVISOR_PROPERTY = "charlie.advisor";
    protected Topology serverTopology;
    protected Topology clientTopology;
    protected final List<Hid> hids = new ArrayList<>();
    protected final HashMap<Hid,Hand> hands = new HashMap<>();
    protected int handIndex = 0;
    protected boolean trucking = false;
    protected boolean dubblable;
    protected IAdvisor advisor;
    protected Hand dealerHand;

    /**
     * Constructor
     */
    public GameFrame() {
        initComponents();

        init();
    }

    /**
     * Initializes the frame.
     */
    protected final void init() {
        panel = new ATable(this, this.surface);

        surface.add(panel);

        this.setLocationRelativeTo(null);

        enableDeal(false);

        enablePlay(false);

        loadAdvisor();
    }
    
    protected void loadAdvisor() {
        try {
            Properties props = new Properties();
            
            props.load(new FileInputStream("charlie.props"));
            
            String className = props.getProperty(ADVISOR_PROPERTY);

            if (className == null)
                return;
             
            LOG.info("attempting to load advisor "+ADVISOR_PROPERTY);
            Class<?> clazz;
            clazz = Class.forName(className);

            this.advisor = (IAdvisor) clazz.newInstance();
            
            LOG.info("successfully loaded advisor");              
        } catch (FileNotFoundException ex) {
            LOG.error(ex.toString());
        } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            LOG.error(ex.toString());
        }
    }
    
    /**
     * Confirm play with advisor
     * @param hid
     * @param play
     * @return True if advising, false otherwise
     */
    protected boolean confirmed(Hid hid,Play play) {
        if(!this.adviseCheckBox.isSelected() || advisor == null || dealerHand.size() < 2)
            return true;
        
        Hand myHand = hands.get(hid);

        Play advice = advisor.advise(myHand,dealerHand.getCard(1));

        if (this.adviseCheckBox.isSelected() && advice != play) {
            Object[] options = {
                play,
                "Cancel"};
            String msg = "<html><font color=\"blue\" size=\"6\">This is some text!I suggest " + advice + ".</font>";
            int n = JOptionPane.showOptionDialog(this,
                    msg,
                    "Advisor",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null,
                    options,
                    options[1]);

            if (n == 1) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * Connects local courier to remote courier (on server).
     *
     * @param panel Panel courier perceives.
     * @return True if connected, false if connect attempt fails.
     */
    private boolean connect(ATable panel) {
        try {
            // Login to the server to get the house address
            Socket client = new Socket(GAME_SERVER, GAME_SERVER_PORT);
            LOG.info("opened socket to game server " + GAME_SERVER + ":" + GAME_SERVER_PORT);

            OutputStream os = client.getOutputStream();

            ObjectOutputStream oos = new ObjectOutputStream(os);

            oos.writeObject(new Login("abc", "def"));

            Ticket ticket;
            try (InputStream is = client.getInputStream(); ObjectInputStream ois = new ObjectInputStream(is)) {
                ticket = (Ticket) ois.readObject();
            }

            // Get the house actor
            Address addr = ticket.getHouse();

            LOG.info("got house addr = " + addr);

            clientTopology = new ClientTopology(GAME_SERVER, HOUSE_PORT, 5, TimeUnit.SECONDS, 3, TimeUnit.SECONDS);

            house = clientTopology.getActor(addr);
            LOG.info("got house actor");

            // Connect the courier to its ghost surrogate
            courier = new Courier(panel);

            serverTopology = new ServerTopology(MY_HOST, MY_PORT);

            Address me = serverTopology.spawnActor(COURIER_ACTOR, courier);
            LOG.info("spawned my addr = " + me);

            courier.setMyAddress(me);

            // Sending this message causes the house to spawn a ghost
            // which if all goes well sends us a connect message which we wait for.
            house.send(new Arrival(ticket, me));
            LOG.info("sent ARRIVAL to " + house);

            // Wait for the acknowledgement from the surrogate
            synchronized (panel) {
                try {
                    panel.wait(5000);

                    Double bankroll = ticket.getBankroll();

                    panel.setBankroll(bankroll);

                    LOG.info("connected to channel bankroll = " + bankroll);

                } catch (InterruptedException ex) {
                    LOG.info("failed to connect to channel: " + ex);

                    failOver();

                    return false;
                }
            }

//            clientTopology.shutdown();

            return true;
        } catch (IOException | ClassNotFoundException e) {
            LOG.info("failed to connect to server: " + e);

            return false;
        }
    }
    
    public void deal(Hid hid, Card card, int[] handValues) {      
        Hand hand = hands.get(hid);
        
        if(hand == null) {
            hand = new Hand(hid);
            
            hands.put(hid, hand);
            
            if(hid.getSeat() == Seat.DEALER)
                this.dealerHand = hand;
        }
            
        hand.hit(card);
    }

    public void enableDeal(boolean deal) {
        this.dealButton.setEnabled(deal);

        this.panel.enableBetting(deal);

        this.hitButton.setEnabled(false);

        this.stayButton.setEnabled(false);

        this.splitButton.setEnabled(false);

        this.ddownButton.setEnabled(false);
    }

    public void enablePlay(boolean playing) {
        this.hitButton.setEnabled(playing && trucking);

        this.stayButton.setEnabled(playing && trucking);

        this.ddownButton.setEnabled(playing && dubblable && trucking);
    }

    public void enableTrucking(boolean trucking) {
        this.trucking = trucking;
    }

    protected void failOver() {
        if (serverTopology != null) {
            serverTopology.shutdown();
        }

        if (clientTopology != null) {
            clientTopology.shutdown();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        surface = new javax.swing.JPanel();
        accessButton = new javax.swing.JButton();
        dealButton = new javax.swing.JButton();
        hitButton = new javax.swing.JButton();
        stayButton = new javax.swing.JButton();
        ddownButton = new javax.swing.JButton();
        splitButton = new javax.swing.JButton();
        adviseCheckBox = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        org.jdesktop.layout.GroupLayout surfaceLayout = new org.jdesktop.layout.GroupLayout(surface);
        surface.setLayout(surfaceLayout);
        surfaceLayout.setHorizontalGroup(
            surfaceLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 0, Short.MAX_VALUE)
        );
        surfaceLayout.setVerticalGroup(
            surfaceLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 401, Short.MAX_VALUE)
        );

        accessButton.setText("Login");
        accessButton.setToolTipText("");
        accessButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                accessButtonActionPerformed(evt);
            }
        });

        dealButton.setText("Deal");
        dealButton.setActionCommand(" Bet ");
        dealButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dealButtonActionPerformed(evt);
            }
        });

        hitButton.setText(" Hit ");
        hitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hitButtonActionPerformed(evt);
            }
        });

        stayButton.setText(" Stay");
        stayButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stayButtonActionPerformed(evt);
            }
        });

        ddownButton.setText("DDown");
        ddownButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ddownButtonActionPerformed(evt);
            }
        });

        splitButton.setText("Split");

        adviseCheckBox.setText("Advise");
        adviseCheckBox.setEnabled(false);
        adviseCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                adviseCheckBoxActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(surface, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(adviseCheckBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 244, Short.MAX_VALUE)
                        .add(splitButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(ddownButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(hitButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(stayButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(dealButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(accessButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(surface, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(accessButton)
                    .add(dealButton)
                    .add(hitButton)
                    .add(stayButton)
                    .add(ddownButton)
                    .add(splitButton)
                    .add(adviseCheckBox))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void accessButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_accessButtonActionPerformed
        final GameFrame frame = this;
        if (!connected) {
            frame.accessButton.setEnabled(false);
            
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    connected = frame.connect(panel);

                    if (connected) {
                        // Prime the audio player
                        SoundFactory.prime();

                        JOptionPane.showMessageDialog(frame,
                                "Successfully connected to server.",
                                "Status",
                                JOptionPane.INFORMATION_MESSAGE);

                        frame.accessButton.setText("Logout");

                        frame.enableDeal(true);
                        
                        if(advisor != null)
                            frame.adviseCheckBox.setEnabled(true);
                    } else {
                        JOptionPane.showMessageDialog(frame,
                                "Failed to connect to server.",
                                "Status",
                                JOptionPane.ERROR_MESSAGE);
                    }

                    frame.accessButton.setEnabled(true);
                }
            });
        } else {
            System.exit(0);
        }
    }//GEN-LAST:event_accessButtonActionPerformed

    private void dealButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dealButtonActionPerformed
        hids.clear();
        
        hands.clear();

        this.handIndex = 0;

        this.dubblable = true;

        final GameFrame frame = this;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // Clear out old bets, stats, etc.
                frame.panel.clear();
                
                Integer amt = frame.panel.getBetAmt();

                if (amt <= 0) {
                    JOptionPane.showMessageDialog(frame,
                            "Minimum bet is $5.",
                            "Status",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Get player side wager on table
                Integer sideAmt = frame.panel.getSideAmt();

                // Tell courier to send bet to dealer which gives us a myHand id
                // since bets are only associated with hands
                Hid hid = courier.bet(amt, sideAmt);

                hids.add(hid);
                
                hands.put(hid, new Hand(hid));

                enableDeal(false);
            }
        });

    }//GEN-LAST:event_dealButtonActionPerformed

    private void stayButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stayButtonActionPerformed
        final GameFrame frame = this;

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Hid hid = hids.get(frame.handIndex);

                if (!confirmed(hid, Play.STAY))
                    return;

                courier.stay(hids.get(frame.handIndex));
                enableTrucking(false);
                enablePlay(false);
            }
        });        
    }//GEN-LAST:event_stayButtonActionPerformed

    private void hitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hitButtonActionPerformed
        final GameFrame frame = this;
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Hid hid = hids.get(frame.handIndex);
                
                if(!confirmed(hid,Play.HIT))
                    return;
                
                // NOTE: this isables double down on all hids and will have to be
                // fixed when splitting hids
                frame.dubblable = false;

                // Disable play until the card arrives
                enablePlay(false);

                courier.hit(hid);
            }
        });

    }//GEN-LAST:event_hitButtonActionPerformed

    private void ddownButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ddownButtonActionPerformed
       final GameFrame frame = this;

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Hid hid = hids.get(frame.handIndex);

                if (!confirmed(hid, Play.DOUBLE_DOWN))
                    return;

                // Disable further playing since this is ouble-down
                enableTrucking(false);

                enablePlay(false);

                // No further dubbling until the next bet made
                dubblable = false;

                // Double the bet in the myHand using a copy since this
                // is a transient bet.
                hid.dubble();

                // Send this off to the dealer
                courier.dubble(hid);

                // Double the bet on the panel
                panel.dubble(hid);
            }
        });
    }//GEN-LAST:event_ddownButtonActionPerformed

    private void adviseCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_adviseCheckBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_adviseCheckBoxActionPerformed

    /**
     * Main starting point of app.
     *
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(GameFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new GameFrame().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton accessButton;
    private javax.swing.JCheckBox adviseCheckBox;
    private javax.swing.JButton ddownButton;
    private javax.swing.JButton dealButton;
    private javax.swing.JButton hitButton;
    private javax.swing.JButton splitButton;
    private javax.swing.JButton stayButton;
    private javax.swing.JPanel surface;
    // End of variables declaration//GEN-END:variables
}
