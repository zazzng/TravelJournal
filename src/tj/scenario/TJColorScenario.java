package tj.scenario;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import tj.Command.TJCmdToDeselectSelectedPtCurves;
import tj.TJ;
import tj.TJCanvas2D;
import tj.TJScene;
import tj.cmd.TJCmdToChangeColorOfSelectedPtCurves;
import utils.TJNavPanel;
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
        this.addScene(TJColorScenario.ColorChangeScene.createSingleton(this));
    }
    
    public static class ColorChangeScene extends TJScene {
        // UI Components
        private JPanel mTopNavPanel;
        private JPanel mBottomNavPanel;
        
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
        
        private void initializeTopNav() {
            TJ tj = (TJ)this.mScenario.getApp();
            String title = "Untitled Journal";
            if (tj.getJournalBookMgr().getCurBook() != null) {
                title = tj.getJournalBookMgr().getCurBook().getTitle();
            }
            
            this.mTopNavPanel = TJNavPanel.createTopNavPanel(tj, title);
        }
        
        private void initializeBottomNav() {
            TJ tj = (TJ)this.mScenario.getApp();
            this.mBottomNavPanel = TJNavPanel.createBottomNavPanel(tj, this);
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
                // if curves are selected
                if (!tj.getPtCurveMgr().getSelectedPtCurves().isEmpty()) {
                    TJCmdToChangeColorOfSelectedPtCurves.execute(tj, c);
                    TJCmdToDeselectSelectedPtCurves.execute(tj);
                    
                } else {
                    tj.getCanvas2D().setCurColorForPtCurve(c);
                    // TODO
                    // TJCmdToChangeColorForNewPtCurveTo.execute(tj, c); 
                }
            }
            
            XCmdToChangeScene.execute(tj,
                TJDrawScenario.DrawReadyScene.getSingleton(), null);
        }

        @Override
        public void handleKeyDown(KeyEvent e) {
           
        }

        @Override
        public void handleKeyUp(KeyEvent e) {
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
            TJCanvas2D canvas = tj.getCanvas2D();
            
            tj.getColorChooser().drawCells(g2, canvas.getWidth(), canvas.getHeight());
            
            canvas.drawPenTip(g2);
        }

        @Override
        public void getReady() {
            TJ tj = (TJ)this.mScenario.getApp();
            
            if (this.mTopNavPanel == null) {
                initializeTopNav();
            }
            if (this.mBottomNavPanel == null) {
                initializeBottomNav();
            }
            
            tj.setTopPanel(this.mTopNavPanel);
            tj.setBottomPanel(this.mBottomNavPanel);
        }

        @Override
        public void wrapUp() {
            TJ tj = (TJ)this.mScenario.getApp();
            tj.setTopPanel(null);
            tj.setBottomPanel(null);
        }

        @Override
        public void drawBackground(Graphics2D g2) {
            TJ tj = (TJ)this.mScenario.getApp();
            TJCanvas2D canvas = tj.getCanvas2D();
            
            g2.setColor(TJCanvas2D.COLOR_BACKGROUND_DARK); 
            g2.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        }
    }
}
