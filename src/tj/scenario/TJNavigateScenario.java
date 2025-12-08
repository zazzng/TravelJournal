package tj.scenario;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import tj.TJ;
import tj.TJCanvas2D;
import tj.TJScene;
import tj.TJXform;
import tj.Command.TJCmdToSetStartScreenPt;
import tj.Command.TJCmdToTranslateTo;
import tj.Command.TJCmdToZoomRotateTo;
import x.XApp;
import x.XCmdToChangeScene;
import x.XScenario;

public class TJNavigateScenario extends XScenario {
    // singleton pattern
    private static TJNavigateScenario mSingleton = null;
    public static TJNavigateScenario getSingle() {
        assert(TJNavigateScenario.mSingleton != null);
        return TJNavigateScenario.mSingleton;
    }
    public static TJNavigateScenario createSingleton(XApp app) {
        assert(TJNavigateScenario.mSingleton == null);
        TJNavigateScenario.mSingleton = new TJNavigateScenario(app);
        return TJNavigateScenario.mSingleton;
    }
    private TJNavigateScenario(XApp app) {
        super(app);
    }

    @Override
    protected void addScenes() {
        this.addScene(TJNavigateScenario.PanReadyScene.createSingleton(this));
        this.addScene(TJNavigateScenario.PanScene.createSingleton(this));
        this.addScene(TJNavigateScenario.ZoomRotateReadyScene.createSingleton(this));
        this.addScene(TJNavigateScenario.ZoomRotateScene.createSingleton(this));
    }

    public static class PanReadyScene extends TJScene {
        // singleton pattern
        private static PanReadyScene mSingleton = null;
        public static PanReadyScene getSingleton() {
            assert(PanReadyScene.mSingleton != null);
            return PanReadyScene.mSingleton;
        }
        public static PanReadyScene createSingleton(XScenario scenario) {
            assert(PanReadyScene.mSingleton == null);
            PanReadyScene.mSingleton = new PanReadyScene(scenario);
            return PanReadyScene.mSingleton;
        }
        private PanReadyScene(XScenario scenario) {
            super(scenario);
        }

        @Override
        public void handleMousePress(MouseEvent e) {
            TJ tj = (TJ)this.mScenario.getApp();
            Point pt = e.getPoint();
            TJCmdToSetStartScreenPt.execute(tj, pt);
            
            XCmdToChangeScene.execute(tj,
                TJNavigateScenario.PanScene.getSingleton(),
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
                case KeyEvent.VK_CONTROL:
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
    
    public static class PanScene extends TJScene {
        // singleton pattern
        private static PanScene mSingleton = null;
        public static PanScene getSingleton() {
            assert(PanScene.mSingleton != null);
            return PanScene.mSingleton;
        }
        public static PanScene createSingleton(XScenario scenario) {
            assert(PanScene.mSingleton == null);
            PanScene.mSingleton = new PanScene(scenario);
            return PanScene.mSingleton;
        }
        private PanScene(XScenario scenario) {
            super(scenario);
        }

        @Override
        public void handleMousePress(MouseEvent e) {
        }

        @Override
        public void handleMouseDrag(MouseEvent e) {
            TJ tj = (TJ)this.mScenario.getApp();
            Point pt = e.getPoint();
            
            TJCmdToTranslateTo.execute(tj, pt);
        }

        @Override
        public void handleMouseRelease(MouseEvent e) {
            TJ tj = (TJ)this.mScenario.getApp();
            
            TJCmdToSetStartScreenPt.execute(tj, null);
            XCmdToChangeScene.execute(tj,
                TJNavigateScenario.PanReadyScene.getSingleton(),
                this.getReturnScene());
        }

        @Override
        public void handleKeyDown(KeyEvent e) {
           
        }

        @Override
        public void handleKeyUp(KeyEvent e) {
            TJ tj = (TJ)this.mScenario.getApp();
            int code = e.getKeyCode();
            
            switch (code) {
                case KeyEvent.VK_CONTROL:
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
            TJNavigateScenario scenario = (TJNavigateScenario) this.mScenario;
            scenario.drawPanCrossHair(g2);
        }

        @Override
        public void getReady() {
        }

        @Override
        public void wrapUp() {
        }
    }
    
    public static class ZoomRotateReadyScene extends TJScene {
        // singleton pattern
        private static ZoomRotateReadyScene mSingleton = null;
        public static ZoomRotateReadyScene getSingleton() {
            assert(ZoomRotateReadyScene.mSingleton != null);
            return ZoomRotateReadyScene.mSingleton;
        }
        public static ZoomRotateReadyScene createSingleton(XScenario scenario) {
            assert(ZoomRotateReadyScene.mSingleton == null);
            ZoomRotateReadyScene.mSingleton = new ZoomRotateReadyScene(scenario);
            return ZoomRotateReadyScene.mSingleton;
        }
        private ZoomRotateReadyScene(XScenario scenario) {
            super(scenario);
        }

        @Override
        public void handleMousePress(MouseEvent e) {
            TJ tj = (TJ)this.mScenario.getApp();
            Point pt = e.getPoint();
            TJCmdToSetStartScreenPt.execute(tj, pt);
            
            XCmdToChangeScene.execute(tj,
                TJNavigateScenario.ZoomRotateScene.getSingleton(),
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
                case KeyEvent.VK_ALT:
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
            TJNavigateScenario scenario = (TJNavigateScenario) this.mScenario;
            scenario.drawZoomRotateCrossHair(g2);
        }

        @Override
        public void getReady() {
        }

        @Override
        public void wrapUp() {
        }
    }
    
    public static class ZoomRotateScene extends TJScene {
        // singleton pattern
        private static ZoomRotateScene mSingleton = null;
        public static ZoomRotateScene getSingleton() {
            assert(ZoomRotateScene.mSingleton != null);
            return ZoomRotateScene.mSingleton;
        }
        public static ZoomRotateScene createSingleton(XScenario scenario) {
            assert(ZoomRotateScene.mSingleton == null);
            ZoomRotateScene.mSingleton = new ZoomRotateScene(scenario);
            return ZoomRotateScene.mSingleton;
        }
        private ZoomRotateScene(XScenario scenario) {
            super(scenario);
        }

        @Override
        public void handleMousePress(MouseEvent e) {
        }

        @Override
        public void handleMouseDrag(MouseEvent e) {
            TJ tj = (TJ)this.mScenario.getApp();
            Point pt = e.getPoint();
            
            TJCmdToZoomRotateTo.execute(tj, pt);
        }

        @Override
        public void handleMouseRelease(MouseEvent e) {
            TJ tj = (TJ)this.mScenario.getApp();
            
            TJCmdToSetStartScreenPt.execute(tj, null);
            XCmdToChangeScene.execute(tj,
                TJNavigateScenario.ZoomRotateReadyScene.getSingleton(),
                this.getReturnScene());
        }

        @Override
        public void handleKeyDown(KeyEvent e) {
           
        }

        @Override
        public void handleKeyUp(KeyEvent e) {
            TJ tj = (TJ)this.mScenario.getApp();
            int code = e.getKeyCode();
            
            switch (code) {
                case KeyEvent.VK_ALT:
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
            TJNavigateScenario scenario = (TJNavigateScenario) this.mScenario;
            scenario.drawZoomRotateCrossHair(g2);
        }

        @Override
        public void getReady() {
        }

        @Override
        public void wrapUp() {
        }
    }
    
    private void drawZoomRotateCrossHair(Graphics2D g2) {
        double r = TJCanvas2D.ZOOM_ROTATE_CROSS_HAIR_RADIUS;
        Point ctr = TJXform.PIVOT_PT;
        Line2D hline = new Line2D.Double(ctr.x - r, ctr.y, ctr.x + r, ctr.y);
        Line2D vline = new Line2D.Double(ctr.x, ctr.y - r, ctr.x, ctr.y + r);

        g2.setColor(TJCanvas2D.COLOR_CROSS_HAIR);
        g2.setStroke(TJCanvas2D.STROKE_CROSS_HAIR);
        g2.draw(vline);
        g2.draw(hline);
    }
    
    private void drawPanCrossHair(Graphics2D g2) {
        TJ tj = (TJ) this.mApp;
        Point penPt = tj.getPenMarkMgr().getLastPenMark().getLastPt();
        
        Line2D hline = new Line2D.Double(0.0, penPt.y,
            tj.getCanvas2D().getWidth(), penPt.y);
        Line2D vline = new Line2D.Double(penPt.x, 0.0, penPt.x, tj.getCanvas2D().getHeight());
        
        g2.setColor(TJCanvas2D.COLOR_CROSS_HAIR);
        g2.setStroke(TJCanvas2D.STROKE_CROSS_HAIR);
        g2.draw(vline);
        g2.draw(hline);
    }
}
