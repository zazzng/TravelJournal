package tj;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import tj.scenario.TJColorScenario;
import tj.scenario.TJDefaultScenario;
import x.XCmdToChangeScene;

public class TJEventListener implements MouseListener, MouseMotionListener, KeyListener {
    private TJ mTJ = null;
    public TJ getTJ() {
        return this.mTJ;
    }
    
    // constructor
    public TJEventListener(TJ tj) {
        this.mTJ = tj;
    }
    
    @Override
    public void mousePressed(MouseEvent e) {
        if (this.mTJ.getPenMarkMgr().handleMousePress(e)) {
            System.out.println("Mouse pressed");
            TJScene curScene = (TJScene)this.mTJ.getScenarioMgr().getCurScene();
            curScene.handleMousePress(e);
            this.mTJ.getCanvas2D().repaint();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (this.mTJ.getPenMarkMgr().handleMouseDrag(e)) {
            TJScene curScene = (TJScene)this.mTJ.getScenarioMgr().getCurScene();
            curScene.handleMouseDrag(e);
            this.mTJ.getCanvas2D().repaint();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (this.mTJ.getPenMarkMgr().handleMouseRelease(e)) {
            TJScene curScene = (TJScene)this.mTJ.getScenarioMgr().getCurScene();
            curScene.handleMouseRelease(e);
            this.mTJ.getCanvas2D().repaint();
        }
    }

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

    @Override
    public void mouseClicked(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

        int panelW = mTJ.getCanvas2D().getWidth();
        int panelH = mTJ.getCanvas2D().getHeight();

        if (mTJ.getCanvas2D().getColorButton().clicked(x, y, panelW, panelH)) {
            System.out.println("Color button clicked");

            TJScene curScene = (TJScene)mTJ.getScenarioMgr().getCurScene();

            if (curScene instanceof TJColorScenario.ColorChangeScene) {
                XCmdToChangeScene.execute(
                    mTJ,
                    TJDefaultScenario.ReadyScene.getSingleton(),
                    null
                );
            } else {
                XCmdToChangeScene.execute(
                    mTJ,
                    TJColorScenario.getSingle().getColorChangeScene(),
                    TJDefaultScenario.ReadyScene.getSingleton()
                );
            }

            mTJ.getCanvas2D().repaint();
            return;
        }
    }


    @Override
    public void mouseEntered(MouseEvent e) {
        //
    }

    @Override
    public void mouseExited(MouseEvent e) {
        //
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        //
    }

    @Override
    public void keyTyped(KeyEvent e) {
        //
    }
}
