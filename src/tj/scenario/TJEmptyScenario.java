package tj.scenario;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import tj.TJ;
import tj.TJCanvas2D;
import tj.TJScene;
import x.XApp;
import x.XScenario;

public class TJEmptyScenario extends XScenario {
    // singleton pattern
    private static TJEmptyScenario mSingleton = null;
    public static TJEmptyScenario getSingle() {
        assert(TJEmptyScenario.mSingleton != null);
        return TJEmptyScenario.mSingleton;
    }
    public static TJEmptyScenario createSingleton(XApp app) {
        assert(TJEmptyScenario.mSingleton == null);
        TJEmptyScenario.mSingleton = new TJEmptyScenario(app);
        return TJEmptyScenario.mSingleton;
    }
    private TJEmptyScenario(XApp app) {
        super(app);
    }

    @Override
    protected void addScenes() {
        this.addScene(TJEmptyScenario.EmptyScene.createSingleton(this));
    }

    public static class EmptyScene extends TJScene {
        // UI Components
        private JPanel mTopNavPanel;
        
        private JPanel mBottomNavPanel;
        
        // singleton pattern
        private static EmptyScene mSingleton = null;
        public static EmptyScene getSingleton() {
            assert(EmptyScene.mSingleton != null);
            return EmptyScene.mSingleton;
        }
        public static EmptyScene createSingleton(XScenario scenario) {
            assert(EmptyScene.mSingleton == null);
            EmptyScene.mSingleton = new EmptyScene(scenario);
            return EmptyScene.mSingleton;
        }
        private EmptyScene(XScenario scenario) {
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
        public void drawBackground(Graphics2D g2) {
            TJ tj = (TJ)this.mScenario.getApp();
            TJCanvas2D canvas = tj.getCanvas2D();
            
            g2.setColor(TJCanvas2D.COLOR_BACKGROUND_DARK); 
            g2.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
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
    }
}
