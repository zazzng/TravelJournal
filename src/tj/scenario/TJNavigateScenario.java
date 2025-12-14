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
import tj.cmd.TJCmdToSetStartScreenPt;
import tj.cmd.TJCmdToTranslateTo;
import tj.cmd.TJCmdToZoomTo;
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
        this.addScene(TJNavigateScenario.ZoomReadyScene.createSingleton(this));
        this.addScene(TJNavigateScenario.ZoomScene.createSingleton(this));
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

        @Override
        public void drawBackground(Graphics2D g2) {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
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

        @Override
        public void drawBackground(Graphics2D g2) {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }
    }
    
    public static class ZoomReadyScene extends TJScene {
        // singleton pattern
        private static ZoomReadyScene mSingleton = null;
        public static ZoomReadyScene getSingleton() {
            assert(ZoomReadyScene.mSingleton != null);
            return ZoomReadyScene.mSingleton;
        }
        public static ZoomReadyScene createSingleton(XScenario scenario) {
            assert(ZoomReadyScene.mSingleton == null);
            ZoomReadyScene.mSingleton = new ZoomReadyScene(scenario);
            return ZoomReadyScene.mSingleton;
        }
        private ZoomReadyScene(XScenario scenario) {
            super(scenario);
        }

        @Override
        public void handleMousePress(MouseEvent e) {
            TJ tj = (TJ)this.mScenario.getApp();
            Point pt = e.getPoint();
            TJCmdToSetStartScreenPt.execute(tj, pt);
            
            XCmdToChangeScene.execute(tj,
                TJNavigateScenario.ZoomScene.getSingleton(),
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
            
        }

        @Override
        public void getReady() {
        }

        @Override
        public void wrapUp() {
        }

        @Override
        public void drawBackground(Graphics2D g2) {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }
    }
    
    public static class ZoomScene extends TJScene {
        // singleton pattern
        private static ZoomScene mSingleton = null;
        public static ZoomScene getSingleton() {
            assert(ZoomScene.mSingleton != null);
            return ZoomScene.mSingleton;
        }
        public static ZoomScene createSingleton(XScenario scenario) {
            assert(ZoomScene.mSingleton == null);
            ZoomScene.mSingleton = new ZoomScene(scenario);
            return ZoomScene.mSingleton;
        }
        private ZoomScene(XScenario scenario) {
            super(scenario);
        }

        @Override
        public void handleMousePress(MouseEvent e) {
        }

        @Override
        public void handleMouseDrag(MouseEvent e) {
            TJ tj = (TJ)this.mScenario.getApp();
            Point pt = e.getPoint();
            
            TJCmdToZoomTo.execute(tj, pt);
        }

        @Override
        public void handleMouseRelease(MouseEvent e) {
            TJ tj = (TJ)this.mScenario.getApp();
            
            TJCmdToSetStartScreenPt.execute(tj, null);
            XCmdToChangeScene.execute(tj,
                TJNavigateScenario.ZoomReadyScene.getSingleton(),
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
            
        }

        @Override
        public void getReady() {
        }

        @Override
        public void wrapUp() {
        }

        @Override
        public void drawBackground(Graphics2D g2) {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }
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
