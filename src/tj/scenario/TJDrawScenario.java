package tj.scenario;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;
import tj.TJ;
import tj.TJCanvas2D;
import tj.TJPage;

import tj.TJScene;
import tj.cmd.TJCmdToAddCurPtCurveToPtCurves;
import tj.cmd.TJCmdToCreateCurPtCurve;
import tj.cmd.TJCmdToUpdateCurPtCurve;
import utils.TJNavPanel;
import x.XApp;
import x.XCmdToChangeScene;
import x.XScenario;

public class TJDrawScenario extends XScenario {
    private TJPage mTargetPage = null;
    private Rectangle mTargetBounds = null;
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

    private static TJDrawScenario mSingleton = null;
    public static TJDrawScenario getSingle() {
        assert(TJDrawScenario.mSingleton != null);
        return TJDrawScenario.mSingleton;
    }
    public static TJDrawScenario createSingleton(XApp app) {
        assert(TJDrawScenario.mSingleton == null);
        TJDrawScenario.mSingleton = new TJDrawScenario(app);
        return TJDrawScenario.mSingleton;
    }

    private TJDrawScenario(XApp app) {
        super(app);
    }
    
    private void updatePageBounds(TJ tj) {
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

    @Override
    protected void addScenes() {
        this.addScene(TJDrawScenario.DrawReadyScene.createSingleton(this));
        this.addScene(TJDrawScenario.DrawScene.createSingleton(this));
    }
    
    public static class DrawReadyScene extends TJScene {
        // UI components
        private JPanel mTopNavPanel;
        private JPanel mBottomNavPanel;
        
        private static DrawReadyScene mSingleton = null;
        public static DrawReadyScene getSingleton() {
            assert(DrawReadyScene.mSingleton != null);
            return DrawReadyScene.mSingleton;
        }
        public static DrawReadyScene createSingleton(XScenario scenario) {
            assert(DrawReadyScene.mSingleton == null);
            DrawReadyScene.mSingleton = new DrawReadyScene(scenario);
            return DrawReadyScene.mSingleton;
        }

        private DrawReadyScene(XScenario scenario) {
            super(scenario);
        }
        
        private void initializeTopNav() {
            TJ tj = (TJ)this.mScenario.getApp();
            this.mTopNavPanel = TJNavPanel.createTopNavPanel(tj);
        }
        
        private void initializeBottomNav() {
            TJ tj = (TJ)this.mScenario.getApp();
            this.mBottomNavPanel = TJNavPanel.createBottomNavPanel(tj, this);
        }

        @Override
        public void handleMousePress(MouseEvent e) {
            TJ tj = (TJ)this.mScenario.getApp();
            TJDrawScenario scenario = (TJDrawScenario)this.mScenario;
            Point pt = e.getPoint();
            
            Rectangle leftBounds = scenario.getLeftPageBounds();
            Rectangle rightBounds = scenario.getRightPageBounds();
            
            if (leftBounds.contains(pt)) {
                scenario.mTargetPage = tj.getPageMgr().getCurPage()[0];
                scenario.mTargetBounds = leftBounds;
            } else if (rightBounds.contains(pt)) {
                scenario.mTargetPage = tj.getPageMgr().getCurPage()[1];
                scenario.mTargetBounds = rightBounds;
            }
            if (scenario.mTargetPage != null) {
                TJCmdToCreateCurPtCurve.execute(tj, pt);
                XCmdToChangeScene.execute(tj,
                    TJDrawScenario.DrawScene.getSingleton(), this);
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
        }
        
        @Override
        public void handleKeyUp(KeyEvent e) {
        }
        
        @Override
        public void updateSupportObjects() {
            ((TJDrawScenario)this.mScenario).updatePageBounds(
                (TJ)this.mScenario.getApp());
        }
        
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
            TJDrawScenario scenario = (TJDrawScenario)this.mScenario;
            
            Rectangle leftBounds = scenario.getLeftPageBounds();
            
            int startX = leftBounds.x;
            int startY = leftBounds.y;
            int pageWidth = leftBounds.width;
            int pageHeight = leftBounds.height;

            TJPage[] curPage = tj.getPageMgr().getCurPage();
            if (curPage == null) return;
            
            scenario.drawPageAndContent(g2, canvas, curPage, startX, startY, pageWidth, pageHeight);
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
            
            ((TJDrawScenario)this.mScenario).updatePageBounds(
                (TJ)this.mScenario.getApp());
        }
        @Override
        public void wrapUp() {
            TJ tj = (TJ)this.mScenario.getApp();
            tj.setTopPanel(null);
            tj.setBottomPanel(null);
        }
    }

    public static class DrawScene extends TJScene {
        // UI components
        private JPanel mTopNavPanel;
        private JPanel mBottomNavPanel;
        
        private static DrawScene mSingleton = null;
        public static DrawScene getSingleton() {
            assert(DrawScene.mSingleton != null);
            return DrawScene.mSingleton;
        }
        public static DrawScene createSingleton(XScenario scenario) {
            assert(DrawScene.mSingleton == null);
            DrawScene.mSingleton = new DrawScene(scenario);
            return DrawScene.mSingleton;
        }

        private DrawScene(XScenario scenario) {
            super(scenario);
        }
        
        private void initializeTopNav() {
            TJ tj = (TJ)this.mScenario.getApp();
            this.mTopNavPanel = TJNavPanel.createTopNavPanel(tj);
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
            TJDrawScenario scenario = (TJDrawScenario)this.mScenario;
            
            Rectangle leftBounds = scenario.getLeftPageBounds();
            Rectangle rightBounds = scenario.getRightPageBounds();

            if (scenario.mTargetPage != null) {
                Rectangle totalBounds = new Rectangle(leftBounds.x, leftBounds.y, leftBounds.width + rightBounds.width, leftBounds.height);
                
                if (totalBounds.contains(e.getPoint())) {
                    TJCmdToUpdateCurPtCurve.execute(tj, e.getPoint());
                }
            }
        }
        
        @Override
        public void handleMouseRelease(MouseEvent e) {
            TJ tj = (TJ) this.mScenario.getApp();
            TJDrawScenario scenario = (TJDrawScenario)this.mScenario;
            
            if (scenario.mTargetPage != null) {
                TJCmdToAddCurPtCurveToPtCurves.execute(tj, scenario.mTargetPage);
            }
            
            scenario.mTargetPage = null;
            
            XCmdToChangeScene.execute(tj, this.mReturnScene, null);
        }
        @Override
        public void handleKeyDown(KeyEvent e) {
            // TODO Auto-generated method stub
        }
        @Override
        public void handleKeyUp(KeyEvent e) {
            // TODO Auto-generated method stub
        }
        @Override
        public void updateSupportObjects() {
            // TODO Auto-generated method stub
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
            TJDrawScenario scenario = (TJDrawScenario)this.mScenario;
            
            Rectangle leftBounds = scenario.getLeftPageBounds();
            
            int startX = leftBounds.x;
            int startY = leftBounds.y;
            int pageWidth = leftBounds.width;
            int pageHeight = leftBounds.height;

            TJPage[] curPage = tj.getPageMgr().getCurPage();
            if (curPage == null) return;
            
            scenario.drawPageStructure(g2, startX, startY, pageWidth, pageHeight);
            
            // clipping the pages
            Shape originalClip = g2.getClip();
            Rectangle totalPageArea = new Rectangle(startX, startY, pageWidth * 2, pageHeight);
            g2.setClip(totalPageArea);
            
            // draw content
            canvas.drawPtCurves(g2, curPage[0].getPtCurves());
            canvas.drawSelectedPtCurves(g2, curPage[0].getSelectedPtCurves());

            canvas.drawPtCurves(g2, curPage[1].getPtCurves());
            canvas.drawSelectedPtCurves(g2, curPage[1].getSelectedPtCurves());
            
            canvas.drawCurPtCurve(g2);
        }
        @Override
        public void renderScreenObjects(Graphics2D g2) {
            // TODO Auto-generated method stub
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
    
    private void drawPageAndContent(Graphics2D g2, TJCanvas2D canvas, TJPage[] curPage, int startX, int startY, int pageWidth, int pageHeight) {
        drawPageStructure(g2, startX, startY, pageWidth, pageHeight);
        
        canvas.drawPtCurves(g2, curPage[0].getPtCurves());
        canvas.drawSelectedPtCurves(g2, curPage[0].getSelectedPtCurves());
        
        canvas.drawCurPtCurve(g2);
        canvas.drawPtCurves(g2, curPage[1].getPtCurves());
        canvas.drawSelectedPtCurves(g2, curPage[1].getSelectedPtCurves());
    }
}
