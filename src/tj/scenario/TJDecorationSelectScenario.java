package tj.scenario;

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
import tj.TJEmojiPage;
import tj.TJPtCurve;
import tj.TJScene;
import tj.TJSelectionBox;
import tj.cmd.TJCmdToCreateSelectionBox;
import tj.cmd.TJCmdToCreateEmojiSelectionBox;
import tj.cmd.TJCmdToDeleteEmojiSelectedPtCurves;
import tj.cmd.TJCmdToDeleteSelectedPtCurves;
import tj.cmd.TJCmdToDeselectEmojiSelectedPtCurves;
import tj.cmd.TJCmdToDeselectSelectedPtCurves;
import tj.cmd.TJCmdToDestroyEmojiSelectionBox;
import tj.cmd.TJCmdToDestroySelectionBox;
import tj.cmd.TJCmdToIncreaseStrokeWidthForSelectedPtCurves;
import tj.cmd.TJCmdToUpdateEmojiSelectionBox;
import utils.TJNavPanel;
import x.XApp;
import x.XCmdToChangeScene;
import x.XScenario;

public class TJDecorationSelectScenario extends XScenario {

    // singleton pattern
    private static TJDecorationSelectScenario mSingleton = null;
    public static TJDecorationSelectScenario getSingle() {
        assert(TJDecorationSelectScenario.mSingleton != null);
        return TJDecorationSelectScenario.mSingleton;
    }
    public static TJDecorationSelectScenario createSingleton(XApp app) {
        assert(TJDecorationSelectScenario.mSingleton == null);
        TJDecorationSelectScenario.mSingleton = new TJDecorationSelectScenario(app);
        return TJDecorationSelectScenario.mSingleton;
    }
    private TJDecorationSelectScenario(XApp app) {
        super(app);
    }

    @Override
    protected void addScenes() {
        this.addScene(TJDecorationSelectScenario.DecorationSelectReadyScene.createSingleton(this));
        this.addScene(TJDecorationSelectScenario.DecorationSelectScene.createSingleton(this));
        this.addScene(TJDecorationSelectScenario.DecorationSelectedReadyScene.createSingleton(this));
    }

    // Selection box management
    private TJSelectionBox mSelectionBox = null;
    public TJSelectionBox getSelectionBox() {
        return this.mSelectionBox;
    }
    
    public void setSelectionBox(TJSelectionBox selectionBox) {
        this.mSelectionBox = selectionBox;
    }
    
    public void drawSelectionBox(Graphics2D g2) {
        if (TJDecorationSelectScenario.getSingle().getSelectionBox() != null) {
            Rectangle selBox = TJDecorationSelectScenario.getSingle().getSelectionBox();
            System.out.println("[DRAW EMOJI SELECTION BOX] Screen coords: " + selBox);
            
            g2.setColor(TJCanvas2D.COLOR_SELECTION_BOX);
            g2.setStroke(TJCanvas2D.STROKE_SELECTION_BOX);
            g2.draw(selBox);
        }
    }
    
    public void updatePageBounds(TJ tj) {
        // Updated for emoji selection - no specific bounds needed as emoji uses circle
    }

    public void updateSelectedPtCurves() {
        TJ tj = (TJ)this.mApp;
        TJEmojiScenario emojiScenario = TJEmojiScenario.getSingle();
        TJEmojiPage emojiPage = emojiScenario.getTargetEmojiPage();
        
        if (emojiPage == null) {
            System.out.println("[EMOJI-SELECT] No target emoji page, cannot update selected curves");
            return;
        }
        
        TJSelectionBox selBox = this.getSelectionBox();
        if (selBox == null) {
            System.out.println("[EMOJI-SELECT] No selection box");
            return;
        }
        
        Rectangle normalized = selBox;
        
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

        ArrayList<TJPtCurve> newlySelectedPtCurves = new ArrayList<TJPtCurve>();

        System.out.println("\n========== UPDATE SELECTED PT CURVES (EMOJI) ==========");
        System.out.println("Selection box (screen): " + normalized);
        System.out.println("Selection box (world): " + worldSelectionBox);
        System.out.println("Total curves in emoji page: " + emojiPage.getPtCurves().size());
        
        // Check curves directly from emoji page
        for (TJPtCurve ptCurve : emojiPage.getPtCurves()) {
            Rectangle2D curveBounds = ptCurve.getBoundingBox();
            System.out.println("\n--- Checking curve from emoji page ---");
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
                }
            } else {
                System.out.println("  ✗ Bounds don't intersect and not empty");
            }
        }
        
        // Update the emoji page's selected curves
        emojiPage.getSelectedPtCurves().clear();
        emojiPage.getSelectedPtCurves().addAll(newlySelectedPtCurves);
        
        System.out.println("\n[EMOJI-SELECT] Selected " + newlySelectedPtCurves.size() + " curves from emoji page");
    }

    // ==================== INNER SCENES ====================

    public static class DecorationSelectReadyScene extends TJScene {
        private JPanel mTopNavPanel;
        private JPanel mBottomNavPanel;
        
        private static DecorationSelectReadyScene mSingleton = null;
        public static DecorationSelectReadyScene getSingleton() {
            assert(DecorationSelectReadyScene.mSingleton != null);
            return DecorationSelectReadyScene.mSingleton;
        }
        public static DecorationSelectReadyScene createSingleton(XScenario scenario) {
            assert(DecorationSelectReadyScene.mSingleton == null);
            DecorationSelectReadyScene.mSingleton = new DecorationSelectReadyScene(scenario);
            return DecorationSelectReadyScene.mSingleton;
        }
        private DecorationSelectReadyScene(XScenario scenario) {
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
            TJCmdToCreateEmojiSelectionBox.execute(tj, pt);
            
            XCmdToChangeScene.execute(tj,
                TJDecorationSelectScenario.DecorationSelectScene.getSingleton(),
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
                    TJCmdToDestroyEmojiSelectionBox.execute(tj);
                    XCmdToChangeScene.execute(tj,
                        TJEmojiScenario.EmojiDrawScene.getSingleton(),
                        null);
                    break;
            }
        }

        @Override
        public void updateSupportObjects() {
        }
        
        @Override
        public void drawBackground(Graphics2D g2) {
            TJ tj = (TJ)this.mScenario.getApp();
            TJCanvas2D canvas = tj.getCanvas2D();
            
            g2.setColor(TJCanvas2D.COLOR_BACKGROUND_LIGHT); 
            g2.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        }

        @Override
        public void renderWorldObjects(Graphics2D g2) {
            TJ tj = (TJ)this.mScenario.getApp();
            TJCanvas2D canvas = tj.getCanvas2D();
            TJEmojiScenario scenario = (TJEmojiScenario)TJEmojiScenario.getSingle();

            if(scenario.getTargetBounds() == null) return;
            if(scenario.getTargetEmojiPage() == null) return; 

            scenario.drawEmojiPageAndContent(g2, canvas, scenario.getTargetBounds(), scenario.getTargetEmojiPage());
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
            tj.setTopPanel(null);
            tj.setBottomPanel(null);
        }
    }

    public static class DecorationSelectScene extends TJScene {
        private JPanel mTopNavPanel;     
        private JPanel mBottomNavPanel;
        
        private static DecorationSelectScene mSingleton = null;
        public static DecorationSelectScene getSingleton() {
            assert(DecorationSelectScene.mSingleton != null);
            return DecorationSelectScene.mSingleton;
        }
        public static DecorationSelectScene createSingleton(XScenario scenario) {
            assert(DecorationSelectScene.mSingleton == null);
            DecorationSelectScene.mSingleton = new DecorationSelectScene(scenario);
            return DecorationSelectScene.mSingleton;
        }
        private DecorationSelectScene(XScenario scenario) {
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
            
            if (TJDecorationSelectScenario.getSingle().getSelectionBox() != null) {
                TJCmdToUpdateEmojiSelectionBox.execute(tj, pt);
            }
        }

        @Override
        public void handleMouseRelease(MouseEvent e) {
            TJ tj = (TJ)this.mScenario.getApp();
            TJDecorationSelectScenario.getSingle().updateSelectedPtCurves();
            
            TJSelectionBox selBox = TJDecorationSelectScenario.getSingle().getSelectionBox();
            if (selBox != null && !selBox.isEmpty()) {
                XCmdToChangeScene.execute(
                    tj,
                    TJDecorationSelectScenario.DecorationSelectedReadyScene.getSingleton(),
                    this.getReturnScene()
                );
            } else {
                XCmdToChangeScene.execute(
                    tj,
                    TJEmojiScenario.EmojiDrawScene.getSingleton(),
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
        }
        
        @Override
        public void drawBackground(Graphics2D g2) {
            TJ tj = (TJ)this.mScenario.getApp();
            TJCanvas2D canvas = tj.getCanvas2D();
            
            g2.setColor(TJCanvas2D.COLOR_BACKGROUND_LIGHT); 
            g2.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        }

        @Override
        public void renderWorldObjects(Graphics2D g2) {
            TJ tj = (TJ)this.mScenario.getApp();
            TJCanvas2D canvas = tj.getCanvas2D();
            TJEmojiScenario scenario = (TJEmojiScenario)TJEmojiScenario.getSingle();

            if(scenario.getTargetBounds() == null) return;
            if(scenario.getTargetEmojiPage() == null) return; 

            scenario.drawEmojiPageAndContent(g2, canvas, scenario.getTargetBounds(), scenario.getTargetEmojiPage());
        }

        @Override
        public void renderScreenObjects(Graphics2D g2) {
            TJDecorationSelectScenario selectScenario = TJDecorationSelectScenario.getSingle();
            selectScenario.drawSelectionBox(g2);
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
            tj.setTopPanel(null);
            tj.setBottomPanel(null);
        }
    }



    public static class DecorationSelectedReadyScene extends TJScene {
        private JPanel mTopNavPanel;
        private JPanel mBottomNavPanel;
        
        private static DecorationSelectedReadyScene mSingleton = null;
        public static DecorationSelectedReadyScene getSingleton() {
            assert(DecorationSelectedReadyScene.mSingleton != null);
            return DecorationSelectedReadyScene.mSingleton;
        }
        public static DecorationSelectedReadyScene createSingleton(XScenario scenario) {
            assert(DecorationSelectedReadyScene.mSingleton == null);
            DecorationSelectedReadyScene.mSingleton = new DecorationSelectedReadyScene(scenario);
            return DecorationSelectedReadyScene.mSingleton;
        }
        private DecorationSelectedReadyScene(XScenario scenario) {
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
                        TJDecorationSelectScenario.DecorationSelectedReadyScene.getSingleton(), this);
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
                    TJCmdToDeselectEmojiSelectedPtCurves.execute(tj);
                    break;
                case KeyEvent.VK_BACK_SPACE:
                    TJCmdToDeleteEmojiSelectedPtCurves.execute(tj);
                    break;
            }
        }

        @Override
        public void updateSupportObjects() {
            TJSelectScenario.getSingle().updatePageBounds(
                (TJ)this.mScenario.getApp());
        }
        
        @Override
        public void drawBackground(Graphics2D g2) {
            TJ tj = (TJ)this.mScenario.getApp();
            TJCanvas2D canvas = tj.getCanvas2D();
            
            g2.setColor(TJCanvas2D.COLOR_BACKGROUND_LIGHT); 
            g2.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        }

        @Override
        public void renderWorldObjects(Graphics2D g2) {
            TJ tj = (TJ)this.mScenario.getApp();
            TJCanvas2D canvas = tj.getCanvas2D();
            TJEmojiScenario scenario = (TJEmojiScenario)TJEmojiScenario.getSingle();

            if(scenario.getTargetBounds() == null) return;
            if(scenario.getTargetEmojiPage() == null) return; 

            scenario.drawEmojiPageAndContent(g2, canvas, scenario.getTargetBounds(), scenario.getTargetEmojiPage());
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
            tj.setTopPanel(null);
            tj.setBottomPanel(null);
        }
    }
}

