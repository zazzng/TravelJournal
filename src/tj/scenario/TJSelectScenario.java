package tj.scenario;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import tj.TJ;
import tj.TJScene;
import x.XApp;
import x.XScenario;

public class TJSelectScenario extends XScenario {
    // singleton pattern
    private static TJSelectScenario mSingleton = null;
    public static TJSelectScenario getSingle() {
        assert(TJSelectScenario.mSingleton != null);
        return TJSelectScenario.mSingleton;
    }
    public static TJSelectScenario createSingleton(XApp app) {
        assert(TJSelectScenario.mSingleton == null);
        TJSelectScenario.mSingleton = new TJSelectScenario(app);
        return TJSelectScenario.mSingleton;
    }
    private TJSelectScenario(XApp app) {
        super(app);
    }

    @Override
    protected void addScenes() {
        this.addScene(TJSelectScenario.SelectReadyScene.createSingleton(this));
    }

    public static class SelectReadyScene extends TJScene {
        // UI Components
        private JPanel mTopNavPanel;
        
        private JPanel mBottomNavPanel;
        
        // singleton pattern
        private static SelectReadyScene mSingleton = null;
        public static SelectReadyScene getSingleton() {
            assert(SelectReadyScene.mSingleton != null);
            return SelectReadyScene.mSingleton;
        }
        public static SelectReadyScene createSingleton(XScenario scenario) {
            assert(SelectReadyScene.mSingleton == null);
            SelectReadyScene.mSingleton = new SelectReadyScene(scenario);
            return SelectReadyScene.mSingleton;
        }
        private SelectReadyScene(XScenario scenario) {
            super(scenario);
        }
        
        private void initializeTopNav() {
            
        }
        
        private void initializeBottomNav() {
            
        }

        @Override
        public void handleMousePress(MouseEvent e) {
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
//            tj.setTopPanel(null);
//            tj.setBottomPanel(null);
        }

        @Override
        public void drawBackground(Graphics2D g2) {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }
    }
}
