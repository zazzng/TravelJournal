package tj.scenario;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JPanel;
import tj.TJ;
import tj.TJCanvas2D;
import tj.TJPage;
import tj.TJPtCurve;
import tj.TJScene;
import tj.cmd.TJCmdToAddCurPtCurveToPtCurves;
import tj.cmd.TJCmdToCreateCurPtCurve;
import tj.cmd.TJCmdToIncreaseStrokeWidthForCurPtCurve;
import tj.cmd.TJCmdToUpdateCurPtCurve;
import utils.TJNavPanel;
import x.XApp;
import x.XCmdToChangeScene;
import x.XScenario;

public class TJEmojiScenario extends XScenario {
    private TJPage mTargetPage = null;
    private Rectangle mTargetBounds = null;
    private Point mLastMousePoint = null; // Track last click point for scene transitions
    
    public Rectangle getTargetBounds() {
        return this.mTargetBounds;
    }
    
    private Rectangle mLeftPageBounds = null;
    public Rectangle getLeftPageBounds() { 
        return this.mLeftPageBounds;
    }
    
    private Rectangle mRightPageBounds = null;
    public Rectangle getRightPageBounds() {
        return this.mRightPageBounds;
    }

    private static TJEmojiScenario mSingleton = null;
    public static TJEmojiScenario getSingle() {
        assert(TJEmojiScenario.mSingleton != null);
        return TJEmojiScenario.mSingleton;
    }
    public static TJEmojiScenario createSingleton(XApp app) {
        assert(TJEmojiScenario.mSingleton == null);
        TJEmojiScenario.mSingleton = new TJEmojiScenario(app);
        return TJEmojiScenario.mSingleton;
    }
    
    private TJEmojiScenario(XApp app) {
        super(app);
    }
    
    @Override
    protected void addScenes() {
        this.addScene(TJEmojiScenario.EmojiDrawScene.createSingleton(this));
        this.addScene(TJEmojiScenario.EmojiDrawingScene.createSingleton(this));
    }

    private Rectangle getTotalPageBounds(TJ tj) {
        TJCanvas2D canvas = tj.getCanvas2D();
        int appWidth = canvas.getWidth();
        int appHeight = canvas.getHeight();
        
        int pageHeight = (int)(appHeight * TJCanvas2D.PAGE_EDIT_HEIGHT_RATIO); 
        int pageWidth = (int)(pageHeight * TJCanvas2D.PAGE_ASPECT_RATIO);
        int startX = (appWidth - pageWidth * 2) / 2;
        int startY = (appHeight - pageHeight) / 2;
        
        return new Rectangle(startX, startY, pageWidth * 2, pageHeight);
    }

    public void updatePageBounds(TJ tj) {
        Rectangle totalBounds = getTotalPageBounds(tj);
        TJPage[] pagesArray = tj.getPageMgr().getCurPage();
        List<TJPage> pages = Arrays.asList(pagesArray);

        mLeftPageBounds = null;
        mRightPageBounds = null;

        int pageWidth = totalBounds.width / pages.size();
        int startX = totalBounds.x;
        int startY = totalBounds.y;
        
        // Create bounds for each page
        for (int i = 0; i < pages.size(); i++) {
            Rectangle pageBounds = new Rectangle(startX + i * pageWidth, startY, pageWidth, totalBounds.height);
            if (i == 0) {
                mLeftPageBounds = pageBounds;
            } else if (i == 1) {
                mRightPageBounds = pageBounds;
            }
        }
    }

        
    // Inner class for Emoji Drawing Scene - handles actual drawing interaction
    public static class EmojiDrawScene extends TJScene {
        private JPanel mTopNavPanel;
        private JPanel mBottomNavPanel;
        private Ellipse2D.Double mEmojiCircle;
        private static final Color EMOJI_CIRCLE_COLOR = Color.YELLOW;
        private static final double CIRCLE_RADIUS_RATIO = 0.25; // 25% of page width
        
        private static EmojiDrawScene mSingleton = null;
        public static EmojiDrawScene getSingleton() {
            assert(EmojiDrawScene.mSingleton != null);
            return EmojiDrawScene.mSingleton;
        }
        public static EmojiDrawScene createSingleton(XScenario scenario) {
            assert(EmojiDrawScene.mSingleton == null);
            EmojiDrawScene.mSingleton = new EmojiDrawScene(scenario);
            return EmojiDrawScene.mSingleton;
        }

        private EmojiDrawScene(XScenario scenario) {
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
            this.mBottomNavPanel = TJNavPanel.createEmojiDrawBottomNavPanel(tj, this);
        }

        @Override
        public void handleMousePress(MouseEvent e) {
            TJ tj = (TJ)this.mScenario.getApp();
            TJEmojiScenario scenario = (TJEmojiScenario)this.mScenario;
            Point screenPt = e.getPoint();

            System.out.println("[DEBUG-EMOJI] EmojiDrawScene.handleMousePress: screenPt = " + screenPt);
            System.out.println("[DEBUG-EMOJI] EmojiDrawScene is currently active");
            
            // Store the mouse point for use when transitioning
            scenario.mLastMousePoint = screenPt;
            
            List<TJPage> pages = Arrays.asList(tj.getPageMgr().getCurPage());
            if (pages.isEmpty()) return;
            // Only transition to drawing scene if click is inside emoji circle
            if (scenario.mTargetPage != null &&
                mEmojiCircle != null &&
                mEmojiCircle.contains(screenPt)) {

                System.out.println("[DEBUG-EMOJI] EmojiDrawScene transitioning to EmojiDrawingScene at point: " + screenPt);
                XCmdToChangeScene.execute(tj, EmojiDrawingScene.getSingleton(), this);
            }
        }

        
        @Override
        public void handleMouseDrag(MouseEvent e) {
            TJ tj = (TJ)this.mScenario.getApp();
            TJEmojiScenario scenario = (TJEmojiScenario)this.mScenario;
            Point screenPt = e.getPoint();

            if (scenario.mTargetPage != null &&
                tj.getPtCurveMgr().getCurPtCurve() != null &&
                mEmojiCircle != null &&
                mEmojiCircle.contains(screenPt)) {

                TJCmdToUpdateCurPtCurve.execute(tj, screenPt);
            }
        }

        
        @Override
        public void handleMouseRelease(MouseEvent e) {
            TJ tj = (TJ) this.mScenario.getApp();
            TJEmojiScenario scenario = (TJEmojiScenario)this.mScenario;
            
            System.out.println("[EmojiDrawScene] Mouse released at: " + e.getPoint());
            System.out.println("[EmojiDrawScene] mTargetPage=" + scenario.mTargetPage + 
                ", curPtCurve=" + tj.getPtCurveMgr().getCurPtCurve());
            
            // Only finalize the curve if one was actually created
            if (scenario.mTargetPage != null && tj.getPtCurveMgr().getCurPtCurve() != null) {
                System.out.println("[EmojiDrawScene] Adding curve to page");
                TJCmdToAddCurPtCurveToPtCurves.execute(tj, scenario.mTargetPage);
            }
            // Keep mTargetPage set so user can draw multiple strokes without returning to ready scene
            // User must explicitly control when to go back (via button or ESC key)
        }

        @Override
        public void handleKeyDown(KeyEvent e) {
            System.out.println("a key is pressed");
            TJ tj = (TJ)this.mScenario.getApp();
            TJEmojiScenario scenario = (TJEmojiScenario)this.mScenario;
            int code = e.getKeyCode();
            
            switch (code) {
                case KeyEvent.VK_ESCAPE:
                    // ESC key to go back to previous scene
                    scenario.mTargetPage = null;  // Clear target page when returning
                    XCmdToChangeScene.execute(tj, this.mReturnScene, null);
                    break;
                case KeyEvent.VK_UP:
                    System.out.println("add stroke width pls");
                    TJCmdToIncreaseStrokeWidthForCurPtCurve.execute(tj,
                        TJCanvas2D.STROKE_WIDTH_INCREMENT);
                    break;
                case KeyEvent.VK_DOWN:
                    TJCmdToIncreaseStrokeWidthForCurPtCurve.execute(tj,
                        -TJCanvas2D.STROKE_WIDTH_INCREMENT);
                    break;
            }
        }

        @Override
        public void handleKeyUp(KeyEvent e) {
        }

        @Override
        public void updateSupportObjects() {
            ((TJEmojiScenario)this.mScenario).updatePageBounds((TJ)this.mScenario.getApp());
        }

        @Override
        public void drawBackground(Graphics2D g2) {
            TJ tj = (TJ)this.mScenario.getApp();
            TJCanvas2D canvas = tj.getCanvas2D();
            
            g2.setColor(TJCanvas2D.COLOR_BACKGROUND_DARK); 
            g2.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        }

        @Override
        public void renderWorldObjects(Graphics2D g2) {
            TJ tj = (TJ)this.mScenario.getApp();
            TJCanvas2D canvas = tj.getCanvas2D();
            TJEmojiScenario scenario = (TJEmojiScenario)this.mScenario;

            // Ensure page bounds are initialized
            scenario.updatePageBounds(tj);

            List<TJPage> pages = Arrays.asList(tj.getPageMgr().getCurPage());
            if (pages.isEmpty()) return;

            int startY = scenario.getLeftPageBounds().y;
            int pageHeight = scenario.getLeftPageBounds().height;
            int pageWidth = scenario.getLeftPageBounds().width;
            int totalWidth = pageWidth * pages.size();
            int startX = (canvas.getWidth() - totalWidth) / 2;

            // Draw pages
            for (int i = 0; i < pages.size(); i++) {
                TJPage page = pages.get(i);
                int pageX = startX + i * pageWidth;
                drawPageStructure(g2, pageX, startY, pageWidth, pageHeight);
                canvas.drawPtCurves(g2, page.getPtCurves());
                canvas.drawSelectedPtCurves(g2, page.getSelectedPtCurves());
            }

            // Update emoji circle to match page layout
            updateEmojiCircle(startX, startY, totalWidth, pageHeight);

            // Draw emoji circle
            drawEmojiCircle(g2, startX, startY, totalWidth, pageHeight);

            // Draw current curve on target page
            canvas.drawCurPtCurve(g2);
        }

        @Override
        public void renderScreenObjects(Graphics2D g2) {
            TJ tj = (TJ)this.mScenario.getApp();
            TJCanvas2D canvas = tj.getCanvas2D();
        }
        
        @Override
        public void getReady() {
            TJ tj = (TJ)this.mScenario.getApp();
            TJEmojiScenario scenario = (TJEmojiScenario)this.mScenario;
            
            if (this.mTopNavPanel == null) {
                initializeTopNav();
            }
            if (this.mBottomNavPanel == null) {
                initializeBottomNav();
            }
            
            tj.setTopPanel(this.mTopNavPanel);
            tj.setBottomPanel(this.mBottomNavPanel);
            
            // Set target page to right page for emoji drawing
            scenario.mTargetPage = tj.getPageMgr().getCurPage()[1];
            
            // Initialize circle bounds before any mouse input
            initializeEmojiCircle(tj);
        }

        @Override
        public void wrapUp() {
            TJ tj = (TJ)this.mScenario.getApp();
            tj.setTopPanel(null);
            tj.setBottomPanel(null);
        }
        
        private void initializeEmojiCircle(TJ tj) {
            TJEmojiScenario scenario = (TJEmojiScenario)this.mScenario;
            TJCanvas2D canvas = tj.getCanvas2D();
            
            Rectangle leftBounds = scenario.getLeftPageBounds();
            Rectangle rightBounds = scenario.getRightPageBounds();
            if (leftBounds == null || rightBounds == null) {
                scenario.updatePageBounds(tj);
                leftBounds = scenario.getLeftPageBounds();
                rightBounds = scenario.getRightPageBounds();
            }
            
            int startY = leftBounds.y;
            int pageHeight = leftBounds.height;
            int singlePageWidth = leftBounds.width;
            int newWidth = singlePageWidth * 2;
            int centerX = (canvas.getWidth() / 2);
            int startX = centerX - (newWidth / 2);
            
            updateEmojiCircle(startX, startY, newWidth, pageHeight);
        }
        
        // Update circle bounds to match current page layout
        private void updateEmojiCircle(int startX, int startY, int pageWidth, int pageHeight) {
            int centerPageX = startX + pageWidth / 2;
            int centerPageY = startY + pageHeight / 2;
            int radius = (int)(pageWidth * CIRCLE_RADIUS_RATIO / 2);
            int diameter = radius * 2;
            
            int x = centerPageX - radius;
            int y = centerPageY - radius;
            
            mEmojiCircle = new Ellipse2D.Double(x, y, diameter, diameter);
        }

        private void drawPageStructure(Graphics2D g2, int startX, int startY, int pageWidth, int pageHeight) {
            // Draw centered page with specified width without divider
            g2.setColor(Color.WHITE);
            g2.fillRect(startX, startY, pageWidth, pageHeight);
            g2.setStroke(TJHomeScenario.PAGE_BORDER_STROKE);
            g2.setColor(Color.LIGHT_GRAY);
            g2.drawRect(startX, startY, pageWidth, pageHeight);
        }

        private void drawEmojiCircle(Graphics2D g2, int startX, int startY, int pageWidth, int pageHeight) {
            int centerX = startX + pageWidth / 2;
            int centerY = startY + pageHeight / 2;
            int radius = (int)(pageWidth * CIRCLE_RADIUS_RATIO / 2);
            int diameter = radius * 2;
            
            int x = centerX - radius;
            int y = centerY - radius;
            
            g2.setColor(EMOJI_CIRCLE_COLOR);
            g2.fill(mEmojiCircle);
            
            g2.setColor(Color.DARK_GRAY);
            g2.setStroke(new BasicStroke(2));
            g2.draw(mEmojiCircle);
        }
        
        private void drawCurPtCurveInPageSpace(Graphics2D g2, TJ tj) {
            TJEmojiScenario scenario = (TJEmojiScenario)this.mScenario;
            TJPtCurve curPtCurve = tj.getPtCurveMgr().getCurPtCurve();
            if (curPtCurve == null) {
                return;
            }
            
            Rectangle targetBounds = scenario.getTargetBounds();
            if (targetBounds == null) {
                return;
            }
            
            // Draw the curve by converting world coordinates to page-local coordinates
            ArrayList<Point2D.Double> pts = curPtCurve.getPts();
            if (pts.size() < 2) {
                return;
            }
            
            Path2D.Double path = new Path2D.Double();
            Point2D.Double pt0 = pts.get(0);
            // Convert first point from world to page-local
            double localX0 = pt0.x - targetBounds.x;
            double localY0 = pt0.y - targetBounds.y;
            path.moveTo(localX0, localY0);
            
            for (int i = 1; i < pts.size(); i++) {
                Point2D.Double pt = pts.get(i);
                // Convert each point from world to page-local
                double localX = pt.x - targetBounds.x;
                double localY = pt.y - targetBounds.y;
                path.lineTo(localX, localY);
            }
            
            g2.setColor(curPtCurve.getColor());
            g2.setStroke(curPtCurve.getStroke());
            g2.draw(path);
        }
    }

    // Inner class for Emoji Drawing Scene
    public static class EmojiDrawingScene extends TJScene {
        private JPanel mTopNavPanel;
        private JPanel mBottomNavPanel;
        private static EmojiDrawingScene mSingleton = null;
        private Ellipse2D.Double mEmojiCircle;
        private static final Color EMOJI_CIRCLE_COLOR = Color.YELLOW;
        private static final double CIRCLE_RADIUS_RATIO = 0.25; // 25% of page width
        
        public static EmojiDrawingScene getSingleton() {
            assert(EmojiDrawingScene.mSingleton != null);
            return EmojiDrawingScene.mSingleton;
        }
        public static EmojiDrawingScene createSingleton(XScenario scenario) {
            assert(EmojiDrawingScene.mSingleton == null);
            EmojiDrawingScene.mSingleton = new EmojiDrawingScene(scenario);
            return EmojiDrawingScene.mSingleton;
        }
        public Ellipse2D.Double getEmojiCircle() {
            return this.mEmojiCircle;
        }
        private EmojiDrawingScene(XScenario scenario) {
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
            this.mBottomNavPanel = TJNavPanel.createEmojiDrawBottomNavPanel(tj, this);
        }

        @Override
        public void handleMousePress(MouseEvent e) {
            TJ tj = (TJ)this.mScenario.getApp();
            TJEmojiScenario scenario = (TJEmojiScenario)this.mScenario;
            Point screenPt = e.getPoint();
            
            System.out.println("[DEBUG-EMOJI] handleMousePress: screenPt = " + screenPt);
            
            // Only create/update curve if point is inside the circle
            if (mEmojiCircle != null && mEmojiCircle.contains(screenPt)) {
                System.out.println("[DEBUG-EMOJI] Mouse press INSIDE circle at: " + screenPt);
                System.out.println("[DEBUG-EMOJI] Circle bounds: " + mEmojiCircle);
                
                if (tj.getPtCurveMgr().getCurPtCurve() == null) {
                    // Create new curve
                    System.out.println("[DEBUG-EMOJI] Creating new curve at position: " + screenPt);
                    TJCmdToCreateCurPtCurve.execute(tj, screenPt);
                    System.out.println("[DEBUG-EMOJI] Curve created. Current curve: " + tj.getPtCurveMgr().getCurPtCurve());
                } else {
                    // Update existing curve
                    System.out.println("[DEBUG-EMOJI] Updating existing curve at position: " + screenPt);
                    TJCmdToUpdateCurPtCurve.execute(tj, screenPt);
                }
            } else {
                System.out.println("[DEBUG-EMOJI] Mouse press OUTSIDE circle at: " + screenPt);
                if (mEmojiCircle != null) {
                    System.out.println("[DEBUG-EMOJI] Circle bounds: " + mEmojiCircle);
                } else {
                    System.out.println("[DEBUG-EMOJI] ERROR: mEmojiCircle is NULL");
                }
            }
        }

        @Override
        public void handleMouseDrag(MouseEvent e) {
            TJ tj = (TJ)this.mScenario.getApp();
            TJEmojiScenario scenario = (TJEmojiScenario)this.mScenario;
            Point screenPt = e.getPoint();
            
            if (scenario.mTargetPage != null && mEmojiCircle != null) {
                // Only update curve if point is within the circle
                if (mEmojiCircle.contains(screenPt)) {
                    System.out.println("[DEBUG-EMOJI] handleMouseDrag INSIDE circle at: " + screenPt);
                    TJCmdToUpdateCurPtCurve.execute(tj, screenPt);
                } else {
                    System.out.println("[DEBUG-EMOJI] handleMouseDrag OUTSIDE circle at: " + screenPt);
                }
            } else {
                System.out.println("[DEBUG-EMOJI] handleMouseDrag: targetPage=" + scenario.mTargetPage + ", circle=" + mEmojiCircle);
            }
        }

        @Override
        public void handleMouseRelease(MouseEvent e) {
            TJ tj = (TJ) this.mScenario.getApp();
            TJEmojiScenario scenario = (TJEmojiScenario)this.mScenario;
            
            System.out.println("[DEBUG-EMOJI] handleMouseRelease called");
            System.out.println("[DEBUG-EMOJI] targetPage: " + scenario.mTargetPage);
            System.out.println("[DEBUG-EMOJI] curPtCurve: " + tj.getPtCurveMgr().getCurPtCurve());
            
            if (scenario.mTargetPage != null && tj.getPtCurveMgr().getCurPtCurve() != null) {
                // Finalize the current curve
                System.out.println("[DEBUG-EMOJI] Finalizing curve. Curve details: " + tj.getPtCurveMgr().getCurPtCurve());
                System.out.println("[DEBUG-EMOJI] Total saved curves: " + tj.getPtCurveMgr().getPtCurves().size());
                TJCmdToAddCurPtCurveToPtCurves.execute(tj, scenario.mTargetPage);
                System.out.println("[DEBUG-EMOJI] Curve added. Total curves now: " + tj.getPtCurveMgr().getPtCurves().size());
            } else {
                System.out.println("[DEBUG-EMOJI] Could not finalize: targetPage=" + scenario.mTargetPage + ", curPtCurve=" + tj.getPtCurveMgr().getCurPtCurve());
            }
            // Return to EmojiDrawScene after each stroke
            System.out.println("[DEBUG-EMOJI] Returning to EmojiDrawScene");
            XCmdToChangeScene.execute(tj, TJEmojiScenario.EmojiDrawScene.getSingleton(), this);
        }

        @Override
        public void handleKeyDown(KeyEvent e) {
            TJ tj = (TJ)this.mScenario.getApp();
            TJEmojiScenario scenario = (TJEmojiScenario)this.mScenario;
            int code = e.getKeyCode();
            
            switch (code) {
                case KeyEvent.VK_ESCAPE:
                    // ESC key to go back to EmojiDrawScene
                    scenario.mTargetPage = null;
                    XCmdToChangeScene.execute(tj, TJEmojiScenario.EmojiDrawScene.getSingleton(), this);
                    break;
                case KeyEvent.VK_UP:
                    TJCmdToIncreaseStrokeWidthForCurPtCurve.execute(tj,
                        TJCanvas2D.STROKE_WIDTH_INCREMENT);
                    break;
                case KeyEvent.VK_DOWN:
                    TJCmdToIncreaseStrokeWidthForCurPtCurve.execute(tj,
                        -TJCanvas2D.STROKE_WIDTH_INCREMENT);
                    break;
            }
        }

        @Override
        public void handleKeyUp(KeyEvent e) {
        }

        @Override
        public void updateSupportObjects() {
            ((TJEmojiScenario)this.mScenario).updatePageBounds((TJ)this.mScenario.getApp());
        }

        @Override
        public void drawBackground(Graphics2D g2) {
            TJ tj = (TJ)this.mScenario.getApp();
            TJCanvas2D canvas = tj.getCanvas2D();
            
            g2.setColor(TJCanvas2D.COLOR_BACKGROUND_DARK); 
            g2.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        }

        @Override
        public void renderWorldObjects(Graphics2D g2) {
            TJ tj = (TJ)this.mScenario.getApp();
            TJCanvas2D canvas = tj.getCanvas2D();
            TJEmojiScenario scenario = (TJEmojiScenario)this.mScenario;

            // Ensure page bounds are initialized
            scenario.updatePageBounds(tj);

            List<TJPage> pages = Arrays.asList(tj.getPageMgr().getCurPage());
            if (pages.isEmpty()) return;

            int startY = scenario.getLeftPageBounds().y;
            int pageHeight = scenario.getLeftPageBounds().height;
            int pageWidth = scenario.getLeftPageBounds().width;
            int totalWidth = pageWidth * pages.size();
            int startX = (canvas.getWidth() - totalWidth) / 2;

            // Draw pages
            for (int i = 0; i < pages.size(); i++) {
                TJPage page = pages.get(i);
                int pageX = startX + i * pageWidth;
                drawPageStructure(g2, pageX, startY, pageWidth, pageHeight);
                canvas.drawPtCurves(g2, page.getPtCurves());
                canvas.drawSelectedPtCurves(g2, page.getSelectedPtCurves());
            }

            // Update emoji circle to match page layout
            updateEmojiCircle(startX, startY, totalWidth, pageHeight);

            // Draw emoji circle
            drawEmojiCircle(g2, startX, startY, totalWidth, pageHeight);

            // Draw current curve on target page
            canvas.drawCurPtCurve(g2);
        }


        @Override
        public void renderScreenObjects(Graphics2D g2) {
        }

        @Override
        public void getReady() {
            TJ tj = (TJ)this.mScenario.getApp();
            
            System.out.println("[DEBUG-EMOJI] EmojiDrawingScene.getReady() called");
            
            if (this.mTopNavPanel == null) {
                initializeTopNav();
            }
            if (this.mBottomNavPanel == null) {
                initializeBottomNav();
            }
            
            tj.setTopPanel(this.mTopNavPanel);
            tj.setBottomPanel(this.mBottomNavPanel);
            
            // Initialize circle bounds before any mouse input
            System.out.println("[DEBUG-EMOJI] Initializing emoji circle...");
            initializeEmojiCircle(tj);
            System.out.println("[DEBUG-EMOJI] Emoji circle initialized: " + mEmojiCircle);
        }

        @Override
        public void wrapUp() {
            TJ tj = (TJ)this.mScenario.getApp();
            tj.setTopPanel(null);
            tj.setBottomPanel(null);
        }
        
        private void initializeEmojiCircle(TJ tj) {
            TJEmojiScenario scenario = (TJEmojiScenario)this.mScenario;
            TJCanvas2D canvas = tj.getCanvas2D();
            
            System.out.println("[DEBUG-EMOJI] EmojiDrawingScene.initializeEmojiCircle() called");
            
            Rectangle leftBounds = scenario.getLeftPageBounds();
            Rectangle rightBounds = scenario.getRightPageBounds();
            System.out.println("[DEBUG-EMOJI] leftBounds: " + leftBounds + ", rightBounds: " + rightBounds);
            
            if (leftBounds == null || rightBounds == null) {
                System.out.println("[DEBUG-EMOJI] Bounds null, updating page bounds...");
                scenario.updatePageBounds(tj);
                leftBounds = scenario.getLeftPageBounds();
                rightBounds = scenario.getRightPageBounds();
                System.out.println("[DEBUG-EMOJI] After update - leftBounds: " + leftBounds + ", rightBounds: " + rightBounds);
            }
            
            int startY = leftBounds.y;
            int pageHeight = leftBounds.height;
            int singlePageWidth = leftBounds.width;
            int newWidth = singlePageWidth * 2;
            int centerX = (canvas.getWidth() / 2);
            int startX = centerX - (newWidth / 2);
            
            System.out.println("[DEBUG-EMOJI] Circle params: startX=" + startX + ", startY=" + startY + ", pageWidth=" + newWidth + ", pageHeight=" + pageHeight);
            
            updateEmojiCircle(startX, startY, newWidth, pageHeight);
            System.out.println("[DEBUG-EMOJI] EmojiDrawingScene circle initialized: " + mEmojiCircle);
        }
        
        // Update circle bounds to match current page layout
        private void updateEmojiCircle(int startX, int startY, int pageWidth, int pageHeight) {
            int centerPageX = startX + pageWidth / 2;
            int centerPageY = startY + pageHeight / 2;
            int radius = (int)(pageWidth * CIRCLE_RADIUS_RATIO / 2);
            int diameter = radius * 2;
            
            int x = centerPageX - radius;
            int y = centerPageY - radius;
            
            mEmojiCircle = new Ellipse2D.Double(x, y, diameter, diameter);
            System.out.println("[DEBUG-EMOJI] EmojiDrawingScene.updateEmojiCircle: created circle at (" + x + "," + y + ") with diameter=" + diameter);
        }
        
        private void drawPageStructure(Graphics2D g2, int startX, int startY, int pageWidth, int pageHeight) {
            // Draw centered page with specified width without divider
            g2.setColor(Color.WHITE);
            g2.fillRect(startX, startY, pageWidth, pageHeight);
            g2.setStroke(TJHomeScenario.PAGE_BORDER_STROKE);
            g2.setColor(Color.LIGHT_GRAY);
            g2.drawRect(startX, startY, pageWidth, pageHeight);
        }

        private void drawEmojiCircle(Graphics2D g2, int startX, int startY, int pageWidth, int pageHeight) {
            int centerX = startX + pageWidth / 2;
            int centerY = startY + pageHeight / 2;
            int radius = (int)(pageWidth * CIRCLE_RADIUS_RATIO / 2);
            int diameter = radius * 2;
            
            int x = centerX - radius;
            int y = centerY - radius;
            
            g2.setColor(EMOJI_CIRCLE_COLOR);
            g2.fill(mEmojiCircle);
            
            g2.setColor(Color.DARK_GRAY);
            g2.setStroke(new BasicStroke(2));
            g2.draw(mEmojiCircle);
        }
        
        private void drawCurPtCurveInPageSpace(Graphics2D g2, TJ tj) {
            TJEmojiScenario scenario = (TJEmojiScenario)this.mScenario;
            TJPtCurve curPtCurve = tj.getPtCurveMgr().getCurPtCurve();
            if (curPtCurve == null) {
                return;
            }
            
            Rectangle targetBounds = scenario.getTargetBounds();
            if (targetBounds == null) {
                return;
            }
            
            // Draw the curve by converting world coordinates to page-local coordinates
            ArrayList<Point2D.Double> pts = curPtCurve.getPts();
            if (pts.size() < 2) {
                return;
            }
            
            Path2D.Double path = new Path2D.Double();
            Point2D.Double pt0 = pts.get(0);
            // Convert first point from world to page-local
            double localX0 = pt0.x - targetBounds.x;
            double localY0 = pt0.y - targetBounds.y;
            path.moveTo(localX0, localY0);
            
            for (int i = 1; i < pts.size(); i++) {
                Point2D.Double pt = pts.get(i);
                // Convert each point from world to page-local
                double localX = pt.x - targetBounds.x;
                double localY = pt.y - targetBounds.y;
                path.lineTo(localX, localY);
            }
            
            g2.setColor(curPtCurve.getColor());
            g2.setStroke(curPtCurve.getStroke());
            g2.draw(path);
        }
    }
}

