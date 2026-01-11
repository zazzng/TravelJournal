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
import tj.TJDecorationDragMgr;
import tj.TJXform;
import tj.TJEmojiPage;
import tj.TJImage;
import tj.TJPtCurve;
import tj.TJScene;
import tj.cmd.TJCmdToAddCurPtCurveToEmojiPage;
import tj.cmd.TJCmdToCreateCurPtCurve;
import tj.cmd.TJCmdToIncreaseStrokeWidthForCurPtCurve;
import tj.cmd.TJCmdToTranslateTo;
import tj.cmd.TJCmdToSetStartScreenPt;
import tj.cmd.TJCmdToZoomTo;
import tj.cmd.TJCmdToUpdateCurPtCurve;
import utils.TJNavPanel;
import utils.ImageLoader;
import utils.TJDecorationPresetPanel;
import x.XApp;
import x.XCmdToChangeScene;
import x.XScenario;

public class TJEmojiScenario extends XScenario {
    public Ellipse2D.Double mTargetCircle = null;
    public Point mLastMousePoint = null; // Track last click point for scene transitions
    private TJEmojiPage mTargetEmojiPage = null; // The emoji page for circle drawing
    private Color mEmojiFaceColor = Color.YELLOW; // Default face color for the circle
    private TJDecorationDragMgr mDecorationDragMgr = new TJDecorationDragMgr(); // Decoration drag manager
    public TJDecorationPresetPanel.PresetIcon mCurrentDraggedPreset = null; // Track dragged preset across scenes
    private static TJImage sLastCreatedDecoration = null; // Prevent creating duplicate decorations in same drag
    private static Point2D.Double sDragOffsetWorld = null; // Track offset during decoration drag
    private TJDecorationPresetPanel mSharedDecorationPanel = null; // SHARED decoration panel for both scenes
    // Selection state for emoji decorations
    private TJImage mSelectedDecoration = null;
    public TJImage getSelectedDecoration() { return this.mSelectedDecoration; }
    // When selection changes, apply a visual cue (opacity) to selected and
    // revert the previous one back to full opacity.
    public void setSelectedDecoration(TJImage img) {
        // revert previous selection
        if (this.mSelectedDecoration != null) {
            this.mSelectedDecoration.setAlpha(1.0f);
        }
        this.mSelectedDecoration = img;
        // apply highlight on new selection
        if (this.mSelectedDecoration != null) {
            // Gentle dim so it stays clearly visible
            this.mSelectedDecoration.setAlpha(0.75f);
        }
    }
    
    public Color getEmojiFaceColor() {
        return this.mEmojiFaceColor;
    }
    
    public TJDecorationDragMgr getDecorationDragMgr() {
        return this.mDecorationDragMgr;
    }
    
    public void setEmojiFaceColor(Color color) {
        this.mEmojiFaceColor = color;
    }
    
    public Ellipse2D.Double getTargetBounds() {
        return this.mTargetCircle;
    }
    
    // Get the shared decoration panel (create if needed)
    public TJDecorationPresetPanel getSharedDecorationPanel() {
        if (this.mSharedDecorationPanel == null) {
            this.mSharedDecorationPanel = new TJDecorationPresetPanel(150, 600);
            System.out.println("[DEBUG-EMOJI] Created SHARED decoration panel at scenario level: " + this.mSharedDecorationPanel);
        }
        return this.mSharedDecorationPanel;
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
        this.addScene(TJEmojiScenario.EmojiDecorationSelectScene.createSingleton(this));
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
        // Convert the screen-space circle to world-space since g2 is already in world->screen
        TJ tj = (TJ)this.getApp();
        AffineTransform s2w = tj.getXform().getCurrentXformFromScreenToWorld();
        Shape worldCircle = s2w.createTransformedShape(circle);

        // Draw circle background using the configured face color
        g2.setColor(this.mEmojiFaceColor);
        g2.fill(worldCircle);

        g2.setColor(Color.DARK_GRAY);
        g2.setStroke(new BasicStroke(2));
        g2.draw(worldCircle);

        // Clipping to the emoji circle in world space
        Shape originalClip = g2.getClip();
        g2.setClip(worldCircle);

        // Draw content inside the clipped area (curves and current curve)
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

        // Draw decorations (stored in world space) under the same transform
        try {
            ArrayList<TJImage> decorations = emojiPage.getDecorations();
            for (TJImage decoration : decorations) {
                decoration.draw(g2);
            }
        } catch (Exception ex) {
            System.out.println("[DEBUG-EMOJI] Failed to draw decorations during export: " + ex);
        }
        
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
        private JPanel mDecorationPanel;
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
            // Ensure any decoration drag state is cleared when releasing on canvas
            TJ tj = (TJ)this.mScenario.getApp();
            TJEmojiScenario scenario = (TJEmojiScenario)this.mScenario;
            Point screenPt = e.getPoint();

            if (TJDecorationPresetPanel.sCurrentDraggedPreset != null) {
                System.out.println("[DEBUG-DECORATION] EmojiDrawScene: Mouse release with active preset drag at screen: " + screenPt);

                // If there is a recently created decoration, snap it to release point
                if (TJEmojiScenario.sLastCreatedDecoration != null) {
                    Point2D.Double worldPt = tj.getXform().calcPtFromScreenToWorld(screenPt);
                    TJEmojiScenario.sLastCreatedDecoration.setPosition(worldPt.x, worldPt.y);
                    System.out.println("[DEBUG-DECORATION] EmojiDrawScene: Finalized sLastCreatedDecoration position at world: " + worldPt);
                    tj.getCanvas2D().repaint();
                } else {
                    // Fallback: snap the most recent decoration on the target page
                    TJEmojiPage targetPage = scenario.getTargetEmojiPage();
                    if (targetPage != null && !targetPage.getDecorations().isEmpty()) {
                        ArrayList<TJImage> decorations = targetPage.getDecorations();
                        TJImage lastDecoration = decorations.get(decorations.size() - 1);
                        Point2D.Double worldPt = tj.getXform().calcPtFromScreenToWorld(screenPt);
                        lastDecoration.setPosition(worldPt.x, worldPt.y);
                        System.out.println("[DEBUG-DECORATION] EmojiDrawScene: Snapped last decoration from page to world: " + worldPt);
                        tj.getCanvas2D().repaint();
                    }
                }

                // Clear global drag state to prevent duplicate creation on next press
                TJDecorationPresetPanel.sCurrentDraggedPreset = null;
                TJEmojiScenario.sLastCreatedDecoration = null;
                TJEmojiScenario.sDragOffsetWorld = null;
                return;
            }
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
                case KeyEvent.VK_C:
                    XCmdToChangeScene.execute(tj,
                         TJColorScenario.ColorChangeScene.getSingleton(), this);
                    break;
                case KeyEvent.VK_F:
                    TJEmojiScenario.openFaceColorPicker(tj, this);
                    break;
                case KeyEvent.VK_E:
                    TJEmojiScenario.exportEmojiCircleDrawing(tj);
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
            
            // Draw decorations in WORLD space; g2 is already world->screen transformed
            Shape oldClip = g2.getClip();
            try {
                AffineTransform s2w = tj.getXform().getCurrentXformFromScreenToWorld();
                Shape worldCircle = s2w.createTransformedShape(circle);
                g2.setClip(worldCircle);
                ArrayList<TJImage> decorations = emojiPage.getDecorations();
                for (TJImage decoration : decorations) {
                    decoration.draw(g2);
                }
            } catch (Exception ex) {
                System.out.println("[DEBUG-RENDER] Failed to clip decorations: " + ex);
            } finally {
                g2.setClip(oldClip);
            }
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
            
            System.out.println("[DEBUG-EMOJI] EmojiDrawScene.getReady() called");
            
            if (this.mTopNavPanel == null) {
                initializeTopNav();
            }
            if (this.mBottomNavPanel == null) {
                initializeBottomNav();
            }
            
            // Get the SHARED decoration panel from scenario (not creating new one per scene)
            this.mDecorationPanel = scenario.getSharedDecorationPanel();
            System.out.println("[DEBUG-EMOJI] EmojiDrawScene: Using shared decoration panel: " + this.mDecorationPanel);
            
            // Always set up decoration listener - panel is shared between scenes
            if (this.mDecorationPanel instanceof TJDecorationPresetPanel) {
                System.out.println("[DEBUG-EMOJI] EmojiDrawScene: Setting up decoration listener for panel");
                TJDecorationPresetPanel presetPanel = (TJDecorationPresetPanel) this.mDecorationPanel;
                presetPanel.setDecorationListener(new TJDecorationPresetPanel.DecorationCreationListener() {
                    private TJImage mCurrentDecoration = null;
                    private java.awt.Point mLastScreenPoint = null; // last drag point
                    private Point2D.Double mDragOffsetWorld = null; // offset from center to grab point
                    
                    @Override
                    public void onDecorationDragStart(TJDecorationPresetPanel.PresetIcon preset) {
                        System.out.println("[DECORATION-LISTENER] EmojiDrawScene: Drag started for: " + preset.name);
                        mCurrentDecoration = null;
                        mDragOffsetWorld = null;
                        mLastScreenPoint = null;
                    }
                    
                    @Override
                    public void onDecorationDragMove(java.awt.Point screenPoint) {
                        EmojiDrawScene scene = EmojiDrawScene.this;
                        
                        if (TJDecorationPresetPanel.sCurrentDraggedPreset != null) {
                            TJ tj = (TJ)EmojiDrawScene.this.mScenario.getApp();
                            TJCanvas2D canvas = tj.getCanvas2D();
                            TJEmojiPage targetPage = tj.getJournalBookMgr().getCurEmojiPage();
                            
                            if (targetPage == null) {
                                System.out.println("[DECORATION-LISTENER] EmojiDrawScene: ERROR - Target emoji page is NULL!");
                                return;
                            }
                            
                            // Convert absolute screen point to canvas-local, then to world coordinates
                            java.awt.Point canvasLoc = canvas.getLocationOnScreen();
                            java.awt.Point canvasPoint = new java.awt.Point(
                                screenPoint.x - canvasLoc.x,
                                screenPoint.y - canvasLoc.y
                            );
                            System.out.println("[DECORATION-LISTENER] EmojiDrawScene: screenPoint=" + screenPoint + ", canvasPoint=" + canvasPoint + ", canvasLoc=" + canvasLoc);
                            Point2D.Double worldPt = tj.getXform().calcPtFromScreenToWorld(canvasPoint);
                            mLastScreenPoint = new java.awt.Point(screenPoint);
                            
                            // Only create decoration if we're over the emoji circle
                            if (mCurrentDecoration == null && scene.mEmojiCircle != null && scene.mEmojiCircle.contains(screenPoint)) {
                                // Create decoration with cursor at its center
                                mCurrentDecoration = new TJImage(
                                    TJDecorationPresetPanel.sCurrentDraggedPreset.image,
                                    (int)worldPt.x,
                                    (int)worldPt.y,
                                    0.3
                                );
                                // Initialize drag offset as zero (cursor is at center)
                                mDragOffsetWorld = new Point2D.Double(0, 0);
                                targetPage.addDecoration(mCurrentDecoration);
                                System.out.println("[DECORATION-LISTENER] EmojiDrawScene: Created decoration at world: " + worldPt);
                            } else if (mCurrentDecoration != null && mDragOffsetWorld != null) {
                                // Update position maintaining grab offset
                                double newX = worldPt.x - mDragOffsetWorld.x;
                                double newY = worldPt.y - mDragOffsetWorld.y;
                                mCurrentDecoration.setPosition(newX, newY);
                                System.out.println("[DECORATION-LISTENER-DRAG] Updated position to: (" + newX + ", " + newY + ")");
                            }
                            // Ensure immediate visibility while dragging
                            if (mCurrentDecoration != null) {
                                tj.getCanvas2D().repaint();
                            }
                        }
                    }
                    
                    @Override
                    public void onDecorationDragEnd() {
                        System.out.println("[DECORATION-LISTENER] EmojiDrawScene: Drag ended");
                        // Snap decoration to the last known drag point even if release happens outside canvas
                        if (mLastScreenPoint != null) {
                            TJ tj = (TJ)EmojiDrawScene.this.mScenario.getApp();
                            TJCanvas2D canvas = tj.getCanvas2D();
                            java.awt.Point canvasLoc = canvas.getLocationOnScreen();
                            java.awt.Point canvasPoint = new java.awt.Point(
                                mLastScreenPoint.x - canvasLoc.x,
                                mLastScreenPoint.y - canvasLoc.y
                            );
                            Point2D.Double worldPt = tj.getXform().calcPtFromScreenToWorld(canvasPoint);
                            if (mCurrentDecoration != null) {
                                mCurrentDecoration.setPosition(worldPt.x, worldPt.y);
                                System.out.println("[DECORATION-LISTENER] EmojiDrawScene: Finalized decoration at world: " + worldPt);
                                tj.getCanvas2D().repaint();
                            } else {
                                // Fallback: adjust last decoration on page
                                TJEmojiScenario scenario = (TJEmojiScenario)EmojiDrawScene.this.mScenario;
                                TJEmojiPage targetPage = scenario.getTargetEmojiPage();
                                if (targetPage != null && !targetPage.getDecorations().isEmpty()) {
                                    TJImage lastDecoration = targetPage.getDecorations().get(targetPage.getDecorations().size() - 1);
                                    lastDecoration.setPosition(worldPt.x, worldPt.y);
                                    System.out.println("[DECORATION-LISTENER] EmojiDrawScene: Finalized fallback decoration at world: " + worldPt);
                                    tj.getCanvas2D().repaint();
                                }
                            }
                        }
                        // Clear state
                        mCurrentDecoration = null;
                        mDragOffsetWorld = null;
                        mLastScreenPoint = null;
                        TJDecorationPresetPanel.sCurrentDraggedPreset = null;
                        TJEmojiScenario.sLastCreatedDecoration = null;
                        TJEmojiScenario.sDragOffsetWorld = null;
                    }
                });
            }
            
            tj.setTopPanel(this.mTopNavPanel);
            tj.setBottomPanel(this.mBottomNavPanel);
            tj.setRightPanel(this.mDecorationPanel);
            System.out.println("[DEBUG-EMOJI] Set right panel in EmojiDrawScene");
            
            // Ensure any selection visuals are reset when returning to draw
            TJEmojiPage targetPage = scenario.getTargetEmojiPage();
            if (targetPage != null) {
                // Restore decoration alphas and clear selected decorations
                if (targetPage.getSelectedDecorations() != null && !targetPage.getSelectedDecorations().isEmpty()) {
                    for (TJImage img : targetPage.getSelectedDecorations()) {
                        img.setAlpha(1.0f);
                    }
                    targetPage.getSelectedDecorations().clear();
                }
                // Clear selected curves list so they render with original appearance
                if (targetPage.getSelectedPtCurves() != null) {
                    targetPage.getSelectedPtCurves().clear();
                }
                // Clear single-selection state
                scenario.setSelectedDecoration(null);
                // Reflect changes immediately
                tj.getCanvas2D().repaint();
            }
            
            // Initialize circle bounds before any mouse input
            initializeEmojiCircle(tj);
            
            // Set target emoji page immediately so add and render paths reference the same page
            scenario.setTargetEmojiPage(tj.getJournalBookMgr().getCurEmojiPage());
            System.out.println("[DEBUG-EMOJI] EmojiDrawScene: Set target emoji page to: " + scenario.getTargetEmojiPage());
        }

        @Override
        public void wrapUp() {
            TJ tj = (TJ)this.mScenario.getApp();
            tj.setTopPanel(null);
            tj.setBottomPanel(null);
            tj.setRightPanel(null);
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
        private JPanel mDecorationPanel;
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
            
            // Check if click is on decoration preset panel
            if (this.mDecorationPanel != null) {
                // Convert canvas coordinates to frame coordinates
                Point frameCoords = new Point(screenPt);
                frameCoords.x += this.mDecorationPanel.getParent().getX();
                frameCoords.y += this.mDecorationPanel.getParent().getY();
                
                // Check if point is within panel bounds
                if (frameCoords.x >= this.mDecorationPanel.getX() && 
                    frameCoords.x < this.mDecorationPanel.getX() + this.mDecorationPanel.getWidth() &&
                    frameCoords.y >= this.mDecorationPanel.getY() && 
                    frameCoords.y < this.mDecorationPanel.getY() + this.mDecorationPanel.getHeight()) {
                    
                    // Convert to panel-local coordinates
                    Point panelCoords = new Point(
                        frameCoords.x - this.mDecorationPanel.getX(),
                        frameCoords.y - this.mDecorationPanel.getY()
                    );
                    System.out.println("[DEBUG-DECORATION] Panel click detected at panel coords: " + panelCoords);
                    handlePresetPanelPress(panelCoords);
                    return;
                }
            }
            
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
            
            // Check if dragging a decoration preset that was started from the panel
            // Use the static field from TJDecorationPresetPanel
            if (TJDecorationPresetPanel.sCurrentDraggedPreset != null) {
                // Convert screen point to world coordinates
                Point2D.Double worldPt = tj.getXform().calcPtFromScreenToWorld(screenPt);
                
                // Create decoration on first entry into circle
                if (sLastCreatedDecoration == null && mEmojiCircle != null && mEmojiCircle.contains(screenPt)) {
                    System.out.println("[DEBUG-DECORATION] Creating decoration at world: " + worldPt);
                    TJImage decoration = new TJImage(
                        TJDecorationPresetPanel.sCurrentDraggedPreset.image,
                        (int)worldPt.x,
                        (int)worldPt.y,
                        0.3
                    );
                    scenario.getTargetEmojiPage().addDecoration(decoration);
                    sLastCreatedDecoration = decoration;
                    // Initialize offset as zero (cursor is at center)
                    sDragOffsetWorld = new Point2D.Double(0, 0);
                    System.out.println("[DEBUG-DECORATION] Decoration created!");
                } else if (sLastCreatedDecoration != null && sDragOffsetWorld != null) {
                    // Update position maintaining grab offset
                    double newX = worldPt.x - sDragOffsetWorld.x;
                    double newY = worldPt.y - sDragOffsetWorld.y;
                    sLastCreatedDecoration.setPosition(newX, newY);
                    System.out.println("[DEBUG-DECORATION-DRAG] Updated position to: (" + newX + ", " + newY + ")");
                }
                // Ensure immediate visibility while dragging
                if (sLastCreatedDecoration != null) {
                    tj.getCanvas2D().repaint();
                }
                return;
            }
            
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
            Point screenPt = e.getPoint();
            
            // Check if we were dragging a decoration preset
            if (TJDecorationPresetPanel.sCurrentDraggedPreset != null) {
                System.out.println("[DEBUG-DECORATION] Released preset: " + TJDecorationPresetPanel.sCurrentDraggedPreset.name + " at screen: " + screenPt);
                System.out.println("[DEBUG-DECORATION] sLastCreatedDecoration is: " + sLastCreatedDecoration);
                
                // If a decoration was created earlier during drag, snap it to the release point
                if (sLastCreatedDecoration != null) {
                    Point2D.Double worldPt = tj.getXform().calcPtFromScreenToWorld(screenPt);
                    sLastCreatedDecoration.setPosition(worldPt.x, worldPt.y);
                    System.out.println("[DEBUG-DECORATION] Finalized sLastCreatedDecoration position at world: " + worldPt);
                    tj.getCanvas2D().repaint();
                } else {
                    // If sLastCreatedDecoration is null, try to snap the most recent decoration from the emoji page
                    TJEmojiPage targetPage = scenario.getTargetEmojiPage();
                    if (targetPage != null && !targetPage.getDecorations().isEmpty()) {
                        ArrayList<TJImage> decorations = targetPage.getDecorations();
                        TJImage lastDecoration = decorations.get(decorations.size() - 1);
                        Point2D.Double worldPt = tj.getXform().calcPtFromScreenToWorld(screenPt);
                        lastDecoration.setPosition(worldPt.x, worldPt.y);
                        System.out.println("[DEBUG-DECORATION] Snapped last decoration from page to world: " + worldPt);
                        tj.getCanvas2D().repaint();
                    }
                }
                // Clear the decoration tracking
                TJDecorationPresetPanel.sCurrentDraggedPreset = null;
                sLastCreatedDecoration = null;
                sDragOffsetWorld = null;
                return;
            }
            
            System.out.println("[DEBUG-EMOJI] handleMouseRelease called");
            TJEmojiPage targetEmojiPage = scenario.getTargetEmojiPage();
            System.out.println("[DEBUG-EMOJI] targetEmojiPage: " + targetEmojiPage);
            System.out.println("[DEBUG-EMOJI] curPtCurve: " + tj.getPtCurveMgr().getCurPtCurve());
            
            if (targetEmojiPage != null && tj.getPtCurveMgr().getCurPtCurve() != null) {
                // Finalize the current curve
                System.out.println("[DEBUG-EMOJI] Finalizing curve. Curve details: " + tj.getPtCurveMgr().getCurPtCurve());
                System.out.println("[DEBUG-EMOJI] PtCurveMgr saved curves BEFORE move: " + tj.getPtCurveMgr().getPtCurves().size());
                System.out.println("[DEBUG-EMOJI] EmojiPage curves BEFORE move: " + targetEmojiPage.getPtCurves().size());
                TJCmdToAddCurPtCurveToEmojiPage.execute(tj);
                System.out.println("[DEBUG-EMOJI] PtCurveMgr saved curves AFTER move (expected 0): " + tj.getPtCurveMgr().getPtCurves().size());
                System.out.println("[DEBUG-EMOJI] EmojiPage curves AFTER move: " + targetEmojiPage.getPtCurves().size());
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
            
            // Draw decorations in WORLD space; g2 is already world->screen transformed
            Shape oldClip = g2.getClip();
            try {
                AffineTransform s2w = tj.getXform().getCurrentXformFromScreenToWorld();
                Shape worldCircle = s2w.createTransformedShape(circle);
                g2.setClip(worldCircle);
                ArrayList<TJImage> decorations = emojiPage.getDecorations();
                System.out.println("[DEBUG-RENDER] Total decorations to render: " + decorations.size());
                for (TJImage decoration : decorations) {
                    System.out.println("[DEBUG-RENDER] Drawing decoration at world: " + decoration.getX() + ", " + decoration.getY());
                    decoration.draw(g2);
                }
            } catch (Exception ex) {
                System.out.println("[DEBUG-RENDER] Failed to clip decorations: " + ex);
            } finally {
                g2.setClip(oldClip);
            }
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
            
            // Get the SHARED decoration panel from scenario (not creating new one per scene)
            this.mDecorationPanel = scenario.getSharedDecorationPanel();
            System.out.println("[DEBUG-EMOJI] EmojiDrawingScene: Using shared decoration panel: " + this.mDecorationPanel);
            
            // Always set up decoration listener to handle drag events (even if panel already exists)
            if (this.mDecorationPanel instanceof TJDecorationPresetPanel) {
                TJDecorationPresetPanel presetPanel = (TJDecorationPresetPanel) this.mDecorationPanel;
                presetPanel.setDecorationListener(new TJDecorationPresetPanel.DecorationCreationListener() {
                    private TJImage mCurrentDecoration = null;
                    private java.awt.Point mLastScreenPoint = null; // last drag point
                    
                    @Override
                    public void onDecorationDragStart(TJDecorationPresetPanel.PresetIcon preset) {
                        System.out.println("[DECORATION-LISTENER] EmojiDrawingScene: Drag started for: " + preset.name);
                        mCurrentDecoration = null;
                        mLastScreenPoint = null;
                    }
                    
                    @Override
                    public void onDecorationDragMove(java.awt.Point screenPoint) {
                        TJEmojiScenario scenario = (TJEmojiScenario)EmojiDrawingScene.this.mScenario;
                        EmojiDrawingScene scene = EmojiDrawingScene.this;
                        
                        System.out.println("[DECORATION-LISTENER] EmojiDrawingScene: Target emoji page: " + scenario.getTargetEmojiPage());
                        mLastScreenPoint = new java.awt.Point(screenPoint);
                        
                        if (scene.mEmojiCircle != null && scene.mEmojiCircle.contains(screenPoint)) {
                            // Check if we haven't created a decoration yet for this drag
                            if (mCurrentDecoration == null && TJDecorationPresetPanel.sCurrentDraggedPreset != null) {
                                TJ tj = (TJ)EmojiDrawingScene.this.mScenario.getApp();
                                TJCanvas2D canvas = tj.getCanvas2D();
                                TJEmojiPage targetPage = scenario.getTargetEmojiPage();
                                
                                if (targetPage == null) {
                                    System.out.println("[DECORATION-LISTENER] EmojiDrawingScene: ERROR: Target emoji page is NULL! Cannot add decoration.");
                                    return;
                                }
                                // Convert absolute screen point to canvas-local, then to world coordinates
                                java.awt.Point canvasLoc = canvas.getLocationOnScreen();
                                java.awt.Point canvasPoint = new java.awt.Point(
                                    screenPoint.x - canvasLoc.x,
                                    screenPoint.y - canvasLoc.y
                                );
                                System.out.println("[DECORATION-LISTENER] EmojiDrawingScene: screenPoint=" + screenPoint + ", canvasPoint=" + canvasPoint + ", canvasLoc=" + canvasLoc);
                                Point2D.Double worldPt = tj.getXform().calcPtFromScreenToWorld(canvasPoint);
                                System.out.println("[DECORATION-LISTENER] EmojiDrawingScene: Creating decoration at world point: " + worldPt);
                                
                                mCurrentDecoration = new TJImage(TJDecorationPresetPanel.sCurrentDraggedPreset.image, (int)worldPt.x, (int)worldPt.y, 0.3);
                                targetPage.addDecoration(mCurrentDecoration);
                                System.out.println("[DECORATION-LISTENER] EmojiDrawingScene: Decoration created and added!");
                            }
                        }
                    }
                    
                    @Override
                    public void onDecorationDragEnd() {
                        System.out.println("[DECORATION-LISTENER] EmojiDrawingScene: Drag ended");
                        // Snap decoration to the last known drag point
                        if (mLastScreenPoint != null) {
                            TJ tj = (TJ)EmojiDrawingScene.this.mScenario.getApp();
                            TJCanvas2D canvas = tj.getCanvas2D();
                            java.awt.Point canvasLoc = canvas.getLocationOnScreen();
                            java.awt.Point canvasPoint = new java.awt.Point(
                                mLastScreenPoint.x - canvasLoc.x,
                                mLastScreenPoint.y - canvasLoc.y
                            );
                            Point2D.Double worldPt = tj.getXform().calcPtFromScreenToWorld(canvasPoint);
                            if (mCurrentDecoration != null) {
                                mCurrentDecoration.setPosition(worldPt.x, worldPt.y);
                                System.out.println("[DECORATION-LISTENER] EmojiDrawingScene: Finalized decoration at world: " + worldPt);
                                tj.getCanvas2D().repaint();
                            } else {
                                // Fallback: adjust last decoration on page
                                TJEmojiScenario scenario = (TJEmojiScenario)EmojiDrawingScene.this.mScenario;
                                TJEmojiPage targetPage = scenario.getTargetEmojiPage();
                                if (targetPage != null && !targetPage.getDecorations().isEmpty()) {
                                    TJImage lastDecoration = targetPage.getDecorations().get(targetPage.getDecorations().size() - 1);
                                    lastDecoration.setPosition(worldPt.x, worldPt.y);
                                    System.out.println("[DECORATION-LISTENER] EmojiDrawingScene: Finalized fallback decoration at world: " + worldPt);
                                    tj.getCanvas2D().repaint();
                                }
                            }
                        }
                        // Clear state (listener-level)
                        mCurrentDecoration = null;
                        mLastScreenPoint = null;
                        // Also clear global drag state to prevent follow-up duplicates
                        TJDecorationPresetPanel.sCurrentDraggedPreset = null;
                        TJEmojiScenario.sLastCreatedDecoration = null;
                        TJEmojiScenario.sDragOffsetWorld = null;
                    }
                });
            }
            
            tj.setTopPanel(this.mTopNavPanel);
            tj.setBottomPanel(this.mBottomNavPanel);
            tj.setRightPanel(this.mDecorationPanel);
            System.out.println("[DEBUG-EMOJI] Set right panel with decoration presets");
            
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
            tj.setRightPanel(null);
        }
        
        private void handlePresetPanelPress(Point screenPt) {
            if (this.mDecorationPanel == null || !(this.mDecorationPanel instanceof TJDecorationPresetPanel)) {
                return;
            }
            
            TJDecorationPresetPanel presetPanel = (TJDecorationPresetPanel) this.mDecorationPanel;
            TJDecorationPresetPanel.PresetIcon preset = presetPanel.getPresetAtPoint(screenPt.x, screenPt.y);
            
            if (preset != null) {
                TJ tj = (TJ)this.mScenario.getApp();
                TJEmojiScenario scenario = (TJEmojiScenario)this.mScenario;
                // Track the dragged preset at scenario level so we can access it even when mouse leaves the panel
                scenario.mCurrentDraggedPreset = preset;
                System.out.println("[DEBUG-DECORATION] Started dragging preset: " + preset.name);
            }
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

    // New scene: select emoji decoration and zoom
    public static class EmojiDecorationSelectScene extends TJScene {
        private JPanel mTopNavPanel;
        private JPanel mBottomNavPanel;
        private JPanel mDecorationPanel;
        private static EmojiDecorationSelectScene mSingleton = null;
        public static EmojiDecorationSelectScene getSingleton() {
            assert(EmojiDecorationSelectScene.mSingleton != null);
            return EmojiDecorationSelectScene.mSingleton;
        }
        public static EmojiDecorationSelectScene createSingleton(XScenario scenario) {
            assert(EmojiDecorationSelectScene.mSingleton == null);
            EmojiDecorationSelectScene.mSingleton = new EmojiDecorationSelectScene(scenario);
            return EmojiDecorationSelectScene.mSingleton;
        }
        private EmojiDecorationSelectScene(XScenario scenario) { super(scenario); }

        private void initializeTopNav() {
            TJ tj = (TJ)this.mScenario.getApp();
            String title = "Decoration Select";
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
            TJEmojiScenario scenario = TJEmojiScenario.getSingle();
            Point screenPt = e.getPoint();

            TJEmojiPage page = scenario.getTargetEmojiPage();
            if (page == null) return;
            ArrayList<TJImage> decos = page.getDecorations();
            TJImage clicked = null;
            // Convert screen -> world before hit-testing to account for global xform
            Point2D.Double worldPt = tj.getXform().calcPtFromScreenToWorld(screenPt);
            for (int i = decos.size() - 1; i >= 0; i--) {
                TJImage img = decos.get(i);
                if (img.containsWorld(worldPt)) { clicked = img; break; }
            }
            scenario.setSelectedDecoration(clicked);
            System.out.println("[EMOJI-SELECT] Selected decoration: " + clicked);
            tj.getCanvas2D().repaint();
        }

        @Override
        public void handleMouseDrag(MouseEvent e) { }
        @Override
        public void handleMouseRelease(MouseEvent e) { }

        @Override
        public void handleKeyDown(KeyEvent e) {
            TJ tj = (TJ)this.mScenario.getApp();
            TJEmojiScenario scenario = TJEmojiScenario.getSingle();
            int code = e.getKeyCode();
            if (code == KeyEvent.VK_Z) {
                // Zoom to selected decoration: center to pivot, then scale up
                TJImage sel = scenario.getSelectedDecoration();
                if (sel == null) return;
                Point2D.Double worldCenter = new Point2D.Double(sel.getX(), sel.getY());
                Point screenCenter = tj.getXform().calcPtFromWorldToScreen(worldCenter);
                // 1) Translate: move center to pivot
                TJCmdToSetStartScreenPt.execute(tj, screenCenter);
                TJCmdToTranslateTo.execute(tj, TJXform.PIVOT_PT);
                // 2) Zoom: use fixed factor by setting start/target arm lengths around pivot
                int arm = (int)TJXform.MIN_START_ARM_LENGTH_FOR_SCALING;
                Point start = new Point(TJXform.PIVOT_PT.x - arm, TJXform.PIVOT_PT.y);
                int targetArm = (int)(arm * 1.5); // 1.5x zoom per press
                Point target = new Point(TJXform.PIVOT_PT.x + targetArm, TJXform.PIVOT_PT.y);
                TJCmdToSetStartScreenPt.execute(tj, start);
                TJCmdToZoomTo.execute(tj, target);
                tj.getCanvas2D().repaint();
            } else if (code == KeyEvent.VK_ESCAPE || code == KeyEvent.VK_SHIFT) {
                // Exit selection mode and clear selection highlight
                scenario.setSelectedDecoration(null);
                XCmdToChangeScene.execute(tj, TJEmojiScenario.EmojiDrawScene.getSingleton(), this);
            }
        }

        @Override
        public void handleKeyUp(KeyEvent e) { }
        @Override
        public void updateSupportObjects() { }

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
            TJEmojiScenario scenario = TJEmojiScenario.getSingle();
            Ellipse2D.Double circle = scenario.getTargetBounds();
            TJEmojiPage emojiPage = scenario.getTargetEmojiPage();
            if (circle == null || emojiPage == null) return;
            scenario.drawEmojiPageAndContent(g2, canvas, circle, emojiPage);

            // Also draw decorations clipped to the emoji circle in WORLD space
            Shape oldClip = g2.getClip();
            try {
                AffineTransform s2w = tj.getXform().getCurrentXformFromScreenToWorld();
                Shape worldCircle = s2w.createTransformedShape(circle);
                g2.setClip(worldCircle);
                ArrayList<TJImage> decorations = emojiPage.getDecorations();
                for (TJImage decoration : decorations) {
                    decoration.draw(g2);
                }
            } catch (Exception ex) {
                System.out.println("[EMOJI-SELECT] Failed to render decorations: " + ex);
            } finally {
                g2.setClip(oldClip);
            }
        }

        @Override
        public void renderScreenObjects(Graphics2D g2) { }

        @Override
        public void getReady() {
            TJ tj = (TJ)this.mScenario.getApp();
            TJEmojiScenario scenario = TJEmojiScenario.getSingle();
            if (this.mTopNavPanel == null) initializeTopNav();
            if (this.mBottomNavPanel == null) initializeBottomNav();
            tj.setTopPanel(this.mTopNavPanel);
            tj.setBottomPanel(this.mBottomNavPanel);
            // Show the decoration preset panel in selection scene as well
            this.mDecorationPanel = scenario.getSharedDecorationPanel();
            tj.setRightPanel(this.mDecorationPanel);
            System.out.println("[EMOJI-SELECT] Decoration selection ready");
        }

        @Override
        public void wrapUp() {
            TJ tj = (TJ)this.mScenario.getApp();
            tj.setTopPanel(null);
            tj.setBottomPanel(null);
            tj.setRightPanel(null);
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

