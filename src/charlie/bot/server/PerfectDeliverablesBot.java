package charlie.bot.server;

import charlie.advisor.Advisor;
import charlie.card.Card;
import charlie.card.Hand;
import charlie.card.Hid;
import charlie.dealer.Dealer;
import charlie.dealer.Seat;
import charlie.plugin.IBot;
import charlie.util.Constant;
import charlie.util.Play;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jacob
 */
public class PerfectDeliverablesBot implements IBot
{

    private static final Advisor adviser = new Advisor();
    private Hid _hid;
    private Hand _hand;
    private Seat _seat;
    private Dealer _dealer;
    private Card _upCard;
    private Boolean _turn = false;

    private void doPlay()
    {
        final IBot myBot = this;
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Play toPlay = adviser.advise(_hand, _upCard);
                System.out.println("**************************************************: " + toPlay);
                try
                {
                    Random random = new Random();
                    Thread.sleep(random.nextInt(2000) + 1000);
                } catch (InterruptedException ex)
                {
                    Logger.getLogger(PerfectDeliverablesBot.class.getName()).log(Level.SEVERE, null, ex);
                }

                if (toPlay == Play.HIT)
                {
                    _dealer.hit(myBot, _hid);
                }
                else if (toPlay == Play.STAY)
                {
                    _dealer.stay(myBot, _hid);
                }
                else if (toPlay == Play.DOUBLE_DOWN)
                {
                    _dealer.doubleDown(myBot, _hid);
                    _turn = false;
                }
            }
        }).start();
    }

    @Override
    public void play(Hid hid)
    {
        _turn = true;
        //If it is our hand, play
        if (hid.getSeat() == _seat)
        {
            System.out.println("HUH--==================================================================");
            doPlay();
        }
    }

    @Override
    public Hand getHand()
    {
        return _hand;
    }

    @Override
    public void setDealer(Dealer dealer)
    {
        _dealer = dealer;
    }

    @Override
    public void sit(Seat seat)
    {
        _seat = seat;
        _hid = new Hid(_seat, Constant.BOT_MIN_BET, 0);
        _hand = new Hand(_hid);
    }

    @Override
    public void deal(Hid hid, Card card, int[] values)
    {

        //If it is the dealer's up card, save it
        if (hid.getSeat() == Seat.DEALER)
        {
            _upCard = card;
        }

        //If the card is mine
        if (hid.getSeat() == _seat)
        {
            //If my hand is not broke, and it is my turn, play
            if (!_hand.isBroke() && _turn)
            {
                System.out.println("WHATTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTt");
                doPlay();
            }
        }
        else
        {
            _turn = false;
        }
    }

    @Override
    public void insure()
    {

    }

    @Override
    public void bust(Hid hid)
    {

    }

    @Override
    public void win(Hid hid)
    {

    }

    @Override
    public void blackjack(Hid hid)
    {

    }

    @Override
    public void charlie(Hid hid)
    {

    }

    @Override
    public void lose(Hid hid)
    {

    }

    @Override
    public void push(Hid hid)
    {

    }

    @Override
    public void shuffling()
    {

    }

    @Override
    public void startGame(List<Hid> hids, int shoeSize)
    {

    }

    @Override
    public void endGame(int shoeSize)
    {

    }
}
