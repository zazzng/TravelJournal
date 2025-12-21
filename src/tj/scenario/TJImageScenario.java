package tj.scenario;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;
import tj.TJ;
import tj.TJCanvas2D;
import tj.TJImage;
import tj.TJPage;
import tj.TJScene;
import tj.cmd.TJCmdToCreateCurPtCurve;
import tj.cmd.TJCmdToUpdateCurPtCurve;
import tj.cmd.TJCmdToUpdateImageCurPtCurve;
import utils.TJNavPanel;
import x.XApp;
import x.XCmdToChangeScene;
import x.XScenario;

public class TJImageScenario extends XScenario {
    // constants
    private TJImage mSelectedImage = null;
    private Point mLastMousePt = null;
    private Rectangle mTargetBounds = null;
    public Rectangle getTargetBounds() {
        return this.mTargetBounds;
    }

    
    private static final Color SELECTION_BORDER_COLOR = new Color(50, 150, 255);
    private static final BasicStroke SELECTION_STROKE = new BasicStroke(3.0f);
    
    // singleton pattern
    private static TJImageScenario mSingleton = null;
    public static TJImageScenario getSingle() {
        assert(TJImageScenario.mSingleton != null);
        return TJImageScenario.mSingleton;
    }
    public static TJImageScenario createSingleton(XApp app) {
        assert(TJImageScenario.mSingleton == null);
        TJImageScenario.mSingleton = new TJImageScenario(app);
        return TJImageScenario.mSingleton;
    }
    private TJImageScenario(XApp app) {
        super(app);
    }

    @Override
    protected void addScenes() {
        this.addScene(TJImageScenario.ImageReadyScene.createSingleton(this));
        this.addScene(TJImageScenario.ImageGestureScene.createSingleton(this));
        this.addScene(TJImageScenario.ImageRotateScene.createSingleton(this));
        this.addScene(TJImageScenario.ImageMoveScene.createSingleton(this));
        this.addScene(TJImageScenario.ImageScaleScene.createSingleton(this));
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

    public static class ImageReadyScene extends TJScene {
        // constants
        private static final int LONG_TAP_DURATION = 2000;
        
        // UI components
        private JPanel mTopNavPanel;
        private JPanel mBottomNavPanel;
        
        // fields
        private Timer mLongTapTimer;
        private Point mPressPoint;
        
        // singleton pattern
        private static ImageReadyScene mSingleton = null;
        public static ImageReadyScene getSingleton() {
            assert(ImageReadyScene.mSingleton != null);
            return ImageReadyScene.mSingleton;
        }
        public static ImageReadyScene createSingleton(XScenario scenario) {
            assert(ImageReadyScene.mSingleton == null);
            ImageReadyScene.mSingleton = new ImageReadyScene(scenario);
            return ImageReadyScene.mSingleton;
        }
        private ImageReadyScene(XScenario scenario) {
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
            TJImageScenario scenario = (TJImageScenario)this.mScenario;
            TJPage[] pages = tj.getJournalBookMgr().getCurPage();
            
            TJImage clickedImage = null;
            
            // 1. check if user clicked an existing image (for selection)
            for (TJPage p : pages) {
                ArrayList<TJImage> imgs = p.getImages();
                for (int i = imgs.size() - 1; i >= 0; i--) {
                    if (imgs.get(i).contains(pt)) {
                        clickedImage = imgs.get(i);
                        break; // found image, don't start timer
                    }
                }
                if (clickedImage != null) break;
            }
            
            if (clickedImage != null) {
                // a new image was clicked: select it
                scenario.mSelectedImage = clickedImage;
                scenario.mLastMousePt = pt;
                // tj.getCanvas2D().repaint();
                // return; 
            } else {
                scenario.mSelectedImage = null;
                // tj.getCanvas2D().repaint();

                Rectangle totalBounds = scenario.getTotalPageBounds(tj);
                int midX = totalBounds.x + totalBounds.width / 2;
                
                if (pt.x < midX) {
                    scenario.mTargetBounds = new Rectangle(totalBounds.x,
                        totalBounds.y, totalBounds.width / 2, totalBounds.height);
                } else {
                    scenario.mTargetBounds = new Rectangle(midX, totalBounds.y,
                        totalBounds.width / 2, totalBounds.height);
                }
                
                TJCmdToCreateCurPtCurve.execute(tj, pt); 
                XCmdToChangeScene.execute(tj, TJImageScenario.ImageGestureScene.
                    getSingleton(), this);
            }

            tj.getCanvas2D().repaint();

            // // 2. start long tap timer for adding new image
            // ActionListener task = evt -> scenario.handleLongTap(tj, mPressPoint);
            // mLongTapTimer = new Timer(LONG_TAP_DURATION, task);
            // mLongTapTimer.setRepeats(false);
            // mLongTapTimer.start();
        }

        @Override
        public void handleMouseDrag(MouseEvent e) {
            // if (mLongTapTimer != null && mLongTapTimer.isRunning()) {
            //     if (mPressPoint.distance(e.getPoint()) > 10) {
            //         mLongTapTimer.stop();
            //     }
            // }
        }

        @Override
        public void handleMouseRelease(MouseEvent e) {
            // if (mLongTapTimer != null && mLongTapTimer.isRunning()) {
                // mLongTapTimer.stop();
            // }
        }

        @Override
        public void handleKeyDown(KeyEvent e) {
           TJ tj = (TJ)this.mScenario.getApp();
            TJImageScenario scenario = (TJImageScenario)this.mScenario;

            if (scenario.mSelectedImage == null) return;

            switch (e.getKeyCode()) {
                case KeyEvent.VK_R:
                    // R: Rotate Mode
                    XCmdToChangeScene.execute(tj, TJImageScenario.ImageRotateScene.getSingleton(), this);
                    break;
                case KeyEvent.VK_M:
                    // M: Move Mode
                    XCmdToChangeScene.execute(tj, TJImageScenario.ImageMoveScene.getSingleton(), this);
                    break;
                case KeyEvent.VK_S:
                    // S: Scale Mode
                    XCmdToChangeScene.execute(tj, TJImageScenario.ImageScaleScene.getSingleton(), this);
                    break;
            }
        }

        @Override
        public void handleKeyUp(KeyEvent e) {
            TJ tj = (TJ)this.mScenario.getApp();
            int code = e.getKeyCode();

            switch (code) {
                case KeyEvent.VK_I:
                     XCmdToChangeScene.execute(tj,
                         TJDrawScenario.DrawReadyScene.getSingleton(), null);
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
            
            g2.setColor(TJCanvas2D.COLOR_BACKGROUND_DARK); 
            g2.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        }

        @Override
        public void renderWorldObjects(Graphics2D g2) {
            TJ tj = (TJ)this.mScenario.getApp();
            TJCanvas2D canvas = tj.getCanvas2D();
            TJImageScenario scenario = (TJImageScenario)this.mScenario;
            
            Rectangle clipRect = scenario.getTotalPageBounds(tj);
            int startX = clipRect.x;
            int startY = clipRect.y;
            int pageWidth = clipRect.width / 2;
            int pageHeight = clipRect.height;
            
            TJPage[] curPage = tj.getJournalBookMgr().getCurPage();
            if (curPage == null) return;
            
            scenario.drawPageAndContent(g2, canvas, curPage, startX, startY,
                pageWidth, pageHeight);
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

    public static class ImageGestureScene extends TJScene {
        // UI Components
        private JPanel mTopNavPanel;
        private JPanel mBottomNavPanel;
        
        // singleton pattern
        private static ImageGestureScene mSingleton = null;
        public static ImageGestureScene getSingleton() {
            assert(ImageGestureScene.mSingleton != null);
            return ImageGestureScene.mSingleton;
        }
        public static ImageGestureScene createSingleton(XScenario scenario) {
            assert(ImageGestureScene.mSingleton == null);
            ImageGestureScene.mSingleton = new ImageGestureScene(scenario);
            return ImageGestureScene.mSingleton;
        }
        private ImageGestureScene(XScenario scenario) {
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
            TJImageScenario scenario = (TJImageScenario)this.mScenario;
            scenario.mLastMousePt = e.getPoint();
        }

        @Override
        public void handleMouseDrag(MouseEvent e) {
            TJ tj = (TJ)this.mScenario.getApp();
            TJCmdToUpdateImageCurPtCurve.execute(tj, e.getPoint(), "Image");
        }

        @Override
        public void handleMouseRelease(MouseEvent e) {
            TJ tj = (TJ)this.mScenario.getApp();
            TJImageScenario scenario = (TJImageScenario)this.mScenario;

            tj.TJPtCurve curCurve = tj.getPtCurveMgr().getCurPtCurve();

            if (curCurve != null) {
                Rectangle2D.Double bounds = curCurve.getBoundingBox();

                tj.getPtCurveMgr().setCurPtCurve(null);
                
                if (bounds.width > 20 && bounds.height > 20) {
                    TJPage[] pages = tj.getJournalBookMgr().getCurPage();
                    Rectangle totalBounds = scenario.getTotalPageBounds(tj);
                    int midX = totalBounds.x + totalBounds.width / 2;

                    TJPage targetPage = (bounds.getCenterX() < midX) ?
                        pages[0] : pages[1];

                    Rectangle intBounds = new Rectangle(
                        (int)bounds.x, (int)bounds.y, 
                        (int)bounds.width, (int)bounds.height
                    );

                    scenario.handleGesturePopUp(tj, intBounds, targetPage);
                }
            }

            XCmdToChangeScene.execute(tj, ImageReadyScene.getSingleton(), null);
            tj.getCanvas2D().repaint();
        }

        @Override
        public void handleKeyDown(KeyEvent e) {
        }

        @Override
        public void handleKeyUp(KeyEvent e) {
            TJ tj = (TJ)this.mScenario.getApp();
            int code = e.getKeyCode();

            switch (code) {
                case KeyEvent.VK_I:
                     XCmdToChangeScene.execute(tj,
                         TJDrawScenario.DrawReadyScene.getSingleton(), null);
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
            
            g2.setColor(TJCanvas2D.COLOR_BACKGROUND_DARK); 
            g2.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        }

        @Override
        public void renderWorldObjects(Graphics2D g2) {
            TJ tj = (TJ)this.mScenario.getApp();
            TJCanvas2D canvas = tj.getCanvas2D();
            TJImageScenario scenario = (TJImageScenario)this.mScenario;
            
            Rectangle clipRect = scenario.getTotalPageBounds(tj);
            int startX = clipRect.x;
            int startY = clipRect.y;
            int pageWidth = clipRect.width / 2;
            int pageHeight = clipRect.height;
            
            TJPage[] curPage = tj.getJournalBookMgr().getCurPage();
            if (curPage == null) return;
            
            scenario.drawPageAndContent(g2, canvas, curPage, startX, startY,
                pageWidth, pageHeight);
            canvas.drawPenTip(g2);
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
    
    public static class ImageRotateScene extends TJScene {
        // UI Components
        private JPanel mTopNavPanel;
        private JPanel mBottomNavPanel;
        
        // singleton pattern
        private static ImageRotateScene mSingleton = null;
        public static ImageRotateScene getSingleton() {
            assert(ImageRotateScene.mSingleton != null);
            return ImageRotateScene.mSingleton;
        }
        public static ImageRotateScene createSingleton(XScenario scenario) {
            assert(ImageRotateScene.mSingleton == null);
            ImageRotateScene.mSingleton = new ImageRotateScene(scenario);
            return ImageRotateScene.mSingleton;
        }
        private ImageRotateScene(XScenario scenario) {
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
            TJImageScenario scenario = (TJImageScenario)this.mScenario;
            scenario.mLastMousePt = e.getPoint();
        }

        @Override
        public void handleMouseDrag(MouseEvent e) {
            TJ tj = (TJ)this.mScenario.getApp();
            TJImageScenario scenario = (TJImageScenario)this.mScenario;
            TJImage selectedImage = scenario.mSelectedImage;
            if (selectedImage == null || scenario.mLastMousePt == null) return;

            Point curPt = e.getPoint();

            // 1. Get the image center (mX, mY)
            Point2D.Double center = new Point2D.Double(selectedImage.getX(), selectedImage.getY());

            // 2. Calculate angle of the last point relative to the center
            double deltaY_prev = scenario.mLastMousePt.y - center.y;
            double deltaX_prev = scenario.mLastMousePt.x - center.x;
            double anglePrev = Math.atan2(deltaY_prev, deltaX_prev);

            // 3. Calculate angle of the current point relative to the center
            double deltaY_cur = curPt.y - center.y;
            double deltaX_cur = curPt.x - center.x;
            double angleCur = Math.atan2(deltaY_cur, deltaX_cur);

            // 4. Calculate the difference (angleAmt)
            double rotateAmt = angleCur - anglePrev;

            // Apply rotation
            selectedImage.rotate(rotateAmt);

            scenario.mLastMousePt = curPt;
            tj.getCanvas2D().repaint();
        }

        @Override
        public void handleMouseRelease(MouseEvent e) {
            TJImageScenario scenario = (TJImageScenario)this.mScenario;
            scenario.mLastMousePt = null;
        }

        @Override
        public void handleKeyDown(KeyEvent e) {
        }

        @Override
        public void handleKeyUp(KeyEvent e) {
            TJ tj = (TJ)this.mScenario.getApp();
            int code = e.getKeyCode();

            switch (code) {
                case KeyEvent.VK_R:
                     XCmdToChangeScene.execute(tj,
                         TJImageScenario.ImageReadyScene.getSingleton(), this);
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
            
            g2.setColor(TJCanvas2D.COLOR_BACKGROUND_DARK); 
            g2.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        }

        @Override
        public void renderWorldObjects(Graphics2D g2) {
            TJ tj = (TJ)this.mScenario.getApp();
            TJCanvas2D canvas = tj.getCanvas2D();
            TJImageScenario scenario = (TJImageScenario)this.mScenario;
            
            Rectangle clipRect = scenario.getTotalPageBounds(tj);
            int startX = clipRect.x;
            int startY = clipRect.y;
            int pageWidth = clipRect.width / 2;
            int pageHeight = clipRect.height;
            
            TJPage[] curPage = tj.getJournalBookMgr().getCurPage();
            if (curPage == null) return;
            
            scenario.drawPageAndContent(g2, canvas, curPage, startX, startY,
                pageWidth, pageHeight);
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
    
    public static class ImageMoveScene extends TJScene {
        // UI Components
        private JPanel mTopNavPanel;
        private JPanel mBottomNavPanel;
        
        // singleton pattern
        private static ImageMoveScene mSingleton = null;
        public static ImageMoveScene getSingleton() {
            assert(ImageMoveScene.mSingleton != null);
            return ImageMoveScene.mSingleton;
        }
        public static ImageMoveScene createSingleton(XScenario scenario) {
            assert(ImageMoveScene.mSingleton == null);
            ImageMoveScene.mSingleton = new ImageMoveScene(scenario);
            return ImageMoveScene.mSingleton;
        }
        private ImageMoveScene(XScenario scenario) {
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
            TJImageScenario scenario = (TJImageScenario)this.mScenario;
            scenario.mLastMousePt = e.getPoint();
        }

        @Override
        public void handleMouseDrag(MouseEvent e) {
            TJ tj = (TJ)this.mScenario.getApp();
            TJImageScenario scenario = (TJImageScenario)this.mScenario;
            if (scenario.mSelectedImage == null || scenario.mLastMousePt == null) return;

            Point curPt = e.getPoint();
            double dx = curPt.x - scenario.mLastMousePt.x;
            double dy = curPt.y - scenario.mLastMousePt.y;

            scenario.mSelectedImage.translate(dx, dy);

            scenario.mLastMousePt = curPt;
            tj.getCanvas2D().repaint();
        }

        @Override
        public void handleMouseRelease(MouseEvent e) {
            TJImageScenario scenario = (TJImageScenario)this.mScenario;
            scenario.mLastMousePt = null;
        }

        @Override
        public void handleKeyDown(KeyEvent e) {
        }

        @Override
        public void handleKeyUp(KeyEvent e) {
            TJ tj = (TJ)this.mScenario.getApp();
            int code = e.getKeyCode();

            switch (code) {
                case KeyEvent.VK_M:
                     XCmdToChangeScene.execute(tj,
                         TJImageScenario.ImageReadyScene.getSingleton(), this);
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
            
            g2.setColor(TJCanvas2D.COLOR_BACKGROUND_DARK); 
            g2.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        }

        @Override
        public void renderWorldObjects(Graphics2D g2) {
            TJ tj = (TJ)this.mScenario.getApp();
            TJCanvas2D canvas = tj.getCanvas2D();
            TJImageScenario scenario = (TJImageScenario)this.mScenario;
            
            Rectangle clipRect = scenario.getTotalPageBounds(tj);
            int startX = clipRect.x;
            int startY = clipRect.y;
            int pageWidth = clipRect.width / 2;
            int pageHeight = clipRect.height;
            
            TJPage[] curPage = tj.getJournalBookMgr().getCurPage();
            if (curPage == null) return;
            
            scenario.drawPageAndContent(g2, canvas, curPage, startX, startY,
                pageWidth, pageHeight);
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
    
    public static class ImageScaleScene extends TJScene {
        // UI Components
        private JPanel mTopNavPanel;
        private JPanel mBottomNavPanel;
        
        // singleton pattern
        private static ImageScaleScene mSingleton = null;
        public static ImageScaleScene getSingleton() {
            assert(ImageScaleScene.mSingleton != null);
            return ImageScaleScene.mSingleton;
        }
        public static ImageScaleScene createSingleton(XScenario scenario) {
            assert(ImageScaleScene.mSingleton == null);
            ImageScaleScene.mSingleton = new ImageScaleScene(scenario);
            return ImageScaleScene.mSingleton;
        }
        private ImageScaleScene(XScenario scenario) {
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
            TJImageScenario scenario = (TJImageScenario)this.mScenario;
            scenario.mLastMousePt = e.getPoint();
        }

        @Override
        public void handleMouseDrag(MouseEvent e) {
            TJ tj = (TJ)this.mScenario.getApp();
            TJImageScenario scenario = (TJImageScenario)this.mScenario;
            if (scenario.mSelectedImage == null || scenario.mLastMousePt == null) return;

            Point curPt = e.getPoint();
            double dy = curPt.y - scenario.mLastMousePt.y;

            // Scaling based on vertical drag
            // Dragging up (dy < 0) scales up (factor > 1)
            double scaleFactor = 1.0 - (dy * 0.01); 
            scenario.mSelectedImage.scale(scaleFactor);

            scenario.mLastMousePt = curPt;
            tj.getCanvas2D().repaint();
        }

        @Override
        public void handleMouseRelease(MouseEvent e) {
            TJImageScenario scenario = (TJImageScenario)this.mScenario;
            scenario.mLastMousePt = null;
        }

        @Override
        public void handleKeyDown(KeyEvent e) {
        }

        @Override
        public void handleKeyUp(KeyEvent e) {
            TJ tj = (TJ)this.mScenario.getApp();
            int code = e.getKeyCode();

            switch (code) {
                case KeyEvent.VK_S:
                     XCmdToChangeScene.execute(tj,
                         TJImageScenario.ImageReadyScene.getSingleton(), this);
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
            
            g2.setColor(TJCanvas2D.COLOR_BACKGROUND_DARK); 
            g2.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        }

        @Override
        public void renderWorldObjects(Graphics2D g2) {
            TJ tj = (TJ)this.mScenario.getApp();
            TJCanvas2D canvas = tj.getCanvas2D();
            TJImageScenario scenario = (TJImageScenario)this.mScenario;
            
            Rectangle clipRect = scenario.getTotalPageBounds(tj);
            int startX = clipRect.x;
            int startY = clipRect.y;
            int pageWidth = clipRect.width / 2;
            int pageHeight = clipRect.height;
            
            TJPage[] curPage = tj.getJournalBookMgr().getCurPage();
            if (curPage == null) return;
            
            scenario.drawPageAndContent(g2, canvas, curPage, startX, startY,
                pageWidth, pageHeight);
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
    
    private void drawPageStructure(Graphics2D g2, int startX, int startY,
        int pageWidth, int pageHeight) {
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
    
    private void drawPageAndContent(Graphics2D g2,
        TJCanvas2D canvas, TJPage[] curPage, int startX, int startY,
        int pageWidth, int pageHeight) {
        drawPageStructure(g2, startX, startY, pageWidth, pageHeight);
        
        // clipping the pages
        Shape originalClip = g2.getClip();
        Rectangle totalPageArea = new Rectangle(startX, startY, pageWidth * 2, pageHeight);
        g2.setClip(totalPageArea);
            
        // draw content
        if (curPage!= null) {
            canvas.drawImages(g2, curPage[0].getImages());
            canvas.drawImages(g2, curPage[1].getImages());

            canvas.drawPtCurves(g2, curPage[0].getPtCurves());
            canvas.drawSelectedPtCurves(g2, curPage[0].getSelectedPtCurves());

            canvas.drawPtCurves(g2, curPage[1].getPtCurves());
            canvas.drawSelectedPtCurves(g2, curPage[1].getSelectedPtCurves());

            canvas.drawCurPtCurve(g2);
            drawSelectedImageBorder(g2);
        }

        g2.setClip(originalClip);
    }
    
    private void drawSelectedImageBorder(Graphics2D g2) {
        if (mSelectedImage == null) return;

        double x = mSelectedImage.getX();
        double y = mSelectedImage.getY();
        double rotation = mSelectedImage.getRotation();
        double scale = mSelectedImage.getScale();

        int w = mSelectedImage.getWidth();
        int h = mSelectedImage.getHeight();
        
        java.awt.geom.AffineTransform saveAT = g2.getTransform();
        Stroke oldStroke = g2.getStroke();
        Color oldColor = g2.getColor();

        g2.translate(x, y);
        g2.rotate(rotation);
        g2.scale(scale, scale); 
        
        g2.setColor(SELECTION_BORDER_COLOR);
        g2.setStroke(new BasicStroke(
        (float)SELECTION_STROKE.getLineWidth() / (float)scale));

        g2.drawRect(-w / 2, -h / 2, w, h);

        g2.setTransform(saveAT);
        g2.setStroke(oldStroke);
        g2.setColor(oldColor);
    }
    
    private void handleGesturePopUp(TJ tj, Rectangle bounds, TJPage targetPage) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Images", "png", "jpg", "jpeg"));

        if (chooser.showOpenDialog(tj.getCanvas2D()) == JFileChooser.APPROVE_OPTION) {
            try {
                File f = chooser.getSelectedFile();
                BufferedImage bImg = ImageIO.read(f);

                if (bImg != null) {
                    // Calculation: Scale based on the minimum dimension to fit inside the drawn box
                    double scaleX = (double) bounds.width / bImg.getWidth();
                    double scaleY = (double) bounds.height / bImg.getHeight();
                    double finalScale = Math.min(scaleX, scaleY);

                    // Position: Center of the gesture
                    TJImage newImg = new TJImage(bImg, (int)bounds.getCenterX(), 
                                                 (int)bounds.getCenterY(), finalScale);

                    targetPage.addImage(newImg);
                    this.mSelectedImage = newImg; // Automatically select it
                    tj.getCanvas2D().repaint();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}