package tj.scenario;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import tj.TJ;
import tj.TJScene;
import tj.Command.TJCmdToChangeColorOfSelectedPtCurves;
import tj.Command.TJCmdToDeselectSelectedPtCurves;
import tj.Command.TJCmdToSetStartScreenPt;
import x.XApp;
import x.XCmdToChangeScene;
import x.XScenario;

public class TJColorScenario extends XScenario {
    // singleton pattern
    private static TJColorScenario mSingleton = null;
    public static TJColorScenario getSingle() {
        assert(TJColorScenario.mSingleton != null);
        return TJColorScenario.mSingleton;
    }
    public static TJColorScenario createSingleton(XApp app) {
        assert(TJColorScenario.mSingleton == null);
        TJColorScenario.mSingleton = new TJColorScenario(app);
        return TJColorScenario.mSingleton;
    }
    private TJColorScenario(XApp app) {
        super(app);
    }

    @Override
    protected void addScenes() {
        this.addScene(TJColorScenario.ColorReadyScene.createSingleton(this));
        this.addScene(TJColorScenario.ColorChangeScene.createSingleton(this));
    }
    
    public ColorChangeScene getColorChangeScene() {
        return ColorChangeScene.getSingleton();
    }

    public static class ColorReadyScene extends TJScene {
        // singleton pattern
        private static ColorReadyScene mSingleton = null;
        public static ColorReadyScene getSingleton() {
            assert(ColorReadyScene.mSingleton != null);
            return ColorReadyScene.mSingleton;
        }
        public static ColorReadyScene createSingleton(XScenario scenario) {
            assert(ColorReadyScene.mSingleton == null);
            ColorReadyScene.mSingleton = new ColorReadyScene(scenario);
            return ColorReadyScene.mSingleton;
        }
        private ColorReadyScene(XScenario scenario) {
            super(scenario);
        }

        @Override
        public void handleMousePress(MouseEvent e) {
            TJ tj = (TJ)this.mScenario.getApp();
            
            XCmdToChangeScene.execute(tj,
                TJColorScenario.ColorChangeScene.getSingleton(),
                this.getReturnScene());
        }

        @Override
        public void handleMouseDrag(MouseEvent e) {
        }

        @Override
        public void handleMouseRelease(MouseEvent e) {
        }

        @Override
        public void handleKeyDown(KeyEvent e) {
           
        }

        @Override
        public void handleKeyUp(KeyEvent e) {
            TJ tj = (TJ)this.mScenario.getApp();
            int code = e.getKeyCode();
            
            switch (code) {
                case KeyEvent.VK_C:
                    XCmdToChangeScene.execute(tj, this.getReturnScene(), null);
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
    
    public static class ColorChangeScene extends TJScene {
        // singleton pattern
        private static ColorChangeScene mSingleton = null;
        public static ColorChangeScene getSingleton() {
            assert(ColorChangeScene.mSingleton != null);
            return ColorChangeScene.mSingleton;
        }
        public static ColorChangeScene createSingleton(XScenario scenario) {
            assert(ColorChangeScene.mSingleton == null);
            ColorChangeScene.mSingleton = new ColorChangeScene(scenario);
            return ColorChangeScene.mSingleton;
        }
        private ColorChangeScene(XScenario scenario) {
            super(scenario);
        }

        @Override
        public void handleMousePress(MouseEvent e) {
        }

        @Override
        public void handleMouseDrag(MouseEvent e) {
        }

        @Override
        public void handleMouseRelease(MouseEvent e) {
            TJ tj = (TJ)this.mScenario.getApp();
            Point pt = e.getPoint();
            Color c = tj.getColorChooser().calcColor(pt, 
                tj.getCanvas2D().getWidth(), tj.getCanvas2D().getHeight());
            
            if (c != null) {
                // Set as default color for new curves in PtCurveMgr
                tj.getPtCurveMgr().setDefaultColor(c);
                // Also update the canvas's current color for new curves
                tj.getCanvas2D().setCurColorForPtCurve(c);
            }
            
            XCmdToChangeScene.execute(tj,
                TJDefaultScenario.ReadyScene.getSingleton(), null);
        }

        @Override
        public void handleKeyDown(KeyEvent e) {
           
        }

        @Override
        public void handleKeyUp(KeyEvent e) {
            TJ tj = (TJ)this.mScenario.getApp();
            int code = e.getKeyCode();
            
            switch (code) {
                case KeyEvent.VK_C:
                    XCmdToChangeScene.execute(tj, this.getReturnScene(), null);
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
            TJ tj = (TJ)this.mScenario.getApp();
            tj.getColorChooser().drawCells(g2, 
                tj.getCanvas2D().getWidth(), 
                tj.getCanvas2D().getHeight());
        }

        @Override
        public void getReady() {
        }

        @Override
        public void wrapUp() {
        }
    }
    
}
