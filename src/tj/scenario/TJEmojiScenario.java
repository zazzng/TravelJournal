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
import tj.TJEmoji;
import tj.TJPage;
import tj.TJPtCurve;
import tj.TJScene;
import tj.cmd.TJCmdToAddCurPtCurveToEmoji;
import tj.cmd.TJCmdToAddCurPtCurveToPtCurves;
import tj.cmd.TJCmdToCreateCurPtCurve;
import tj.cmd.TJCmdToIncreaseStrokeWidthForCurPtCurve;
import tj.cmd.TJCmdToUpdateCurPtCurve;
import utils.TJNavPanel;
import x.XApp;
import x.XCmdToChangeScene;
import x.XScenario;

public class TJEmojiScenario extends XScenario {
    // constants
    private static final double CIRCLE_DIAMETER_RATIO = 0.25; // 25% of appWidth
    private static final Color EMOJI_CIRCLE_COLOR = Color.YELLOW;
    
    // fields
    private TJEmoji mCurrentEmoji = new TJEmoji();
    public TJEmoji getCurrentEmoji() {
        return this.mCurrentEmoji;
    }
    
    private Ellipse2D.Double mEmojiCircle;
    public Ellipse2D.Double getEmojiCircle() {
        return this.mEmojiCircle;
    }
    
    private Point mLastMousePoint = null;
    
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
    
    private Ellipse2D.Double calculateEmojiCircleBounds(TJ tj) {
        TJCanvas2D canvas = tj.getCanvas2D();
        int appWidth = canvas.getWidth();
        int appHeight = canvas.getHeight();
        
        // Calculate dimensions based on appWidth (0.25 ratio)
        int diameter = (int)(appWidth * CIRCLE_DIAMETER_RATIO);
        int radius = diameter / 2;
        
        // Center the circle in the application window
        int centerX = appWidth / 2;
        int centerY = appHeight / 2;
        
        // Calculate top-left corner (x, y)
        int x = centerX - radius;
        int y = centerY - radius;
        
        return new Ellipse2D.Double(x, y, diameter, diameter);
    }
    
    public void updateEmojiCircle(TJ tj) {
        this.mEmojiCircle = calculateEmojiCircleBounds(tj);
    }
        
    // Inner class for Emoji Drawing Scene - handles actual drawing interaction
    public static class EmojiDrawScene extends TJScene {
        private JPanel mTopNavPanel;
        private JPanel mBottomNavPanel;
        
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
            String title = "New Emoji";
            
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

            // Store the mouse point for use when transitioning
            scenario.mLastMousePoint = screenPt;
            
            scenario.updateEmojiCircle(tj);

            if (scenario.mEmojiCircle != null && scenario.mEmojiCircle.contains(screenPt)) {
                tj.getPtCurveMgr().setCurPtCurve(null);
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
            TJ tj = (TJ)this.mScenario.getApp();
            TJEmojiScenario scenario = (TJEmojiScenario)this.mScenario;
            int code = e.getKeyCode();
            
            switch (code) {
                case KeyEvent.VK_ESCAPE:
                    // ESC key to go back to previous scene
                    XCmdToChangeScene.execute(tj, this.mReturnScene, null);
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
            ((TJEmojiScenario)this.mScenario).updateEmojiCircle(
                (TJ)this.mScenario.getApp());
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
            
            // Ensure circle is calculated (redundant, but safe)
            scenario.updateEmojiCircle(tj); 
            Ellipse2D.Double circle = scenario.getEmojiCircle();

            // --- DRAWING ---

            // 1. Set Clip
            Shape oldClip = g2.getClip();
            if (circle != null) {
                g2.setClip(circle); 
            }

            // 2. Draw Emoji Circle FILL
            scenario.drawEmojiCircleFill(g2);

            // 3. Draw Emoji Content (TRANSFORMED)
            if (scenario.getCurrentEmoji() != null && circle != null) {
                AffineTransform saveAT = g2.getTransform();
                g2.translate(circle.getX(), circle.getY());
                
                // Draw saved curves
                tj.getCanvas2D().drawPtCurves(g2, scenario.getCurrentEmoji().getPtCurves());
                
                g2.setTransform(saveAT);
            }
            
            // 4. Restore Clip
            g2.setClip(oldClip); 
            
            // 5. Draw Emoji Circle BORDER (outside clip)
            scenario.drawEmojiCircleBorder(g2);
            
            canvas.drawPenTip(g2);
        }

        @Override
        public void renderScreenObjects(Graphics2D g2) {
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
            
            scenario.updateEmojiCircle(tj);
        }

        @Override
        public void wrapUp() {
            TJ tj = (TJ)this.mScenario.getApp();
            tj.setTopPanel(null);
            tj.setBottomPanel(null);
        }
    }

    // Inner class for Emoji Drawing Scene
    public static class EmojiDrawingScene extends TJScene {
        private JPanel mTopNavPanel;
        private JPanel mBottomNavPanel;
        
        private static EmojiDrawingScene mSingleton = null;
        public static EmojiDrawingScene getSingleton() {
            assert(EmojiDrawingScene.mSingleton != null);
            return EmojiDrawingScene.mSingleton;
        }
        public static EmojiDrawingScene createSingleton(XScenario scenario) {
            assert(EmojiDrawingScene.mSingleton == null);
            EmojiDrawingScene.mSingleton = new EmojiDrawingScene(scenario);
            return EmojiDrawingScene.mSingleton;
        }
        
        private EmojiDrawingScene(XScenario scenario) {
            super(scenario);
        }

        private void initializeTopNav() {
            TJ tj = (TJ)this.mScenario.getApp();
            String title = "New Emoji";
            this.mTopNavPanel = TJNavPanel.createTopNavPanel(tj, title);
        }
        
        private void initializeBottomNav() {
            TJ tj = (TJ)this.mScenario.getApp();
            this.mBottomNavPanel = TJNavPanel.createEmojiDrawBottomNavPanel(tj, this);
        }
        
        private Point toCircleLocal(Point screenPt) {
            TJEmojiScenario scenario = (TJEmojiScenario)this.mScenario;
            Ellipse2D.Double circle = scenario.getEmojiCircle();
            if (circle == null) return new Point();

            // Calculate the double-precision coordinates relative to the circle's top-left
            double localX = screenPt.getX() - circle.getX();
            double localY = screenPt.getY() - circle.getY();

            // Convert back to integer Point (truncation is common, Math.round() is often safer)
            return new Point((int)Math.round(localX), (int)Math.round(localY));
        }

        @Override
        public void handleMousePress(MouseEvent e) {
            TJ tj = (TJ)this.mScenario.getApp();
            TJEmojiScenario scenario = (TJEmojiScenario)this.mScenario;
            Point screenPt = e.getPoint();
            
            // Use scenario's update helper
            scenario.updateEmojiCircle(tj);
            Ellipse2D.Double circle = scenario.getEmojiCircle();
            
            if (circle != null && circle.contains(screenPt)) {
                Point circleLocalPt = toCircleLocal(screenPt);
                
                if (tj.getPtCurveMgr().getCurPtCurve() == null) {
                    TJCmdToCreateCurPtCurve.execute(tj, circleLocalPt);
                } else {
                    TJCmdToUpdateCurPtCurve.execute(tj, circleLocalPt);
                }
            }
        }

        @Override
        public void handleMouseDrag(MouseEvent e) {
            TJ tj = (TJ)this.mScenario.getApp();
            TJEmojiScenario scenario = (TJEmojiScenario)this.mScenario;
            Point screenPt = e.getPoint();
            
            scenario.updateEmojiCircle(tj); // Ensure circle bounds are current
            Ellipse2D.Double circle = scenario.getEmojiCircle();
            
            if (circle != null && tj.getPtCurveMgr().getCurPtCurve() != null) {
                if (circle.contains(screenPt)) {
                    Point circleLocalPt = toCircleLocal(screenPt);
                    TJCmdToUpdateCurPtCurve.execute(tj, circleLocalPt);
                }
            }
        }

        @Override
        public void handleMouseRelease(MouseEvent e) {
            TJ tj = (TJ) this.mScenario.getApp();
            TJEmojiScenario scenario = (TJEmojiScenario)this.mScenario;
            
            if (tj.getPtCurveMgr().getCurPtCurve() != null) {
                // Use new command to add curve to TJEmoji object
                TJCmdToAddCurPtCurveToEmoji.execute(tj, scenario.getCurrentEmoji());
            }
            
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
            ((TJEmojiScenario)this.mScenario).updateEmojiCircle((TJ)this.mScenario.getApp());
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
            
            scenario.updateEmojiCircle(tj); 
            Ellipse2D.Double circle = scenario.getEmojiCircle();
            
            scenario.drawEmojiCircleFill(g2);
            
            Shape oldClip = g2.getClip();
            if (circle != null) {
                g2.setClip(circle); 
            }

//            AffineTransform oldAT = g2.getTransform();
//            g2.translate(circle.getX(), circle.getY());

            tj.getCanvas2D().drawPtCurves(g2, scenario.getCurrentEmoji().getPtCurves());
            tj.getCanvas2D().drawCurPtCurve(g2);

            // 5️⃣ Restore transform + clip
//            g2.setTransform(oldAT);
            g2.setClip(oldClip);

            // 6️⃣ Draw border LAST
            scenario.drawEmojiCircleBorder(g2);

            canvas.drawPenTip(g2);
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
        }

        @Override
        public void wrapUp() {
            TJ tj = (TJ)this.mScenario.getApp();
            tj.setTopPanel(null);
            tj.setBottomPanel(null);
        }
    }
    
    public void drawEmojiCircleFill(Graphics2D g2) {
        if (mEmojiCircle != null) {
            g2.setColor(EMOJI_CIRCLE_COLOR);
            g2.fill(mEmojiCircle);
        }
    }
    
    public void drawEmojiCircleBorder(Graphics2D g2) {
        if (mEmojiCircle != null) {
            g2.setColor(Color.DARK_GRAY);
            g2.setStroke(new BasicStroke(2));
            g2.draw(mEmojiCircle);
        }
    }
    
    private void drawEmojiContent(Graphics2D g2, TJ tj, Ellipse2D.Double circle) {
        if (circle == null) return;
        
        // --- 1. Translate to Local Origin (Top-Left of the Circle) ---
        AffineTransform saveAT = g2.getTransform();
        g2.translate(circle.getX(), circle.getY());
        
        // --- 2. Draw Saved Content (Already stored in local coordinates) ---
        tj.getCanvas2D().drawPtCurves(g2, this.mCurrentEmoji.getPtCurves());
        
        // --- 3. Draw Current Curve (If user is actively drawing) ---
        // Note: The current curve must use the same local coordinates.
        tj.getCanvas2D().drawCurPtCurve(g2);
        
        // --- 4. Restore Transform ---
        g2.setTransform(saveAT);
    }
}

