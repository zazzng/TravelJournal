package tj;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class TJEventListener implements MouseListener, MouseMotionListener, KeyListener {
    // fields
    private TJ mTJ = null;
    public TJ getTJ() {
        return this.mTJ;
    }
    
    // constructor
    public TJEventListener(TJ tj) {
        this.mTJ = tj;
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {
        TJScene curScene = (TJScene)this.mTJ.getScenarioMgr().getCurScene();
        curScene.handleMousePress(e);
        this.mTJ.getCanvas2D().repaint();
    }
    
    @Override
    public void mouseDragged(MouseEvent e) {
        TJScene curScene = (TJScene)this.mTJ.getScenarioMgr().getCurScene();
        curScene.handleMouseDrag(e);
        this.mTJ.getCanvas2D().repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        TJScene curScene = (TJScene)this.mTJ.getScenarioMgr().getCurScene();
        curScene.handleMouseRelease(e);
        this.mTJ.getCanvas2D().repaint();
    }

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        TJScene curScene = (TJScene)this.mTJ.getScenarioMgr().getCurScene();
        curScene.handleKeyDown(e);
        this.mTJ.getCanvas2D().repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        TJScene curScene = (TJScene)this.mTJ.getScenarioMgr().getCurScene();
        curScene.handleKeyUp(e);
        this.mTJ.getCanvas2D().repaint();
    }
}
