/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charlie.view;

import charlie.card.Card;
import charlie.message.view.from.Request;
import java.awt.Graphics2D;

/**
 *
 * @author roncoleman125
 */
public interface IAdvisor {
    public void observe(Card card);
    public Double getBetAdvice(Double minBet,Double base);
    public Request getPlayAdvice();
    public void reset();
}
