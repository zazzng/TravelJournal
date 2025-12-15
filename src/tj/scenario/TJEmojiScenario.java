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
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.JPanel;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import tj.TJ;
import tj.TJCanvas2D;
import tj.TJEmojiPage;
import tj.TJPtCurve;
import tj.TJScene;
import tj.cmd.TJCmdToAddCurPtCurveToEmojiPage;
import tj.cmd.TJCmdToCreateCurPtCurve;
import tj.cmd.TJCmdToIncreaseStrokeWidthForCurPtCurve;
import tj.cmd.TJCmdToUpdateCurPtCurve;
import utils.TJNavPanel;
import utils.ImageLoader;
import x.XApp;
import x.XCmdToChangeScene;
import x.XScenario;

public class TJEmojiScenario extends XScenario {
    public Ellipse2D.Double mTargetCircle = null;
    public Point mLastMousePoint = null; // Track last click point for scene transitions
    private TJEmojiPage mTargetEmojiPage = null; // The emoji page for circle drawing
    private Color mEmojiFaceColor = Color.YELLOW; // Default face color for the circle
    
    public Color getEmojiFaceColor() {
        return this.mEmojiFaceColor;
    }
    
    public void setEmojiFaceColor(Color color) {
        this.mEmojiFaceColor = color;
    }
    
    public Ellipse2D.Double getTargetBounds() {
        return this.mTargetCircle;
    }
    
    // On-demand getter for authoritative emoji page (set by circle interaction)
    public TJEmojiPage getTargetEmojiPage() {
        return this.mTargetEmojiPage;
    }
    
    public void setTargetEmojiPage(TJEmojiPage page) {
        this.mTargetEmojiPage = page;
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
        this.addScene(TJEmojiScenario.FaceColorChangeScene.createSingleton(this));
    }

    public Rectangle getTotalPageBounds(TJ tj) {
        TJCanvas2D canvas = tj.getCanvas2D();
        int appWidth = canvas.getWidth();
        int appHeight = canvas.getHeight();
        
        int pageHeight = (int)(appHeight * TJCanvas2D.PAGE_EDIT_HEIGHT_RATIO); 
        int pageWidth = (int)(pageHeight * TJCanvas2D.PAGE_ASPECT_RATIO);
        int startX = (appWidth - pageWidth * 2) / 2;
        int startY = (appHeight - pageHeight) / 2;
        
        return new Rectangle(startX, startY, pageWidth * 2, pageHeight);
    }

    // Centralized method for drawing emoji page content with clipping
    public void drawEmojiPageAndContent(Graphics2D g2, TJCanvas2D canvas, Ellipse2D.Double circle, TJEmojiPage emojiPage) {
        // Draw circle background using the configured face color
        g2.setColor(this.mEmojiFaceColor);
        g2.fill(circle);
        
        g2.setColor(Color.DARK_GRAY);
        g2.setStroke(new BasicStroke(2));
        g2.draw(circle);
        
        // Clipping to the emoji circle
        Shape originalClip = g2.getClip();
        g2.setClip(circle);
        
        // Draw content inside the clipped area (curves only, no images)
        if (emojiPage != null) {
            canvas.drawPtCurves(g2, emojiPage.getPtCurves());
            canvas.drawSelectedPtCurves(g2, emojiPage.getSelectedPtCurves());
            canvas.drawCurPtCurve(g2);
        }
        
        g2.setClip(originalClip);
    }
    
    // Export emoji circle drawing to a local image file
    public static void exportEmojiCircleDrawing(TJ tj) {
        TJEmojiScenario scenario = TJEmojiScenario.getSingle();
        Ellipse2D.Double circle = scenario.getTargetBounds();
        TJEmojiPage emojiPage = scenario.getTargetEmojiPage();
        
        if (circle == null || emojiPage == null) {
            System.out.println("[DEBUG-EMOJI] Cannot export: circle or emoji page is null");
            return;
        }
        
        // Get circle bounds
        int x = (int) circle.x;
        int y = (int) circle.y;
        int diameter = (int) circle.width;
        
        // Create a BufferedImage for the circle area
        BufferedImage circleImage = new BufferedImage(diameter, diameter, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = circleImage.createGraphics();
        
        // Set rendering hints for quality
        g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, 
            java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw white background
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, diameter, diameter);
        
        // Create translated circle at origin for drawing
        Ellipse2D.Double translatedCircle = new Ellipse2D.Double(0, 0, diameter, diameter);
        
        // Draw circle background using the configured face color
        g2.setColor(scenario.getEmojiFaceColor());
        g2.fill(translatedCircle);
        
        g2.setColor(Color.DARK_GRAY);
        g2.setStroke(new BasicStroke(2));
        g2.draw(translatedCircle);
        
        // Apply clipping to circle
        Shape originalClip = g2.getClip();
        g2.setClip(translatedCircle);
        
        // Translate graphics to account for circle position
        g2.translate(-x, -y);
        
        // Apply world to screen transform
        g2.transform(tj.getXform().getCurrentXformFromWorldToScreen());
        
        // Draw curves in the translated space
        tj.getCanvas2D().drawPtCurves(g2, emojiPage.getPtCurves());
        tj.getCanvas2D().drawSelectedPtCurves(g2, emojiPage.getSelectedPtCurves());
        
        g2.dispose();
        
        // Show file chooser to save the image
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export Emoji Circle Drawing");
        chooser.setFileFilter(new FileNameExtensionFilter("PNG Image", "png"));
        chooser.setSelectedFile(new File("emoji_drawing.png"));
        
        int result = chooser.showSaveDialog(tj.getCanvas2D());
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            String filePath = selectedFile.getAbsolutePath();
            // Remove extension if already present
            if (filePath.endsWith(".png")) {
                filePath = filePath.substring(0, filePath.length() - 4);
            }
            
            // Save the image
            if (ImageLoader.saveImage(circleImage, filePath, "png")) {
            } else {
                System.out.println("[DEBUG-EMOJI] ❌ Failed to export emoji circle drawing");
            }
        }
    }
    
    // Open color picker to select emoji face color
    public static void openFaceColorPicker(TJ tj, TJScene returnScene) {
        XCmdToChangeScene.execute(tj, 
            TJEmojiScenario.FaceColorChangeScene.getSingleton(), 
            returnScene);
    }
    
    // Inner class for Emoji Drawing Scene - handles actual drawing interaction
    public static class EmojiDrawScene extends TJScene {
        private JPanel mTopNavPanel;
        private JPanel mBottomNavPanel;
        public Ellipse2D.Double mEmojiCircle;
        private static final Color EMOJI_CIRCLE_COLOR = Color.YELLOW;
        private static final double CIRCLE_RADIUS_RATIO = 0.5; // 50% of page height
        
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
            this.mBottomNavPanel = TJNavPanel.createBottomNavPanel(tj, this);
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

            // Only transition to drawing scene if click is inside emoji circle
            if (mEmojiCircle != null &&
                mEmojiCircle.contains(screenPt)) {
                // Set the emoji page as the target page for emoji drawing
                scenario.mTargetEmojiPage = tj.getJournalBookMgr().getCurEmojiPage();
                System.out.println("[DEBUG-EMOJI] Set target emoji page for circle drawing: " + scenario.mTargetEmojiPage);
                XCmdToChangeScene.execute(tj, EmojiDrawingScene.getSingleton(), this);
            }
        }


        @Override
        public void handleMouseDrag(MouseEvent e) {
        }

        
        @Override
        public void handleMouseRelease(MouseEvent e) {
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
                case KeyEvent.VK_SHIFT:
                    System.out.println("shift key pressed - entering selection mode");
                    XCmdToChangeScene.execute(tj,
                        TJEmojiSelectScenario.EmojiSelectReadyScene.getSingleton(),
                        TJEmojiScenario.EmojiDrawScene.getSingleton());
                    break;
            }
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
            TJEmojiScenario scenario = (TJEmojiScenario)this.mScenario;

            Ellipse2D.Double circle = scenario.getTargetBounds();
            if(circle == null) return;

            TJEmojiPage emojiPage = scenario.getTargetEmojiPage();
            if(emojiPage == null) return;

            // Draw circle with saved curves visible
            scenario.drawEmojiPageAndContent(g2, canvas, circle, emojiPage);
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
            
            // Target page will be fetched on-demand via getTargetPage()
            
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
            
            Rectangle totalBounds = scenario.getTotalPageBounds(tj);
            int startY = totalBounds.y;
            int pageHeight = totalBounds.height;
            int pageWidth = totalBounds.width;
            int centerX = (canvas.getWidth() / 2);
            int startX = centerX - (pageWidth / 2);
            
            updateEmojiCircle(startX, startY, pageWidth, pageHeight);
            // Set the target bounds to match the emoji circle
            scenario.mTargetCircle = mEmojiCircle;
        }
        
        // Update circle bounds to match current page layout
        private void updateEmojiCircle(int startX, int startY, int pageWidth, int pageHeight) {
            int centerPageX = startX + pageWidth / 2;
            int centerPageY = startY + pageHeight / 2;
            // Circle radius based on page height for consistent sizing
            int radius = (int)(pageHeight * CIRCLE_RADIUS_RATIO);
            int diameter = radius * 2;
            
            int x = centerPageX - radius;
            int y = centerPageY - radius;
            
            mEmojiCircle = new Ellipse2D.Double(x, y, diameter, diameter);
        }

        private void drawEmojiCircle(Graphics2D g2, Ellipse2D.Double circle) {
            g2.setColor(EMOJI_CIRCLE_COLOR);
            g2.fill(circle);
            
            g2.setColor(Color.DARK_GRAY);
            g2.setStroke(new BasicStroke(2));
            g2.draw(circle);
        }
    }

    // Inner class for Emoji Drawing Scene
    public static class EmojiDrawingScene extends TJScene {
        private JPanel mTopNavPanel;
        private JPanel mBottomNavPanel;
        private static EmojiDrawingScene mSingleton = null;
        public Ellipse2D.Double mEmojiCircle;
        private static final Color EMOJI_CIRCLE_COLOR = Color.YELLOW;
        private static final double CIRCLE_RADIUS_RATIO = 0.5; // 50% of page height
        
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
            this.mBottomNavPanel = TJNavPanel.createBottomNavPanel(tj, this);
        }

        @Override
        public void handleMousePress(MouseEvent e) {
            TJ tj = (TJ)this.mScenario.getApp();
            TJEmojiScenario scenario = (TJEmojiScenario)this.mScenario;
            Point screenPt = e.getPoint();
            
            // Convert screen coordinates to world coordinates
            Point2D.Double worldPt = tj.getXform().calcPtFromScreenToWorld(screenPt);

            // Clear any stale curve from other scenarios before starting
            tj.getPtCurveMgr().setCurPtCurve(null);
            // Create curve directly with world coordinates (bypassing TJCmdToCreateCurPtCurve which converts from screen)
            TJPtCurve ptCurve = new TJPtCurve(worldPt,
                tj.getCanvas2D().getCurColorForPtCurve(),
                tj.getCanvas2D().getCurStrokeForPtCurve()
            );
            tj.getPtCurveMgr().setCurPtCurve(ptCurve);
            System.out.println("[DEBUG-EMOJI] Started new curve at screen: " + screenPt + " world: " + worldPt);
        }

        @Override
        public void handleMouseDrag(MouseEvent e) {
            TJ tj = (TJ)this.mScenario.getApp();
            TJEmojiScenario scenario = (TJEmojiScenario)this.mScenario;
            Point screenPt = e.getPoint();
            
            // Only draw inside the circle boundary (screen space check)
            if (mEmojiCircle == null || !mEmojiCircle.contains(screenPt)) {
                return;
            }
            
            // Convert screen coordinates to world coordinates
            Point2D.Double worldPt = tj.getXform().calcPtFromScreenToWorld(screenPt);
            
            TJPtCurve curPtCurve = tj.getPtCurveMgr().getCurPtCurve();
            if (curPtCurve == null) {
                return;
            }
            
            // Check minimum distance between points (in world space)
            int size = curPtCurve.getPts().size();
            if (size > 0) {
                Point2D.Double lastWorldPt = curPtCurve.getPts().get(size - 1);
                if (worldPt.distance(lastWorldPt) < TJPtCurve.MIN_DIST_BTWN_PTS) {
                    return;
                }
            }
            
            // Add point in world coordinates
            curPtCurve.addPt(worldPt);
        }

        @Override
        public void handleMouseRelease(MouseEvent e) {
            TJ tj = (TJ) this.mScenario.getApp();
            TJEmojiScenario scenario = (TJEmojiScenario)this.mScenario;
            
            System.out.println("[DEBUG-EMOJI] handleMouseRelease called");
            TJEmojiPage targetEmojiPage = scenario.getTargetEmojiPage();
            System.out.println("[DEBUG-EMOJI] targetEmojiPage: " + targetEmojiPage);
            System.out.println("[DEBUG-EMOJI] curPtCurve: " + tj.getPtCurveMgr().getCurPtCurve());
            
            if (targetEmojiPage != null && tj.getPtCurveMgr().getCurPtCurve() != null) {
                // Finalize the current curve
                System.out.println("[DEBUG-EMOJI] Finalizing curve. Curve details: " + tj.getPtCurveMgr().getCurPtCurve());
                System.out.println("[DEBUG-EMOJI] Total saved curves: " + tj.getPtCurveMgr().getPtCurves().size());
                TJCmdToAddCurPtCurveToEmojiPage.execute(tj);
                System.out.println("[DEBUG-EMOJI] Curve added. Total curves now: " + tj.getPtCurveMgr().getPtCurves().size());
            } else {
                System.out.println("[DEBUG-EMOJI] Could not finalize: targetEmojiPage=" + targetEmojiPage + ", curPtCurve=" + tj.getPtCurveMgr().getCurPtCurve());
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
            TJEmojiScenario scenario = (TJEmojiScenario)this.mScenario;

            Ellipse2D.Double circle = scenario.getTargetBounds();
            if(circle == null) return;

            TJEmojiPage emojiPage = scenario.getTargetEmojiPage();
            if(emojiPage == null) return; 

            // Use the centralized drawing method from outer class
            scenario.drawEmojiPageAndContent(g2, canvas, circle, emojiPage);
        }


        @Override
        public void renderScreenObjects(Graphics2D g2) {
        }

        @Override
        public void getReady() {
            TJ tj = (TJ)this.mScenario.getApp();
            TJEmojiScenario scenario = (TJEmojiScenario)this.mScenario;

            
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

            if (scenario.mLastMousePoint != null &&
                mEmojiCircle.contains(scenario.mLastMousePoint)) {

                // Convert screen point to world coordinates before creating curve
                Point2D.Double worldPt = tj.getXform().calcPtFromScreenToWorld(scenario.mLastMousePoint);
                TJPtCurve ptCurve = new TJPtCurve(worldPt,
                    tj.getCanvas2D().getCurColorForPtCurve(),
                    tj.getCanvas2D().getCurStrokeForPtCurve()
                );
                tj.getPtCurveMgr().setCurPtCurve(ptCurve);
                System.out.println("[DEBUG-EMOJI] CurPtCurve created at screen: " + scenario.mLastMousePoint + " world: " + worldPt);
            }
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
            
            Rectangle totalBounds = scenario.getTotalPageBounds(tj);
            System.out.println("[DEBUG-EMOJI] totalBounds: " + totalBounds);
            
            int startY = totalBounds.y;
            int pageHeight = totalBounds.height;
            int pageWidth = totalBounds.width;
            int centerX = (canvas.getWidth() / 2);
            int startX = centerX - (pageWidth / 2);
            
            System.out.println("[DEBUG-EMOJI] Circle params: startX=" + startX + ", startY=" + startY + ", pageWidth=" + pageWidth + ", pageHeight=" + pageHeight);
            
            updateEmojiCircle(startX, startY, pageWidth, pageHeight);
            // Set the target bounds to match the emoji circle
            scenario.mTargetCircle = mEmojiCircle;
            System.out.println("[DEBUG-EMOJI] EmojiDrawingScene circle initialized: " + mEmojiCircle);
        }
        
        // Update circle bounds to match current page layout
        private void updateEmojiCircle(int startX, int startY, int pageWidth, int pageHeight) {
            int centerPageX = startX + pageWidth / 2;
            int centerPageY = startY + pageHeight / 2;
            
            // Circle radius should match EmojiDrawScene ratio for consistency
            int radius = (int)(pageHeight * CIRCLE_RADIUS_RATIO);
            int diameter = radius * 2;
            
            int x = centerPageX - radius;
            int y = centerPageY - radius;
            
            mEmojiCircle = new Ellipse2D.Double(x, y, diameter, diameter);
            System.out.println("[DEBUG-EMOJI] EmojiDrawingScene.updateEmojiCircle: center=(" + centerPageX + "," + centerPageY + ") radius=" + radius + " circle=(" + x + "," + y + "," + diameter + "," + diameter + ")");
        }

        private void drawEmojiCircle(Graphics2D g2, Ellipse2D.Double circle) {
            g2.setColor(EMOJI_CIRCLE_COLOR);
            g2.fill(circle);
            
            g2.setColor(Color.DARK_GRAY);
            g2.setStroke(new BasicStroke(2));
            g2.draw(circle);
        }
    }
    
    // Inner class for Face Color Change Scene
    public static class FaceColorChangeScene extends TJScene {
        private JPanel mTopNavPanel;
        private JPanel mBottomNavPanel;
        private static FaceColorChangeScene mSingleton = null;
        
        public static FaceColorChangeScene getSingleton() {
            assert(FaceColorChangeScene.mSingleton != null);
            return FaceColorChangeScene.mSingleton;
        }
        
        public static FaceColorChangeScene createSingleton(XScenario scenario) {
            assert(FaceColorChangeScene.mSingleton == null);
            FaceColorChangeScene.mSingleton = new FaceColorChangeScene(scenario);
            return FaceColorChangeScene.mSingleton;
        }
        
        private FaceColorChangeScene(XScenario scenario) {
            super(scenario);
        }
        
        private void initializeTopNav() {
            TJ tj = (TJ)this.mScenario.getApp();
            String title = "Choose Face Color";
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
            TJ tj = (TJ)this.mScenario.getApp();
            Point pt = e.getPoint();
            Color c = tj.getColorChooser().calcColor(pt, 
                tj.getCanvas2D().getWidth(), tj.getCanvas2D().getHeight());
            
            if (c != null) {
                TJEmojiScenario scenario = TJEmojiScenario.getSingle();
                scenario.setEmojiFaceColor(c);
                System.out.println("[DEBUG-EMOJI] Face color changed to: " + c);
                
                // Return to the return scene after color selection
                XCmdToChangeScene.execute(tj, 
                    this.mReturnScene, 
                    null);
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
        }
        
        @Override
        public void renderScreenObjects(Graphics2D g2) {
            TJ tj = (TJ)this.mScenario.getApp();
            TJCanvas2D canvas = tj.getCanvas2D();
            
            tj.getColorChooser().drawCells(g2, canvas.getWidth(), canvas.getHeight());
            
            canvas.drawPenTip(g2);
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

