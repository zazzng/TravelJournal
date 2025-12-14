package tj.scenario;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import tj.TJ;
import tj.TJCanvas2D;
import tj.TJPage;
import tj.TJScene;
import tj.cmd.TJCmdToCreateCurPtCurve;
import utils.TJNavPanel;
import x.XApp;
import x.XCmdToChangeScene;
import x.XScenario;

public class TJDefaultScenario extends XScenario {
    // Page bounds
    private Rectangle mLeftPageBounds = null;
    public Rectangle getLeftPageBounds() {
        return this.mLeftPageBounds;
    }
    
    private Rectangle mRightPageBounds = null;
    public Rectangle getRightPageBounds() {
        return this.mRightPageBounds;
    }
    
    // singleton pattern
    private static TJDefaultScenario mSingleton = null;
    public static TJDefaultScenario getSingle() {
        assert(TJDefaultScenario.mSingleton != null);
        return TJDefaultScenario.mSingleton;
    }
    public static TJDefaultScenario createSingleton(XApp app) {
        assert(TJDefaultScenario.mSingleton == null);
        TJDefaultScenario.mSingleton = new TJDefaultScenario(app);
        return TJDefaultScenario.mSingleton;
    }
    private TJDefaultScenario(XApp app) {
        super(app);
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

    @Override
    protected void addScenes() {
        this.addScene(TJDefaultScenario.ReadyScene.createSingleton(this));
    }

    public static class ReadyScene extends TJScene {
        // UI Components
        private JPanel mTopNavPanel;
        private JPanel mBottomNavPanel;
        
        private Rectangle mLeftPageBounds = null;
        private Rectangle mRightPageBounds = null;
        private TJPage mTargetPage = null;

        // singleton pattern
        private static ReadyScene mSingleton = null;
        public static ReadyScene getSingleton() {
            assert(ReadyScene.mSingleton != null);
            return ReadyScene.mSingleton;
        }
        public static ReadyScene createSingleton(XScenario scenario) {
            assert(ReadyScene.mSingleton == null);
            ReadyScene.mSingleton = new ReadyScene(scenario);
            return ReadyScene.mSingleton;
        }
        private ReadyScene(XScenario scenario) {
            super(scenario);
        }
        
        public void initializeTopNav() {
            TJ tj = (TJ)this.mScenario.getApp();
            String title = "Untitled Journal";
            if (tj.getJournalBookMgr().getCurBook() != null) {
                title = tj.getJournalBookMgr().getCurBook().getTitle();
            }
            
            this.mTopNavPanel = TJNavPanel.createTopNavPanel(tj, title);
        }

        public void initializeBottomNav() {
            TJ tj = (TJ)this.mScenario.getApp();
            this.mBottomNavPanel = TJNavPanel.createBottomNavPanel(tj, this);
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
            TJDefaultScenario scenario = (TJDefaultScenario)this.mScenario;
            
            Rectangle leftBounds = scenario.getLeftPageBounds();
            if (leftBounds == null) return;
            
            int startX = leftBounds.x;
            int startY = leftBounds.y;
            int pageWidth = leftBounds.width;
            int pageHeight = leftBounds.height;

            TJPage[] curPage = tj.getPageMgr().getCurPage();
            if (curPage == null) return;
            
            drawPageStructure(g2, startX, startY, pageWidth, pageHeight);
            
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
            
            // restore clip
            g2.setClip(originalClip);
        }

        @Override public void handleMousePress(MouseEvent e) {}
        
        @Override public void handleMouseDrag(MouseEvent e) {}
        
        @Override public void handleMouseRelease(MouseEvent e) {}
        
        @Override public void handleKeyDown(KeyEvent e) {}
        
        @Override public void handleKeyUp(KeyEvent e) {}
        
        @Override 
        public void updateSupportObjects() {
            ((TJDefaultScenario)this.mScenario).updatePageBounds(
                (TJ)this.mScenario.getApp());
        }
        
        @Override public void renderScreenObjects(Graphics2D g2) {}
        
        @Override
        public void getReady() {
            TJ tj = (TJ)this.mScenario.getApp();
            
            // Auto-create a page if none exists
            if (tj.getPageMgr().getCurPage() == null) {
                tj.getPageMgr().addEmptyPage();
            }
            
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


        public void drawPageAndContent(Graphics2D g2, TJCanvas2D canvas, TJPage[] curPage, int startX, int startY,
            int pageWidth, int pageHeight) {
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
}