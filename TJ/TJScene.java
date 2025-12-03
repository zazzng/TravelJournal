package TJ;

import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import java.awt.Graphics2D;

import X.XScenario;
import X.XScene;

public abstract class TJScene extends XScene{
    protected TJScene(XScenario scenario) {
        super(scenario);
    }

    //Event handling abstract methods
    public abstract void handleMousePress(MouseEvent e);
    public abstract void handleMouseDrag(MouseEvent e);
    public abstract void handleMouseRelease(MouseEvent e);
    public abstract void handleKeyDown(KeyEvent e);
    public abstract void handleKeyUp(KeyEvent e);

    public abstract void updateSupportObjects();
    public abstract void renderWorldObjects(Graphics2D g2);
    public abstract void renderSceenObjects(Graphics2D g2);
}
