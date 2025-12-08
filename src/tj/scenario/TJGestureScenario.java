package tj.scenario;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import tj.TJ;
import tj.TJPenMark;
import tj.TJScene;
import tj.Command.TJCmdToDeselectSelectedPtCurves;
import x.XApp;
import x.XCmdToChangeScene;
import x.XScenario;

public class TJGestureScenario extends XScenario {
    // singleton pattern
    private static TJGestureScenario mSingleton = null;
    public static TJGestureScenario getSingle() {
        assert(TJGestureScenario.mSingleton != null);
        return TJGestureScenario.mSingleton;
    }
    public static TJGestureScenario createSingleton(XApp app) {
        assert(TJGestureScenario.mSingleton == null);
        TJGestureScenario.mSingleton = new TJGestureScenario(app);
        return TJGestureScenario.mSingleton;
    }
    private TJGestureScenario(XApp app) {
        super(app);
    }

    @Override
    protected void addScenes() {
        this.addScene(TJGestureScenario.GestureDrawScene.createSingleton(this));
    }

    public static class GestureDrawScene extends TJScene {
        // singleton pattern
        private static GestureDrawScene mSingleton = null;
        public static GestureDrawScene getSingleton() {
            assert(GestureDrawScene.mSingleton != null);
            return GestureDrawScene.mSingleton;
        }
        public static GestureDrawScene createSingleton(XScenario scenario) {
            assert(GestureDrawScene.mSingleton == null);
            GestureDrawScene.mSingleton = new GestureDrawScene(scenario);
            return GestureDrawScene.mSingleton;
        }
        private GestureDrawScene(XScenario scenario) {
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
            TJ tj = (TJ) this.mScenario.getApp();
            TJPenMark penMark = tj.getPenMarkMgr().getLastPenMark();
            if (penMark.getPts().size() == 1) { // tap
                TJCmdToDeselectSelectedPtCurves.execute(tj);
                XCmdToChangeScene.execute(tj, TJDefaultScenario.ReadyScene.getSingleton(), null);
            } else {
                XCmdToChangeScene.execute(tj, this.mReturnScene, null);
            }
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
