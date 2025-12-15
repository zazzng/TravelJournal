package tj.scenario;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;

import javax.swing.JPanel;

import tj.TJ;
import tj.TJCanvas2D;
import tj.TJPage;
import tj.TJScene;
import tj.TJXform;
import tj.cmd.TJCmdToSetStartScreenPt;
import tj.cmd.TJCmdToTranslateTo;
import tj.cmd.TJCmdToZoomTo;
import utils.TJNavPanel;
import x.XApp;
import x.XCmdToChangeScene;
import x.XScenario;

public class TJNavigateScenario extends XScenario {
    private JPanel mTopNavPanel;
    private JPanel mBottomNavPanel;
    // singleton pattern
    private static TJNavigateScenario mSingleton = null;
    public static TJNavigateScenario getSingle() {
        assert(TJNavigateScenario.mSingleton != null);
        return TJNavigateScenario.mSingleton;
    }
    public static TJNavigateScenario createSingleton(XApp app) {
        assert(TJNavigateScenario.mSingleton == null);
        TJNavigateScenario.mSingleton = new TJNavigateScenario(app);
        return TJNavigateScenario.mSingleton;
    }
    private TJNavigateScenario(XApp app) {
        super(app);
    }

    @Override
    protected void addScenes() {
        this.addScene(TJNavigateScenario.PanReadyScene.createSingleton(this));
        this.addScene(TJNavigateScenario.PanScene.createSingleton(this));
        this.addScene(TJNavigateScenario.ZoomReadyScene.createSingleton(this));
        this.addScene(TJNavigateScenario.ZoomScene.createSingleton(this));
    }

    public static class PanReadyScene extends TJScene {
        // singleton pattern
        private static PanReadyScene mSingleton = null;
        public static PanReadyScene getSingleton() {
            assert(PanReadyScene.mSingleton != null);
            return PanReadyScene.mSingleton;
        }
        public static PanReadyScene createSingleton(XScenario scenario) {
            assert(PanReadyScene.mSingleton == null);
            PanReadyScene.mSingleton = new PanReadyScene(scenario);
            return PanReadyScene.mSingleton;
        }
        private PanReadyScene(XScenario scenario) {
            super(scenario);
        }

        @Override
        public void handleMousePress(MouseEvent e) {
            TJ tj = (TJ)this.mScenario.getApp();
            Point pt = e.getPoint();
            TJCmdToSetStartScreenPt.execute(tj, pt);
            
            XCmdToChangeScene.execute(tj,
                TJNavigateScenario.PanScene.getSingleton(),
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
                case KeyEvent.VK_CONTROL:
                    XCmdToChangeScene.execute(tj, this.getReturnScene(), null);
                    break;
            }
        }

        @Override
        public void updateSupportObjects() {
        }

        @Override
        public void renderWorldObjects(Graphics2D g2) {
            TJ tj = (TJ)this.mScenario.getApp();
            TJCanvas2D canvas = tj.getCanvas2D();
            TJNavigateScenario navigationScenario = (TJNavigateScenario)this.mScenario;
            TJDrawScenario scenario = (TJDrawScenario)this.mScenario;
            
            Rectangle leftBounds = scenario.getLeftPageBounds();
            
            int startX = leftBounds.x;
            int startY = leftBounds.y;
            int pageWidth = leftBounds.width;
            int pageHeight = leftBounds.height;

            TJPage[] curPage = tj.getJournalBookMgr().getCurPage();
            if (curPage == null) return;
            
            navigationScenario.drawPageStructure(g2, startX, startY, pageWidth, pageHeight);
            
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
        }

        @Override
        public void getReady() {
            TJ tj = (TJ)this.mScenario.getApp();
            TJNavigateScenario navigationScenario = (TJNavigateScenario)this.mScenario;
            
            if (navigationScenario.mTopNavPanel == null) {
                navigationScenario.initializeTopNav();
            }
            if (navigationScenario.mBottomNavPanel == null) {
                navigationScenario.initializeBottomNav();
            }
            
            tj.setTopPanel(navigationScenario.mTopNavPanel);
            tj.setBottomPanel(navigationScenario.mBottomNavPanel);
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
    
    public static class PanScene extends TJScene {
        // singleton pattern
        private static PanScene mSingleton = null;
        public static PanScene getSingleton() {
            assert(PanScene.mSingleton != null);
            return PanScene.mSingleton;
        }
        public static PanScene createSingleton(XScenario scenario) {
            assert(PanScene.mSingleton == null);
            PanScene.mSingleton = new PanScene(scenario);
            return PanScene.mSingleton;
        }
        private PanScene(XScenario scenario) {
            super(scenario);
        }

        @Override
        public void handleMousePress(MouseEvent e) {
        }

        @Override
        public void handleMouseDrag(MouseEvent e) {
            TJ tj = (TJ)this.mScenario.getApp();
            Point pt = e.getPoint();
            
            TJCmdToTranslateTo.execute(tj, pt);
        }

        @Override
        public void handleMouseRelease(MouseEvent e) {
            TJ tj = (TJ)this.mScenario.getApp();
            
            TJCmdToSetStartScreenPt.execute(tj, null);
            XCmdToChangeScene.execute(tj,
                TJNavigateScenario.PanReadyScene.getSingleton(),
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
                case KeyEvent.VK_CONTROL:
                    XCmdToChangeScene.execute(tj, this.getReturnScene(), null);
                    break;
            }
        }

        @Override
        public void updateSupportObjects() {
        }

        @Override
        public void renderWorldObjects(Graphics2D g2) {
            TJ tj = (TJ)this.mScenario.getApp();
            TJCanvas2D canvas = tj.getCanvas2D();
            TJNavigateScenario navigationScenario = (TJNavigateScenario)this.mScenario;
            TJDrawScenario scenario = (TJDrawScenario)this.mScenario;
            
            Rectangle leftBounds = scenario.getLeftPageBounds();
            
            int startX = leftBounds.x;
            int startY = leftBounds.y;
            int pageWidth = leftBounds.width;
            int pageHeight = leftBounds.height;

            TJPage[] curPage = tj.getJournalBookMgr().getCurPage();
            if (curPage == null) return;
            
            navigationScenario.drawPageStructure(g2, startX, startY, pageWidth, pageHeight);
            
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
            TJNavigateScenario scenario = (TJNavigateScenario) this.mScenario;
            scenario.drawPanCrossHair(g2);
        }

        @Override
        public void getReady() {
            TJ tj = (TJ)this.mScenario.getApp();
            TJNavigateScenario navigationScenario = (TJNavigateScenario)this.mScenario;
            
            if (navigationScenario.mTopNavPanel == null) {
                navigationScenario.initializeTopNav();
            }
            if (navigationScenario.mBottomNavPanel == null) {
                navigationScenario.initializeBottomNav();
            }
            
            tj.setTopPanel(navigationScenario.mTopNavPanel);
            tj.setBottomPanel(navigationScenario.mBottomNavPanel);
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
    
    public static class ZoomReadyScene extends TJScene {
        // singleton pattern
        private static ZoomReadyScene mSingleton = null;
        public static ZoomReadyScene getSingleton() {
            assert(ZoomReadyScene.mSingleton != null);
            return ZoomReadyScene.mSingleton;
        }
        public static ZoomReadyScene createSingleton(XScenario scenario) {
            assert(ZoomReadyScene.mSingleton == null);
            ZoomReadyScene.mSingleton = new ZoomReadyScene(scenario);
            return ZoomReadyScene.mSingleton;
        }
        private ZoomReadyScene(XScenario scenario) {
            super(scenario);
        }

        @Override
        public void handleMousePress(MouseEvent e) {
            TJ tj = (TJ)this.mScenario.getApp();
            Point pt = e.getPoint();
            TJCmdToSetStartScreenPt.execute(tj, pt);
            
            XCmdToChangeScene.execute(tj,
                TJNavigateScenario.ZoomScene.getSingleton(),
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
                case KeyEvent.VK_ALT:
                    XCmdToChangeScene.execute(tj, this.getReturnScene(), null);
                    break;
            }
        }

        @Override
        public void updateSupportObjects() {
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

            TJPage[] curPage = tj.getJournalBookMgr().getCurPage();
            if (curPage == null) return;
            
            ((TJNavigateScenario)this.mScenario).drawPageStructure(g2, startX, startY, pageWidth, pageHeight);
            
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
            
        }

        @Override
        public void getReady() {
            TJ tj = (TJ)this.mScenario.getApp();
            TJNavigateScenario navigationScenario = (TJNavigateScenario)this.mScenario;
            
            if (navigationScenario.mTopNavPanel == null) {
                navigationScenario.initializeTopNav();
            }
            if (navigationScenario.mBottomNavPanel == null) {
                navigationScenario.initializeBottomNav();
            }
            
            tj.setTopPanel(navigationScenario.mTopNavPanel);
            tj.setBottomPanel(navigationScenario.mBottomNavPanel);
        }

        @Override
        public void wrapUp() {TJ tj = (TJ)this.mScenario.getApp();
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
    
    public static class ZoomScene extends TJScene {
        // singleton pattern
        private static ZoomScene mSingleton = null;
        public static ZoomScene getSingleton() {
            assert(ZoomScene.mSingleton != null);
            return ZoomScene.mSingleton;
        }
        public static ZoomScene createSingleton(XScenario scenario) {
            assert(ZoomScene.mSingleton == null);
            ZoomScene.mSingleton = new ZoomScene(scenario);
            return ZoomScene.mSingleton;
        }
        private ZoomScene(XScenario scenario) {
            super(scenario);
        }

        @Override
        public void handleMousePress(MouseEvent e) {
        }

        @Override
        public void handleMouseDrag(MouseEvent e) {
            TJ tj = (TJ)this.mScenario.getApp();
            Point pt = e.getPoint();
            
            TJCmdToZoomTo.execute(tj, pt);
        }

        @Override
        public void handleMouseRelease(MouseEvent e) {
            TJ tj = (TJ)this.mScenario.getApp();
            
            TJCmdToSetStartScreenPt.execute(tj, null);
            XCmdToChangeScene.execute(tj,
                TJNavigateScenario.ZoomReadyScene.getSingleton(),
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
                case KeyEvent.VK_ALT:
                    XCmdToChangeScene.execute(tj, this.getReturnScene(), null);
                    break;
            }
        }

        @Override
        public void updateSupportObjects() {
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

            TJPage[] curPage = tj.getJournalBookMgr().getCurPage();
            if (curPage == null) return;
            
            ((TJNavigateScenario)this.mScenario).drawPageStructure(g2, startX, startY, pageWidth, pageHeight);
            
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
            
        }

        @Override
        public void getReady() {
            TJ tj = (TJ)this.mScenario.getApp();
            TJNavigateScenario navigationScenario = (TJNavigateScenario)this.mScenario;
            
            if (navigationScenario.mTopNavPanel == null) {
                navigationScenario.initializeTopNav();
            }
            if (navigationScenario.mBottomNavPanel == null) {
                navigationScenario.initializeBottomNav();
            }
            
            tj.setTopPanel(navigationScenario.mTopNavPanel);
            tj.setBottomPanel(navigationScenario.mBottomNavPanel);
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
    
    private void drawPanCrossHair(Graphics2D g2) {
        TJ tj = (TJ) this.mApp;
        Point penPt = tj.getPenMarkMgr().getLastPenMark().getLastPt();
        
        Line2D hline = new Line2D.Double(0.0, penPt.y,
            tj.getCanvas2D().getWidth(), penPt.y);
        Line2D vline = new Line2D.Double(penPt.x, 0.0, penPt.x, tj.getCanvas2D().getHeight());
        
        g2.setColor(TJCanvas2D.COLOR_CROSS_HAIR);
        g2.setStroke(TJCanvas2D.STROKE_CROSS_HAIR);
        g2.draw(vline);
        g2.draw(hline);
    }

    public void drawPageStructure(Graphics2D g2, int startX, int startY, int pageWidth, int pageHeight) {
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
    
    public void drawPageAndContent(Graphics2D g2, TJCanvas2D canvas, TJPage[] curPage, int startX, int startY, int pageWidth, int pageHeight) {
        this.drawPageStructure(g2, startX, startY, pageWidth, pageHeight);
        
        canvas.drawPtCurves(g2, curPage[0].getPtCurves());
        canvas.drawSelectedPtCurves(g2, curPage[0].getSelectedPtCurves());
        
        canvas.drawCurPtCurve(g2);
        canvas.drawPtCurves(g2, curPage[1].getPtCurves());
        canvas.drawSelectedPtCurves(g2, curPage[1].getSelectedPtCurves());
    }

    public void initializeTopNav() {
            TJ tj = (TJ)this.mApp;
            String title = "Untitled Journal";
            if (tj.getJournalBookMgr().getCurBook() != null) {
                title = tj.getJournalBookMgr().getCurBook().getTitle();
            }
            
            this.mTopNavPanel = TJNavPanel.createTopNavPanel(tj, title);
        }
        
    public void initializeBottomNav() {
        TJ tj = (TJ)this.mApp;
        this.mBottomNavPanel = TJNavPanel.createBottomNavPanel(tj, null);
    }
}
