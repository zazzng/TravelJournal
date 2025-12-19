package tj.scenario;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import javax.swing.JPanel;
import tj.TJ;
import tj.TJCanvas2D;
import tj.TJPage;
import tj.TJPtCurve;
import tj.TJScene;
import tj.TJSelectionBox;
import tj.cmd.TJCmdToCreateSelectionBox;
import tj.cmd.TJCmdToDeleteSelectedPtCurves;
import tj.cmd.TJCmdToDeselectSelectedPtCurves;
import tj.cmd.TJCmdToDestroySelectionBox;
import tj.cmd.TJCmdToIncreaseStrokeWidthForSelectedPtCurves;
import tj.cmd.TJCmdToUpdateSelectedPtCurves;
import tj.cmd.TJCmdToUpdateSelectionBox;
import utils.TJNavPanel;
import x.XApp;
import x.XCmdToChangeScene;
import x.XScenario;

public class TJSelectScenario extends XScenario {

    private Rectangle mLeftPageBounds = null;
    public Rectangle getLeftPageBounds() { 
        return this.mLeftPageBounds;
    }
    
    private Rectangle mRightPageBounds = null;
    public Rectangle getRightPageBounds() { 
        return this.mRightPageBounds;
    }
    
    public void updatePageBounds(TJ tj) {
        TJCanvas2D canvas = tj.getCanvas2D();
        int appWidth = canvas.getWidth();
        int appHeight = canvas.getHeight();
        int pageHeight = (int)(appHeight * TJCanvas2D.PAGE_EDIT_HEIGHT_RATIO); 
        int pageWidth = (int)(pageHeight * TJCanvas2D.PAGE_ASPECT_RATIO);
        int startX = (appWidth - pageWidth * 2) / 2;
        int startY = (appHeight - pageHeight) / 2;

        this.mLeftPageBounds = new Rectangle(startX, startY, pageWidth, pageHeight);
        this.mRightPageBounds = new Rectangle(startX + pageWidth, startY, pageWidth, pageHeight);
    }
    
    // singleton pattern
    static TJSelectScenario mSingleton = null;
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
        // UI components
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
            TJ tj = (TJ)this.mScenario.getApp();
            String title = "Untitled Journal";
            if (tj.getJournalBookMgr().getCurBook() != null) {
                title = tj.getJournalBookMgr().getCurBook().getTitle();
            }
            this.mTopNavPanel = TJNavPanel.createTopNavPanel(tj, title);
        }
        
        private void initializeBottomNav() {
            TJ tj = (TJ)this.mScenario.getApp();
            this.mBottomNavPanel = TJNavPanel.createBottomNavPanel(tj, this);
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
                    TJCmdToDestroySelectionBox.execute(tj);

                    XCmdToChangeScene.execute(tj,
                        TJDrawScenario.DrawReadyScene.getSingleton(),
                        null);
                    break;
            }
        }

        @Override
        public void updateSupportObjects() {
            TJSelectScenario.getSingle().updatePageBounds(
                (TJ)this.mScenario.getApp());
        }

        @Override
        public void renderWorldObjects(Graphics2D g2) {
            TJ tj = (TJ)this.mScenario.getApp();
            TJCanvas2D canvas = tj.getCanvas2D();
            TJDrawScenario drawScenario = TJDrawScenario.getSingle();
            
            Rectangle leftBounds = drawScenario.getLeftPageBounds();
            if (leftBounds == null) return;  // Page bounds not initialized yet
            
            int startX = leftBounds.x;
            int startY = leftBounds.y;
            int pageWidth = leftBounds.width;
            int pageHeight = leftBounds.height;

            TJPage[] curPage = tj.getJournalBookMgr().getCurPage();
            if (curPage == null) return;
            
            ((TJSelectScenario)this.mScenario).drawPageAndContent(g2, canvas, curPage, startX, startY, pageWidth, pageHeight);
        }


        @Override
        public void renderScreenObjects(Graphics2D g2) {
        }

        @Override
        public void getReady() {
            TJSelectScenario.getSingle().updatePageBounds(
                (TJ)this.mScenario.getApp());

            TJ tj = (TJ)this.mScenario.getApp();
            
            // Auto-create a page if none exists
            if (tj.getJournalBookMgr().getCurPage() == null) {
                tj.getJournalBookMgr().addEmptyPage();
            }
            
            if (this.mTopNavPanel == null) {
                initializeTopNav();
            }
            if (this.mBottomNavPanel == null) {
                initializeBottomNav();
            }
            
            tj.setTopPanel(this.mTopNavPanel);
            tj.setBottomPanel(this.mBottomNavPanel);
            
            TJSelectScenario.getSingle().updatePageBounds(
                (TJ)this.mScenario.getApp());
        }

        @Override
        public void wrapUp() {
            TJ tj = (TJ)this.mScenario.getApp();
            tj.setTopPanel(null);
            tj.setBottomPanel(null);
        }
        @Override
        public void drawBackground(Graphics2D g2) {
            TJ tj = (TJ)this.mScenario.getApp();
            TJCanvas2D canvas = tj.getCanvas2D();
            
            g2.setColor(TJCanvas2D.COLOR_BACKGROUND_DARK);
            g2.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        }
    }
    
    public static class SelectScene extends TJScene {
        // UI components
        private JPanel mTopNavPanel;
        private JPanel mBottomNavPanel;
        
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
        
        private void initializeTopNav() {
            TJ tj = (TJ)this.mScenario.getApp();
            String title = "Untitled Journal";
            if (tj.getJournalBookMgr().getCurBook() != null) {
                title = tj.getJournalBookMgr().getCurBook().getTitle();
            }
            this.mTopNavPanel = TJNavPanel.createTopNavPanel(tj, title);
        }
        
        private void initializeBottomNav() {
            TJ tj = (TJ)this.mScenario.getApp();
            this.mBottomNavPanel = TJNavPanel.createBottomNavPanel(tj, this);
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
                // TJCmdToUpdateSelectedPtCurves.execute(tj);
            }
        }

        @Override
        public void handleMouseRelease(MouseEvent e) {
            TJ tj = (TJ)this.mScenario.getApp();
            System.out.println("[HANDLE MOUSE RELEASE] Executing TJCmdToUpdateSelectedPtCurves");
            TJCmdToUpdateSelectedPtCurves.execute(tj);
            TJCmdToDestroySelectionBox.execute(tj);
            System.out.println("[HANDLE MOUSE RELEASE] Total selected curves after update: " + 
                             tj.getPtCurveMgr().getSelectedPtCurves().size());
            if (!tj.getPtCurveMgr().getSelectedPtCurves().isEmpty()) {
                XCmdToChangeScene.execute(
                    tj,
                    TJSelectScenario.SelectedReadyScene.getSingleton(),
                    this.getReturnScene()
                );
            } else {
                XCmdToChangeScene.execute(
                    tj,
                    TJDrawScenario.DrawReadyScene.getSingleton(),
                    this.getReturnScene()
                );
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
            TJSelectScenario.getSingle().updatePageBounds(
                (TJ)this.mScenario.getApp());
        }

        @Override
        public void renderWorldObjects(Graphics2D g2) {
            TJ tj = (TJ)this.mScenario.getApp();
            TJCanvas2D canvas = tj.getCanvas2D();
            TJSelectScenario selectScenario = TJSelectScenario.getSingle();
            
            Rectangle leftBounds = selectScenario.getLeftPageBounds();
            if (leftBounds == null) return;  // Page bounds not initialized yet
            
            int startX = leftBounds.x;
            int startY = leftBounds.y;
            int pageWidth = leftBounds.width;
            int pageHeight = leftBounds.height;

            TJPage[] curPage = tj.getJournalBookMgr().getCurPage();
            if (curPage == null) return;
            
            ((TJSelectScenario)this.mScenario).drawPageAndContent(g2, canvas, curPage, startX, startY, pageWidth, pageHeight);
        }

        @Override
        public void renderScreenObjects(Graphics2D g2) {
            TJSelectScenario selectScenario = TJSelectScenario.getSingle();
            selectScenario.drawSelectionBox(g2);
        }

        @Override
        public void getReady() {
            TJ tj = (TJ)this.mScenario.getApp();
            
            // Auto-create a page if none exists
            if (tj.getJournalBookMgr().getCurPage() == null) {
                tj.getJournalBookMgr().addEmptyPage();
            }
            
            if (this.mTopNavPanel == null) {
                initializeTopNav();
            }
            if (this.mBottomNavPanel == null) {
                initializeBottomNav();
            }
            
            tj.setTopPanel(this.mTopNavPanel);
            tj.setBottomPanel(this.mBottomNavPanel);
            
            TJSelectScenario.getSingle().updatePageBounds(
                (TJ)this.mScenario.getApp());
        }

        @Override
        public void wrapUp() {
            TJ tj = (TJ)this.mScenario.getApp();
            tj.setTopPanel(null);
            tj.setBottomPanel(null);
        }
        @Override
        public void drawBackground(Graphics2D g2) {
            TJ tj = (TJ)this.mScenario.getApp();
            TJCanvas2D canvas = tj.getCanvas2D();
            
            g2.setColor(TJCanvas2D.COLOR_BACKGROUND_DARK);
            g2.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        }
    }
    
    public static class SelectedReadyScene extends TJScene {
        // UI components
        private JPanel mTopNavPanel;
        private JPanel mBottomNavPanel;
        
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
        
        private void initializeTopNav() {
            TJ tj = (TJ)this.mScenario.getApp();
            String title = "Untitled Journal";
            if (tj.getJournalBookMgr().getCurBook() != null) {
                title = tj.getJournalBookMgr().getCurBook().getTitle();
            }
            this.mTopNavPanel = TJNavPanel.createTopNavPanel(tj, title);
        }
        
        private void initializeBottomNav() {
            TJ tj = (TJ)this.mScenario.getApp();
            this.mBottomNavPanel = TJNavPanel.createBottomNavPanel(tj, this);
        }

        @Override
        public void handleMousePress(MouseEvent e) {
            // TJ tj = (TJ)this.mScenario.getApp();
            // XCmdToChangeScene.execute(tj,
            //     TJGestureScenario.GestureDrawScene.getSingleton(), this);
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
//                case KeyEvent.VK_CONTROL:
//                    XCmdToChangeScene.execute(tj,
//                        TJNavigateScenario.PanReadyScene.getSingleton(), this);
//                    break;
                // case KeyEvent.VK_ALT:
                //     XCmdToChangeScene.execute(tj,
                //         TJNavigateScenario.ZoomRotateReadyScene.getSingleton(), this);
                //     break;
                // case KeyEvent.VK_C:
                //     XCmdToChangeScene.execute(tj,
                //         TJColorScenario.ColorReadyScene.getSingleton(), this);
                //     break;
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
            System.out.println("SelectedReadyScene.handleKeyUp called: " + e.getKeyCode());
            TJ tj = (TJ)this.mScenario.getApp();
            int code = e.getKeyCode();
            
            switch (code) {
                case KeyEvent.VK_ESCAPE:
                    TJCmdToDeselectSelectedPtCurves.execute(tj);
                    break;
                case KeyEvent.VK_BACK_SPACE:
                    TJCmdToDeleteSelectedPtCurves.execute(tj);
                    break;
                // case KeyEvent.VK_SPACE:
                //     TJCmdToHome.execute(tj);
                //     break;
            }
        }

        @Override
        public void updateSupportObjects() {
            TJSelectScenario.getSingle().updatePageBounds(
                (TJ)this.mScenario.getApp());
        }

        @Override
        public void renderWorldObjects(Graphics2D g2) {
            TJ tj = (TJ)this.mScenario.getApp();
            TJCanvas2D canvas = tj.getCanvas2D();
            TJDrawScenario drawScenario = TJDrawScenario.getSingle();
            
            Rectangle leftBounds = drawScenario.getLeftPageBounds();
            if (leftBounds == null) return;  // Page bounds not initialized yet
            
            int startX = leftBounds.x;
            int startY = leftBounds.y;
            int pageWidth = leftBounds.width;
            int pageHeight = leftBounds.height;

            TJPage[] curPage = tj.getJournalBookMgr().getCurPage();
            if (curPage == null) return;
            
            ((TJSelectScenario)this.mScenario).drawPageAndContent(g2, canvas, curPage, startX, startY, pageWidth, pageHeight);
        }

        @Override
        public void renderScreenObjects(Graphics2D g2) {
        }

        @Override
        public void getReady() { 
            TJSelectScenario.getSingle().updatePageBounds( (TJ)this.mScenario.getApp()); 
            TJ tj = (TJ)this.mScenario.getApp(); 
            tj.getCanvas2D().requestFocusInWindow(); 
            
            // Auto-create a page if none exists
            if (tj.getJournalBookMgr().getCurPage() == null) {
                tj.getJournalBookMgr().addEmptyPage();
            }
            
            if (this.mTopNavPanel == null) {
                initializeTopNav();
            }
            if (this.mBottomNavPanel == null) {
                initializeBottomNav();
            }
            
            tj.setTopPanel(this.mTopNavPanel);
            tj.setBottomPanel(this.mBottomNavPanel);
            
            TJSelectScenario.getSingle().updatePageBounds(
                (TJ)this.mScenario.getApp());
        }

        @Override
        public void wrapUp() {
            TJ tj = (TJ)this.mScenario.getApp();
            tj.setTopPanel(null);
            tj.setBottomPanel(null);
        }
        
        @Override
        public void drawBackground(Graphics2D g2) {
            TJ tj = (TJ)this.mScenario.getApp();
            TJCanvas2D canvas = tj.getCanvas2D();
            
            g2.setColor(TJCanvas2D.COLOR_BACKGROUND_DARK); 
            g2.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
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
            Rectangle selBox = TJSelectScenario.getSingle().getSelectionBox();
            System.out.println("[DRAW SELECTION BOX] Screen coords: " + selBox);
            
            g2.setColor(TJCanvas2D.COLOR_SELECTION_BOX);
            g2.setStroke(TJCanvas2D.STROKE_SELECTION_BOX);
            g2.draw(selBox);
        }
    }
    
    public void updateSelectedPtCurves() {
        Rectangle r = this.mSelectionBox;

        int x = Math.min(r.x, r.x + r.width);
        int y = Math.min(r.y, r.y + r.height);
        int w = Math.abs(r.width);
        int h = Math.abs(r.height);

        Rectangle normalized = new Rectangle(x, y, w, h);

        TJ tj = (TJ)this.mApp;
        
        // Transform the four corners of the selection box from screen to world
        AffineTransform at = tj.getXform().getCurrentXformFromScreenToWorld();
        Point2D.Double topLeft = new Point2D.Double(normalized.x, normalized.y);
        Point2D.Double bottomRight = new Point2D.Double(normalized.x + normalized.width, normalized.y + normalized.height);
        at.transform(topLeft, topLeft);
        at.transform(bottomRight, bottomRight);
        
        // Create world-space rectangle from transformed corners
        double worldX = Math.min(topLeft.x, bottomRight.x);
        double worldY = Math.min(topLeft.y, bottomRight.y);
        double worldW = Math.abs(bottomRight.x - topLeft.x);
        double worldH = Math.abs(bottomRight.y - topLeft.y);
        Rectangle2D worldSelectionBox = new Rectangle2D.Double(worldX, worldY, worldW, worldH);

        ArrayList<TJPtCurve> newlySelectedPtCurves = 
            new ArrayList<TJPtCurve>();

        // DEBUG: Print selection box info
        System.out.println("\n========== UPDATE SELECTED PT CURVES ==========");
        System.out.println("Selection box (normalized): " + normalized);
        System.out.println("TopLeft (after transform): " + topLeft);
        System.out.println("BottomRight (after transform): " + bottomRight);
        System.out.println("Selection box (world): " + worldSelectionBox);
        System.out.println("AffineTransform: " + at);
        System.out.println("Total curves in manager: " + tj.getPtCurveMgr().getPtCurves().size());
        System.out.println("Transform is identity? " + at.isIdentity());
        System.out.println("Curves in PtCurveMgr: " + tj.getPtCurveMgr().getPtCurves().size());
        
        TJPage[] curPage = tj.getJournalBookMgr().getCurPage();
        if (curPage != null) {
            System.out.println("Page[0] curves: " + curPage[0].getPtCurves().size());
            System.out.println("Page[1] curves: " + curPage[1].getPtCurves().size());
        }
        
        for (TJPtCurve ptCurve : tj.getPtCurveMgr().getPtCurves()) {
            Rectangle2D curveBounds = ptCurve.getBoundingBox();
            System.out.println("\n--- Checking curve from PtCurveMgr ---");
            System.out.println("Curve bounds: " + curveBounds);
            System.out.println("Number of points: " + ptCurve.getPts().size());
            
            if (worldSelectionBox.intersects(curveBounds) || curveBounds.isEmpty()) {
                System.out.println("  ✓ Bounds intersect or empty");
                int pointsInBox = 0;
                for (Point2D.Double pt : ptCurve.getPts()) {
                    if (worldSelectionBox.contains(pt)) {
                        System.out.println("    ✓ Point " + pt + " is inside box");
                        newlySelectedPtCurves.add(ptCurve);
                        pointsInBox++;
                        break;
                    }
                }
                if (pointsInBox == 0) {
                    System.out.println("  ✗ No points found inside box");
                    // Print first few points for debugging
                    for (int i = 0; i < Math.min(3, ptCurve.getPts().size()); i++) {
                        Point2D.Double pt = ptCurve.getPts().get(i);
                        System.out.println("    Point[" + i + "]: " + pt + " -> inside? " + worldSelectionBox.contains(pt));
                    }
                }
            } else {
                System.out.println("  ✗ Bounds don't intersect and not empty");
            }
        }
        
        // Also check curves directly from pages
        if (curPage != null) {
            System.out.println("\n--- Checking curves from pages ---");
            for (TJPtCurve ptCurve : curPage[0].getPtCurves()) {
                Rectangle2D curveBounds = ptCurve.getBoundingBox();
                System.out.println("Page[0] Curve bounds: " + curveBounds);
                if (worldSelectionBox.intersects(curveBounds) || curveBounds.isEmpty()) {
                    System.out.println("  ✓ Bounds intersect");
                    for (Point2D.Double pt : ptCurve.getPts()) {
                        if (worldSelectionBox.contains(pt)) {
                            System.out.println("    ✓ Point " + pt + " is inside box");
                            newlySelectedPtCurves.add(ptCurve);
                            break;
                        }
                    }
                }
            }
            for (TJPtCurve ptCurve : curPage[1].getPtCurves()) {
                Rectangle2D curveBounds = ptCurve.getBoundingBox();
                System.out.println("Page[1] Curve bounds: " + curveBounds);
                if (worldSelectionBox.intersects(curveBounds) || curveBounds.isEmpty()) {
                    System.out.println("  ✓ Bounds intersect");
                    for (Point2D.Double pt : ptCurve.getPts()) {
                        if (worldSelectionBox.contains(pt)) {
                            System.out.println("    ✓ Point " + pt + " is inside box");
                            newlySelectedPtCurves.add(ptCurve);
                            break;
                        }
                    }
                }
            }
        }
        
        
        System.out.println("\nNewly selected curves: " + newlySelectedPtCurves.size());
        
        // Clear previous selections first
        tj.getPtCurveMgr().getPtCurves().addAll(tj.getPtCurveMgr().getSelectedPtCurves());
        tj.getPtCurveMgr().getSelectedPtCurves().clear();
        
        // Now add the newly selected curves
        tj.getPtCurveMgr().getPtCurves().removeAll(newlySelectedPtCurves);
        tj.getPtCurveMgr().getSelectedPtCurves().addAll(newlySelectedPtCurves);
        
        System.out.println("Total unselected curves: " + tj.getPtCurveMgr().getPtCurves().size());
        System.out.println("Total selected curves: " + tj.getPtCurveMgr().getSelectedPtCurves().size());
        
        // Sync selected curves to current pages
        if (curPage != null) {
            // Clear page selections
            curPage[0].getSelectedPtCurves().clear();
            curPage[1].getSelectedPtCurves().clear();
            // Add selected curves that belong to each page
            for (TJPtCurve selectedCurve : tj.getPtCurveMgr().getSelectedPtCurves()) {
                if (curPage[0].getPtCurves().contains(selectedCurve)) {
                    curPage[0].getSelectedPtCurves().add(selectedCurve);
                    System.out.println("  Added to page[0]: " + selectedCurve);
                } else if (curPage[1].getPtCurves().contains(selectedCurve)) {
                    curPage[1].getSelectedPtCurves().add(selectedCurve);
                    System.out.println("  Added to page[1]: " + selectedCurve);
                }
            }
            System.out.println("Page[0] selected: " + curPage[0].getSelectedPtCurves().size());
            System.out.println("Page[1] selected: " + curPage[1].getSelectedPtCurves().size());
        }
        System.out.println("==========================================\n");
    }

    public void drawPageAndContent(Graphics2D g2, TJCanvas2D canvas, TJPage[] curPage, int startX, int startY,
            int pageWidth, int pageHeight) {
        System.out.println("[DRAW PAGE AND CONTENT] startX=" + startX + ", startY=" + startY + 
                          ", pageWidth=" + pageWidth + ", pageHeight=" + pageHeight);
        System.out.println("  Page[0] curves: " + curPage[0].getPtCurves().size() + 
                          ", selected: " + curPage[0].getSelectedPtCurves().size());
        System.out.println("  Page[1] curves: " + curPage[1].getPtCurves().size() + 
                          ", selected: " + curPage[1].getSelectedPtCurves().size());
        
        drawPageStructure(g2, startX, startY, pageWidth, pageHeight);
        
        canvas.drawPtCurves(g2, curPage[0].getPtCurves());
        canvas.drawSelectedPtCurves(g2, curPage[0].getSelectedPtCurves());
        
        canvas.drawCurPtCurve(g2);
        canvas.drawPtCurves(g2, curPage[1].getPtCurves());
        canvas.drawSelectedPtCurves(g2, curPage[1].getSelectedPtCurves());
    }
    private void drawPageStructure(Graphics2D g2, int startX, int startY, int pageWidth, int pageHeight) {
        // left page
        g2.setColor(Color.WHITE);
        g2.fillRect(startX, startY, pageWidth, pageHeight);
        g2.setStroke(TJHomeScenario.PAGE_BORDER_STROKE);
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawRect(startX, startY, pageWidth, pageHeight);
        
        // right page
        g2.setColor(Color.WHITE);
        g2.fillRect(startX + pageWidth, startY, pageWidth, pageHeight);
        g2.setStroke(TJHomeScenario.PAGE_BORDER_STROKE);
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawRect(startX + pageWidth, startY, pageWidth, pageHeight);

        // divider
        g2.setStroke(new BasicStroke(2.0f));
        g2.setColor(new Color(200, 200, 200)); 
        g2.drawLine(startX + pageWidth, startY, startX + pageWidth, startY + pageHeight);
    }
}
