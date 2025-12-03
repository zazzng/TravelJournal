package tj.scenario;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import tj.TJScene;
import x.XApp;
import x.XScenario;

public class TJHomeScenario extends XScenario {
    // singleton pattern
    private static TJHomeScenario mSingleton = null;
    public static TJHomeScenario getSingle() {
        assert(TJHomeScenario.mSingleton != null);
        return TJHomeScenario.mSingleton;
    }
    public static TJHomeScenario createSingleton(XApp app) {
        assert(TJHomeScenario.mSingleton == null);
        TJHomeScenario.mSingleton = new TJHomeScenario(app);
        return TJHomeScenario.mSingleton;
    }
    private TJHomeScenario(XApp app) {
        super(app);
    }

    @Override
    protected void addScenes() {
        this.addScene(TJHomeScenario.CatalogueScene.createSingleton(this));
    }

    public static class CatalogueScene extends TJScene {
        // singleton pattern
        private static CatalogueScene mSingleton = null;
        public static CatalogueScene getSingleton() {
            assert(CatalogueScene.mSingleton != null);
            return CatalogueScene.mSingleton;
        }
        public static CatalogueScene createSingleton(XScenario scenario) {
            assert(CatalogueScene.mSingleton == null);
            CatalogueScene.mSingleton = new CatalogueScene(scenario);
            return CatalogueScene.mSingleton;
        }
        private CatalogueScene(XScenario scenario) {
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