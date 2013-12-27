/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charlie.view.sprite;

import charlie.util.Constant;
import java.awt.Graphics2D;
import javax.swing.ImageIcon;

/**
 *
 * @author roncoleman125
 */
public class TurnSprite extends Sprite {  
    private boolean visible = false;
    
    public TurnSprite() {
        String path = Constant.DIR_IMGS + "arrow-180-1.png";
        
        ImageIcon icon = new ImageIcon(path);

        img = icon.getImage();
    }
    
    @Override
    public void render(Graphics2D g) {
        if(visible)
            super.render(g);
    }
    
    public void show(boolean b) {
        this.visible = b;
    }
}
