package tj.scenario;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import tj.TJ;
import tj.TJCanvas2D;
import tj.TJPtCurve;
import tj.TJScene;
import tj.TJSelectionBox;
import tj.Command.TJCmdToCreateSelectionBox;
import tj.Command.TJCmdToDeleteSelectedPtCurves;
import tj.Command.TJCmdToDeselectSelectedPtCurves;
import tj.Command.TJCmdToDestroySelectionBox;
import tj.Command.TJCmdToHome;
import tj.Command.TJCmdToIncreaseStrokeWidthForSelectedPtCurves;
import tj.Command.TJCmdToUpdateSelectedPtCurves;
import tj.Command.TJCmdToUpdateSelectionBox;
import x.XApp;
import x.XCmdToChangeScene;
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
        this.addScene(TJSelectScenario.SelectScene.createSingleton(this));
        this.addScene(TJSelectScenario.SelectedReadyScene.createSingleton(this));
    }

    public static class SelectReadyScene extends TJScene {
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

        @Override
        public void handleMousePress(MouseEvent e) {
            TJ tj = (TJ)this.mScenario.getApp();
            Point pt = e.getPoint();
            TJCmdToCreateSelectionBox.execute(tj, pt);
            
            XCmdToChangeScene.execute(tj,
                TJSelectScenario.SelectScene.getSingleton(),
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
                case KeyEvent.VK_SHIFT:
                    if (!tj.getPtCurveMgr().getSelectedPtCurves().isEmpty()) {
                        XCmdToChangeScene.execute(tj,
                            TJSelectScenario.SelectedReadyScene.getSingleton(),
                            this.getReturnScene());
                    } else {
                        XCmdToChangeScene.execute(tj, this.getReturnScene(), null);
                    }
                    TJCmdToDestroySelectionBox.execute(tj);
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
    
    public static class SelectScene extends TJScene {
        // singleton pattern
        private static SelectScene mSingleton = null;
        public static SelectScene getSingleton() {
            assert(SelectScene.mSingleton != null);
            return SelectScene.mSingleton;
        }
        public static SelectScene createSingleton(XScenario scenario) {
            assert(SelectScene.mSingleton == null);
            SelectScene.mSingleton = new SelectScene(scenario);
            return SelectScene.mSingleton;
        }
        private SelectScene(XScenario scenario) {
            super(scenario);
        }

        @Override
        public void handleMousePress(MouseEvent e) {
        }

        @Override
        public void handleMouseDrag(MouseEvent e) {
            TJ tj = (TJ)this.mScenario.getApp();
            Point pt = e.getPoint();
            
            if (TJSelectScenario.getSingle().getSelectionBox() != null) {
                TJCmdToUpdateSelectionBox.execute(tj, pt);
                TJCmdToUpdateSelectedPtCurves.execute(tj);
            }
        }

        @Override
        public void handleMouseRelease(MouseEvent e) {
            TJ tj = (TJ)this.mScenario.getApp();
            
            TJCmdToDestroySelectionBox.execute(tj);
            XCmdToChangeScene.execute(tj,
                TJSelectScenario.SelectReadyScene.getSingleton(),
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
                case KeyEvent.VK_SHIFT:
                    if (!tj.getPtCurveMgr().getSelectedPtCurves().isEmpty()) {
                        XCmdToChangeScene.execute(tj,
                            TJSelectScenario.SelectedReadyScene.getSingleton(),
                            this.getReturnScene());
                    } else {
                        XCmdToChangeScene.execute(tj,
                            TJDefaultScenario.ReadyScene.getSingleton(), null);
                    }
                    TJCmdToDestroySelectionBox.execute(tj);
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
            TJSelectScenario scenario = (TJSelectScenario)this.mScenario;
            scenario.drawSelectionBox(g2);
        }

        @Override
        public void getReady() {
        }

        @Override
        public void wrapUp() {
        }
    }
    
    public static class SelectedReadyScene extends TJScene {
        // singleton pattern
        private static SelectedReadyScene mSingleton = null;
        public static SelectedReadyScene getSingleton() {
            assert(SelectedReadyScene.mSingleton != null);
            return SelectedReadyScene.mSingleton;
        }
        public static SelectedReadyScene createSingleton(XScenario scenario) {
            assert(SelectedReadyScene.mSingleton == null);
            SelectedReadyScene.mSingleton = new SelectedReadyScene(scenario);
            return SelectedReadyScene.mSingleton;
        }
        private SelectedReadyScene(XScenario scenario) {
            super(scenario);
        }

        @Override
        public void handleMousePress(MouseEvent e) {
            TJ tj = (TJ)this.mScenario.getApp();
            XCmdToChangeScene.execute(tj,
                TJGestureScenario.GestureDrawScene.getSingleton(), this);
            // XCmdToChangeScene.execute(tj,
            //             TJColorScenario.ColorReadyScene.getSingleton(), this);
        }

        @Override
        public void handleMouseDrag(MouseEvent e) {
        }

        @Override
        public void handleMouseRelease(MouseEvent e) {
        }

        @Override
        public void handleKeyDown(KeyEvent e) {
            TJ tj = (TJ)this.mScenario.getApp();
            int code = e.getKeyCode();
            
            switch (code) {
                case KeyEvent.VK_SHIFT:
                    XCmdToChangeScene.execute(tj,
                        TJSelectScenario.SelectReadyScene.getSingleton(), this);
                    break;
                case KeyEvent.VK_CONTROL:
                    XCmdToChangeScene.execute(tj,
                        TJNavigateScenario.PanReadyScene.getSingleton(), this);
                    break;
                case KeyEvent.VK_ALT:
                    XCmdToChangeScene.execute(tj,
                        TJNavigateScenario.ZoomRotateReadyScene.getSingleton(), this);
                    break;
                case KeyEvent.VK_C:
                    XCmdToChangeScene.execute(tj,
                        TJColorScenario.ColorReadyScene.getSingleton(), this);
                    break;
                case KeyEvent.VK_UP:
                    TJCmdToIncreaseStrokeWidthForSelectedPtCurves.execute(tj,
                        TJCanvas2D.STROKE_WIDTH_INCREMENT);
                    break;
                case KeyEvent.VK_DOWN:
                    TJCmdToIncreaseStrokeWidthForSelectedPtCurves.execute(tj,
                        -TJCanvas2D.STROKE_WIDTH_INCREMENT);
                    break;
            }
        }

        @Override
        public void handleKeyUp(KeyEvent e) {
            TJ tj = (TJ)this.mScenario.getApp();
            int code = e.getKeyCode();
            
            switch (code) {
                case KeyEvent.VK_ESCAPE:
                    TJCmdToDeselectSelectedPtCurves.execute(tj);
                    break;
                case KeyEvent.VK_BACK_SPACE:
                    TJCmdToDeleteSelectedPtCurves.execute(tj);
                    break;
                case KeyEvent.VK_SPACE:
                    TJCmdToHome.execute(tj);
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
    
    private TJSelectionBox mSelectionBox = null;
    public TJSelectionBox getSelectionBox() {
        return this.mSelectionBox;
    }
    public void setSelectionBox(TJSelectionBox selectionBox) {
        this.mSelectionBox = selectionBox;
    }
    public void drawSelectionBox(Graphics2D g2) {
        // draw current selection box
        if (TJSelectScenario.getSingle().getSelectionBox() != null) {
            g2.setColor(TJCanvas2D.COLOR_SELECTION_BOX);
            g2.setStroke(TJCanvas2D.STROKE_SELECTION_BOX);
            g2.draw(TJSelectScenario.getSingle().getSelectionBox());
        }
    }
    
    public void updateSelectedPtCurves() {
        TJ tj = (TJ)this.mApp;
        
        AffineTransform at = tj.getXform().getCurrentXformFromScreenToWorld();
        Shape worldSelectionBoxShape = at.createTransformedShape(
            this.mSelectionBox);

        ArrayList<TJPtCurve> newlySelectedPtCurves = 
            new ArrayList<TJPtCurve>();

        for (TJPtCurve ptCurve : tj.getPtCurveMgr().getPtCurves()) {
            if (worldSelectionBoxShape.intersects(ptCurve.getBoundingBox()) || 
                ptCurve.getBoundingBox().isEmpty()) {
                for (Point2D.Double pt : ptCurve.getPts()) {
                    if (worldSelectionBoxShape.contains(pt)) {
                        newlySelectedPtCurves.add(ptCurve);
                        break;
                    }
                }
            }
        }
        
        tj.getPtCurveMgr().getPtCurves().removeAll(newlySelectedPtCurves);
        tj.getPtCurveMgr().getSelectedPtCurves().addAll(newlySelectedPtCurves);
    }
}
