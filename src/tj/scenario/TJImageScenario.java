package tj.scenario;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
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
import utils.TJNavPanel;
import x.XApp;
import x.XCmdToChangeScene;
import x.XScenario;

public class TJImageScenario extends XScenario {
    // constants
    private TJImage mSelectedImage = null;
    private Point mLastMousePt = null;
    
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
            mPressPoint = e.getPoint();
            
            // 1. check if user clicked an existing image (for selection)
            TJImageScenario scenario = (TJImageScenario)this.mScenario;
            TJPage[] pages = tj.getPageMgr().getCurPage();
            for (TJPage p : pages) {
                ArrayList<TJImage> imgs = p.getImages();
                for (int i = imgs.size() - 1; i >= 0; i--) {
                    if (imgs.get(i).contains(mPressPoint)) {
                        scenario.mSelectedImage = imgs.get(i);
                        scenario.mLastMousePt = mPressPoint;
//                        XCmdToChangeScene.execute(tj,
//                            TJImageScenario.ImageManipulateScene.getSingleton(),
//                            this);
                        return; // found image, don't start timer
                    }
                }
            }

            // 2. start long tap timer for adding new image
            ActionListener task = evt -> scenario.handleLongTap(tj, mPressPoint);
            mLongTapTimer = new Timer(LONG_TAP_DURATION, task);
            mLongTapTimer.setRepeats(false);
            mLongTapTimer.start();
        }

        @Override
        public void handleMouseDrag(MouseEvent e) {
            if (mLongTapTimer != null && mLongTapTimer.isRunning()) {
                if (mPressPoint.distance(e.getPoint()) > 10) {
                    mLongTapTimer.stop();
                }
            }
        }

        @Override
        public void handleMouseRelease(MouseEvent e) {
            if (mLongTapTimer != null && mLongTapTimer.isRunning()) {
                mLongTapTimer.stop();
            }
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
            
            TJPage[] curPage = tj.getPageMgr().getCurPage();
            if (curPage == null) return;
            
            scenario.drawPageStructure(g2, startX, startY, pageWidth, pageHeight);
            
            Shape oldClip = g2.getClip();
            g2.setClip(clipRect);
            
            if (curPage!= null) {
                // canvas.drawImages(g2, curPage[0].getImages());
                // canvas.drawImages(g2, curPage[1].getImages());
                
                canvas.drawPtCurves(g2, curPage[0].getPtCurves());
                canvas.drawSelectedPtCurves(g2, curPage[0].getSelectedPtCurves());

                canvas.drawPtCurves(g2, curPage[1].getPtCurves());
                canvas.drawSelectedPtCurves(g2, curPage[1].getSelectedPtCurves());

                canvas.drawCurPtCurve(g2);
            }
            
            g2.setClip(oldClip);
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
            if (scenario.mSelectedImage == null || scenario.mLastMousePt == null) return;

            Point curPt = e.getPoint();
            double dx = curPt.x - scenario.mLastMousePt.x;

            // Rotation proportional to horizontal drag (using radians)
            double rotateAmt = Math.toRadians(dx * 0.5); 
            scenario.mSelectedImage.rotate(rotateAmt);

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
        public void handleKeyUp(KeyEvent e) {
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
            TJImageScenario scenario = (TJImageScenario)this.mScenario;
            scenario.drawPageAndContent(g2);
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
        public void handleKeyUp(KeyEvent e) {
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
            TJImageScenario scenario = (TJImageScenario)this.mScenario;
            scenario.drawPageAndContent(g2);
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
        public void handleKeyUp(KeyEvent e) {
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
            TJImageScenario scenario = (TJImageScenario)this.mScenario;
            scenario.drawPageAndContent(g2);
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
    
    private void drawPageAndContent(Graphics2D g2) {
        TJ tj = (TJ)this.getApp();
        TJCanvas2D canvas = tj.getCanvas2D();

        Rectangle clipRect = this.getTotalPageBounds(tj);
        int startX = clipRect.x;
        int startY = clipRect.y;
        int pageWidth = clipRect.width / 2;
        int pageHeight = clipRect.height;

        TJPage[] curPage = tj.getPageMgr().getCurPage();
        if (curPage == null) return;

        this.drawPageStructure(g2, startX, startY, pageWidth, pageHeight);

        Shape oldClip = g2.getClip();
        g2.setClip(clipRect);

        if (curPage!= null) {
            // canvas.drawImages(g2, curPage[0].getImages());
            // canvas.drawImages(g2, curPage[1].getImages());

            canvas.drawPtCurves(g2, curPage[0].getPtCurves());
            canvas.drawSelectedPtCurves(g2, curPage[0].getSelectedPtCurves());

            canvas.drawPtCurves(g2, curPage[1].getPtCurves());
            canvas.drawSelectedPtCurves(g2, curPage[1].getSelectedPtCurves());

            canvas.drawCurPtCurve(g2);
        }

        g2.setClip(oldClip);
    }
    
    private void handleLongTap(TJ tj, Point pt) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Image");
        chooser.setFileFilter(new FileNameExtensionFilter("Images", "png",
            "jpg", "jpeg"));

        int res = chooser.showOpenDialog(tj.getCanvas2D());
        if (res == JFileChooser.APPROVE_OPTION) {
            try {
                File f = chooser.getSelectedFile();
                BufferedImage bImg = ImageIO.read(f);
                if (bImg != null) {
                    // Create image at the tapped point
                    // NOTE: Ensure pt is in World Coordinates if your system uses transform.
                    // Assuming pt is Screen, and we draw in World:
                    // Point2D worldPt = tj.getXform().screenToWorld(pt); 
                    // For now, assuming 1:1 or logic handles it:
                    int appHeight = tj.getCanvas2D().getHeight();
                    int pageHeight = (int)(appHeight * TJCanvas2D.PAGE_EDIT_HEIGHT_RATIO);
                    int pageWidth = (int)(pageHeight * TJCanvas2D.PAGE_ASPECT_RATIO);
                    
                    int targetWidth = (int)(0.6 * pageWidth);
                    int originalImageWidth = bImg.getWidth();
                    double scale = (double)targetWidth / (double)originalImageWidth;
                    
                    TJImage newImg = new TJImage(bImg, pt.x, pt.y, scale);

                    Rectangle totalBounds = this.getTotalPageBounds(tj);
                    int midX = totalBounds.x + totalBounds.width / 2;

                    TJPage[] curPages = tj.getPageMgr().getCurPage();
                    if (pt.x < midX) {
                        curPages[0].addImage(newImg);
                    } else {
                        curPages[1].addImage(newImg);
                    }

                    tj.getCanvas2D().repaint();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
