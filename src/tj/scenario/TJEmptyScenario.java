package tj.scenario;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
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
        }

        @Override
        public void wrapUp() {
        }
    }
}
