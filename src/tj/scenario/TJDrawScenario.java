package tj.scenario;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import tj.TJScene;
import x.XApp;
import x.XScenario;

public class TJDrawScenario extends XScenario {

    private static TJDrawScenario mSingleton = null;
    public static TJDrawScenario getSingle() {
        assert(TJDrawScenario.mSingleton != null);
        return TJDrawScenario.mSingleton;
    }
    public static TJDrawScenario createSingleton(XApp app) {
        assert(TJDrawScenario.mSingleton == null);
        TJDrawScenario.mSingleton = new TJDrawScenario(app);
        return TJDrawScenario.mSingleton;
    }

    private TJDrawScenario(XApp app) {
        super(app);
    }

    @Override
    protected void addScenes() {
        this.addScene(TJDrawScenario.DrawScene.createSingleton(this));
    }

    public static class DrawScene extends TJScene {
        private static DrawScene mSingleton = null;
        public static DrawScene getSingleton() {
            assert(DrawScene.mSingleton != null);
            return DrawScene.mSingleton;
        }
        public static DrawScene createSingleton(XScenario scenario) {
            assert(DrawScene.mSingleton == null);
            DrawScene.mSingleton = new DrawScene(scenario);
            return DrawScene.mSingleton;
        }

        private DrawScene(XScenario scenario) {
            super(scenario);
        }
        @Override
        public void getReady() {
            // TODO Auto-generated method stub
        }
        @Override
        public void wrapUp() {
            // TODO Auto-generated method stub
        }
        @Override
        public void handleMousePress(MouseEvent e) {
            // TODO Auto-generated method stub
        }
        @Override
        public void handleMouseDrag(MouseEvent e) {
            // TODO Auto-generated method stub
        }
        @Override
        public void handleMouseRelease(MouseEvent e) {
            // TODO Auto-generated method stub
        }
        @Override
        public void handleKeyDown(KeyEvent e) {
            // TODO Auto-generated method stub
        }
        @Override
        public void handleKeyUp(KeyEvent e) {
            // TODO Auto-generated method stub
        }
        @Override
        public void updateSupportObjects() {
            // TODO Auto-generated method stub
        }
        @Override
        public void renderWorldObjects(Graphics2D g2) {
            // TODO Auto-generated method stub
        }
        @Override
        public void renderScreenObjects(Graphics2D g2) {
            // TODO Auto-generated method stub
        }
    }
    
}
