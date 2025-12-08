package tj.scenario;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import tj.TJ;
import tj.TJPtCurve;
import tj.TJScene;
import tj.Command.TJCmdToAddCurPtCurveToPtCurves;
import tj.Command.TJCmdToUpdateCurPtCurve;
import x.XApp;
import x.XCmdToChangeScene;
import x.XScenario;

public class TJDrawScenario extends XScenario {
    // singleton pattern
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
        // singleton pattern
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
        public void handleMousePress(MouseEvent e) {
        }

        @Override
        public void handleMouseDrag(MouseEvent e) {
            TJ TJ = (TJ)this.mScenario.getApp();
            Point pt = e.getPoint();
            
            TJCmdToUpdateCurPtCurve.execute(TJ, pt);
        }

        @Override
        public void handleMouseRelease(MouseEvent e) {
            TJ TJ = (TJ) this.mScenario.getApp();
            TJCmdToAddCurPtCurveToPtCurves.execute(TJ);
            
            XCmdToChangeScene.execute(TJ, this.mReturnScene, null);
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
