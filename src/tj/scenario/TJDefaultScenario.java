package tj.scenario;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import tj.TJ;
import tj.TJCanvas2D;
import tj.TJScene;
import tj.Command.TJCmdToCreateCurPtCurve;
import tj.Command.TJCmdToHome;
import tj.Command.TJCmdToIncreaseStrokeWidthForCurPtCurve;
import x.XApp;
import x.XCmdToChangeScene;
import x.XScenario;

public class TJDefaultScenario extends XScenario {
    // singleton pattern
    private static TJDefaultScenario mSingleton = null;
    public static TJDefaultScenario getSingle() {
        assert(TJDefaultScenario.mSingleton != null);
        return TJDefaultScenario.mSingleton;
    }
    public static TJDefaultScenario createSingleton(XApp app) {
        assert(TJDefaultScenario.mSingleton == null);
        TJDefaultScenario.mSingleton = new TJDefaultScenario(app);
        return TJDefaultScenario.mSingleton;
    }
    private TJDefaultScenario(XApp app) {
        super(app);
    }

    @Override
    protected void addScenes() {
        this.addScene(TJDefaultScenario.ReadyScene.createSingleton(this));
    }

    public static class ReadyScene extends TJScene {
        // singleton pattern
        private static ReadyScene mSingleton = null;
        public static ReadyScene getSingleton() {
            assert(ReadyScene.mSingleton != null);
            return ReadyScene.mSingleton;
        }
        public static ReadyScene createSingleton(XScenario scenario) {
            assert(ReadyScene.mSingleton == null);
            ReadyScene.mSingleton = new ReadyScene(scenario);
            return ReadyScene.mSingleton;
        }
        private ReadyScene(XScenario scenario) {
            super(scenario);
        }

        @Override
        public void handleMousePress(MouseEvent e) {
            TJ tj = (TJ)this.mScenario.getApp();
            Point pt = e.getPoint();
            TJCmdToCreateCurPtCurve.execute(tj, pt);
            
            XCmdToChangeScene.execute(tj,
                TJDrawScenario.DrawScene.getSingleton(), this);
        }

        @Override
        public void handleMouseDrag(MouseEvent e) {
        }

        @Override
        public void handleMouseRelease(MouseEvent e) {
        }

        @Override
        public void handleKeyDown(KeyEvent e) {
            TJ tj = (TJ)this.mScenario.getApp();
            int code = e.getKeyCode();
            
            switch (code) {
                case KeyEvent.VK_SHIFT:
                    XCmdToChangeScene.execute(tj,
                        TJSelectScenario.SelectReadyScene.getSingleton(), this);
                    break;
                case KeyEvent.VK_CONTROL:
                    XCmdToChangeScene.execute(tj,
                        TJNavigateScenario.PanReadyScene.getSingleton(), this);
                    break;
                case KeyEvent.VK_ALT:
                    XCmdToChangeScene.execute(tj,
                        TJNavigateScenario.ZoomRotateReadyScene.getSingleton(), this);
                    break;
                case KeyEvent.VK_C:
                    XCmdToChangeScene.execute(tj,
                        TJColorScenario.ColorReadyScene.getSingleton(), this);
                    break;
                case KeyEvent.VK_UP:
                    TJCmdToIncreaseStrokeWidthForCurPtCurve.execute(tj,
                        TJCanvas2D.STROKE_WIDTH_INCREMENT);
                    break;
                case KeyEvent.VK_DOWN:
                    TJCmdToIncreaseStrokeWidthForCurPtCurve.execute(tj,
                        -TJCanvas2D.STROKE_WIDTH_INCREMENT);
                    break;
            }
        }

        @Override
        public void handleKeyUp(KeyEvent e) {
            TJ tj = (TJ)this.mScenario.getApp();
            int code = e.getKeyCode();
            
            switch (code) {
                case KeyEvent.VK_SPACE:
                    TJCmdToHome.execute(tj);
                    break;
            }
        }

        @Override
        public void updateSupportObjects() {
        }

        @Override
        public void renderWorldObjects(Graphics2D g2) {
        }

        @Override
        public void renderScreenObjects(Graphics2D g2) {
        }

        @Override
        public void getReady() {
        }

        @Override
        public void wrapUp() {
        }
    }
    
}
